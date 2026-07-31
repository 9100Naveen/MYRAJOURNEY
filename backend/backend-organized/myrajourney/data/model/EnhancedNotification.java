package com.example.myrajourney.data.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Enhanced notification model for colorful, categorized notifications
 */
public class EnhancedNotification {
    
    public enum NotificationType {
        MEDICATION("Medication", "#4CAF50", "💊"),
        APPOINTMENT("Appointment", "#2196F3", "🩺"),
        EXERCISE("Exercise", "#FF9800", "🏃"),
        SYMPTOM("Symptom", "#F44336", "📊"),
        REPORT("Report", "#9C27B0", "📋"),
        ALERT("Alert", "#FF5722", "🚨"),
        INFO("Information", "#607D8B", "ℹ️");
        
        private final String displayName;
        private final String color;
        private final String icon;
        
        NotificationType(String displayName, String color, String icon) {
            this.displayName = displayName;
            this.color = color;
            this.icon = icon;
        }
        
        public String getDisplayName() { return displayName; }
        public String getColor() { return color; }
        public String getIcon() { return icon; }
    }
    
    private int id;
    private String title;
    private String message;
    private NotificationType type;
    private long timestamp;
    private boolean isRead;
    private String patientName;
    private int patientId;
    private String actionData; // Additional data for actions
    
    public EnhancedNotification() {
        this.timestamp = System.currentTimeMillis();
        this.isRead = false;
    }
    
    public EnhancedNotification(String title, String message, NotificationType type) {
        this();
        this.title = title;
        this.message = message;
        this.type = type;
    }
    
    public EnhancedNotification(String title, String message, NotificationType type, String patientName) {
        this(title, message, type);
        this.patientName = patientName;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    
    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }
    
    public String getActionData() { return actionData; }
    public void setActionData(String actionData) { this.actionData = actionData; }
    
    // Utility methods
    public String getFormattedTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
    
    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
    
    public String getRelativeTime() {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;
        
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (seconds < 60) {
            return "Just now";
        } else if (minutes < 60) {
            return minutes + " min ago";
        } else if (hours < 24) {
            return hours + " hr ago";
        } else if (days < 7) {
            return days + " day" + (days > 1 ? "s" : "") + " ago";
        } else {
            return getFormattedTimestamp();
        }
    }
    
    public String getDisplayMessage() {
        StringBuilder sb = new StringBuilder();
        
        if (type != null) {
            sb.append(type.getIcon()).append(" ");
        }
        
        if (patientName != null && !patientName.isEmpty()) {
            sb.append(patientName).append(": ");
        }
        
        sb.append(message);
        
        return sb.toString();
    }
    
    public String getFullDisplayText() {
        return getDisplayMessage() + "\n" + getFormattedTimestamp() + " at " + getFormattedTime();
    }
    
    // Factory methods for common notification types
    public static EnhancedNotification createMedicationNotification(String patientName, String medicationName) {
        return new EnhancedNotification(
            "Medication Taken",
            "took " + medicationName,
            NotificationType.MEDICATION,
            patientName
        );
    }
    
    public static EnhancedNotification createAppointmentNotification(String patientName, String appointmentType) {
        return new EnhancedNotification(
            "Appointment Scheduled",
            "scheduled " + appointmentType + " appointment",
            NotificationType.APPOINTMENT,
            patientName
        );
    }
    
    public static EnhancedNotification createExerciseNotification(String patientName, String exerciseName) {
        return new EnhancedNotification(
            "Exercise Completed",
            "completed " + exerciseName + " exercise session",
            NotificationType.EXERCISE,
            patientName
        );
    }
    
    public static EnhancedNotification createSymptomNotification(String patientName, String symptomType) {
        return new EnhancedNotification(
            "Symptom Logged",
            "logged " + symptomType + " symptoms",
            NotificationType.SYMPTOM,
            patientName
        );
    }
    
    public static EnhancedNotification createReportNotification(String patientName, String reportType) {
        return new EnhancedNotification(
            "Report Generated",
            reportType + " report is ready for " + patientName,
            NotificationType.REPORT,
            patientName
        );
    }
    
    public static EnhancedNotification createAlertNotification(String patientName, String alertMessage) {
        return new EnhancedNotification(
            "Medical Alert",
            patientName + ": " + alertMessage,
            NotificationType.ALERT,
            patientName
        );
    }
}