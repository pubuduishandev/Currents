package com.example.currents.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.currents.R;

public class SplashActivity extends AppCompatActivity{
    // Duration of the splash screen in milliseconds
    private static final long SPLASH_SCREEN_DURATION = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Use a Handler to delay the execution
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Finish the SplashActivity after the duration
            // Since no next activity is started, the app will close or
            // return to the previous app if launched from recent tasks.
            finish();
        }, SPLASH_SCREEN_DURATION);
    }
}
