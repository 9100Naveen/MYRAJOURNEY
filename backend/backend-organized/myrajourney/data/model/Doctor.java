package com.example.myrajourney.data.model;

import com.google.gson.annotations.SerializedName;

public class Doctor {
    @SerializedName("id")
    private int id; // Changed to int to match usage: user.getInt("id")

    @SerializedName("name")
    private String name;

    @SerializedName("email") // Added this field as it is used in AssignPatientToDoctorActivity
    private String email;

    @SerializedName("specialization")
    private String specialization;

    // --- Getters ---
    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getSpecialization() { return specialization; }

    // --- Setters (ADDED) ---
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
}