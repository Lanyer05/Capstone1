package com.hcdc.capstone.rewardprocess;

public class RewardsData {

    String points, category; // Change rewardName to category

    public RewardsData() {
        // Default constructor required by Firestore
    }

    public RewardsData(String points, String category) { // Change rewardName to category
        this.points = points;
        this.category = category; // Change rewardName to category
    }

    public String getPoints() {
        return points;
    }

    public String getCategory() { // Change getRewardName to getCategory
        return category; // Change rewardName to category
    }
}

