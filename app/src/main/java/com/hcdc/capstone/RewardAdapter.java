package com.hcdc.capstone;

import android.annotation.SuppressLint;
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
import com.google.firebase.firestore.auth.User;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import org.checkerframework.checker.units.qual.C;

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
                    //to add if else for point modifiers lacks many shit
                    public void onClick(View v) {
                        // Create a new reward request document in the "rewardrequest" collection
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("rewardrequest")
                                .add(new RewardRequest(rewards.getRewardName(), currentUserId))
                                .addOnSuccessListener(documentReference -> {
                                    // Reward request added successfully
                                    Toast.makeText(Rcontext, "Reward requested!", Toast.LENGTH_SHORT).show();
                                    alertDialog.dismiss();
                                })
                                .addOnFailureListener(e -> {
                                    // Handle failure
                                    Toast.makeText(Rcontext, "Reward request failed.", Toast.LENGTH_SHORT).show();
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
