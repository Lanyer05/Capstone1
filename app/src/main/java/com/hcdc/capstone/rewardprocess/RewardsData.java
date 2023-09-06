package com.hcdc.capstone.rewardprocess;

public class RewardsData {

    String points, rewardName;

    public RewardsData() {
        // Default constructor required by Firestore
    }

    public RewardsData(String points, String rewardName) {
        this.points = points;
        this.rewardName = rewardName;
    }

    public String getPoints() {
        return points;
    }

    public String getRewardName() {
        return rewardName;
    }
}
