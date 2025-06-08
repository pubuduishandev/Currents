package com.example.currents.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.currents.R;
import com.example.currents.adapter.NewsAdapter;
import com.example.currents.model.NewsItem;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class SearchViewActivity extends AppCompatActivity {
    // UI components
    private MaterialToolbar searchToolbar;
    private SearchView appCompatSearchView;
    private RecyclerView searchResultsRecyclerView;
    private NewsAdapter searchResultsAdapter;
    private ChipGroup categoryChipGroup;

    // To store the currently selected category
    private String selectedCategoryFilter = "";

    // To hold all available news items and the currently filtered items
    private List<NewsItem> allAvailableNewsItems;

    // New list to hold items filtered by category
    private List<NewsItem> currentFilteredNewsItems;

    // Request code for search activity result
    public static final int SEARCH_REQUEST_CODE = 1;

    // Intent extras for passing data
    public static final String EXTRA_ALL_NEWS_ITEMS = "extra_all_news_items";
    public static final String EXTRA_SELECTED_NEWS_ITEM = "extra_selected_news_item";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Call the superclass method
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_view);

        // Initialize UI components
        searchToolbar = findViewById(R.id.searchToolbar);
        appCompatSearchView = findViewById(R.id.appCompatSearchView);
        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView);
        categoryChipGroup = findViewById(R.id.categoryChipGroup); // Initialize ChipGroup

        setSupportActionBar(searchToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        searchToolbar.setNavigationOnClickListener(v -> onBackPressed());

        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        if (getIntent().hasExtra(EXTRA_ALL_NEWS_ITEMS)) {
            allAvailableNewsItems = (List<NewsItem>) getIntent().getSerializableExtra(EXTRA_ALL_NEWS_ITEMS);
            // Initially, allAvailableNewsItems are also the current filtered items before any category/search filter
            currentFilteredNewsItems = new ArrayList<>(allAvailableNewsItems);
        } else {
            allAvailableNewsItems = new ArrayList<>();
            currentFilteredNewsItems = new ArrayList<>();
            Toast.makeText(this, R.string.no_news, Toast.LENGTH_LONG).show();
        }

        searchResultsAdapter = new NewsAdapter(new ArrayList<>(), new NewsAdapter.OnNewsClickListener() {
            @Override
            public void onNewsClick(NewsItem newsItem) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra(EXTRA_SELECTED_NEWS_ITEM, newsItem);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
        searchResultsRecyclerView.setAdapter(searchResultsAdapter);

        // Set up ChipGroup listener
        setupChipGroup();

        // Initial search to display all news if no query, or apply existing query if activity recreated
        // It's better to call performSearch with an empty string initially to show all results
        // or re-run the last query if that's your desired behavior.
        // For now, let's assume an empty query shows nothing until typed.
        // If you want to show all results initially, call performSearch(""); here.
        // Or if you want to show results based on an initial selected category, call filterByCategory("initial_category");
        // For simplicity, let's start with an empty list for search results.
        // show all news initially based on current category selection (which is none by default)
        performSearch(appCompatSearchView.getQuery().toString()); // Use the current query text (might be empty)

        appCompatSearchView.onActionViewExpanded();
        appCompatSearchView.requestFocusFromTouch();
        showKeyboard(appCompatSearchView);

        appCompatSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                hideKeyboard(appCompatSearchView);
                appCompatSearchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                performSearch(newText);
                return true;
            }
        });

        appCompatSearchView.setOnCloseListener(() -> {
            // When search is cleared, clear category filter too, and update results
            clearCategorySelection(); // Uncheck all chips
            performSearch(""); // Show all results (or no results depending on your preference)
            return false;
        });
    }

    // Method to set up the ChipGroup for category filtering
    private void setupChipGroup() {
        categoryChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                selectedCategoryFilter = ""; // No chip selected
            } else {
                Chip selectedChip = findViewById(checkedIds.get(0));
                selectedCategoryFilter = selectedChip.getText().toString();
            }
            // Re-run the search with the updated category filter
            performSearch(appCompatSearchView.getQuery().toString());
        });
    }

    // Method to add chips dynamically based on available categories in news items
    private void clearCategorySelection() {
        categoryChipGroup.clearCheck();
        selectedCategoryFilter = "";
    }

    // Method to perform the search based on the current query and selected category
    private void performSearch(String query) {
        if (allAvailableNewsItems == null || allAvailableNewsItems.isEmpty()) {
            searchResultsAdapter.setNewsList(new ArrayList<>());
            return;
        }

        // 1. Filter by category first (if a chip is selected)
        List<NewsItem> categoryFilteredList;
        if (!selectedCategoryFilter.isEmpty()) {
            categoryFilteredList = allAvailableNewsItems.stream()
                    .filter(news -> news.getCategory() != null &&
                            news.getCategory().equalsIgnoreCase(selectedCategoryFilter))
                    .collect(Collectors.toList());
        } else {
            // If no category is selected, start with all available items
            categoryFilteredList = new ArrayList<>(allAvailableNewsItems);
        }

        // 2. Then, apply text search on the category-filtered list
        List<NewsItem> finalFilteredList;
        if (query == null || query.trim().isEmpty()) {
            // If search query is empty, show all items from the category-filtered list
            finalFilteredList = new ArrayList<>(categoryFilteredList);
        } else {
            String lowerCaseQuery = query.toLowerCase(Locale.getDefault());
            finalFilteredList = categoryFilteredList.stream()
                    .filter(news -> (news.getTitle() != null && news.getTitle().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery)) ||
                            (news.getContent() != null && news.getContent().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery)) ||
                            // Also search in category if it's not the primary filter
                            (selectedCategoryFilter.isEmpty() && news.getCategory() != null && news.getCategory().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery)))
                    .collect(Collectors.toList());
        }

        searchResultsAdapter.setNewsList(finalFilteredList);

        if (finalFilteredList.isEmpty() && (!query.trim().isEmpty() || !selectedCategoryFilter.isEmpty())) {
            // Optional: Provide feedback if no results after applying filters
            // Toast.makeText(this, "No results found for your filters.", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to handle back press and hide keyboard
    @Override
    public void onBackPressed() {
        hideKeyboard(appCompatSearchView);
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    // Helper methods to show and hide the keyboard
    private void showKeyboard(View view) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }

    // Method to hide the keyboard
    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}