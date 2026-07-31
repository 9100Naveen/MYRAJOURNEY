package com.example.myrajourney.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Request DTO for removing/stopping medication
 */
public class RemoveMedicationRequest {
    @SerializedName("medication_id")
    String medicationId;

    @SerializedName("action")
    String action; // 'remove' or 'stop'

    public RemoveMedicationRequest(String medicationId, String action) {
        this.medicationId = medicationId;
        this.action = action;
    }
}
