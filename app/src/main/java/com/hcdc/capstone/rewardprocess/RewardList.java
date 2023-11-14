package com.hcdc.capstone.rewardprocess;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hcdc.capstone.R;
import com.hcdc.capstone.adapters.RewardCategoryItems;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RewardList extends AppCompatActivity implements RewardCategoryItems.RewardItemClickListener {

    RecyclerView recyclerView;
    RewardCategoryItems rewardCategoryItems;
    ArrayList<RewardItems> rewardItemsArrayList;

    FirebaseFirestore firestore;

    TextView currentuserPoints;
    private TextView totalPointsTextView;
    private TextView selectedItemsTextView;
    private int totalPoints = 0;
    private long currentUserPoints = 0;

    private boolean processingClick = false;

    Button checkout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward_list);

        totalPointsTextView = findViewById(R.id.totalpointselected);
        selectedItemsTextView = findViewById(R.id.selectedItems);

        currentuserPoints = findViewById(R.id.userPoints);
        checkout = findViewById(R.id.checkout_button);

        recyclerView = findViewById(R.id.rewardrecycler);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        rewardItemsArrayList = new ArrayList<>();
        rewardCategoryItems = new RewardCategoryItems(this, rewardItemsArrayList);
        rewardCategoryItems.itemClickListener = this;
        recyclerView.setAdapter(rewardCategoryItems);

        firestore = FirebaseFirestore.getInstance();
        fetchDataFromFirestore();
        fetchAndDisplayCurrentUserPoints();

        checkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (totalPoints > currentUserPoints) {
                    showPointsExceedDialog(currentUserPoints);
                } else if (totalPoints <= 0) {
                    Toast.makeText(getApplicationContext(), "No items selected to be redeemed", Toast.LENGTH_SHORT).show();
                } else {
                    showCustomDialog();
                }
            }
        });
    }

    private void fetchDataFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference rewardsCollection = db.collection("rewards");
        rewardsCollection.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<RewardItems> rewardsList = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        RewardItems reward = document.toObject(RewardItems.class);

                        // Only add items with quantity greater than 0
                        if (reward != null && reward.getQuantity() > 0) {
                            rewardsList.add(reward);
                        }
                    }

                    rewardCategoryItems.setRewardItems(rewardsList);
                    int itemCount = rewardsList.size();
                    Log.d("Firestore", "Collected " + itemCount + " items from Firestore");
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error fetching data: " + e.getMessage());
                });
    }


    @SuppressLint("SetTextI18n")
    private void fetchAndDisplayCurrentUserPoints() {
        String currentUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        DocumentReference userRef = firestore.collection("users").document(currentUserId);
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Long userPoints = documentSnapshot.getLong("userpoints");
                if (userPoints != null) {
                    currentUserPoints = userPoints;
                    currentuserPoints.setText("Current points: " + String.valueOf(currentUserPoints));
                }
            }
        }).addOnFailureListener(e -> {
            Log.e("RewardList", "Error fetching user points: " + e.getMessage());
        });
    }

    @SuppressLint("SetTextI18n")
    private void showCustomDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.items_selected_dialog, null);
        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        TextView allItemsTextView = dialogView.findViewById(R.id.all_items);
        TextView totalPointsTextView = dialogView.findViewById(R.id.totalpoints_items);
        dialogTitle.setText("Items Selected");
        totalPointsTextView.setText("Total points: " + String.valueOf(totalPoints));
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView)
                .setPositiveButton("Redeem", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        redeemSelectedItems();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.d("RewardList", "Dialog cancelled");
                    }
                });
        AlertDialog dialog = builder.create();
        StringBuilder allItemsText = new StringBuilder();
        for (RewardItems item : rewardItemsArrayList) {
            allItemsText.append(item.getRewardName()).append(" x").append(item.getSelectedquantity()).append(", ");
        }
        allItemsTextView.setText(allItemsText.toString());
        dialog.show();
    }

    @SuppressLint("SetTextI18n")
    // Import necessary classes

    private void redeemSelectedItems() {
        // Show a progress bar while redeeming
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Redeeming items...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        String couponCode = generateCouponCode();
        long newUserPoints = currentUserPoints - totalPoints;

        // Update the user's points in Firestore
        updatePointsInFirestore(userId, newUserPoints);
        deductStocksFromFirestore();

        // Assuming you have a "coupons" collection in your Firestore
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference couponsCollection = firestore.collection("coupons");

        // Create a new coupon document with the user's ID, coupon code, selected items, and isClaimed flag
        Map<String, Object> couponData = new HashMap<>();
        couponData.put("userId", userId);
        couponData.put("email", userEmail);
        couponData.put("couponCode", couponCode);
        couponData.put("isClaimed", false);

        // List to store the selected items with their quantities
        List<Map<String, Object>> selectedItemsList = new ArrayList<>();

        // Add selected items with quantities to the list
        for (RewardItems item : rewardItemsArrayList) {
            Map<String, Object> selectedItemData = new HashMap<>();
            selectedItemData.put("rewardId", item.getRewardName());
            selectedItemData.put("selectedQuantity", item.getSelectedquantity());
            selectedItemsList.add(selectedItemData);
        }

        // Add the list of selected items to the coupon data
        couponData.put("selectedItems", selectedItemsList);

        // Add the coupon to the "coupons" collection
        couponsCollection.whereEqualTo("userId", userId)
                .whereEqualTo("isClaimed", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int unclaimedItemCount = queryDocumentSnapshots.size();

                    if (unclaimedItemCount >= 3) {
                        Toast.makeText(getApplicationContext(),"Coupon capacity full, You have (3) unclaimed coupons.",Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                    else {
                        couponsCollection.add(couponData)
                                .addOnSuccessListener(documentReference -> {
                                    // Coupon added successfully
                                    Log.d("RewardList", "Coupon added with ID: " + documentReference.getId());

                                    // Clear the selected items list after redemption
                                    rewardItemsArrayList.clear();

                                    // Update the UI to reflect the changes
                                    totalPoints = 0;
                                    totalPointsTextView.setText("Total points: " + totalPoints);
                                    selectedItemsTextView.setText("Items: ");

                                    // Dismiss the progress bar
                                    progressDialog.dismiss();

                                    // Show success message
                                    Toast.makeText(getApplicationContext(), "Redeemed Successfully!", Toast.LENGTH_SHORT).show();

                                    // Navigate to the Reward activity after a delay
                                    new Handler().postDelayed(() -> {
                                        Intent i = new Intent(getApplicationContext(), Reward.class);
                                        startActivity(i);
                                        finish();
                                    }, 2000);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("RewardList", "Error adding coupon: " + e.getMessage());

                                    // Dismiss the progress bar
                                    progressDialog.dismiss();

                                    // Show an error message
                                    Toast.makeText(getApplicationContext(), "Redeem Failed. Please try again.", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("RewardList", "Error checking unclaimed items count: " + e.getMessage());
                    // Show an error message or take appropriate action
                    Toast.makeText(getApplicationContext(), "Error checking unclaimed items count", Toast.LENGTH_SHORT).show();
                });
    }



    private void updatePointsInFirestore(String userId, long newUserPoints) {
        DocumentReference userRef = firestore.collection("users").document(userId);
        userRef.update("userpoints", newUserPoints)
                .addOnSuccessListener(aVoid -> {
                    Log.d("RewardList", "User points updated to: " + newUserPoints);
                })
                .addOnFailureListener(e -> {
                    Log.e("RewardList", "Error updating user points: " + e.getMessage());
                });
    }

    @Override
    public void onItemClicked(RewardItems rewardItem, int position) {
        if (processingClick) {
            return;
        }
        processingClick = true;
        Log.d("RewardList", "Clicked on item: " + rewardItem.getRewardName());
        int selectedQuantity = rewardItem.getSelectedquantity();
        int pointsForItem = rewardItem.getPointsAsInt() * selectedQuantity;
        if (selectedQuantity > 0) {
            if (!rewardItemsArrayList.contains(rewardItem)) {
                totalPoints += pointsForItem;
                Log.d("RewardList", "Adding points for " + rewardItem.getRewardName() + ": " + pointsForItem);
                rewardItemsArrayList.add(rewardItem);
            }
        } else {
            if (rewardItemsArrayList.contains(rewardItem)) {
                totalPoints -= pointsForItem;
                Log.d("RewardList", "Subtracting points for " + rewardItem.getRewardName() + ": " + pointsForItem);
                rewardItemsArrayList.remove(rewardItem);
            }
        }

        totalPoints = 0;
        for (RewardItems item : rewardItemsArrayList) {
            totalPoints += item.getPointsAsInt() * item.getSelectedquantity();
        }
        // Update the UI on the main thread
        runOnUiThread(() -> {
            totalPointsTextView.setText("Total points: " + totalPoints);
            StringBuilder selectedItemsText = new StringBuilder("Items: ");
            for (RewardItems item : rewardItemsArrayList) {
                selectedItemsText.append(item.getRewardName()).append(" x").append(item.getSelectedquantity()).append(", ");
            }
            if (selectedItemsText.length() > 2) {
                selectedItemsText.delete(selectedItemsText.length() - 2, selectedItemsText.length());
            }
            selectedItemsTextView.setText(selectedItemsText.toString());
        });
        processingClick = false;
    }

    private void showPointsExceedDialog(long userPoints) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error")
                .setMessage(" Total points exceed your current points. You have " + userPoints + " points. ")
                .setPositiveButton("OK", (dialog, which) -> {
                    // Handle OK button click
                    dialog.dismiss();
                })
                .show();
    }

    private String generateCouponCode() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[8];
        secureRandom.nextBytes(randomBytes);

        String base64Code = Base64.getEncoder().encodeToString(randomBytes);
        return  base64Code.substring(0, 8);
    }


    private void deductStocksFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference rewardsCollection = db.collection("rewards");

        for (RewardItems item : rewardItemsArrayList) {
            // Get the rewardName for each selected reward
            String rewardName = item.getRewardName();

            // Query the rewards collection to find the document with the matching rewardName
            rewardsCollection.whereEqualTo("rewardName", rewardName)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {

                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                            int currentQuantity = document.getLong("quantity").intValue();
                            int newQuantity = currentQuantity - item.getSelectedquantity();
                            rewardsCollection.document(document.getId())
                                    .update("quantity", newQuantity)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("RewardList", "Stock quantity updated for " + rewardName + ": " + newQuantity);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("RewardList", "Error updating stock quantity for " + rewardName + ": " + e.getMessage());
                                    });
                        } else {
                            Log.e("RewardList", "Document does not exist for rewardName: " + rewardName);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("RewardList", "Error fetching document for rewardName: " + rewardName + ", Error: " + e.getMessage());
                    });
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, Reward.class);
        startActivity(intent);
        finish();
    }

}
