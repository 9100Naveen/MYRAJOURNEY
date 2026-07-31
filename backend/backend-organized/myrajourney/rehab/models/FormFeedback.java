package com.example.myrajourney.rehab.models;

import android.graphics.Point;
import java.util.Map;

/**
 * Model class representing real-time form feedback during exercise
 */
public class FormFeedback {
    private boolean isCorrectForm;
    private float accuracy;
    private String specificFeedback;
    private Map<String, Point> jointPositions;

    public FormFeedback() {
    }

    public FormFeedback(boolean isCorrectForm, float accuracy, String specificFeedback,
            Map<String, Point> jointPositions) {
        this.isCorrectForm = isCorrectForm;
        this.accuracy = accuracy;
        this.specificFeedback = specificFeedback;
        this.jointPositions = jointPositions;
    }

    // Getters and Setters
    public boolean isCorrectForm() {
        return isCorrectForm;
    }

    public void setCorrectForm(boolean correctForm) {
        isCorrectForm = correctForm;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public String getSpecificFeedback() {
        return specificFeedback;
    }

    public void setSpecificFeedback(String specificFeedback) {
        this.specificFeedback = specificFeedback;
    }

    public Map<String, Point> getJointPositions() {
        return jointPositions;
    }

    public void setJointPositions(Map<String, Point> jointPositions) {
        this.jointPositions = jointPositions;
    }

    // Compatibility method
    public String getFeedbackMessage() {
        return specificFeedback;
    }
}