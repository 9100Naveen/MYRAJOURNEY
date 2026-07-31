package com.example.myrajourney.data.model;

import com.google.gson.annotations.SerializedName;

public class AppointmentRequest {

    @SerializedName("patient_id")
    private String patientId;

    @SerializedName("doctor_id")
    private String doctorId;

    @SerializedName("title")
    private String title;

    @SerializedName("reason")
    private String reason;

    @SerializedName("start_time")
    private String startTime;

    @SerializedName("end_time")
    private String endTime;


    // -------------------------
    // Full constructor
    // -------------------------
    public AppointmentRequest(String patientId, String doctorId, String title, String reason,
                              String startTime, String endTime) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.title = title;
        this.reason = reason;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // -------------------------
    // Getters
    // -------------------------
    public String getPatientId() {
        return patientId;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public String getTitle() {
        return title;
    }

    public String getReason() {
        return reason;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    // -------------------------
    // Setters
    // -------------------------
    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    // -------------------------
    // Optional helper constructor (if backend auto-assigns patient/doctor)
    // -------------------------
    public AppointmentRequest(String title, String reason, String startTime, String endTime) {
        this.title = title;
        this.reason = reason;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
