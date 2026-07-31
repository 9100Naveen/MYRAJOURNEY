package com.example.myrajourney.patient.medications.model;

import android.graphics.Color;

public class DailyProgress {
    
    private String date;
    private int totalMedications;
    private int takenMedications;
    private int skippedMedications;
    private int missedMedications;
    
    public DailyProgress() {
        this.totalMedications = 0;
        this.takenMedications = 0;
        this.skippedMedications = 0;
        this.missedMedications = 0;
    }
    
    public DailyProgress(int totalMedications, int takenMedications, 
                        int skippedMedications, int missedMedications) {
        this.totalMedications = totalMedications;
        this.takenMedications = takenMedications;
        this.skippedMedications = skippedMedications;
        this.missedMedications = missedMedications;
    }
    
    public float getAdherencePercentage() {
        if (totalMedications == 0) return 0;
        return ((float) takenMedications / totalMedications) * 100;
    }
    
    public int getProgressColor() {
        float percentage = getAdherencePercentage();
        if (percentage >= 80) {
            return Color.parseColor("#4CAF50"); // Green
        } else if (percentage >= 50) {
            return Color.parseColor("#FF9800"); // Orange
        } else {
            return Color.parseColor("#F44336"); // Red
        }
    }
    
    public String getProgressText() {
        return takenMedications + "/" + totalMedications + " taken";
    }
    
    // Getters and Setters
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    
    public int getTotalMedications() { return totalMedications; }
    public void setTotalMedications(int totalMedications) { 
        this.totalMedications = totalMedications; 
    }
    
    public int getTakenMedications() { return takenMedications; }
    public void setTakenMedications(int takenMedications) { 
        this.takenMedications = takenMedications; 
    }
    
    public int getSkippedMedications() { return skippedMedications; }
    public void setSkippedMedications(int skippedMedications) { 
        this.skippedMedications = skippedMedications; 
    }
    
    public int getMissedMedications() { return missedMedications; }
    public void setMissedMedications(int missedMedications) { 
        this.missedMedications = missedMedications; 
    }
}
