package com.example.currents.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.currents.R;
import com.example.currents.adapter.NewsAdapter;
import com.example.currents.model.NewsItem;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.search.SearchBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors; // For filtering (ensure Java 8 compatibility)

public class HomeActivity extends AppCompatActivity implements NewsAdapter.OnNewsClickListener {

    private RecyclerView horizontalCardRecyclerView;
    private RecyclerView verticalCardRecyclerView;
    private SearchBar searchBar;
    private BottomNavigationView bottomNavigationView;
    private MaterialToolbar searchToolbar;
    private SearchView appCompatSearchView;

    private NewsAdapter verticalNewsAdapter; // Adapter for the vertical RecyclerView
    private List<NewsItem> allNewsItems; // Master list of all news

    // Keys for passing data to ReadNewsActivity (should be consistent with SavedNewsActivity)
    public static final String EXTRA_NEWS_TITLE = "extra_news_title";
    public static final String EXTRA_NEWS_DATE = "extra_news_date";
    public static final String EXTRA_NEWS_IMAGE_RES_ID = "extra_news_image_res_id";
    public static final String EXTRA_NEWS_CONTENT = "extra_news_content";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize RecyclerViews
        horizontalCardRecyclerView = findViewById(R.id.horizontalCardRecyclerView);
        verticalCardRecyclerView = findViewById(R.id.verticalCardRecyclerView);

        // Initialize SearchBar, BottomNavigationView
        searchBar = findViewById(R.id.searchBar);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Initialize MaterialToolbar and SearchView
        searchToolbar = findViewById(R.id.searchToolbar);
        appCompatSearchView = findViewById(R.id.appCompatSearchView);

        // Set up LinearLayoutManager for horizontal RecyclerView
        horizontalCardRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        // TODO: You'll need to set an adapter for horizontalCardRecyclerView here later,
        //  e.g., for "trending news" or "featured categories".

        // Set up LinearLayoutManager for vertical RecyclerView
        verticalCardRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        // --- Sample News Data ---
        allNewsItems = new ArrayList<>();
        // Add sample news items for Sports
        allNewsItems.add(new NewsItem("Champions League Final Recap", "2024-05-28", R.drawable.news_placeholder, "Sports", getString(R.string.lorem_ipsum)));
        allNewsItems.add(new NewsItem("NBA Playoffs: Game 7 Thriller", "2024-05-27", R.drawable.news_placeholder, "Sports", getString(R.string.lorem_ipsum)));
        allNewsItems.add(new NewsItem("Local Marathon Results", "2024-05-26", R.drawable.news_placeholder, "Sports", getString(R.string.lorem_ipsum)));
        allNewsItems.add(new NewsItem("Cricket World Cup Preparations", "2024-05-25", R.drawable.news_placeholder, "Sports", getString(R.string.lorem_ipsum)));

        // Add sample news items for Academic
        allNewsItems.add(new NewsItem("New AI Research Published", "2024-05-29", R.drawable.news_placeholder, "Academic", getString(R.string.lorem_ipsum)));
        allNewsItems.add(new NewsItem("University Hosts Tech Symposium", "2024-05-28", R.drawable.news_placeholder, "Academic", getString(R.string.lorem_ipsum)));
        allNewsItems.add(new NewsItem("Scholarship Opportunities for Students", "2024-05-27", R.drawable.news_placeholder, "Academic", getString(R.string.lorem_ipsum)));
        allNewsItems.add(new NewsItem("Breakthrough in Medical Science", "2024-05-26", R.drawable.news_placeholder, "Academic", getString(R.string.lorem_ipsum)));

