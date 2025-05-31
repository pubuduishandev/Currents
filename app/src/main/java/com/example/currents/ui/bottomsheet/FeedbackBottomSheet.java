package com.example.currents.ui.bottomsheet;

import android.os.Bundle;
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
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class FeedbackBottomSheet extends BottomSheetDialogFragment {

    private ImageView[] stars;
    private int selectedRating = 0;
    private EditText feedbackEditText;
    private Button sendFeedbackButton;

    public static FeedbackBottomSheet newInstance() {
        return new FeedbackBottomSheet();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_feedback, container, false);

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
                Toast.makeText(getContext(), "Please select a star rating.", Toast.LENGTH_SHORT).show();
            } else if (feedback.isEmpty()) {
                Toast.makeText(getContext(), "Please enter your feedback.", Toast.LENGTH_SHORT).show();
            } else {
                // Here you would typically send the feedback and rating to your backend or analytics
                Toast.makeText(getContext(), "Feedback sent! Rating: " + selectedRating + " stars. Feedback: " + feedback, Toast.LENGTH_LONG).show();
                dismiss(); // Close the bottom sheet
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
}