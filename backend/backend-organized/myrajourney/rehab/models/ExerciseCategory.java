package com.example.myrajourney.rehab.models;

/**
 * Enum representing different categories of RA exercises
 */
public enum ExerciseCategory {
    WRIST("Wrist Exercises", "Exercises targeting wrist joints"),
    THUMB("Thumb Exercises", "Exercises targeting thumb joints"),
    FINGER("Finger Exercises", "Exercises targeting finger joints"),
    KNEE("Knee Exercises", "Exercises targeting knee joints"),
    HIP("Hip Exercises", "Exercises targeting hip joints");

    private final String displayName;
    private final String description;

    ExerciseCategory(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}