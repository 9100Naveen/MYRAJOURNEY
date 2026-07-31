package com.example.myrajourney.rehab.services;

import androidx.camera.view.PreviewView;
import com.example.myrajourney.rehab.models.FormFeedback;

/**
 * Service interface for motion tracking and form validation
 */
public interface MotionTrackingService {

    /**
     * Start motion tracking for an exercise
     * 
     * @param exerciseId    The exercise ID
     * @param cameraPreview The camera preview view
     */
    void startTracking(String exerciseId, PreviewView cameraPreview);

    /**
     * Stop motion tracking and get results
     * 
     * @return Motion analysis result
     */
    MotionAnalysisResult stopTracking();

    /**
     * Get current form accuracy percentage
     * 
     * @return Current accuracy (0.0 to 1.0)
     */
    float getCurrentFormAccuracy();

    /**
     * Set callback for real-time form validation feedback
     * 
     * @param callback Callback to receive form feedback
     */
    void setFormValidationCallback(FormValidationCallback callback);

    /**
     * Check if motion tracking is currently active
     * 
     * @return true if tracking is active
     */
    boolean isTrackingActive();

    /**
     * Analyze a single frame (ImageProxy) from the camera
     * 
     * @param imageProxy The camera frame to analyze
     */
    void analyzeFrame(androidx.camera.core.ImageProxy imageProxy);

    /**
     * Callback interface for form validation feedback
     */
    interface FormValidationCallback {
        void onFormFeedback(FormFeedback feedback);

        // Added for progress tracking
        default void onProgressUpdate(int repetitionCount, float currentAngle) {
        }

        // Added for repetition completion
        default void onRepetitionCompleted(int totalReps) {
        }

        // Added for error handling
        default void onError(String error) {
        }
    }

    /**
     * Result class for motion analysis
     */
    class MotionAnalysisResult {
        private float overallAccuracy;
        private long sessionDuration;
        private int totalFrames;
        private int correctFrames;

        public MotionAnalysisResult(float overallAccuracy, long sessionDuration,
                int totalFrames, int correctFrames) {
            this.overallAccuracy = overallAccuracy;
            this.sessionDuration = sessionDuration;
            this.totalFrames = totalFrames;
            this.correctFrames = correctFrames;
        }

        // Getters
        public float getOverallAccuracy() {
            return overallAccuracy;
        }

        public long getSessionDuration() {
            return sessionDuration;
        }

        public long getDurationSeconds() {
            return sessionDuration / 1000;
        }

        public int getTotalFrames() {
            return totalFrames;
        }

        public int getCorrectFrames() {
            return correctFrames;
        }
    }
}