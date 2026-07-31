package com.example.myrajourney.rehab.repository;

import com.example.myrajourney.rehab.database.ExerciseSessionEntity;
import com.example.myrajourney.rehab.models.ExerciseSession;
import com.example.myrajourney.rehab.models.MotionFrame;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Repository interface for exercise session data management
 */
public interface ExerciseSessionRepository {
    
    /**
     * Create a new exercise session
     */
    CompletableFuture<String> createSession(String patientId, String exerciseId);
    
    /**
     * Update session with motion data and completion status
     */
    CompletableFuture<Boolean> updateSession(String sessionId, List<MotionFrame> motionData, 
                                           List<Float> accuracyScores, boolean completed);
    
    /**
     * Complete a session with final metrics
     */
    CompletableFuture<Boolean> completeSession(String sessionId, float overallAccuracy, 
                                             float completionRate, int durationSeconds);
    
    /**
     * Get session by ID
     */
    CompletableFuture<ExerciseSession> getSessionById(String sessionId);
    
    /**
     * Get all sessions for a patient
     */
    CompletableFuture<List<ExerciseSession>> getPatientSessions(String patientId);
    
    /**
     * Get sessions for a patient within date range
     */
    CompletableFuture<List<ExerciseSession>> getPatientSessionsInRange(String patientId, 
                                                                      LocalDateTime startDate, 
                                                                      LocalDateTime endDate);
    
    /**
     * Get sessions for a specific exercise
     */
    CompletableFuture<List<ExerciseSession>> getExerciseSessions(String patientId, String exerciseId);
    
    /**
     * Get recent sessions (last N sessions)
     */
    CompletableFuture<List<ExerciseSession>> getRecentSessions(String patientId, int limit);
    
    /**
     * Get completed sessions only
     */
    CompletableFuture<List<ExerciseSession>> getCompletedSessions(String patientId);
    
    /**
     * Delete session
     */
    CompletableFuture<Boolean> deleteSession(String sessionId);
    
    /**
     * Get session statistics for a patient
     */
    CompletableFuture<SessionStatistics> getSessionStatistics(String patientId);
    
    /**
     * Get session statistics for a specific exercise
     */
    CompletableFuture<SessionStatistics> getExerciseStatistics(String patientId, String exerciseId);
    
    /**
     * Statistics class for session data
     */
    class SessionStatistics {
        private final int totalSessions;
        private final int completedSessions;
        private final float averageAccuracy;
        private final long totalDuration; // in seconds
        private final LocalDateTime lastSessionDate;
        
        public SessionStatistics(int totalSessions, int completedSessions, float averageAccuracy, 
                               long totalDuration, LocalDateTime lastSessionDate) {
            this.totalSessions = totalSessions;
            this.completedSessions = completedSessions;
            this.averageAccuracy = averageAccuracy;
            this.totalDuration = totalDuration;
            this.lastSessionDate = lastSessionDate;
        }
        
        public int getTotalSessions() { return totalSessions; }
        public int getCompletedSessions() { return completedSessions; }
        public float getAverageAccuracy() { return averageAccuracy; }
        public long getTotalDuration() { return totalDuration; }
        public LocalDateTime getLastSessionDate() { return lastSessionDate; }
        public float getCompletionRate() { 
            return totalSessions > 0 ? (float) completedSessions / totalSessions : 0.0f; 
        }
    }
}