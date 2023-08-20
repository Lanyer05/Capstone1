package com.hcdc.capstone;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

public class TaskDetails extends BaseActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private TextView tskTitle, tskPoint, tskDesc, tskLoc, tskDura;

    private String uID, userEmail;

    Button acceptTask, cancelTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_details);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        tskTitle = findViewById(R.id.tdTitle);
        tskDesc = findViewById(R.id.tdDesc);
        tskPoint = findViewById(R.id.tdPoints);
        tskLoc = findViewById(R.id.tdLocation);
        tskDura = findViewById(R.id.tdDuration);

        acceptTask = findViewById(R.id.tdAccept);
        cancelTask = findViewById(R.id.tdCancel);

        Bundle extra = getIntent().getExtras();
        String tTitle = extra.getString("tasktitle");
        String tDesc = extra.getString("taskdetails");
        String tPoints = extra.getString("taskpoint");
        String tLoc = extra.getString("tasklocation");
        String tDura = extra.getString("taskDuration");

        tskTitle.setText(tTitle);
        tskDesc.setText(tDesc);
        tskLoc.setText(tLoc);
        tskPoint.setText(tPoints);
        tskDura.setText(tDura);

        acceptTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uID = auth.getCurrentUser().getUid();

                // Check if the user has already accepted a task
                firestore.collection("user_acceptedTask")
                        .whereEqualTo("acceptedBy", uID)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (queryDocumentSnapshots.isEmpty()) {
                                // User hasn't accepted any task, show confirmation overlay
                                showAcceptConfirmationOverlay();
                            } else {
                                // User has already accepted a task, show a message
                                Log.d(TAG, "User has already accepted a task.");
                                Toast.makeText(TaskDetails.this, "You have already accepted a task. Please finish it before accepting a new task.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error checking accepted tasks", e);
                        });
            }
        });

        cancelTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Task.class);
                startActivity(intent);
                finish();
            }
        });
    }

    // Function to handle accepting a new task
    private void acceptNewTask() {
        // Create a batch object
        WriteBatch batch = firestore.batch();

        // Update the corresponding task's isAccepted field in the "tasks" collection
        firestore.collection("tasks")
                .whereEqualTo("taskName", tskTitle.getText().toString())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);

                        // Update the isAccepted field using the batch
                        batch.update(documentSnapshot.getReference(), "isAccepted", true);

                        // Store accepted task details in user_acceptedTask collection
                        Map<String, Object> userTaskAccepted = new HashMap<>();
                        userTaskAccepted.put("taskName", tskTitle.getText().toString());
                        userTaskAccepted.put("description", tskDesc.getText().toString());
                        userTaskAccepted.put("location", tskLoc.getText().toString());
                        userTaskAccepted.put("points", tskPoint.getText().toString());
                        userTaskAccepted.put("isAccepted", true);
                        userTaskAccepted.put("acceptedBy", uID);
                        userTaskAccepted.put("acceptedByEmail", userEmail);

                        if (documentSnapshot.contains("timeFrame")) {
                            userTaskAccepted.put("timeFrame", documentSnapshot.get("timeFrame"));
                        }

                        batch.delete(documentSnapshot.getReference());

                        // Add the userTaskAccepted document to the batch
                        batch.set(firestore.collection("user_acceptedTask").document(), userTaskAccepted);

                        // Commit the batch
                        batch.commit()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Batch write successful");

                                    // After batch write, you can update your UI or take further actions
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Batch write failed", e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting tasks for update", e);
                });
    }

    // Show the confirmation overlay for accepting a task
    private void showAcceptConfirmationOverlay() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View overlayView = getLayoutInflater().inflate(R.layout.accept_confirmation_overlay, null);
        Button confirmButton = overlayView.findViewById(R.id.confirmButton);
        Button cancelButton = overlayView.findViewById(R.id.cancelButton);

        builder.setView(overlayView);
        AlertDialog dialog = builder.create();
        dialog.show();

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // User confirmed, proceed with accepting the task
                dialog.dismiss();
                userEmail = auth.getCurrentUser().getEmail();
                acceptNewTask();

                Intent intent = new Intent(getApplicationContext(), Task.class);
                intent.putExtra("navigateToMyTasks", true);
                startActivity(intent);
                finish();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // User canceled, dismiss the dialog
                dialog.dismiss();
            }
        });
    }
}
