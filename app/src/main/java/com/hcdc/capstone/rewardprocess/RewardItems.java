package com.hcdc.capstone.rewardprocess;

public class RewardItems {

    private String category;
    private String points;
    private String quantity;
    private String rewardName;


    public RewardItems()
    {

    }

    public RewardItems(String category, String points, String quantity, String rewardName) {
        this.category = category;
        this.points = points;
        this.quantity = quantity;
        this.rewardName = rewardName;
    }

    public String getCategory() {
        return category;
    }

    public String getrewardPoints() {
        return points;
    }

    public String getQuantity() {
        return quantity;
    }

    public String getRewardName() {
        return rewardName;
    }
}
