package com.hcdc.capstone;

public class Tasks {

    String taskName, description, points, location;

    // Add public no-argument constructor
    public Tasks() {
        // Default constructor required by Firestore
    }

    public Tasks(String taskName, String description, String points, String location) {
        this.taskName = taskName;
        this.description = description;
        this.points = points;
        this.location = location;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getDescription() {
        return description;
    }

    public String getPoints() {
        return points;
    }

    public String getLocation() {
        return location;
    }
}
