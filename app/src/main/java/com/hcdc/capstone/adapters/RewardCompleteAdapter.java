package com.hcdc.capstone.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.hcdc.capstone.R;
import com.hcdc.capstone.rewardprocess.Coupons;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

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

        // Format the Timestamp to "MM/DD/YYYY HH:MM" and set it to TextView
        String formattedClaimDate = formatTimestamp(coupons.getClaimDate());
        holder.finClaimDate.setText(formattedClaimDate);
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

        TextView finRewardName, finRewardCode, finClaimDate;

        public RewardViewHolder(@NonNull View itemView) {
            super(itemView);
            finRewardCode = itemView.findViewById(R.id.fincoupCode);
            finRewardName = itemView.findViewById(R.id.fincoupRewardName);
            finClaimDate = itemView.findViewById(R.id.coupRewardDate); // Add TextView for claimDate
        }
    }

    // Helper method to format Timestamp
    private String formatTimestamp(Timestamp timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy (hh:mma)", Locale.US);
        return sdf.format(new Date(timestamp.getSeconds() * 1000));
    }
}
