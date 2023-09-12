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
import com.google.firebase.messaging.FirebaseMessaging;;
import com.google.firebase.firestore.DocumentReference;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;

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

        // Retrieve and store the FCM device token
        retrieveAndStoreFCMToken();
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

    private void retrieveAndStoreFCMToken() {
        // Retrieve the FCM device token
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String token) {
                        // Store the FCM token in the user's document in Firestore
                        updateFCMTokenInFirestore(token);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle error
                        Toast.makeText(getApplicationContext(), "Error Occurred while retrieving FCM token", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void updateFCMTokenInFirestore(String token) {
        // Get the current user's ID
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Reference to the user's document in Firestore
        DocumentReference userRef = FirebaseFirestore.getInstance().collection("users").document(currentUserId);

        // Update the FCM token in the user's document
        userRef.update("fcmToken", token)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Successfully stored FCM token in the user's document
                        // You can add further actions or handling here if needed
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle error
                        Toast.makeText(getApplicationContext(), "Error Occurred while storing FCM token", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void navigateToActivity(Class<?> targetActivity) {
        Intent intent = new Intent(this, targetActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }
}
