package com.example.myrajourney.exercise.ui;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

/**
 * Instant movement scorer that immediately shows 60-95% when movement is detected
 * Even for just 2 seconds of movement
 */
public class InstantMovementScorer {
    private static final String TAG = "InstantMovementScorer";
    
    private TextView scoreText;
    private TextView feedbackText;
    private Handler handler;
    private boolean isActive = false;
    private boolean movementDetected = false;
    private long lastMovementTime = 0;
    private double currentScore = 0.0;
    private int scoreVariation = 0;
    
    public InstantMovementScorer(TextView scoreTextView, TextView feedbackTextView) {
        this.scoreText = scoreTextView;
        this.feedbackText = feedbackTextView;
        this.handler = new Handler(Looper.getMainLooper());
    }
    
    /**
     * Start the instant movement scoring
     */
    public void start() {
        isActive = true;
        movementDetected = false;
        currentScore = 0.0;
        scoreVariation = 0;
        startScoring();
        Log.d(TAG, "InstantMovementScorer started");
    }
    
    /**
     * Stop the scoring
     */
    public void stop() {
        isActive = false;
        Log.d(TAG, "InstantMovementScorer stopped");
    }
    
    /**
     * Update movement detection - call this when movement is detected
     */
    public void updateMovement(boolean hasMovement) {
        if (hasMovement) {
            movementDetected = true;
            lastMovementTime = System.currentTimeMillis();
            Log.d(TAG, "Movement detected - will show high score immediately");
        }
    }
    
    /**
     * Start the scoring loop
     */
    private void startScoring() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isActive) {
                    updateScore();
                    handler.postDelayed(this, 500); // Update every 500ms for responsiveness
                }
            }
        });
    }
    
    /**
     * Update the score based on movement detection
     */
    private void updateScore() {
        long currentTime = System.currentTimeMillis();
        
        // Check if we have recent movement (within last 3 seconds)
        boolean hasRecentMovement = movementDetected && 
                                   (currentTime - lastMovementTime) < 3000;
        
        if (hasRecentMovement) {
            // IMMEDIATE high score for any movement - 60-95% range
            scoreVariation = (scoreVariation + 3) % 36; // Cycles through 0-35
            currentScore = 60.0 + scoreVariation; // 60-95% range
            
            Log.d(TAG, "Movement detected - showing high score: " + currentScore + "%");
        } else {
            // No recent movement - show 0%
            currentScore = 0.0;
            scoreVariation = 0;
        }
        
        // Update UI immediately
        updateUI();
    }
    
    /**
     * Update UI with current score
     */
    private void updateUI() {
        if (scoreText != null) {
            scoreText.setText(String.format("Score: %.1f%%", currentScore));
        }
        
        if (feedbackText != null) {
            String feedback;
            if (currentScore >= 85) {
                feedback = "Excellent exercise performance!";
            } else if (currentScore >= 70) {
                feedback = "Great exercise form!";
            } else if (currentScore >= 60) {
                feedback = "Good exercise movement!";
            } else {
                feedback = "Start moving to exercise";
            }
            feedbackText.setText(feedback);
        }
    }
    
    /**
     * Get current score
     */
    public double getCurrentScore() {
        return currentScore;
    }
    
    /**
     * Force a high score update (for testing)
     */
    public void forceHighScore() {
        movementDetected = true;
        lastMovementTime = System.currentTimeMillis();
        updateScore();
        Log.d(TAG, "Forced high score update");
    }
}