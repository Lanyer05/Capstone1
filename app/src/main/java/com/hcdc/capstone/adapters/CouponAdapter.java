package com.hcdc.capstone.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.hcdc.capstone.R;
import com.hcdc.capstone.rewardprocess.Coupons;

import java.util.ArrayList;

public class CouponAdapter extends RecyclerView.Adapter<CouponAdapter.CouponViewHolder> {

    Context coupContext;
    ArrayList<Coupons> coupList;

    public CouponAdapter(Context coupContext,  ArrayList<Coupons> coupList)
    {
        this.coupContext = coupContext;
        this.coupList = coupList;
    }
    @NonNull
    @Override
    public CouponAdapter.CouponViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(coupContext).inflate(R.layout.couponlayout,parent,false);
        return new CouponAdapter.CouponViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CouponAdapter.CouponViewHolder holder, int position) {
        Coupons coupons = coupList.get(position);

        holder.coupCod.setText(coupons.getCouponCode());

        // Iterate through the list of SelectedItems and concatenate the data
        StringBuilder selectedItemsText = new StringBuilder();
        for (Coupons.SelectedItems selectedItem : coupons.getSelectedItems()) {
            String rewardId = selectedItem.getRewardId();
            int selectedQuantity = selectedItem.getSelectedQuantity();

            // Concatenate the data into a single string
            String itemText = rewardId + " x" + selectedQuantity + ", ";
            selectedItemsText.append(itemText);
        }

        // Remove the trailing comma and space
        if (selectedItemsText.length() > 2) {
            selectedItemsText.delete(selectedItemsText.length() - 2, selectedItemsText.length());
        }

        // Set the concatenated string to the TextView
        holder.coupName.setText(selectedItemsText.toString());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "You clicked me",Toast.LENGTH_SHORT).show();
                showCustomDialog(coupons);
            }
        });
    }

    @Override
    public int getItemCount() {
        return coupList.size(); // Return the size of your coupList
    }


    public static class CouponViewHolder extends RecyclerView.ViewHolder{

        TextView coupCod, coupName;
        public CouponViewHolder(@NonNull View itemView) {
            super(itemView);

            coupCod = itemView.findViewById(R.id.coupCode);
            coupName= itemView.findViewById(R.id.coupRewardName);
        }
    }
    private void showCustomDialog(Coupons coupons) {
        AlertDialog.Builder builder = new AlertDialog.Builder(coupContext);
        View dialogView = LayoutInflater.from(coupContext).inflate(R.layout.coupon_details, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Find the views in the custom dialog
        TextView couponCode = dialogView.findViewById(R.id.textCoupon);
        TextView couponItems = dialogView.findViewById(R.id.textCouponList);
        ImageButton closeButton = dialogView.findViewById(R.id.closeDialog);

        // Set the title and description in the dialog
        couponCode.setText("Sample muna");
        couponItems.setText("blank text muna");

        // Close the dialog when the "Close" button is clicked
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }
}
