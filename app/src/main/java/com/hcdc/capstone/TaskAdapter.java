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
        holder.taskpoint.setText(tasks.getPoints()+"pts");
        holder.taskloc.setText(tasks.getLocation());
        holder.taskdesc.setText(tasks.getDescription());

        Log.d("TaskAdapter", "Binding task: " + tasks.getTaskName());
        Log.d("TaskAdapter", "Binding task: " + tasks.getPoints());
        Log.d("TaskAdapter", "Binding task: " + tasks.getLocation());
        Log.d("TaskAdapter", "Binding task: " + tasks.getDescription());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(context, TaskDetails.class);

                i.putExtra("tasktitle",tasks.taskName);
                i.putExtra("taskpoint",tasks.points);
                i.putExtra("tasklocation",tasks.location);
                i.putExtra("taskdetails",tasks.description);

                context.startActivity(i);

            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder{

        TextView tasktitle, taskdesc, taskpoint, taskloc;
        public TaskViewHolder(@NonNull View taskView)
        {
            super(taskView);

            tasktitle = taskView.findViewById(R.id.taskTitle);
            taskdesc = taskView.findViewById(R.id.taskDesc);
            taskpoint = taskView.findViewById(R.id.taskPoint);
            taskloc = taskView.findViewById(R.id.taskLocation);
        }
    }
}
