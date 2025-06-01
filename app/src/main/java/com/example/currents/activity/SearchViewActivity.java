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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SearchViewActivity extends AppCompatActivity { // Removed NewsAdapter.OnNewsClickListener from implements

    private MaterialToolbar searchToolbar;
    private SearchView appCompatSearchView;
    private RecyclerView searchResultsRecyclerView;
    private NewsAdapter searchResultsAdapter;

    private List<NewsItem> allAvailableNewsItems; // List to search from, passed by HomeActivity

    // Request code for starting this activity and getting a result back
    public static final int SEARCH_REQUEST_CODE = 1;
    public static final String EXTRA_ALL_NEWS_ITEMS = "extra_all_news_items"; // Key to receive all news
    public static final String EXTRA_SELECTED_NEWS_ITEM = "extra_selected_news_item"; // Key to send back selected news

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_view);

        searchToolbar = findViewById(R.id.searchToolbar);
        appCompatSearchView = findViewById(R.id.appCompatSearchView);
        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView);

        setSupportActionBar(searchToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        searchToolbar.setNavigationOnClickListener(v -> onBackPressed());

        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Retrieve the list of all news items from the Intent
        // Use getSerializableExtra and cast it
        if (getIntent().hasExtra(EXTRA_ALL_NEWS_ITEMS)) {
            allAvailableNewsItems = (List<NewsItem>) getIntent().getSerializableExtra(EXTRA_ALL_NEWS_ITEMS);
        } else {
            allAvailableNewsItems = new ArrayList<>(); // Fallback if no data received
            Toast.makeText(this, "No news data available for search.", Toast.LENGTH_LONG).show();
        }

        // Initialize adapter with an empty list initially, and a custom click listener for this activity
        searchResultsAdapter = new NewsAdapter(new ArrayList<>(), new NewsAdapter.OnNewsClickListener() {
            @Override
            public void onNewsClick(NewsItem newsItem) {
                // When a search result is clicked, send it back to HomeActivity
                Intent resultIntent = new Intent();
                resultIntent.putExtra(EXTRA_SELECTED_NEWS_ITEM, newsItem);
                setResult(RESULT_OK, resultIntent);
                finish(); // Close SearchViewActivity
            }
        });
        searchResultsRecyclerView.setAdapter(searchResultsAdapter);

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
            searchResultsAdapter.setNewsList(new ArrayList<>());
            return false;
        });
    }

    private void performSearch(String query) {
        if (allAvailableNewsItems == null || allAvailableNewsItems.isEmpty()) {
            searchResultsAdapter.setNewsList(new ArrayList<>());
            return;
        }

        if (query == null || query.trim().isEmpty()) {
            searchResultsAdapter.setNewsList(new ArrayList<>()); // Clear results if query is empty
            return;
        }

        List<NewsItem> filteredList = allAvailableNewsItems.stream()
                .filter(news -> news.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                        news.getContent().toLowerCase().contains(query.toLowerCase()) ||
                        news.getCategory().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());

        searchResultsAdapter.setNewsList(filteredList);

        if (filteredList.isEmpty() && !query.trim().isEmpty()) {
            // Optional: Provide feedback if no results
            // Toast.makeText(this, "No results found for \"" + query + "\"", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        hideKeyboard(appCompatSearchView);
        setResult(RESULT_CANCELED); // Set result to CANCELED if user just backs out without selecting
        super.onBackPressed();
    }

    private void showKeyboard(View view) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}