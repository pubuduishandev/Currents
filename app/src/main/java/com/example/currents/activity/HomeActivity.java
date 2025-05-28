package com.example.currents.activity;

import android.os.Bundle;
import android.view.View; // Import View
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView; // Import SearchView
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.currents.R;
import com.google.android.material.appbar.MaterialToolbar; // Import MaterialToolbar
import com.google.android.material.search.SearchBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView horizontalCardRecyclerView;
    private RecyclerView verticalCardRecyclerView;
    private SearchBar searchBar;
    private BottomNavigationView bottomNavigationView;
    private TextView selectedNameTextView;
    private MaterialToolbar searchToolbar; // Declare MaterialToolbar
    private SearchView appCompatSearchView; // Declare SearchView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize RecyclerViews
        horizontalCardRecyclerView = findViewById(R.id.horizontalCardRecyclerView);
        verticalCardRecyclerView = findViewById(R.id.verticalCardRecyclerView);

        // Initialize SearchBar, BottomNavigationView, TextView
        searchBar = findViewById(R.id.searchBar);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Initialize MaterialToolbar and SearchView
        searchToolbar = findViewById(R.id.searchToolbar);
        appCompatSearchView = findViewById(R.id.appCompatSearchView);

        // Set up LinearLayoutManager for horizontal RecyclerView
        horizontalCardRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        // You'll need to set an adapter for horizontalCardRecyclerView here later

        // Set up LinearLayoutManager for vertical RecyclerView
        verticalCardRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        // You'll need to set an adapter for verticalCardRecyclerView here later

        // --- SearchBar and SearchView Logic ---

        // Make SearchBar click listener to reveal SearchView
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
            // You might want to hide keyboard here too
            // InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            // imm.hideSoftInputFromWindow(appCompatSearchView.getWindowToken(), 0);
        });

        // Implement SearchView query text listener
        appCompatSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Toast.makeText(HomeActivity.this, "Searching for: " + query, Toast.LENGTH_SHORT).show();
                // Perform your search operation here
                // After search, you might want to hide the keyboard and possibly the search view
                // appCompatSearchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Filter your data as the user types
                // Log.d("SearchView", "Query text changed: " + newText);
                return false;
            }
        });

        // Optional: Listen for close button on SearchView
        appCompatSearchView.setOnCloseListener(() -> {
            // This is triggered when the 'x' icon is pressed if iconifiedByDefault is true
            // Since we set iconifiedByDefault to false, this might not be strictly needed for basic close
            // But good to have if you later decide to allow collapsing
            return false;
        });

        // --- Existing SearchBar Menu Item Clicks ---
        searchBar.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.read_news) {
                Toast.makeText(HomeActivity.this, "Read News Temp", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.developer_info) {
                Toast.makeText(HomeActivity.this, "Developer Info", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.profile) {
                Toast.makeText(HomeActivity.this, "View Profile", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });


        // --- BottomNavigationView Logic ---
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_sports) {
                searchBar.setHint(getString(R.string.sports));
                return true;
            } else if (itemId == R.id.navigation_academic) {
                searchBar.setHint(getString(R.string.academic));
                return true;
            } else if (itemId == R.id.navigation_events) {
                searchBar.setHint(getString(R.string.events));
                return true;
            }
            return false;
        });

        // Set initial SearchBar hint and TextView text
        searchBar.setHint(getString(R.string.sports));
    }

    @Override
    public void onBackPressed() {
        // If the search toolbar is visible, hide it instead of exiting the activity
        if (searchToolbar.getVisibility() == View.VISIBLE) {
            searchToolbar.setVisibility(View.GONE);
            searchBar.setVisibility(View.VISIBLE);
            appCompatSearchView.setQuery("", false);
            appCompatSearchView.clearFocus();
            // Optionally hide keyboard
            // InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            // imm.hideSoftInputFromWindow(appCompatSearchView.getWindowToken(), 0);
        } else {
            super.onBackPressed(); // Let the system handle back press
        }
    }
}