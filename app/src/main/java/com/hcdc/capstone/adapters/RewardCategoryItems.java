package com.hcdc.capstone.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.hcdc.capstone.R;
import com.hcdc.capstone.rewardprocess.RewardItems;
import com.hcdc.capstone.rewardprocess.RewardsData;

import java.util.ArrayList;

public class RewardCategoryItems extends RecyclerView.Adapter<RewardCategoryItems.RewardCategoryViewHolder> {

    Context Rcontext;
    ArrayList<RewardItems> Rcategoitems;

    public RewardCategoryItems(Context Rcontext, ArrayList<RewardItems> Rcategoitems) {
        this.Rcontext = Rcontext;
        this.Rcategoitems = Rcategoitems;
    }

    @NonNull
    @Override
    public RewardCategoryItems.RewardCategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(Rcontext).inflate(R.layout.card_reward_items, parent, false);
        return new RewardCategoryItems.RewardCategoryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RewardCategoryItems.RewardCategoryViewHolder holder, int position) {
        RewardItems rewardItems = Rcategoitems.get(position);
        holder.title.setText(rewardItems.getRewardName());
        holder.points.setText("Points needed: "+rewardItems.getrewardPoints());
        holder.quantity.setText("Items left:"+rewardItems.getQuantity());
    }

    @Override
    public int getItemCount() {
        return Rcategoitems.size();
    }

    public static class RewardCategoryViewHolder extends RecyclerView.ViewHolder{

        TextView title, points, quantity;

        public RewardCategoryViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.rewarditem_title);
            points = itemView.findViewById(R.id.rewarditem_points);
            quantity = itemView.findViewById(R.id.rewarditem_quantity);
        }
    }
}
