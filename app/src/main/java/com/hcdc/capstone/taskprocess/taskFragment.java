package com.hcdc.capstone.taskprocess;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.hcdc.capstone.R;
import com.hcdc.capstone.adapters.TaskAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class taskFragment extends Fragment {

    RecyclerView rv;
    FirebaseFirestore db;
    TaskAdapter ta;
    ArrayList<TaskData> tList;
    TextView emptyTaskView;

    public taskFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.task_fragment, container, false);
        emptyTaskView = rootView.findViewById(R.id.emptyTask);

        // Create sample data for the RecyclerView
        db = FirebaseFirestore.getInstance();

        // Set up the RecyclerView
        rv = rootView.findViewById(R.id.tasklistsfragment);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setHasFixedSize(true);
        tList = new ArrayList<>();
        ta = new TaskAdapter(getContext(), tList);
        rv.setAdapter(ta);

        // Fetch data from Firestore
        fetchDataFromFirestore();
        return rootView;
    }


    private void fetchDataFromFirestore() {
        emptyTaskView.setVisibility(View.INVISIBLE);
        db.collection("tasks")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e("Firestore Error", error.getMessage());
                            return;
                        }
                        for (DocumentChange dc : value.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED || dc.getType() == DocumentChange.Type.MODIFIED) {
                                TaskData task = dc.getDocument().toObject(TaskData.class);
                                // Check if the task is accepted before adding it to the list
                                if (!task.isAccepted()) {
                                    // Check if the timeFrame field exists in the document
                                    if (dc.getDocument().contains("timeFrame")) {
                                        // Retrieve the timeFrame map
                                        Map<String, Object> timeFrameMap = (Map<String, Object>) dc.getDocument().get("timeFrame");
                                        if (timeFrameMap != null) {
                                            // Retrieve hours and minutes from the timeFrame map
                                            int hours = ((Long) timeFrameMap.get("hours")).intValue();
                                            int minutes = ((Long) timeFrameMap.get("minutes")).intValue();
                                            task.hours = hours;
                                            task.minutes = minutes;
                                        }
                                    }
                                    // Check if the task already exists in tList before adding it
                                    if (!containsTaskWithId(tList, task.getTaskName())) {
                                        tList.add(task);
                                    } else {
                                        // Update the existing task in tList with the new data
                                        updateTaskInList(tList, task);
                                    }
                                }
                            } else if (dc.getType() == DocumentChange.Type.REMOVED) {
                                // Handle the case where a document is removed (optional)
                                // You might want to remove the corresponding task from tList here
                            }
                        }
                        ta.notifyDataSetChanged();
                        // Check if tList is empty and update the TextView
                        if (tList.isEmpty()) {
                            emptyTaskView.setText("No tasks available");
                            emptyTaskView.setVisibility(View.VISIBLE);
                            emptyTaskView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                        } else {
                            emptyTaskView.setVisibility(View.GONE);
                            emptyTaskView.getLayoutParams().height = 0;
                        }
                    }
                });
    }

    // Helper function to check if a task with a specific ID already exists in the list
    private boolean containsTaskWithId(@NonNull List<TaskData> taskList, String taskName) {
        for (TaskData task : taskList) {
            if (task.getTaskName().equals(taskName)) {
                return true;
            }
        }
        return false;
    }

    // Helper function to update an existing task in the list with new data
    private void updateTaskInList(@NonNull List<TaskData> taskList, TaskData updatedTask) {
        for (int i = 0; i < taskList.size(); i++) {
            if (taskList.get(i).getTaskName().equals(updatedTask.getTaskName())) {
                taskList.set(i, updatedTask);
                break;
            }
        }
    }
}
