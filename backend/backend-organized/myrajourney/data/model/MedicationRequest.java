package com.example.myrajourney.data.model;

import com.google.gson.annotations.SerializedName;

public class MedicationRequest {
    @SerializedName("name")
    private String name;

    @SerializedName("dosage")
    private String dosage;

    public MedicationRequest(String name, String dosage) {
        this.name = name;
        this.dosage = dosage;
    }
}






