package com.example.currents.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageButton; // Import ImageButton
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.currents.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale; // For formatting time

public class VerifyOtpActivity extends AppCompatActivity {

    private TextInputEditText otpEditText;
    private TextInputLayout otpInputLayout;
    private MaterialButton verifyOtpButton;
    private TextView pageInstructionText;
    private TextView resendOtpButton;
    private ImageButton backButton; // Declare ImageButton

    // Countdown Timer variables
    private TextView countdownTimerText;
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis = 60000; // 60 seconds (1 minute)
    private boolean timerRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);

        // Initialize views
        otpEditText = findViewById(R.id.otp_edit_text);
        otpInputLayout = findViewById(R.id.otp_input_layout);
        verifyOtpButton = findViewById(R.id.verify_otp_button);
        pageInstructionText = findViewById(R.id.page_instruction_text);
        resendOtpButton = findViewById(R.id.resend_otp_button);
        backButton = findViewById(R.id.back_button); // Initialize back button
        countdownTimerText = findViewById(R.id.countdown_timer_text); // Initialize countdown timer TextView

        // Retrieve the email passed from ForgotPasswordActivity
        String email = getIntent().getStringExtra("email");
        if (email != null && !email.isEmpty()) {
            String maskedEmail = maskEmail(email);
            pageInstructionText.setText("Please enter the 6-digits code we sent to your email " + maskedEmail);
        } else {
            pageInstructionText.setText("Please enter the 6-digits code sent to your email.");
            Toast.makeText(this, "Error: Email not received.", Toast.LENGTH_LONG).show();
            // Consider redirecting if email is crucial and not received
            finish();
        }

        // Add TextWatcher to clear error when user starts typing
        otpEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (otpInputLayout.isErrorEnabled()) {
                    otpInputLayout.setError(null);
                    otpInputLayout.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Set OnClickListener for Verify OTP button
        verifyOtpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String otp = otpEditText.getText().toString().trim();

                if (otp.isEmpty()) {
                    otpInputLayout.setError("OTP cannot be empty");
                    otpInputLayout.setErrorEnabled(true);
                    Toast.makeText(VerifyOtpActivity.this, "Please enter the OTP", Toast.LENGTH_SHORT).show();
                } else if (otp.length() < 6) {
                    otpInputLayout.setError("OTP must be 6 digits");
                    otpInputLayout.setErrorEnabled(true);
                    Toast.makeText(VerifyOtpActivity.this, "OTP must be 6 digits", Toast.LENGTH_SHORT).show();
                } else {
                    // OTP is entered and meets length requirement
                    // In a real application, you would send this OTP to your backend for verification.
                    // For now, we'll simulate success and navigate to ResetPasswordActivity.

                    Toast.makeText(VerifyOtpActivity.this, "OTP verified", Toast.LENGTH_SHORT).show();

                    // Stop the timer when OTP is successfully entered/verified
                    if (countDownTimer != null) {
                        countDownTimer.cancel();
                    }

                    // Navigate to ResetPasswordActivity
                    Intent intent = new Intent(VerifyOtpActivity.this, ResetPasswordActivity.class);
                    // You might pass the email or a verification token
                    intent.putExtra("email", email); // Pass email if needed for reset
                    startActivity(intent);
                    finish();
                }
            }
        });

        // Set OnClickListener for Resend OTP button
        resendOtpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!timerRunning) { // Only allow resend if timer is not running
                    // Implement your resend OTP logic here
                    Toast.makeText(VerifyOtpActivity.this, "Resending OTP to " + email, Toast.LENGTH_SHORT).show();
                    timeLeftInMillis = 60000; // Reset timer for 60 seconds
                    startTimer(); // Start the timer again
                    resendOtpButton.setEnabled(false); // Disable resend button
                    resendOtpButton.setTextColor(getResources().getColor(R.color.text_secondary)); // Optional: gray out text
                } else {
                    Toast.makeText(VerifyOtpActivity.this, "Please wait before resending OTP", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set OnClickListener for the Back Button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // This will go back to the previous activity (ForgotPasswordActivity)
            }
        });

        // Start the countdown timer when the activity is created
        startTimer();
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountdownText();
            }

            @Override
            public void onFinish() {
                timerRunning = false;
                countdownTimerText.setText("00:00");
                resendOtpButton.setEnabled(true); // Enable resend button
                resendOtpButton.setTextColor(getResources().getColor(R.color.primary_color)); // Optional: bring back original color
                Toast.makeText(VerifyOtpActivity.this, "OTP expired. Please resend.", Toast.LENGTH_LONG).show();
            }
        }.start();

        timerRunning = true;
        resendOtpButton.setEnabled(false); // Disable resend when timer starts
        resendOtpButton.setTextColor(getResources().getColor(R.color.text_secondary)); // Optional: gray out text
    }

    private void updateCountdownText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;

        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        countdownTimerText.setText(timeFormatted);
    }

    /**
     * Helper method to mask the email for display.
     */
    private String maskEmail(String email) {
        if (email == null || email.isEmpty()) {
            return "";
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) { // No '@' or too short before '@'
            return email;
        }

        // To match the image "202*******ac.lk", we'll just show the first 3 chars
        // and then mask the rest before the @ symbol.
        String localPart = email.substring(0, atIndex);
        String domainPart = email.substring(atIndex);

        if (localPart.length() > 3) {
            return localPart.substring(0, 3) + "*******" + domainPart;
        } else {
            return localPart + "*******" + domainPart; // Fallback if local part is too short
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel the timer to prevent memory leaks if the activity is destroyed
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}