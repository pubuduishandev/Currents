package com.example.currents.activity;

import android.content.Intent; // Import Intent
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.example.currents.R;
import com.example.currents.adapter.NewsAdapter;
import com.example.currents.model.NewsItem;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// Implement NewsAdapter.OnNewsClickListener
public class SavedNewsActivity extends AppCompatActivity implements NewsAdapter.OnNewsClickListener {

    private Toolbar toolbar;
    private ChipGroup categoryChipGroup;
    private RecyclerView savedNewsRecyclerView;
    private NewsAdapter newsAdapter;
    private List<NewsItem> allNewsItems;

    // These constants should be public static final and ideally in a utility class
    // or the activity that first defines them if they are exclusively used there.
    // For now, defining them here as per your HomeActivity's requirement.
    public static final String EXTRA_NEWS_TITLE = "extra_news_title";
    public static final String EXTRA_NEWS_DATE = "extra_news_date";
    public static final String EXTRA_NEWS_IMAGE_RES_ID = "extra_news_image_res_id";
    public static final String EXTRA_NEWS_CONTENT = "extra_news_content";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_news);

        toolbar = findViewById(R.id.savedNewsToolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        categoryChipGroup = findViewById(R.id.categoryChipGroup);
        savedNewsRecyclerView = findViewById(R.id.savedNewsRecyclerView);

        // Initialize all news items (sample data)
        allNewsItems = new ArrayList<>();
        // Add sample news items for Sports
        allNewsItems.add(new NewsItem("Local Football Match Result", "2024-05-30", R.drawable.news_placeholder, "Sports", getString(R.string.sample_news_content)));
        allNewsItems.add(new NewsItem("Basketball Tournament Highlights", "2024-05-29", R.drawable.news_placeholder, "Sports", getString(R.string.sample_news_content)));
        allNewsItems.add(new NewsItem("Athlete Prepares for Olympics", "2024-05-28", R.drawable.news_placeholder, "Sports", getString(R.string.sample_news_content)));

        // Add sample news items for Academic
        allNewsItems.add(new NewsItem("University Research Breakthrough", "2024-05-27", R.drawable.news_placeholder, "Academic", getString(R.string.sample_news_content)));
        allNewsItems.add(new NewsItem("New Course Offerings Announced", "2024-05-26", R.drawable.news_placeholder, "Academic", getString(R.string.sample_news_content)));
        allNewsItems.add(new NewsItem("Student Wins National Essay Contest", "2024-05-25", R.drawable.news_placeholder, "Academic", getString(R.string.sample_news_content)));

        // Add sample news items for Events
        allNewsItems.add(new NewsItem("Annual Tech Summit Dates", "2024-05-24", R.drawable.news_placeholder, "Events", getString(R.string.sample_news_content)));
        allNewsItems.add(new NewsItem("Music Festival Lineup Revealed", "2024-05-23", R.drawable.news_placeholder, "Events", getString(R.string.sample_news_content)));
        allNewsItems.add(new NewsItem("Community Fair This Weekend", "2024-05-22", R.drawable.news_placeholder, "Events", getString(R.string.sample_news_content)));

        // Initialize adapter with an empty list and pass 'this' as the listener
        newsAdapter = new NewsAdapter(new ArrayList<>(), this);
        savedNewsRecyclerView.setAdapter(newsAdapter);

        // Set up initial selection and filter to "All"
        Chip allChip = findViewById(R.id.chipAll);
        if (allChip != null) {
            allChip.setChecked(true);
            filterNewsByCategory("All"); // Show all news initially
        }

        // Set up listener for chip selection changes
        categoryChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                filterNewsByCategory("All"); // Default to showing all if somehow nothing is checked
            } else {
                Chip selectedChip = findViewById(checkedIds.get(0));
                if (selectedChip != null) {
                    filterNewsByCategory(selectedChip.getText().toString());
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.saved_news_toolbar_menu, menu); // Inflate the new menu
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_clear_bookmarks) {
            Toast.makeText(this, "All bookmarks cleared", Toast.LENGTH_SHORT).show();
            // TODO: Implement actual logic to clear saved news from your data source
            allNewsItems.clear(); // Clear the underlying data for this example
            filterNewsByCategory("All"); // Refresh the display (will now show empty)
            Chip allChip = findViewById(R.id.chipAll);
            if (allChip != null) {
                allChip.setChecked(true);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // --- NewsAdapter.OnNewsClickListener Implementation ---
    @Override
    public void onNewsClick(NewsItem newsItem) {
        // Start ReadNewsActivity and pass news data
        Intent intent = new Intent(SavedNewsActivity.this, ReadNewsActivity.class);
        intent.putExtra(EXTRA_NEWS_TITLE, newsItem.getTitle());
        intent.putExtra(EXTRA_NEWS_DATE, newsItem.getPostedDate());
        intent.putExtra(EXTRA_NEWS_IMAGE_RES_ID, newsItem.getImageResId());
        intent.putExtra(EXTRA_NEWS_CONTENT, newsItem.getContent()); // Pass the full content
        startActivity(intent);
    }

    private void filterNewsByCategory(String category) {
        List<NewsItem> filteredList;
        if (category == null || category.isEmpty() || category.equalsIgnoreCase("All")) {
            filteredList = new ArrayList<>(allNewsItems); // Show all news
        } else {
            filteredList = allNewsItems.stream()
                    .filter(news -> news.getCategory().equalsIgnoreCase(category))
                    .collect(Collectors.toList());
        }
        newsAdapter.setNewsList(filteredList);
    }
}