package com.example.myrajourney.data.model;

import com.google.gson.annotations.SerializedName;

public class ActiveRequest {
    @SerializedName("active")
    private boolean active;

    public ActiveRequest(boolean active) {
        this.active = active;
    }
}






