package com.hcdc.capstone.rewardprocess;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.hcdc.capstone.BaseActivity;
import com.hcdc.capstone.Homepage;
import com.hcdc.capstone.R;
import com.hcdc.capstone.transactionprocess.Transaction;
import com.hcdc.capstone.taskprocess.Task;

import java.util.Objects;

public class Reward extends BaseActivity {

    private TextView pointsSystemTextView;

    private Button redeem;
    FirebaseFirestore firestore;

    @SuppressLint({"NonConstantResourceId", "NotifyDataSetChanged", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rewards);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        pointsSystemTextView = findViewById(R.id.points_system1);
        ImageView coupon = findViewById(R.id.couponBox);
        redeem = findViewById(R.id.redeemreward);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_home:
                    navigateToActivity(Homepage.class);
                    return true;

                case R.id.action_task:
                    navigateToActivity(Task.class);
                    return true;

                case R.id.action_reward:
                    return true;

                case R.id.action_transaction:
                    navigateToActivity(Transaction.class);
                    return true;
            }
            return false;
        });

        coupon.setOnClickListener(v -> {
            Intent i = new Intent(getApplicationContext(), userCoupons.class);
            startActivity(i);
            finish();
        });

        firestore = FirebaseFirestore.getInstance();



        // Fetch and display the current user's points
        fetchAndDisplayCurrentUserPoints();

        redeem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),RewardList.class);
                startActivity(i);
                finish();
            }
        });

    }

    private void fetchAndDisplayCurrentUserPoints() {
        // Get the current user's UID
        String currentUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

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
         });
        batch.commit().addOnSuccessListener(aVoid -> {
        }).addOnFailureListener(e -> {
         });
    }

    private void navigateToActivity(Class<?> targetActivity) {
        Intent intent = new Intent(this, targetActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }
}
