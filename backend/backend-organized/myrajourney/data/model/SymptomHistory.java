package com.example.myrajourney.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Model for displaying symptom history in doctor's patient record
 */
public class SymptomHistory {
    @SerializedName("id")
    private int id;

    @SerializedName("patient_id")
    private int patientId;

    @SerializedName("date")
    private String date;

    @SerializedName("pain_level")
    private int painLevel;

    @SerializedName("stiffness_level")
    private int stiffnessLevel;

    @SerializedName("fatigue_level")
    private int fatigueLevel;

    @SerializedName("joint_count")
    private Integer jointCount;

    @SerializedName("notes")
    private String notes;

    @SerializedName("created_at")
    private String createdAt;

    // Default constructor
    public SymptomHistory() {}

    // Getters
    public int getId() {
        return id;
    }

    public int getPatientId() {
        return patientId;
    }

    public String getDate() {
        return date;
    }

    public int getPainLevel() {
        return painLevel;
    }

    public int getStiffnessLevel() {
        return stiffnessLevel;
    }

    public int getFatigueLevel() {
        return fatigueLevel;
    }

    public Integer getJointCount() {
        return jointCount;
    }

    public String getNotes() {
        return notes;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setPainLevel(int painLevel) {
        this.painLevel = painLevel;
    }

    public void setStiffnessLevel(int stiffnessLevel) {
        this.stiffnessLevel = stiffnessLevel;
    }

    public void setFatigueLevel(int fatigueLevel) {
        this.fatigueLevel = fatigueLevel;
    }

    public void setJointCount(Integer jointCount) {
        this.jointCount = jointCount;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    // Helper methods for display
    public String getPainLevelText() {
        if (painLevel == 0) return "No Pain";
        if (painLevel <= 3) return "Mild (" + painLevel + "/10)";
        if (painLevel <= 6) return "Moderate (" + painLevel + "/10)";
        return "Severe (" + painLevel + "/10)";
    }

    public String getStiffnessLevelText() {
        if (stiffnessLevel == 0) return "No Stiffness";
        if (stiffnessLevel <= 3) return "Mild (" + stiffnessLevel + "/10)";
        if (stiffnessLevel <= 6) return "Moderate (" + stiffnessLevel + "/10)";
        return "Severe (" + stiffnessLevel + "/10)";
    }

    public String getFatigueLevelText() {
        if (fatigueLevel == 0) return "No Fatigue";
        if (fatigueLevel <= 3) return "Mild (" + fatigueLevel + "/10)";
        if (fatigueLevel <= 6) return "Moderate (" + fatigueLevel + "/10)";
        return "Severe (" + fatigueLevel + "/10)";
    }

    public String getJointCountText() {
        if (jointCount == null || jointCount == 0) return "No joints affected";
        if (jointCount == 1) return "1 joint affected";
        return jointCount + " joints affected";
    }

    public String getFormattedDate() {
        if (date == null) return "";
        try {
            java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault());
            java.util.Date parsedDate = inputFormat.parse(date);
            return outputFormat.format(parsedDate);
        } catch (Exception e) {
            return date;
        }
    }
}