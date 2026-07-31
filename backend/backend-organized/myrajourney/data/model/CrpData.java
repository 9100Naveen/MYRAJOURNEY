package com.example.myrajourney.data.model;

import com.google.gson.annotations.SerializedName;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CrpData {
    
    @SerializedName("crpValue")
    private float crpValue;
    
    @SerializedName("measurementDate")
    private String measurementDate;
    
    @SerializedName("measurementUnit")
    private String measurementUnit;
    
    @SerializedName("patientId")
    private int patientId;
    
    @SerializedName("doctorId")
    private Integer doctorId;
    
    @SerializedName("reportId")
    private Integer reportId;
    
    @SerializedName("notes")
    private String notes;
    
    @SerializedName("id")
    private int id;
    
    // Constructors
    public CrpData() {}
    
    public CrpData(float crpValue, String measurementDate) {
        this.crpValue = crpValue;
        this.measurementDate = measurementDate;
    }
    
    // Getters and Setters
    public float getCrpValue() {
        return crpValue;
    }
    
    public void setCrpValue(float crpValue) {
        this.crpValue = crpValue;
    }
    
    public String getMeasurementDate() {
        return measurementDate;
    }
    
    public void setMeasurementDate(String measurementDate) {
        this.measurementDate = measurementDate;
    }
    
    public String getMeasurementUnit() {
        return measurementUnit;
    }
    
    public void setMeasurementUnit(String measurementUnit) {
        this.measurementUnit = measurementUnit;
    }
    
    public int getPatientId() {
        return patientId;
    }
    
    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }
    
    public Integer getDoctorId() {
        return doctorId;
    }
    
    public void setDoctorId(Integer doctorId) {
        this.doctorId = doctorId;
    }
    
    public Integer getReportId() {
        return reportId;
    }
    
    public void setReportId(Integer reportId) {
        this.reportId = reportId;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    // Helper methods
    
    /**
     * Format the measurement date for display
     * Converts "2024-12-05" to "Dec 5"
     */
    public String getFormattedDate() {
        if (measurementDate == null || measurementDate.isEmpty()) {
            return "";
        }
        
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM d", Locale.US);
            Date date = inputFormat.parse(measurementDate);
            return date != null ? outputFormat.format(date) : measurementDate;
        } catch (ParseException e) {
            // If parsing fails, return original
            return measurementDate;
        }
    }
    
    /**
     * Get color category based on CRP value
     * @return "normal" (<10), "elevated" (10-100), or "high" (>100)
     */
    public String getColorCategory() {
        if (crpValue < 10) {
            return "normal";
        } else if (crpValue <= 100) {
            return "elevated";
        } else {
            return "high";
        }
    }
    
    /**
     * Get Android color resource ID based on CRP value
     */
    public int getColorInt() {
        if (crpValue < 10) {
            return android.graphics.Color.GREEN;
        } else if (crpValue <= 100) {
            return android.graphics.Color.rgb(255, 165, 0); // Orange
        } else {
            return android.graphics.Color.RED;
        }
    }
    
    @Override
    public String toString() {
        return "CrpData{" +
                "crpValue=" + crpValue +
                ", measurementDate='" + measurementDate + '\'' +
                ", measurementUnit='" + measurementUnit + '\'' +
                ", patientId=" + patientId +
                '}';
    }
}
