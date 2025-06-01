package com.example.currents.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.currents.R;
import com.example.currents.adapter.NewsAdapter;
import com.example.currents.adapter.CarouselNewsAdapter; // Import the new adapter
import com.example.currents.model.NewsItem;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.search.SearchBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HomeActivity extends AppCompatActivity implements NewsAdapter.OnNewsClickListener {

    private RecyclerView horizontalCardRecyclerView;
    private RecyclerView verticalCardRecyclerView;
    private SearchBar searchBar;
    private BottomNavigationView bottomNavigationView;
    private MaterialToolbar searchToolbar;
    private SearchView appCompatSearchView;

    private NewsAdapter verticalNewsAdapter;
    private CarouselNewsAdapter horizontalNewsAdapter; // Change type to CarouselNewsAdapter
    private List<NewsItem> allNewsItems;
    private List<NewsItem> savedNewsItems; // List for saved news

    public static final String EXTRA_NEWS_TITLE = "extra_news_title";
    public static final String EXTRA_NEWS_DATE = "extra_news_date";
    public static final String EXTRA_NEWS_IMAGE_RES_ID = "extra_news_image_res_id";
    public static final String EXTRA_NEWS_CONTENT = "extra_news_content";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        horizontalCardRecyclerView = findViewById(R.id.horizontalCardRecyclerView);
        verticalCardRecyclerView = findViewById(R.id.verticalCardRecyclerView);

        searchBar = findViewById(R.id.searchBar);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        searchToolbar = findViewById(R.id.searchToolbar);
        appCompatSearchView = findViewById(R.id.appCompatSearchView);

        // Set up LinearLayoutManager for horizontal RecyclerView
        horizontalCardRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        // Initialize horizontal RecyclerView adapter with CarouselNewsAdapter
        savedNewsItems = getSavedNewsData(); // Retrieve saved news data
        horizontalNewsAdapter = new CarouselNewsAdapter(savedNewsItems, this); // Use 'this' as OnNewsClickListener
        horizontalCardRecyclerView.setAdapter(horizontalNewsAdapter);

        // Set up LinearLayoutManager for vertical RecyclerView
        verticalCardRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        // --- Sample News Data ---
        allNewsItems = new ArrayList<>();
        // Add sample news items for Sports
        allNewsItems.add(new NewsItem("Champions League Final Recap", "2024-05-28", R.drawable.news_placeholder, "Sports", getString(R.string.sample_news_content)));
        allNewsItems.add(new NewsItem("NBA Playoffs: Game 7 Thriller", "2024-05-27", R.drawable.news_placeholder, "Sports", getString(R.string.sample_news_content)));
        allNewsItems.add(new NewsItem("Local Marathon Results", "2024-05-26", R.drawable.news_placeholder, "Sports", getString(R.string.sample_news_content)));
        allNewsItems.add(new NewsItem("Cricket World Cup Preparations", "2024-05-25", R.drawable.news_placeholder, "Sports", getString(R.string.sample_news_content)));

        // Add sample news items for Academic
        allNewsItems.add(new NewsItem("New AI Research Published", "2024-05-29", R.drawable.news_placeholder, "Academic", getString(R.string.sample_news_content)));
        allNewsItems.add(new NewsItem("University Hosts Tech Symposium", "2024-05-28", R.drawable.news_placeholder, "Academic", getString(R.string.sample_news_content)));
        allNewsItems.add(new NewsItem("Scholarship Opportunities for Students", "2024-05-27", R.drawable.news_placeholder, "Academic", getString(R.string.sample_news_content)));
        allNewsItems.add(new NewsItem("Breakthrough in Medical Science", "2024-05-26", R.drawable.news_placeholder, "Academic", getString(R.string.sample_news_content)));

        // Add sample news items for Events
        allNewsItems.add(new NewsItem("Summer Music Festival Announced", "2024-05-30", R.drawable.news_placeholder, "Events", getString(R.string.sample_news_content)));
        allNewsItems.add(new NewsItem("Community Art Fair This Weekend", "2024-05-29", R.drawable.news_placeholder, "Events", getString(R.string.sample_news_content)));
        allNewsItems.add(new NewsItem("Annual Food Festival Dates", "2024-05-28", R.drawable.news_placeholder, "Events", getString(R.string.sample_news_content)));
        allNewsItems.add(new NewsItem("Local Charity Run Success", "2024-05-27", R.drawable.news_placeholder, "Events", getString(R.string.sample_news_content)));


        // Initialize vertical RecyclerView adapter
        verticalNewsAdapter = new NewsAdapter(new ArrayList<>(), this);
        verticalCardRecyclerView.setAdapter(verticalNewsAdapter);


        // --- SearchBar and SearchView Logic ---
        searchBar.setOnClickListener(v -> {
            searchBar.setVisibility(View.GONE);
            searchToolbar.setVisibility(View.VISIBLE);
            appCompatSearchView.setIconified(false);
            appCompatSearchView.requestFocus();
        });

        searchToolbar.setNavigationOnClickListener(v -> {
            searchToolbar.setVisibility(View.GONE);
            searchBar.setVisibility(View.VISIBLE);
            appCompatSearchView.setQuery("", false);
            appCompatSearchView.clearFocus();
            filterNewsByCategory(getSelectedCategoryFromBottomNav());
        });

        appCompatSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Toast.makeText(HomeActivity.this, "Searching for: " + query, Toast.LENGTH_SHORT).show();
                filterNewsByQuery(query);
                appCompatSearchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // If you want live search, uncomment and adjust this block
                // if (!newText.isEmpty()) {
                //     filterNewsByQuery(newText);
                // } else {
                //     filterNewsByCategory(getSelectedCategoryFromBottomNav());
                // }
                return false;
            }
        });

        appCompatSearchView.setOnCloseListener(() -> {
            appCompatSearchView.setQuery("", false);
            appCompatSearchView.clearFocus();
            filterNewsByCategory(getSelectedCategoryFromBottomNav());
            return false;
        });

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

        // Set initial state
        bottomNavigationView.setSelectedItemId(R.id.navigation_sports);
        searchBar.setHint(getString(R.string.sports));
        filterNewsByCategory(getString(R.string.sports));
    }

    @Override
    public void onNewsClick(NewsItem newsItem) {
        Intent intent = new Intent(HomeActivity.this, ReadNewsActivity.class);
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
            filteredList = allNewsItems.stream()
                    .filter(news -> news.getCategory().equalsIgnoreCase(category))
                    .collect(Collectors.toList());
        }
        verticalNewsAdapter.setNewsList(filteredList);
    }

    private void filterNewsByQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            filterNewsByCategory(getSelectedCategoryFromBottomNav());
            return;
        }

        String currentCategory = getSelectedCategoryFromBottomNav();
        List<NewsItem> currentCategoryNews;

        if (currentCategory.isEmpty()) {
            currentCategoryNews = allNewsItems;
        } else {
            currentCategoryNews = allNewsItems.stream()
                    .filter(news -> news.getCategory().equalsIgnoreCase(currentCategory))
                    .collect(Collectors.toList());
        }

        List<NewsItem> filteredByQuery = currentCategoryNews.stream()
                .filter(news -> news.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                        news.getContent().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());

        verticalNewsAdapter.setNewsList(filteredByQuery);
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
        return getString(R.string.sports);
    }

    @Override
    public void onBackPressed() {
        if (searchToolbar.getVisibility() == View.VISIBLE) {
            searchToolbar.setVisibility(View.GONE);
            searchBar.setVisibility(View.VISIBLE);
            appCompatSearchView.setQuery("", false);
            appCompatSearchView.clearFocus();
            filterNewsByCategory(getSelectedCategoryFromBottomNav());
        } else {
            super.onBackPressed();
        }
    }

    // --- Mock method to retrieve saved news data ---
    private List<NewsItem> getSavedNewsData() {
        List<NewsItem> saved = new ArrayList<>();
        // Add some sample saved news items for demonstration
        saved.add(new NewsItem("Local Charity Run Success", "2024-05-27", R.drawable.news_placeholder, "Events", getString(R.string.sample_news_content)));
        saved.add(new NewsItem("NBA Playoffs: Game 7 Thriller", "2024-05-27", R.drawable.news_placeholder, "Sports", getString(R.string.sample_news_content)));
        saved.add(new NewsItem("New AI Research Published", "2024-05-29", R.drawable.news_placeholder, "Academic", getString(R.string.sample_news_content)));
        saved.add(new NewsItem("Summer Music Festival Announced", "2024-05-30", R.drawable.news_placeholder, "Events", getString(R.string.sample_news_content)));
        saved.add(new NewsItem("Breakthrough in Medical Science", "2024-05-26", R.drawable.news_placeholder, "Academic", getString(R.string.sample_news_content)));
        return saved;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (horizontalNewsAdapter != null) {
            savedNewsItems = getSavedNewsData();
            horizontalNewsAdapter.setNewsList(savedNewsItems);
        }
    }
}