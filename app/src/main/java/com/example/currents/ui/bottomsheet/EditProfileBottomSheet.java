package com.example.currents.ui.bottomsheet; // Adjust package name

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.example.currents.R; // Make sure this R points to your project's R file

public class EditProfileBottomSheet extends BottomSheetDialogFragment {

    private TextInputEditText firstNameField;
    private TextInputEditText lastNameField;
    private TextInputEditText usernameField;
    private TextInputEditText emailField;
    private Button cancelButton;
    private Button okButton;

    // Interface to communicate changes back to ProfileActivity
    public interface OnProfileEditedListener {
        void onProfileEdited(String firstName, String lastName, String username, String email);
    }

    private OnProfileEditedListener listener;

    public static EditProfileBottomSheet newInstance(String currentFirstName, String currentLastName, String currentUsername, String currentEmail) {
        EditProfileBottomSheet fragment = new EditProfileBottomSheet();
        Bundle args = new Bundle();
        args.putString("firstName", currentFirstName);
        args.putString("lastName", currentLastName);
        args.putString("username", currentUsername);
        args.putString("email", currentEmail);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Ensure the hosting activity implements the listener
        if (getParentFragment() instanceof OnProfileEditedListener) {
            listener = (OnProfileEditedListener) getParentFragment();
        } else if (getActivity() instanceof OnProfileEditedListener) {
            listener = (OnProfileEditedListener) getActivity();
        } else {
            // Optional: throw an exception if the listener is not implemented
            // throw new RuntimeException(context.toString() + " must implement OnProfileEditedListener");
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

        // Populate fields with current data from arguments
        if (getArguments() != null) {
            firstNameField.setText(getArguments().getString("firstName"));
            lastNameField.setText(getArguments().getString("lastName"));
            usernameField.setText(getArguments().getString("username"));
            emailField.setText(getArguments().getString("email"));
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
                String newEmail = emailField.getText().toString().trim();

                if (newFirstName.isEmpty() || newLastName.isEmpty() || newUsername.isEmpty() || newEmail.isEmpty()) {
                    Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                } else {
                    if (listener != null) {
                        listener.onProfileEdited(newFirstName, newLastName, newUsername, newEmail);
                    }
                    dismiss(); // Close the bottom sheet
                }
            }
        });

        return view;
    }
}