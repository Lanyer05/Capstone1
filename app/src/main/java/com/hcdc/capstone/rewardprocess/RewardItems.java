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

    public void setCategory(String category) {
        this.category = category;
    }

    public void setPoints(String points) {
        this.points = points;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public void setRewardName(String rewardName) {
        this.rewardName = rewardName;
    }
}
