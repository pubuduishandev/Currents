package com.example.currents.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable; // Import Editable
import android.text.TextUtils;
import android.text.TextWatcher; // Import TextWatcher
import android.util.Patterns;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.currents.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout; // Import TextInputLayout

public class SignupActivity extends AppCompatActivity {

    // UI elements
    private TextInputEditText firstNameEditText;
    private TextInputEditText lastNameEditText;
    private TextInputEditText userNameEditText;
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private TextInputEditText confirmPasswordEditText;
    private MaterialButton signupButton;
    private LinearLayout signinTextContainer;

    // Declare TextInputLayout references
    private TextInputLayout firstNameInputLayout;
    private TextInputLayout lastNameInputLayout;
    private TextInputLayout userNameInputLayout; // Renamed to match XML ID
    private TextInputLayout emailInputLayout;
    private TextInputLayout passwordInputLayout;
    private TextInputLayout confirmPasswordInputLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize UI elements
        firstNameEditText = findViewById(R.id.first_name_edit_text);
        lastNameEditText = findViewById(R.id.last_name_edit_text);
        userNameEditText = findViewById(R.id.user_name_edit_text); // Matches XML ID now
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        confirmPasswordEditText = findViewById(R.id.confirm_password_edit_text);
        signupButton = findViewById(R.id.signup_button);
        signinTextContainer = findViewById(R.id.signin_text_container);

        // Initialize TextInputLayout references
        firstNameInputLayout = findViewById(R.id.first_name_input_layout);
        lastNameInputLayout = findViewById(R.id.last_name_input_layout);
        userNameInputLayout = findViewById(R.id.user_name_input_layout); // Matches XML ID now
        emailInputLayout = findViewById(R.id.email_input_layout);
        passwordInputLayout = findViewById(R.id.password_input_layout);
        confirmPasswordInputLayout = findViewById(R.id.confirm_password_input_layout);


        // --- Add TextWatchers to clear errors as user types ---
        firstNameEditText.addTextChangedListener(new GenericTextWatcher(firstNameInputLayout));
        lastNameEditText.addTextChangedListener(new GenericTextWatcher(lastNameInputLayout));
        userNameEditText.addTextChangedListener(new GenericTextWatcher(userNameInputLayout));
        emailEditText.addTextChangedListener(new GenericTextWatcher(emailInputLayout));
        passwordEditText.addTextChangedListener(new GenericTextWatcher(passwordInputLayout));
        confirmPasswordEditText.addTextChangedListener(new GenericTextWatcher(confirmPasswordInputLayout));
        // --- End TextWatchers ---


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
     * Helper class for TextWatcher to avoid repetitive code for each field.
     * Clears error when text changes.
     */
    private class GenericTextWatcher implements TextWatcher {
        private final TextInputLayout textInputLayout;

        private GenericTextWatcher(TextInputLayout textInputLayout) {
            this.textInputLayout = textInputLayout;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (textInputLayout.isErrorEnabled()) {
                textInputLayout.setError(null);
                textInputLayout.setErrorEnabled(false);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {}
    }


    /**
     * Attempts to sign up the account specified by the signup form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual signup attempt is made.
     */
    private void attemptSignup() {
        // Clear previous errors for all fields
        firstNameInputLayout.setError(null);
        lastNameInputLayout.setError(null);
        userNameInputLayout.setError(null);
        emailInputLayout.setError(null);
        passwordInputLayout.setError(null);
        confirmPasswordInputLayout.setError(null);

        firstNameInputLayout.setErrorEnabled(false);
        lastNameInputLayout.setErrorEnabled(false);
        userNameInputLayout.setErrorEnabled(false);
        emailInputLayout.setErrorEnabled(false);
        passwordInputLayout.setErrorEnabled(false);
        confirmPasswordInputLayout.setErrorEnabled(false);


        // Store values at the time of the signup attempt.
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String userName = userNameEditText.getText().toString().trim(); // Corresponds to student_id_edit_text
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        boolean cancel = false;
        View focusView = null; // View to focus if an error occurs

        // 1. Check if all fields are filled & other validations

        // Check for Confirm Password
        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordInputLayout.setError("Confirm Password is required");
            confirmPasswordInputLayout.setErrorEnabled(true);
            focusView = confirmPasswordEditText;
            cancel = true;
        } else if (!confirmPassword.equals(password)) {
            confirmPasswordInputLayout.setError("Passwords do not match");
            confirmPasswordInputLayout.setErrorEnabled(true);
            focusView = confirmPasswordEditText;
            cancel = true;
        }

        // Check for Password
        if (TextUtils.isEmpty(password)) {
            passwordInputLayout.setError("Password is required");
            passwordInputLayout.setErrorEnabled(true);
            if (focusView == null) focusView = passwordEditText; // Set focus if no other error found yet
            cancel = true;
        } else if (password.length() < 6) { // Example: Password must be at least 6 characters
            passwordInputLayout.setError("Password too short (min 6 characters)");
            passwordInputLayout.setErrorEnabled(true);
            if (focusView == null) focusView = passwordEditText;
            cancel = true;
        }

        // Check for Email
        if (TextUtils.isEmpty(email)) {
            emailInputLayout.setError("Email is required");
            emailInputLayout.setErrorEnabled(true);
            if (focusView == null) focusView = emailEditText;
            cancel = true;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout.setError("Enter a valid email address");
            emailInputLayout.setErrorEnabled(true);
            if (focusView == null) focusView = emailEditText;
            cancel = true;
        }

        // Check for Username
        if (TextUtils.isEmpty(userName)) {
            userNameInputLayout.setError("Username is required");
            userNameInputLayout.setErrorEnabled(true);
            if (focusView == null) focusView = userNameEditText;
            cancel = true;
        } else if (userName.length() < 5) { // Example: Username must be at least 5 characters
            userNameInputLayout.setError("Username must be at least 5 characters");
            userNameInputLayout.setErrorEnabled(true);
            if (focusView == null) focusView = userNameEditText;
            cancel = true;
        }

        // Check for Last Name
        if (TextUtils.isEmpty(lastName)) {
            lastNameInputLayout.setError("Last Name is required");
            lastNameInputLayout.setErrorEnabled(true);
            if (focusView == null) focusView = lastNameEditText;
            cancel = true;
        }

        // Check for First Name (done last so it gets focus if all are empty)
        if (TextUtils.isEmpty(firstName)) {
            firstNameInputLayout.setError("First Name is required");
            firstNameInputLayout.setErrorEnabled(true);
            if (focusView == null) focusView = firstNameEditText;
            cancel = true;
        }


        if (cancel) {
            // There was an error; don't attempt signup and focus the first form field with an error.
            if (focusView != null) {
                focusView.requestFocus();
            }
            Toast.makeText(SignupActivity.this, "Please fix the errors to sign up", Toast.LENGTH_SHORT).show();
        } else {
            // All validations are success
            Toast.makeText(SignupActivity.this, "Account Created Successfully", Toast.LENGTH_LONG).show();
            // In a real app, you would send this data to a server for registration
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
        // Replace HomeActivity.class with your actual Home Activity when it's ready.
        Intent intent = new Intent(SignupActivity.this, HomeActivity.class);
        // Clear back stack to prevent going back to signup/login after successful registration
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Finish SignupActivity
    }
}