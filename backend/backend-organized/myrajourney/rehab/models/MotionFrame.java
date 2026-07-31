package com.example.myrajourney.rehab.models;

import android.graphics.Point;
import java.util.Map;

/**
 * Model class representing a single frame of motion tracking data
 */
public class MotionFrame {
    private long timestamp;
    private Map<String, Point> jointPositions;
    private float formAccuracy;
    private boolean isCorrectForm;

    public MotionFrame() {}

    public MotionFrame(long timestamp, Map<String, Point> jointPositions, 
                      float formAccuracy, boolean isCorrectForm) {
        this.timestamp = timestamp;
        this.jointPositions = jointPositions;
        this.formAccuracy = formAccuracy;
        this.isCorrectForm = isCorrectForm;
    }

    // Getters and Setters
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public Map<String, Point> getJointPositions() { return jointPositions; }
    public void setJointPositions(Map<String, Point> jointPositions) { this.jointPositions = jointPositions; }

    public float getFormAccuracy() { return formAccuracy; }
    public void setFormAccuracy(float formAccuracy) { this.formAccuracy = formAccuracy; }

    public boolean isCorrectForm() { return isCorrectForm; }
    public void setCorrectForm(boolean correctForm) { isCorrectForm = correctForm; }
}