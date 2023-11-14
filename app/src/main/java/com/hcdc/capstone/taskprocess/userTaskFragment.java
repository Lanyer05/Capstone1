package com.hcdc.capstone.taskprocess;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.hcdc.capstone.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class userTaskFragment extends Fragment {

    private FirebaseFirestore firestore;
    private TextView taskNameTextView, taskPointsTextView, taskLocationTextView, taskTimeFrameTextView, taskDescriptionTextView, taskEmptyTextView, taskMaxUserTextView;
    private Button cancelButton, startButton;

    private ImageView imgLoc, imgTime, imgMax;

    public userTaskFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.usertask_fragment, container, false);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        taskNameTextView = view.findViewById(R.id.taskTitle);
        taskPointsTextView = view.findViewById(R.id.taskPoint);
        taskLocationTextView = view.findViewById(R.id.taskLocation);
        taskTimeFrameTextView = view.findViewById(R.id.taskTimeFrame);
        taskDescriptionTextView = view.findViewById(R.id.taskDesc);
        taskMaxUserTextView = view.findViewById(R.id.taskMaxUser);
        taskEmptyTextView = view.findViewById(R.id.taskEmptyuser);

        cancelButton = view.findViewById(R.id.button2);
        startButton = view.findViewById(R.id.button);

        imgLoc = view.findViewById(R.id.loc);
        imgTime = view.findViewById(R.id.ctTimer);
        imgMax = view.findViewById(R.id.max);

        String currentUserUID = Objects.requireNonNull(auth.getCurrentUser()).getUid();

        firestore.collection("user_acceptedTask")
                .whereEqualTo("acceptedBy", currentUserUID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot documents = task.getResult();
                        if (documents != null && !documents.isEmpty()) {
                            QueryDocumentSnapshot document = (QueryDocumentSnapshot) documents.getDocuments().get(0);

                            String taskName = document.getString("taskName");
                            Long taskPointsLong = document.getLong("points");
                            String taskPoints = String.valueOf(taskPointsLong) + " points";
                            String taskLocation = document.getString("location");
                            String taskDescription = document.getString("description");
                            Long taskMaxUser = document.getLong("maxUsers");
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
                            displayAcceptedUserRatio(taskName, taskMaxUser);

                            int finalTaskHours = taskHours;
                            int finalTaskMinutes = taskMinutes;

                            // Handle cancel button click to delete the document
                            cancelButton.setOnClickListener(v -> {
                                View overlayView = LayoutInflater.from(getContext()).inflate(R.layout.confirmation_overlay, null);
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                                alertDialogBuilder.setView(overlayView);
                                AlertDialog alertDialog = alertDialogBuilder.create();
                                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                alertDialog.show();

                                Button confirmButton = overlayView.findViewById(R.id.confirmButton);
                                Button cancelButtonOverlay = overlayView.findViewById(R.id.cancelButton);

                                confirmButton.setOnClickListener(viewConfirm -> {
                                    WriteBatch batch = firestore.batch();

                                    // Remove the user's UID from the acceptedByUsers field in the tasks collection
                                    firestore.collection("tasks")
                                            .whereEqualTo("taskName", taskName)
                                            .get()
                                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                                if (!queryDocumentSnapshots.isEmpty()) {
                                                    DocumentSnapshot taskDocument = queryDocumentSnapshots.getDocuments().get(0);
                                                    List<String> acceptedByUsers = (List<String>) taskDocument.get("acceptedByUsers");

                                                    if (acceptedByUsers != null) {
                                                        acceptedByUsers.remove(currentUserUID);
                                                        firestore.collection("tasks")
                                                                .document(taskDocument.getId())
                                                                .update("acceptedByUsers", acceptedByUsers)
                                                                .addOnSuccessListener(aVoid -> {
                                                                    if (getActivity() != null) {
                                                                        getActivity().finish();
                                                                    }
                                                                })
                                                                .addOnFailureListener(e -> {
                                                                });
                                                    }
                                                }
                                            })
                                            .addOnFailureListener(e -> {
                                            });
                                    batch.delete(document.getReference());
                                    batch.commit()
                                            .addOnSuccessListener(aVoid -> {
                                                if (getActivity() != null) {
                                                    getActivity().finish();
                                                }
                                            })
                                            .addOnFailureListener(e -> {
                                            });
                                    alertDialog.dismiss();
                                });

                                cancelButtonOverlay.setOnClickListener(viewCancel -> {
                                    alertDialog.dismiss();
                                });
                            });

                            // Implement the startButton click listener here
                            startButton.setOnClickListener(v -> {
                                View overlayView = LayoutInflater.from(getContext()).inflate(R.layout.start_confirmation_overlay, null);
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                                alertDialogBuilder.setView(overlayView);
                                AlertDialog alertDialog = alertDialogBuilder.create();
                                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                alertDialog.show();

                                Button confirmButton = overlayView.findViewById(R.id.confirmButton);
                                Button cancelButtonOverlay = overlayView.findViewById(R.id.cancelButton);

                                confirmButton.setOnClickListener(viewConfirm -> {
                                    // Calculate task duration in milliseconds
                                    long taskDurationMillis = (finalTaskHours * 60 + finalTaskMinutes) * 60 * 1000;

                                    Intent intent = new Intent(getContext(), TaskProgress.class);

                                    intent.putExtra("taskName", taskName);
                                    intent.putExtra("taskPoints", taskPoints);
                                    intent.putExtra("taskDescription", taskDescription);
                                    intent.putExtra("taskLocation", taskLocation);
                                    intent.putExtra("taskDurationMillis", taskDurationMillis);
                                    intent.putExtra("timeFrameHours", finalTaskHours);
                                    intent.putExtra("timeFrameMinutes", finalTaskMinutes);

                                    startActivity(intent);

                                    alertDialog.dismiss();

                                    getActivity().finish();
                                });

                                cancelButtonOverlay.setOnClickListener(viewCancel -> {
                                    alertDialog.dismiss();
                                });
                            });
                        } else {
                            // Handle the case when there are no accepted tasks
                            taskNameTextView.setVisibility(View.GONE);
                            taskPointsTextView.setVisibility(View.GONE);
                            taskLocationTextView.setVisibility(View.GONE);
                            taskTimeFrameTextView.setVisibility(View.GONE);
                            taskDescriptionTextView.setVisibility(View.GONE);
                            taskMaxUserTextView.setVisibility(View.GONE);
                            taskEmptyTextView.setVisibility(View.VISIBLE);

                            cancelButton.setVisibility(View.GONE);
                            startButton.setVisibility(View.GONE);

                            imgTime.setVisibility(View.GONE);
                            imgLoc.setVisibility(View.GONE);
                            imgMax.setVisibility(View.GONE);
                        }
                    }
                });

        return view;
    }

    @SuppressLint("SetTextI18n")
    private void displayAcceptedUserRatio(String taskName, Long taskMaxUser) {
        firestore.collection("tasks")
                .whereEqualTo("taskName", taskName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        List<String> acceptedByUsers = (List<String>) documentSnapshot.get("acceptedByUsers");
                        if (acceptedByUsers != null) {
                            int currentAcceptedUsers = acceptedByUsers.size();
                            Long maxUsers = documentSnapshot.getLong("maxUsers");
                            String ratioText = currentAcceptedUsers + "/" + maxUsers;
                            taskMaxUserTextView.setText("Max User: " + ratioText);
                        } else {
                            taskMaxUserTextView.setText("Max User: N/A");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle the error
                });
    }

}