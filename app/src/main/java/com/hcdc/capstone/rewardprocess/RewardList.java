package com.hcdc.capstone.rewardprocess;

import static java.lang.Integer.parseInt;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.hcdc.capstone.R;
import com.hcdc.capstone.adapters.RewardCategoryItems;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

public class RewardList extends AppCompatActivity implements RewardCategoryItems.RewardItemClickListener {

    RecyclerView recyclerView;
    RewardCategoryItems rewardCategoryItems;
    ArrayList<RewardItems> rewardItemsArrayList;

    FirebaseFirestore firestore;

    TextView currentuserPoints ;
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
                    Toast.makeText(getApplicationContext(),"No items selected to be redeemed",Toast.LENGTH_SHORT).show();
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
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
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

        // Set text or perform any other customization as needed
        dialogTitle.setText("Items Selected");

        // Build the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView)
                .setPositiveButton("Redeem", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Handle positive button click
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
            allItemsText.append(item.getRewardName()).append(" x").append(item.getSelectedquantity()).append("\n");
        }
        // Set the text to the TextView
        allItemsTextView.setText(allItemsText.toString());

        dialog.show();
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
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Handle OK button click
                        dialog.dismiss();
                    }
                })
                .show();
    }


}
