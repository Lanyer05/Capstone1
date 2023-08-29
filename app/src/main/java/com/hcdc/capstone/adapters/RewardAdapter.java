package com.hcdc.capstone.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hcdc.capstone.R;
import com.hcdc.capstone.taskprocess.RewardRequest;
import com.hcdc.capstone.taskprocess.Rewards;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Map;

public class RewardAdapter extends RecyclerView.Adapter<RewardAdapter.RewardViewHolder>{

    Context Rcontext;
    ArrayList<Rewards> Rlist;

    String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    public RewardAdapter (Context Rcontext, ArrayList<Rewards> Rlist)
    {
        this.Rcontext = Rcontext;
        this.Rlist = Rlist;
    }

    @NonNull
    @Override
    public RewardAdapter.RewardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(Rcontext).inflate(R.layout.rewardslayout,parent,false);
        return new RewardAdapter.RewardViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RewardAdapter.RewardViewHolder holder, int position) {
        Rewards rewards = Rlist.get(position);
        holder.rewardname.setText(rewards.getRewardName());
        holder.rewardpoint.setText(rewards.getPoints() + " points");

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder rewardBuilder = new AlertDialog.Builder(Rcontext);
                View rewardPopup = LayoutInflater.from(Rcontext).inflate(R.layout.reward_dialog, null);

                TextView rwrd = rewardPopup.findViewById(R.id.userRemainingPoints);
                TextView rwrdtitle = rewardPopup.findViewById(R.id.getRewardTitle);
                TextView rwrdpoint = rewardPopup.findViewById(R.id.getRewardPoint);

                AppCompatImageButton closerwrd = rewardPopup.findViewById(R.id.rewardclose);
                AppCompatButton reqrwrd = rewardPopup.findViewById(R.id.requestReward);

                rwrdtitle.setText(rewards.getRewardName());
                rwrdpoint.setText("Required points to claim: " + rewards.getPoints() + " points");


                rewardBuilder.setView(rewardPopup);
                AlertDialog alertDialog = rewardBuilder.create();
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                alertDialog.show();



                // Fetch user's points from Firestore
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference userRef = db.collection("users").document(currentUserId);
                userRef.get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> userData = documentSnapshot.getData();
                        if (userData != null && userData.containsKey("userpoints")) {
                            Long userPointsLong = (Long) userData.get("userpoints");
                            int userPoints = userPointsLong != null ? userPointsLong.intValue() : 0;
                            rwrd.setText("Points Available:  " + userPoints);
                        }
                    }
                }).addOnFailureListener(e -> {
                    // Handle failure
                    Toast.makeText(Rcontext, "Failed to fetch user points.", Toast.LENGTH_SHORT).show();
                });



                closerwrd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });

                reqrwrd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Fetch user's points from Firestore
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        DocumentReference userRef = db.collection("users").document(currentUserId);
                        userRef.get().addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                Map<String, Object> userData = documentSnapshot.getData();
                                if (userData != null && userData.containsKey("userpoints")) {
                                    Long userPointsLong = (Long) userData.get("userpoints");
                                    int userPoints = userPointsLong != null ? userPointsLong.intValue() : 0;

                                    // Convert rewards.getPoints() to integer
                                    int requiredPoints = Integer.parseInt(rewards.getPoints());

                                    if (userPoints >= requiredPoints) {
                                        // Check for existing pending reward requests
                                        db.collection("rewardrequest")
                                                .whereEqualTo("userId", currentUserId)
                                                .whereEqualTo("pendingStatus", true)
                                                .get()
                                                .addOnSuccessListener(querySnapshot -> {
                                                    if (querySnapshot.isEmpty()) {
                                                        // No pending requests, proceed with reward request
                                                        db.collection("rewardrequest")
                                                                .add(new RewardRequest(rewards.getRewardName(), currentUserId, true))
                                                                .addOnSuccessListener(documentReference -> {
                                                                    // Display a success dialog after the reward request is added successfully
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
                                                    } else {
                                                        // User already has a pending request
                                                        AlertDialog.Builder pendingRequestDialog = new AlertDialog.Builder(Rcontext);
                                                        pendingRequestDialog.setTitle("Pending Request");
                                                        pendingRequestDialog.setMessage("You already have a pending reward request. Please wait for it to be accepted.");
                                                        pendingRequestDialog.setPositiveButton("OK", (dialog, which) -> {
                                                            dialog.dismiss();
                                                        });
                                                        pendingRequestDialog.create().show();
                                                    }
                                                })
                                                .addOnFailureListener(e -> {
                                                    // Handle failure
                                                    Toast.makeText(Rcontext, "Failed to check existing requests.", Toast.LENGTH_SHORT).show();
                                                });
                                    } else {
                                        // User has insufficient points
                                        AlertDialog.Builder insufficientPointsDialog = new AlertDialog.Builder(Rcontext);
                                        insufficientPointsDialog.setTitle("Insufficient Points");
                                        insufficientPointsDialog.setMessage("You do not have enough points to claim this reward.");
                                        insufficientPointsDialog.setPositiveButton("OK", (dialog, which) -> {
                                            dialog.dismiss();
                                        });
                                        insufficientPointsDialog.create().show();
                                    }
                                }
                            }
                        }).addOnFailureListener(e -> {
                            // Handle failure
                            Toast.makeText(Rcontext, "Failed to fetch user points.", Toast.LENGTH_SHORT).show();
                        });
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

        TextView rewardname, rewardpoint;
        public RewardViewHolder(@NonNull View rewardView) {
            super(rewardView);

            rewardname = rewardView.findViewById(R.id.rewardTitle);
            rewardpoint = rewardView.findViewById(R.id.rewardPoint);
        }
    }
}
