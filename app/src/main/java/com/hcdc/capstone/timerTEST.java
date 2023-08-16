package com.hcdc.capstone;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class timerTEST extends AppCompatActivity {

    private Button startButton;
    private TextView timerTextView;
    private boolean timerRunning = false;
    private long startTime = 0L;
    private Handler handler = new Handler();
    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;
            int hours = minutes / 60;
            minutes = minutes % 60;

            timerTextView.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));

            if (timerRunning) {
                handler.postDelayed(this, 1000);
            }
        }
    };

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startButton = findViewById(R.id.startButton);
        timerTextView = findViewById(R.id.timerTextView);

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
    }

    @Override
    protected void onStop() {
        super.onStop();
        timerRunning = false;
        handler.removeCallbacks(timerRunnable);
    }
}
