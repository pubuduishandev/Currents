package com.example.currents.ui.bottomsheet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView; // Import TextView
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout; // Import TextInputLayout
import com.example.currents.R;

public class EditProfileBottomSheet extends BottomSheetDialogFragment {

    private TextInputEditText firstNameField;
    private TextInputEditText lastNameField;
    private TextInputEditText usernameField;
    private TextInputEditText emailField;
    private Button cancelButton;
    private Button okButton;
    private TextView emailWarningTextView; // New TextView for the warning message
    private TextInputLayout emailLayout; // To access the parent layout of emailField

    // Modified Interface: Email is no longer edited here, so remove it from the listener
    public interface OnProfileEditedListener {
        void onProfileEdited(String newFirstName, String newLastName, String newUsername);
    }

    private OnProfileEditedListener listener;

    public static EditProfileBottomSheet newInstance(String currentFirstName, String currentLastName, String currentUsername, String currentEmail) {
        EditProfileBottomSheet fragment = new EditProfileBottomSheet();
        Bundle args = new Bundle();
        args.putString("firstName", currentFirstName);
        args.putString("lastName", currentLastName);
        args.putString("username", currentUsername);
        args.putString("email", currentEmail); // Still pass email to display it
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getParentFragment() instanceof OnProfileEditedListener) {
            listener = (OnProfileEditedListener) getParentFragment();
        } else if (getActivity() instanceof OnProfileEditedListener) {
            listener = (OnProfileEditedListener) getActivity();
        } else {
            // Consider throwing an exception here if the listener contract is strict
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_edit_profile, container, false);

        firstNameField = view.findViewById(R.id.firstNameField);
        lastNameField = view.findViewById(R.id.lastNameField);
        usernameField = view.findViewById(R.id.usernameField);
        emailField = view.findViewById(R.id.emailField);
        cancelButton = view.findViewById(R.id.cancelButton);
        okButton = view.findViewById(R.id.okButton);
        emailWarningTextView = view.findViewById(R.id.emailWarningTextView); // Initialize the new TextView
        emailLayout = view.findViewById(R.id.emailLayout); // Initialize TextInputLayout

        // Populate fields with current data from arguments
        if (getArguments() != null) {
            firstNameField.setText(getArguments().getString("firstName"));
            lastNameField.setText(getArguments().getString("lastName"));
            usernameField.setText(getArguments().getString("username"));
            emailField.setText(getArguments().getString("email"));

            // --- Make Email Field Read-Only and show message ---
            emailField.setFocusable(false); // Prevents direct focus
            emailField.setFocusableInTouchMode(false); // Prevents focus on touch
            emailField.setCursorVisible(false); // Hides the cursor
            emailField.setKeyListener(null); // Prevents input from soft keyboard
            emailField.setBackground(null); // Optional: Remove background to make it look less like an input field

            // Optional: Change text color to indicate it's not editable
            emailField.setTextColor(getResources().getColor(R.color.text_secondary, null));

            // Show the warning message
            emailWarningTextView.setVisibility(View.VISIBLE);
            // If you want to disable the input layout completely, uncomment next line:
            // emailLayout.setEnabled(false); // Disables the whole TextInputLayout
        }

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss(); // Close the bottom sheet
            }
        });

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newFirstName = firstNameField.getText().toString().trim();
                String newLastName = lastNameField.getText().toString().trim();
                String newUsername = usernameField.getText().toString().trim();
                // No need to get newEmail as it's not editable

                if (newFirstName.isEmpty() || newLastName.isEmpty() || newUsername.isEmpty()) {
                    Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                } else {
                    if (listener != null) {
                        listener.onProfileEdited(newFirstName, newLastName, newUsername); // Only pass editable fields
                    }
                    dismiss(); // Close the bottom sheet
                }
            }
        });

        return view;
    }
}