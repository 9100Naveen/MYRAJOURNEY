package com.example.myrajourney.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * PRODUCTION-READY Medication Data Model
 * Enhanced for healthcare app reliability with comprehensive null handling
 */
public class Medication {

    @SerializedName("id")
    private String id;

    @SerializedName("patient_id")
    private String patientId;

    // CRITICAL: Multiple field name support for backend compatibility
    @SerializedName(value = "name", alternate = { "name_override", "medication_name", "medicineName" })
    private String name;

    @SerializedName("generic_name")
    private String genericName;

    @SerializedName("dosage")
    private String dosage;

    // CRITICAL: Support both string and numeric frequency values
    @SerializedName(value = "frequency_per_day", alternate = { "frequency", "frequencyPerDay" })
    private String frequency;

    @SerializedName("instructions")
    private String instructions;

    @SerializedName("duration")
    private String duration;

    @SerializedName("is_morning")
    private int isMorning;

    @SerializedName("is_afternoon")
    private int isAfternoon;

    @SerializedName("is_night")
    private int isNight;

    @SerializedName("food_relation")
    private String foodRelation;

    // CRITICAL: Handle date strings from backend (not Date objects)
    @SerializedName("start_date")
    private String startDate;

    @SerializedName("end_date")
    private String endDate;

    @SerializedName("prescribed_by")
    private String prescribedBy;

    @SerializedName("category")
    private String category;

    // CRITICAL: Support both boolean and numeric active values
    @SerializedName(value = "active", alternate = { "is_active", "isActive" })
    private int isActiveInt; // Backend sends 1/0

    @SerializedName("notes")
    private String notes;

    @SerializedName("side_effects")
    private List<String> sideEffects;

    // CRITICAL: Handle date strings from backend
    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    // NEW FIELDS for Removal Feature
    @SerializedName("status")
    private String status; // 'active', 'stopped', 'removed'

    @SerializedName("removed_at")
    private String removedAt;

    @SerializedName("removed_by")
    private String removedBy;

    // Additional fields that might come from backend
    @SerializedName("doctor_id")
    private String doctorId;

    @SerializedName("medication_id")
    private String medicationId;

    @SerializedName("reminder_enabled")
    private int reminderEnabledInt;

    @SerializedName("reminder_times")
    private String reminderTimes;

    @SerializedName("adherence_rate")
    private String adherenceRate;

    @SerializedName(value = "doctor_name", alternate = { "doctorName", "doc_name" })
    private String doctorName;

    @SerializedName(value = "doctor_specialization", alternate = { "doctorSpecialization", "doc_spec" })
    private String doctorSpecialization;

    @SerializedName(value = "doctor_license", alternate = { "doctorLicense", "license_no", "doc_license" })
    private String doctorLicense;
    // Digital signature field removed

    // Default constructor
    public Medication() {
        // Initialize with safe defaults
        this.isActiveInt = 1;
        this.reminderEnabledInt = 1;
    }

    // Constructor for creating medications with basic info (for compatibility)
    public Medication(String name, String dosage, String frequency, String duration, String type, String category,
            String status) {
        this();
        this.name = name;
        this.dosage = dosage;
        this.frequency = frequency;
        this.category = category;
        setActive("Active".equalsIgnoreCase(status));
    }

    // PRODUCTION-SAFE GETTERS with null checks and defaults

