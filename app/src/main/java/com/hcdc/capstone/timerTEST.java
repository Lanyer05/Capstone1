package com.hcdc.capstone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Map;

public class timerTEST extends AppCompatActivity {



    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private Button startButton;
    private Button doneButton;
    private TextView timerTextView;
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
        String currentUserUID = auth.getCurrentUser().getUid();

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

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!timerRunning) {
                    timerRunning = true;
                    startTime = System.currentTimeMillis();
                    handler.postDelayed(timerRunnable, 0);
                }
            }
        });

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Mark the task as done in the database or handle it as needed
                // ...

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

                                                    // Add the data to completed_task collection with isCompleted set to true
                                                    acceptedTaskData.put("isCompleted", true);
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

    }



    @Override
    protected void onStop() {
        super.onStop();
        timerRunning = false;
        handler.removeCallbacks(timerRunnable);
    }

}
