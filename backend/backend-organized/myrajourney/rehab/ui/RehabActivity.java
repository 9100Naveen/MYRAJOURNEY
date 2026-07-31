package com.example.myrajourney.rehab.ui;

import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageProxy;
import androidx.fragment.app.FragmentTransaction;
import com.example.myrajourney.R;
import com.example.myrajourney.rehab.models.FormFeedback;
import com.example.myrajourney.rehab.services.MotionTrackingService;
import com.example.myrajourney.rehab.services.impl.MotionTrackingServiceImpl;

/**
 * Main Activity for Live Rehabilitation Session with Split-Screen View
 * Hosts VideoPlayerFragment (Guide) and CameraPreviewFragment (User)
 * Orchestrates Real-Time Motion Tracking and Feedback
 */
public class RehabActivity extends AppCompatActivity {

    public static final String EXTRA_EXERCISE_ID = "exercise_id";
    public static final String EXTRA_EXERCISE_NAME = "exercise_name";
    public static final String EXTRA_VIDEO_URL = "video_url";

    private String exerciseId;
    private String exerciseName;
    private String videoUrl;

    private MotionTrackingService motionTrackingService;
    private MotionOverlayView overlayView;
    private TextView feedbackTextView;
    private TextView scoreTextView;
    private CameraPreviewFragment cameraFragment;
    private VideoPlayerFragment videoFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get Intent Data
        if (getIntent() != null) {
            exerciseId = getIntent().getStringExtra(EXTRA_EXERCISE_ID);
            exerciseName = getIntent().getStringExtra(EXTRA_EXERCISE_NAME);
            videoUrl = getIntent().getStringExtra(EXTRA_VIDEO_URL);
        }

        if (exerciseId == null) {
            exerciseId = "ex_001"; // Fallback for testing
            exerciseName = "Wrist Flexion";
        }

        // Setup Main Layout (Programmatically to avoid XML dep)
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

