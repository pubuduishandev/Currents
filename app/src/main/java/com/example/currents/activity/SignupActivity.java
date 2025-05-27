package com.example.currents.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.currents.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

// Assuming you have a LoginActivity and a placeholder for MainActivity
// If you don't have LoginActivity or MainActivity yet, you'll need to create them.
// For example, an empty Activity named LoginActivity.java and MainActivity.java

public class SignupActivity extends AppCompatActivity {

    // UI elements
    private TextInputEditText firstNameEditText;
    private TextInputEditText lastNameEditText;
    private TextInputEditText userNameEditText;
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private TextInputEditText confirmPasswordEditText;
    private MaterialButton signupButton;
    private LinearLayout signinTextContainer; // For "Already have an account? Sign In" link

    // Developer info TextViews (Optional, if you want to reference them in Java)
    // private TextView developerInfoConcept;
    // private TextView developerInfoDept;
    // private TextView developerInfoFaculty;
    // private TextView appVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize UI elements
        firstNameEditText = findViewById(R.id.first_name_edit_text);
        lastNameEditText = findViewById(R.id.last_name_edit_text);
        userNameEditText = findViewById(R.id.student_id_edit_text);
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        confirmPasswordEditText = findViewById(R.id.confirm_password_edit_text);
        signupButton = findViewById(R.id.signup_button);
        signinTextContainer = findViewById(R.id.signin_text_container);

        // If you need to access developer info TextViews in Java, uncomment these:
        // developerInfoConcept = findViewById(R.id.developer_info_concept);
        // developerInfoDept = findViewById(R.id.developer_info_dept);
        // developerInfoFaculty = findViewById(R.id.developer_info_faculty);
        // appVersion = findViewById(R.id.app_version);


        // Set OnClickListener for the Sign Up Button
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSignup();
            }
        });

        // Set OnClickListener for "Already have an account? Sign In" link
        signinTextContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToLogin();
            }
        });
    }

    /**
     * Attempts to sign up the account specified by the signup form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual signup attempt is made.
     */
    private void attemptSignup() {
        // Reset errors
        firstNameEditText.setError(null);
        lastNameEditText.setError(null);
        userNameEditText.setError(null);
        emailEditText.setError(null);
        passwordEditText.setError(null);
        confirmPasswordEditText.setError(null);

        // Store values at the time of the signup attempt.
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String userName = userNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        boolean cancel = false;
        View focusView = null; // View to focus if an error occurs

        // 1. Check if all fields are filled & other validations
        // Check for First Name
        if (TextUtils.isEmpty(firstName)) {
            firstNameEditText.setError("First Name is required");
            focusView = firstNameEditText;
            cancel = true;
        }

        // Check for Last Name
        if (TextUtils.isEmpty(lastName)) {
            lastNameEditText.setError("Last Name is required");
            if (focusView == null) focusView = lastNameEditText;
            cancel = true;
        }

        // Check for Student ID
        if (TextUtils.isEmpty(userName)) {
            userNameEditText.setError("Username is required");
            if (focusView == null) focusView = userNameEditText;
            cancel = true;
        } else if (userName.length() < 5) { // Example: Student ID must be at least 5 characters
            userNameEditText.setError("Username must be at least 5 characters");
            if (focusView == null) focusView = userNameEditText;
            cancel = true;
        }

        // 2. Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
            if (focusView == null) focusView = emailEditText;
            cancel = true;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Enter a valid email address");
            if (focusView == null) focusView = emailEditText;
            cancel = true;
        }

        // Check for password
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            if (focusView == null) focusView = passwordEditText;
            cancel = true;
        } else if (password.length() < 6) { // Example: Password must be at least 6 characters
            passwordEditText.setError("Password too short (min 6 characters)");
            if (focusView == null) focusView = passwordEditText;
            cancel = true;
        }

        // Check for confirm password
        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordEditText.setError("Confirm Password is required");
            if (focusView == null) focusView = confirmPasswordEditText;
            cancel = true;
        } else if (!confirmPassword.equals(password)) { // 3. password and confirm password are same
            confirmPasswordEditText.setError("Passwords do not match");
            if (focusView == null) focusView = confirmPasswordEditText;
            cancel = true;
        }


        if (cancel) {
            // There was an error; don't attempt signup and focus the first form field with an error.
            if (focusView != null) {
                focusView.requestFocus();
            }
        } else {
            // All validations are success
            Toast.makeText(SignupActivity.this, "Account Created Successfully", Toast.LENGTH_LONG).show();
            navigateToHome();
        }
    }

    /**
     * Navigates to the LoginActivity.
     */
    private void navigateToLogin() {
        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
        startActivity(intent);
        finish(); // Finish SignupActivity so user cannot go back to it
    }

    /**
     * Navigates to the Home Activity (MainActivity placeholder).
     * This will be the main screen after successful signup.
     */
    private void navigateToHome() {
        // Replace MainActivity.class with your actual Home Activity when it's ready.
        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clears back stack
        startActivity(intent);
        finish(); // Finish SignupActivity
    }
}