package com.example.currents.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.currents.R;
import com.example.currents.adapter.NewsAdapter;
import com.example.currents.model.NewsItem;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

// Firebase Imports
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class SavedNewsActivity extends AppCompatActivity implements NewsAdapter.OnNewsClickListener {

    private static final String TAG = "SavedNewsActivity";

    private Toolbar toolbar;
    private ChipGroup categoryChipGroup;
    private RecyclerView savedNewsRecyclerView;
    private NewsAdapter newsAdapter;
    private List<NewsItem> allNewsItems;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private static final String PREF_NAME = "CurrentUserPrefs";
    private static final String KEY_USER_UID = "user_uid";

    // Re-use constants from HomeActivity for consistency when passing data
    public static final String EXTRA_ARTICLE_ID = "extra_article_id"; // Added for article ID
    public static final String EXTRA_NEWS_TITLE = "extra_news_title";
    public static final String EXTRA_NEWS_DATE = "extra_news_date";
    public static final String EXTRA_NEWS_IMAGE_RES_ID = "extra_news_image_res_id";
    public static final String EXTRA_NEWS_IMAGE_URL = "extra_news_image_url"; // NEW: Constant for image URL
    public static final String EXTRA_NEWS_CONTENT = "extra_news_content";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_news);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        toolbar = findViewById(R.id.savedNewsToolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        categoryChipGroup = findViewById(R.id.categoryChipGroup);
        savedNewsRecyclerView = findViewById(R.id.savedNewsRecyclerView);

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

        fetchBookmarkedNews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.saved_news_toolbar_menu, menu);
        return true;
    }

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

    /**
     * Fetches bookmarked news articles for the current user from Firestore.
     */
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
            Toast.makeText(this, "Please log in to see your saved articles.", Toast.LENGTH_SHORT).show();
            allNewsItems.clear();
            newsAdapter.setNewsList(allNewsItems);
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
                            Toast.makeText(SavedNewsActivity.this, "No saved articles found.", Toast.LENGTH_SHORT).show();
                            return;
                        }

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
                                                String imageUrl = document.getString("imageUrl"); // NEW: Get imageUrl from Firestore

                                                // Default placeholder if no specific image is found/provided
                                                int imageResId = R.drawable.news_placeholder;

                                                if (title != null && category != null && content != null) {
                                                    // Pass imageUrl to the NewsItem constructor
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

                                        Chip selectedChip = findViewById(categoryChipGroup.getCheckedChipIds().get(0));
                                        if (selectedChip != null) {
                                            filterNewsByCategory(selectedChip.getText().toString());
                                        } else {
                                            filterNewsByCategory("All");
                                        }

                                    } else {
                                        Log.e(TAG, "Error getting bookmarked articles: ", articleTask.getException());
                                        Toast.makeText(SavedNewsActivity.this, "Failed to load saved articles.", Toast.LENGTH_SHORT).show();
                                        allNewsItems.clear();
                                        newsAdapter.setNewsList(allNewsItems);
                                    }
                                });

                    } else {
                        Log.e(TAG, "Error getting bookmark IDs: ", bookmarkTask.getException());
                        Toast.makeText(SavedNewsActivity.this, "Failed to load bookmarks.", Toast.LENGTH_SHORT).show();
                        allNewsItems.clear();
                        newsAdapter.setNewsList(allNewsItems);
                    }
                });
    }

    /**
     * Deletes all bookmarks for the current user from Firestore.
     */
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
            Toast.makeText(this, "Cannot clear bookmarks: user not logged in.", Toast.LENGTH_SHORT).show();
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
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(SavedNewsActivity.this, "Failed to clear saved articles: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    Log.e(TAG, "Error clearing bookmarks for user: " + currentUserId, e);
                                });
                    } else {
                        Toast.makeText(SavedNewsActivity.this, "Failed to fetch bookmarks to clear.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error fetching bookmarks to clear: ", task.getException());
                    }
                });
    }


    // --- NewsAdapter.OnNewsClickListener Implementation ---
    @Override
    public void onNewsClick(NewsItem newsItem) {
        // Start ReadNewsActivity and pass news data
        Intent intent = new Intent(SavedNewsActivity.this, ReadNewsActivity.class);
        intent.putExtra(EXTRA_ARTICLE_ID, newsItem.getId()); // Pass the article ID
        intent.putExtra(EXTRA_NEWS_TITLE, newsItem.getTitle());
        intent.putExtra(EXTRA_NEWS_DATE, newsItem.getPostedDate());
        intent.putExtra(EXTRA_NEWS_IMAGE_RES_ID, newsItem.getImageResId()); // Pass resource ID as fallback
        intent.putExtra(EXTRA_NEWS_IMAGE_URL, newsItem.getImageUrl()); // NEW: Pass the imageUrl
        intent.putExtra(EXTRA_NEWS_CONTENT, newsItem.getContent());
        startActivity(intent);
    }

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchBookmarkedNews();
    }
}