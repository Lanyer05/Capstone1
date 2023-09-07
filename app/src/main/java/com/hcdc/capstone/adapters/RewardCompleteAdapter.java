package com.hcdc.capstone.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hcdc.capstone.R;
import com.hcdc.capstone.rewardprocess.Coupons;

import java.util.ArrayList;

public class RewardCompleteAdapter extends RecyclerView.Adapter<RewardCompleteAdapter.RewardViewHolder> {

    private final Context finRewardContext;
    private ArrayList<Coupons> finRewardList;

    public RewardCompleteAdapter(Context finRewardContext, ArrayList<Coupons> finRewardList) {
        this.finRewardContext = finRewardContext;
        this.finRewardList = finRewardList;
    }


    @NonNull
    @Override
    public RewardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(finRewardContext).inflate(R.layout.cardtransactreward, parent, false);
        return new RewardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RewardViewHolder holder, int position) {
        Coupons coupons = finRewardList.get(position);
        holder.finRewardCode.setText(coupons.getUsercouponCode());
        holder.finRewardName.setText(coupons.getRewardName());
    }

    @Override
    public int getItemCount() {
        return finRewardList.size();
    }

    public void setFinRewardList(ArrayList<Coupons> claimedRewards) {
        finRewardList = claimedRewards;
        notifyDataSetChanged();
    }

    public static class RewardViewHolder extends RecyclerView.ViewHolder {

        TextView finRewardName, finRewardCode;

        public RewardViewHolder(@NonNull View itemView) {
            super(itemView);
            finRewardCode = itemView.findViewById(R.id.fincoupCode);
            finRewardName = itemView.findViewById(R.id.fincoupRewardName);
        }
    }
}
