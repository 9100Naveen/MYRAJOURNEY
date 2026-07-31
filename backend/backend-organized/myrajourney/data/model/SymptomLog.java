package com.example.myrajourney.data.model;

import java.util.Date;

/**
 * Data model for symptom logging and tracking
 * Used for monitoring RA symptoms and disease activity
 */
public class SymptomLog {
    
    private String id;
    private String patientId;
    private String symptom;
    private int severity; // 1-10 scale
    private String description;
    private Date timestamp;
    private String location; // Body part/joint affected
    private String triggers; // Potential triggers
    private String reliefMethods; // What helped
    private int duration; // Duration in minutes
    private String mood; // Patient's mood during symptom
    
    // Constructors
    public SymptomLog() {
        this.timestamp = new Date();
    }
    
    public SymptomLog(String patientId, String symptom, int severity) {
        this();
        this.patientId = patientId;
        this.symptom = symptom;
        this.severity = severity;
    }
    
    public SymptomLog(String patientId, String symptom, int severity, String description) {
        this(patientId, symptom, severity);
        this.description = description;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getPatientId() {
        return patientId;
    }
    
    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }
    
    public String getSymptom() {
        return symptom;
    }
    
    public void setSymptom(String symptom) {
        this.symptom = symptom;
    }
    
    public int getSeverity() {
        return severity;
    }
    
    public void setSeverity(int severity) {
        this.severity = Math.max(1, Math.min(10, severity)); // Ensure 1-10 range
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Date getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getTriggers() {
        return triggers;
    }
    
    public void setTriggers(String triggers) {
        this.triggers = triggers;
    }
    
    public String getReliefMethods() {
        return reliefMethods;
    }
    
    public void setReliefMethods(String reliefMethods) {
        this.reliefMethods = reliefMethods;
    }
    
    public int getDuration() {
        return duration;
    }
    
    public void setDuration(int duration) {
        this.duration = duration;
    }
    
    public String getMood() {
        return mood;
    }
    
    public void setMood(String mood) {
        this.mood = mood;
    }
    
    // Utility methods
    public String getSeverityDescription() {
        switch (severity) {
            case 1:
            case 2:
                return "Mild";
            case 3:
            case 4:
                return "Mild-Moderate";
            case 5:
            case 6:
                return "Moderate";
            case 7:
            case 8:
                return "Moderate-Severe";
            case 9:
            case 10:
                return "Severe";
            default:
                return "Unknown";
        }
    }
    
    public boolean isHighSeverity() {
        return severity >= 7;
    }
    
    public boolean isModerateSeverity() {
        return severity >= 4 && severity <= 6;
    }
    
    public boolean isMildSeverity() {
        return severity <= 3;
    }
    
    public String getFormattedTimestamp() {
        if (timestamp == null) return "";
        
        java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("MMM dd, yyyy HH:mm");
        return formatter.format(timestamp);
    }
    
    public String getDurationDescription() {
        if (duration <= 0) return "Not specified";
        
        if (duration < 60) {
            return duration + " minutes";
        } else if (duration < 1440) { // Less than 24 hours
            int hours = duration / 60;
            int minutes = duration % 60;
            if (minutes == 0) {
                return hours + " hour" + (hours > 1 ? "s" : "");
            } else {
                return hours + "h " + minutes + "m";
            }
        } else {
            int days = duration / 1440;
            return days + " day" + (days > 1 ? "s" : "");
        }
    }
    
    @Override
    public String toString() {
        return String.format("SymptomLog{symptom='%s', severity=%d, location='%s', timestamp=%s}",
            symptom, severity, location, getFormattedTimestamp());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        SymptomLog that = (SymptomLog) obj;
        return id != null ? id.equals(that.id) : that.id == null;
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}