package com.hcdc.capstone.taskprocess;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hcdc.capstone.BaseActivity;
import com.hcdc.capstone.R;

import java.util.HashMap;
import java.util.Map;

public class TaskProgress extends BaseActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private Button startButton, doneButton, submitButton;
    private ImageButton cancelButton;
    private ImageView uploadButton;
    private TextView timerTextView;

    private static final int IMAGE_PICK_REQUEST_CODE = 100;
    private String currentUserUID;
    private boolean timerRunning = false;
    private long startTime = 0L;
    private long taskDurationMillis;
    private Handler handler = new Handler();
    private Uri selectedImageUri;

    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            long remainingMillis = taskDurationMillis - millis;
            if (remainingMillis <= 0) {
                timerTextView.setText("Time's up!");
                timerRunning = false;
                doneButton.setVisibility(View.VISIBLE);
            } else {
                int seconds = (int) (remainingMillis / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;
                int hours = minutes / 60;
                minutes = minutes % 60;

                timerTextView.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));

                if (timerRunning) {
                    handler.postDelayed(this, 1000);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer_test);

        startButton = findViewById(R.id.startButton);
        doneButton = findViewById(R.id.doneButton);
        timerTextView = findViewById(R.id.timerTextView);
        TextView taskNameTextView = findViewById(R.id.taskTitle4);
        TextView taskPointsTextView = findViewById(R.id.taskPoint);
        TextView taskDescriptionTextView = findViewById(R.id.taskDesc);
        TextView taskLocationTextView = findViewById(R.id.taskLocation);
        TextView taskTimeFrameTextView = findViewById(R.id.taskTimeFrame);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        String taskName = getIntent().getStringExtra("taskName");
        String taskPoints = getIntent().getStringExtra("taskPoints");
        String taskDescription = getIntent().getStringExtra("taskDescription");
        String taskLocation = getIntent().getStringExtra("taskLocation");
        currentUserUID = auth.getCurrentUser().getUid();

        int timeFrameHours = getIntent().getIntExtra("timeFrameHours", 0);
        int timeFrameMinutes = getIntent().getIntExtra("timeFrameMinutes", 0);

        taskNameTextView.setText(taskName);
        taskPointsTextView.setText(taskPoints);
        taskDescriptionTextView.setText(taskDescription);
        taskLocationTextView.setText(taskLocation);

        long timeFrameMillis = (timeFrameHours * 60 + timeFrameMinutes) * 60 * 1000;
        taskDurationMillis = timeFrameMillis;

        int initialHours = timeFrameHours;
        int initialMinutes = timeFrameMinutes;
        int initialSeconds = (int) ((timeFrameMillis % (60 * 1000)) / 1000);
        timerTextView.setText(String.format("%02d:%02d:%02d", initialHours, initialMinutes, initialSeconds));

        String taskTimeFrame = timeFrameHours + " hours " + timeFrameMinutes + " minutes";
        taskTimeFrameTextView.setText(taskTimeFrame);

        View tasksubmitCustomDialog = LayoutInflater.from(TaskProgress.this).inflate(R.layout.tasksubmit_dialog, null);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(TaskProgress.this);
        alertDialog.setView(tasksubmitCustomDialog);
        cancelButton = tasksubmitCustomDialog.findViewById(R.id.closeSubmission);
        submitButton = tasksubmitCustomDialog.findViewById(R.id.taskSubmission);
        uploadButton = tasksubmitCustomDialog.findViewById(R.id.upload_submission);
        final AlertDialog dialog = alertDialog.create();

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, IMAGE_PICK_REQUEST_CODE);
            }
        });


        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.collection("user_acceptedTask")
                        .whereEqualTo("acceptedBy", currentUserUID)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                if (!queryDocumentSnapshots.isEmpty()) {
                                    DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                                    String documentId = documentSnapshot.getId();
                                    Map<String, Object> acceptedTaskData = documentSnapshot.getData();

                                    db.collection("user_acceptedTask")
                                            .document(documentId)
                                            .update("isCompleted", true)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    long remainingMillis = taskDurationMillis - (System.currentTimeMillis() - startTime);
                                                    int remainingSeconds = (int) (remainingMillis / 1000);
                                                    int remainingMinutes = remainingSeconds / 60;
                                                    remainingSeconds = remainingSeconds % 60;
                                                    int remainingHours = remainingMinutes / 60;
                                                    remainingMinutes = remainingMinutes % 60;

                                                    String remainingTime = String.format("%02d:%02d:%02d", remainingHours, remainingMinutes, remainingSeconds);

                                                    acceptedTaskData.put("isCompleted", true);
                                                    acceptedTaskData.put("remainingTime", remainingTime);

                                                    if (selectedImageUri != null) {
                                                        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                                                        StorageReference imageRef = storageRef.child("images/" + currentUserUID + "_" + System.currentTimeMillis());

                                                        imageRef.putFile(selectedImageUri)
                                                                .addOnSuccessListener(taskSnapshot -> {
                                                                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                                                        String imageUrl = uri.toString();
                                                                        storeImageUrlInFirestore(imageUrl); // Store the URL in Firestore

                                                                        // Update the acceptedTaskData with the image URL
                                                                        acceptedTaskData.put("imageUrl", imageUrl);

                                                                        // Update the Firestore document
                                                                        db.collection("completed_task")
                                                                                .add(acceptedTaskData)
                                                                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                                                    @Override
                                                                                    public void onSuccess(DocumentReference documentReference) {
                                                                                        db.collection("user_acceptedTask")
                                                                                                .document(documentId)
                                                                                                .delete()
                                                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onSuccess(Void aVoid) {
                                                                                                        Intent intent = new Intent(TaskProgress.this, taskFragment.class);
                                                                                                        startActivity(intent);
                                                                                                        finish();
                                                                                                    }
                                                                                                })
                                                                                                .addOnFailureListener(new OnFailureListener() {
                                                                                                    @Override
                                                                                                    public void onFailure(@NonNull Exception e) {
                                                                                                        // Handle failure
                                                                                                    }
                                                                                                });
                                                                                    }
                                                                                })
                                                                                .addOnFailureListener(new OnFailureListener() {
                                                                                    @Override
                                                                                    public void onFailure(@NonNull Exception e) {
                                                                                        // Handle failure
                                                                                    }
                                                                                });
                                                                    });
                                                                })
                                                                .addOnFailureListener(e -> {
                                                                    // Handle image upload failure
                                                                });
                                                    } else {
                                                        // No image selected
                                                        db.collection("completed_task")
                                                                .add(acceptedTaskData)
                                                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                                    @Override
                                                                    public void onSuccess(DocumentReference documentReference) {
                                                                        db.collection("user_acceptedTask")
                                                                                .document(documentId)
                                                                                .delete()
                                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                    @Override
                                                                                    public void onSuccess(Void aVoid) {
                                                                                        Intent intent = new Intent(TaskProgress.this, taskFragment.class);
                                                                                        startActivity(intent);
                                                                                        finish();
                                                                                    }
                                                                                })
                                                                                .addOnFailureListener(new OnFailureListener() {
                                                                                    @Override
                                                                                    public void onFailure(@NonNull Exception e) {
                                                                                        // Handle failure
                                                                                    }
                                                                                });
                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        // Handle failure
                                                                    }
                                                                });
                                                    }
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    // Handle failure
                                                }
                                            });
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Handle failure
                            }
                        });
            }
        });


        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!timerRunning) {
                    timerRunning = true;
                    startTime = System.currentTimeMillis();
                    handler.postDelayed(timerRunnable, 0);

                    startButton.setVisibility(View.GONE);
                    doneButton.setVisibility(View.VISIBLE);

                    db.collection("user_acceptedTask")
                            .whereEqualTo("acceptedBy", currentUserUID)
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                    if (!queryDocumentSnapshots.isEmpty()) {
                                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                                        String documentId = documentSnapshot.getId();

                                        db.collection("user_acceptedTask")
                                                .document(documentId)
                                                .update("isStarted", true)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        // isStarted field updated successfully
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        // Handle failure
                                                    }
                                                });
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Handle failure
                                }
                            });
                }
            }
        });

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });
    }

    private void storeImageUrlInFirestore(String imageUrl) {
        db.collection("images")
                .add(new HashMap<String, Object>() {{
                    put("imageUrl", imageUrl);
                }})
                .addOnSuccessListener(documentReference -> {
                    // Image URL stored successfully in Firestore
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_PICK_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
        }
    }

    @Override
    public void onBackPressed() {
        if (timerRunning) {
            Toast.makeText(this, "Task is in progress. Cannot go back.", Toast.LENGTH_SHORT).show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        timerRunning = false;
        handler.removeCallbacks(timerRunnable);
    }
}
