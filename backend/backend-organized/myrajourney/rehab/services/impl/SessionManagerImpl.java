package com.example.myrajourney.rehab.services.impl;

import android.graphics.Point;
import com.example.myrajourney.rehab.models.FormFeedback;
import com.example.myrajourney.rehab.models.MotionFrame;
import com.example.myrajourney.rehab.repository.ExerciseSessionRepository;
import com.example.myrajourney.rehab.repository.ExerciseSessionRepositoryImpl;
import com.example.myrajourney.rehab.services.SessionManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of SessionManager for exercise session recording and tracking
 */
public class SessionManagerImpl implements SessionManager {
    
    private final ExerciseSessionRepository sessionRepository;
    
    // Active session tracking
    private final Map<String, ActiveSession> activeSessions = new HashMap<>();
    
    public SessionManagerImpl() {
        this.sessionRepository = new ExerciseSessionRepositoryImpl();
    }
    
    public SessionManagerImpl(ExerciseSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }
    
    @Override
    public CompletableFuture<String> startSession(String patientId, String exerciseId) {
        return sessionRepository.createSession(patientId, exerciseId)
            .thenApply(sessionId -> {
                ActiveSession activeSession = new ActiveSession(sessionId, patientId, exerciseId);
                activeSessions.put(sessionId, activeSession);
                
                android.util.Log.d("SessionManager", "Started session: " + sessionId);
                return sessionId;
            });
    }
    
    @Override
    public void recordMotionData(String sessionId, FormFeedback feedback) {
        ActiveSession session = activeSessions.get(sessionId);
        if (session == null || session.status != SessionStatus.ACTIVE) {
            return;
        }
        
        // Create motion frame from feedback
        MotionFrame frame = new MotionFrame(
            System.currentTimeMillis(),
            feedback.getJointPositions(),
            feedback.getAccuracy(),
            feedback.isCorrectForm()
        );
        
        // Record the frame
        session.recordFrame(frame, feedback.getAccuracy());
        
        // Update session in repository periodically (every 10 frames)
        if (session.motionFrames.size() % 10 == 0) {
            updateSessionInRepository(session);
        }
    }
    
    @Override
    public CompletableFuture<SessionResult> completeSession(String sessionId) {
        ActiveSession session = activeSessions.get(sessionId);
        if (session == null) {
            return CompletableFuture.completedFuture(null);
        }
        
        session.complete();
        
        // Calculate final metrics
        float overallAccuracy = session.getOverallAccuracy();
        float completionRate = session.getCompletionRate();
        long durationSeconds = session.getDurationSeconds();
        
        // Update repository with final data
        return sessionRepository.updateSession(sessionId, session.motionFrames, 
                                             session.accuracyScores, true)
            .thenCompose(updated -> 
                sessionRepository.completeSession(sessionId, overallAccuracy, 
                                                completionRate, (int) durationSeconds))
            .thenApply(completed -> {
                activeSessions.remove(sessionId);
                
                android.util.Log.d("SessionManager", String.format(
                    "Completed session: %s (Accuracy: %.2f%%, Duration: %ds)",
                    sessionId, overallAccuracy * 100, durationSeconds));
                
                return new SessionResult(sessionId, overallAccuracy, completionRate,
                                       durationSeconds, session.motionFrames.size(),
                                       session.correctFrames);
            });
    }
    
    @Override
    public void pauseSession(String sessionId) {
        ActiveSession session = activeSessions.get(sessionId);
        if (session != null && session.status == SessionStatus.ACTIVE) {
            session.pause();
            android.util.Log.d("SessionManager", "Paused session: " + sessionId);
        }
    }
    
    @Override
    public void resumeSession(String sessionId) {
        ActiveSession session = activeSessions.get(sessionId);
        if (session != null && session.status == SessionStatus.PAUSED) {
            session.resume();
            android.util.Log.d("SessionManager", "Resumed session: " + sessionId);
        }
    }
    
    @Override
    public CompletableFuture<Boolean> cancelSession(String sessionId) {
        ActiveSession session = activeSessions.get(sessionId);
        if (session == null) {
            return CompletableFuture.completedFuture(false);
        }
        
        session.cancel();
        activeSessions.remove(sessionId);
        
        android.util.Log.d("SessionManager", "Cancelled session: " + sessionId);
        return CompletableFuture.completedFuture(true);
    }
    
