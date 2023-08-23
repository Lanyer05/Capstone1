package com.hcdc.capstone;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class userTaskFragment extends Fragment {

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private TextView taskNameTextView, taskPointsTextView, taskLocationTextView, taskTimeFrameTextView, taskDescriptionTextView, taskEmptyTextView;
    private Button cancelButton, startButton;

    private ImageView imgLoc, imgTime;

    public userTaskFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.usertask_fragment, container, false);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        taskNameTextView = view.findViewById(R.id.taskTitle);
        taskPointsTextView = view.findViewById(R.id.taskPoint);
        taskLocationTextView = view.findViewById(R.id.taskLocation);
        taskTimeFrameTextView = view.findViewById(R.id.taskTimeFrame);
        taskDescriptionTextView = view.findViewById(R.id.taskDesc);

        taskEmptyTextView = view.findViewById(R.id.taskEmptyuser);

        cancelButton = view.findViewById(R.id.button2);
        startButton = view.findViewById(R.id.button);

        imgLoc = view.findViewById(R.id.loc);
        imgTime = view.findViewById(R.id.ctTimer);

        String currentUserUID = auth.getCurrentUser().getUid();

        firestore.collection("user_acceptedTask")
                .whereEqualTo("acceptedBy", currentUserUID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot documents = task.getResult();
                        if (documents != null && !documents.isEmpty()) {
                            QueryDocumentSnapshot document = (QueryDocumentSnapshot) documents.getDocuments().get(0);

                            String taskName = document.getString("taskName");
                            String taskPoints = document.getString("points");
                            String taskLocation = document.getString("location");
                            String taskDescription = document.getString("description");
                            Map<String, Object> timeFrameMap = (Map<String, Object>) document.get("timeFrame");
                            int taskHours = 0;
                            int taskMinutes = 0;
                            String taskTimeFrame = "";

                            if (timeFrameMap != null) {
                                taskHours = ((Long) timeFrameMap.get("hours")).intValue();
                                taskMinutes = ((Long) timeFrameMap.get("minutes")).intValue();

                                if (taskHours > 0 || taskMinutes > 0) {
                                    taskTimeFrame = taskHours + " hours " + taskMinutes + " minutes";
                                }
                            }

                            taskNameTextView.setText(taskName);
                            taskPointsTextView.setText(taskPoints);
                            taskLocationTextView.setText(taskLocation);
                            taskTimeFrameTextView.setText(taskTimeFrame);
                            taskDescriptionTextView.setText(taskDescription);

                            int finalTaskHours = taskHours;
                            int finalTaskMinutes = taskMinutes;
                            cancelButton.setOnClickListener(v -> {
                                // Show the confirmation overlay
                                View overlayView = LayoutInflater.from(getContext()).inflate(R.layout.confirmation_overlay, null);
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                                alertDialogBuilder.setView(overlayView);
                                AlertDialog alertDialog = alertDialogBuilder.create();
                                alertDialog.show();

                                Button confirmButton = overlayView.findViewById(R.id.confirmButton);
                                Button cancelButtonOverlay = overlayView.findViewById(R.id.cancelButton);

                                confirmButton.setOnClickListener(viewConfirm -> {
                                    // Delete the task from user_acceptedTask collection
                                    document.getReference().delete()
                                            .addOnSuccessListener(aVoid -> {
                                                // Add the task back to the tasks collection
                                                Map<String, Object> taskData = new HashMap<>();
                                                taskData.put("taskName", taskName);
                                                taskData.put("description", taskDescription);
                                                taskData.put("location", taskLocation);
                                                taskData.put("points", taskPoints);
                                                taskData.put("isAccepted", false);
                                                taskData.put("isStarted", false);
                                                taskData.put("isCompleted", false); // Add the isStarted field

                                                // Only add the timeFrame if hours or minutes are greater than 0
                                                if (finalTaskHours > 0 || finalTaskMinutes > 0) {
                                                    Map<String, Object> timeFrameMapNew = new HashMap<>();
                                                    timeFrameMapNew.put("hours", finalTaskHours);
                                                    timeFrameMapNew.put("minutes", finalTaskMinutes);
                                                    taskData.put("timeFrame", timeFrameMapNew);
                                                }

                                                firestore.collection("tasks")
                                                        .add(taskData)
                                                        .addOnSuccessListener(documentReference -> {
                                                            // Successfully added task back to tasks collection
                                                            // Update UI or take further actions
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            // Failed to add task back to tasks collection
                                                            // Handle the error
                                                        });
                                            })
                                            .addOnFailureListener(e -> {
                                                // Failed to delete task from user_acceptedTask collection
                                                // Handle the error
                                            });

                                    // Dismiss the confirmation overlay
                                    alertDialog.dismiss();
                                });

                                cancelButtonOverlay.setOnClickListener(viewCancel -> {
                                    // Dismiss the confirmation overlay
                                    alertDialog.dismiss();
                                });
                            });

                            // Implement the startButton click listener here
                            startButton.setOnClickListener(v -> {
                                View overlayView = LayoutInflater.from(getContext()).inflate(R.layout.start_confirmation_overlay, null);
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                                alertDialogBuilder.setView(overlayView);
                                AlertDialog alertDialog = alertDialogBuilder.create();
                                alertDialog.show();

                                Button confirmButton = overlayView.findViewById(R.id.confirmButton);
                                Button cancelButtonOverlay = overlayView.findViewById(R.id.cancelButton);

                                confirmButton.setOnClickListener(viewConfirm -> {
                                    // Calculate task duration in milliseconds
                                    long taskDurationMillis = (finalTaskHours * 60 + finalTaskMinutes) * 60 * 1000;

                                    Intent intent = new Intent(getContext(), timerTEST.class);

                                    intent.putExtra("taskName", taskName);
                                    intent.putExtra("taskPoints", taskPoints);
                                    intent.putExtra("taskDescription", taskDescription);
                                    intent.putExtra("taskLocation", taskLocation);
                                    intent.putExtra("taskDurationMillis", taskDurationMillis);
                                    intent.putExtra("timeFrameHours", finalTaskHours);
                                    intent.putExtra("timeFrameMinutes", finalTaskMinutes);

                                    startActivity(intent);

                                    alertDialog.dismiss();
                                });

                                cancelButtonOverlay.setOnClickListener(viewCancel -> {
                                    alertDialog.dismiss();
                                });
                            });
                        } else {
                            taskNameTextView.setVisibility(View.GONE);
                            taskPointsTextView.setVisibility(View.GONE);
                            taskLocationTextView.setVisibility(View.GONE);
                            taskTimeFrameTextView.setVisibility(View.GONE);
                            taskDescriptionTextView.setVisibility(View.GONE);

                            taskEmptyTextView.setVisibility(View.VISIBLE);

                            cancelButton.setVisibility(View.GONE);
                            startButton.setVisibility(View.GONE);

                            imgTime.setVisibility(View.GONE);
                            imgLoc.setVisibility(View.GONE);
                        }
                    }
                });

        return view;
    }
}
