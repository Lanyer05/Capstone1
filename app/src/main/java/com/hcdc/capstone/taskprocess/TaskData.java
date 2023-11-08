package com.hcdc.capstone.taskprocess;

import java.util.ArrayList;

public class TaskData {

    String taskName, description, points, location, camera;
    private boolean isAccepted;
    private ArrayList<String> acceptedByUsers;
    int hours, minutes, maxUsers;

    // Add public no-argument constructor
    public TaskData() {
        // Default constructor required by Firestore
    }

    public TaskData(String taskName, String description, String points, String location, String camera, boolean isAccepted, int hours, int minutes, int maxUsers, ArrayList<String> acceptedByUsers) {
        this.taskName = taskName;
        this.description = description;
        this.points = points;
        this.location = location;
        this.isAccepted = isAccepted;
        this.hours = hours ;
        this.minutes = minutes;
        this.maxUsers = maxUsers;
        this.camera = camera;

        if (acceptedByUsers != null) {
            this.acceptedByUsers = acceptedByUsers;
        } else {
            this.acceptedByUsers = new ArrayList<>();
        }
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

    public boolean isAccepted() {
        return isAccepted;
    }

    public int getHours() {
        return hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public int getMaxUsers(){
        return maxUsers;
    }

    public String getcamera(){
        return camera;
    }

    public ArrayList<String> getAcceptedByUsers() {
        return acceptedByUsers;
    }
}