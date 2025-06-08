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

import com.bumptech.glide.Glide;

import com.example.currents.R;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ReadNewsActivity extends AppCompatActivity {
    // Tag for logging
    private static final String TAG = "ReadNewsActivity";

    // UI Components
    private Toolbar toolbar;
    private TextView readNewsTitleTextView;
    private ImageView readNewsImageView;
    private TextView readNewsContentTextView;
    private TextView readNewsPostedDateTextView;

    // Menu Items
    private MenuItem bookmarkMenuItem;
    private MenuItem readAloudMenuItem;

    // State Variables
    private boolean isBookmarked = false;
    private boolean isReadingAloud = false;
    private boolean isTtsInitialized = false;

    // Text-to-Speech
    private TextToSpeech textToSpeech;

    // Current News Data
    private String currentNewsContent;
    private String currentNewsTitle;
    private String currentNewsDate;
    private int currentNewsImageResId; // Kept for local fallback, though imageUrl is preferred
    private String currentNewsImageUrl; // NEW: To store the URL from Firebase Storage
    private String currentArticleId;

    // Firebase Components
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private CollectionReference bookmarksRef;
    private String currentUserId;
    private String foundBookmarkDocId = null;
    private ListenerRegistration articleListenerRegistration;

    // Shared Preferences for storing current user ID
    private static final String PREF_NAME = "CurrentUserPrefs";
    private static final String KEY_USER_UID = "user_uid";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Call the superclass method
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_news);

        // Initialize the toolbar
        toolbar = findViewById(R.id.readNewsToolBar);
        setSupportActionBar(toolbar);

        // Enable the Up button in the toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("");
        }

        // Initialize UI components
        readNewsTitleTextView = findViewById(R.id.readNewsTitle);
        readNewsImageView = findViewById(R.id.readNewsImage);
        readNewsContentTextView = findViewById(R.id.readNewsContent);
        readNewsPostedDateTextView = findViewById(R.id.readNewsPostedDate);

        // Initialize Firebase components
        Intent intent = getIntent();
        if (intent != null) {
            currentNewsTitle = intent.getStringExtra(HomeActivity.EXTRA_NEWS_TITLE);
            currentNewsDate = intent.getStringExtra(HomeActivity.EXTRA_NEWS_DATE);
            currentNewsImageResId = intent.getIntExtra(HomeActivity.EXTRA_NEWS_IMAGE_RES_ID, 0);
            currentNewsImageUrl = intent.getStringExtra(HomeActivity.EXTRA_NEWS_IMAGE_URL); // NEW: Get image URL
            currentNewsContent = intent.getStringExtra(HomeActivity.EXTRA_NEWS_CONTENT);
            currentArticleId = intent.getStringExtra(HomeActivity.EXTRA_ARTICLE_ID);

            // Set initial data from intent (will be overwritten by real-time updates)
            if (currentNewsTitle != null) {
                readNewsTitleTextView.setText(currentNewsTitle);
            }
            if (currentNewsDate != null) {
                readNewsPostedDateTextView.setText(currentNewsDate);
            }

            // --- Initial Image Loading with Glide (from Intent) ---
            loadImageWithGlide(currentNewsImageUrl, currentNewsImageResId);
            // --- End Initial Image Loading ---

            if (currentNewsContent != null) {
                readNewsContentTextView.setText(currentNewsContent);
            }
        }

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        bookmarksRef = db.collection("bookmarks");

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

        if (currentUserId != null && currentArticleId != null) {
            checkBookmarkStatus();
        } else {
            isBookmarked = false;
            invalidateOptionsMenu();
            if (currentArticleId == null) {
                Log.e(TAG, "currentArticleId is null. Cannot perform bookmark operations or listen for article changes.");
            }
        }

        // Initialize Text-to-Speech
        initTextToSpeech();
    }

    // Lifecycle methods for starting and stopping the article real-time listener
    @Override
    protected void onStart() {
        super.onStart();
        startArticleRealtimeListener();
    }

    // Lifecycle method to stop the listener when the activity is no longer visible
    @Override
    protected void onStop() {
        super.onStop();
        stopArticleRealtimeListener();
    }

    // Method to start listening for real-time updates on the article
    private void startArticleRealtimeListener() {
        if (currentArticleId == null || currentArticleId.isEmpty()) {
            Log.e(TAG, "Cannot start article real-time listener: currentArticleId is null or empty.");
            return;
        }

        DocumentReference articleDocRef = db.collection("articles").document(currentArticleId);

        articleListenerRegistration = articleDocRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@androidx.annotation.Nullable DocumentSnapshot snapshot,
                                @androidx.annotation.Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed for article: " + currentArticleId, e);
                    Toast.makeText(ReadNewsActivity.this, "Failed to load article updates: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.d(TAG, "Real-time update received for article: " + currentArticleId);
                    currentNewsTitle = snapshot.getString("title");
                    currentNewsContent = snapshot.getString("content");
                    Timestamp timestamp = snapshot.getTimestamp("createdAt");
                    String imageUrlFromFirestore = snapshot.getString("imageUrl"); // NEW: Get imageUrl from Firestore

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    currentNewsDate = (timestamp != null) ? sdf.format(timestamp.toDate()) : "Unknown Date";

                    // Update currentNewsImageUrl
                    currentNewsImageUrl = imageUrlFromFirestore;

                    // Update the TextViews and ImageView
                    readNewsTitleTextView.setText(currentNewsTitle != null ? currentNewsTitle : "N/A");
                    readNewsContentTextView.setText(currentNewsContent != null ? currentNewsContent : "No content available.");
                    readNewsPostedDateTextView.setText(currentNewsDate);

                    // --- Image Loading with Glide (from Realtime Update) ---
                    // Use the imageUrl from Firestore, with the local placeholder as fallback
                    loadImageWithGlide(currentNewsImageUrl, R.drawable.news_placeholder);
                    // --- End Image Loading ---

                    if (isReadingAloud && textToSpeech.isSpeaking()) {
                        stopReadingAloud();
                    }

                } else {
                    Log.d(TAG, "Article " + currentArticleId + " no longer exists or is empty.");
                    Toast.makeText(ReadNewsActivity.this, "This article is no longer available.", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        });
    }

    private void stopArticleRealtimeListener() {
        if (articleListenerRegistration != null) {
            articleListenerRegistration.remove();
            articleListenerRegistration = null;
            Log.d(TAG, "Stopped article real-time listener for: " + currentArticleId);
        }
    }

    // Helper method to load images using Glide.
    // Prioritizes imageUrl; falls back to imageResId if imageUrl is null or empty.
    private void loadImageWithGlide(String imageUrl, int localImageResId) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.news_placeholder) // Show this while loading
                    .error(R.drawable.news_placeholder) // Show this if loading fails
                    .into(readNewsImageView);
            Log.d(TAG, "Loading image from URL: " + imageUrl);
        } else if (localImageResId != 0) {
            // Fallback to local resource if imageUrl is absent
            Glide.with(this)
                    .load(localImageResId)
                    .error(R.drawable.news_placeholder) // Fallback if local resource is invalid somehow
                    .into(readNewsImageView);
            Log.d(TAG, "Loading image from local resource ID: " + localImageResId);
        } else {
            // If neither is available, set the default placeholder
            readNewsImageView.setImageResource(R.drawable.news_placeholder);
            Log.d(TAG, "No image URL or local resource ID, showing default placeholder.");
        }
    }

    // Method to check if the current news article is bookmarked by the user
    private void checkBookmarkStatus() {
        if (currentUserId == null || currentArticleId == null || currentArticleId.isEmpty()) {
            isBookmarked = false;
            foundBookmarkDocId = null;
            invalidateOptionsMenu();
            Log.w(TAG, "Cannot check bookmark status: User ID or Article ID is missing.");
            return;
        }

        bookmarksRef.whereEqualTo("userId", currentUserId)
                .whereEqualTo("articleId", currentArticleId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (!task.getResult().isEmpty()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    foundBookmarkDocId = document.getId();
                                    isBookmarked = true;
                                    Log.d(TAG, "News is bookmarked. Bookmark Doc ID: " + foundBookmarkDocId);
                                    break;
                                }
                            } else {
                                isBookmarked = false;
                                foundBookmarkDocId = null;
                                Log.d(TAG, "News is NOT bookmarked.");
                            }
                            invalidateOptionsMenu();
                        } else {
                            Log.e(TAG, "Error getting bookmark documents: ", task.getException());
                            isBookmarked = false;
                            foundBookmarkDocId = null;
                            invalidateOptionsMenu();
                        }
                    }
                });
    }

    // Method to toggle the bookmark adding or removing
    private void toggleBookmark() {
        if (currentUserId == null) {
            Toast.makeText(this, R.string.logging_error_bookmarks, Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentArticleId == null || currentArticleId.isEmpty()) {
            Toast.makeText(this, R.string.article_missing, Toast.LENGTH_SHORT).show();
            return;
        }
        if (isBookmarked && foundBookmarkDocId != null) {
            bookmarksRef.document(foundBookmarkDocId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        isBookmarked = false;
                        foundBookmarkDocId = null;
                        updateBookmarkIcon();
                        Toast.makeText(ReadNewsActivity.this, R.string.bookmark_removed, Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Bookmark successfully deleted for article: " + currentArticleId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error deleting bookmark", e);
                        Toast.makeText(ReadNewsActivity.this, R.string.bookmark_remove_error, Toast.LENGTH_SHORT).show();
                    }
                });
        } else {
            Map<String, Object> bookmark = new HashMap<>();
            bookmark.put("userId", currentUserId);
            bookmark.put("articleId", currentArticleId);
            bookmark.put("createdAt", FieldValue.serverTimestamp());

            bookmarksRef.add(bookmark)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        isBookmarked = true;
                        foundBookmarkDocId = documentReference.getId();
                        updateBookmarkIcon();
                        Toast.makeText(ReadNewsActivity.this, R.string.bookmark_added, Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Bookmark successfully added with ID: " + foundBookmarkDocId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error adding bookmark", e);
                        Toast.makeText(ReadNewsActivity.this, R.string.bookmark_add_error, Toast.LENGTH_SHORT).show();
                    }
                });
        }
    }

    // Method to update the bookmark icon based on the current state
    private void updateBookmarkIcon() {
        if (bookmarkMenuItem != null) {
            if (isBookmarked) {
                bookmarkMenuItem.setIcon(R.drawable.bookmark_filled);
            } else {
                bookmarkMenuItem.setIcon(R.drawable.bookmark);
            }
            bookmarkMenuItem.setEnabled(currentUserId != null && currentArticleId != null);
        }
    }

    // Method to initialize Text-to-Speech
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

    // Method to start or stop reading the news content aloud
    private void startReadingAloud() {
        if (textToSpeech != null && isTtsInitialized && !textToSpeech.isSpeaking() && currentNewsContent != null && !currentNewsContent.isEmpty()) {
            textToSpeech.speak(currentNewsContent, TextToSpeech.QUEUE_FLUSH, null, "newsContent");
            isReadingAloud = true;
            Toast.makeText(this, R.string.reading_aloud, Toast.LENGTH_SHORT).show();
        } else if (textToSpeech != null && textToSpeech.isSpeaking()) {
            Toast.makeText(this, R.string.already_reading, Toast.LENGTH_SHORT).show();
        } else if (currentNewsContent == null || currentNewsContent.isEmpty()) {
            Toast.makeText(this, R.string.content_error, Toast.LENGTH_SHORT).show();
        }
        updateReadAloudIcon();
    }

    // Method to stop reading the news content aloud
    private void stopReadingAloud() {
        if (textToSpeech != null && textToSpeech.isSpeaking()) {
            textToSpeech.stop();
            isReadingAloud = false;
            Toast.makeText(this, R.string.reading_stopped, Toast.LENGTH_SHORT).show();
        }
        updateReadAloudIcon();
    }

    // Method to update the Read Aloud icon based on the TTS state
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

    // Method to inflate the menu and set up the bookmark and read aloud icons
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.read_news_toolbar_menu, menu);
        bookmarkMenuItem = menu.findItem(R.id.action_bookmark);
        readAloudMenuItem = menu.findItem(R.id.action_read_aloud);

        updateBookmarkIcon();
        updateReadAloudIcon();
        return true;
    }

    // Method to handle menu item selections
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

    // Method to handle the Up button in the toolbar
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Lifecycle method to clean up Text-to-Speech resources
    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            Log.d(TAG, "TextToSpeech shut down.");
        }
        stopArticleRealtimeListener();
        super.onDestroy();
    }
}