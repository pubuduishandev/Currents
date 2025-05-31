package com.example.currents.activity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.currents.R;

import java.util.Locale;

public class ReadNewsActivity extends AppCompatActivity {
    private static final String TAG = "ReadNewsActivity";

    private Toolbar toolbar;
    private TextView readNewsTitleTextView;
    private ImageView readNewsImageView;
    private TextView readNewsContentTextView;
    private TextView readNewsPostedDateTextView;

    private MenuItem bookmarkMenuItem;
    private MenuItem readAloudMenuItem;

    private boolean isBookmarked = false;
    private boolean isReadingAloud = false;
    private boolean isTtsInitialized = false; // New flag to track TTS initialization status

    private TextToSpeech textToSpeech;
    private String currentNewsContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_news);

        toolbar = findViewById(R.id.readNewsToolBar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("");
        }

        readNewsTitleTextView = findViewById(R.id.readNewsTitle);
        readNewsImageView = findViewById(R.id.readNewsImage);
        readNewsContentTextView = findViewById(R.id.readNewsContent);
        readNewsPostedDateTextView = findViewById(R.id.readNewsPostedDate);

        Intent intent = getIntent();
        if (intent != null) {
            String title = intent.getStringExtra(HomeActivity.EXTRA_NEWS_TITLE);
            String date = intent.getStringExtra(HomeActivity.EXTRA_NEWS_DATE);
            int imageResId = intent.getIntExtra(HomeActivity.EXTRA_NEWS_IMAGE_RES_ID, 0);
            currentNewsContent = intent.getStringExtra(HomeActivity.EXTRA_NEWS_CONTENT);

            if (title != null) {
                readNewsTitleTextView.setText(title);
            }
            if (date != null) {
                readNewsPostedDateTextView.setText(date);
            }
            if (imageResId != 0) {
                readNewsImageView.setImageResource(imageResId);
            } else {
                readNewsImageView.setImageResource(R.drawable.news_placeholder);
            }
            if (currentNewsContent != null) {
                readNewsContentTextView.setText(currentNewsContent);
            }
        }

        // Initialize TextToSpeech engine
        // The icon will reflect loading state until init completes
        initTextToSpeech();
    }

    private void initTextToSpeech() {
        // Set TTS to an uninitialized state initially
        isTtsInitialized = false;
        // Update icon to a "loading" or "disabled" state immediately
        // This call handles the initial icon state if menu is already inflated
        // or will be called again in onCreateOptionsMenu
        updateReadAloudIcon();

        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.US);

                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "Language not supported or missing data for TTS");
                    // Language not supported, keep TTS as not initialized
                    isTtsInitialized = false;
                    // Update icon to disabled
                    updateReadAloudIcon();
                } else {
                    Log.d(TAG, "TextToSpeech initialized successfully.");
                    isTtsInitialized = true; // TTS is ready
                    updateReadAloudIcon(); // Update icon to enabled state

                    textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String utteranceId) {
                            Log.d(TAG, "Speech started: " + utteranceId);
                        }

                        @Override
                        public void onDone(String utteranceId) {
                            Log.d(TAG, "Speech finished: " + utteranceId);
                            runOnUiThread(() -> {
                                isReadingAloud = false;
                                updateReadAloudIcon();
                            });
                        }

                        @Override
                        public void onError(String utteranceId) {
                            Log.e(TAG, "Speech error: " + utteranceId);
                            runOnUiThread(() -> {
                                isReadingAloud = false;
                                updateReadAloudIcon();
                            });
                        }
                    });
                }
            } else {
                Log.e(TAG, "TextToSpeech initialization failed.");
                isTtsInitialized = false; // TTS failed to initialize
                updateReadAloudIcon(); // Update icon to disabled
            }
        });
    }

    private void startReadingAloud() {
        if (textToSpeech != null && isTtsInitialized && !textToSpeech.isSpeaking() && currentNewsContent != null && !currentNewsContent.isEmpty()) {
            textToSpeech.speak(currentNewsContent, TextToSpeech.QUEUE_FLUSH, null, "newsContent");
            isReadingAloud = true;
            Toast.makeText(this, "Reading news aloud...", Toast.LENGTH_SHORT).show(); // Keep this toast
        } else if (textToSpeech != null && textToSpeech.isSpeaking()) {
            Toast.makeText(this, "Already reading.", Toast.LENGTH_SHORT).show(); // Keep this toast
        } else if (currentNewsContent == null || currentNewsContent.isEmpty()) {
            Toast.makeText(this, "No content to read.", Toast.LENGTH_SHORT).show(); // Keep this toast
        }
        updateReadAloudIcon();
    }

    private void stopReadingAloud() {
        if (textToSpeech != null && textToSpeech.isSpeaking()) {
            textToSpeech.stop();
            isReadingAloud = false;
            Toast.makeText(this, "Reading stopped.", Toast.LENGTH_SHORT).show(); // Keep this toast
        }
        updateReadAloudIcon();
    }

    private void updateReadAloudIcon() {
        if (readAloudMenuItem != null) {
            if (!isTtsInitialized) {
                // Show a 'loading' or 'disabled' state while TTS is not ready
                readAloudMenuItem.setIcon(R.drawable.read_aloud_disable); // Use disabled icon for loading/error
                readAloudMenuItem.setEnabled(false); // Disable clicks until ready
            } else if (isReadingAloud) {
                // TTS is initialized and currently speaking
                readAloudMenuItem.setIcon(R.drawable.read_aloud_disable); // Show stop icon
                readAloudMenuItem.setEnabled(true);
            } else {
                // TTS is initialized but not speaking (ready to play)
                readAloudMenuItem.setIcon(R.drawable.read_aloud_enable); // Show play icon
                readAloudMenuItem.setEnabled(true);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.read_news_toolbar_menu, menu);
        bookmarkMenuItem = menu.findItem(R.id.action_bookmark);
        readAloudMenuItem = menu.findItem(R.id.action_read_aloud);

        // Set initial icon for bookmark
        if (isBookmarked) {
            bookmarkMenuItem.setIcon(R.drawable.bookmark_filled);
        } else {
            bookmarkMenuItem.setIcon(R.drawable.bookmark);
        }

        // Set initial icon for read aloud based on its current state (loading/enabled/disabled)
        updateReadAloudIcon();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_bookmark) {
            isBookmarked = !isBookmarked;
            if (isBookmarked) {
                if (bookmarkMenuItem != null) {
                    bookmarkMenuItem.setIcon(R.drawable.bookmark_filled);
                }
                Toast.makeText(this, "Bookmark added", Toast.LENGTH_SHORT).show();
            } else {
                if (bookmarkMenuItem != null) {
                    bookmarkMenuItem.setIcon(R.drawable.bookmark);
                }
                Toast.makeText(this, "Bookmark removed", Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (id == R.id.action_read_aloud) {
            // Only proceed if TTS is initialized and enabled
            if (isTtsInitialized) {
                if (!textToSpeech.isSpeaking()) {
                    startReadingAloud();
                } else {
                    stopReadingAloud();
                }
            }
            // Removed the "Text-to-speech not ready." toast message
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            Log.d(TAG, "TextToSpeech shut down.");
        }
        super.onDestroy();
    }
}