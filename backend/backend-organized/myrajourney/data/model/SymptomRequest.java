package com.example.myrajourney.data.model;

import com.google.gson.annotations.SerializedName;

public class SymptomRequest {
    @SerializedName("patient_id")
    private String patientId;

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

    // ✅ Constructor matching the 3-arg usage
    public SymptomRequest(String patientId, String date, int painLevel) {
        this.patientId = patientId;
        this.date = date;
        this.painLevel = painLevel;
    }

    // Setters
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

    // Getters
    public String getPatientId() {
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
}