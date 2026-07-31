package com.example.myrajourney.common.messaging;

import com.example.myrajourney.data.model.Medication;
import com.example.myrajourney.data.model.SymptomLog;

import java.util.List;
import java.util.Map;
import java.util.Date;

/**
 * Comprehensive patient context for AI personalization
 * Contains all relevant patient data for generating personalized responses
 */
public class PatientContext {
    
    private String userId;
    private String patientName;
    private int age;
    private Date diagnosisDate;
    private List<String> riskFactors;
    private List<Medication> currentMedications;
    private List<SymptomLog> recentSymptoms;
    private Map<String, Object> preferences;
    private long timestamp;
    
    // Cache configuration
    private static final long CACHE_DURATION = 5 * 60 * 1000; // 5 minutes
    
    public PatientContext() {
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getters and Setters
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getPatientName() {
        return patientName;
    }
    
    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }
    
    public int getAge() {
        return age;
    }
    
    public void setAge(int age) {
        this.age = age;
    }
    
    public Date getDiagnosisDate() {
        return diagnosisDate;
    }
    
    public void setDiagnosisDate(Date diagnosisDate) {
        this.diagnosisDate = diagnosisDate;
    }
    
    public List<String> getRiskFactors() {
        return riskFactors;
    }
    
    public void setRiskFactors(List<String> riskFactors) {
        this.riskFactors = riskFactors;
    }
    
    public List<Medication> getCurrentMedications() {
        return currentMedications;
    }
    
    public void setCurrentMedications(List<Medication> currentMedications) {
        this.currentMedications = currentMedications;
    }
    
    public List<SymptomLog> getRecentSymptoms() {
        return recentSymptoms;
    }
    
    public void setRecentSymptoms(List<SymptomLog> recentSymptoms) {
        this.recentSymptoms = recentSymptoms;
    }
    
    public Map<String, Object> getPreferences() {
        return preferences;
    }
    
    public void setPreferences(Map<String, Object> preferences) {
        this.preferences = preferences;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    /**
     * Check if context data is expired and needs refresh
     */
    public boolean isExpired() {
        return (System.currentTimeMillis() - timestamp) > CACHE_DURATION;
    }
    
    /**
     * Get patient's RA duration in years
     */
    public int getRADurationYears() {
        if (diagnosisDate == null) return 0;
        
        long diffInMillis = System.currentTimeMillis() - diagnosisDate.getTime();
        return (int) (diffInMillis / (365L * 24 * 60 * 60 * 1000));
    }
    
    /**
     * Check if patient is newly diagnosed (less than 1 year)
     */
    public boolean isNewlyDiagnosed() {
        return getRADurationYears() < 1;
    }
    
    /**
     * Get communication preference for technical language
     */
    public boolean prefersSimpleLanguage() {
        if (preferences == null) return true;
        return (Boolean) preferences.getOrDefault("simple_language", true);
    }
    
    /**
     * Get preferred response length
     */
    public String getPreferredResponseLength() {
        if (preferences == null) return "medium";
        return (String) preferences.getOrDefault("response_length", "medium");
    }
    
    /**
     * Check if patient has specific medication
     */
    public boolean isOnMedication(String medicationName) {
        if (currentMedications == null) return false;
        
        return currentMedications.stream()
            .anyMatch(med -> med.getName().toLowerCase().contains(medicationName.toLowerCase()));
    }
    
    /**
     * Get recent symptom severity average
     */
    public double getRecentSymptomSeverity() {
        if (recentSymptoms == null || recentSymptoms.isEmpty()) return 0.0;
        
        return recentSymptoms.stream()
            .mapToInt(SymptomLog::getSeverity)
            .average()
            .orElse(0.0);
    }
    
    /**
     * Check if patient has high symptom activity
     */
    public boolean hasHighSymptomActivity() {
        return getRecentSymptomSeverity() >= 7.0;
    }
    
    /**
     * Get personalized greeting based on context
     */
    public String getPersonalizedGreeting() {
        StringBuilder greeting = new StringBuilder();
        
        if (patientName != null && !patientName.isEmpty()) {
            greeting.append("Hello ").append(patientName).append("! ");
        } else {
            greeting.append("Hello! ");
        }
        
        if (isNewlyDiagnosed()) {
            greeting.append("I'm here to help you navigate your RA journey. ");
        } else {
            greeting.append("How can I help you manage your RA today? ");
        }
        
        return greeting.toString();
    }
    
    /**
     * Get context summary for logging/debugging
     */
    public String getContextSummary() {
        return String.format(
            "PatientContext[userId=%s, name=%s, age=%d, RADuration=%d years, medications=%d, symptoms=%d]",
            userId,
            patientName != null ? patientName : "unknown",
            age,
            getRADurationYears(),
            currentMedications != null ? currentMedications.size() : 0,
            recentSymptoms != null ? recentSymptoms.size() : 0
        );
    }
}