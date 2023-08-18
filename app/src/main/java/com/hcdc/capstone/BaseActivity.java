package com.hcdc.capstone;

import android.app.PendingIntent;
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
        boolean isActivityInStack = false;
        Intent intent = new Intent(this, getClass());

        isActivityInStack = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_NO_CREATE) != null;

        if (isActivityInStack) {
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        } else {
            super.onBackPressed();
        }
    }


    private boolean isLoggedIn() {
        return true;
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
