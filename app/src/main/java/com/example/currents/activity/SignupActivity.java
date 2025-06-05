package com.example.currents.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.currents.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

// Firebase Imports
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue; // For server timestamps
import com.google.firebase.firestore.QuerySnapshot; // For username check

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "SignupActivity";

    // UI elements
    private TextInputEditText firstNameEditText;
    private TextInputEditText lastNameEditText;
    private TextInputEditText userNameEditText;
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private TextInputEditText confirmPasswordEditText;
    private MaterialButton signupButton;
    private LinearLayout signinTextContainer;

    // TextInputLayout references
    private TextInputLayout firstNameInputLayout;
    private TextInputLayout lastNameInputLayout;
    private TextInputLayout userNameInputLayout;
    private TextInputLayout emailInputLayout;
    private TextInputLayout passwordInputLayout;
    private TextInputLayout confirmPasswordInputLayout;

    // Firebase instances
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // SharedPreferences name and keys (MUST MATCH LoginActivity/ProfileActivity)
    private static final String PREF_NAME = "CurrentUserPrefs";
    private static final String KEY_USER_UID = "user_uid"; // Document ID / Firebase Auth UID
    private static final String KEY_USERNAME = "username";
    private static final String KEY_FIRST_NAME = "first_name";
    private static final String KEY_LAST_NAME = "last_name";
    private static final String KEY_EMAIL = "email";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI elements
        firstNameEditText = findViewById(R.id.first_name_edit_text);
        lastNameEditText = findViewById(R.id.last_name_edit_text);
        userNameEditText = findViewById(R.id.user_name_edit_text);
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        confirmPasswordEditText = findViewById(R.id.confirm_password_edit_text);
        signupButton = findViewById(R.id.signup_button);
        signinTextContainer = findViewById(R.id.signin_text_container);

        // Initialize TextInputLayout references
        firstNameInputLayout = findViewById(R.id.first_name_input_layout);
        lastNameInputLayout = findViewById(R.id.last_name_input_layout);
        userNameInputLayout = findViewById(R.id.user_name_input_layout);
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
        // Reset errors
        resetInputLayoutErrors();

        // Store values at the time of the signup attempt.
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String username = userNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        // 1. All fields are filled & other validations
        // Order of validation for focus control: from bottom to top of the form, generally

        // Check for Confirm Password (5. password and confirm password matched)
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

        // Check for Password (3. password is more than 6 characters)
        if (TextUtils.isEmpty(password)) {
            passwordInputLayout.setError("Password is required");
            passwordInputLayout.setErrorEnabled(true);
            if (focusView == null) focusView = passwordEditText;
            cancel = true;
        } else if (password.length() < 6) {
            passwordInputLayout.setError("Password too short (min 6 characters)");
            passwordInputLayout.setErrorEnabled(true);
            if (focusView == null) focusView = passwordEditText;
            cancel = true;
        }

        // Check for Email (1. field filled, 2. valid email)
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

        // Check for Username (1. field filled, min length)
        if (TextUtils.isEmpty(username)) {
            userNameInputLayout.setError("Username is required");
            userNameInputLayout.setErrorEnabled(true);
            if (focusView == null) focusView = userNameEditText;
            cancel = true;
        } else if (username.length() < 5) {
            userNameInputLayout.setError("Username must be at least 5 characters");
            userNameInputLayout.setErrorEnabled(true);
            if (focusView == null) focusView = userNameEditText;
            cancel = true;
        }

        // Check for Last Name (1. field filled)
        if (TextUtils.isEmpty(lastName)) {
            lastNameInputLayout.setError("Last Name is required");
            lastNameInputLayout.setErrorEnabled(true);
            if (focusView == null) focusView = lastNameEditText;
            cancel = true;
        }

        // Check for First Name (1. field filled)
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
            // All client-side validations passed. Proceed with Firebase checks.
            signupButton.setEnabled(false); // Disable button
            signupButton.setText("Creating Account..."); // Provide feedback

            // 4. Check if username is already in use in Firestore
            checkUsernameAndCreateUser(firstName, lastName, username, email, password);
        }
    }

    private void resetInputLayoutErrors() {
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
    }

    private void checkUsernameAndCreateUser(String firstName, String lastName, String username, String email, String password) {
        db.collection("users")
                .whereEqualTo("username", username)
                .limit(1) // Assuming usernames are unique
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null && !task.getResult().isEmpty()) {
                                // Username already exists
                                userNameInputLayout.setError("Username already taken. Please choose another.");
                                userNameInputLayout.setErrorEnabled(true);
                                userNameEditText.requestFocus();
                                Toast.makeText(SignupActivity.this, "Username already exists.", Toast.LENGTH_SHORT).show();
                                resetSignupForm(); // Re-enable button etc.
                                Log.d(TAG, "Username '" + username + "' already taken.");
                            } else {
                                // Username is available, proceed to create user with Firebase Auth
                                Log.d(TAG, "Username '" + username + "' is available. Proceeding with Auth creation.");
                                createUserWithFirebaseAuth(firstName, lastName, username, email, password);
                            }
                        } else {
                            // Error checking username uniqueness
                            Toast.makeText(SignupActivity.this, "Error checking username: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                            Log.e(TAG, "Error checking username uniqueness", task.getException());
                            resetSignupForm();
                        }
                    }
                });
    }

    private void createUserWithFirebaseAuth(String firstName, String lastName, String username, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // User created successfully in Firebase Auth
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                Log.d(TAG, "Firebase Auth user created: " + user.getUid());

                                // Store user data in Firestore
                                saveUserDataToFirestore(user, firstName, lastName, username, email);

                            } else {
                                Log.e(TAG, "Firebase Auth user creation successful, but currentUser is null.");
                                Toast.makeText(SignupActivity.this, "Signup failed: Internal error.",
                                        Toast.LENGTH_LONG).show();
                                resetSignupForm();
                            }
                        } else {
                            // If sign up fails, display a message to the user.
                            resetSignupForm(); // Re-enable button etc.
                            String errorMessage = "Signup failed.";
                            Exception exception = task.getException();

                            if (exception instanceof FirebaseAuthWeakPasswordException) {
                                errorMessage = "Password is too weak. Please use a stronger password.";
                                passwordInputLayout.setError(errorMessage);
                                passwordInputLayout.setErrorEnabled(true);
                                passwordEditText.requestFocus();
                            } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                                errorMessage = "Invalid email format. Please check your email.";
                                emailInputLayout.setError(errorMessage);
                                emailInputLayout.setErrorEnabled(true);
                                emailEditText.requestFocus();
                            } else if (exception instanceof FirebaseAuthUserCollisionException) {
                                errorMessage = "This email is already registered. Please sign in or use a different email.";
                                emailInputLayout.setError(errorMessage);
                                emailInputLayout.setErrorEnabled(true);
                                emailEditText.requestFocus();
                            } else {
                                errorMessage = "Signup failed: " + (exception != null ? exception.getMessage() : "Unknown error");
                                Log.e(TAG, "Firebase Auth signup failed: " + errorMessage, exception);
                            }
                            Toast.makeText(SignupActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void saveUserDataToFirestore(FirebaseUser user, String firstName, String lastName, String username, String email) {
        // Capture the current timestamp for fields like lastPasswordChange, createdAt etc.
        // This will be stored in Firestore as a Server Timestamp, and we can use current device time for SharedPreferences
        long currentTimestampMillis = System.currentTimeMillis();

        Map<String, Object> userData = new HashMap<>();
        userData.put("firstName", firstName);
        userData.put("lastName", lastName);
        userData.put("username", username);
        userData.put("email", email);
        userData.put("createdAt", FieldValue.serverTimestamp());
        userData.put("updatedAt", FieldValue.serverTimestamp());

        // Store data in Firestore using the user's UID as the document ID
        db.collection("users").document(user.getUid())
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User data successfully saved to Firestore for UID: " + user.getUid());

                    // --- Send Email Verification ---
                    user.sendEmailVerification()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "Verification email sent to " + user.getEmail());
                                        Toast.makeText(SignupActivity.this,
                                                "Account created! Please check your email for a verification link.",
                                                Toast.LENGTH_LONG).show();
                                    } else {
                                        Log.e(TAG, "Failed to send verification email.", task.getException());
                                        Toast.makeText(SignupActivity.this,
                                                "Account created, but failed to send verification email. Please check your spam folder or try verifying later.",
                                                Toast.LENGTH_LONG).show();
                                    }
                                    // --- Navigate to HomeActivity directly after email verification attempt ---
                                    navigateToHome();
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    // If Firestore save fails, delete the user from Firebase Auth to avoid inconsistencies
                    Log.e(TAG, "Error saving user data to Firestore for UID: " + user.getUid(), e);
                    user.delete()
                            .addOnCompleteListener(deleteTask -> {
                                if (deleteTask.isSuccessful()) {
                                    Log.d(TAG, "User deleted from Firebase Auth due to Firestore save failure.");
                                    Toast.makeText(SignupActivity.this, "Account creation failed due to database error. Please try again.",
                                            Toast.LENGTH_LONG).show();
                                } else {
                                    Log.e(TAG, "Failed to delete user from Auth after Firestore save failure.", deleteTask.getException());
                                    Toast.makeText(SignupActivity.this, "Account created, but a database error occurred.",
                                            Toast.LENGTH_LONG).show();
                                }
                                resetSignupForm();
                            });
                });
    }


    /**
     * Resets the signup form UI elements (button state).
     */
    private void resetSignupForm() {
        signupButton.setEnabled(true);
        signupButton.setText(R.string.sign_up); // Assuming R.string.signup contains "Sign Up"
    }

    /**
     * Navigates to the LoginActivity.
     */
    private void navigateToLogin() {
        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
        startActivity(intent);
        finish(); // Finish SignupActivity
    }

    /**
     * Navigates to the Home Activity (MainActivity placeholder).
     * This will be the main screen after successful signup.
     */
    private void navigateToHome() {
        // --- Store user data in SharedPreferences before navigating ---
        // This part needs to be done here, as saveUserDataToFirestore's success listener
        // now contains the email verification step.
        // We get the data directly from the input fields since we know it's valid at this point.
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String username = userNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        FirebaseUser user = mAuth.getCurrentUser(); // Get current user (should not be null here)

        if (user != null) {
            SharedPreferences sharedPref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();

            editor.putString(KEY_USER_UID, user.getUid());
            editor.putString(KEY_FIRST_NAME, firstName);
            editor.putString(KEY_LAST_NAME, lastName);
            editor.putString(KEY_USERNAME, username);
            editor.putString(KEY_EMAIL, email);

            editor.apply(); // Apply changes asynchronously
            Log.d(TAG, "User data saved to SharedPreferences before navigating to Home.");
        } else {
            Log.e(TAG, "User is null when trying to save to SharedPreferences in navigateToHome.");
            Toast.makeText(this, "Internal error: Could not save local data.", Toast.LENGTH_SHORT).show();
        }

        // --- Actual Navigation ---
        Intent intent = new Intent(SignupActivity.this, HomeActivity.class);
        // Clear back stack to prevent going back to signup/login after successful registration
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Finish SignupActivity
    }
}