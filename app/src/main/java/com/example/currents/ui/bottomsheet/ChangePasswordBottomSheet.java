package com.example.currents.ui.bottomsheet;

import android.os.Bundle;
import android.text.Editable;
import android.util.Log; // Import Log
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView; // Import TextView for instructions
import android.widget.Toast;
import android.text.TextWatcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.example.currents.R;

public class ChangePasswordBottomSheet extends BottomSheetDialogFragment {

    private TextInputEditText passwordField; // Renamed from oldPasswordField
    private Button cancelButton;
    private Button okButton;
    private TextView instructionTextView; // New TextView for instructions

    // Add TextInputLayout references
    private TextInputLayout passwordLayout; // Renamed from oldPasswordLayout

    public static ChangePasswordBottomSheet newInstance() {
        return new ChangePasswordBottomSheet();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_change_password, container, false);

        passwordField = view.findViewById(R.id.passwordField); // Use new ID
        cancelButton = view.findViewById(R.id.cancelButton);
        okButton = view.findViewById(R.id.okButton);
        instructionTextView = view.findViewById(R.id.instructionTextView); // Initialize instruction TextView

        // Initialize TextInputLayout reference
        passwordLayout = view.findViewById(R.id.passwordLayout); // Use new ID

        // Set instruction text
        instructionTextView.setText(R.string.change_password_instructions);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss(); // Close the bottom sheet
            }
        });

        passwordField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Clear error on the TextInputLayout
                if (passwordLayout.isErrorEnabled()) {
                    passwordLayout.setError(null);
                    passwordLayout.setErrorEnabled(false);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentPassword = passwordField.getText().toString().trim();

                if (currentPassword.isEmpty()) {
                    passwordLayout.setError("Password cannot be empty");
                    passwordLayout.setErrorEnabled(true);
                    Toast.makeText(getContext(), "Please enter your password", Toast.LENGTH_SHORT).show();
                    return; // Stop execution if password is empty
                }

                // Proceed with Firebase re-authentication and then send password reset email
                reauthenticateAndSendPasswordResetEmail(currentPassword);
            }
        });

        return view;
    }

    private void reauthenticateAndSendPasswordResetEmail(String password) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null || user.getEmail() == null) {
            Toast.makeText(getContext(), "Error: User not logged in or email not found.", Toast.LENGTH_LONG).show();
            dismiss(); // Close bottom sheet if user is not valid
            return;
        }

        // Create a credential with the user's email and password
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);

        // Re-authenticate the user
        user.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("ChangePasswordBS", "User re-authenticated successfully.");
                        // Re-authentication successful, now send password reset email
                        sendPasswordResetEmail(user.getEmail());
                    } else {
                        String errorMessage = "Re-authentication failed: " + task.getException().getMessage();
                        Log.e("ChangePasswordBS", errorMessage);
                        Toast.makeText(getContext(), "Password verification failed. " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void sendPasswordResetEmail(String email) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("ChangePasswordBS", "Password reset email sent successfully to " + email);
                        Toast.makeText(getContext(), "Password reset email sent to " + email + ". Please check your inbox.", Toast.LENGTH_LONG).show();
                        dismiss(); // Close the bottom sheet after sending email
                    } else {
                        String errorMessage = "Failed to send password reset email: " + task.getException().getMessage();
                        Log.e("ChangePasswordBS", errorMessage);
                        Toast.makeText(getContext(), "Failed to send password reset email: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}