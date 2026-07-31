package com.example.myrajourney.rehab.repository;

import com.example.myrajourney.rehab.database.ExerciseSessionEntity;
import com.example.myrajourney.rehab.models.ExerciseSession;
import com.example.myrajourney.rehab.models.MotionFrame;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Implementation of ExerciseSessionRepository
 * This is a mock implementation that stores data in memory
 * In production, this would use Room database
 */
public class ExerciseSessionRepositoryImpl implements ExerciseSessionRepository {
    
    // In-memory storage (replace with Room database in production)
    private final List<ExerciseSessionEntity> sessions = new ArrayList<>();
    
    @Override
    public CompletableFuture<String> createSession(String patientId, String exerciseId) {
        return CompletableFuture.supplyAsync(() -> {
            String sessionId = UUID.randomUUID().toString();
            ExerciseSessionEntity entity = new ExerciseSessionEntity(
                sessionId, patientId, exerciseId, LocalDateTime.now()
            );
            sessions.add(entity);
            
            android.util.Log.d("SessionRepository", "Created session: " + sessionId);
            return sessionId;
        });
    }
    
    @Override
    public CompletableFuture<Boolean> updateSession(String sessionId, List<MotionFrame> motionData, 
                                                   List<Float> accuracyScores, boolean completed) {
        return CompletableFuture.supplyAsync(() -> {
            ExerciseSessionEntity entity = findSessionById(sessionId);
            if (entity != null) {
                entity.motionData = motionData;
                entity.formAccuracyScores = accuracyScores;
                entity.completed = completed;
                
                android.util.Log.d("SessionRepository", "Updated session: " + sessionId);
                return true;
            }
            return false;
        });
    }
    
    @Override
    public CompletableFuture<Boolean> completeSession(String sessionId, float overallAccuracy, 
                                                     float completionRate, int durationSeconds) {
        return CompletableFuture.supplyAsync(() -> {
            ExerciseSessionEntity entity = findSessionById(sessionId);
            if (entity != null) {
                entity.endTime = LocalDateTime.now();
                entity.overallAccuracy = overallAccuracy;
                entity.completionRate = completionRate;
                entity.sessionDuration = durationSeconds;
                entity.completed = true;
                
                android.util.Log.d("SessionRepository", String.format(
                    "Completed session: %s (Accuracy: %.2f%%, Duration: %ds)",
                    sessionId, overallAccuracy * 100, durationSeconds));
                return true;
            }
            return false;
        });
    }
    
    @Override
    public CompletableFuture<ExerciseSession> getSessionById(String sessionId) {
        return CompletableFuture.supplyAsync(() -> {
            ExerciseSessionEntity entity = findSessionById(sessionId);
            return entity != null ? entityToModel(entity) : null;
        });
    }
    
    @Override
    public CompletableFuture<List<ExerciseSession>> getPatientSessions(String patientId) {
        return CompletableFuture.supplyAsync(() -> 
            sessions.stream()
                .filter(s -> s.patientId.equals(patientId))
                .map(this::entityToModel)
                .collect(Collectors.toList())
        );
    }
    
    @Override
    public CompletableFuture<List<ExerciseSession>> getPatientSessionsInRange(String patientId, 
                                                                             LocalDateTime startDate, 
                                                                             LocalDateTime endDate) {
        return CompletableFuture.supplyAsync(() -> 
            sessions.stream()
                .filter(s -> s.patientId.equals(patientId))
                .filter(s -> s.startTime.isAfter(startDate) && s.startTime.isBefore(endDate))
                .map(this::entityToModel)
                .collect(Collectors.toList())
        );
    }
    
    @Override
    public CompletableFuture<List<ExerciseSession>> getExerciseSessions(String patientId, String exerciseId) {
        return CompletableFuture.supplyAsync(() -> 
            sessions.stream()
                .filter(s -> s.patientId.equals(patientId) && s.exerciseId.equals(exerciseId))
                .map(this::entityToModel)
                .collect(Collectors.toList())
        );
    }
    
    @Override
    public CompletableFuture<List<ExerciseSession>> getRecentSessions(String patientId, int limit) {
        return CompletableFuture.supplyAsync(() -> 
            sessions.stream()
                .filter(s -> s.patientId.equals(patientId))
                .sorted((s1, s2) -> s2.startTime.compareTo(s1.startTime)) // Most recent first
                .limit(limit)
                .map(this::entityToModel)
                .collect(Collectors.toList())
        );
    }
    
    @Override
    public CompletableFuture<List<ExerciseSession>> getCompletedSessions(String patientId) {
        return CompletableFuture.supplyAsync(() -> 
            sessions.stream()
                .filter(s -> s.patientId.equals(patientId) && s.completed)
                .map(this::entityToModel)
                .collect(Collectors.toList())
        );
    }
    
    @Override
    public CompletableFuture<Boolean> deleteSession(String sessionId) {
        return CompletableFuture.supplyAsync(() -> {
            boolean removed = sessions.removeIf(s -> s.id.equals(sessionId));
            if (removed) {
                android.util.Log.d("SessionRepository", "Deleted session: " + sessionId);
            }
            return removed;
        });
    }
    
    @Override
    public CompletableFuture<SessionStatistics> getSessionStatistics(String patientId) {
        return CompletableFuture.supplyAsync(() -> {
            List<ExerciseSessionEntity> patientSessions = sessions.stream()
                .filter(s -> s.patientId.equals(patientId))
                .collect(Collectors.toList());
            
            return calculateStatistics(patientSessions);
        });
    }
    
    @Override
    public CompletableFuture<SessionStatistics> getExerciseStatistics(String patientId, String exerciseId) {
        return CompletableFuture.supplyAsync(() -> {
            List<ExerciseSessionEntity> exerciseSessions = sessions.stream()
                .filter(s -> s.patientId.equals(patientId) && s.exerciseId.equals(exerciseId))
                .collect(Collectors.toList());
            
            return calculateStatistics(exerciseSessions);
        });
    }
    
    /**
     * Helper method to find session by ID
     */
    private ExerciseSessionEntity findSessionById(String sessionId) {
        return sessions.stream()
            .filter(s -> s.id.equals(sessionId))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Convert entity to model
     */
    private ExerciseSession entityToModel(ExerciseSessionEntity entity) {
        return new ExerciseSession(
            entity.id,
            entity.patientId,
            entity.exerciseId,
            entity.startTime,
            entity.endTime,
            entity.motionData,
            entity.formAccuracyScores,
            entity.completed
        );
    }
    
    /**
     * Calculate statistics from session list
     */
    private SessionStatistics calculateStatistics(List<ExerciseSessionEntity> sessionList) {
        if (sessionList.isEmpty()) {
            return new SessionStatistics(0, 0, 0.0f, 0L, null);
        }
        
        int totalSessions = sessionList.size();
        int completedSessions = (int) sessionList.stream().filter(s -> s.completed).count();
        
        float averageAccuracy = (float) sessionList.stream()
            .filter(s -> s.overallAccuracy != null)
            .mapToDouble(s -> s.overallAccuracy)
            .average()
            .orElse(0.0);
        
        long totalDuration = sessionList.stream()
            .filter(s -> s.sessionDuration != null)
            .mapToLong(s -> s.sessionDuration)
            .sum();
        
        LocalDateTime lastSessionDate = sessionList.stream()
            .map(s -> s.startTime)
            .max(LocalDateTime::compareTo)
            .orElse(null);
        
        return new SessionStatistics(totalSessions, completedSessions, averageAccuracy, 
                                    totalDuration, lastSessionDate);
    }
}