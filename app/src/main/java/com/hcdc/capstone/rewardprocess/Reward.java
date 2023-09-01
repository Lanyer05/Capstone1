package com.hcdc.capstone.rewardprocess;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView; // Import TextView

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.hcdc.capstone.BaseActivity;
import com.hcdc.capstone.Homepage;
import com.hcdc.capstone.R;
import com.hcdc.capstone.Transaction;
import com.hcdc.capstone.adapters.RewardAdapter;
import com.hcdc.capstone.taskprocess.Task;

import java.util.ArrayList;

public class Reward extends BaseActivity {

    private BottomNavigationView bottomNavigationView;
    private TextView pointsSystemTextView;

    RecyclerView recyclerView;
    FirebaseFirestore firestore;
    RewardAdapter rewardAdapter;
    ArrayList<Rewards> rewardList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rewards);

        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        pointsSystemTextView = findViewById(R.id.points_system1); // Initialize points_system1 TextView

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

        // Fetch and display the current user's points
        fetchAndDisplayCurrentUserPoints();

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

    private void fetchAndDisplayCurrentUserPoints() {
        // Get the current user's UID
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Create a batch to execute multiple Firestore operations
        WriteBatch batch = firestore.batch();

        // Create a reference to the user's document
        DocumentReference userRef = firestore.collection("users").document(currentUserId);

        // Get the user's points field and set it to the TextView
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Assuming you have a field named "userpoints" in the document
                Long userPoints = documentSnapshot.getLong("userpoints");
                if (userPoints != null) {
                    pointsSystemTextView.setText(String.valueOf(userPoints));
                }
            }
        }).addOnFailureListener(e -> {
            // Handle error
        });

        // Commit the batch to execute all operations
        batch.commit().addOnSuccessListener(aVoid -> {
            // Batch operation successful
        }).addOnFailureListener(e -> {
            // Handle batch operation failure
        });
    }


    private void navigateToActivity(Class<?> targetActivity) {
        Intent intent = new Intent(this, targetActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }
}
