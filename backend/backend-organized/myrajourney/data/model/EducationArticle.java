package com.example.myrajourney.data.model;

import com.google.gson.annotations.SerializedName;

public class EducationArticle {
    @SerializedName("id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("slug")
    private String slug;

    @SerializedName("content")
    private String content;

    @SerializedName("content_html")
    private String contentHtml;

    @SerializedName("thumbnail")
    private String thumbnail;

    @SerializedName("category")
    private String category;

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getSlug() { return slug; }
    public String getContent() { return content; }
    public String getThumbnail() { return thumbnail; }
    public String getCategory() { return category; }

    // Helper: returns HTML content if available, otherwise falls back to plain content
    public String getContentHtml() {
        return contentHtml != null && !contentHtml.isEmpty() ? contentHtml : content;
    }
}