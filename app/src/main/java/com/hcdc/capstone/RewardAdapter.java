package com.hcdc.capstone;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

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
        holder.rewardpoint.setText(rewards.getPoints()+" points");

        Log.d("RewardAdapter", "Binding task: " + rewards.getRewardName());
        Log.d("RewardAdapter", "Binding task: " + rewards.getPoints());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               View rewardPopup = LayoutInflater.from(Rcontext).inflate(R.layout.reward_dialog,null);

                AlertDialog.Builder rewardBuilder = new AlertDialog.Builder(Rcontext);

                TextView rwrd = rewardPopup.findViewById(R.id.userRemainingPoints);
                TextView rwrdtitle = rewardPopup.findViewById(R.id.getRewardTitle);
                TextView rwrdpoint = rewardPopup.findViewById(R.id.getRewardPoint);

                @SuppressLint({"MissingInflatedId", "LocalSuppress"})
                AppCompatButton closerwrd = rewardPopup.findViewById(R.id.rewardclose);
                @SuppressLint({"MissingInflatedId", "LocalSuppress"})
                AppCompatButton reqrwrd = rewardPopup.findViewById(R.id.requestReward);


                rwrd.setText("Remaining Points: + userpointHere");
                rwrdpoint.setText(rewards.getPoints());
                rwrdtitle.setText(rewards.getRewardName());

                AlertDialog alertDialog = rewardBuilder.create();
                alertDialog.show();

                closerwrd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.cancel();
                    }
                });

                reqrwrd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // Create a new reward request document in the "rewardrequest" collection
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("rewardrequest")
                                .add(new RewardRequest(rewards.getRewardName(), currentUserId))
                                .addOnSuccessListener(documentReference -> {
                                    // Reward request added successfully
                                    Toast.makeText(Rcontext, "Reward requested!", Toast.LENGTH_SHORT).show();
                                    alertDialog.cancel();
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
