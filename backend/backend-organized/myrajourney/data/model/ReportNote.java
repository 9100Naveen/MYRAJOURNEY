package com.example.myrajourney.data.model;

import com.google.gson.annotations.SerializedName;

public class ReportNote {

    @SerializedName("id")
    private String id;

    @SerializedName("report_id")
    private String reportId;

    @SerializedName("doctor_id")
    private String doctorId;

    @SerializedName("diagnosis_text")
    private String diagnosisText;

    @SerializedName("suggestions_text")
    private String suggestionsText;

    @SerializedName("crp_value")
    private Float crpValue;

    @SerializedName("created_at")
    private String createdAt;

    // Getters
    public String getId() { return id; }
    public String getReportId() { return reportId; }
    public String getDoctorId() { return doctorId; }
    public String getDiagnosisText() { return diagnosisText; }
    public String getSuggestionsText() { return suggestionsText; }
    public Float getCrpValue() { return crpValue; }
    public String getCreatedAt() { return createdAt; }

    // Setters
    public void setReportId(String reportId) { this.reportId = reportId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }
    public void setDiagnosisText(String diagnosisText) { this.diagnosisText = diagnosisText; }
    public void setSuggestionsText(String suggestionsText) { this.suggestionsText = suggestionsText; }
    public void setCrpValue(Float crpValue) { this.crpValue = crpValue; }
}
