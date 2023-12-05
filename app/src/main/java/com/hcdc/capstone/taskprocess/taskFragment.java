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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.task_fragment, container, false);
        emptyTaskView = rootView.findViewById(R.id.emptyTask);

        db = FirebaseFirestore.getInstance();

        rv = rootView.findViewById(R.id.tasklistsfragment);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setHasFixedSize(true);
        tList = new ArrayList<>();
        ta = new TaskAdapter(getContext(), tList);
        rv.setAdapter(ta);

        fetchDataFromFirestore();
        return rootView;
    }

    private void fetchDataFromFirestore() {
        emptyTaskView.setVisibility(View.INVISIBLE);

        // Get the current user's UID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserUid = currentUser != null ? currentUser.getUid() : null;

        db.collection("tasks")
                .orderBy("createdAt", Query.Direction.DESCENDING) // Sort by createdAt in descending order
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e("Firestore Error", error.getMessage());
                            return;
                        }

                        tList.clear(); // Clear the existing list to avoid duplicates

                        for (DocumentChange dc : value.getDocumentChanges()) {
                            TaskData task = dc.getDocument().toObject(TaskData.class);

                            // Check if the task is accepted
                            if (!task.isAccepted()) {
                                // Check if the current user's UID is not in the acceptedBy field
                                if (!containsUserUid(task.getAcceptedByUsers(), currentUserUid)) {
                                    if (dc.getType() == DocumentChange.Type.MODIFIED) {
                                        // Handle the case where a document is modified (optional)
                                        updateTaskInList(tList, task);
                                    } else if (dc.getType() == DocumentChange.Type.ADDED) {
                                        // Check if the timeFrame field exists in the document
                                        if (dc.getDocument().contains("timeFrame")) {
                                            // Retrieve the timeFrame map
                                            Map<String, Object> timeFrameMap = (Map<String, Object>) dc.getDocument().get("timeFrame");
                                            if (timeFrameMap != null) {
                                                // Retrieve hours and minutes from the timeFrame map
                                                int hours = ((Double) timeFrameMap.get("hours")).intValue();
                                                int minutes = ((Double) timeFrameMap.get("minutes")).intValue();
                                                task.hours = hours;
                                                task.minutes = minutes;
                                            }
                                        }
                                        tList.add(task);
                                    }
                                }
                            }
                        }

                        ta.notifyDataSetChanged();

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


    private boolean containsUserUid(List<String> acceptedBy, String currentUserUid) {
        return acceptedBy != null && currentUserUid != null && acceptedBy.contains(currentUserUid);
    }

    private boolean containsTaskWithId(@NonNull List<TaskData> taskList, String taskName) {
        for (TaskData task : taskList) {
            if (task.getTaskName().equals(taskName)) {
                return true;
            }
        }
        return false;
    }
    private void updateTaskInList(@NonNull List<TaskData> taskList, TaskData updatedTask) {
        for (int i = 0; i < taskList.size(); i++) {
            if (taskList.get(i).getTaskName().equals(updatedTask.getTaskName())) {
                taskList.set(i, updatedTask);
                break;
            }
        }
    }
}