        // Add sample news items for Events
        allNewsItems.add(new NewsItem("Summer Music Festival Announced", "2024-05-30", R.drawable.news_placeholder, "Events", getString(R.string.lorem_ipsum)));
        allNewsItems.add(new NewsItem("Community Art Fair This Weekend", "2024-05-29", R.drawable.news_placeholder, "Events", getString(R.string.lorem_ipsum)));
        allNewsItems.add(new NewsItem("Annual Food Festival Dates", "2024-05-28", R.drawable.news_placeholder, "Events", getString(R.string.lorem_ipsum)));
        allNewsItems.add(new NewsItem("Local Charity Run Success", "2024-05-27", R.drawable.news_placeholder, "Events", getString(R.string.lorem_ipsum)));


        // Initialize vertical RecyclerView adapter
        // Pass 'this' as the OnNewsClickListener for handling item clicks
        verticalNewsAdapter = new NewsAdapter(new ArrayList<>(), this);
        verticalCardRecyclerView.setAdapter(verticalNewsAdapter);




        // --- SearchBar and SearchView Logic ---

        // Make SearchBar clickable to reveal SearchView
        searchBar.setOnClickListener(v -> {
            searchBar.setVisibility(View.GONE); // Hide the SearchBar
            searchToolbar.setVisibility(View.VISIBLE); // Show the search Toolbar
            appCompatSearchView.setIconified(false); // Expand the SearchView immediately
            appCompatSearchView.requestFocus(); // Give focus to the SearchView
        });

        // Handle SearchToolbar navigation icon (back arrow) click
        searchToolbar.setNavigationOnClickListener(v -> {
            // Hide search toolbar and show search bar
            searchToolbar.setVisibility(View.GONE);
            searchBar.setVisibility(View.VISIBLE);
            appCompatSearchView.setQuery("", false); // Clear query
            appCompatSearchView.clearFocus(); // Remove focus
            // After closing search, ensure the correct category list is shown
            filterNewsByCategory(getSelectedCategoryFromBottomNav());
            // You might want to hide keyboard here too
            // InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            // imm.hideSoftInputFromWindow(appCompatSearchView.getWindowToken(), 0);
        });

