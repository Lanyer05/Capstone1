package com.hcdc.capstone.rewardprocess;

public class Rewards {

    String points, rewardName;

    public Rewards() {
        // Default constructor required by Firestore
    }

    public Rewards(String points, String rewardName) {
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
