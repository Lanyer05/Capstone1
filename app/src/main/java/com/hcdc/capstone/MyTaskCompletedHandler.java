package com.hcdc.capstone;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

public class MyTaskCompletedHandler {
    public void notifyReactApp(String taskId) {
        // Create a message with data
        RemoteMessage message = new RemoteMessage.Builder("680499772205" + "@gcm.googleapis.com")
                .setMessageId(Integer.toString(1))
                .addData("title", "Task Completed")
                .addData("body", "Task with ID " + taskId + " has been completed.")
                .build();

        FirebaseMessaging.getInstance().send(message);
    }
}