    @Override
    public SessionStatus getSessionStatus(String sessionId) {
        ActiveSession session = activeSessions.get(sessionId);
        return session != null ? session.status : SessionStatus.NOT_STARTED;
    }
    
    @Override
    public SessionStats getSessionStats(String sessionId) {
        ActiveSession session = activeSessions.get(sessionId);
        if (session == null) {
            return new SessionStats(0.0f, 0L, 0, 0, new ArrayList<>());
        }
        
        return new SessionStats(
            session.getCurrentAccuracy(),
            session.getElapsedTime(),
            session.motionFrames.size(),
            session.correctFrames,
            session.getRecentAccuracyScores(10)
        );
    }
    
    @Override
    public boolean isSessionActive(String sessionId) {
        ActiveSession session = activeSessions.get(sessionId);
        return session != null && session.status == SessionStatus.ACTIVE;
    }
    
    /**
     * Update session data in repository
     */
    private void updateSessionInRepository(ActiveSession session) {
        sessionRepository.updateSession(session.sessionId, session.motionFrames,
                                      session.accuracyScores, false);
    }
    
    /**
     * Active session tracking class
     */
    private static class ActiveSession {
        final String sessionId;
        final String patientId;
        final String exerciseId;
        final long startTime;
        
        SessionStatus status;
        List<MotionFrame> motionFrames;
        List<Float> accuracyScores;
        int correctFrames;
        long pausedDuration;
        long lastPauseTime;
        
        ActiveSession(String sessionId, String patientId, String exerciseId) {
            this.sessionId = sessionId;
            this.patientId = patientId;
            this.exerciseId = exerciseId;
            this.startTime = System.currentTimeMillis();
            this.status = SessionStatus.ACTIVE;
            this.motionFrames = new ArrayList<>();
            this.accuracyScores = new ArrayList<>();
            this.correctFrames = 0;
            this.pausedDuration = 0;
        }
        
        void recordFrame(MotionFrame frame, float accuracy) {
            if (status != SessionStatus.ACTIVE) return;
            
            motionFrames.add(frame);
            accuracyScores.add(accuracy);
            
            if (frame.isCorrectForm()) {
                correctFrames++;
            }
        }
        
        void pause() {
            if (status == SessionStatus.ACTIVE) {
                status = SessionStatus.PAUSED;
                lastPauseTime = System.currentTimeMillis();
            }
        }
        
        void resume() {
            if (status == SessionStatus.PAUSED) {
                status = SessionStatus.ACTIVE;
                pausedDuration += System.currentTimeMillis() - lastPauseTime;
            }
        }
        
        void complete() {
            status = SessionStatus.COMPLETED;
        }
        
        void cancel() {
            status = SessionStatus.CANCELLED;
        }
        
        float getCurrentAccuracy() {
            return motionFrames.isEmpty() ? 0.0f : (float) correctFrames / motionFrames.size();
        }
        
        float getOverallAccuracy() {
            if (accuracyScores.isEmpty()) return 0.0f;
            
            return (float) accuracyScores.stream()
                .mapToDouble(Float::doubleValue)
                .average()
                .orElse(0.0);
        }
        
        float getCompletionRate() {
            // For now, completion rate is based on accuracy
            // In a real implementation, this could be based on exercise-specific criteria
            return getCurrentAccuracy();
        }
        
        long getDurationSeconds() {
            long currentTime = status == SessionStatus.PAUSED ? lastPauseTime : System.currentTimeMillis();
            return (currentTime - startTime - pausedDuration) / 1000;
        }
        
        long getElapsedTime() {
            long currentTime = status == SessionStatus.PAUSED ? lastPauseTime : System.currentTimeMillis();
            return currentTime - startTime - pausedDuration;
        }
        
        List<Float> getRecentAccuracyScores(int count) {
            int size = accuracyScores.size();
            int startIndex = Math.max(0, size - count);
            return new ArrayList<>(accuracyScores.subList(startIndex, size));
        }
    }
}