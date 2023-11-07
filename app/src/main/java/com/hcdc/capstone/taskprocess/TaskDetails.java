package com.hcdc.capstone.taskprocess;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hcdc.capstone.BaseActivity;
import com.hcdc.capstone.R;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskDetails extends BaseActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private TextView tskTitle, tskPoint, tskDesc, tskLoc, tskDura, tskMaxUser;

    private String uID, userEmail;

    private Button acceptTask, cancelTask;

    @SuppressLint("SetTextI18n")
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
        tskMaxUser = findViewById(R.id.tdMaxUser);
        acceptTask = findViewById(R.id.tdAccept);
        cancelTask = findViewById(R.id.tdCancel);

        Bundle extra = getIntent().getExtras();
        String tTitle = extra.getString("tasktitle");
        String tDesc = extra.getString("taskdetails");
        String tPoints = extra.getString("taskpoint");
        String tLoc = extra.getString("tasklocation");
        String tDura = extra.getString("taskDuration");
        String tMaxUser = extra.getString("taskMaxUser");

        tskTitle.setText(tTitle);
        tskDesc.setText(tDesc);
        tskLoc.setText(tLoc);
        tskPoint.setText(tPoints);
        tskDura.setText(tDura);

        displayAcceptedUserRatio(tMaxUser);

        acceptTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uID = auth.getCurrentUser().getUid();

                // Check if the user has already accepted the task
                firestore.collection("user_acceptedTask")
                        .whereEqualTo("acceptedBy", uID)
                        .whereEqualTo("taskName", tskTitle.getText().toString()) // Add this condition to check for the task
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                // User has already accepted the task
                                Toast.makeText(TaskDetails.this, " You have already accepted this task. ", Toast.LENGTH_SHORT).show();
                            } else {
                                // Before showing the confirmation overlay, check if the task has reached its max user limit
                                firestore.collection("tasks")
                                        .whereEqualTo("taskName", tskTitle.getText().toString())
                                        .get()
                                        .addOnSuccessListener(queryDocumentSnapshots1 -> {
                                            if (!queryDocumentSnapshots1.isEmpty()) {
                                                DocumentSnapshot documentSnapshot = queryDocumentSnapshots1.getDocuments().get(0);
                                                List<String> acceptedByUsers = (List<String>) documentSnapshot.get("acceptedByUsers");
                                                Long maxUsers = documentSnapshot.getLong("maxUsers"); // Retrieve maxUsers as a Long

                                                if (acceptedByUsers != null && maxUsers != null) {
                                                    int currentAcceptedUsers = acceptedByUsers.size();

                                                    if (currentAcceptedUsers >= maxUsers.intValue()) {
                                                        Toast.makeText(TaskDetails.this, " Task is full. Cannot be accepted. ", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        showAcceptConfirmationOverlay();
                                                    }
                                                }
                                            }
                                        });
                            }
                        });
            }
        });

        cancelTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void displayAcceptedUserRatio(String tMaxUser) {
        firestore.collection("tasks")
                .whereEqualTo("taskName", tskTitle.getText().toString())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        List<String> acceptedByUsers = (List<String>) documentSnapshot.get("acceptedByUsers");
                        if (acceptedByUsers != null) {
                            int currentAcceptedUsers = acceptedByUsers.size();
                            Long maxUsers = documentSnapshot.getLong("maxUsers");
                            String ratioText = currentAcceptedUsers + "/" + maxUsers;
                            tskMaxUser.setText("Max User: " + ratioText);
                        } else {
                            tskMaxUser.setText("Max User: N/A");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                });
    }

    private void acceptNewTask() {
        uID = auth.getCurrentUser().getUid();
        firestore.collection("tasks")
                .whereEqualTo("taskName", tskTitle.getText().toString())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        Long maxUsers = documentSnapshot.getLong("maxUsers"); // Retrieve maxUsers as a Long
                        String camera = documentSnapshot.getString("camera");
                        if (maxUsers != null) {
                            List<String> acceptedByUsers = (List<String>) documentSnapshot.get("acceptedByUsers");
                            if (acceptedByUsers != null) {
                                int currentAcceptedUsers = acceptedByUsers.size();

                                if (currentAcceptedUsers < maxUsers.intValue()) {
                                    firestore.collection("tasks")
                                            .document(documentSnapshot.getId())
                                            .update("isAccepted", true);
                                    acceptedByUsers.add(uID);
                                    firestore.collection("tasks")
                                            .document(documentSnapshot.getId())
                                            .update("acceptedByUsers", acceptedByUsers);

                                    // Create a new document in "user_acceptedTask" for the accepting user
                                    Map<String, Object> userTaskAccepted = new HashMap<>();
                                    userTaskAccepted.put("taskName", tskTitle.getText().toString());
                                    userTaskAccepted.put("description", tskDesc.getText().toString());
                                    userTaskAccepted.put("location", tskLoc.getText().toString());
                                    userTaskAccepted.put("points", tskPoint.getText().toString());
                                    userTaskAccepted.put("isAccepted", true);
                                    userTaskAccepted.put("isStarted", false);
                                    userTaskAccepted.put("isCompleted", false);
                                    userTaskAccepted.put("isConfirmed", false);
                                    userTaskAccepted.put("acceptedBy", uID);
                                    userTaskAccepted.put("acceptedByEmail", userEmail);
                                    userTaskAccepted.put("maxUsers", maxUsers); // Store maxUsers as a Long
                                    userTaskAccepted.put("camera", camera);

                                    if (documentSnapshot.contains("timeFrame")) {
                                        userTaskAccepted.put("timeFrame", documentSnapshot.get("timeFrame"));
                                    }
                                    long currentTimeMillis = System.currentTimeMillis();
                                    String formattedDate = formatDateTime(currentTimeMillis);
                                    userTaskAccepted.put("acceptedDateTime", formattedDate);
                                    firestore.collection("user_acceptedTask").add(userTaskAccepted)
                                            .addOnSuccessListener(documentReference -> {
                                                Log.d(TAG, "Task accepted and document created in user_acceptedTask: " + documentReference.getId());
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e(TAG, "Error creating a document in user_acceptedTask", e);
                                            });
                                } else {
                                    Log.d(TAG, "Task cannot be accepted due to users limitation.");
                                    Toast.makeText(TaskDetails.this, " Task cannot be accepted due to maxUsers limitation ", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.e(TAG, "The 'acceptedByUsers' field is not properly initialized");
                                Toast.makeText(TaskDetails.this, " Task cannot be accepted at the moment ", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e(TAG, "maxUsers is not a valid integer");
                            Toast.makeText(TaskDetails.this, "Task cannot be accepted due to maxUsers format issue", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting tasks for update", e);
                });
    }

    private String formatDateTime(long timestamp) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a MM/dd/yy");
        Date date = new Date(timestamp);
        return sdf.format(date);
    }

    private void showAcceptConfirmationOverlay() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View overlayView = getLayoutInflater().inflate(R.layout.accept_confirmation_overlay, null);
        Button confirmButton = overlayView.findViewById(R.id.confirmButton);
        Button cancelButton = overlayView.findViewById(R.id.cancelButton);
        builder.setView(overlayView);
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                dialog.dismiss();
            }
        });
    }
}