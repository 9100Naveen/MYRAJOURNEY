package com.example.myrajourney.exercise.ui;

import android.graphics.Bitmap;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple movement detector that works even when pose detection fails
 * Uses basic image analysis to detect movement and activity
 */
public class SimpleMovementDetector {
    private static final String TAG = "SimpleMovementDetector";
    
    private List<Integer> recentPixelSums = new ArrayList<>();
    private static final int HISTORY_SIZE = 10;
    private static final int MOVEMENT_THRESHOLD = 30000; // Lower threshold for more sensitivity
    
    private boolean isMovementDetected = false;
    private double movementIntensity = 0.0;
    private long lastMovementTime = 0;
    
    /**
     * Process frame to detect movement
     */
    public void processFrame(Bitmap frame) {
        if (frame == null) {
            return;
        }
        
        try {
            // Calculate simple pixel sum for movement detection
            int pixelSum = calculatePixelSum(frame);
            
            // Add to history
            recentPixelSums.add(pixelSum);
            if (recentPixelSums.size() > HISTORY_SIZE) {
                recentPixelSums.remove(0);
            }
            
            // Detect movement based on pixel changes
            detectMovement();
            
        } catch (Exception e) {
            Log.e(TAG, "Error processing frame for movement detection", e);
        }
    }
    
    /**
     * Calculate simple pixel sum for movement detection
     */
    private int calculatePixelSum(Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled()) {
            return 0;
        }
        
        try {
            // Sample pixels from center region (where user is likely to be)
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int centerX = width / 2;
            int centerY = height / 2;
            int sampleSize = Math.min(width, height) / 4;
            
            int pixelSum = 0;
            int sampleCount = 0;
            
            // Sample pixels in a grid pattern
            for (int y = centerY - sampleSize; y < centerY + sampleSize; y += 10) {
                for (int x = centerX - sampleSize; x < centerX + sampleSize; x += 10) {
                    if (x >= 0 && x < width && y >= 0 && y < height) {
                        int pixel = bitmap.getPixel(x, y);
                        // Convert to grayscale and sum
                        int gray = (int) (0.299 * ((pixel >> 16) & 0xFF) + 
                                         0.587 * ((pixel >> 8) & 0xFF) + 
                                         0.114 * (pixel & 0xFF));
                        pixelSum += gray;
                        sampleCount++;
                    }
                }
            }
            
            return sampleCount > 0 ? pixelSum / sampleCount : 0;
            
        } catch (Exception e) {
            Log.e(TAG, "Error calculating pixel sum", e);
            return 0;
        }
    }
    
    /**
     * Detect movement based on pixel changes
     */
    private void detectMovement() {
        if (recentPixelSums.size() < 3) {
            return;
        }
        
        // Calculate variance in recent pixel sums
        double mean = recentPixelSums.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        double variance = 0.0;
        
        for (int sum : recentPixelSums) {
            variance += Math.pow(sum - mean, 2);
        }
        variance /= recentPixelSums.size();
        
        // Movement detected if variance is above threshold
        boolean movementDetected = variance > MOVEMENT_THRESHOLD;
        
        if (movementDetected) {
            isMovementDetected = true;
            lastMovementTime = System.currentTimeMillis();
            movementIntensity = Math.min(1.0, variance / (MOVEMENT_THRESHOLD * 2.0));
        } else {
            // Decay movement detection over time - shorter grace period
            long timeSinceMovement = System.currentTimeMillis() - lastMovementTime;
            if (timeSinceMovement > 1000) { // 1 second instead of 2
                isMovementDetected = false;
                movementIntensity *= 0.8; // Faster decay
            }
        }
        
        Log.d(TAG, String.format("Movement: %b, Intensity: %.2f, Variance: %.0f", 
                isMovementDetected, movementIntensity, variance));
    }
    
    /**
     * Check if movement is currently detected
     */
    public boolean isMovementDetected() {
        return isMovementDetected;
    }
    
    /**
     * Get movement intensity (0.0 to 1.0)
     */
    public double getMovementIntensity() {
        return movementIntensity;
    }
    
    /**
     * Get confidence based on movement detection
     */
    public double getMovementConfidence() {
        if (isMovementDetected) {
            return 0.6 + (movementIntensity * 0.3); // 0.6 to 0.9 range
        } else {
            return 0.2; // Low confidence when no movement
        }
    }
    
    /**
     * Reset detector state
     */
    public void reset() {
        recentPixelSums.clear();
        isMovementDetected = false;
        movementIntensity = 0.0;
        lastMovementTime = 0;
        Log.d(TAG, "Movement detector reset");
    }
}