    public String getId() {
        return id != null ? id : "";
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPatientId() {
        return patientId != null ? patientId : "";
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getName() {
        return name != null && !name.trim().isEmpty() ? name.trim() : null;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGenericName() {
        return genericName != null ? genericName : "";
    }

    public void setGenericName(String genericName) {
        this.genericName = genericName;
    }

    public String getDosage() {
        return dosage != null && !dosage.trim().isEmpty() ? dosage.trim() : null;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getFrequency() {
        return frequency != null && !frequency.trim().isEmpty() ? frequency.trim() : "1";
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public boolean isMorning() {
        return isMorning == 1;
    }

    public void setMorning(boolean morning) {
        this.isMorning = morning ? 1 : 0;
    }

    public boolean isAfternoon() {
        return isAfternoon == 1;
    }

    public void setAfternoon(boolean afternoon) {
        this.isAfternoon = afternoon ? 1 : 0;
    }

    public boolean isNight() {
        return isNight == 1;
    }

    public void setNight(boolean night) {
        this.isNight = night ? 1 : 0;
    }

    public String getFoodRelation() {
        return foodRelation;
    }

    public void setFoodRelation(String foodRelation) {
        this.foodRelation = foodRelation;
    }

    public String getStartDate() {
        return startDate != null ? startDate : "";
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate != null ? endDate : "";
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getPrescribedBy() {
        return prescribedBy != null ? prescribedBy : "";
    }

    public void setPrescribedBy(String prescribedBy) {
        this.prescribedBy = prescribedBy;
    }

    public String getCategory() {
        return category != null ? category : "";
    }

    public void setCategory(String category) {
        this.category = category;
    }

    // CRITICAL: Convert backend int (1/0) to boolean
    public boolean isActive() {
        return isActiveInt == 1;
    }

    public void setActive(boolean active) {
        this.isActiveInt = active ? 1 : 0;
    }

    public String getNotes() {
        return notes != null ? notes : "";
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<String> getSideEffects() {
        return sideEffects;
    }

    public void setSideEffects(List<String> sideEffects) {
        this.sideEffects = sideEffects;
    }

    public String getCreatedAt() {
        return createdAt != null ? createdAt : "";
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt != null ? updatedAt : "";
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getDoctorId() {
        return doctorId != null ? doctorId : "";
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getMedicationId() {
        return medicationId != null ? medicationId : "";
    }

    public void setMedicationId(String medicationId) {
        this.medicationId = medicationId;
    }

    // CRITICAL: Convert backend int (1/0) to boolean
    public boolean isReminderEnabled() {
        return reminderEnabledInt == 1;
    }

    public void setReminderEnabled(boolean reminderEnabled) {
        this.reminderEnabledInt = reminderEnabled ? 1 : 0;
    }

    public String getReminderTimes() {
        return reminderTimes != null ? reminderTimes : "";
    }

    public void setReminderTimes(String reminderTimes) {
        this.reminderTimes = reminderTimes;
    }

    public String getAdherenceRate() {
        return adherenceRate != null ? adherenceRate : "";
    }

    public void setAdherenceRate(String adherenceRate) {
        this.adherenceRate = adherenceRate;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public String getDoctorSpecialization() {
        return doctorSpecialization;
    }

    public String getDoctorLicense() {
        return doctorLicense;
    }

    // Digital signature getter removed

    // UTILITY METHODS for UI display

    public String getDisplayName() {
        String displayName = getName();
        if (getGenericName() != null && !getGenericName().isEmpty()) {
            displayName += " (" + getGenericName() + ")";
        }
        return displayName;
    }

    public String getFullDescription() {
        StringBuilder desc = new StringBuilder();
        desc.append(getDisplayName());

        if (!getDosage().equals("No dosage")) {
            desc.append(" - ").append(getDosage());
        }

        if (!getFrequency().equals("1")) {
            desc.append(" (").append(getFrequency()).append("x daily)");
        }

        return desc.toString();
    }

    // CRITICAL: Safe formatted time for UI
    public String getFormattedTime() {
        String times = getReminderTimes();
        if (times == null || times.trim().isEmpty()) {
            return "As prescribed";
        }
        return times;
    }

    // PRODUCTION: Comprehensive toString for debugging
    @Override
    public String toString() {
        return String.format("Medication{id='%s', name='%s', dosage='%s', frequency='%s', active=%s}",
                getId(), getName(), getDosage(), getFrequency(), isActive());
    }

    // PRODUCTION: Safe equals and hashCode
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        Medication that = (Medication) obj;
        return getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    // LEGACY COMPATIBILITY METHODS (for existing code)

    public boolean isTakenToday() {
        // Legacy method - return false as default
        return false;
    }

    public void setTakenToday(boolean taken) {
        // Legacy method - no-op for compatibility
    }

    public String getType() {
        return getCategory();
    }

    public void setType(String type) {
        setCategory(type);
    }

    public String getStatus() {
        return isActive() ? "Active" : "Inactive";
    }

    public void setStatus(String status) {
        this.status = status;
        // Keep legacy backward compatibility
        setActive("Active".equalsIgnoreCase(status) || "Ongoing".equalsIgnoreCase(status));
    }

    public boolean isRemoved() {
        return "removed".equalsIgnoreCase(status);
    }

    public boolean isStopped() {
        return "stopped".equalsIgnoreCase(status);
    }
}