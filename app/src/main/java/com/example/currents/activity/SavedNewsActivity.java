package com.example.currents.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.currents.R;
import com.example.currents.adapter.NewsAdapter;
import com.example.currents.model.NewsItem;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class SavedNewsActivity extends AppCompatActivity implements NewsAdapter.OnNewsClickListener {
    // Tag for logging
    private static final String TAG = "SavedNewsActivity";

    // UI components
    private Toolbar toolbar;
    private HorizontalScrollView chipScrollView;
    private ChipGroup categoryChipGroup;
    private RecyclerView savedNewsRecyclerView;
    private LinearLayout noSavedNewsLayout;
    private NewsAdapter newsAdapter;
    private List<NewsItem> allNewsItems;

    // Firebase components
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // Flag to track if there are any saved news articles
    private boolean hasSavedNews = false;

    // SharedPreferences keys for storing current user ID
    private static final String PREF_NAME = "CurrentUserPrefs";
    private static final String KEY_USER_UID = "user_uid";

    // Constants for passing data to ReadNewsActivity
    public static final String EXTRA_ARTICLE_ID = "extra_article_id";
    public static final String EXTRA_NEWS_TITLE = "extra_news_title";
    public static final String EXTRA_NEWS_DATE = "extra_news_date";
    public static final String EXTRA_NEWS_IMAGE_RES_ID = "extra_news_image_res_id";
    public static final String EXTRA_NEWS_IMAGE_URL = "extra_news_image_url";
    public static final String EXTRA_NEWS_CONTENT = "extra_news_content";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Call the superclass method
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_news);

        // Initialize Firebase components
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI components
        toolbar = findViewById(R.id.savedNewsToolbar);
        setSupportActionBar(toolbar);

        // Set the title of the toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Initialize the UI components
        chipScrollView = findViewById(R.id.chipScrollView);
        categoryChipGroup = findViewById(R.id.categoryChipGroup);
        savedNewsRecyclerView = findViewById(R.id.savedNewsRecyclerView);
        noSavedNewsLayout = findViewById(R.id.noSavedNewsLayout);

        allNewsItems = new ArrayList<>();

        savedNewsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        newsAdapter = new NewsAdapter(new ArrayList<>(), this);
        savedNewsRecyclerView.setAdapter(newsAdapter);

        categoryChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                filterNewsByCategory("All");
            } else {
                Chip selectedChip = findViewById(checkedIds.get(0));
                if (selectedChip != null) {
                    filterNewsByCategory(selectedChip.getText().toString());
                }
            }
        });

        Chip allChip = findViewById(R.id.chipAll);
        if (allChip != null) {
            allChip.setChecked(true);
        }

        // Fetch bookmarked news articles for the current user
        fetchBookmarkedNews();
    }

    // Inflate the menu; this adds items to the action bar if it is present.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.saved_news_toolbar_menu, menu);
        return true;
    }

    // This method is called every time the menu is displayed
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem clearBookmarksItem = menu.findItem(R.id.action_clear_bookmarks);
        if (clearBookmarksItem != null) {
            clearBookmarksItem.setVisible(hasSavedNews); // Set visibility based on the flag
        }
        return super.onPrepareOptionsMenu(menu);
    }

    // Handle action bar item clicks here.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_clear_bookmarks) {
            clearAllBookmarksForCurrentUser();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Fetches bookmarked news articles for the current user from firestore.
    private void fetchBookmarkedNews() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = null;

        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            SharedPreferences sharedPref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            userId = sharedPref.getString(KEY_USER_UID, null);
        }

        if (userId == null) {
            Log.e(TAG, "No current user ID found. Cannot fetch bookmarks.");
            Toast.makeText(this, R.string.saved_article_error, Toast.LENGTH_SHORT).show();
            allNewsItems.clear();
            newsAdapter.setNewsList(allNewsItems);

            // Update flag and UI visibility
            hasSavedNews = false;
            chipScrollView.setVisibility(View.GONE);
            savedNewsRecyclerView.setVisibility(View.GONE);
            noSavedNewsLayout.setVisibility(View.VISIBLE);

            // Invalidate to update menu visibility
            invalidateOptionsMenu();
            return;
        }

        final String currentUserId = userId;

        db.collection("bookmarks")
            .whereEqualTo("userId", currentUserId)
            .get()
            .addOnCompleteListener(bookmarkTask -> {
                if (bookmarkTask.isSuccessful()) {
                    List<String> bookmarkedArticleIds = new ArrayList<>();
                    for (QueryDocumentSnapshot document : bookmarkTask.getResult()) {
                        String articleId = document.getString("articleId");
                        if (articleId != null) {
                            bookmarkedArticleIds.add(articleId);
                        }
                    }

                    if (bookmarkedArticleIds.isEmpty()) {
                        Log.d(TAG, "No bookmarks found for user: " + currentUserId);
                        allNewsItems.clear();
                        newsAdapter.setNewsList(allNewsItems);
                        hasSavedNews = false;
                        chipScrollView.setVisibility(View.GONE);
                        savedNewsRecyclerView.setVisibility(View.GONE);
                        noSavedNewsLayout.setVisibility(View.VISIBLE);
                        invalidateOptionsMenu(); // Invalidate to update menu visibility
                        return;
                    }

                    // If bookmarks are found, hide the "No saved news" layout and show chips/recycler
                    chipScrollView.setVisibility(View.VISIBLE);
                    savedNewsRecyclerView.setVisibility(View.VISIBLE);
                    noSavedNewsLayout.setVisibility(View.GONE);


                    List<String> finalArticleIds = new ArrayList<>(bookmarkedArticleIds);

                    // Firestore's whereIn clause has a limit of 10. If a user has more, you need to chunk the list.
                    if (finalArticleIds.size() > 10) {
                        Log.w(TAG, "User has more than 10 bookmarks. Only fetching the first 10 due to whereIn limit.");
                        finalArticleIds = finalArticleIds.subList(0, 10);
                    }

                    db.collection("articles")
                        .whereIn(FieldPath.documentId(), finalArticleIds)
                        .get()
                        .addOnCompleteListener(articleTask -> {
                            if (articleTask.isSuccessful()) {
                                List<NewsItem> fetchedBookmarkedNews = new ArrayList<>();
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                                for (QueryDocumentSnapshot document : articleTask.getResult()) {
                                    try {
                                        String articleId = document.getId();
                                        String title = document.getString("title");
                                        String category = document.getString("category");
                                        String content = document.getString("content");
                                        Timestamp timestamp = document.getTimestamp("createdAt");
                                        String postedDate = (timestamp != null) ? sdf.format(timestamp.toDate()) : "Unknown Date";
                                        String imageUrl = document.getString("imageUrl");

                                        int imageResId = R.drawable.news_placeholder; // Default placeholder

                                        if (title != null && category != null && content != null) {
                                            fetchedBookmarkedNews.add(new NewsItem(articleId, title, postedDate, imageResId, category, content, imageUrl));
                                        } else {
                                            Log.w(TAG, "Skipping bookmarked article with missing fields: " + document.getId());
                                        }
                                    } catch (Exception e) {
                                        Log.e(TAG, "Error parsing bookmarked article document " + document.getId() + ": " + e.getMessage(), e);
                                    }
                                }
                                allNewsItems.clear();
                                allNewsItems.addAll(fetchedBookmarkedNews);
                                Log.d(TAG, "Fetched " + allNewsItems.size() + " bookmarked news items.");

                                Chip selectedChip = findViewById(categoryChipGroup.getCheckedChipIds().isEmpty() ? R.id.chipAll : categoryChipGroup.getCheckedChipIds().get(0));
                                if (selectedChip != null) {
                                    filterNewsByCategory(selectedChip.getText().toString());
                                } else {
                                    filterNewsByCategory("All");
                                }

                                // After fetching and updating the list, check if it's still empty
                                hasSavedNews = !allNewsItems.isEmpty(); // Update the flag
                                if (!hasSavedNews) { // If still no saved news after fetching articles
                                    chipScrollView.setVisibility(View.GONE);
                                    savedNewsRecyclerView.setVisibility(View.GONE);
                                    noSavedNewsLayout.setVisibility(View.VISIBLE);
                                } else {
                                    chipScrollView.setVisibility(View.VISIBLE);
                                    savedNewsRecyclerView.setVisibility(View.VISIBLE);
                                    noSavedNewsLayout.setVisibility(View.GONE);
                                }
                                invalidateOptionsMenu(); // Invalidate to update menu visibility

                            } else {
                                Log.e(TAG, "Error getting bookmarked articles: ", articleTask.getException());
                                Toast.makeText(SavedNewsActivity.this, R.string.failed_to_load_bookmarks, Toast.LENGTH_SHORT).show();
                                allNewsItems.clear();
                                newsAdapter.setNewsList(allNewsItems);
                                // On error, revert to "No saved news" state and update flag
                                hasSavedNews = false;
                                chipScrollView.setVisibility(View.GONE);
                                savedNewsRecyclerView.setVisibility(View.GONE);
                                noSavedNewsLayout.setVisibility(View.VISIBLE);
                                invalidateOptionsMenu(); // Invalidate to update menu visibility
                            }
                        });

                } else {
                    Log.e(TAG, "Error getting bookmark IDs: ", bookmarkTask.getException());
                    Toast.makeText(SavedNewsActivity.this, R.string.failed_to_load_bookmarks, Toast.LENGTH_SHORT).show();
                    allNewsItems.clear();
                    newsAdapter.setNewsList(allNewsItems);
                    // On error, revert to "No saved news" state and update flag
                    hasSavedNews = false;
                    chipScrollView.setVisibility(View.GONE);
                    savedNewsRecyclerView.setVisibility(View.GONE);
                    noSavedNewsLayout.setVisibility(View.VISIBLE);
                    invalidateOptionsMenu(); // Invalidate to update menu visibility
                }
            });
    }

    // Deletes all bookmarks for the current user from firestore.
    private void clearAllBookmarksForCurrentUser() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = null;

        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            SharedPreferences sharedPref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            userId = sharedPref.getString(KEY_USER_UID, null);
        }

        if (userId == null) {
            Toast.makeText(this, R.string.failed_to_clear_all_bookmarks, Toast.LENGTH_SHORT).show();
            return;
        }

        final String currentUserId = userId;

        db.collection("bookmarks")
            .whereEqualTo("userId", currentUserId)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    WriteBatch batch = db.batch();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        batch.delete(document.getReference());
                    }

                    batch.commit()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(SavedNewsActivity.this, "All saved articles cleared!", Toast.LENGTH_SHORT).show();
                            allNewsItems.clear();
                            newsAdapter.setNewsList(new ArrayList<>());
                            Chip allChip = findViewById(R.id.chipAll);
                            if (allChip != null) {
                                allChip.setChecked(true);
                            }
                            Log.d(TAG, "Successfully cleared all bookmarks for user: " + currentUserId);
                            // After clearing, show "No saved news" layout and hide chips/recycler
                            hasSavedNews = false; // No more saved news
                            chipScrollView.setVisibility(View.GONE);
                            savedNewsRecyclerView.setVisibility(View.GONE);
                            noSavedNewsLayout.setVisibility(View.VISIBLE);
                            invalidateOptionsMenu(); // Invalidate to update menu visibility
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(SavedNewsActivity.this, R.string.failed_to_bookmark_remove + e.getMessage(), Toast.LENGTH_LONG).show();
                            Log.e(TAG, "Error clearing bookmarks for user: " + currentUserId, e);
                        });
                } else {
                    Toast.makeText(SavedNewsActivity.this, R.string.failed_to_fetch_clear_bookmark, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error fetching bookmarks to clear: ", task.getException());
                }
            });
    }


    // NewsAdapter.OnNewsClickListener Implementation
    @Override
    public void onNewsClick(NewsItem newsItem) {
        // Start ReadNewsActivity and pass news data
        Intent intent = new Intent(SavedNewsActivity.this, ReadNewsActivity.class);
        intent.putExtra(EXTRA_ARTICLE_ID, newsItem.getId());
        intent.putExtra(EXTRA_NEWS_TITLE, newsItem.getTitle());
        intent.putExtra(EXTRA_NEWS_DATE, newsItem.getPostedDate());
        intent.putExtra(EXTRA_NEWS_IMAGE_RES_ID, newsItem.getImageResId());
        intent.putExtra(EXTRA_NEWS_IMAGE_URL, newsItem.getImageUrl());
        intent.putExtra(EXTRA_NEWS_CONTENT, newsItem.getContent());
        startActivity(intent);
    }

    // Method to filter news items by category
    private void filterNewsByCategory(String category) {
        List<NewsItem> filteredList;
        if (category == null || category.isEmpty() || category.equalsIgnoreCase("All")) {
            filteredList = new ArrayList<>(allNewsItems);
        } else {
            if (allNewsItems != null) {
                filteredList = allNewsItems.stream()
                        .filter(news -> news.getCategory() != null && news.getCategory().equalsIgnoreCase(category))
                        .collect(Collectors.toList());
            } else {
                filteredList = new ArrayList<>();
            }
        }
        newsAdapter.setNewsList(filteredList);

        // This method also needs to ensure the UI visibility is correct,
        // especially if filtering results in an empty list, but the allNewsItems might not be empty.
        // The hasSavedNews flag should primarily be controlled by fetchBookmarkedNews() and clearAllBookmarksForCurrentUser().
        // Here, we just ensure the filtered view is correct.
        if (filteredList.isEmpty() && hasSavedNews) { // If filtered list is empty but there ARE saved news in other categories
            // We might want to show a "No news in this category" message instead of the main "No saved news"
            // For now, let's stick to the overall state
            chipScrollView.setVisibility(View.VISIBLE); // Chips should still be visible to change category
            savedNewsRecyclerView.setVisibility(View.GONE);
            noSavedNewsLayout.setVisibility(View.VISIBLE); // Show the "No news" message if filtered list is empty
            // You might want a different message here, e.g., "No saved news in this category"
            // For simplicity, we are reusing the same layout.
        } else if (filteredList.isEmpty() && !hasSavedNews) { // No saved news at all
            chipScrollView.setVisibility(View.GONE);
            savedNewsRecyclerView.setVisibility(View.GONE);
            noSavedNewsLayout.setVisibility(View.VISIBLE);
        } else { // Has saved news and filtered list is not empty
            chipScrollView.setVisibility(View.VISIBLE);
            savedNewsRecyclerView.setVisibility(View.VISIBLE);
            noSavedNewsLayout.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchBookmarkedNews();
    }
}