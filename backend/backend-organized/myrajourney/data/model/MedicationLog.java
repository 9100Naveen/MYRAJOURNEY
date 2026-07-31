package com.example.myrajourney.data.model;

import com.google.gson.annotations.SerializedName;

public class MedicationLog {
    @SerializedName("id")
    private int id;

    @SerializedName("status")
    private String status;

    @SerializedName("taken_at")
    private String takenAt;

    public int getId() { return id; }
    public String getStatus() { return status; }
    public String getTakenAt() { return takenAt; }
}