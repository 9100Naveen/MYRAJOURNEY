package com.example.myrajourney.rehab.models;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Model class representing an exercise session with motion tracking data
 */
public class ExerciseSession {
    private String id;
    private String patientId;
    private String exerciseId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<MotionFrame> motionData;
    private List<Float> formAccuracyScores;
    private boolean completed;

    public ExerciseSession() {}

    public ExerciseSession(String id, String patientId, String exerciseId, 
                          LocalDateTime startTime, LocalDateTime endTime,
                          List<MotionFrame> motionData, List<Float> formAccuracyScores, 
                          boolean completed) {
        this.id = id;
        this.patientId = patientId;
        this.exerciseId = exerciseId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.motionData = motionData;
        this.formAccuracyScores = formAccuracyScores;
        this.completed = completed;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getExerciseId() { return exerciseId; }
    public void setExerciseId(String exerciseId) { this.exerciseId = exerciseId; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public List<MotionFrame> getMotionData() { return motionData; }
    public void setMotionData(List<MotionFrame> motionData) { this.motionData = motionData; }

    public List<Float> getFormAccuracyScores() { return formAccuracyScores; }
    public void setFormAccuracyScores(List<Float> formAccuracyScores) { this.formAccuracyScores = formAccuracyScores; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}