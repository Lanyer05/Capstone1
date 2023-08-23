package com.hcdc.capstone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class Reward extends BaseActivity {

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

        bottomNavigationView = findViewById(R.id.bottom_navigation_view);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_home:
                        navigateToActivity(Homepage.class);
                        return true;

                    case R.id.action_task:
                        navigateToActivity(Task.class);
                        return true;

                    case R.id.action_reward:

                        bottomNavigationView.setSelectedItemId(R.id.action_reward);
                        return true;

                    case R.id.action_transaction:
                        navigateToActivity(Transaction.class);
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
                    Log.e("FirestoreError", "Error fetching tasks: " + error.getMessage());
                    return;
                }

                rewardList.clear();

                for (DocumentSnapshot documentSnapshot : value.getDocuments()) {
                    Rewards rewards = documentSnapshot.toObject(Rewards.class);
                    rewardList.add(rewards);
                }

                rewardAdapter.notifyDataSetChanged();

                Log.d("FirestoreSuccess", "Number of rewards fetched: " + rewardList.size());
            }
        });
    }

    private void navigateToActivity(Class<?> targetActivity) {
        Intent intent = new Intent(this, targetActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }
}
