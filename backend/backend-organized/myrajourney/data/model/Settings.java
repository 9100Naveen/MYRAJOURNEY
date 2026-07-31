package com.example.myrajourney.data.model;

import com.google.gson.annotations.SerializedName;

public class Settings {
    @SerializedName("notifications_enabled")
    private boolean notificationsEnabled;

    @SerializedName("theme")
    private String theme;

    public boolean isNotificationsEnabled() { return notificationsEnabled; }
    public String getTheme() { return theme; }
}






