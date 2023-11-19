package com.hcdc.capstone;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hcdc.capstone.accounthandling.LoginActivity;
import com.hcdc.capstone.taskprocess.TaskProgress;

public class BaseActivity extends AppCompatActivity {

    private boolean shouldRedirectToLogin = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (shouldRedirectToLogin) {
            redirectToLogin();
        } else if (!isLoggedIn()) {
            shouldRedirectToLogin = true;
            redirectToLogin();
        }
    }

    @Override
    public void onBackPressed() {
        if (!handleBackPressed()) {
            super.onBackPressed();
        }
    }

    protected boolean handleBackPressed() {
        if (getClass() == Homepage.class) {
            return false;
        }

        boolean isExcludedActivity = getClass() == TaskProgress.class;
        Intent intent = new Intent(this, Homepage.class);

        if (!isExcludedActivity && PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE) != null) {
            // If Homepage activity is already in the stack, bring it to the front
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            finish();
            return true;
        }

        return false;
    }

    private boolean isLoggedIn() {
        // Replace this with your actual login check logic
        return true;
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}