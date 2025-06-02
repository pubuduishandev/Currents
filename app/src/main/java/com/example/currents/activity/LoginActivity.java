package com.example.currents.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log; // Added for logging
import android.util.Patterns;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.currents.R;

// Firebase Imports
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot; // Added for query results
import com.google.firebase.Timestamp; // Import Timestamp for handling Firestore Timestamps

import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity"; // For Logcat

    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private MaterialButton loginButton;
    private TextView forgotPasswordText;
    private LinearLayout signupTextContainer;

    private TextInputLayout emailInputLayout;
    private TextInputLayout passwordInputLayout;

    // Firebase instances
    private FirebaseAuth mAuth;
    private FirebaseFirestore db; // Re-added Firestore instance

    // SharedPreferences name and keys
    private static final String PREF_NAME = "CurrentUserPrefs";
    private static final String KEY_USER_UID = "user_uid"; // Document ID / Firebase Auth UID
    private static final String KEY_USERNAME = "username";
    private static final String KEY_FIRST_NAME = "first_name";
    private static final String KEY_LAST_NAME = "last_name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_LAST_PASSWORD_CHANGE = "last_password_change";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // Re-initialized Firestore

        // Initialize UI elements
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        loginButton = findViewById(R.id.login_button);
        forgotPasswordText = findViewById(R.id.forgot_password_text);
        signupTextContainer = findViewById(R.id.signup_text_container);

        emailInputLayout = findViewById(R.id.email_input_layout);
        passwordInputLayout = findViewById(R.id.password_input_layout);

        // --- TextWatchers to clear errors as user types ---
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

        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (passwordInputLayout.isErrorEnabled()) {
                    passwordInputLayout.setError(null);
                    passwordInputLayout.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        // --- End TextWatchers ---

        // Set OnClickListener for the Login Button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                boolean isValid = true; // Flag to track overall form validity

                // --- 1. Validate Email Field (empty and format) ---
                if (email.isEmpty()) {
                    emailInputLayout.setError("Email cannot be empty");
                    emailInputLayout.setErrorEnabled(true);
                    isValid = false;
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailInputLayout.setError("Enter a valid email address");
                    emailInputLayout.setErrorEnabled(true);
                    isValid = false;
                } else {
                    emailInputLayout.setError(null);
                    emailInputLayout.setErrorEnabled(false);
                }

                // --- 2. Validate Password Field (empty) ---
                if (password.isEmpty()) {
                    passwordInputLayout.setError("Password cannot be empty");
                    passwordInputLayout.setErrorEnabled(true);
                    isValid = false;
                } else {
                    passwordInputLayout.setError(null);
                    passwordInputLayout.setErrorEnabled(false);
                }

                // --- Proceed if all fields are valid ---
                if (isValid) {
                    // Start Firebase Authentication
                    loginButton.setEnabled(false); // Disable button to prevent multiple clicks
                    loginButton.setText("Logging In..."); // Provide feedback to the user

                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Login successful in Firebase Auth
                                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                        if (firebaseUser != null) {
                                            // 5. If match, then login success & retrieve data from Firestore
                                            // Pass the email used for login to fetch data
                                            fetchAndStoreUserDataByEmail(firebaseUser.getEmail(), firebaseUser.getUid()); // Also pass UID
                                        } else {
                                            // This case should ideally not happen if task is successful
                                            Log.e(TAG, "Authentication successful but FirebaseUser is null.");
                                            Toast.makeText(LoginActivity.this, "Authentication successful, but user data not found. Please contact support.",
                                                    Toast.LENGTH_LONG).show();
                                            resetLoginForm();
                                        }
                                    } else {
                                        // Login failed in Firebase Auth
                                        resetLoginForm();

                                        String errorMessage = "Authentication failed.";
                                        Exception exception = task.getException();

                                        if (exception instanceof FirebaseAuthInvalidUserException) {
                                            // 3. check firebase auth & users table have given email (if not error message show "User not registered")
                                            errorMessage = "User not registered. Please sign up.";
                                            emailInputLayout.setError(errorMessage);
                                            emailInputLayout.setErrorEnabled(true);
                                        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                                            // 4. check given email and password match in firebase auth (if not then error message show "Invalid email or password")
                                            errorMessage = "Invalid email or password.";
                                            passwordInputLayout.setError(errorMessage);
                                            passwordInputLayout.setErrorEnabled(true);
                                        } else {
                                            // General error (e.g., network issues)
                                            errorMessage = "Login failed: " + exception.getMessage();
                                            Log.e(TAG, "Login failed: " + exception.getMessage(), exception);
                                        }
                                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                }
            }
        });

        // Set OnClickListener for Forgot Password
        forgotPasswordText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });

        // Set OnClickListener for Sign Up (using the LinearLayout as the clickable area)
        signupTextContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });
    }

    // Method to fetch user data from Firestore using email and store in SharedPreferences
    private void fetchAndStoreUserDataByEmail(String email, String authUid) { // Pass both email and Auth UID
        db.collection("users")
                .whereEqualTo("email", email) // Querying by email as requested
                .limit(1) // Assuming email is unique, limit to 1 document
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() { // QuerySnapshot for queries
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null && !task.getResult().isEmpty()) {
                                // Found at least one document matching the email
                                DocumentSnapshot document = task.getResult().getDocuments().get(0); // Get the first one
                                Map<String, Object> userData = document.getData();

                                // Store user data in SharedPreferences
                                SharedPreferences sharedPref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPref.edit();

                                // Store Auth UID as document ID
                                editor.putString(KEY_USER_UID, document.getId()); // Use document.getId() which should be the UID

                                // Ensure email from Auth matches Firestore email, if not, something is wrong
                                if (!document.getId().equals(authUid)) {
                                    // This indicates a potential inconsistency, log it but proceed
                                    Log.w(TAG, "Firestore document UID does not match Auth UID for email: " + email);
                                }


                                if (userData != null) { // Check if userData map is not null
                                    if (userData.containsKey("username")) {
                                        editor.putString(KEY_USERNAME, (String) userData.get("username"));
                                    } else { Log.d(TAG, "User data missing 'username' field."); }
                                    if (userData.containsKey("firstName")) {
                                        editor.putString(KEY_FIRST_NAME, (String) userData.get("firstName"));
                                    } else { Log.d(TAG, "User data missing 'firstName' field."); }
                                    if (userData.containsKey("lastName")) {
                                        editor.putString(KEY_LAST_NAME, (String) userData.get("lastName"));
                                    } else { Log.d(TAG, "User data missing 'lastName' field."); }
                                    if (userData.containsKey("email")) {
                                        editor.putString(KEY_EMAIL, (String) userData.get("email"));
                                    } else { Log.d(TAG, "User data missing 'email' field."); }

                                    // Handle Timestamp for lastPasswordChange
                                    if (userData.containsKey("lastPasswordChange")) {
                                        Object timestampObj = userData.get("lastPasswordChange");
                                        if (timestampObj instanceof Timestamp) {
                                            // Convert Firebase Timestamp to a long (milliseconds) for SharedPreferences
                                            editor.putLong(KEY_LAST_PASSWORD_CHANGE, ((Timestamp) timestampObj).toDate().getTime());
                                        } else {
                                            Log.w(TAG, "'lastPasswordChange' is not a Timestamp object.");
                                        }
                                    } else { Log.d(TAG, "User data missing 'lastPasswordChange' field."); }

                                    // Add other fields from your user document as needed
                                    // e.g., editor.putString("KEY_PROFILE_PIC_URL", (String) userData.get("profilePicUrl"));
                                } else {
                                    Log.e(TAG, "Firestore document data is null for UID: " + document.getId());
                                }

                                editor.apply(); // Apply changes asynchronously

                                Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                startActivity(intent);
                                finish(); // Finish LoginActivity
                            } else {
                                // No document found in Firestore for the given email
                                Log.w(TAG, "No user document found in Firestore for email: " + email);
                                Toast.makeText(LoginActivity.this, "Login successful, but user profile data not found. Please ensure your profile is complete.",
                                        Toast.LENGTH_LONG).show();
                                mAuth.signOut(); // Consider logging out from Auth if profile is missing
                                resetLoginForm();
                            }
                        } else {
                            // Error fetching document from Firestore
                            Log.e(TAG, "Error fetching user data from Firestore for email: " + email, task.getException());
                            Toast.makeText(LoginActivity.this, "Error fetching user data: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                            mAuth.signOut(); // Log out if data fetch fails
                            resetLoginForm();
                        }
                    }
                });
    }

    // Helper method to reset UI state after login attempt
    private void resetLoginForm() {
        loginButton.setEnabled(true);
        loginButton.setText(R.string.login); // Assuming R.string.login contains "Login"
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is already signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            // User is already logged in, skip login activity and go to HomeActivity
            // Here, you might want to fetch user data again if you rely on it being fresh,
            // or just use the data already stored in SharedPreferences (if it's assumed to be recent enough).
            // For now, it will directly navigate.
            Log.d(TAG, "User already logged in: " + currentUser.getEmail());
            Toast.makeText(this, "Already logged in as " + currentUser.getEmail(), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
    }
}