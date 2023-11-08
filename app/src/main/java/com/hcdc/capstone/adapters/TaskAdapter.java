package com.hcdc.capstone.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentChange;
import com.hcdc.capstone.R;
import com.hcdc.capstone.taskprocess.TaskData;
import com.hcdc.capstone.taskprocess.TaskDetails;

import java.util.ArrayList;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    Context context;
    ArrayList<TaskData> list;
    FirebaseFirestore db;

    public TaskAdapter(Context context, ArrayList<TaskData> list) {
        this.context = context;
        this.list = list;
        db = FirebaseFirestore.getInstance();
        // Initialize the Firestore listener
        initFirestoreListener();
    }

    // Add Firestore listener to watch for changes
    @SuppressLint("NotifyDataSetChanged")
    private void initFirestoreListener() {
        db.collection("tasks")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        // Handle error
                        return;
                    }
                    assert value != null;
                    for (DocumentChange dc : value.getDocumentChanges()) {
                        // Check changes in the data
                        if (dc.getType() == DocumentChange.Type.MODIFIED) {
                            // Handle modified data here
                            TaskData modifiedTask = dc.getDocument().toObject(TaskData.class);
                            // Find the position of the modified task in your list and update it
                            int index = findTaskIndex(modifiedTask);
                            if (index >= 0) {
                                list.set(index, modifiedTask);
                            }
                        }
                    }
                    // Notify the adapter that the data has changed
                    notifyDataSetChanged();
                });
    }

    // Helper method to find the index of a task in the list
    private int findTaskIndex(TaskData task) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getTaskName().equals(task.getTaskName())) {
                return i;
            }
        }
        return -1;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.tasklayout, parent, false);
        return new TaskViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        TaskData taskData = list.get(position);

        if (taskData.getAcceptedByUsers() != null && taskData.getAcceptedByUsers().size() < taskData.getMaxUsers()) {
            holder.tasktitle.setText(taskData.getTaskName());
            holder.taskpoint.setText(taskData.getPoints() + " points");
            holder.taskloc.setText(taskData.getLocation());
            holder.taskdesc.setText(taskData.getDescription());

            if (taskData.getHours() > 0 || taskData.getMinutes() > 0) {
                String timeFrameText = "Time Frame: " + taskData.getHours() + " hours " + taskData.getMinutes() + " minutes";
                holder.taskTimer.setText(timeFrameText);
                holder.taskTimer.setVisibility(View.VISIBLE);
            } else {
                holder.taskTimer.setVisibility(View.GONE);
            }
            holder.taskMaxUser.setText("Max User: " + taskData.getAcceptedByUsers().size() + "/" + taskData.getMaxUsers());
            holder.taskMaxUser.setVisibility(View.VISIBLE);

            holder.itemView.setOnClickListener(v -> {
                int clickedPosition = holder.getAdapterPosition();
                if (clickedPosition != RecyclerView.NO_POSITION) {
                    TaskData selectedTask = list.get(clickedPosition);

                    Intent intent = new Intent(context, TaskDetails.class);
                    intent.putExtra("tasktitle", selectedTask.getTaskName());
                    intent.putExtra("taskdetails", selectedTask.getDescription());
                    intent.putExtra("taskpoint", selectedTask.getPoints());
                    intent.putExtra("tasklocation", selectedTask.getLocation());
                    intent.putExtra("taskDuration", "Hours: " + selectedTask.getHours() + " Minutes: " + selectedTask.getMinutes());
                    intent.putExtra("taskMaxUser", selectedTask.getMaxUsers());
                    context.startActivity(intent);
                }
            });
        } else {
            // Hide the item if the task is at its max users limit
            holder.itemView.setVisibility(View.GONE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tasktitle, taskdesc, taskpoint, taskloc, taskTimer, taskMaxUser;

        public TaskViewHolder(@NonNull View taskView) {
            super(taskView);
            tasktitle = taskView.findViewById(R.id.taskTitle);
            taskdesc = taskView.findViewById(R.id.taskDesc);
            taskpoint = taskView.findViewById(R.id.taskPoint);
            taskloc = taskView.findViewById(R.id.taskLocation);
            taskTimer = taskView.findViewById(R.id.taskTimeFrame);
            taskMaxUser = taskView.findViewById(R.id.taskMaxUser);
        }
    }
}