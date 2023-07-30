package com.hcdc.capstone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Rewards extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rewards);
        // Your code for setting up the home page, if any
        // For example, you can add widgets, set up views, etc.

        bottomNavigationView = findViewById(R.id.bottom_navigation_view);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_home:
                        Intent iii = new Intent(getApplicationContext(), Homepage.class);
                        startActivity(iii);
                        return true;

                    case R.id.action_task:
                        // Handle "Task" item click if needed
                        // For example, navigate to TaskActivity
                        Intent i = new Intent(getApplicationContext(), Task.class);
                        startActivity(i);
                        return true;

                    case R.id.action_reward:
                        // Handle "Reward" item click if needed
                        // For example, navigate to RewardActivity


                    case R.id.action_transaction:
                        // Handle "Transaction"
                        Intent ii = new Intent(getApplicationContext(), Transaction.class);
                        startActivity(ii);
                        return true;
                }
                return false;
            }
        });
    }
}