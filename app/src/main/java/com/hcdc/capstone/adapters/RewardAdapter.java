package com.hcdc.capstone.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.hcdc.capstone.R;
import com.hcdc.capstone.rewardprocess.RewardItems;
import com.hcdc.capstone.rewardprocess.RewardRequest;
import com.hcdc.capstone.rewardprocess.RewardsData;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import java.security.SecureRandom;
import java.util.ArrayList;

public class RewardAdapter extends RecyclerView.Adapter<RewardAdapter.RewardViewHolder>{

    Context Rcontext;
    ArrayList<RewardsData> Rlist;
    ArrayList<RewardItems> RIlist;

    RecyclerView recyclerView;

    RewardCategoryItems rewardCategoryItems;

    AlertDialog alertDialog;

    String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    public RewardAdapter(Context Rcontext, ArrayList<RewardsData> Rlist,  ArrayList<RewardItems> RIlist;) {
        this.Rcontext = Rcontext;
        this.Rlist = Rlist;
    }

    @NonNull
    @Override
    public RewardAdapter.RewardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(Rcontext).inflate(R.layout.rewardslayout, parent, false);
        return new RewardAdapter.RewardViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RewardAdapter.RewardViewHolder holder, int position) {
        RewardsData rewardsData = Rlist.get(position);
        holder.category.setText(rewardsData.getCategory());
        holder.rewardpoint.setText(rewardsData.getPoints() + " points");

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder rewardBuilder = new AlertDialog.Builder(Rcontext);
                View rewardPopup = LayoutInflater.from(Rcontext).inflate(R.layout.reward_dialog, null);


                rewardBuilder.setView(rewardPopup);
                alertDialog = rewardBuilder.create();
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                alertDialog.show();

                // Find the RecyclerView inside the rewardPopup
                rewardCategoryItems = new RewardCategoryItems(Rcontext, RIlist);
                RecyclerView recyclerView = rewardPopup.findViewById(R.id.rewarditemrecycler);
                recyclerView.setAdapter(rewardCategoryItems);

                Log.d("RIlistSize", "Size of RIlist: " + RIlist.size());


                AppCompatImageButton closerwrd = rewardPopup.findViewById(R.id.rewardclose);

                // Fetch user's points from Firestore
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference categoryRef = db.collection("categories").document(rewardsData.getCategory()); // Assuming 'category' is the category ID
                categoryRef.get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String categoryName = documentSnapshot.getString("category"); // Replace with your actual field name
                        holder.category.setText(categoryName);
                    }
                }).addOnFailureListener(e -> {
                    // Handle failure
                    Toast.makeText(Rcontext, "Failed to fetch category name.", Toast.LENGTH_SHORT).show();
                });

                closerwrd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });

            }
        });
    }

    @Override
    public int getItemCount() {
        return Rlist.size();
    }

    public static class RewardViewHolder extends RecyclerView.ViewHolder{

        TextView category, rewardpoint;
        public RewardViewHolder(@NonNull View rewardView) {
            super(rewardView);
            category = rewardView.findViewById(R.id.rewardTitle);
            rewardpoint = rewardView.findViewById(R.id.rewardPoint);
        }
    }

    private String generateCouponCode(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder couponCode = new StringBuilder(length);
        SecureRandom random = new SecureRandom();

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(characters.length());
            char randomChar = characters.charAt(randomIndex);
            couponCode.append(randomChar);
        }
        String generatedCode = couponCode.toString();
        Log.d("CouponGeneration", "Generated Coupon Code: " + generatedCode); // Add this log
        return generatedCode;
    }

    // Method to generate a unique coupon code
    private void generateUniqueCouponCode(FirebaseFirestore db, RewardsData rewardsData) {
        String couponCode = generateCouponCode(11);

        // Check if the coupon code already exists in Firestore
        db.collection("rewardrequest")
                .whereEqualTo("couponCode", couponCode)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        // The coupon code is unique, proceed with adding the reward request
                      //  addRewardRequestToFirestore(db, rewardsData, couponCode);
                    } else {
                        // The coupon code already exists, generate a new one
                        generateUniqueCouponCode(db, rewardsData);
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Toast.makeText(Rcontext, "Failed to check coupon code uniqueness.", Toast.LENGTH_SHORT).show();
                });
    }

    // Method to add reward request to Firestore
    /*private void addRewardRequestToFirestore(FirebaseFirestore db, RewardsData rewardsData, String couponCode) {
        // Batch write: Add reward request and update user's points
        WriteBatch batch = db.batch();
        DocumentReference rewardRequestRef = db.collection("rewardrequest").document();
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        int rewardPoints = Integer.parseInt(rewardsData.getPoints()); // Get reward points
        batch.set(rewardRequestRef, new RewardRequest(rewardsData.getCategory(), currentUserId, true, userEmail, rewardPoints, couponCode));

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    // Display a success dialog after the batch write
                    AlertDialog.Builder successDialog = new AlertDialog.Builder(Rcontext);
                    successDialog.setTitle("Reward Request Successful");
                    successDialog.setMessage("Your reward request has been submitted. Please wait for it to be processed.");
                    successDialog.setPositiveButton("OK", (dialog, which) -> {
                        dialog.dismiss();
                        alertDialog.dismiss();  // Dismiss the original reward dialog
                    });
                    successDialog.create().show();
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Toast.makeText(Rcontext, "Reward request failed.", Toast.LENGTH_SHORT).show();
                });
    }*/
}