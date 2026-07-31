package com.example.myrajourney.exercise.ui;

import android.util.Log;

/**
 * Ultra-simple, direct score calculator that can't be messed up by IDE autofix
 */
public class DirectScoreCalculator {
    private static final String TAG = "DirectScoreCalculator";
    
    private boolean userVisible = false;
    private boolean userMoving = false;
    private int goodFrameCount = 0;
    private double currentScore = 0.0;
    
    /**
     * Update with detection results
     */
    public void update(boolean visible, boolean moving) {
        userVisible = visible;
        userMoving = moving;
        
        if (visible && moving) {
            goodFrameCount++;
        } else {
            goodFrameCount = Math.max(0, goodFrameCount - 1);
        }
        
        // DIRECT SCORE CALCULATION - NO COMPLEX LOGIC
        calculateScore();
        
        Log.d(TAG, String.format("Score updated: %.1f%% (visible=%b, moving=%b)", 
                currentScore, visible, moving));
    }
    
    /**
     * Calculate score directly
     */
    private void calculateScore() {
        if (userVisible && userMoving) {
            // Exercising - give 60-95% score
            currentScore = 60.0 + (goodFrameCount * 0.7);
            if (currentScore > 95.0) currentScore = 95.0;
        } else {
            // Just staring or not visible - 0%
            currentScore = 0.0;
        }
    }
    
    /**
     * Get current score
     */
    public double getScore() {
        return currentScore;
    }
    
    /**
     * Get feedback message
     */
    public String getFeedback() {
        if (currentScore >= 80) {
            return "Excellent exercise!";
        } else if (currentScore >= 60) {
            return "Great exercise form!";
        } else {
            return "Start moving to exercise";
        }
    }
    
    /**
     * Reset calculator
     */
    public void reset() {
        userVisible = false;
        userMoving = false;
        goodFrameCount = 0;
        currentScore = 0.0;
        Log.d(TAG, "DirectScoreCalculator reset");
    }
}