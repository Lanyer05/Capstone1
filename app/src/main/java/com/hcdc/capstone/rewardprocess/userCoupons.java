package com.hcdc.capstone.rewardprocess;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;
import com.hcdc.capstone.BaseActivity;
import com.hcdc.capstone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.hcdc.capstone.adapters.CouponAdapter;

import android.content.Intent;
import android.widget.Toast;

import java.util.ArrayList;

public class userCoupons extends BaseActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private TextView rewardNameTextView, rewardPointsTextView, userEmailTextView;
    private Button cancelButton;

    private RecyclerView couponRecyclerView;
    private CouponAdapter couponAdapter;
    private ArrayList<Coupons> couponList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_user_coupons);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        cancelButton = findViewById(R.id.cancelButton);
        rewardNameTextView = findViewById(R.id.rewardNameTextView);
        rewardPointsTextView = findViewById(R.id.rewardPointsTextView);
        userEmailTextView = findViewById(R.id.userEmailTextView);

        //Coupon List
        couponRecyclerView = findViewById(R.id.couponRecyclerView);
        couponList = new ArrayList<>();
        couponAdapter = new CouponAdapter(this, couponList);
        fetchCoupons();

        couponRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        couponRecyclerView.setAdapter(couponAdapter);


        //Get In Progress
        String currentUserUID = auth.getCurrentUser().getUid();

        firestore.collection("rewardrequest")
                .whereEqualTo("userId", currentUserUID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot documents = task.getResult();
                        if (documents != null) {
                            for (QueryDocumentSnapshot document : documents) {
                                String rewardName = document.getString("rewardName");
                                Long rewardPoints = document.getLong("rewardPoints"); // Retrieve as Long
                                String userEmail = document.getString("userEmail");

                                if (rewardPoints != null) {
                                    String rewardPointsString = String.valueOf(rewardPoints);
                                    rewardPointsTextView.setText("Reward Points: " + rewardPointsString);
                                } else {
                                    rewardPointsTextView.setText("Reward Points: N/A");
                                }
                                rewardNameTextView.setText("" + rewardName);

                                userEmailTextView.setText("" + userEmail);
                            }
                        } else {
                            rewardNameTextView.setVisibility(View.GONE);
                            rewardPointsTextView.setVisibility(View.GONE);
                            userEmailTextView.setVisibility(View.GONE);
                            findViewById(R.id.cancelButton).setVisibility(View.GONE);

                            TextView noRewardRequestsTextView = findViewById(R.id.noRewardRequestsTextView);
                            noRewardRequestsTextView.setVisibility(View.VISIBLE);
                            noRewardRequestsTextView.setText("No reward requests available.");
                        }
                    } else {
                        Exception exception = task.getException();
                        if (exception != null) {
                        }
                    }
                });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelButton();
            }
        });
    }

    private void cancelButton() {
        String currentUserUID = auth.getCurrentUser().getUid();
        WriteBatch batch = firestore.batch();
        firestore.collection("rewardrequest")
                .whereEqualTo("userId", currentUserUID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot documents = task.getResult();
                        if (documents != null && !documents.isEmpty()) {
                            for (QueryDocumentSnapshot document : documents) {
                                batch.delete(document.getReference());
                            }
                            batch.commit()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            rewardNameTextView.setText("");
                                            rewardPointsTextView.setText("");
                                            userEmailTextView.setText("");
                                            Toast.makeText(userCoupons.this, " Reward request canceled successfully ", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(userCoupons.this, Reward.class);
                                            startActivity(intent);
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                        }
                                    });
                        } else {
                        }
                    } else {
                        Exception exception = task.getException();
                        if (exception != null) {
                        }
                    }
                });
    }

    private void fetchCoupons() {
        // Clear the existing list
        couponList.clear();

        String currentUserUID = auth.getCurrentUser().getUid();

        firestore.collection("complete_rewardreq")
                .whereEqualTo("userId", currentUserUID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot documents = task.getResult();
                        if (documents != null) {
                            for (QueryDocumentSnapshot document : documents) {
                                String rewardName = document.getString("rewardName");
                                String userCouponCode = document.getString("couponuserCode");
                                boolean status = document.getBoolean("pendingStatus");
                                String useremail = document.getString("email");

                                // Check the type of rewardPoints and convert it to int
                                Object rewardPointsObj = document.get("rewardPoints");
                                int rewardPoints = 0; // Default value if not found or conversion fails
                                if (rewardPointsObj != null) {
                                    if (rewardPointsObj instanceof Long) {
                                        rewardPoints = ((Long) rewardPointsObj).intValue();
                                    } else if (rewardPointsObj instanceof Integer) {
                                        rewardPoints = (int) rewardPointsObj;
                                    }
                                }

                                Coupons coupon = new Coupons(rewardName, currentUserUID, status, useremail, rewardPoints, userCouponCode);
                                couponList.add(coupon);

                                Log.d("CouponFetch", "Reward Name: " + rewardName + ", Coupon Code: " + userCouponCode);
                            }

                            // Notify the adapter that data has changed
                            couponAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, Reward.class);
        startActivity(intent);
        finish();
    }

}
