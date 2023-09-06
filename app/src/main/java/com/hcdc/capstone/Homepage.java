package com.hcdc.capstone;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView; // Import TextView
import android.widget.Toast;

import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hcdc.capstone.rewardprocess.Reward;
import com.hcdc.capstone.taskprocess.Task;
import com.hcdc.capstone.transactionprocess.Transaction;

public class Homepage extends BaseActivity {

    private BottomNavigationView bottomNavigationView;
    private TextView pointsSystemTextView; // Add this TextView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        pointsSystemTextView = findViewById(R.id.points_system); // Initialize points_system TextView

        bottomNavigationView.setSelectedItemId(R.id.action_home);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.action_home) {
                    return true;

                } else if (itemId == R.id.action_task) {
                    navigateToActivity(Task.class);
                    return true;
                } else if (itemId == R.id.action_reward) {
                    navigateToActivity(Reward.class);
                    return true;
                } else if (itemId == R.id.action_transaction) {
                    navigateToActivity(Transaction.class);
                    return true;
                }

                return false;
            }
        });

        // Fetch and display the current user's points
        fetchAndDisplayCurrentUserPoints();
    }

    private void fetchAndDisplayCurrentUserPoints() {
        // Get the current user's ID
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Fetch the current user's points from Firestore
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("users")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        if (documentSnapshot.contains("userpoints")) {
                            Long userPoints = documentSnapshot.getLong("userpoints");
                            if (userPoints != null) {
                                pointsSystemTextView.setText("" + userPoints);
                            } else {
                                // Handle the case when userpoints is null
                            }
                        } else {
                            // Handle the case when userpoints field does not exist
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle error
                    Toast.makeText(getApplicationContext(),"Error Occured !!!", Toast.LENGTH_LONG).show();
                });
    }

    private void navigateToActivity(Class<?> targetActivity) {
        Intent intent = new Intent(this, targetActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }
}
