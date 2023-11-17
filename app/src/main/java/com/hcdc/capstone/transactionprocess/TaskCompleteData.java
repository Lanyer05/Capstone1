package com.hcdc.capstone.transactionprocess;

public class TaskCompleteData {
    String taskName, location;
    int points;
    Boolean isConfirmed;

    public TaskCompleteData() {
        // For Firebase
    }

    public TaskCompleteData(String taskName, String location, int points, Boolean isConfirmed) {
        this.taskName = taskName;
        this.location = location;
        this.points = points;
        this.isConfirmed = isConfirmed;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getLocation() {
        return location;
    }

    public int getPoints() {
        return points;
    }

    public Boolean getConfirmed() {
        return isConfirmed;
    }
}
