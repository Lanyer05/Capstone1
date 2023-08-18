package com.hcdc.capstone;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;


public class taskFragment extends Fragment {

    RecyclerView rv;
    FirebaseFirestore db;
    TaskAdapter ta;
    ArrayList<Tasks> tList;

    TextView emptyTaskView;


    public taskFragment() {
        // Required empty public constructor
    }

    @SuppressLint("MissingInflatedId")
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
        db.collection("tasks")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e("Firestore Error", error.getMessage());
                            return;
                        }

                        tList.clear();
                        for (DocumentChange dc : value.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                Tasks task = dc.getDocument().toObject(Tasks.class);
                                tList.add(task);
                            }
                        }

                        // Update the visibility of the emptyTextView based on data availability
                        if (tList.isEmpty()) {
                            emptyTaskView.setVisibility(View.VISIBLE);
                        } else {
                            emptyTaskView.setVisibility(View.GONE);
                        }

                        ta.notifyDataSetChanged();
                    }
                });
    }

}
