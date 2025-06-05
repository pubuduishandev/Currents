package com.example.currents.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.currents.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue; // Import for server timestamp
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot; // For querying
import com.google.firebase.firestore.QuerySnapshot; // For querying

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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
    private boolean isTtsInitialized = false;

    private TextToSpeech textToSpeech;

    private String currentNewsContent;
    private String currentNewsTitle;
    private String currentNewsDate;
    private int currentNewsImageResId;
    private String currentArticleId; // New: To store the article's Firestore document ID

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private CollectionReference bookmarksRef;
    private String currentUserId; // To store the logged-in user's ID
    private String foundBookmarkDocId = null; // To store the specific bookmark document ID if found

    // SharedPreferences name and key for User UID (MUST MATCH LoginActivity/ProfileActivity)
    private static final String PREF_NAME = "CurrentUserPrefs";
    private static final String KEY_USER_UID = "user_uid";

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
            currentNewsTitle = intent.getStringExtra(HomeActivity.EXTRA_NEWS_TITLE);
            currentNewsDate = intent.getStringExtra(HomeActivity.EXTRA_NEWS_DATE);
            currentNewsImageResId = intent.getIntExtra(HomeActivity.EXTRA_NEWS_IMAGE_RES_ID, 0);
            currentNewsContent = intent.getStringExtra(HomeActivity.EXTRA_NEWS_CONTENT);
            currentArticleId = intent.getStringExtra(HomeActivity.EXTRA_ARTICLE_ID); // Retrieve the article ID

            if (currentNewsTitle != null) {
                readNewsTitleTextView.setText(currentNewsTitle);
            }
            if (currentNewsDate != null) {
                readNewsPostedDateTextView.setText(currentNewsDate);
            }
            if (currentNewsImageResId != 0) {
                readNewsImageView.setImageResource(currentNewsImageResId);
            } else {
                readNewsImageView.setImageResource(R.drawable.news_placeholder);
            }
            if (currentNewsContent != null) {
                readNewsContentTextView.setText(currentNewsContent);
            }
        }

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance(); // Initialize FirebaseAuth
        bookmarksRef = db.collection("bookmarks");

        // Get current user ID (prioritize Firebase Auth, then SharedPreferences)
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            Log.d(TAG, "Current user ID from Auth: " + currentUserId);
        } else {
            SharedPreferences sharedPref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            currentUserId = sharedPref.getString(KEY_USER_UID, null);
            if (currentUserId != null) {
                Log.d(TAG, "Current user ID from SharedPreferences: " + currentUserId);
            } else {
                Log.e(TAG, "No current user ID found. Bookmarking features may be limited.");
            }
        }

        // Check bookmark status on activity creation, only if user and article ID are available
        if (currentUserId != null && currentArticleId != null) {
            checkBookmarkStatus();
        } else {
            isBookmarked = false; // Default to not bookmarked if essential info is missing
            invalidateOptionsMenu(); // Update UI
            if (currentArticleId == null) {
                Log.e(TAG, "currentArticleId is null. Cannot perform bookmark operations.");
            }
        }

        // Initialize TextToSpeech engine
        initTextToSpeech();
    }

    private void checkBookmarkStatus() {
        if (currentUserId == null || currentArticleId == null || currentArticleId.isEmpty()) {
            isBookmarked = false;
            foundBookmarkDocId = null;
            invalidateOptionsMenu();
            Log.w(TAG, "Cannot check bookmark status: User ID or Article ID is missing.");
            return;
        }

        bookmarksRef.whereEqualTo("userId", currentUserId)
                .whereEqualTo("articleId", currentArticleId) // Query by article ID
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (!task.getResult().isEmpty()) {
                                // Found a bookmark for this user and article
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    foundBookmarkDocId = document.getId(); // Store the actual document ID
                                    isBookmarked = true;
                                    Log.d(TAG, "News is bookmarked. Bookmark Doc ID: " + foundBookmarkDocId);
                                    break; // Assuming only one bookmark per user per article
                                }
                            } else {
                                isBookmarked = false;
                                foundBookmarkDocId = null;
                                Log.d(TAG, "News is NOT bookmarked.");
                            }
                            // Update the UI after checking bookmark status
                            invalidateOptionsMenu(); // Recreate the menu to update the icon
                        } else {
                            Log.e(TAG, "Error getting bookmark documents: ", task.getException());
                            // Default to not bookmarked on error
                            isBookmarked = false;
                            foundBookmarkDocId = null;
                            invalidateOptionsMenu(); // Recreate the menu
                        }
                    }
                });
    }

    private void toggleBookmark() {
        if (currentUserId == null) {
            Toast.makeText(this, "Please log in to bookmark news.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentArticleId == null || currentArticleId.isEmpty()) {
            Toast.makeText(this, "Cannot bookmark: News article ID is missing.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentNewsTitle == null || currentNewsTitle.isEmpty()) {
            Toast.makeText(this, "Cannot bookmark: News title is missing.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isBookmarked && foundBookmarkDocId != null) {
            // Remove bookmark using the stored document ID
            bookmarksRef.document(foundBookmarkDocId)
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            isBookmarked = false;
                            foundBookmarkDocId = null; // Clear the stored ID
                            updateBookmarkIcon();
                            Toast.makeText(ReadNewsActivity.this, "Bookmark removed", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Bookmark successfully deleted for article: " + currentArticleId);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Error deleting bookmark", e);
                            Toast.makeText(ReadNewsActivity.this, "Failed to remove bookmark.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // Add bookmark
            Map<String, Object> bookmark = new HashMap<>();
            bookmark.put("userId", currentUserId);
            bookmark.put("articleId", currentArticleId); // Use the retrieved articleId
            bookmark.put("createdAt", FieldValue.serverTimestamp()); // Server timestamp

            bookmarksRef.add(bookmark) // Use .add() for auto-generated document ID (bookmarkId)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            isBookmarked = true;
                            foundBookmarkDocId = documentReference.getId(); // Store the newly generated bookmarkId
                            updateBookmarkIcon();
                            Toast.makeText(ReadNewsActivity.this, "Bookmark added", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Bookmark successfully added with ID: " + foundBookmarkDocId);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Error adding bookmark", e);
                            Toast.makeText(ReadNewsActivity.this, "Failed to add bookmark.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void updateBookmarkIcon() {
        if (bookmarkMenuItem != null) {
            if (isBookmarked) {
                bookmarkMenuItem.setIcon(R.drawable.bookmark_filled);
            } else {
                bookmarkMenuItem.setIcon(R.drawable.bookmark);
            }
            // Disable bookmarking if no user is logged in or article ID is missing
            bookmarkMenuItem.setEnabled(currentUserId != null && currentArticleId != null);
        }
    }

    private void initTextToSpeech() {
        isTtsInitialized = false;
        updateReadAloudIcon();

        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.US);

                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "Language not supported or missing data for TTS");
                    isTtsInitialized = false;
                    updateReadAloudIcon();
                } else {
                    Log.d(TAG, "TextToSpeech initialized successfully.");
                    isTtsInitialized = true;
                    updateReadAloudIcon();

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
                isTtsInitialized = false;
                updateReadAloudIcon();
            }
        });
    }

    private void startReadingAloud() {
        if (textToSpeech != null && isTtsInitialized && !textToSpeech.isSpeaking() && currentNewsContent != null && !currentNewsContent.isEmpty()) {
            textToSpeech.speak(currentNewsContent, TextToSpeech.QUEUE_FLUSH, null, "newsContent");
            isReadingAloud = true;
            Toast.makeText(this, "Reading news aloud...", Toast.LENGTH_SHORT).show();
        } else if (textToSpeech != null && textToSpeech.isSpeaking()) {
            Toast.makeText(this, "Already reading.", Toast.LENGTH_SHORT).show();
        } else if (currentNewsContent == null || currentNewsContent.isEmpty()) {
            Toast.makeText(this, "No content to read.", Toast.LENGTH_SHORT).show();
        }
        updateReadAloudIcon();
    }

    private void stopReadingAloud() {
        if (textToSpeech != null && textToSpeech.isSpeaking()) {
            textToSpeech.stop();
            isReadingAloud = false;
            Toast.makeText(this, "Reading stopped.", Toast.LENGTH_SHORT).show();
        }
        updateReadAloudIcon();
    }

    private void updateReadAloudIcon() {
        if (readAloudMenuItem != null) {
            if (!isTtsInitialized) {
                readAloudMenuItem.setIcon(R.drawable.read_aloud_disable);
                readAloudMenuItem.setEnabled(false);
            } else if (isReadingAloud) {
                readAloudMenuItem.setIcon(R.drawable.read_aloud_disable);
                readAloudMenuItem.setEnabled(true);
            } else {
                readAloudMenuItem.setIcon(R.drawable.read_aloud_enable);
                readAloudMenuItem.setEnabled(true);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.read_news_toolbar_menu, menu);
        bookmarkMenuItem = menu.findItem(R.id.action_bookmark);
        readAloudMenuItem = menu.findItem(R.id.action_read_aloud);

        updateBookmarkIcon();
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
            toggleBookmark();
            return true;
        } else if (id == R.id.action_read_aloud) {
            if (isTtsInitialized) {
                if (!textToSpeech.isSpeaking()) {
                    startReadingAloud();
                } else {
                    stopReadingAloud();
                }
            }
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