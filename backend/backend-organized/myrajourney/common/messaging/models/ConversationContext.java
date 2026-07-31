package com.example.myrajourney.common.messaging.models;

import com.example.myrajourney.patient.models.Patient;
import com.example.myrajourney.data.model.Appointment;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * Represents the context information for a conversation
 */
public class ConversationContext {
    private Patient patientProfile;
    private List<MedicalRecord> medicalHistory;
    private List<Medication> currentMedications;
    private List<Appointment> recentAppointments;
    private UserPreferences preferences;
    private List<String> riskFactors;
    private Map<String, Object> sessionData;

    public ConversationContext() {
        this.medicalHistory = new ArrayList<>();
        this.currentMedications = new ArrayList<>();
        this.recentAppointments = new ArrayList<>();
        this.riskFactors = new ArrayList<>();
        this.sessionData = new HashMap<>();
    }

    // Getters and Setters
    public Patient getPatientProfile() {
        return patientProfile;
    }

    public void setPatientProfile(Patient patientProfile) {
        this.patientProfile = patientProfile;
    }

    public List<MedicalRecord> getMedicalHistory() {
        return medicalHistory;
    }

    public void setMedicalHistory(List<MedicalRecord> medicalHistory) {
        this.medicalHistory = medicalHistory;
    }

    public List<Medication> getCurrentMedications() {
        return currentMedications;
    }

    public void setCurrentMedications(List<Medication> currentMedications) {
        this.currentMedications = currentMedications;
    }

    public List<Appointment> getRecentAppointments() {
        return recentAppointments;
    }

    public void setRecentAppointments(List<Appointment> recentAppointments) {
        this.recentAppointments = recentAppointments;
    }

    public UserPreferences getPreferences() {
        return preferences;
    }

    public void setPreferences(UserPreferences preferences) {
        this.preferences = preferences;
    }

    public List<String> getRiskFactors() {
        return riskFactors;
    }

    public void setRiskFactors(List<String> riskFactors) {
        this.riskFactors = riskFactors;
    }

    public Map<String, Object> getSessionData() {
        return sessionData;
    }

    public void setSessionData(Map<String, Object> sessionData) {
        this.sessionData = sessionData;
    }

    // Helper methods
    public void addMedicalRecord(MedicalRecord record) {
        this.medicalHistory.add(record);
    }

    public void addMedication(Medication medication) {
        this.currentMedications.add(medication);
    }

    public void addRiskFactor(String riskFactor) {
        this.riskFactors.add(riskFactor);
    }

    public void setSessionValue(String key, Object value) {
        this.sessionData.put(key, value);
    }

    public Object getSessionValue(String key) {
        return this.sessionData.get(key);
    }

    public boolean hasActiveMedications() {
        return currentMedications != null && !currentMedications.isEmpty();
    }

    public boolean hasRecentAppointments() {
        return recentAppointments != null && !recentAppointments.isEmpty();
    }

    /**
     * Placeholder classes for medical data - these should be replaced with actual models
     */
    public static class MedicalRecord {
        private String condition;
        private String diagnosis;
        private String date;
        private String notes;

        // Getters and setters
        public String getCondition() { return condition; }
        public void setCondition(String condition) { this.condition = condition; }
        public String getDiagnosis() { return diagnosis; }
        public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public static class Medication {
        private String name;
        private String dosage;
        private String frequency;
        private String instructions;
        private boolean isActive;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDosage() { return dosage; }
        public void setDosage(String dosage) { this.dosage = dosage; }
        public String getFrequency() { return frequency; }
        public void setFrequency(String frequency) { this.frequency = frequency; }
        public String getInstructions() { return instructions; }
        public void setInstructions(String instructions) { this.instructions = instructions; }
        public boolean isActive() { return isActive; }
        public void setActive(boolean active) { isActive = active; }
    }

    public static class UserPreferences {
        private String communicationStyle; // "technical", "simple", "detailed"
        private String language;
        private boolean enablePersonalization;
        private boolean shareDataForLearning;

        // Getters and setters
        public String getCommunicationStyle() { return communicationStyle; }
        public void setCommunicationStyle(String communicationStyle) { this.communicationStyle = communicationStyle; }
        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
        public boolean isEnablePersonalization() { return enablePersonalization; }
        public void setEnablePersonalization(boolean enablePersonalization) { this.enablePersonalization = enablePersonalization; }
        public boolean isShareDataForLearning() { return shareDataForLearning; }
        public void setShareDataForLearning(boolean shareDataForLearning) { this.shareDataForLearning = shareDataForLearning; }
    }
}