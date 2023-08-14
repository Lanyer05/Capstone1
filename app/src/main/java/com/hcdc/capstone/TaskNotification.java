package com.hcdc.capstone;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class TaskNotification {

    private static final String CHANNEL_ID = "task_channel";
    private Context context;
    private FirebaseFirestore firestore;
    private CollectionReference tasksCollection;

    public TaskNotification(Context context) {
        this.context = context;
        firestore = FirebaseFirestore.getInstance();
        tasksCollection = firestore.collection("tasks");
        createNotificationChannel();
    }

    public void startListeningForNewTasks() {
        tasksCollection.addSnapshotListener((value, error) -> {
            if (error != null) {
                // Handle error
                return;
            }

            for (QueryDocumentSnapshot doc : value) {
                // Check if the document is new or already seen by the user
                // You might want to store the last seen document ID or timestamp locally

                // If it's a new document, show a notification
                sendNotification("New Task Added", "new task is available!");
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void sendNotification(String title, String message) {
        // Create an explicit intent for the Task activity
        Intent taskIntent = new Intent(context, Task.class);
        taskIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, taskIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)  // Set the PendingIntent for the notification
                .setAutoCancel(true);  // Automatically dismiss the notification when clicked

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(0, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Task Channel";
            String description = "Channel for new task notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
