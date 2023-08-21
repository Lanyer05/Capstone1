package com.hcdc.capstone;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentReference;

public class timerTEST extends AppCompatActivity {

    private Button startButton;
    private Button doneButton;
    private TextView timerTextView;
    private boolean timerRunning = false;
    private long startTime = 0L;
    private long taskDurationMillis; // Task duration in milliseconds
    private Handler handler = new Handler();
    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            long remainingMillis = taskDurationMillis - millis;
            if (remainingMillis <= 0) {
                // Time's up, mark the task as done or handle it as needed
                timerTextView.setText("Time's up!");
                timerRunning = false;
                doneButton.setVisibility(View.VISIBLE);
            } else {
                int seconds = (int) (remainingMillis / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;
                int hours = minutes / 60;
                minutes = minutes % 60;

                timerTextView.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));

                if (timerRunning) {
                    handler.postDelayed(this, 1000);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer_test);

        startButton = findViewById(R.id.startButton);
        doneButton = findViewById(R.id.doneButton);
        timerTextView = findViewById(R.id.timerTextView);
        TextView taskNameTextView = findViewById(R.id.taskTitle4);
        TextView taskPointsTextView = findViewById(R.id.taskPoint);
        TextView taskDescriptionTextView = findViewById(R.id.taskDesc);
        TextView taskLocationTextView = findViewById(R.id.taskLocation);

        String taskName = getIntent().getStringExtra("taskName");
        String taskPoints = getIntent().getStringExtra("taskPoints");
        String taskDescription = getIntent().getStringExtra("taskDescription");
        String taskLocation = getIntent().getStringExtra("taskLocation");

        taskNameTextView.setText(taskName);
        taskPointsTextView.setText(taskPoints);
        taskDescriptionTextView.setText(taskDescription);
        taskLocationTextView.setText(taskLocation);



        // Retrieve task duration in milliseconds from the intent
        taskDurationMillis = getIntent().getLongExtra("taskDurationMillis", 0);

        // Retrieve timeFrame data from the intent or wherever it's stored
        int timeFrameHours = getIntent().getIntExtra("timeFrameHours", 0);
        int timeFrameMinutes = getIntent().getIntExtra("timeFrameMinutes", 0);

        // Convert time frame data into milliseconds
        long timeFrameMillis = (timeFrameHours * 60 + timeFrameMinutes) * 60 * 1000;

        // Set the initial time on the timerTextView
        int initialHours = timeFrameHours;
        int initialMinutes = timeFrameMinutes;
        int initialSeconds = (int) ((timeFrameMillis % (60 * 1000)) / 1000);
        timerTextView.setText(String.format("%02d:%02d:%02d", initialHours, initialMinutes, initialSeconds));

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!timerRunning) {
                    timerRunning = true;
                    startTime = System.currentTimeMillis();
                    handler.postDelayed(timerRunnable, 0);
                }
            }
        });

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Mark the task as done in the database or handle it as needed
                // ...
                finish(); // Finish the activity and return to the previous one
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        timerRunning = false;
        handler.removeCallbacks(timerRunnable);
    }
}