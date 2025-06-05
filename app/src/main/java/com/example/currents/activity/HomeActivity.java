package com.example.currents.activity;

import android.content.Context; // Added for SharedPreferences
import android.content.Intent;
import android.content.SharedPreferences; // Added for SharedPreferences
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.currents.R;
import com.example.currents.adapter.NewsAdapter;
import com.example.currents.adapter.CarouselNewsAdapter;
import com.example.currents.model.NewsItem;
import com.google.android.material.search.SearchBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

// Firebase Imports
import com.google.firebase.auth.FirebaseAuth; // Added for FirebaseAuth
import com.google.firebase.auth.FirebaseUser; // Added for FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldPath; // Added for whereIn documentId query
import com.google.firebase.firestore.ListenerRegistration; // Import for ListenerRegistration

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class HomeActivity extends AppCompatActivity implements NewsAdapter.OnNewsClickListener {

    private static final String TAG = "HomeActivity";

    private RecyclerView horizontalCardRecyclerView;
    private RecyclerView verticalCardRecyclerView;
    private SearchBar searchBar;
    private BottomNavigationView bottomNavigationView;

    private NewsAdapter verticalNewsAdapter;
    private CarouselNewsAdapter horizontalNewsAdapter;
    private List<NewsItem> allNewsItems;
    private List<NewsItem> bookmarkedNewsItems; // New list for bookmarked articles

    public static final String EXTRA_ARTICLE_ID = "extra_article_id";
    public static final String EXTRA_NEWS_TITLE = "extra_news_title";
    public static final String EXTRA_NEWS_DATE = "extra_news_date";
    public static final String EXTRA_NEWS_IMAGE_RES_ID = "extra_news_image_res_id";
    public static final String EXTRA_NEWS_CONTENT = "extra_news_content";

    private ActivityResultLauncher<Intent> searchActivityLauncher;

    // Firebase Firestore and Auth instances
    private FirebaseFirestore db;
    private FirebaseAuth mAuth; // Added FirebaseAuth

    // SharedPreferences name and key for User UID (MUST MATCH LoginActivity/ProfileActivity)
    private static final String PREF_NAME = "CurrentUserPrefs";
    private static final String KEY_USER_UID = "user_uid";

    // Listener Registration for Firestore (for real-time updates)
    private ListenerRegistration newsListenerRegistration;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize Firebase Firestore and Auth
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance(); // Initialize FirebaseAuth

        horizontalCardRecyclerView = findViewById(R.id.horizontalCardRecyclerView);
        verticalCardRecyclerView = findViewById(R.id.verticalCardRecyclerView);

        searchBar = findViewById(R.id.searchBar);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        allNewsItems = new ArrayList<>(); // Initialize empty list for all news
        bookmarkedNewsItems = new ArrayList<>(); // Initialize empty list for bookmarked news

        // Register the ActivityResultLauncher
        searchActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.hasExtra(SearchViewActivity.EXTRA_SELECTED_NEWS_ITEM)) {
                            NewsItem selectedNewsItem = (NewsItem) data.getSerializableExtra(SearchViewActivity.EXTRA_SELECTED_NEWS_ITEM);
                            if (selectedNewsItem != null) {
                                onNewsClick(selectedNewsItem);
                            }
                        }
                    }
                }
        );

        // Set up LinearLayoutManager for horizontal RecyclerView (for bookmarked news)
        horizontalCardRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        horizontalNewsAdapter = new CarouselNewsAdapter(bookmarkedNewsItems, this); // Use bookmarkedNewsItems
        horizontalCardRecyclerView.setAdapter(horizontalNewsAdapter);

        // Set up LinearLayoutManager for vertical RecyclerView (for all categorized news)
        verticalCardRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        verticalNewsAdapter = new NewsAdapter(new ArrayList<>(), this); // Start with empty list
        verticalCardRecyclerView.setAdapter(verticalNewsAdapter);

        // --- Fetch news items and bookmarks from Firestore ---
        // Changed to use real-time listener
        setupNewsRealtimeListener();
        fetchBookmarkedNewsFromFirestore(); // Fetch bookmarked news

        // --- SearchBar Click Listener (Now launches SearchViewActivity) ---
        searchBar.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SearchViewActivity.class);
            intent.putExtra(SearchViewActivity.EXTRA_ALL_NEWS_ITEMS, (ArrayList<NewsItem>) allNewsItems);
            searchActivityLauncher.launch(intent);
        });

        // --- SearchBar Menu Item Clicks (top right menu on SearchBar) ---
        searchBar.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.about) {
                Intent intent = new Intent(HomeActivity.this, AboutActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.profile) {
                Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });

        // --- BottomNavigationView Logic ---
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            String category = "";
            if (itemId == R.id.navigation_sports) {
                category = getString(R.string.sports);
            } else if (itemId == R.id.navigation_academic) {
                category = getString(R.string.academic);
            } else if (itemId == R.id.navigation_events) {
                category = getString(R.string.events);
            }
            searchBar.setHint(category);
            filterNewsByCategory(category);
            return true;
        });

        // Set initial state: select Sports in bottom navigation and filter news accordingly
        bottomNavigationView.setSelectedItemId(R.id.navigation_sports);
        searchBar.setHint(getString(R.string.sports));
    }

    /**
     * Sets up a real-time listener for the "articles" collection in Firestore.
     * This method will automatically update `allNewsItems` when changes occur.
     */
    private void setupNewsRealtimeListener() {
        if (newsListenerRegistration != null) {
            newsListenerRegistration.remove(); // Remove previous listener if it exists
        }

        newsListenerRegistration = db.collection("articles")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        Toast.makeText(HomeActivity.this, "Failed to load news updates.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (snapshots != null) {
                        List<NewsItem> fetchedNews = new ArrayList<>();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                        for (QueryDocumentSnapshot document : snapshots) {
                            try {
                                String articleId = document.getId();
                                String title = document.getString("title");
                                String category = document.getString("category");
                                String content = document.getString("content");
                                Timestamp timestamp = document.getTimestamp("createdAt");
                                String postedDate = (timestamp != null) ? sdf.format(timestamp.toDate()) : "Unknown Date";

                                String imageName = document.getString("imageName");
                                int imageResId = getResources().getIdentifier(imageName != null ? imageName : "news_placeholder", "drawable", getPackageName());
                                if (imageResId == 0) {
                                    imageResId = R.drawable.news_placeholder;
                                }

                                if (title != null && category != null && content != null) {
                                    fetchedNews.add(new NewsItem(articleId, title, postedDate, imageResId, category, content));
                                } else {
                                    Log.w(TAG, "Skipping document with missing fields: " + document.getId());
                                }
                            } catch (Exception ex) {
                                Log.e(TAG, "Error parsing document " + document.getId() + ": " + ex.getMessage(), ex);
                            }
                        }
                        allNewsItems.clear();
                        allNewsItems.addAll(fetchedNews);

                        Log.d(TAG, "Realtime update: Fetched " + allNewsItems.size() + " news items from Firestore.");

                        // After fetching all news, apply initial filter based on selected bottom nav item
                        filterNewsByCategory(getSelectedCategoryFromBottomNav());
                    }
                });
    }


    /**
     * Fetches bookmarked news articles for the current user from Firestore.
     */
    private void fetchBookmarkedNewsFromFirestore() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = null;

        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            // Fallback: If FirebaseAuth.getCurrentUser() is null, try SharedPreferences
            SharedPreferences sharedPref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            userId = sharedPref.getString(KEY_USER_UID, null);
        }

        if (userId == null) {
            Log.e(TAG, "No current user ID found. Cannot fetch bookmarks.");
            bookmarkedNewsItems.clear();
            horizontalNewsAdapter.setNewsList(bookmarkedNewsItems); // Clear horizontal list
            // Toast.makeText(this, "Please log in to see your bookmarks.", Toast.LENGTH_SHORT).show(); // Avoid spamming if not logged in
            return;
        }

        final String currentUserId = userId; // Make final for use in inner classes

        // Step 1: Get article IDs from the "bookmarks" collection for the current user
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
                            bookmarkedNewsItems.clear();
                            horizontalNewsAdapter.setNewsList(bookmarkedNewsItems); // Update adapter
                            return; // No bookmarks, so nothing further to fetch
                        }

                        // Step 2: Fetch articles from "articles" collection using the gathered article IDs
                        // Firestore's whereIn clause has a limit of 10. If a user has more, you need to chunk the list.
                        // For simplicity, we'll handle the first 10 for now.
                        List<String> finalArticleIds = new ArrayList<>(bookmarkedArticleIds); // Create a mutable copy
                        if (finalArticleIds.size() > 10) {
                            Log.w(TAG, "User has more than 10 bookmarks. Only fetching the first 10 due to whereIn limit.");
                            finalArticleIds = finalArticleIds.subList(0, 10); // Take only the first 10
                        }

                        db.collection("articles")
                                .whereIn(FieldPath.documentId(), finalArticleIds) // Query by document ID
                                .get()
                                .addOnCompleteListener(articleTask -> {
                                    if (articleTask.isSuccessful()) {
                                        List<NewsItem> fetchedBookmarkedNews = new ArrayList<>();
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                                        for (QueryDocumentSnapshot document : articleTask.getResult()) {
                                            try {
                                                String articleId = document.getId(); // Get the actual article ID
                                                String title = document.getString("title");
                                                String category = document.getString("category");
                                                String content = document.getString("content");
                                                Timestamp timestamp = document.getTimestamp("createdAt");
                                                String postedDate = (timestamp != null) ? sdf.format(timestamp.toDate()) : "Unknown Date";

                                                String imageName = document.getString("imageName");
                                                int imageResId = getResources().getIdentifier(imageName != null ? imageName : "news_placeholder", "drawable", getPackageName());
                                                if (imageResId == 0) {
                                                    imageResId = R.drawable.news_placeholder;
                                                }

                                                if (title != null && category != null && content != null) {
                                                    fetchedBookmarkedNews.add(new NewsItem(articleId, title, postedDate, imageResId, category, content));
                                                } else {
                                                    Log.w(TAG, "Skipping bookmarked article with missing fields: " + document.getId());
                                                }
                                            } catch (Exception e) {
                                                Log.e(TAG, "Error parsing bookmarked article document " + document.getId() + ": " + e.getMessage(), e);
                                            }
                                        }
                                        bookmarkedNewsItems.clear();
                                        bookmarkedNewsItems.addAll(fetchedBookmarkedNews);
                                        horizontalNewsAdapter.setNewsList(bookmarkedNewsItems); // Update adapter
                                        Log.d(TAG, "Fetched " + bookmarkedNewsItems.size() + " bookmarked news items.");

                                    } else {
                                        Log.e(TAG, "Error getting bookmarked articles: ", articleTask.getException());
                                        Toast.makeText(HomeActivity.this, "Failed to load bookmarked news.", Toast.LENGTH_SHORT).show();
                                        bookmarkedNewsItems.clear();
                                        horizontalNewsAdapter.setNewsList(bookmarkedNewsItems); // Clear and update adapter
                                    }
                                });

                    } else {
                        Log.e(TAG, "Error getting bookmark IDs: ", bookmarkTask.getException());
                        Toast.makeText(HomeActivity.this, "Failed to load bookmarks.", Toast.LENGTH_SHORT).show();
                        bookmarkedNewsItems.clear();
                        horizontalNewsAdapter.setNewsList(bookmarkedNewsItems); // Clear and update adapter
                    }
                });
    }


    @Override
    public void onNewsClick(NewsItem newsItem) {
        Intent intent = new Intent(HomeActivity.this, ReadNewsActivity.class);
        intent.putExtra(EXTRA_ARTICLE_ID, newsItem.getId());
        intent.putExtra(EXTRA_NEWS_TITLE, newsItem.getTitle());
        intent.putExtra(EXTRA_NEWS_DATE, newsItem.getPostedDate());
        intent.putExtra(EXTRA_NEWS_IMAGE_RES_ID, newsItem.getImageResId());
        intent.putExtra(EXTRA_NEWS_CONTENT, newsItem.getContent());
        startActivity(intent);
    }

    private void filterNewsByCategory(String category) {
        List<NewsItem> filteredList;
        if (category == null || category.isEmpty()) {
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
        verticalNewsAdapter.setNewsList(filteredList);
    }

    private String getSelectedCategoryFromBottomNav() {
        int selectedId = bottomNavigationView.getSelectedItemId();
        if (selectedId == R.id.navigation_sports) {
            return getString(R.string.sports);
        } else if (selectedId == R.id.navigation_academic) {
            return getString(R.string.academic);
        } else if (selectedId == R.id.navigation_events) {
            return getString(R.string.events);
        }
        return getString(R.string.sports); // Default category
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Re-fetch bookmarks on resume to reflect changes (e.g., if user bookmarks/unbookmarks from ReadNewsActivity)
        fetchBookmarkedNewsFromFirestore();
        // The main news feed (allNewsItems) is now handled by the real-time listener,
        // so no need to call fetchNewsFromFirestore() here.
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Detach the Firestore listener to avoid memory leaks
        if (newsListenerRegistration != null) {
            newsListenerRegistration.remove();
            newsListenerRegistration = null;
            Log.d(TAG, "Firestore news listener removed.");
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}