package com.example.currents.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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

        emailCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendEmail("mailto:2020t00876@stu.cmb.ac.lk");
            }
        });

        gitHubCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                visitGitHub("https://github.com/pubuduishandev");
            }
        });

        linkedInCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                visitLinkedIn("https://www.linkedin.com/in/pubuduishan/");
            }
        });
    }

    void sendEmail(String s) {
        try{
            Uri uri = Uri.parse(s);
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Email not found", Toast.LENGTH_SHORT).show();
        }
    }

    void visitGitHub(String s) {
        try{
            Uri uri = Uri.parse(s);
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "GitHub not found", Toast.LENGTH_SHORT).show();
        }
    }

    void visitLinkedIn(String s) {
        try{
            Uri uri = Uri.parse(s);
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "LinkedIn not found", Toast.LENGTH_SHORT).show();
        }
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

        if (id == R.id.action_favorite) {
            // Show the FeedbackBottomSheet when favorite icon is clicked
            FeedbackBottomSheet feedbackBottomSheet = FeedbackBottomSheet.newInstance();
            feedbackBottomSheet.show(getSupportFragmentManager(), feedbackBottomSheet.getTag());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}