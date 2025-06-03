package com.example.currents.ui.bottomsheet;

import android.os.Bundle;
import android.text.format.DateUtils; // Import for DateUtils
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.example.currents.R;

public class EditProfileBottomSheet extends BottomSheetDialogFragment {

    private TextInputEditText firstNameField;
    private TextInputEditText lastNameField;
    private TextInputEditText usernameField;
    private TextInputEditText emailField;
    private Button cancelButton;
    private Button okButton;
    private TextView emailWarningTextView;
    private TextInputLayout emailLayout;
    private TextView lastUpdatedTextView; // NEW: TextView for "Last Updated"

    public interface OnProfileEditedListener {
        void onProfileEdited(String newFirstName, String newLastName, String newUsername);
    }

    private OnProfileEditedListener listener;

    // Modified newInstance to accept currentUpdatedAtMillis
    public static EditProfileBottomSheet newInstance(String currentFirstName, String currentLastName, String currentUsername, String currentEmail, long currentUpdatedAtMillis) {
        EditProfileBottomSheet fragment = new EditProfileBottomSheet();
        Bundle args = new Bundle();
        args.putString("firstName", currentFirstName);
        args.putString("lastName", currentLastName);
        args.putString("username", currentUsername);
        args.putString("email", currentEmail);
        args.putLong("updatedAtMillis", currentUpdatedAtMillis); // NEW: Pass updatedAtMillis
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
        emailWarningTextView = view.findViewById(R.id.emailWarningTextView);
        emailLayout = view.findViewById(R.id.emailLayout);
        lastUpdatedTextView = view.findViewById(R.id.lastUpdatedTextView); // NEW: Initialize Last Updated TextView

        if (getArguments() != null) {
            firstNameField.setText(getArguments().getString("firstName"));
            lastNameField.setText(getArguments().getString("lastName"));
            usernameField.setText(getArguments().getString("username"));
            emailField.setText(getArguments().getString("email"));

            // Get and display updatedAtMillis
            long updatedAtMillis = getArguments().getLong("updatedAtMillis", 0L);
            if (updatedAtMillis > 0) {
                String timeAgo = DateUtils.getRelativeTimeSpanString(
                        updatedAtMillis,
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS,
                        DateUtils.FORMAT_ABBREV_RELATIVE
                ).toString();
                lastUpdatedTextView.setText("Last updated: " + timeAgo);
                lastUpdatedTextView.setVisibility(View.VISIBLE); // Make sure it's visible
            } else {
                lastUpdatedTextView.setText("Last updated: N/A");
                lastUpdatedTextView.setVisibility(View.VISIBLE); // Show N/A if no timestamp
            }

            emailField.setFocusable(false);
            emailField.setFocusableInTouchMode(false);
            emailField.setCursorVisible(false);
            emailField.setKeyListener(null);
            emailField.setBackground(null);
            emailField.setTextColor(getResources().getColor(R.color.text_secondary, null));
            emailWarningTextView.setVisibility(View.VISIBLE);
        }

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newFirstName = firstNameField.getText().toString().trim();
                String newLastName = lastNameField.getText().toString().trim();
                String newUsername = usernameField.getText().toString().trim();

                if (newFirstName.isEmpty() || newLastName.isEmpty() || newUsername.isEmpty()) {
                    Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                } else {
                    if (listener != null) {
                        listener.onProfileEdited(newFirstName, newLastName, newUsername);
                    }
                    dismiss();
                }
            }
        });

        return view;
    }
}