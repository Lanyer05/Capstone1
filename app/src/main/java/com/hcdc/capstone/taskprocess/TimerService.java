package com.hcdc.capstone.taskprocess;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.hcdc.capstone.R;

public class TimerService extends Service {
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "TimerServiceChannel";
    private Handler handler;
    private Runnable timerRunnable;

    private long startTime; // Add this variable
    private long taskDurationMillis; // Add this variable

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();

        // Initialize startTime and taskDurationMillis as needed
        startTime = System.currentTimeMillis(); // Set the start time
        taskDurationMillis = 60 * 60 * 1000; // Example: 1 hour (adjust as needed)

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                // Timer logic here
                long elapsedTime = System.currentTimeMillis() - startTime;
                long remainingTimeMillis = taskDurationMillis - elapsedTime;

                if (remainingTimeMillis <= 0) {
                    // Timer expired, stop the service or update the notification accordingly
                    stopSelf();
                } else {
                    // Calculate remaining time in HH:MM:SS format
                    int seconds = (int) (remainingTimeMillis / 1000);
                    int minutes = seconds / 60;
                    seconds = seconds % 60;
                    int hours = minutes / 60;
                    minutes = minutes % 60;
                    String timerValue = String.format("%02d:%02d:%02d", hours, minutes, seconds);

                    // Update the notification with the timer value
                    updateNotification(timerValue);

                    // Restart this runnable to keep the timer running
                    handler.postDelayed(this, 1000);
                }
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID, createNotification());
        handler.post(timerRunnable);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(timerRunnable);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, TaskProgress.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Task Timer")
                .setContentText("Timer is running...")
                .setSmallIcon(R.drawable.ic_timer)
                .setContentIntent(pendingIntent)
                .build();
    }

    private void updateNotification(String timerValue) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Task Timer")
                .setContentText("Time Remaining: " + timerValue)
                .setSmallIcon(R.drawable.ic_timer);

        Intent notificationIntent = new Intent(this, TaskProgress.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        builder.setContentIntent(pendingIntent);

        Notification notification = builder.build();
        startForeground(NOTIFICATION_ID, notification);
    }
}
