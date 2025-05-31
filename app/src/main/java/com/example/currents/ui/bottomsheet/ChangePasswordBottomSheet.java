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

public class ChangePasswordBottomSheet extends BottomSheetDialogFragment {

    private TextInputEditText oldPasswordField;
    private TextInputEditText newPasswordField;
    private TextInputEditText confirmPasswordField;
    private Button cancelButton;
    private Button okButton;

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

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss(); // Close the bottom sheet
            }
        });

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Here you would implement your password change logic
                String oldPassword = oldPasswordField.getText().toString();
                String newPassword = newPasswordField.getText().toString();
                String confirmPassword = confirmPasswordField.getText().toString();

                if (newPassword.isEmpty() || confirmPassword.isEmpty() || oldPassword.isEmpty()) {
                    Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                } else if (!newPassword.equals(confirmPassword)) {
                    Toast.makeText(getContext(), "New passwords do not match", Toast.LENGTH_SHORT).show();
                } else {
                    // Password change logic (e.g., call an API, update local data)
                    Toast.makeText(getContext(), "Password change logic here!", Toast.LENGTH_SHORT).show();
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