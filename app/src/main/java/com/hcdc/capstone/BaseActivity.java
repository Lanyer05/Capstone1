package com.hcdc.capstone;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
        // Override onBackPressed to ensure it leads to Homepage.java
        Intent intent = new Intent(this, Homepage.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private boolean isLoggedIn() {
        return true; // Replace with your login check logic
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}

