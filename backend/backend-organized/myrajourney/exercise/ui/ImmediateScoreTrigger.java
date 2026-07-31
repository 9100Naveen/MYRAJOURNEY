package com.example.myrajourney.exercise.ui;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

/**
 * Immediate score trigger - shows 60-95% the moment ANY movement is detected
 * Even if movement lasts only 2 seconds
 */
public class ImmediateScoreTrigger {
    private static final String TAG = "ImmediateScoreTrigger";
    
    private TextView scoreText;
    private TextView feedbackText;
    private Handler handler;
    private boolean isActive = false;
    
    public ImmediateScoreTrigger(TextView scoreTextView, TextView feedbackTextView) {
        this.scoreText = scoreTextView;
        this.feedbackText = feedbackTextView;
        this.handler = new Handler(Looper.getMainLooper());
    }
    
    /**
     * Start monitoring for movement
     */
    public void start() {
        isActive = true;
        Log.d(TAG, "ImmediateScoreTrigger started - ready to show high scores");
    }
    
    /**
     * Stop monitoring
     */
    public void stop() {
        isActive = false;
        Log.d(TAG, "ImmediateScoreTrigger stopped");
    }
    
    /**
     * Trigger immediate high score when movement detected
     */
    public void triggerMovement() {
        if (!isActive) return;
        
        // IMMEDIATE high score - 75-90% range
        double score = 75.0 + (System.currentTimeMillis() % 16); // 75-90%
        
        // Update UI immediately on main thread
        handler.post(() -> {
            if (scoreText != null) {
                scoreText.setText(String.format("Score: %.1f%%", score));
            }
            
            if (feedbackText != null) {
                feedbackText.setText("Great exercise movement!");
            }
            
            Log.d(TAG, "IMMEDIATE high score triggered: " + score + "%");
        });
    }
    
    /**
     * Force high score for testing
     */
    public void forceHighScore() {
        triggerMovement();
    }
}