package com.example.currents.ui.bottomsheet; // Adjust package name

import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import android.text.TextWatcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout; // Import TextInputLayout
import com.example.currents.R; // Make sure this R points to your project's R file

public class ChangePasswordBottomSheet extends BottomSheetDialogFragment {

    private TextInputEditText oldPasswordField;
    private TextInputEditText newPasswordField;
    private TextInputEditText confirmPasswordField;
    private Button cancelButton;
    private Button okButton;

    // Add TextInputLayout references
    private TextInputLayout oldPasswordLayout;
    private TextInputLayout newPasswordLayout;
    private TextInputLayout confirmPasswordLayout;

    public static ChangePasswordBottomSheet newInstance() {
        return new ChangePasswordBottomSheet();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_change_password, container, false);

        oldPasswordField = view.findViewById(R.id.oldPasswordField);
        newPasswordField = view.findViewById(R.id.newPasswordField);
        confirmPasswordField = view.findViewById(R.id.confirmPasswordField);
        cancelButton = view.findViewById(R.id.cancelButton);
        okButton = view.findViewById(R.id.okButton);

        // Initialize TextInputLayout references
        oldPasswordLayout = view.findViewById(R.id.oldPasswordLayout);
        newPasswordLayout = view.findViewById(R.id.newPasswordLayout);
        confirmPasswordLayout = view.findViewById(R.id.confirmPasswordLayout);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss(); // Close the bottom sheet
            }
        });

        oldPasswordField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Clear error on the TextInputLayout
                if (oldPasswordLayout.isErrorEnabled()) {
                    oldPasswordLayout.setError(null);
                    oldPasswordLayout.setErrorEnabled(false);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        newPasswordField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Clear error on the TextInputLayout
                if (newPasswordLayout.isErrorEnabled()) {
                    newPasswordLayout.setError(null);
                    newPasswordLayout.setErrorEnabled(false);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        confirmPasswordField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Clear error on the TextInputLayout
                if (confirmPasswordLayout.isErrorEnabled()) {
                    confirmPasswordLayout.setError(null);
                    confirmPasswordLayout.setErrorEnabled(false);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Here you would implement your password change logic
                String oldPassword = oldPasswordField.getText().toString();
                String newPassword = newPasswordField.getText().toString();
                String confirmPassword = confirmPasswordField.getText().toString();

                boolean hasError = false;

                if (oldPassword.isEmpty()) {
                    oldPasswordLayout.setError("Old password cannot be empty");
                    oldPasswordLayout.setErrorEnabled(true);
                    hasError = true;
                }
                if (newPassword.isEmpty()) {
                    newPasswordLayout.setError("New password cannot be empty");
                    newPasswordLayout.setErrorEnabled(true);
                    hasError = true;
                }
                if (confirmPassword.isEmpty()) {
                    confirmPasswordLayout.setError("Confirm password cannot be empty");
                    confirmPasswordLayout.setErrorEnabled(true);
                    hasError = true;
                }

                if (hasError) {
                    Toast.makeText(getContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
                } else if (!newPassword.equals(confirmPassword)) {
                    confirmPasswordLayout.setError("New passwords do not match");
                    confirmPasswordLayout.setErrorEnabled(true);
                    Toast.makeText(getContext(), "New passwords do not match", Toast.LENGTH_SHORT).show();
                } else {
                    // Password change logic (e.g., call an API, update local data)
                    Toast.makeText(getContext(), "Password changed", Toast.LENGTH_SHORT).show();
                    dismiss(); // Close the bottom sheet after successful operation
                }
            }
        });

        return view;
    }

    // Optional: Customize the bottom sheet behavior (e.g., peek height, state)
    // @Override
    // public void onStart() {
    //     super.onStart();
    //     BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
    //     if (dialog != null) {
    //         View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
    //         if (bottomSheet != null) {
    //             BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
    //             behavior.setState(BottomSheetBehavior.STATE_EXPANDED); // Make it fully expanded by default
    //         }
    //     }
    // }
}