package com.hcdc.capstone;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class Task extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    RecyclerView rv;
    FirebaseFirestore db;
    TaskAdapter ta;
    ArrayList<Tasks> tList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        bottomNavigationView = findViewById(R.id.bottom_navigation_view);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_home:
                        Intent iii = new Intent(getApplicationContext(), Homepage.class);
                        startActivity(iii);
                        overridePendingTransition(R.anim.slide_left, R.anim.slide_right);
                        return true;

                    case R.id.action_task:
                        Intent intent = new Intent(getApplicationContext(), Task.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_left, R.anim.slide_right);
                        return true;

                    case R.id.action_reward:
                        Intent ii = new Intent(getApplicationContext(), Reward.class);
                        startActivity(ii);
                        overridePendingTransition(R.anim.slide_right, R.anim.slide_left);
                        return true;

                    case R.id.action_transaction:
                        Intent i = new Intent(getApplicationContext(), Transaction.class);
                        startActivity(i);
                        overridePendingTransition(R.anim.slide_right, R.anim.slide_left);
                        return true;
                }
                return false;
            }
        });

        rv = findViewById(R.id.tasklists);
        db = FirebaseFirestore.getInstance();

        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(this));

        tList = new ArrayList<>();
        ta = new TaskAdapter(this, tList);
        rv.setAdapter(ta);

        db.collection("tasks").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot snapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.e("FirestoreError", "Error fetching tasks: " + e.getMessage()); // Log the error
                    return;
                }

                tList.clear();
                for (DocumentSnapshot docSnapshot : snapshot.getDocuments()) {
                    Tasks tasks = docSnapshot.toObject(Tasks.class);
                    tList.add(tasks);
                }

                ta.notifyDataSetChanged();

                Log.d("FirestoreSuccess", "Number of tasks fetched: " + tList.size()); // Log the success
            }
        });
    }
}
