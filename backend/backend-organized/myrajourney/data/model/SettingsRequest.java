package com.example.myrajourney.data.model;

import com.google.gson.annotations.SerializedName;

public class SettingsRequest {
    @SerializedName("notifications_enabled")
    private boolean notificationsEnabled;

    @SerializedName("theme")
    private String theme;

    public SettingsRequest(boolean notificationsEnabled, String theme) {
        this.notificationsEnabled = notificationsEnabled;
        this.theme = theme;
    }
}






