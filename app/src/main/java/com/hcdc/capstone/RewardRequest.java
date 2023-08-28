package com.hcdc.capstone;

public class RewardRequest {
    private String rewardName;
    private String userId;

    public RewardRequest() {
        // Default constructor required for Firestore
    }

    public RewardRequest(String rewardName, String userId) {
        this.rewardName = rewardName;
        this.userId = userId;
    }

    public String getRewardName() {
        return rewardName;
    }

    public String getUserId() {
        return userId;
    }
}
