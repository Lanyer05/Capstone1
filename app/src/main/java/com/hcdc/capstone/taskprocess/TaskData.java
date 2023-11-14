package com.hcdc.capstone.taskprocess;

import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class TaskData {

    String taskName, description, location, camera;
    private boolean isAccepted;
    private ArrayList<String> acceptedByUsers;
    int hours, minutes, maxUsers;
    private Timestamp expirationDateTime;
    private long points;

    // Add public no-argument constructor
    public TaskData() {
        // Default constructor required by Firestore
    }

    public TaskData(String taskName, String description, long points, String location, String camera, boolean isAccepted, int hours, int minutes, int maxUsers, ArrayList<String> acceptedByUsers, Timestamp expirationDateTime) {
        this.taskName = taskName;
        this.description = description;
        this.points = points;
        this.location = location;
        this.isAccepted = isAccepted;
        this.hours = hours ;
        this.minutes = minutes;
        this.maxUsers = maxUsers;
        this.camera = camera;
        this.expirationDateTime = expirationDateTime;

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

    public long getPoints() {
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

    public Timestamp getExpirationDateTime() {
        return expirationDateTime;
    }

    public String getFormattedExpirationDateTime() {
        // Format expirationDateTime as a human-readable string with AM/PM indicator
        if (expirationDateTime != null) {
            Date date = expirationDateTime.toDate();
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a | MM/dd/yy", Locale.getDefault());
            return sdf.format(date);
        } else {
            return "N/A";
        }
    }
}