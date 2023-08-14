package com.hcdc.capstone;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class Reward extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;


    RecyclerView recyclerView;
    FirebaseFirestore firestore;
    RewardAdapter rewardAdapter;
    ArrayList<Rewards> rewardList;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rewards);
        // Your code for setting up the home page, if any
        // For example, you can add widgets, set up views, etc.

        bottomNavigationView = findViewById(R.id.bottom_navigation_view);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_home:
                        Intent iii = new Intent(getApplicationContext(), Homepage.class);
                        startActivity(iii);
                        return true;

                    case R.id.action_task:
                        // Handle "Task" item click if needed
                        // For example, navigate to TaskActivity
                        Intent i = new Intent(getApplicationContext(), Task.class);
                        startActivity(i);
                        return true;

                    case R.id.action_reward:
                        // Handle "Reward" item click if needed
                        // For example, navigate to RewardActivity


                    case R.id.action_transaction:
                        // Handle "Transaction"
                        Intent ii = new Intent(getApplicationContext(), Transaction.class);
                        startActivity(ii);
                        return true;
                }
                return false;
            }
        });


        recyclerView = findViewById(R.id.rewardslist);
        firestore = FirebaseFirestore.getInstance();

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        rewardList = new ArrayList<>();
        rewardAdapter = new RewardAdapter(this, rewardList);
        recyclerView.setAdapter(rewardAdapter);

        firestore.collection("rewards").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                if (error != null) {
                    Log.e("FirestoreError", "Error fetching tasks: " + error.getMessage()); // Log the error
                    return;
                }

                rewardList.clear();

                for (DocumentSnapshot documentSnapshot : value.getDocuments())
                {
                    Rewards rewards = documentSnapshot.toObject(Rewards.class);
                    rewardList.add(rewards);
                }

                rewardAdapter.notifyDataSetChanged();

                Log.d("FirestoreSuccess", "Number of rewards fetched: " + rewardList.size()); // Log the success
            }
        });

    }
}