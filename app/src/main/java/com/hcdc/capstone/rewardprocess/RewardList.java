package com.hcdc.capstone.rewardprocess;

import android.annotation.SuppressLint;
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
import com.google.firebase.firestore.WriteBatch;
import com.hcdc.capstone.Homepage;
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

        // Fetch data from Firestore and set the adapter after successful retrieval
        firestore = FirebaseFirestore.getInstance();
        fetchDataFromFirestore();
        fetchAndDisplayCurrentUserPoints();

        checkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (totalPoints > currentUserPoints) {
                    // Total points exceed user points, show error dialog
                    showPointsExceedDialog(currentUserPoints);
                } else if (totalPoints <= 0) {
                    Toast.makeText(getApplicationContext(), "No items selected to be redeemed", Toast.LENGTH_SHORT).show();
                } else {
                    // Total points are within the user points limit, proceed with the checkout
                    showCustomDialog();
                }
            }
        });
    }

    private void fetchDataFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference rewardsCollection = db.collection("rewards"); // Replace with your collection name

        rewardsCollection.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Handle successful data retrieval
                    // Convert the QuerySnapshot to a list of RewardItems
                    ArrayList<RewardItems> rewardsList = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        RewardItems reward = document.toObject(RewardItems.class);
                        rewardsList.add(reward);
                    }

                    // Update the RecyclerView adapter with the retrieved data
                    rewardCategoryItems.setRewardItems(rewardsList);

                    // Log the count of collected data
                    int itemCount = rewardsList.size();
                    Log.d("Firestore", "Collected " + itemCount + " items from Firestore");
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Log.e("Firestore", "Error fetching data: " + e.getMessage());
                });
    }

    private void fetchAndDisplayCurrentUserPoints() {
        // Get the current user's UID
        String currentUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        // Create a reference to the user's document
        DocumentReference userRef = firestore.collection("users").document(currentUserId);

        // Get the user's points field and set it to the TextView
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Assuming you have a field named "userpoints" in the document
                Long userPoints = documentSnapshot.getLong("userpoints");
                if (userPoints != null) {
                    currentUserPoints = userPoints; // Store the user points
                    currentuserPoints.setText("Current points: " + String.valueOf(currentUserPoints));
                }
            }
        }).addOnFailureListener(e -> {
            // Handle failure
            Log.e("RewardList", "Error fetching user points: " + e.getMessage());
        });
    }

    private void showCustomDialog() {
        // Inflate the custom dialog layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.items_selected_dialog, null);

        // Find views in the custom dialog layout
        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        TextView allItemsTextView = dialogView.findViewById(R.id.all_items);
        TextView totalpowents = dialogView.findViewById(R.id.totalpoints_items);

        // Set text or perform any other customization as needed
        dialogTitle.setText("Items Selected");
        totalpowents.setText("Total points: " + String.valueOf(totalPoints));

        // Build the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView)
                .setPositiveButton("Redeem", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Handle positive button click
                        redeemSelectedItems();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Handle cancel action
                        Log.d("RewardList", "Dialog cancelled");
                    }
                });

        // Show the AlertDialog
        AlertDialog dialog = builder.create();

        // Build the string for all selected items
        StringBuilder allItemsText = new StringBuilder();
        for (RewardItems item : rewardItemsArrayList) {
            allItemsText.append(item.getRewardName()).append(" x").append(item.getSelectedquantity()).append(", ");
        }
        // Set the text to the TextView
        allItemsTextView.setText(allItemsText.toString());

        dialog.show();
    }

    private void redeemSelectedItems() {
        // Generate a unique coupon code for the user
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String couponCode = generateCouponCode(userId);

        // Calculate the new user points after deduction
        long newUserPoints = currentUserPoints - totalPoints;

        // Update the user's points in Firestores
        updatePointsInFirestore(userId, newUserPoints);

        // Assuming you have a "coupons" collection in your Firestore
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference couponsCollection = firestore.collection("coupons");

        // Create a new coupon document with the user's ID, coupon code, selected items, and isClaimed flag
        Map<String, Object> couponData = new HashMap<>();
        couponData.put("userId", userId);
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
        couponsCollection.add(couponData)
                .addOnSuccessListener(documentReference -> {
                    // Coupon added successfully
                    Log.d("RewardList", "Coupon added with ID: " + documentReference.getId());

                    // TODO: You may want to update your database or perform other actions here


                    // Clear the selected items list after redemption
                    rewardItemsArrayList.clear();

                    // Update the UI to reflect the changes
                    totalPoints = 0;
                    totalPointsTextView.setText("Total points: " + totalPoints);
                    selectedItemsTextView.setText("Items added: ");

                  //  updateClaimedQuantityInFirestore();


                    // Delay the transition to the Homepage
                    new Handler().postDelayed(() -> {
                        Toast.makeText(getApplicationContext(), "Redeemed Successfully!", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(getApplicationContext(), Homepage.class);
                        startActivity(i);
                        finish();
                    }, 2000); // 2000 milliseconds (adjust as needed)
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Log.e("RewardList", "Error adding coupon: " + e.getMessage());
                });
    }


    private void updatePointsInFirestore(String userId, long newUserPoints) {
        // Reference to the user's document
        DocumentReference userRef = firestore.collection("users").document(userId);

        // Update the user's points field in Firestore
        userRef.update("userpoints", newUserPoints)
                .addOnSuccessListener(aVoid -> {
                    // User points updated successfully
                    Log.d("RewardList", "User points updated to: " + newUserPoints);
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Log.e("RewardList", "Error updating user points: " + e.getMessage());
                });
    }

    @Override
    public void onItemClicked(RewardItems rewardItem, int position) {
        // Disable further clicks while processing
        if (processingClick) {
            return;
        }

        processingClick = true;

        Log.d("RewardList", "Clicked on item: " + rewardItem.getRewardName());

        int selectedQuantity = rewardItem.getSelectedquantity();

        // Calculate points for the selected quantity and item
        int pointsForItem = rewardItem.getPointsAsInt() * selectedQuantity;

        if (selectedQuantity > 0) {
            // Check for duplicates before adding
            if (!rewardItemsArrayList.contains(rewardItem)) {
                totalPoints += pointsForItem; // Add points
                Log.d("RewardList", "Adding points for " + rewardItem.getRewardName() + ": " + pointsForItem);
                rewardItemsArrayList.add(rewardItem);
            }
        } else {
            // Check for duplicates before removing
            if (rewardItemsArrayList.contains(rewardItem)) {
                totalPoints -= pointsForItem; // Subtract points
                Log.d("RewardList", "Subtracting points for " + rewardItem.getRewardName() + ": " + pointsForItem);
                rewardItemsArrayList.remove(rewardItem);
            }
        }

        // Recalculate total points by iterating through the array
        totalPoints = 0;
        for (RewardItems item : rewardItemsArrayList) {
            totalPoints += item.getPointsAsInt() * item.getSelectedquantity();
        }

        // Update the UI on the main thread
        runOnUiThread(() -> {
            totalPointsTextView.setText("Total points: " + totalPoints);
            StringBuilder selectedItemsText = new StringBuilder("Items added: ");
            for (RewardItems item : rewardItemsArrayList) {
                selectedItemsText.append(item.getRewardName()).append(" x").append(item.getSelectedquantity()).append(", ");
            }
            if (selectedItemsText.length() > 2) {
                selectedItemsText.delete(selectedItemsText.length() - 2, selectedItemsText.length()); // Remove the trailing comma and space
            }
            selectedItemsTextView.setText(selectedItemsText.toString());
        });

        // Enable further clicks after processing is completed
        processingClick = false;
    }

    private void showPointsExceedDialog(long userPoints) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error")
                .setMessage("Total points exceed your current points. You have " + userPoints + " points.")
                .setPositiveButton("OK", (dialog, which) -> {
                    // Handle OK button click
                    dialog.dismiss();
                })
                .show();
    }

    private String generateCouponCode(String userId) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[8];
        secureRandom.nextBytes(randomBytes);

        String base64Code = Base64.getEncoder().encodeToString(randomBytes);
        return userId + "-" + base64Code.substring(0, 8);
    }

    // update rewards quantity
    private void updateClaimedQuantityInFirestore() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        WriteBatch batch = firestore.batch();

        for (RewardItems rewardItem : rewardItemsArrayList) {
            String rewardId = rewardItem.getRewardName();
            int selectedQuantity = rewardItem.getSelectedquantity();

            DocumentReference rewardRef = firestore.collection("rewards").document(rewardId);

            // Retrieve the current quantity of the reward
            rewardRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String currentQuantityStr = documentSnapshot.getString("quantity");

                    // Convert the current quantity from string to integer
                    int currentQuantity = Integer.parseInt(currentQuantityStr);

                    // Calculate the new quantity after deduction
                    int newQuantity = currentQuantity - selectedQuantity;

                    // Convert the new quantity to string
                    String newQuantityStr = String.valueOf(newQuantity);

                    // Update the quantity field in Firestore
                    batch.update(rewardRef, "quantity", newQuantityStr);
                }
            }).addOnFailureListener(e -> {
                // Handle failure
                Log.e("RewardList", "Error updating claimed quantity: " + e.getMessage());
            });
        }

        // Commit the batch write
        batch.commit().addOnSuccessListener(aVoid -> {
            // Batch write successful
            Log.d("RewardList", "Claimed quantities updated successfully");

            // Continue with redeeming the items
            redeemSelectedItems();
        }).addOnFailureListener(e -> {
            // Handle failure
            Log.e("RewardList", "Error committing batch writes: " + e.getMessage());
        });
    }

}
