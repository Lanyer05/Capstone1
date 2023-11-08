package com.hcdc.capstone.rewardprocess;

public class RewardsData {

    String points, category;

    public RewardsData() {
        // Default constructor required by Firestore
    }

    public RewardsData(String points, String category) {
        this.points = points;
        this.category = category;
    }

    public String getPoints() {
        return points;
    }

    public String getCategory() {
        return category;
    }
}

