package com.example.myrajourney.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PatientOverview {

    @SerializedName("next_appointment")
    private Appointment nextAppointment;

    @SerializedName("unread_notifications")
    private int unreadNotifications;

    @SerializedName("das28_score")
    private double das28Score;

    @SerializedName("pain_level")
    private int painLevel;

    @SerializedName("patient_name")
    private String patientName;

    @SerializedName("recent_reports")
    private List<Report> recentReports;

    @SerializedName("latest_metrics")
    private List<Object> latestMetrics; // Replace later with your real Metric model

    // --- Getters ---
    public Appointment getNextAppointment() {
        return nextAppointment;
    }

    public int getUnreadNotifications() {
        return unreadNotifications;
    }

    public double getDas28Score() {
        return das28Score;
    }

    public int getPainLevel() {
        return painLevel;
    }

    public String getPatientName() {
        return patientName;
    }

    public List<Report> getRecentReports() {
        return recentReports;
    }

    public List<Object> getLatestMetrics() {
        return latestMetrics;
    }
}
