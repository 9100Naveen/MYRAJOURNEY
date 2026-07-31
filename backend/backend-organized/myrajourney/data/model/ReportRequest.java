package com.example.myrajourney.data.model;

import com.google.gson.annotations.SerializedName;

public class ReportRequest {

    @SerializedName("patient_id")
    private String patientId;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    public ReportRequest(String patientId, String title, String description) {
        this.patientId = patientId;
        this.title = title;
        this.description = description;
    }
}
