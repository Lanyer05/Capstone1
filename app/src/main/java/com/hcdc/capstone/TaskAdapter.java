package com.hcdc.capstone;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    Context context;
    ArrayList<Tasks> list;

    public TaskAdapter(Context context, ArrayList<Tasks> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(context).inflate(R.layout.tasklayout,parent,false);
        return new TaskViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Tasks tasks = list.get(position);
        holder.tasktitle.setText(tasks.getTaskName());
        holder.taskpoint.setText(tasks.getPoints() + "points");
        holder.taskloc.setText(tasks.getLocation());
        holder.taskdesc.setText(tasks.getDescription());

        // Check if the task has a timeFrame
        if (tasks.getHours() > 0 || tasks.getMinutes() > 0) {
            String timeFrameText = "Time Frame: " + tasks.getHours() + " hours " + tasks.getMinutes() + " minutes";
            holder.taskTimer.setText(timeFrameText);
            holder.taskTimer.setVisibility(View.VISIBLE);
        } else {
            holder.taskTimer.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int clickedPosition = holder.getAdapterPosition();
                if (clickedPosition != RecyclerView.NO_POSITION) {
                    Tasks selectedTask = list.get(clickedPosition);

                    // Create an intent to open the TaskDetails activity
                    Intent intent = new Intent(context, TaskDetails.class);
                    intent.putExtra("tasktitle", selectedTask.getTaskName());
                    intent.putExtra("taskdetails", selectedTask.getDescription());
                    intent.putExtra("taskpoint", selectedTask.getPoints());
                    intent.putExtra("tasklocation", selectedTask.getLocation());
                    intent.putExtra("taskDuration", "Hours: " + selectedTask.getHours() + " Minutes: " + selectedTask.getMinutes());

                    context.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder{

        TextView tasktitle, taskdesc, taskpoint, taskloc, taskTimer;
        public TaskViewHolder(@NonNull View taskView)
        {
            super(taskView);

            tasktitle = taskView.findViewById(R.id.taskTitle);
            taskdesc = taskView.findViewById(R.id.taskDesc);
            taskpoint = taskView.findViewById(R.id.taskPoint);
            taskloc = taskView.findViewById(R.id.taskLocation);
            taskTimer = taskView.findViewById(R.id.taskTimeFrame);
        }
    }
}