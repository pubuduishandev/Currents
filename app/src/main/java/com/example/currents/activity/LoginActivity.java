package com.example.currents.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.currents.R;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.Timestamp;

import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    // Tag for logging
    private static final String TAG = "LoginActivity";

    // UI components
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private MaterialButton loginButton;
    private TextView forgotPasswordText;
    private LinearLayout signupTextContainer;
    private TextInputLayout emailInputLayout;
    private TextInputLayout passwordInputLayout;

    // Firebase components
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // SharedPreferences keys
    private static final String PREF_NAME = "CurrentUserPrefs";
    private static final String KEY_USER_UID = "user_uid";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_FIRST_NAME = "first_name";
    private static final String KEY_LAST_NAME = "last_name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_UPDATED_AT = "updated_at";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Call the superclass method
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth and firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI components
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        loginButton = findViewById(R.id.login_button);
        forgotPasswordText = findViewById(R.id.forgot_password_text);
        signupTextContainer = findViewById(R.id.signup_text_container);
        emailInputLayout = findViewById(R.id.email_input_layout);
        passwordInputLayout = findViewById(R.id.password_input_layout);

        // Set up listeners for text changes
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

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });

        forgotPasswordText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });

        signupTextContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });
    }

    // Method to handle login attempt
    private void attemptLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        boolean isValid = true;

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

        if (password.isEmpty()) {
            passwordInputLayout.setError("Password cannot be empty");
            passwordInputLayout.setErrorEnabled(true);
            isValid = false;
        } else {
            passwordInputLayout.setError(null);
            passwordInputLayout.setErrorEnabled(false);
        }

        if (isValid) {
            setLoginButtonState(false, "Checking User...");

            // Step 1: Check if the email exists in firestore "users" collection
            db.collection("users")
                .whereEqualTo("email", email) // Assuming you store email in your user documents
                .limit(1) // We only need to find one
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null && !task.getResult().isEmpty()) {
                                // Email exists in firestore, proceed with Firebase Auth login
                                Log.d(TAG, "Email found in firestore, proceeding with Auth login.");
                                signInWithFirebase(email, password);
                            } else {
                                // Email not found in firestore, user is not registered
                                Log.d(TAG, "Email not found in firestore.");
                                setLoginButtonState(true, getString(R.string.login)); // Reset button
                                showUnregisteredUserDialog();
                            }
                        } else {
                            // Error querying firestore
                            Log.e(TAG, "Error checking email existence in firestore: " + task.getException().getMessage(), task.getException());
                            Toast.makeText(LoginActivity.this, R.string.user_not_registered, Toast.LENGTH_LONG).show();
                            setLoginButtonState(true, getString(R.string.login)); // Reset button
                        }
                    }
                });
        }
    }

    // Method to sign in with Firebase Auth
    private void signInWithFirebase(String email, String password) {
        setLoginButtonState(false, "Logging In..."); // Update button text for Auth login

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            fetchAndStoreUserDataByUid(firebaseUser.getUid());
                        } else {
                            Log.e(TAG, "Authentication successful but FirebaseUser is null.");
                            Toast.makeText(LoginActivity.this, R.string.something_went_wrong,
                                    Toast.LENGTH_LONG).show();
                            setLoginButtonState(true, getString(R.string.login));
                        }
                    } else {
                        setLoginButtonState(true, getString(R.string.login)); // Reset button on Auth failure
                        Exception exception = task.getException();

                        if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                            // This now specifically means incorrect password for an EXISTING user
                            String errorMessage = "Invalid password."; // Or "Invalid email or password." if you prefer
                            passwordInputLayout.setError(errorMessage);
                            passwordInputLayout.setErrorEnabled(true);
                            Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                        // FirebaseAuthInvalidUserException should ideally not happen here
                        // because we already checked firestore. If it does, it's an inconsistency.
                        else if (exception instanceof FirebaseAuthInvalidUserException) {
                            Log.w(TAG, "Auth returned FirebaseAuthInvalidUserException even after Firestore check. Inconsistency detected.");
                            showUnregisteredUserDialog(); // Fallback in case of unexpected auth error
                        }
                        else {
                            // General login failure
                            String errorMessage = "Login failed: " + (exception != null ? exception.getMessage() : "Unknown error");
                            Log.e(TAG, "Login failed: " + errorMessage, exception);
                            Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });
    }

    // Method to fetch user data from firestore by UID and store it in SharedPreferences
    private void fetchAndStoreUserDataByUid(String uid) {
        DocumentReference userDocRef = db.collection("users").document(uid);

        userDocRef.get()
            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Map<String, Object> userData = document.getData();

                            SharedPreferences sharedPref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();

                            editor.putString(KEY_USER_UID, uid);

                            if (userData != null) {
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

                                if (userData.containsKey("updatedAt")) {
                                    Object timestampObj = userData.get("updatedAt");
                                    if (timestampObj instanceof Timestamp) {
                                        editor.putLong(KEY_UPDATED_AT, ((Timestamp) timestampObj).toDate().getTime());
                                    } else {
                                        Log.w(TAG, "'updatedAt' is not a Timestamp object or is null.");
                                    }
                                } else {
                                    Log.d(TAG, "User data missing 'updatedAt' field. Defaulting to 0.");
                                    editor.putLong(KEY_UPDATED_AT, 0L);
                                }
                            } else {
                                Log.e(TAG, "firestore document data is null for UID: " + uid);
                            }

                            editor.apply();

                            Toast.makeText(LoginActivity.this, R.string.login_success, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Log.w(TAG, "No user document found in firestore for UID: " + uid + " after successful Auth login.");
                            Toast.makeText(LoginActivity.this, R.string.user_not_found,
                                    Toast.LENGTH_LONG).show();
                            mAuth.signOut(); // Sign out as data is incomplete
                            setLoginButtonState(true, getString(R.string.login));
                        }
                    } else {
                        Log.e(TAG, "Error fetching user data from firestore for UID: " + uid, task.getException());
                        Toast.makeText(LoginActivity.this, R.string.error_fetching_profile + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                        mAuth.signOut(); // Sign out on error
                        setLoginButtonState(true, getString(R.string.login));
                    }
                }
            });
    }

    // Method to show a dialog for unregistered users
    private void showUnregisteredUserDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.user_not_registered_title)
            .setMessage(R.string.user_not_registered_message)
            .setPositiveButton(R.string.sign_up, (dialog, which) -> {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                // Optionally, pass the entered email to the signup activity
                intent.putExtra("email", emailEditText.getText().toString().trim());
                startActivity(intent);
            })
            .setNegativeButton(R.string.cancel_button, (dialog, which) -> {
                dialog.dismiss();
                emailEditText.setText("");
                passwordEditText.setText("");
            })
            .show();
    }

    // Method to set the state of the login button
    private void setLoginButtonState(boolean enabled, String text) {
        loginButton.setEnabled(enabled);
        loginButton.setText(text);
    }

    // Override onStart to check if user is already logged in
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Log.d(TAG, R.string.already_login + currentUser.getEmail());
            Toast.makeText(this, R.string.already_login + currentUser.getEmail(), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
    }
}