package com.example.myrajourney.data.model;

import com.google.gson.annotations.SerializedName;

public class Symptom {
    @SerializedName("id")
    private String id;

    @SerializedName("pain_level")
    private int painLevel;

    @SerializedName("stiffness")
    private int stiffness;

    public String getId() { return id; }
    public int getPainLevel() { return painLevel; }
    public int getStiffness() { return stiffness; }
}






