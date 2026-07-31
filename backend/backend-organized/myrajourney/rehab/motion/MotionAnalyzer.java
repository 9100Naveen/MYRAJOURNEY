package com.example.myrajourney.rehab.motion;

import android.graphics.Point;
import com.example.myrajourney.rehab.models.FormFeedback;
import com.example.myrajourney.rehab.models.MotionFrame;
import java.util.List;
import java.util.Map;

/**
 * Core motion analysis engine for real-time exercise form validation
 */
public class MotionAnalyzer {
    
    private JointTracker jointTracker;
    private FormValidator formValidator;
    private FeedbackGenerator feedbackGenerator;
    
    // Analysis state
    private String currentExerciseId;
    private boolean isAnalyzing = false;
    private long analysisStartTime;
    private int totalFrames = 0;
    private int correctFrames = 0;
    
    public MotionAnalyzer() {
        this.jointTracker = new JointTracker();
        this.formValidator = new FormValidator();
        this.feedbackGenerator = new FeedbackGenerator();
    }
    
    /**
     * Start motion analysis for a specific exercise
     */
    public void startAnalysis(String exerciseId) {
        this.currentExerciseId = exerciseId;
        this.isAnalyzing = true;
        this.analysisStartTime = System.currentTimeMillis();
        this.totalFrames = 0;
        this.correctFrames = 0;
        
        // Configure form validator for the specific exercise
        formValidator.setExerciseType(exerciseId);
        
        android.util.Log.d("MotionAnalyzer", "Started analysis for exercise: " + exerciseId);
    }
    
    /**
     * Stop motion analysis and return results
     */
    public MotionAnalysisResult stopAnalysis() {
        isAnalyzing = false;
        
        long duration = System.currentTimeMillis() - analysisStartTime;
        float accuracy = totalFrames > 0 ? (float) correctFrames / totalFrames : 0.0f;
        
        android.util.Log.d("MotionAnalyzer", String.format(
            "Analysis completed - Accuracy: %.2f%%, Frames: %d/%d, Duration: %dms",
            accuracy * 100, correctFrames, totalFrames, duration));
        
        return new MotionAnalysisResult(accuracy, duration, totalFrames, correctFrames);
    }
    
    /**
     * Analyze a single frame and return feedback
     */
    public FormFeedback analyzeFrame(Object imageProxy) {
        if (!isAnalyzing) {
            return null;
        }
        
        totalFrames++;
        
        try {
            // Step 1: Extract joint positions from image
            Map<String, Point> jointPositions = jointTracker.trackJoints(imageProxy, currentExerciseId);
            
            // Step 2: Validate form based on joint positions
            FormValidator.ValidationResult validation = formValidator.validateForm(jointPositions);
            
            // Step 3: Generate feedback
            FormFeedback feedback = feedbackGenerator.generateFeedback(validation, jointPositions);
            
            // Update statistics
            if (feedback.isCorrectForm()) {
                correctFrames++;
            }
            
            return feedback;
            
        } catch (Exception e) {
            android.util.Log.e("MotionAnalyzer", "Error analyzing frame: " + e.getMessage());
            return createErrorFeedback();
        }
    }
    
    /**
     * Analyze frame with pre-extracted joint positions (for mock/testing)
     */
    public FormFeedback analyzeFrame(Map<String, Point> jointPositions) {
        if (!isAnalyzing) {
            return null;
        }
        
        totalFrames++;
        
        try {
            // Step 1: Validate form based on joint positions
            FormValidator.ValidationResult validation = formValidator.validateForm(jointPositions);
            
            // Step 2: Generate feedback
            FormFeedback feedback = feedbackGenerator.generateFeedback(validation, jointPositions);
            
            // Update statistics
            if (feedback.isCorrectForm()) {
                correctFrames++;
            }
            
            android.util.Log.d("MotionAnalyzer", String.format(
                "Frame analyzed - Accuracy: %.2f%%, Correct: %s, Total frames: %d/%d",
                feedback.getAccuracy() * 100, feedback.isCorrectForm(), correctFrames, totalFrames));
            
            return feedback;
            
        } catch (Exception e) {
            android.util.Log.e("MotionAnalyzer", "Error analyzing frame: " + e.getMessage());
            return createErrorFeedback();
        }
    }
    
    /**
     * Get current analysis statistics
     */
    public AnalysisStats getCurrentStats() {
        float currentAccuracy = totalFrames > 0 ? (float) correctFrames / totalFrames : 0.0f;
        long currentDuration = isAnalyzing ? System.currentTimeMillis() - analysisStartTime : 0;
        
        return new AnalysisStats(currentAccuracy, currentDuration, totalFrames, correctFrames);
    }
    
    /**
     * Check if analysis is currently active
     */
    public boolean isAnalyzing() {
        return isAnalyzing;
    }
    
    /**
     * Create error feedback when analysis fails
     */
    private FormFeedback createErrorFeedback() {
        return new FormFeedback(
            false,
            0.0f,
            "Unable to track movement. Please ensure good lighting and clear view.",
            null
        );
    }
    
    /**
     * Result class for motion analysis
     */
    public static class MotionAnalysisResult {
        private final float overallAccuracy;
        private final long sessionDuration;
        private final int totalFrames;
        private final int correctFrames;
        
        public MotionAnalysisResult(float overallAccuracy, long sessionDuration, 
                                  int totalFrames, int correctFrames) {
            this.overallAccuracy = overallAccuracy;
            this.sessionDuration = sessionDuration;
            this.totalFrames = totalFrames;
            this.correctFrames = correctFrames;
        }
        
        public float getOverallAccuracy() { return overallAccuracy; }
        public long getSessionDuration() { return sessionDuration; }
        public int getTotalFrames() { return totalFrames; }
        public int getCorrectFrames() { return correctFrames; }
    }
    
    /**
     * Statistics class for current analysis state
     */
    public static class AnalysisStats {
        private final float currentAccuracy;
        private final long currentDuration;
        private final int totalFrames;
        private final int correctFrames;
        
        public AnalysisStats(float currentAccuracy, long currentDuration, 
                           int totalFrames, int correctFrames) {
            this.currentAccuracy = currentAccuracy;
            this.currentDuration = currentDuration;
            this.totalFrames = totalFrames;
            this.correctFrames = correctFrames;
        }
        
        public float getCurrentAccuracy() { return currentAccuracy; }
        public long getCurrentDuration() { return currentDuration; }
        public int getTotalFrames() { return totalFrames; }
        public int getCorrectFrames() { return correctFrames; }
    }
}