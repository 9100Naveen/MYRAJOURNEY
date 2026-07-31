package com.example.myrajourney.rehab.services;

import com.example.myrajourney.rehab.models.FormFeedback;
import com.example.myrajourney.rehab.models.MotionFrame;
import com.example.myrajourney.rehab.repository.ExerciseSessionRepository;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service for managing exercise session recording and tracking
 */
public interface SessionManager {
    
    /**
     * Start a new exercise session
     */
    CompletableFuture<String> startSession(String patientId, String exerciseId);
    
    /**
     * Record motion data during session
     */
    void recordMotionData(String sessionId, FormFeedback feedback);
    
    /**
     * Complete the current session
     */
    CompletableFuture<SessionResult> completeSession(String sessionId);
    
    /**
     * Pause the current session
     */
    void pauseSession(String sessionId);
    
    /**
     * Resume a paused session
     */
    void resumeSession(String sessionId);
    
    /**
     * Cancel the current session
     */
    CompletableFuture<Boolean> cancelSession(String sessionId);
    
    /**
     * Get current session status
     */
    SessionStatus getSessionStatus(String sessionId);
    
    /**
     * Get session statistics
     */
    SessionStats getSessionStats(String sessionId);
    
    /**
     * Check if session is active
     */
    boolean isSessionActive(String sessionId);
    
    /**
     * Session status enum
     */
    enum SessionStatus {
        NOT_STARTED,
        ACTIVE,
        PAUSED,
        COMPLETED,
        CANCELLED
    }
    
    /**
     * Session result class
     */
    class SessionResult {
        private final String sessionId;
        private final float overallAccuracy;
        private final float completionRate;
        private final long durationSeconds;
        private final int totalFrames;
        private final int correctFrames;
        
        public SessionResult(String sessionId, float overallAccuracy, float completionRate,
                           long durationSeconds, int totalFrames, int correctFrames) {
            this.sessionId = sessionId;
            this.overallAccuracy = overallAccuracy;
            this.completionRate = completionRate;
            this.durationSeconds = durationSeconds;
            this.totalFrames = totalFrames;
            this.correctFrames = correctFrames;
        }
        
        public String getSessionId() { return sessionId; }
        public float getOverallAccuracy() { return overallAccuracy; }
        public float getCompletionRate() { return completionRate; }
        public long getDurationSeconds() { return durationSeconds; }
        public int getTotalFrames() { return totalFrames; }
        public int getCorrectFrames() { return correctFrames; }
    }
    
    /**
     * Session statistics class
     */
    class SessionStats {
        private final float currentAccuracy;
        private final long elapsedTime;
        private final int totalFrames;
        private final int correctFrames;
        private final List<Float> recentAccuracyScores;
        
        public SessionStats(float currentAccuracy, long elapsedTime, int totalFrames,
                          int correctFrames, List<Float> recentAccuracyScores) {
            this.currentAccuracy = currentAccuracy;
            this.elapsedTime = elapsedTime;
            this.totalFrames = totalFrames;
            this.correctFrames = correctFrames;
            this.recentAccuracyScores = recentAccuracyScores;
        }
        
        public float getCurrentAccuracy() { return currentAccuracy; }
        public long getElapsedTime() { return elapsedTime; }
        public int getTotalFrames() { return totalFrames; }
        public int getCorrectFrames() { return correctFrames; }
        public List<Float> getRecentAccuracyScores() { return recentAccuracyScores; }
        public float getCompletionRate() { 
            return totalFrames > 0 ? (float) correctFrames / totalFrames : 0.0f; 
        }
    }
}