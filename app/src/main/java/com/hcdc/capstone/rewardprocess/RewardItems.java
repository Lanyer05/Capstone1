package com.hcdc.capstone.rewardprocess;

public class RewardItems {

    private String category;
    private String points;
    private String quantity;
    private String rewardName;

    private int selectedquantity;


    public RewardItems()
    {

    }

    public RewardItems(String category, String points, String quantity, String rewardName, String selectedquantity) {
        this.category = category;
        this.points = points;
        this.quantity = quantity;
        this.rewardName = rewardName;
        this.selectedquantity = Integer.parseInt(selectedquantity);
    }

    public String getCategory() {
        return category;
    }

    public String getPoints() {
        return points;
    }

    public void setPoints(String points) {
        this.points = points;
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


    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public void setRewardName(String rewardName) {
        this.rewardName = rewardName;
    }

    public int getSelectedquantity() {
        return selectedquantity;
    }

    public void setSelectedquantity(int selectedquantity) {
        this.selectedquantity = selectedquantity;
    }
}
