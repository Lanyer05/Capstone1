package com.hcdc.capstone.rewardprocess;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.hcdc.capstone.BaseActivity;
import com.hcdc.capstone.R;
import com.hcdc.capstone.adapters.CouponAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class userCoupons extends BaseActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private CouponAdapter couponAdapter;
    private ArrayList<Coupons> couponList;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_coupons);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Coupon List
        RecyclerView couponRecyclerView = findViewById(R.id.couponRecyclerView);
        couponList = new ArrayList<>();
        couponAdapter = new CouponAdapter(this, couponList);

        couponRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        couponRecyclerView.setAdapter(couponAdapter);

        fetchCoupons();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchCoupons() {

        String currentUserUID = Objects.requireNonNull(auth.getCurrentUser()).getUid();

        firestore.collection("coupons")
                .whereEqualTo("userId", currentUserUID).whereEqualTo("isClaimed",Boolean.FALSE)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot documents = task.getResult();
                        if (documents != null) {
                            for (QueryDocumentSnapshot document : documents) {
                                Log.d("userCoupons", "Document data: " + document.getData());

                                Coupons coupon = new Coupons(
                                        document.getString("userId"),
                                        document.getString("couponCode"),
                                        parseSelectedItems(document.get("selectedItems"))
                                );
                                couponList.add(coupon);
                            }

                            // Notify the adapter that data has changed
                            couponAdapter.notifyDataSetChanged();

                        }
                    } else {
                        // Handle the error here
                        Log.e("userCoupons", "Failed to fetch coupons", task.getException());
                        Toast.makeText(userCoupons.this, "Failed to fetch coupons", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    // Parse the List<Map<String, Object>> to List<Coupons.SelectedItems>
    private List<Coupons.SelectedItems> parseSelectedItems(Object selectedItemsObj) {
        List<Coupons.SelectedItems> selectedItems = new ArrayList<>();

        if (selectedItemsObj instanceof List) {
            List<Map<String, Object>> selectedItemsList = (List<Map<String, Object>>) selectedItemsObj;
            for (Map<String, Object> itemData : selectedItemsList) {
                String rewardId = (String) itemData.get("rewardId");
                long selectedQuantity = (long) itemData.get("selectedQuantity");
                selectedItems.add(new Coupons.SelectedItems(rewardId, (int) selectedQuantity));
            }
        }

        return selectedItems;
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, Reward.class);
        startActivity(intent);
        finish();
    }
}
