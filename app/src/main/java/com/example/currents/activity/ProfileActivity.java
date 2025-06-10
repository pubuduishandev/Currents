package com.example.currents.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.example.currents.R;
import com.example.currents.ui.bottomsheet.ChangePasswordBottomSheet;
import com.example.currents.ui.bottomsheet.EditProfileBottomSheet;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity implements EditProfileBottomSheet.OnProfileEditedListener {
    // UI components
    private Toolbar toolbar;
    private TextView avatarInitialsTextView;
    private TextView userNameTextView;
    private TextView fullNameValue;
    private TextView usernameValue;
    private TextView emailValue;
    private TextView passwordValue;
    private CardView passwordCard;
    private LinearLayout logoutCard;
    private Button deleteAccountButton;

    // SharedPreferences name and keys
    private static final String PREF_NAME = "CurrentUserPrefs";
    private static final String KEY_USER_UID = "user_uid";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_FIRST_NAME = "first_name";
    private static final String KEY_LAST_NAME = "last_name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_UPDATED_AT = "updated_at";

    // Member variables to hold the current profile data
    private String currentUid;
    private String currentFirstName;
    private String currentLastName;
    private String currentUsername;
    private String currentEmail;
    private long currentUpdatedAtMillis;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Call the superclass method
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize UI components
        toolbar = findViewById(R.id.profileToolBar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Initialize SharedPreferences and load profile data
        avatarInitialsTextView = findViewById(R.id.avatarInitialsTextView);
        userNameTextView = findViewById(R.id.userNameTextView);
        fullNameValue = findViewById(R.id.fullNameValue);
        usernameValue = findViewById(R.id.usernameValue);
        emailValue = findViewById(R.id.emailValue);
        passwordValue = findViewById(R.id.passwordValue);
        passwordCard = findViewById(R.id.passwordCard);
        deleteAccountButton = findViewById(R.id.deleteAccountButton);

        // Initialize logoutCard if it exists in the layout
        loadProfileDataFromSharedPreferences();
        updateProfileUI();

        passwordCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangePasswordBottomSheet bottomSheet = ChangePasswordBottomSheet.newInstance();
                bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
            }
        });

        // Set OnClickListener for the new delete account button
        deleteAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteAccountConfirmationDialog();
            }
        });

        // Initialize logoutCard and set OnClickListener
        if (logoutCard != null) { // Check if logoutCard is not null before setting listener
            logoutCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    signOutUser();
                }
            });
        }
    }

    // Load profile data from SharedPreferences
    private void loadProfileDataFromSharedPreferences() {
        SharedPreferences sharedPref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        currentUid = sharedPref.getString(KEY_USER_UID, null);
        currentUsername = sharedPref.getString(KEY_USERNAME, "N/A");
        currentFirstName = sharedPref.getString(KEY_FIRST_NAME, "N/A");
        currentLastName = sharedPref.getString(KEY_LAST_NAME, "N/A");
        currentEmail = sharedPref.getString(KEY_EMAIL, "N/A");
        currentUpdatedAtMillis = sharedPref.getLong(KEY_UPDATED_AT, 0L); // NEW: Load updatedAt
    }

    // Update the UI with the loaded profile data
    private void updateProfileUI() {
        String initials = "";
        if (currentFirstName != null && !currentFirstName.isEmpty()) {
            initials += currentFirstName.charAt(0);
        }
        if (currentLastName != null && !currentLastName.isEmpty()) {
            initials += currentLastName.charAt(0);
        }
        avatarInitialsTextView.setText(initials.toUpperCase());

        String fullName = currentFirstName + " " + currentLastName;
        userNameTextView.setText(fullName);
        fullNameValue.setText(fullName);
        usernameValue.setText(currentUsername);
        emailValue.setText(currentEmail);
        passwordValue.setText(R.string.password_change); // Reset to fixed text
    }

    // Override onCreateOptionsMenu to inflate the menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.profile_toolbar_menu, menu);
        return true;
    }

    // Override onOptionsItemSelected to handle menu item clicks
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_edit) {
            // Pass currentUpdatedAtMillis to the EditProfileBottomSheet
            EditProfileBottomSheet bottomSheet = EditProfileBottomSheet.newInstance(
                    currentFirstName,
                    currentLastName,
                    currentUsername,
                    currentEmail,
                    currentUpdatedAtMillis // NEW: Pass updatedAt
            );
            bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
            return true;
        } else if (id == R.id.action_bookmark) {
            Intent intent = new Intent(ProfileActivity.this, SavedNewsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_more) {
            showSignOutConfirmationDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Override onSupportNavigateUp to handle the back button in the toolbar
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Implement the OnProfileEditedListener interface method
    @Override
    public void onProfileEdited(String newFirstName, String newLastName, String newUsername) {
        updateProfileInFirestore(newFirstName, newLastName, newUsername, currentEmail);
    }

    // Method to update the profile in firestore
    private void updateProfileInFirestore(String firstName, String lastName, String username, String email) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, R.string.error_user_not_found, Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> updates = new HashMap<>();
        updates.put("firstName", firstName);
        updates.put("lastName", lastName);
        updates.put("username", username);
        updates.put("email", email); // Keep email in Firestore updated with current user's email
        updates.put("updatedAt", Timestamp.now()); // NEW: Update the updatedAt timestamp

        db.collection("users").document(user.getUid())
            .update(updates)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(ProfileActivity.this, R.string.profile_updated_success, Toast.LENGTH_SHORT).show();
                // Update local SharedPreferences and UI only after successful firestore update
                // NEW: Also update currentUpdatedAtMillis in shared prefs
                saveProfileDataToSharedPreferences(firstName, lastName, username, email, Timestamp.now().toDate().getTime());
                currentFirstName = firstName;
                currentLastName = lastName;
                currentUsername = username;
                currentUpdatedAtMillis = Timestamp.now().toDate().getTime(); // Update member variable
                updateProfileUI(); // Refresh UI
            })
            .addOnFailureListener(e -> {
                Toast.makeText(ProfileActivity.this, R.string.profile_update_error + e.getMessage(), Toast.LENGTH_LONG).show();
            });
    }

    // Helper method to save updated profile data to SharedPreferences
    private void saveProfileDataToSharedPreferences(String firstName, String lastName, String username, String email, long updatedAtMillis) {
        SharedPreferences sharedPref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(KEY_FIRST_NAME, firstName);
        editor.putString(KEY_LAST_NAME, lastName);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_EMAIL, email);
        editor.putLong(KEY_UPDATED_AT, updatedAtMillis); // NEW: Save updatedAt
        editor.apply();
    }

    // Method to show a confirmation dialog for signing out
    private void showSignOutConfirmationDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.sign_out) // Use a string resource for title
            .setMessage(R.string.sign_out_confirmation_message) // Use a string resource for message
            .setPositiveButton(R.string.yes, (dialog, which) -> {
                signOutUser();
            })
            .setNegativeButton(R.string.no, (dialog, which) -> {
                dialog.dismiss();
            })
            .show();
    }

    // Method to sign out the user
    private void signOutUser() {
        FirebaseAuth.getInstance().signOut();

        SharedPreferences sharedPref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.apply();

        Toast.makeText(ProfileActivity.this, R.string.logout_success, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // Method to show a confirmation dialog before deleting the account
    private void showDeleteAccountConfirmationDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_account_title)
            .setMessage(R.string.delete_account_message)
            .setPositiveButton(R.string.yes, (dialog, which) -> {
                deleteUser();
            })
            .setNegativeButton(R.string.no, (dialog, which) -> {
                dialog.dismiss();
            })
            .show();
    }

    // Method to delete the user account
    public void deleteUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Toast.makeText(this, R.string.no_user_found, Toast.LENGTH_SHORT).show();
            // Redirect to login if no user is found unexpectedly
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        String userId = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Step 1: Delete user document from firestore
        db.collection("users").document(userId)
            .delete()
            .addOnSuccessListener(aVoid -> {
                // Step 2: Delete user from Firebase Authentication
                user.delete()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(ProfileActivity.this, R.string.delete_account_success, Toast.LENGTH_LONG).show();

                                // Step 3: Clear SharedPreferences
                                SharedPreferences sharedPref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.clear();
                                editor.apply();

                                // Step 4: Navigate to LoginActivity
                                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();

                            } else {
                                Toast.makeText(ProfileActivity.this, R.string.delete_account_error, Toast.LENGTH_LONG).show();
                                signOutUser(); // Assuming you have this function
                            }
                        }
                    });
            })
            .addOnFailureListener(e -> {
                Toast.makeText(ProfileActivity.this, R.string.delete_account_error, Toast.LENGTH_LONG).show();
            });
    }
}