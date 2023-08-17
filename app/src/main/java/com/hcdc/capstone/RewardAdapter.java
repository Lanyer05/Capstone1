package com.hcdc.capstone;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RewardAdapter extends RecyclerView.Adapter<RewardAdapter.RewardViewHolder>{

    Context Rcontext;
    ArrayList<Rewards> Rlist;

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
