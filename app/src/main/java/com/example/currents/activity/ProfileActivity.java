package com.example.currents.activity; // Adjust package name

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.example.currents.R;
import com.example.currents.ui.bottomsheet.ChangePasswordBottomSheet;
import com.example.currents.ui.bottomsheet.EditProfileBottomSheet; // Import your new BottomSheet

public class ProfileActivity extends AppCompatActivity implements EditProfileBottomSheet.OnProfileEditedListener {
    // Implement the interface

    private Toolbar toolbar;
    private TextView avatarInitialsTextView;
    private TextView userNameTextView;
    private TextView fullNameValue;
    private TextView usernameValue;
    private TextView emailValue;
    private TextView passwordValue;
    private CardView passwordCard;

    // Store the current profile data
    private String currentFirstName = "Pubudu";
    private String currentLastName = "Ishan";
    private String currentUsername = "pubuduishan";
    private String currentEmail = "2020t00876@stu.cmb.ac.lk";
    private String currentPasswordStatus = "Changed one month ago";


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

        // Set initial data
        updateProfileUI();


        // Set click listener for the passwordCard
        passwordCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangePasswordBottomSheet bottomSheet = ChangePasswordBottomSheet.newInstance();
                bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
            }
        });
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
        userNameTextView.setText(currentFirstName + " " + currentLastName);
        fullNameValue.setText(currentFirstName + " " + currentLastName);
        usernameValue.setText(currentUsername);
        emailValue.setText(currentEmail);
        passwordValue.setText(currentPasswordStatus);
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
        } else if (id == R.id.action_more) { // This is your "Sign out" item
            // Navigate to LoginActivity and clear back stack
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
            startActivity(intent);
            Toast.makeText(ProfileActivity.this, "Logout successful", Toast.LENGTH_SHORT).show();
            finish(); // Finish the current activity
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

        Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
        // Here you would typically save the updated data to your backend or local storage
    }
}