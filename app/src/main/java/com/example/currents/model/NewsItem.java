// com.example.currents.model.NewsItem.java
package com.example.currents.model;

import java.io.Serializable;

public class NewsItem implements Serializable {
    private String id; // <--- ADD THIS FIELD
    private String title;
    private String postedDate;
    private int imageResId;
    private String category;
    private String content;

    // <--- UPDATE THIS CONSTRUCTOR
    public NewsItem(String id, String title, String postedDate, int imageResId, String category, String content) {
        this.id = id; // <--- INITIALIZE THE NEW ID FIELD
        this.title = title;
        this.postedDate = postedDate;
        this.imageResId = imageResId;
        this.category = category;
        this.content = content;
    }

    // <--- ADD THIS GETTER METHOD
    public String getId() {
        return id;
    }

    // You might also want a setter if needed, but not strictly necessary for this use case
    public void setId(String id) {
        this.id = id;
    }

    // Keep your existing getters for other fields
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

    // Optionally, override toString() for easier debugging
    @Override
    public String toString() {
        return "NewsItem{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", category='" + category + '\'' +
                '}';
    }
}