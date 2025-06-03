package com.example.currents.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.example.currents.R;
import com.example.currents.ui.bottomsheet.ChangePasswordBottomSheet;
import com.example.currents.ui.bottomsheet.EditProfileBottomSheet;
// No longer importing ReauthenticateUserBottomSheet here
// import com.example.currents.ui.bottomsheet.ReauthenticateUserBottomSheet;
import com.google.firebase.auth.AuthCredential; // Still needed if ChangePasswordBottomSheet uses reauth
import com.google.firebase.auth.EmailAuthProvider; // Still needed if ChangePasswordBottomSheet uses reauth
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity
        implements EditProfileBottomSheet.OnProfileEditedListener
        /* REMOVED: , ReauthenticateUserBottomSheet.OnReauthenticationListener */ {

    private Toolbar toolbar;
    private TextView avatarInitialsTextView;
    private TextView userNameTextView;
    private TextView fullNameValue;
    private TextView usernameValue;
    private TextView emailValue;
    private TextView passwordValue;
    private CardView passwordCard;
    private LinearLayout logoutCard;

    // SharedPreferences name and keys (MUST MATCH LoginActivity/SignupActivity)
    private static final String PREF_NAME = "CurrentUserPrefs";
    private static final String KEY_USER_UID = "user_uid";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_FIRST_NAME = "first_name";
    private static final String KEY_LAST_NAME = "last_name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_LAST_PASSWORD_CHANGE = "last_password_change";

    // Member variables to hold the current profile data
    private String currentUid;
    private String currentFirstName;
    private String currentLastName;
    private String currentUsername;
    private String currentEmail;
    private long currentLastPasswordChangeMillis;

    // Variables to hold the new profile data while re-authenticating
    // These are no longer strictly needed for email change, but keep if ChangePasswordBottomSheet needs them
    private String pendingNewFirstName;
    private String pendingNewLastName;
    private String pendingNewUsername;
    // REMOVED: private String pendingNewEmail; // Email is not pending change from here

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        toolbar = findViewById(R.id.profileToolBar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Initialize all your views
        avatarInitialsTextView = findViewById(R.id.avatarInitialsTextView);
        userNameTextView = findViewById(R.id.userNameTextView);
        fullNameValue = findViewById(R.id.fullNameValue);
        usernameValue = findViewById(R.id.usernameValue);
        emailValue = findViewById(R.id.emailValue);
        passwordValue = findViewById(R.id.passwordValue);
        passwordCard = findViewById(R.id.passwordCard);

        // --- Retrieve data from SharedPreferences ---
        loadProfileDataFromSharedPreferences();

        // Set initial data to UI
        updateProfileUI();

        // Set click listener for the passwordCard
        passwordCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ChangePasswordBottomSheet should still handle re-authentication if it changes password
                ChangePasswordBottomSheet bottomSheet = ChangePasswordBottomSheet.newInstance();
                bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
            }
        });

        // Set click listener for the Logout card/button
        if (logoutCard != null) {
            logoutCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    signOutUser();
                }
            });
        }
    }

    private void loadProfileDataFromSharedPreferences() {
        SharedPreferences sharedPref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        currentUid = sharedPref.getString(KEY_USER_UID, null);
        currentUsername = sharedPref.getString(KEY_USERNAME, "N/A");
        currentFirstName = sharedPref.getString(KEY_FIRST_NAME, "N/A");
        currentLastName = sharedPref.getString(KEY_LAST_NAME, "N/A");
        currentEmail = sharedPref.getString(KEY_EMAIL, "N/A");
        currentLastPasswordChangeMillis = sharedPref.getLong(KEY_LAST_PASSWORD_CHANGE, 0L);
    }

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

        if (currentLastPasswordChangeMillis > 0) {
            String timeAgo = DateUtils.getRelativeTimeSpanString(
                    currentLastPasswordChangeMillis,
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE
            ).toString();
            passwordValue.setText("Changed " + timeAgo);
        } else {
            passwordValue.setText("Never changed or N/A");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.profile_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_edit) {
            EditProfileBottomSheet bottomSheet = EditProfileBottomSheet.newInstance(
                    currentFirstName,
                    currentLastName,
                    currentUsername,
                    currentEmail // Still pass email to display it in the sheet
            );
            bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
            return true;
        } else if (id == R.id.action_bookmark) {
            Intent intent = new Intent(ProfileActivity.this, SavedNewsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_more) {
            signOutUser();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // --- Implementation of OnProfileEditedListener ---
    @Override
    public void onProfileEdited(String newFirstName, String newLastName, String newUsername) {
        // Email is no longer passed or updated here.
        // Directly update Firestore with the new (editable) profile data
        updateProfileInFirestore(newFirstName, newLastName, newUsername, currentEmail); // Use currentEmail
    }

    // --- REMOVED: Implementation of OnReauthenticationListener ---
    // This interface and its method are no longer needed for email updates.
    // If ChangePasswordBottomSheet needs it, it must implement its own re-authentication
    // logic or ProfileActivity needs to keep this if ChangePasswordBottomSheet delegates it here.
    /*
    @Override
    public void onReauthenticationNeeded(String currentPassword) {
        // ... (removed)
    }
    */

    // --- Firebase Update Functions ---
    // The updateEmailInFirebaseAuth method is no longer used or needed for this flow.
    // private void updateEmailInFirebaseAuth(String newEmail, String firstName, String lastName, String username) { ... }

    private void updateProfileInFirestore(String firstName, String lastName, String username, String email) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> updates = new HashMap<>();
        updates.put("firstName", firstName);
        updates.put("lastName", lastName);
        updates.put("username", username);
        updates.put("email", email); // Keep email in Firestore updated with current user's email

        db.collection("users").document(user.getUid())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ProfileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                    // Update local SharedPreferences and UI only after successful Firestore update
                    saveProfileDataToSharedPreferences(firstName, lastName, username, email); // email will be the original one
                    currentFirstName = firstName;
                    currentLastName = lastName;
                    currentUsername = username;
                    // currentEmail remains unchanged as it's not edited
                    updateProfileUI(); // Refresh UI
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ProfileActivity.this, "Error updating profile" + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // Helper method to save updated profile data to SharedPreferences
    private void saveProfileDataToSharedPreferences(String firstName, String lastName, String username, String email) {
        SharedPreferences sharedPref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(KEY_FIRST_NAME, firstName);
        editor.putString(KEY_LAST_NAME, lastName);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_EMAIL, email); // email will be the original one
        // Note: KEY_LAST_PASSWORD_CHANGE is updated when password is changed separately
        editor.apply();
    }

    private void signOutUser() {
        FirebaseAuth.getInstance().signOut();

        SharedPreferences sharedPref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.apply();

        Toast.makeText(ProfileActivity.this, "Logout successful", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}