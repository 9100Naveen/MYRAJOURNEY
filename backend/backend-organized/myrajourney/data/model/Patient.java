package com.example.myrajourney.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.Date;
import java.util.List;

/**
 * Data model for patient information
 * Contains comprehensive patient profile data for RA management
 */
public class Patient {
    
    @SerializedName("id")
    private int id;
    
    @SerializedName("user_id")
    private int userId;
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("email")
    private String email;
    
    @SerializedName("phone")
    private String phone;
    
    @SerializedName("age")
    private int age;
    
    @SerializedName("gender")
    private String gender;
    
    @SerializedName("address")
    private String address;
    
    @SerializedName("emergency_contact")
    private String emergencyContact;
    
    @SerializedName("medical_history")
    private String medicalHistory;
    
    @SerializedName("current_medications")
    private String currentMedications;
    
    @SerializedName("doctor_notes")
    private String doctorNotes;
    
    @SerializedName("created_at")
    private String createdAt;
    
    @SerializedName("updated_at")
    private String updatedAt;
    
    // Additional fields for backward compatibility
    private Date diagnosisDate;
    private String doctorId;
    private String profilePicture;
    private List<String> riskFactors;
    private String emergencyPhone;
    
    // RA-specific fields
    private String raType; // Seropositive, Seronegative, etc.
    private String diseaseActivity; // Low, Moderate, High
    private List<String> affectedJoints;
    private String currentTreatmentPlan;
    private boolean isActive;
    
    // Constructors
    public Patient() {
        this.isActive = true;
    }
    
    public Patient(String name, String email, int age) {
        this();
        this.name = name;
        this.email = email;
        this.age = age;
    }
    
    // Legacy constructor for backward compatibility
    public Patient(int id, String name, String email, String ageStr) {
        this();
        this.id = id;
        this.name = name;
        this.email = email;
        
        // Parse age string
        try {
            if (!"N/A".equals(ageStr)) {
                this.age = Integer.parseInt(ageStr);
            }
        } catch (NumberFormatException e) {
            this.age = 0;
        }
    }
    
    // Another legacy constructor
    public Patient(String id, String name, String email, String ageStr) {
        this();
        try {
            this.id = Integer.parseInt(id);
        } catch (NumberFormatException e) {
            this.id = 0;
        }
        this.name = name;
        this.email = email;
        
        // Parse age string
        try {
            if (!"N/A".equals(ageStr)) {
                this.age = Integer.parseInt(ageStr);
            }
        } catch (NumberFormatException e) {
            this.age = 0;
        }
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public int getAge() {
        return age;
    }
    
    public void setAge(int age) {
        this.age = age;
    }
    
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getEmergencyContact() {
        return emergencyContact;
    }
    
    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }
    
    public String getMedicalHistory() {
        return medicalHistory;
    }
    
    public void setMedicalHistory(String medicalHistory) {
        this.medicalHistory = medicalHistory;
    }
    
    public String getCurrentMedications() {
        return currentMedications;
    }
    
    public void setCurrentMedications(String currentMedications) {
        this.currentMedications = currentMedications;
    }
    
    public String getDoctorNotes() {
        return doctorNotes;
    }
    
    public void setDoctorNotes(String doctorNotes) {
        this.doctorNotes = doctorNotes;
    }
    
    public String getCreatedAt() { 
        return createdAt; 
    }
    
    public void setCreatedAt(String createdAt) { 
        this.createdAt = createdAt; 
    }
    
    public String getUpdatedAt() { 
        return updatedAt; 
    }
    
    public void setUpdatedAt(String updatedAt) { 
        this.updatedAt = updatedAt; 
    }
    
    
    // Legacy compatibility fields and methods
    public Date getDiagnosisDate() {
        return diagnosisDate;
    }
    
    public void setDiagnosisDate(Date diagnosisDate) {
        this.diagnosisDate = diagnosisDate;
    }
    
    public String getDoctorId() {
        return doctorId;
    }
    
    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }
    
    public String getProfilePicture() {
        return profilePicture;
    }
    
    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }
    
    public List<String> getRiskFactors() {
        return riskFactors;
    }
    
    public void setRiskFactors(List<String> riskFactors) {
        this.riskFactors = riskFactors;
    }
    
    public String getEmergencyPhone() {
        return emergencyPhone;
    }
    
    public void setEmergencyPhone(String emergencyPhone) {
        this.emergencyPhone = emergencyPhone;
    }
    
    public String getRaType() {
        return raType;
    }
    
    public void setRaType(String raType) {
        this.raType = raType;
    }
    
    public String getDiseaseActivity() {
        return diseaseActivity;
    }
    
    public void setDiseaseActivity(String diseaseActivity) {
        this.diseaseActivity = diseaseActivity;
    }
    
    public List<String> getAffectedJoints() {
        return affectedJoints;
    }
    
    public void setAffectedJoints(List<String> affectedJoints) {
        this.affectedJoints = affectedJoints;
    }
    
    public String getCurrentTreatmentPlan() {
        return currentTreatmentPlan;
    }
    
    public void setCurrentTreatmentPlan(String currentTreatmentPlan) {
        this.currentTreatmentPlan = currentTreatmentPlan;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    // Utility methods
    public int getRADurationYears() {
        if (diagnosisDate == null) return 0;
        
        long diffInMillis = System.currentTimeMillis() - diagnosisDate.getTime();
        return (int) (diffInMillis / (365L * 24 * 60 * 60 * 1000));
    }
    
    public boolean isNewlyDiagnosed() {
        return getRADurationYears() < 1;
    }
    
    public String getAgeGroup() {
        if (age < 30) return "Young Adult";
        if (age < 50) return "Middle-aged";
        if (age < 65) return "Older Adult";
        return "Senior";
    }
    
    // Legacy compatibility methods (for existing code)
    public void setImageResId(int imageResId) {
        // This would be used for setting a default image resource
        // For now, just a no-op for compatibility
    }
    
    public String getFormattedDiagnosisDate() {
        if (diagnosisDate == null) return "Not specified";
        
        java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("MMM yyyy");
        return formatter.format(diagnosisDate);
    }
    
    public boolean hasHighDiseaseActivity() {
        return "High".equalsIgnoreCase(diseaseActivity);
    }
    
    public boolean hasModerateDiseaseActivity() {
        return "Moderate".equalsIgnoreCase(diseaseActivity);
    }
    
    public boolean hasLowDiseaseActivity() {
        return "Low".equalsIgnoreCase(diseaseActivity);
    }
    
    @Override
    public String toString() {
        return String.format("Patient{id=%d, name='%s', age=%d, raType='%s', diseaseActivity='%s'}",
            id, name, age, raType, diseaseActivity);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Patient patient = (Patient) obj;
        return id == patient.id;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}