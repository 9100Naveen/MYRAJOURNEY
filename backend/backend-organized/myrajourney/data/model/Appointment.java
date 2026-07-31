package com.example.myrajourney.data.model;

import com.google.gson.annotations.SerializedName;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Appointment {

    @SerializedName("id")
    private String id;

    @SerializedName("doctor_name")
    private String doctorName;

    @SerializedName("patient_name")
    private String patientName;

    @SerializedName("date")
    private String date; // API sends YYYY-MM-DD (optional, sometimes null)

    @SerializedName("start_time")
    private String startTime; // API: "yyyy-MM-dd HH:mm:ss"

    @SerializedName("end_time")
    private String endTime;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("status")
    private String status;

    @SerializedName("appointment_type")
    private String appointmentType;

    @SerializedName("reason")
    private String reason;

    // Local formatted UI fields (NOT from API)
    private String timeSlot;
    private String formattedDate;        // ⭐ ADDED
    private String formattedTimeSlot;    // ⭐ ADDED

    // Default constructor (Retrofit)
    public Appointment() { }

    // Constructor used by DoctorScheduleActivity (kept for compatibility)
    public Appointment(String patientName, String startTime, String title, String date) {
        this.patientName = patientName;
        this.startTime = startTime;
        this.title = title;
        this.date = date;
        updateDerivedFields(); // ⭐ ADDED
    }

    // Constructor used by PatientAppointmentsActivity (kept)
    public Appointment(String patientName, String doctorName, String date, String timeSlot, String title) {
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.date = date;
        this.timeSlot = timeSlot;
        this.title = title;
        updateDerivedFields(); // ⭐ ADDED
    }

    // -------------------------
    // UNIFIED PARSER HELPERS
    // -------------------------

    private void updateDerivedFields() {
        this.formattedDate = computeDate();
        this.formattedTimeSlot = computeTimeSlot();
    }

    private String computeDate() {
        try {
            if (startTime == null) return "";
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date d = input.parse(startTime);
            SimpleDateFormat output = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            return output.format(d);
        } catch (Exception e) {
            return date != null ? date : "";
        }
    }

    private String computeTimeSlot() {
        if (timeSlot != null) return timeSlot;

        if (startTime == null) return "";

        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date s = input.parse(startTime);

            SimpleDateFormat output = new SimpleDateFormat("h:mm a", Locale.getDefault());
            String start = output.format(s);

            String end = start;
            if (endTime != null) {
                Date e = input.parse(endTime);
                end = output.format(e);
            }

            return start + " - " + end;

        } catch (Exception e) {
            return "";
        }
    }

    // -------------------------
    // Getters
    // -------------------------

    public String getId() { return id; }
    public String getDoctorName() { return doctorName; }
    public String getPatientName() { return patientName; }
    public String getDate() { return date; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public String getAppointmentType() { return appointmentType; }
    public String getReason() { return reason; }

    // ⭐ NEW: Always return properly formatted date
    public String getFormattedDate() {
        if (formattedDate == null) updateDerivedFields();
        return formattedDate;
    }

    // ⭐ NEW: Always return proper timeSlot
    public String getFormattedTimeSlot() {
        if (formattedTimeSlot == null) updateDerivedFields();
        return formattedTimeSlot;
    }

    // Legacy alias
    public String getTime() {
        return getFormattedTimeSlot();
    }

    public String getTimeSlot() {
        return getFormattedTimeSlot();
    }

    // -------------------------
    // Setters
    // -------------------------

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDate(String date) {
        this.date = date;
        updateDerivedFields();
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
        updateDerivedFields();
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
        updateDerivedFields();
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
        updateDerivedFields();
    }

    public void setAppointmentType(String appointmentType) {
        this.appointmentType = appointmentType;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
