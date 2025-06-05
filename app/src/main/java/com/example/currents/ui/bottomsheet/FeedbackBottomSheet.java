package com.example.currents.ui.bottomsheet;

import android.content.Context;
import android.content.SharedPreferences; // Import SharedPreferences
import android.os.Bundle;
import android.util.Log; // Import Log
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.currents.R;
import com.google.android.gms.tasks.OnFailureListener; // Import OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener; // Import OnSuccessListener
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth; // Import FirebaseAuth
import com.google.firebase.auth.FirebaseUser; // Import FirebaseUser
import com.google.firebase.firestore.FieldValue; // Import FieldValue for server timestamp
import com.google.firebase.firestore.FirebaseFirestore; // Import FirebaseFirestore
import java.util.HashMap; // Import HashMap
import java.util.Map; // Import Map

public class FeedbackBottomSheet extends BottomSheetDialogFragment {

    private static final String TAG = "FeedbackBottomSheet"; // Define TAG for logging

    private ImageView[] stars;
    private int selectedRating = 0;
    private EditText feedbackEditText;
    private Button sendFeedbackButton;

    // Firebase instances
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // SharedPreferences name and key for User UID (MUST MATCH LoginActivity/ProfileActivity)
    private static final String PREF_NAME = "CurrentUserPrefs";
    private static final String KEY_USER_UID = "user_uid";

    public static FeedbackBottomSheet newInstance() {
        return new FeedbackBottomSheet();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_feedback, container, false);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        stars = new ImageView[5];
        stars[0] = view.findViewById(R.id.star1);
        stars[1] = view.findViewById(R.id.star2);
        stars[2] = view.findViewById(R.id.star3);
        stars[3] = view.findViewById(R.id.star4);
        stars[4] = view.findViewById(R.id.star5);

        feedbackEditText = view.findViewById(R.id.feedbackEditText);
        sendFeedbackButton = view.findViewById(R.id.sendFeedbackButton);

        // Set click listeners for stars
        for (int i = 0; i < stars.length; i++) {
            final int starIndex = i;
            stars[i].setOnClickListener(v -> setRating(starIndex + 1));
        }

        sendFeedbackButton.setOnClickListener(v -> {
            String feedback = feedbackEditText.getText().toString().trim();

            if (selectedRating == 0) {
                Toast.makeText(getContext(), "Please give your rating for the app", Toast.LENGTH_SHORT).show();
            } else if (feedback.isEmpty()) {
                Toast.makeText(getContext(), "Please enter your feedback message", Toast.LENGTH_SHORT).show();
            } else {
                sendFeedbackToFirestore(selectedRating, feedback);
            }
        });

        return view;
    }

    private void setRating(int rating) {
        selectedRating = rating;
        for (int i = 0; i < stars.length; i++) {
            if (i < rating) {
                stars[i].setImageResource(R.drawable.star_filled); // Change to filled star
            } else {
                stars[i].setImageResource(R.drawable.star_outline); // Change to outline star
            }
        }
    }

    private void sendFeedbackToFirestore(int rating, String feedbackMessage) {
        // Get current user UID
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = null;

        if (currentUser != null) {
            userId = currentUser.getUid();
            Log.d(TAG, "User UID from FirebaseAuth: " + userId);
        } else {
            // Fallback: If FirebaseAuth.getCurrentUser() is null, try SharedPreferences
            SharedPreferences sharedPref = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            userId = sharedPref.getString(KEY_USER_UID, null);
            Log.d(TAG, "User UID from SharedPreferences: " + (userId != null ? userId : "null"));
        }

        if (userId == null) {
            Toast.makeText(getContext(), "Could not identify user. Please try logging in again.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Failed to get user UID for feedback submission.");
            return; // Exit if no user ID is found
        }

        // Create a new feedback map
        Map<String, Object> feedbackData = new HashMap<>();
        feedbackData.put("ratings", rating);
        feedbackData.put("feedback", feedbackMessage);
        feedbackData.put("createdBy", userId);
        feedbackData.put("createdAt", FieldValue.serverTimestamp()); // Use server timestamp for consistency

        // Disable button to prevent multiple submissions
        sendFeedbackButton.setEnabled(false);
        sendFeedbackButton.setText("Sending...");

        // Add feedback to "feedbacks" collection
        db.collection("feedbacks")
                .add(feedbackData)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Feedback successfully written with ID: " + documentReference.getId());
                    Toast.makeText(getContext(), "Thanks for your feedback!", Toast.LENGTH_LONG).show();
                    dismiss(); // Close the bottom sheet on success
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding feedback", e);
                    Toast.makeText(getContext(), "Failed to send feedback. Please try again.", Toast.LENGTH_LONG).show();
                    // Re-enable button on failure
                    sendFeedbackButton.setEnabled(true);
                    sendFeedbackButton.setText("Send Feedback");
                });
    }
}