package com.hcdc.capstone.rewardprocess;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
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

        //Snackbar message
        showBubbleText(findViewById(R.id.couponBox));

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

    private void showBubbleText(View targetView) {
        // Create a Snackbar with the custom layout
        Snackbar snackbar = Snackbar.make(targetView, "", Snackbar.LENGTH_LONG);

        // Get the Snackbar view
        View snackbarView = snackbar.getView();

        // Inflate the custom layout into the Snackbar view
        View customSnackbarLayout = getLayoutInflater().inflate(R.layout.custom_snackbar_layout, null);
        ((TextView) customSnackbarLayout.findViewById(R.id.snackbar_text)).setText("Click here for coupons");

        // Add the custom layout to the Snackbar view
        Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout) snackbarView;
        snackbarLayout.addView(customSnackbarLayout, 0);

        // Set the background color of the Snackbar to transparent
        snackbarLayout.setBackgroundColor(Color.TRANSPARENT);

        // Customize the Snackbar view
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) snackbarView.getLayoutParams();
        params.gravity = Gravity.TOP;
        params.setMargins(500, 130, 100, 120); // Adjust the margin as needed
        snackbarView.setLayoutParams(params);

        // Set up fade-in animation
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(1000); // Adjust the duration as needed
        snackbarLayout.setAnimation(fadeIn);

        snackbar.show();

        new Handler().postDelayed(() -> {
            AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
            fadeOut.setDuration(1000);
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }
                @Override
                public void onAnimationEnd(Animation animation) {
                    if (snackbar.isShown()) {
                        snackbar.dismiss();
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    // Do something if needed
                }
            });
            snackbarLayout.startAnimation(fadeOut);
        }, 3000);
    }
}
