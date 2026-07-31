package com.example.myrajourney.patient.models;

public class Patient {
    private String name;
    private String details;
    private int imageResId;

    public Patient(String name, String details, int imageResId) {
        this.name = name;
        this.details = details;
        this.imageResId = imageResId;
    }

    public String getName() { return name; }
    public String getDetails() { return details; }
    public int getImageResId() { return imageResId; }
}






