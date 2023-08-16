package com.hcdc.capstone;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Transaction extends AppCompatActivity {

    Button featureTest1;

    private BottomNavigationView bottomNavigationView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);
        // Your code for setting up the home page, if any
        // For example, you can add widgets, set up views, etc.

        bottomNavigationView = findViewById(R.id.bottom_navigation_view);

        //BUtton
        featureTest1 = findViewById(R.id.timertesting);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_home:
                        Intent iii = new Intent(getApplicationContext(), Homepage.class);
                        startActivity(iii);
                        return true;

                    case R.id.action_task:
                        // Handle "Task"
                        Intent i = new Intent(getApplicationContext(), Task.class);
                        startActivity(i);
                        return true;

                    case R.id.action_reward:
                        // Handle "Reward" item click if needed
                        // For example, navigate to RewardActivity
                        Intent ii = new Intent(getApplicationContext(), Reward.class);
                        startActivity(ii);
                        return true;

                    case R.id.action_transaction:
                        // Handle "Transaction"

                }
                return false;
            }
        });

        featureTest1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), timerTEST.class);
                startActivity(i);
                finish();
            }
        });
    }
}