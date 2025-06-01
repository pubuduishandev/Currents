package com.example.currents.model;

import java.io.Serializable;

public class NewsItem implements Serializable {
    private String title;
    private String postedDate;
    private int imageResId; // Resource ID for the image (e.g., R.drawable.news_placeholder)
    private String category; // "Sports", "Academic", "Events"
    private String content; // Add this field for the full news content

    // Constructor with content
    public NewsItem(String title, String postedDate, int imageResId, String category, String content) {
        this.title = title;
        this.postedDate = postedDate;
        this.imageResId = imageResId;
        this.category = category;
        this.content = content;
    }

    // Constructor without content (for backwards compatibility if needed, but the one above is preferred)
    public NewsItem(String title, String postedDate, int imageResId, String category) {
        this(title, postedDate, imageResId, category, ""); // Default empty content
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getPostedDate() {
        return postedDate;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getCategory() {
        return category;
    }

    // Add getter for content
    public String getContent() {
        return content;
    }
}