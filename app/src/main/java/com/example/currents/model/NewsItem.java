// com.example.currents.model.NewsItem.java
package com.example.currents.model;

import java.io.Serializable;

public class NewsItem implements Serializable {
    private String id;
    private String title;
    private String postedDate;
    private int imageResId; // Kept for placeholder fallback
    private String category;
    private String content;
    private String imageUrl; // NEW: Field to store image URL from Firebase Storage

    // UPDATED CONSTRUCTOR: Now includes imageUrl
    public NewsItem(String id, String title, String postedDate, int imageResId, String category, String content, String imageUrl) {
        this.id = id;
        this.title = title;
        this.postedDate = postedDate;
        this.imageResId = imageResId; // For default/placeholder image
        this.category = category;
        this.content = content;
        this.imageUrl = imageUrl; // Initialize imageUrl
    }

    // Existing constructor (optional, consider removing if all paths use the new one)
    public NewsItem(String id, String title, String postedDate, int imageResId, String category, String content) {
        this.id = id;
        this.title = title;
        this.postedDate = postedDate;
        this.imageResId = imageResId;
        this.category = category;
        this.content = content;
        this.imageUrl = null; // Default to null if not provided
    }

    // ADDED GETTER FOR imageUrl
    public String getImageUrl() {
        return imageUrl;
    }

    // ADDED SETTER FOR imageUrl (optional)
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    // Existing getters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "NewsItem{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", imageUrl='" + (imageUrl != null ? imageUrl : "N/A") + '\'' +
                '}';
    }
}