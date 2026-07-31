package com.example.myrajourney.doctor.meds;

import com.google.gson.annotations.SerializedName;

public class AssignMedicationRequest {

    @SerializedName("patient_id")
    private int patientId;

    @SerializedName("medication_id")
    private Integer medicationId; // optional — if null use name_override

    @SerializedName("name_override")
    private String nameOverride;

    @SerializedName("dosage")
    private String dosage;

    @SerializedName("frequency_per_day")
    private String frequencyPerDay;

    @SerializedName("start_date")
    private String startDate; // yyyy-MM-dd

    @SerializedName("end_date")
    private String endDate;

    public AssignMedicationRequest(int patientId, Integer medicationId, String nameOverride, String dosage, String frequencyPerDay, String startDate, String endDate) {
        this.patientId = patientId;
        this.medicationId = medicationId;
        this.nameOverride = nameOverride;
        this.dosage = dosage;
        this.frequencyPerDay = frequencyPerDay;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // getters if needed...
}