        // Video Container (Top Half)
        FrameLayout videoContainer = new FrameLayout(this);
        videoContainer.setId(android.view.View.generateViewId());
        videoContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f));
        mainLayout.addView(videoContainer);

        // Camera Container (Bottom Half)
        FrameLayout cameraContainer = new FrameLayout(this);
        cameraContainer.setId(android.view.View.generateViewId());
        cameraContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f));
        mainLayout.addView(cameraContainer);

        setContentView(mainLayout);

        // Add Overlay View to Camera Container (on top of fragment)
        overlayView = new MotionOverlayView(this);
        cameraContainer.addView(overlayView);

        // Add Score Display (above feedback text)
        scoreTextView = new TextView(this);
        scoreTextView.setTextSize(24);
        scoreTextView.setTextColor(0xFF4CAF50); // Green color
        scoreTextView.setBackgroundColor(0x80000000); // Semi-transparent black
        scoreTextView.setPadding(32, 16, 32, 16);
        scoreTextView.setGravity(android.view.Gravity.CENTER);
        scoreTextView.setText("Score: 0%");
        scoreTextView.setTypeface(null, android.graphics.Typeface.BOLD);

        FrameLayout.LayoutParams scoreParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        scoreParams.gravity = android.view.Gravity.BOTTOM;
        scoreParams.bottomMargin = 120; // Above feedback text
        cameraContainer.addView(scoreTextView, scoreParams);

        // Add Feedback Text
        feedbackTextView = new TextView(this);
        feedbackTextView.setTextSize(18);
        feedbackTextView.setTextColor(0xFFFFFFFF);
        feedbackTextView.setBackgroundColor(0x80000000); // Semi-transparent black
        feedbackTextView.setPadding(32, 16, 32, 16);
        feedbackTextView.setGravity(android.view.Gravity.CENTER);

        FrameLayout.LayoutParams textParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        textParams.gravity = android.view.Gravity.BOTTOM;
        textParams.bottomMargin = 50;
        cameraContainer.addView(feedbackTextView, textParams);

        // Initialize Services
        motionTrackingService = new MotionTrackingServiceImpl(this);
        setupMotionTracking();

        // Load Fragments
        if (savedInstanceState == null) {
            loadFragments(videoContainer.getId(), cameraContainer.getId());
        }
    }

    private void loadFragments(int videoContainerId, int cameraContainerId) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        // Video Player
        videoFragment = VideoPlayerFragment.newInstance(videoUrl, exerciseName);
        ft.replace(videoContainerId, videoFragment);

        // Camera Preview
        cameraFragment = CameraPreviewFragment.newInstance(true); // usage front camera
        ft.replace(cameraContainerId, cameraFragment);

        ft.commit();
    }

    private void setupMotionTracking() {
        motionTrackingService.setFormValidationCallback(new MotionTrackingService.FormValidationCallback() {
            @Override
            public void onFormFeedback(FormFeedback feedback) {
                runOnUiThread(() -> updateUI(feedback));
            }

            @Override
            public void onProgressUpdate(int repetitionCount, float currentAngle) {
                // Handle progress bar update if needed
            }

            @Override
            public void onRepetitionCompleted(int totalReps) {
                runOnUiThread(() -> Toast
                        .makeText(RehabActivity.this, "Rep " + totalReps + " Completed!", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast
                        .makeText(RehabActivity.this, "Error: " + error, Toast.LENGTH_LONG).show());
            }
        });
    }

    private void updateUI(FormFeedback feedback) {
        if (feedback == null)
            return;

        // Update Score Display
        float score = feedback.getAccuracy();
        scoreTextView.setText(String.format("Score: %.0f%%", score));

        // Color code the score (red < 50, yellow 50-75, green > 75)
        if (score < 50) {
            scoreTextView.setTextColor(0xFFFF5252); // Red
        } else if (score < 75) {
            scoreTextView.setTextColor(0xFFFFEB3B); // Yellow
        } else {
            scoreTextView.setTextColor(0xFF4CAF50); // Green
        }

        // Update Feedback Text
        feedbackTextView.setText(feedback.getFeedbackMessage());
        feedbackTextView.setTextColor(feedback.isCorrectForm() ? 0xFF00FF00 : 0xFFFF0000); // Green or Red

        // Update Overlay (Draw Skeleton)
        if (overlayView != null && feedback.getJointPositions() != null) {
            overlayView.updateJoints(feedback.getJointPositions());
            overlayView.bringToFront(); // Ensure overlay is on top of camera preview
        }
        if (feedbackTextView != null) {
            feedbackTextView.bringToFront(); // Ensure feedback text is visible
        }
        if (scoreTextView != null) {
            scoreTextView.bringToFront(); // Ensure score is visible
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Delay starting tracking slightly to ensure camera is ready
        // In a real app, rely on CameraPreviewFragment callback
        // Here we hook into the fragment's callback setter

        // Use a Handler to check if fragment is ready or use Wait
        new android.os.Handler().postDelayed(this::bindCameraAnalysis, 1000);
    }

    private void bindCameraAnalysis() {
        if (cameraFragment != null) {
            cameraFragment.setImageAnalysisCallback(imageProxy -> {
                // On first frame, start the reference video
                if (videoFragment != null && motionTrackingService != null
                        && !motionTrackingService.isTrackingActive()) {
                    runOnUiThread(() -> videoFragment.startVideo());
                }

                // Pass frame to service
                motionTrackingService.analyzeFrame(imageProxy);
                // NOTE: MotionTrackingService (AdvancedMotionTracker) is now responsible for
                // closing the proxy
                // via its ML Kit listener logic.
            });

            // Start Tracking Service
            motionTrackingService.startTracking(exerciseId, cameraFragment.getPreviewView());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (motionTrackingService != null && motionTrackingService.isTrackingActive()) {
            motionTrackingService.stopTracking();
        }
    }
}
