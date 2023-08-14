package com.hcdc.capstone;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import java.util.concurrent.TimeUnit;

public class Task extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private TaskNotification taskNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        bottomNavigationView = findViewById(R.id.bottom_navigation_view);

        taskNotification = new TaskNotification(this);

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                TaskWorker.class, 1, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(this).enqueue(workRequest);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_home:
                        Intent iii = new Intent(getApplicationContext(), Homepage.class);
                        startActivity(iii);
                        return true;

                    case R.id.action_task:
                        // No need to start listening again, it's handled by the worker
                        return true;

                    case R.id.action_reward:
                        Intent ii = new Intent(getApplicationContext(), Rewards.class);
                        startActivity(ii);
                        return true;

                    case R.id.action_transaction:
                        Intent i = new Intent(getApplicationContext(), Transaction.class);
                        startActivity(i);
                        return true;
                }
                return false;
            }
        });

        Intent serviceIntent = new Intent(this, TaskForegroundService.class);
        startService(serviceIntent);
    }
}
