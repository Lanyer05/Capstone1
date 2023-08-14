package com.hcdc.capstone;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class TaskWorker extends Worker {

    private TaskNotification taskNotification;

    public TaskWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        taskNotification = new TaskNotification(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Listen for new tasks and send notifications
        taskNotification.startListeningForNewTasks();

        return Result.success();
    }
}
