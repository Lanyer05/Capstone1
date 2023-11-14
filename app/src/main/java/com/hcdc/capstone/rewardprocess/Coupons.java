package com.hcdc.capstone.rewardprocess;

import com.google.firebase.Timestamp;

import java.util.Date;
import java.util.List;

public class Coupons {

    private String userId;
    private String couponCode;
    private Timestamp claimDateTime;
    private List<SelectedItems> selectedItems;

    public Coupons() {
        // Default constructor required for Firestore
    }

    public Coupons(String userId, String couponCode, List<SelectedItems> selectedItems) {
        this.userId = userId;
        this.couponCode = couponCode;
        this.selectedItems = selectedItems;
    }

    public Coupons(String userId, String couponCode, List<SelectedItems> selectedItems, Timestamp claimDateTime) {
        this.userId = userId;
        this.couponCode = couponCode;
        this.selectedItems = selectedItems;
        this.claimDateTime = claimDateTime;
    }

    public String getUserId() {
        return userId;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public Timestamp getClaimDate() {
        return claimDateTime != null ? claimDateTime : new Timestamp(new Date());
    }

    public List<SelectedItems> getSelectedItems() {
        return selectedItems;
    }

    // Add setters if needed

    public static class SelectedItems {
        private String rewardId;
        private int selectedQuantity;

        public SelectedItems() {
            // Default constructor required for Firestore
        }

        public SelectedItems(String rewardId, int selectedQuantity) {
            this.rewardId = rewardId;
            this.selectedQuantity = selectedQuantity;
        }

        public String getRewardId() {
            return rewardId;
        }

        public int getSelectedQuantity() {
            return selectedQuantity;
        }
    }
}
