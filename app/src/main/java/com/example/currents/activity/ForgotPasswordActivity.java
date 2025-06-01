package com.example.currents.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns; // For email pattern validation
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.currents.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout; // Import TextInputLayout

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputEditText emailEditText;
    private MaterialButton requestOtpButton;
    private TextView backToLoginButton;

    // Declare TextInputLayout reference
    private TextInputLayout emailInputLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Initialize views
        emailEditText = findViewById(R.id.email_edit_text);
        requestOtpButton = findViewById(R.id.request_otp_button);
        backToLoginButton = findViewById(R.id.back_to_login_button);

        // Initialize TextInputLayout reference
        emailInputLayout = findViewById(R.id.email_input_layout); // Matches XML ID

        // Add TextWatcher to clear error when text changes
        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Clear error when text changes
                if (emailInputLayout.isErrorEnabled()) {
                    emailInputLayout.setError(null);
                    emailInputLayout.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Set OnClickListener for Request OTP button
        requestOtpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();

                // Clear any previous error before validation
                emailInputLayout.setError(null);
                emailInputLayout.setErrorEnabled(false);

                boolean isValid = true;

                // Validate Email
                if (email.isEmpty()) {
                    emailInputLayout.setError("Email cannot be empty");
                    emailInputLayout.setErrorEnabled(true);
                    isValid = false;
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailInputLayout.setError("Enter a valid email address");
                    emailInputLayout.setErrorEnabled(true);
                    isValid = false;
                }

                if (isValid) {
                    Toast.makeText(ForgotPasswordActivity.this, "OTP requested for: " + email, Toast.LENGTH_SHORT).show();
                    // Navigate to VerifyOtpActivity and pass the email
                    Intent intent = new Intent(ForgotPasswordActivity.this, VerifyOtpActivity.class);
                    intent.putExtra("email", email); // Pass the email as an extra
                    startActivity(intent);
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
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