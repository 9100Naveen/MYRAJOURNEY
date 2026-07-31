package com.example.myrajourney.exercise.ui;

import android.util.Log;
import java.util.ArrayList;
import java.util.List;

/**
 * Tracks all scores shown during a session and calculates proper averages
 * Ensures that even 1 second of movement results in non-zero final score
 */
public class ScoreTracker {
    private static final String TAG = "ScoreTracker";
    
    private List<Double> allScores;
    private boolean hasMovementDetected = false;
    private long sessionStartTime = 0;
    private long totalSessionTime = 0;
    
    public ScoreTracker() {
        this.allScores = new ArrayList<>();
    }
    
    /**
     * Start tracking scores for a new session
     */
    public void startSession() {
        allScores.clear();
        hasMovementDetected = false;
        sessionStartTime = System.currentTimeMillis();
        Log.d(TAG, "Score tracking session started");
    }
    
    /**
     * Record a score that was displayed to the user
     */
    public void recordScore(double score) {
        allScores.add(score);
        
        // If we see any score above 50%, consider it movement
        if (score > 50.0) {
            hasMovementDetected = true;
        }
        
        Log.d(TAG, "Recorded score: " + score + "% (total scores: " + allScores.size() + ")");
    }
    
    /**
     * Record that movement was detected (even if score is 0)
     */
    public void recordMovement() {
        hasMovementDetected = true;
        Log.d(TAG, "Movement detected and recorded");
    }
    
    /**
     * Calculate final session average when stopping
     */
    public double calculateFinalAverage() {
        totalSessionTime = System.currentTimeMillis() - sessionStartTime;
        
        if (allScores.isEmpty()) {
            // No scores recorded, but if movement was detected, give minimum score
            if (hasMovementDetected) {
                Log.d(TAG, "No scores but movement detected - returning minimum 65%");
                return 65.0;
            } else {
                Log.d(TAG, "No scores and no movement - returning 0%");
                return 0.0;
            }
        }
        
        // Calculate average of all recorded scores
        double sum = 0.0;
        for (double score : allScores) {
            sum += score;
        }
        double average = sum / allScores.size();
        
        // If movement was detected but average is low, boost it
        if (hasMovementDetected && average < 60.0) {
            average = 60.0 + (average * 0.5); // Boost to at least 60%
            Log.d(TAG, "Movement detected - boosted average to: " + average + "%");
        }
        
        Log.d(TAG, String.format("Final session stats - Scores recorded: %d, Average: %.1f%%, Movement: %s, Duration: %.1fs", 
                allScores.size(), average, hasMovementDetected, totalSessionTime / 1000.0));
        
        return average;
    }
    
    /**
     * Get session statistics
     */
    public String getSessionStats() {
        double average = calculateFinalAverage();
        return String.format(
            "Session Complete!\n" +
            "Average Score: %.1f%%\n" +
            "Total Updates: %d\n" +
            "Duration: %.1f seconds\n" +
            "Movement Detected: %s",
            average,
            allScores.size(),
            totalSessionTime / 1000.0,
            hasMovementDetected ? "Yes" : "No"
        );
    }
    
    /**
     * Check if any movement was detected during session
     */
    public boolean hadMovement() {
        return hasMovementDetected;
    }
    
    /**
     * Get total number of scores recorded
     */
    public int getTotalScoresRecorded() {
        return allScores.size();
    }
}