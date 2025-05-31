package com.example.currents.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.currents.R; // Make sure R.java is correctly generated for your project
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputEditText emailEditText;
    private MaterialButton requestOtpButton;
    private TextView backToLoginButton; // Changed from LinearLayout to TextView as per XML

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Initialize views
        emailEditText = findViewById(R.id.email_edit_text);
        requestOtpButton = findViewById(R.id.request_otp_button);
        backToLoginButton = findViewById(R.id.back_to_login_button); // Initialize the TextView

        // Set OnClickListener for Request OTP button
        requestOtpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();

                if (email.isEmpty()) {
                    Toast.makeText(ForgotPasswordActivity.this, "Please enter your email address", Toast.LENGTH_SHORT).show();
                } else {
                    // Navigate to VerifyOtpActivity and pass the email
                    Intent intent = new Intent(ForgotPasswordActivity.this, VerifyOtpActivity.class);
                    intent.putExtra("email", email); // Pass the email as an extra
                    startActivity(intent);
                }
            }
        });

        // Set OnClickListener for Back to Login button
        backToLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to LoginActivity
                Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                startActivity(intent);
                finish(); // Optional: finish ForgotPasswordActivity so user can't go back with back button
            }
        });
    }
}