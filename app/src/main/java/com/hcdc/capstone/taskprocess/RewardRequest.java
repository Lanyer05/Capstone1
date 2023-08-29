package com.hcdc.capstone.taskprocess;

public class RewardRequest {
    private String rewardName;
    private String userId;
    private boolean pendingStatus;

    public RewardRequest() {
        // Default constructor required for Firestore
    }

    public RewardRequest(String rewardName, String userId, boolean pendingStatus) {
        this.rewardName = rewardName;
        this.userId = userId;
        this.pendingStatus = pendingStatus;
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
}
