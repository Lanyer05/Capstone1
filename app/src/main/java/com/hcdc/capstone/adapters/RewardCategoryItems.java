package com.hcdc.capstone.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hcdc.capstone.R;
import com.hcdc.capstone.rewardprocess.RewardItems;

import java.util.ArrayList;

public class RewardCategoryItems extends RecyclerView.Adapter<RewardCategoryItems.RewardCategoryViewHolder> {

    private Context context;
    private ArrayList<RewardItems> rewardItemsList;

    public RewardCategoryItems(Context context,ArrayList<RewardItems> rewardItemsList) {
        this.context = context;
        this.rewardItemsList = rewardItemsList;
    }

    @NonNull
    @Override
    public RewardCategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_reward_items, parent, false);
        return new RewardCategoryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RewardCategoryViewHolder holder, int position) {
        RewardItems rewardItems = rewardItemsList.get(position);
        holder.title.setText(rewardItems.getRewardName());
        holder.points.setText("Points needed: " + rewardItems.getrewardPoints());
        holder.quantity.setText("Items left: " + rewardItems.getQuantity());
    }

    @Override
    public int getItemCount() {
        return rewardItemsList.size();
    }

    public void setRewardItems(ArrayList<RewardItems> rewardItemsList) {
        this.rewardItemsList = rewardItemsList;
        notifyDataSetChanged();
    }

    public static class RewardCategoryViewHolder extends RecyclerView.ViewHolder {

        TextView title, points, quantity;

        public RewardCategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.rewarditem_title);
            points = itemView.findViewById(R.id.rewarditem_points);
            quantity = itemView.findViewById(R.id.rewarditem_quantity);
        }
    }
}
