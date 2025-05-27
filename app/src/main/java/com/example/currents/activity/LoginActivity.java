// File: app/src/main/java/com/example/currents/auth/LoginActivity.java
package com.example.currents.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.currents.R; // Make sure R.java is correctly generated for your project
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private MaterialButton loginButton;
    private TextView forgotPasswordText;
    private LinearLayout signupTextContainer; // Using LinearLayout as the touch target for signup

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize UI elements
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        loginButton = findViewById(R.id.login_button);
        forgotPasswordText = findViewById(R.id.forgot_password_text);
        signupTextContainer = findViewById(R.id.signup_text_container);

        // Set OnClickListener for the Login Button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please enter both email and password.", Toast.LENGTH_SHORT).show();
                } else {
                    // Placeholder for actual login logic
                    // In a real app, you would integrate with an authentication system (e.g., Firebase, your own backend)
                    if (email.equals("test@example.com") && password.equals("password")) {
                        Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                        // Navigate to the main activity (e.g., MainActivity)
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish(); // Finish LoginActivity so user can't go back
                    } else {
                        Toast.makeText(LoginActivity.this, "Invalid credentials.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // Set OnClickListener for Forgot Password
        forgotPasswordText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Placeholder for Forgot Password navigation
                Toast.makeText(LoginActivity.this, "Forgot Password Clicked", Toast.LENGTH_SHORT).show();
                // Example: Intent to ForgotPasswordActivity
                // Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                // startActivity(intent);
            }
        });

        // Set OnClickListener for Sign Up (using the LinearLayout as the clickable area)
        signupTextContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirect to SignupActivity
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });
    }
}