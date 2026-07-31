package com.example.myrajourney.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class DoctorOverview {
    @SerializedName("active_patients")
    private int activePatients;

    @SerializedName("pending_appointments")
    private int pendingAppointments;

    // Add these if your API returns them, otherwise we map existing fields to the methods called in Dashboard
    @SerializedName("patients_count")
    private Integer patientsCount;

    @SerializedName("today_schedule")
    private List<Appointment> todaySchedule;

    // Getters used by Dashboard
    public Integer getPatientsCount() {
        // Fallback to activePatients if patientsCount is null
        return patientsCount != null ? patientsCount : activePatients;
    }

    public List<Appointment> getTodaySchedule() {
        return todaySchedule;
    }

    // Existing getters
    public int getActivePatients() { return activePatients; }
    public int getPendingAppointments() { return pendingAppointments; }
}