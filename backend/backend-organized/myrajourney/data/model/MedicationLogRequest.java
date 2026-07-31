package com.example.myrajourney.data.model;

import com.google.gson.annotations.SerializedName;

public class MedicationLogRequest {
    @SerializedName("patient_medication_id")
    private String patientMedicationId;

    @SerializedName("taken_at")
    private String takenAt;

    @SerializedName("dosage")
    private String dosage;

    @SerializedName("status")
    private String status;

    @SerializedName("notes")
    private String notes;

    public MedicationLogRequest(String patientMedicationId, String takenAt) {
        this.patientMedicationId = patientMedicationId;
        this.takenAt = takenAt;
        this.status = "TAKEN";
    }

    public void setDosage(String dosage) { this.dosage = dosage; }
    public void setStatus(String status) { this.status = status; }
    public void setNotes(String notes) { this.notes = notes; }
    
    // Getters
    public String getPatientMedicationId() { return patientMedicationId; }
    public String getTakenAt() { return takenAt; }
    public String getDosage() { return dosage; }
    public String getStatus() { return status; }
    public String getNotes() { return notes; }
    
    public void setPatientMedicationId(String patientMedicationId) { this.patientMedicationId = patientMedicationId; }
    public void setTakenAt(String takenAt) { this.takenAt = takenAt; }
}