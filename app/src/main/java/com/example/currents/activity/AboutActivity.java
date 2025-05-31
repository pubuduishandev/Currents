package com.example.currents.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView; // Import ImageView if not already
import android.widget.TextView; // Import TextView if not already
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.example.currents.R;
import com.example.currents.ui.bottomsheet.FeedbackBottomSheet; // Import the new bottom sheet

public class AboutActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private CardView emailCard;
    private CardView gitHubCard; // Corrected ID from githubCard to gitHubCard as per XML
    private CardView linkedInCard; // Corrected ID from linkedinCard to linkedInCard as per XML

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        toolbar = findViewById(R.id.aboutToolBar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Initialize CardViews for click listeners
        emailCard = findViewById(R.id.emailCard);
        gitHubCard = findViewById(R.id.gitHubCard); // Use the correct ID from XML
        linkedInCard = findViewById(R.id.linkedInCard); // Use the correct ID from XML


        // Set click listeners for interactive cards
        emailCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView emailValue = findViewById(R.id.emailValue); // Get the TextView
                String email = emailValue.getText().toString(); // Get email from the TextView
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
                intent.putExtra(Intent.EXTRA_SUBJECT, "Inquiry from Currents App");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(AboutActivity.this, "No email app found.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        gitHubCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView githubValue = findViewById(R.id.gitHubValue);
                String username = githubValue.getText().toString();
                String url = "https://github.com/" + username;
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(AboutActivity.this, "No browser found.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        linkedInCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView linkedinValue = findViewById(R.id.linkedInValue);
                String profile = linkedinValue.getText().toString();
                String url = "https://www.linkedin.com/in/" + profile;
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(AboutActivity.this, "No browser found.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.about_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_favorite) {
            // Show the FeedbackBottomSheet when favorite icon is clicked
            FeedbackBottomSheet feedbackBottomSheet = FeedbackBottomSheet.newInstance();
            feedbackBottomSheet.show(getSupportFragmentManager(), feedbackBottomSheet.getTag());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}