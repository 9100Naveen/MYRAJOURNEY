package com.example.myrajourney.data.model;

import com.google.gson.annotations.SerializedName;

public class Report {

    @SerializedName("id")
    private String id;

    @SerializedName("patient_id")
    private String patientId;

    @SerializedName("patient_name")
    private String patientName;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("file_url")
    private String fileUrl;

    @SerializedName("status")
    private String status;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    // Default constructor
    public Report() {}

    // Basic constructor for patient uploads
    public Report(String title, String fileUrl, String createdAt) {
        this.title = title;
        this.fileUrl = fileUrl;
        this.createdAt = createdAt;
        this.status = "Pending";
    }

    // ------------------- Getters & Setters -------------------

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public String getStatus() { return status == null ? "Pending" : status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    // Convenience Methods
    public String getDate() { return createdAt; }
    public String getName() { return title; }
    public String getReportType() { return title; }
}
