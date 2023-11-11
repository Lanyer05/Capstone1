package com.hcdc.capstone.rewardprocess;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
import java.util.Objects;

public class RewardList extends AppCompatActivity {

    RecyclerView recyclerView;
    RewardCategoryItems rewardCategoryItems;
    ArrayList<RewardItems> rewardItemsArrayList;

    FirebaseFirestore firestore;

    TextView currentuserPoints;

    Button checkout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward_list);

        currentuserPoints = findViewById(R.id.userPoints);
        checkout = findViewById(R.id.checkout_button);

        recyclerView = findViewById(R.id.rewardrecycler);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        rewardItemsArrayList = new ArrayList<>();
        rewardCategoryItems = new RewardCategoryItems(this, rewardItemsArrayList);
        recyclerView.setAdapter(rewardCategoryItems);

        // Fetch data from Firestore and set the adapter after successful retrieval
        firestore = FirebaseFirestore.getInstance();
        fetchDataFromFirestore();
        fetchAndDisplayCurrentUserPoints();

        checkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomDialog();
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
                    currentuserPoints.setText("Current points: "+ String.valueOf(userPoints));
                }
            }
        }).addOnFailureListener(e -> {
        });
        batch.commit().addOnSuccessListener(aVoid -> {
        }).addOnFailureListener(e -> {
        });
    }

    private void showCustomDialog() {
        // Inflate the custom dialog layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.items_selected_dialog, null);

        // Find views in the custom dialog layout
        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        EditText userInputEditText = dialogView.findViewById(R.id.userInput);
        Button dialogButton = dialogView.findViewById(R.id.dialogButton);

        // Set text or perform any other customization as needed
        dialogTitle.setText("Custom Dialog Title");

        // Build the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Handle positive button click
                        String userInput = userInputEditText.getText().toString();
                        Log.d("RewardList", "User input: " + userInput);
                        // You can add logic here to perform actions based on user input
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
        dialog.show();
    }
}
