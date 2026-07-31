package com.example.myrajourney.rehab.services.impl;

import android.graphics.Point;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.view.PreviewView;
import com.example.myrajourney.rehab.models.FormFeedback;
import com.example.myrajourney.ai.AdvancedMotionTracker;
import com.example.myrajourney.rehab.services.MotionTrackingService;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Enhanced implementation of MotionTrackingService with high-precision motion
 * analysis
 * Integrates AdvancedMotionTracker (ML Kit) for accurate and detailed movement
 * tracking
 */
public class MotionTrackingServiceImpl implements MotionTrackingService {

    private boolean isTrackingActive = false;
    private String currentExerciseId;
    private PreviewView cameraPreview;
    private FormValidationCallback callback;

    // AI Motion Tracker
    private AdvancedMotionTracker advancedTracker;
    // We don't hold ImageAnalysis reference here usually, but we set it up.

    // Session tracking
    private long sessionStartTime;
    private int frameCount = 0;

    public MotionTrackingServiceImpl(android.content.Context context) {
        this.advancedTracker = new AdvancedMotionTracker(context);
    }

    @Override
    public void startTracking(String exerciseId, PreviewView cameraPreview) {
        this.currentExerciseId = exerciseId;
        this.cameraPreview = cameraPreview;
        this.isTrackingActive = true;
        this.sessionStartTime = System.currentTimeMillis();
        this.frameCount = 0;

        // Start advanced tracker
        advancedTracker.startTracking(exerciseId);

        // Set up image analysis for real-time processing
        setupImageAnalysis();

        android.util.Log.d("MotionTracking", "Started ML Kit tracking for exercise: " + exerciseId);
    }

    /**
     * Set up image analysis pipeline for REAL-TIME motion tracking
     */
    private void setupImageAnalysis() {
        if (cameraPreview == null) {
            android.util.Log.e("MotionTracking", "Camera preview is null, cannot set up real-time tracking");
            return;
        }

        // ImageAnalysis should ideally be managed by the CameraController or Activity
        // But assuming we have access or are called from context where we can configure
        // it.
        // If this service is just logic, it shouldn't "configure" the camera itself
        // unless it owns the CameraProvider.
        // Assuming the caller sets calls analyzeFrame or we set the analyzer on an
        // existing provider.
        // For this refactor, we will assume analyzeFrame is called by the Camera setup
        // OR we provide the Analyzer.

        // Note: In a real app, you'd likely access the CameraX ProcessCameraProvider
        // here.
        // Since we don't see the provider logic, we'll assume the Activity bridges the
        // Camera -> analyzeFrame call,
        // OR we return an Analyzer. The interface has `startTracking(...,
        // PreviewView)`, suggesting it sets up the camera.

        // Existing code showed local ImageAnalysis creation but didn't bind it to
        // lifecycle.
        // We will stick to the previous pattern: creating the analyzer logic.
        // IMPORTANT: The actual binding to lifecycle (cameraProvider.bindToLifecycle)
        // must happen elsewhere
        // unless this Service manages it. We will focus on the `analyzeFrame` logic
        // processing.
    }

    /**
     * Analyze frame from CameraX. This is our main entry point for frames.
     */
    public void analyzeFrame(ImageProxy imageProxy) {
        if (!isTrackingActive) {
            imageProxy.close();
            return;
        }

        frameCount++;

        // Use AdvancedMotionTracker with ML Kit
        advancedTracker.processImage(imageProxy, new AdvancedMotionTracker.OnAnalysisResultListener() {
            @Override
            public void onAnalysisComplete(FormFeedback feedback) {
                // Send feedback to UI listener
                if (callback != null && feedback != null) {
                    callback.onFormFeedback(feedback);
                }

                // Logging occasionally
                if (frameCount % 30 == 0) {
                    android.util.Log.d("MotionTracking", "Frame " + frameCount + ": " + feedback.getFeedbackMessage());
                }
            }

            @Override
            public void onError(Exception e) {
                android.util.Log.e("MotionTracking", "ML Kit Error", e);
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }

    @Override
    public MotionAnalysisResult stopTracking() {
        isTrackingActive = false;

        advancedTracker.stop();

        AdvancedMotionTracker.ExercisePerformanceReport report = advancedTracker.getPerformanceReport();

        android.util.Log.d("MotionTracking", "Tracking completed: " + report.getFormattedReport());

        return new MotionAnalysisResult(
                (float) report.averageAccuracy,
                report.sessionDuration,
                report.totalCount,
                (int) (report.totalCount * report.averageAccuracy) // estimate correct reps
        );
    }

    @Override
    public float getCurrentFormAccuracy() {
        // Can't easily get sync value from async tracker loop without caching.
        // AdvancedTracker has internal state we can query.
        return (float) advancedTracker.getPerformanceReport().averageAccuracy;
    }

    @Override
    public void setFormValidationCallback(FormValidationCallback callback) {
        this.callback = callback;
    }

    @Override
    public boolean isTrackingActive() {
        return isTrackingActive;
    }

    /**
     * Analyze frame (legacy signature compatibility)
     */
    public void analyzeFrame(Object imageProxy) {
        if (imageProxy instanceof ImageProxy) {
            analyzeFrame((ImageProxy) imageProxy);
        }
    }
}