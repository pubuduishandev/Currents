package com.example.currents.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.currents.R;
import com.example.currents.auth.LoginActivity;

public class SplashActivity extends AppCompatActivity{
    // Duration of the splash screen in milliseconds
    private static final long SPLASH_SCREEN_DURATION = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Create an Intent to start the LoginActivity
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);

            // Finish the SplashActivity so the user cannot go back to it
            finish();
        }, SPLASH_SCREEN_DURATION);
    }
}
