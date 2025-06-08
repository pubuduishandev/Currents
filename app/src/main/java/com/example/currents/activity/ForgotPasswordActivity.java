package com.example.currents.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.currents.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class ForgotPasswordActivity extends AppCompatActivity {
    // UI components
    private TextInputEditText emailEditText;
    private MaterialButton requestLinkButton;
    private TextView backToLoginButton;
    private TextInputLayout emailInputLayout;

    // Firebase instances
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Call the superclass method
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        emailEditText = findViewById(R.id.email_edit_text);
        requestLinkButton = findViewById(R.id.request_otp_button);
        backToLoginButton = findViewById(R.id.back_to_login_button);
        emailInputLayout = findViewById(R.id.email_input_layout);

        // Add TextWatcher to clear error when text changes
        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (emailInputLayout.isErrorEnabled()) {
                    emailInputLayout.setError(null);
                    emailInputLayout.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Set OnClickListener for Request Reset Link button
        requestLinkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();

                // Clear any previous error before validation
                emailInputLayout.setError(null);
                emailInputLayout.setErrorEnabled(false);

                if (validateEmail(email)) {
                    checkEmailExistsInFirestore(email);
                }
            }
        });

        // Set OnClickListener for Back to Login button
        backToLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    // Validate email input
    private boolean validateEmail(String email) {
        if (email.isEmpty()) {
            emailInputLayout.setError("Email cannot be empty");
            emailInputLayout.setErrorEnabled(true);
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout.setError("Enter a valid email address");
            emailInputLayout.setErrorEnabled(true);
            return false;
        }
        return true;
    }

    // Check if the email exists in firestore
    private void checkEmailExistsInFirestore(String email) {
        // Show loading state
        requestLinkButton.setEnabled(false);
        requestLinkButton.setText(R.string.checking);

        // Query firestore to check if email exists in users collection
        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        if (task.getResult() != null && !task.getResult().isEmpty()) {
                            // Email exists in firestore, send reset link
                            sendPasswordResetEmail(email);
                        } else {
                            // Email not found in firestore
                            emailInputLayout.setError("This email is not registered");
                            emailInputLayout.setErrorEnabled(true);
                            Toast.makeText(ForgotPasswordActivity.this,
                                    R.string.no_account_found,
                                    Toast.LENGTH_SHORT).show();
                            resetButtonState();
                        }
                    } else {
                        // firestore query failed
                        Toast.makeText(ForgotPasswordActivity.this,
                                R.string.forgot_password_error + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                        resetButtonState();
                    }
                }
            });
    }

    // Send password reset email using Firebase Auth
    private void sendPasswordResetEmail(String email) {
        mAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(ForgotPasswordActivity.this,
                                 R.string.email_sent_to + email,
                                Toast.LENGTH_SHORT).show();
                        // Optionally navigate back to login
                        Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(ForgotPasswordActivity.this,
                                R.string.email_sent_failed + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                        resetButtonState();
                    }
                }
            });
    }

    // Reset the button state after an operation
    private void resetButtonState() {
        requestLinkButton.setEnabled(true);
        requestLinkButton.setText(R.string.request_link);
    }
}