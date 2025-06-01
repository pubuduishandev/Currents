package com.example.currents.activity; // Adjust package name

import android.annotation.SuppressLint;
import android.content.Context; // Import for SharedPreferences
import android.content.Intent;
import android.content.SharedPreferences; // Import for SharedPreferences
import android.os.Bundle;
import android.text.format.DateUtils; // For formatting time
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout; // Ensure this is imported if you use it in the layout
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.example.currents.R;
import com.example.currents.ui.bottomsheet.ChangePasswordBottomSheet;
import com.example.currents.ui.bottomsheet.EditProfileBottomSheet;
import com.google.firebase.auth.FirebaseAuth; // Import FirebaseAuth for logout

import java.util.Date; // For Date object

public class ProfileActivity extends AppCompatActivity implements EditProfileBottomSheet.OnProfileEditedListener {

    private Toolbar toolbar;
    private TextView avatarInitialsTextView;
    private TextView userNameTextView;
    private TextView fullNameValue;
    private TextView usernameValue;
    private TextView emailValue;
    private TextView passwordValue;
    private CardView passwordCard;
    private LinearLayout logoutCard; // Assuming you have a card/view for logout

    // SharedPreferences name and keys (MUST MATCH LoginActivity/SignupActivity)
    private static final String PREF_NAME = "CurrentUserPrefs";
    private static final String KEY_USER_UID = "user_uid";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_FIRST_NAME = "first_name";
    private static final String KEY_LAST_NAME = "last_name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_LAST_PASSWORD_CHANGE = "last_password_change"; // Stored as long (milliseconds)

    // Member variables to hold the current profile data
    private String currentUid;
    private String currentFirstName;
    private String currentLastName;
    private String currentUsername;
    private String currentEmail;
    private long currentLastPasswordChangeMillis; // Store as long

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
                ChangePasswordBottomSheet bottomSheet = ChangePasswordBottomSheet.newInstance();
                bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
            }
        });

        // Set click listener for the Logout card/button
        if (logoutCard != null) { // Check if the logoutCard exists in your layout
            logoutCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    signOutUser();
                }
            });
        }
    }

    // New method to load profile data from SharedPreferences
    private void loadProfileDataFromSharedPreferences() {
        SharedPreferences sharedPref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        currentUid = sharedPref.getString(KEY_USER_UID, null);
        currentUsername = sharedPref.getString(KEY_USERNAME, "N/A"); // Default value if not found
        currentFirstName = sharedPref.getString(KEY_FIRST_NAME, "N/A");
        currentLastName = sharedPref.getString(KEY_LAST_NAME, "N/A");
        currentEmail = sharedPref.getString(KEY_EMAIL, "N/A");
        // For Timestamp, retrieve as long (milliseconds). Default to 0 or a sensible value if not found.
        currentLastPasswordChangeMillis = sharedPref.getLong(KEY_LAST_PASSWORD_CHANGE, 0L);
    }


    // Helper method to update all UI elements
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

        // Format the last password change date
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
        inflater.inflate(R.menu.profile_toolbar_menu, menu); // Ensure you're inflating the correct menu
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_edit) {
            // Show the EditProfileBottomSheet when the Edit icon is clicked
            EditProfileBottomSheet bottomSheet = EditProfileBottomSheet.newInstance(
                    currentFirstName,
                    currentLastName,
                    currentUsername,
                    currentEmail
            );
            bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
            return true;
        } else if (id == R.id.action_bookmark) {
            // Start the SavedNewsActivity when Bookmark is clicked
            Intent intent = new Intent(ProfileActivity.this, SavedNewsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_more) { // This is your "Sign out" item in the toolbar menu
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

    // Implementation of the OnProfileEditedListener interface
    @Override
    public void onProfileEdited(String firstName, String lastName, String username, String email) {
        // Update the current profile data in the Activity
        this.currentFirstName = firstName;
        this.currentLastName = lastName;
        this.currentUsername = username;
        this.currentEmail = email;

        // Update the UI
        updateProfileUI();

        // IMPORTANT: You should also save this updated data to Firestore here
        // (and potentially update SharedPreferences if you want the local copy to be immediately current)
        // For example:
        // saveUpdatedProfileToFirestore(firstName, lastName, username, email);

        Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
    }

    // Method to handle user sign out
    private void signOutUser() {
        FirebaseAuth.getInstance().signOut(); // Actual Firebase sign out

        // Clear SharedPreferences
        SharedPreferences sharedPref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear(); // Clears all data
        editor.apply();

        Toast.makeText(ProfileActivity.this, "Logout successful", Toast.LENGTH_SHORT).show();

        // Navigate to LoginActivity and clear back stack
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
        startActivity(intent);
        finish(); // Finish the current activity
    }

    // Optional: Method to save updated profile data to Firestore
    // You would call this from onProfileEdited() after user confirms changes
    /*
    private void saveUpdatedProfileToFirestore(String firstName, String lastName, String username, String email) {
        if (currentUid == null) {
            Toast.makeText(this, "User not logged in or UID missing.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> updates = new HashMap<>();
        updates.put(KEY_FIRST_NAME, firstName);
        updates.put(KEY_LAST_NAME, lastName);
        updates.put(KEY_USERNAME, username);
        updates.put(KEY_EMAIL, email); // Update email if allowed by Firebase Auth
        updates.put("updatedAt", FieldValue.serverTimestamp()); // Update timestamp

        db.collection("users").document(currentUid)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ProfileActivity.this, "Profile saved to server!", Toast.LENGTH_SHORT).show();
                    // If successfully saved to Firestore, update SharedPreferences too
                    SharedPreferences sharedPref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(KEY_FIRST_NAME, firstName);
                    editor.putString(KEY_LAST_NAME, lastName);
                    editor.putString(KEY_USERNAME, username);
                    editor.putString(KEY_EMAIL, email);
                    // Note: currentLastPasswordChangeMillis, createdAt, lastLogin are not updated here
                    editor.apply();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ProfileActivity.this, "Failed to save profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error updating profile in Firestore", e);
                });
    }
    */
}