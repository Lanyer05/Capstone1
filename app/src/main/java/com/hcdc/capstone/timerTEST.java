package com.hcdc.capstone;

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
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class timerTEST extends AppCompatActivity {



    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private Button startButton, doneButton , submitButton;
    private ImageButton cancelButton;
    private ImageView uploadButton;
    private TextView timerTextView;

    private static final int IMAGE_PICK_REQUEST_CODE = 100;
    private String currentUserUID;
    private boolean timerRunning = false;
    private long startTime = 0L;
    private long taskDurationMillis; // Task duration in milliseconds
    private Handler handler = new Handler();

    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            long remainingMillis = taskDurationMillis - millis;
            if (remainingMillis <= 0) {
                // Time's up, mark the task as done or handle it as needed
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

        // Convert time frame data into milliseconds
        long timeFrameMillis = (timeFrameHours * 60 + timeFrameMinutes) * 60 * 1000;

        // Store the task duration in milliseconds
        taskDurationMillis = timeFrameMillis;

        // Set the initial time on the timerTextView
        int initialHours = timeFrameHours;
        int initialMinutes = timeFrameMinutes;
        int initialSeconds = (int) ((timeFrameMillis % (60 * 1000)) / 1000);
        timerTextView.setText(String.format("%02d:%02d:%02d", initialHours, initialMinutes, initialSeconds));

        // Set the task time frame
        String taskTimeFrame = timeFrameHours + " hours " + timeFrameMinutes + " minutes";
        taskTimeFrameTextView.setText(taskTimeFrame);

        //Task Submission Dialog Box
        View tasksubmitCustomDialog = LayoutInflater.from(timerTEST.this).inflate(R.layout.tasksubmit_dialog,null);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(timerTEST.this);

        alertDialog.setView(tasksubmitCustomDialog);
        cancelButton = tasksubmitCustomDialog.findViewById(R.id.closeSubmission);
        submitButton = tasksubmitCustomDialog.findViewById(R.id.taskSubmission);
        uploadButton = tasksubmitCustomDialog.findViewById(R.id.upload_submission);

        final AlertDialog dialog = alertDialog.create();

        cancelButton.setOnClickListener(v -> dialog.cancel());

        uploadButton.setOnClickListener(v -> {
            // Open an image picker to select an image
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, IMAGE_PICK_REQUEST_CODE);
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Update isCompleted field to true in the user_acceptedTask table
                db.collection("user_acceptedTask")
                        .whereEqualTo("acceptedBy", currentUserUID)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                if (!queryDocumentSnapshots.isEmpty()) {
                                    // Assuming there's only one document per user
                                    DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                                    String documentId = documentSnapshot.getId();
                                    Map<String, Object> acceptedTaskData = documentSnapshot.getData();

                                    // Update the isCompleted field
                                    db.collection("user_acceptedTask")
                                            .document(documentId)
                                            .update("isCompleted", true)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    // Document updated to isCompleted=true

                                                    // Calculate remaining time in "hh:mm:ss" format
                                                    long remainingMillis = taskDurationMillis - (System.currentTimeMillis() - startTime);
                                                    int remainingSeconds = (int) (remainingMillis / 1000);
                                                    int remainingMinutes = remainingSeconds / 60;
                                                    remainingSeconds = remainingSeconds % 60;
                                                    int remainingHours = remainingMinutes / 60;
                                                    remainingMinutes = remainingMinutes % 60;

                                                    String remainingTime = String.format("%02d:%02d:%02d", remainingHours, remainingMinutes, remainingSeconds);

                                                    // Add the data to completed_task collection with isCompleted set to true
                                                    acceptedTaskData.put("isCompleted", true);
                                                    acceptedTaskData.put("remainingTime", remainingTime); // Store the remaining time

                                                    db.collection("completed_task")
                                                            .add(acceptedTaskData)
                                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                                @Override
                                                                public void onSuccess(DocumentReference documentReference) {
                                                                    // Data added to completed_task, delete the document
                                                                    db.collection("user_acceptedTask")
                                                                            .document(documentId)
                                                                            .delete()
                                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void aVoid) {
                                                                                    // Document deleted, finish the activity
                                                                                    Intent intent = new Intent(timerTEST.this,taskFragment.class);
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

                    // Update the isStarted field in the user_acceptedTask document
                    db.collection("user_acceptedTask")
                            .whereEqualTo("acceptedBy", currentUserUID)
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                    if (!queryDocumentSnapshots.isEmpty()) {
                                        // Assuming there's only one document per user
                                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                                        String documentId = documentSnapshot.getId();

                                        // Update the isStarted field
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
        // Assuming you have a "images" collection in your Firestore database
        db.collection("images")
                .add(new HashMap<String, Object>() {{
                    put("imageUrl", imageUrl);
                }})
                .addOnSuccessListener(documentReference -> {
                    // Image URL stored successfully in Firestore
                    // You can add any further logic here if needed
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                });
    }

    // Add this method for handling the result of the image picker
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_PICK_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            // Get the selected image URI
            Uri imageUri = data.getData();

            // Upload the image to Firebase Storage
            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            StorageReference imageRef = storageRef.child("images/" + currentUserUID + "_" + System.currentTimeMillis());

            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Image uploaded successfully, get the download URL
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            // Now you can store the imageUrl along with other data in Firestore
                            // Call a method to store this imageUrl in your Firestore database
                            storeImageUrlInFirestore(imageUrl);
                        });
                    })
                    .addOnFailureListener(e -> {
                        // Handle image upload failure
                    });
        }
    }
    @Override
    public void onBackPressed() {
        if (timerRunning) {
            // If the timer is running, show a message to the user that they can't go back
            // or handle it in any way you prefer
            Toast.makeText(this, " Task is in progress. Cannot go back. ", Toast.LENGTH_SHORT).show();
        } else {
            // If the timer is not running, allow the default back behavior
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
