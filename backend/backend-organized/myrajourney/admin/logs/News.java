package com.example.myrajourney.admin.logs;

public class News {
    private int imageRes; // Private fields
    private String title;
    private String description;

    public News(int imageRes, String title, String description) {
        this.imageRes = imageRes;
        this.title = title;
        this.description = description;
    }

    // --- GETTERS (Required for Adapter) ---
    public int getImageRes() {
        return imageRes;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}