package com.example.currents.activity;

import android.content.Intent;
import android.os.Bundle;
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

public class ResetPasswordActivity extends AppCompatActivity {

    private ImageButton backButton;
    private TextInputEditText newPasswordEditText;
    private TextInputLayout newPasswordInputLayout;
    private TextInputEditText confirmPasswordEditText;
    private TextInputLayout confirmPasswordInputLayout;
    private MaterialButton resetPasswordButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        // Initialize views
        backButton = findViewById(R.id.back_button);
        newPasswordEditText = findViewById(R.id.new_password_edit_text);
        newPasswordInputLayout = findViewById(R.id.new_password_input_layout);
        confirmPasswordEditText = findViewById(R.id.confirm_password_edit_text);
        confirmPasswordInputLayout = findViewById(R.id.confirm_password_input_layout);
        resetPasswordButton = findViewById(R.id.reset_password_button);

        // Set OnClickListener for the Back Button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to ForgotPasswordActivity (or VerifyOtpActivity if you want to keep the flow)
                // For a typical flow, after OTP, if you go back here, you might want to return to ForgotPasswordActivity
                Intent intent = new Intent(ResetPasswordActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
                finish(); // Finish current activity
            }
        });

        // Add TextWatchers to clear errors when user starts typing
        newPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (newPasswordInputLayout.isErrorEnabled()) {
                    newPasswordInputLayout.setError(null);
                    newPasswordInputLayout.setErrorEnabled(false);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        confirmPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (confirmPasswordInputLayout.isErrorEnabled()) {
                    confirmPasswordInputLayout.setError(null);
                    confirmPasswordInputLayout.setErrorEnabled(false);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });


        // Set OnClickListener for Reset Password button
        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newPassword = newPasswordEditText.getText().toString().trim();
                String confirmPassword = confirmPasswordEditText.getText().toString().trim();

                boolean isValid = true;

                // Password validation
                if (newPassword.isEmpty()) {
                    newPasswordInputLayout.setError("New password cannot be empty");
                    newPasswordInputLayout.setErrorEnabled(true);
                    isValid = false;
                } else if (newPassword.length() < 6) { // Example: minimum 6 characters
                    newPasswordInputLayout.setError("Password must be at least 6 characters");
                    newPasswordInputLayout.setErrorEnabled(true);
                    isValid = false;
                } else {
                    newPasswordInputLayout.setError(null);
                    newPasswordInputLayout.setErrorEnabled(false);
                }

                // Confirm Password validation
                if (confirmPassword.isEmpty()) {
                    confirmPasswordInputLayout.setError("Confirm password cannot be empty");
                    confirmPasswordInputLayout.setErrorEnabled(true);
                    isValid = false;
                } else if (!newPassword.equals(confirmPassword)) {
                    confirmPasswordInputLayout.setError("Passwords do not match");
                    confirmPasswordInputLayout.setErrorEnabled(true);
                    isValid = false;
                } else {
                    confirmPasswordInputLayout.setError(null);
                    confirmPasswordInputLayout.setErrorEnabled(false);
                }


                if (isValid) {
                    // All validations pass
                    // In a real application, you would send the new password to your backend
                    // along with any user identification (e.g., email or verification token)
                    // retrieved from the previous screens.
                    Toast.makeText(ResetPasswordActivity.this, "Password reset successfully!", Toast.LENGTH_SHORT).show();

                    // Navigate to HomeActivity (or LoginActivity) after successful password reset
                    Intent intent = new Intent(ResetPasswordActivity.this, HomeActivity.class); // Assuming HomeActivity is your main screen
                    // Clear the back stack so user cannot go back to reset/forgot password flow
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish(); // Finish current activity
                } else {
                    Toast.makeText(ResetPasswordActivity.this, "Please fix the errors to reset password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}