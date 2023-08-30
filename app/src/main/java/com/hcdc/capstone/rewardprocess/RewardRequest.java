package com.hcdc.capstone.rewardprocess;

public class RewardRequest {
    private String rewardName;
    private String userId;
    private boolean pendingStatus;
    private String userEmail;
    private int rewardPoints;

    public RewardRequest() {
        // Default constructor required for Firestore
    }

    public RewardRequest(String rewardName, String userId, boolean pendingStatus, String userEmail, int rewardPoints) {
        this.rewardName = rewardName;
        this.userId = userId;
        this.pendingStatus = pendingStatus;
        this.userEmail = userEmail;
        this.rewardPoints = rewardPoints; // Fix: Assign rewardPoints here
    }

    public String getRewardName() {
        return rewardName;
    }

    public String getUserId() {
        return userId;
    }

    public boolean isPendingStatus() {
        return pendingStatus;
    }

    public String getEmail() {
        return userEmail;
    }

    public int getRewardPoints() { // Fix: Method name should start with lowercase
        return rewardPoints;
    }
}