        // Implement SearchView query text listener
        appCompatSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Toast.makeText(HomeActivity.this, "Searching for: " + query, Toast.LENGTH_SHORT).show();
                // Perform your search operation here, filtering the current category's news
                filterNewsByQuery(query);
                appCompatSearchView.clearFocus(); // Hide keyboard after search
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Optional: Filter your data as the user types (live search)
                // Be cautious with performance on very large datasets for live filtering
                // For this example, we'll only filter on submit, but you could enable live search here
                // if (!newText.isEmpty() || !appCompatSearchView.getQuery().toString().isEmpty()) { // Only filter if query is not empty
                //     filterNewsByQuery(newText);
                // } else {
                //     filterNewsByCategory(getSelectedCategoryFromBottomNav()); // Show current category if query becomes empty
                // }
                return false; // Return true if you handled the change, false otherwise
            }
        });

        // Optional: Listen for close button on SearchView (the 'x' icon)
        appCompatSearchView.setOnCloseListener(() -> {
            // This is triggered when the 'x' icon is pressed
            appCompatSearchView.setQuery("", false); // Clear query
            appCompatSearchView.clearFocus(); // Remove focus
            // After closing search, ensure the correct category list is shown
            filterNewsByCategory(getSelectedCategoryFromBottomNav());
            return false; // Return true if you consumed the event, false otherwise
        });

        // --- SearchBar Menu Item Clicks (top right menu on SearchBar) ---
        searchBar.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.developer_info) {
                // Navigate to DeveloperInfoActivity
                Intent intent = new Intent(HomeActivity.this, AboutActivity.class); // Assuming AboutActivity is your developer info screen
                startActivity(intent);
                return true;
            } else if (itemId == R.id.profile) {
                // Navigate to ProfileActivity
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
            searchBar.setHint(category); // Update search bar hint
            filterNewsByCategory(category); // Filter vertical news based on category
            return true; // Indicate that the item selection was handled
        });

        // Set initial state: select Sports in bottom navigation and filter news accordingly
        bottomNavigationView.setSelectedItemId(R.id.navigation_sports);
        searchBar.setHint(getString(R.string.sports)); // Set initial hint
        filterNewsByCategory(getString(R.string.sports)); // Initial filter to show Sports news
    }

    // --- NewsAdapter.OnNewsClickListener Implementation ---
    // This method is called when a news item in the vertical RecyclerView is clicked
    @Override
    public void onNewsClick(NewsItem newsItem) {
        // Start ReadNewsActivity and pass all necessary news data
        Intent intent = new Intent(HomeActivity.this, ReadNewsActivity.class);
        intent.putExtra(EXTRA_NEWS_TITLE, newsItem.getTitle());
        intent.putExtra(EXTRA_NEWS_DATE, newsItem.getPostedDate());
        intent.putExtra(EXTRA_NEWS_IMAGE_RES_ID, newsItem.getImageResId());
        intent.putExtra(EXTRA_NEWS_CONTENT, newsItem.getContent()); // Pass the full content
        startActivity(intent);
    }

    // --- Helper method to filter news by category ---
    private void filterNewsByCategory(String category) {
        List<NewsItem> filteredList;
        if (category == null || category.isEmpty()) {
            // If category is null or empty, show all news from the master list
            filteredList = new ArrayList<>(allNewsItems);
        } else {
            // Filter the master list based on the category (case-insensitive)
            filteredList = allNewsItems.stream()
                    .filter(news -> news.getCategory().equalsIgnoreCase(category))
                    .collect(Collectors.toList());
        }
        // Update the adapter with the filtered list, which will refresh the RecyclerView
        verticalNewsAdapter.setNewsList(filteredList);
    }

    // --- Helper method to filter news by search query within the current category ---
    private void filterNewsByQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            // If query is empty, revert to showing news for the currently selected category
            filterNewsByCategory(getSelectedCategoryFromBottomNav());
            return;
        }

        // Get the news items for the currently selected category first
        String currentCategory = getSelectedCategoryFromBottomNav();
        List<NewsItem> currentCategoryNews;

        if (currentCategory.isEmpty()) {
            // Should not happen if bottom nav is always selected, but as a fallback
            currentCategoryNews = allNewsItems;
        } else {
            currentCategoryNews = allNewsItems.stream()
                    .filter(news -> news.getCategory().equalsIgnoreCase(currentCategory))
                    .collect(Collectors.toList());
        }

        // Now, filter these category-specific news items by the search query
        List<NewsItem> filteredByQuery = currentCategoryNews.stream()
                .filter(news -> news.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                        news.getContent().toLowerCase().contains(query.toLowerCase())) // Search in title and content
                .collect(Collectors.toList());

        verticalNewsAdapter.setNewsList(filteredByQuery);
    }

    // Helper method to determine the currently selected category from the bottom navigation
    private String getSelectedCategoryFromBottomNav() {
        int selectedId = bottomNavigationView.getSelectedItemId();
        if (selectedId == R.id.navigation_sports) {
            return getString(R.string.sports);
        } else if (selectedId == R.id.navigation_academic) {
            return getString(R.string.academic);
        } else if (selectedId == R.id.navigation_events) {
            return getString(R.string.events);
        }
        return getString(R.string.sports); // Default to sports if no item is selected (should be avoided)
    }

    @Override
    public void onBackPressed() {
        // If the search toolbar is visible, hide it instead of exiting the activity
        if (searchToolbar.getVisibility() == View.VISIBLE) {
            searchToolbar.setVisibility(View.GONE);
            searchBar.setVisibility(View.VISIBLE);
            appCompatSearchView.setQuery("", false); // Clear any text in SearchView
            appCompatSearchView.clearFocus(); // Remove focus from SearchView
            // After hiding search, ensure the correct category list is shown
            filterNewsByCategory(getSelectedCategoryFromBottomNav());
            // Optionally hide keyboard
            // InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            // imm.hideSoftInputFromWindow(appCompatSearchView.getWindowToken(), 0);
        } else {
            // If search toolbar is not visible, proceed with default back press behavior
            super.onBackPressed();
        }
    }
}