package com.example.myrajourney.rehab.models;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Model class representing a performance report for an exercise session
 */
public class PerformanceReport {
    private String sessionId;
    private String patientId;
    private String exerciseId;
    private Duration sessionDuration;
    private float formAccuracy;
    private float completionRate;
    private Map<String, Object> specificMetrics;
    private List<String> recommendations;
    private LocalDateTime timestamp;

    public PerformanceReport() {}

    public PerformanceReport(String sessionId, String patientId, String exerciseId,
                           Duration sessionDuration, float formAccuracy, float completionRate,
                           Map<String, Object> specificMetrics, List<String> recommendations,
                           LocalDateTime timestamp) {
        this.sessionId = sessionId;
        this.patientId = patientId;
        this.exerciseId = exerciseId;
        this.sessionDuration = sessionDuration;
        this.formAccuracy = formAccuracy;
        this.completionRate = completionRate;
        this.specificMetrics = specificMetrics;
        this.recommendations = recommendations;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getExerciseId() { return exerciseId; }
    public void setExerciseId(String exerciseId) { this.exerciseId = exerciseId; }

    public Duration getSessionDuration() { return sessionDuration; }
    public void setSessionDuration(Duration sessionDuration) { this.sessionDuration = sessionDuration; }

    public float getFormAccuracy() { return formAccuracy; }
    public void setFormAccuracy(float formAccuracy) { this.formAccuracy = formAccuracy; }

    public float getCompletionRate() { return completionRate; }
    public void setCompletionRate(float completionRate) { this.completionRate = completionRate; }

    public Map<String, Object> getSpecificMetrics() { return specificMetrics; }
    public void setSpecificMetrics(Map<String, Object> specificMetrics) { this.specificMetrics = specificMetrics; }

    public List<String> getRecommendations() { return recommendations; }
    public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}