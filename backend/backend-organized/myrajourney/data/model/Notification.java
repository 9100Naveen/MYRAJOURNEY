package com.example.myrajourney.data.model;

import com.google.gson.annotations.SerializedName;

public class Notification {

    @SerializedName("id")
    private String id;

    @SerializedName("title")
    private String title;

    // Backend field is `body`, not `message`
    @SerializedName("body")
    private String body;

    // Backend sends `read_at`: NULL = unread → convert to boolean
    @SerializedName("read_at")
    private String readAt;

    @SerializedName("created_at")
    private String createdAt;

    // ----------- GETTERS ----------- //

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    // Adapter uses getBody()
    public String getBody() {
        return body;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    // TRUE = notification already read
    public boolean isRead() {
        return readAt != null;
    }

    // TRUE = unread
    public boolean isUnread() {
        return readAt == null;
    }
}
