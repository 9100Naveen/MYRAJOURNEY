package com.example.myrajourney.patient.rehab;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.myrajourney.R;

// NEW: Import our video exercise tracking system
import com.example.myrajourney.exercise.models.AnalysisResult;
import com.example.myrajourney.exercise.models.ExerciseTemplate;
import com.example.myrajourney.exercise.models.PoseFrame;
import com.example.myrajourney.exercise.tracking.VideoExerciseTracker;
import com.example.myrajourney.exercise.tracking.SensorTracker;
import com.example.myrajourney.exercise.tracking.PerformanceScorer;
import com.example.myrajourney.exercise.ui.FeedbackOverlayView;

// Keep existing imports for compatibility
import com.example.myrajourney.rehab.models.FormFeedback;
import com.example.myrajourney.rehab.models.RAExercise;
import com.example.myrajourney.rehab.services.ExerciseLibraryService;
import com.example.myrajourney.rehab.services.MotionTrackingService;
import com.example.myrajourney.rehab.services.SessionManager;
import com.example.myrajourney.rehab.services.impl.ExerciseLibraryServiceImpl;
import com.example.myrajourney.rehab.services.impl.MotionTrackingServiceImpl;
import com.example.myrajourney.rehab.services.impl.SessionManagerImpl;
import com.example.myrajourney.rehab.ui.MotionOverlayView;
import com.example.myrajourney.rehab.ui.VideoPlayerFragment;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

/**
 * Activity for conducting exercise sessions with split-screen video guidance
 * and motion tracking
 * ENHANCED: Now uses advanced video-based exercise tracking with ML Kit pose
 * detection
 */
public class ExerciseSessionActivity extends AppCompatActivity implements VideoExerciseTracker.TrackingCallback {

    private static final String TAG = "ExerciseSessionActivity";
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1001;

    // UI Components
    private ImageView btnBack;
    private ImageView btnSettings;
    private TextView txtExerciseName;
    private TextView txtSessionTimer;
    private TextView txtSessionStatus;
    private FrameLayout frameVideoSection;
    private VideoPlayerFragment videoPlayerFragment;
    private FrameLayout frameCameraSection;
    private PreviewView cameraPreview;

    // NEW: Enhanced feedback overlay using our video tracking system
    private FeedbackOverlayView feedbackOverlay;
    private MotionOverlayView motionOverlay; // Keep for compatibility

    private TextView txtFormAccuracy;
    private TextView txtFormFeedback;
    private LinearLayout layoutCameraError;
    private Button btnEnableCamera;
    private Button btnPause;
    private Button btnStop;
    private Button btnComplete;

    // Phone Placement Overlay
    private View layoutPlacementHint;
    private TextView txtPlacementDetails;
    private Button btnGotIt;

    // Services - Enhanced with our video tracking system
    private ExerciseLibraryService exerciseLibraryService;
    private MotionTrackingService motionTrackingService; // Keep for compatibility
    private SessionManager sessionManager;

    // NEW: Our advanced video exercise tracker
    private VideoExerciseTracker exerciseTracker;
    private ExerciseTemplate currentExerciseTemplate;

    // Camera
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ProcessCameraProvider cameraProvider;
    private Preview preview;
    private ImageAnalysis imageAnalysis;

    // Session Data
    private RAExercise currentExercise;
    private String currentSessionId;
    private boolean isSessionActive = false;
    private boolean isSessionPaused = false;
    private SensorTracker sensorTracker;
    private float sensorRom = 0;
    private int sensorReps = 0;
    private float sensorSmoothness = 100;
    private long sessionStartTime;
    private long pausedDuration = 0;
    private Handler timerHandler;
    private Runnable timerRunnable;

    // NEW: Enhanced tracking statistics
    private double currentAccuracyScore = 0.0;
    private int goodFormCount = 0;
    private int totalAnalysisCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        android.util.Log.d("ExerciseSession", "ExerciseSessionActivity onCreate started");

        try {
            setContentView(R.layout.activity_exercise_session);
            android.util.Log.d("ExerciseSession", "Layout set successfully");

            initializeServices();
            android.util.Log.d("ExerciseSession", "Services initialized");

            initializeViews();
            android.util.Log.d("ExerciseSession", "Views initialized");

            loadExerciseData();
            android.util.Log.d("ExerciseSession", "Exercise data loaded");

            setupListeners();
            android.util.Log.d("ExerciseSession", "Listeners set up");

            checkCameraPermission();
            android.util.Log.d("ExerciseSession", "Camera permission checked");

        } catch (Exception e) {
            android.util.Log.e("ExerciseSession", "Error in onCreate: " + e.getMessage());
            Toast.makeText(this, "Error loading exercise session: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopSession();
        if (timerHandler != null && timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
    }

    private void initializeServices() {
        exerciseLibraryService = new ExerciseLibraryServiceImpl();
        motionTrackingService = new MotionTrackingServiceImpl(this); // Keep for compatibility
        sessionManager = new SessionManagerImpl();

        // Fix tracker initialization (constructor takes Context only)
        exerciseTracker = new VideoExerciseTracker(this);

        // Initialize sensor tracker placeholder
        sensorTracker = null;

        // Set up motion tracking callback (legacy compatibility)
        motionTrackingService.setFormValidationCallback(new MotionTrackingService.FormValidationCallback() {
            @Override
            public void onFormFeedback(FormFeedback feedback) {
                runOnUiThread(() -> {
                    updateFormFeedback(feedback);
                    // Record motion data in session
                    if (currentSessionId != null && feedback != null) {
                        sessionManager.recordMotionData(currentSessionId, feedback);
                    }
                });
            }
        });
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        btnSettings = findViewById(R.id.btnSettings);
        txtExerciseName = findViewById(R.id.txtExerciseName);
        txtSessionTimer = findViewById(R.id.txtSessionTimer);
        txtSessionStatus = findViewById(R.id.txtSessionStatus);
        frameVideoSection = findViewById(R.id.frameVideoSection);

        // Initialize video player container
        // VideoPlayerFragment will be added dynamically in loadVideoContent
        frameVideoSection.setVisibility(View.GONE);
        frameCameraSection = findViewById(R.id.frameCameraSection);
        cameraPreview = findViewById(R.id.cameraPreview);

        // NEW: Initialize our enhanced feedback overlay
        feedbackOverlay = findViewById(R.id.feedback_overlay);
        if (feedbackOverlay == null) {
            // Create feedback overlay if not in layout
            feedbackOverlay = new FeedbackOverlayView(this);
            frameCameraSection.addView(feedbackOverlay);
        }

        motionOverlay = findViewById(R.id.motionOverlay); // Keep for compatibility
        txtFormAccuracy = findViewById(R.id.txtFormAccuracy);
        txtFormFeedback = findViewById(R.id.txtFormFeedback);
        layoutCameraError = findViewById(R.id.layoutCameraError);
        btnEnableCamera = findViewById(R.id.btnEnableCamera);
        btnPause = findViewById(R.id.btnPause);
        btnStop = findViewById(R.id.btnStop);
        btnComplete = findViewById(R.id.btnComplete);

        // Initialize placement overlay
        layoutPlacementHint = findViewById(R.id.layoutPlacementHint);
        txtPlacementDetails = findViewById(R.id.txtPlacementDetails);
        btnGotIt = findViewById(R.id.btnGotIt);

        // Initialize timer
        timerHandler = new Handler(Looper.getMainLooper());
    }

    private void loadExerciseData() {
        String exerciseId = getIntent().getStringExtra("exercise_id");
        String exerciseName = getIntent().getStringExtra("exercise_name");

        if (exerciseId != null) {
            currentExercise = exerciseLibraryService.getExerciseById(exerciseId);
        }

        // NEW: Create exercise template for our video tracking system
        createExerciseTemplate(exerciseId, exerciseName);

        // Initialize sensor tracker with actual exercise ID
        if (exerciseId != null) {
            sensorTracker = new SensorTracker(this, exerciseId);
        }

        // If exercise not found by ID, create a basic exercise object from intent data
        if (currentExercise == null && exerciseName != null) {
            // Create a basic exercise object for the session
            android.util.Log.d("ExerciseSession", "Creating basic exercise from intent data");
            // For now, just show the exercise name and continue
            if (txtExerciseName != null) {
                txtExerciseName.setText(exerciseName);
            }
            // Don't finish, let the session continue
            return;
        }

        if (currentExercise != null) {
            updateExerciseInfo();
            loadVideoContent();

            // Show placement hint
            if (layoutPlacementHint != null && txtPlacementDetails != null) {
                String hint = getPlacementHint(currentExercise.getId());
                txtPlacementDetails.setText("Place phone on your " + hint + " using the strap");
                layoutPlacementHint.setVisibility(View.VISIBLE);
            }
        } else if (exerciseName == null) {
            Toast.makeText(this, "No exercise specified", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * NEW: Create exercise template for our video tracking system
     */
    private void createExerciseTemplate(String exerciseId, String exerciseName) {
        currentExerciseTemplate = new ExerciseTemplate();

        if (exerciseId != null) {
            currentExerciseTemplate.setExerciseId(exerciseId);
        } else {
            currentExerciseTemplate.setExerciseId("default_exercise");
        }

        if (exerciseName != null) {
            currentExerciseTemplate.setExerciseName(exerciseName);
            currentExerciseTemplate.setDescription("Rehabilitation exercise for RA patients");
        } else {
            currentExerciseTemplate.setExerciseName("Rehabilitation Exercise");
            currentExerciseTemplate.setDescription("General rehabilitation exercise");
        }

        // Set exercise type based on name
        if (exerciseName != null) {
            if (exerciseName.toLowerCase().contains("shoulder")) {
                currentExerciseTemplate.setExerciseType(ExerciseTemplate.ExerciseType.SHOULDER_ROLL);
            } else if (exerciseName.toLowerCase().contains("finger")) {
                currentExerciseTemplate.setExerciseType(ExerciseTemplate.ExerciseType.FINGER_FLEXION);
            } else if (exerciseName.toLowerCase().contains("ankle")) {
                currentExerciseTemplate.setExerciseType(ExerciseTemplate.ExerciseType.ANKLE_CIRCLE);
            } else {
                currentExerciseTemplate.setExerciseType(ExerciseTemplate.ExerciseType.ARM_RAISE);
            }
        }

        currentExerciseTemplate.setDifficultyLevel(ExerciseTemplate.DifficultyLevel.BEGINNER);
        currentExerciseTemplate.setExpectedDurationSeconds(60); // 1 minute default

        // Add target joints based on exercise type
        switch (currentExerciseTemplate.getExerciseType()) {
            case SHOULDER_ROLL:
                currentExerciseTemplate.getTargetJoints().add("LEFT_SHOULDER");
                currentExerciseTemplate.getTargetJoints().add("RIGHT_SHOULDER");
                break;
            case FINGER_FLEXION:
                currentExerciseTemplate.getTargetJoints().add("LEFT_WRIST");
                currentExerciseTemplate.getTargetJoints().add("RIGHT_WRIST");
                break;
            case ANKLE_CIRCLE:
                currentExerciseTemplate.getTargetJoints().add("LEFT_ANKLE");
                currentExerciseTemplate.getTargetJoints().add("RIGHT_ANKLE");
                break;
            default:
                currentExerciseTemplate.getTargetJoints().add("LEFT_ELBOW");
                currentExerciseTemplate.getTargetJoints().add("RIGHT_ELBOW");
                break;
        }

        android.util.Log.d("ExerciseSession",
                "Created exercise template: " + currentExerciseTemplate.getExerciseName());
    }

    private void updateExerciseInfo() {
        txtExerciseName.setText(currentExercise.getName());
        loadVideoContent();
    }

    private void loadVideoContent() {
        if (currentExercise != null) {
            String videoUrl = exerciseLibraryService.getExerciseVideoUrl(currentExercise.getId());
            android.util.Log.d("ExerciseSession", "Loading video content logic. URL: " + videoUrl);

            if (videoUrl != null) {
                frameVideoSection.setVisibility(View.VISIBLE);

                // Initialize Fragment
                videoPlayerFragment = VideoPlayerFragment.newInstance(videoUrl, currentExercise.getName());
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frameVideoSection, videoPlayerFragment)
                        .commit();

                android.util.Log.d("ExerciseSession", "VideoPlayerFragment added to transaction");
            } else {
                android.util.Log.w("ExerciseSession", "No video URL for exercise: " + currentExercise.getName());
                frameVideoSection.setVisibility(View.GONE);
            }
        } else {
            frameVideoSection.setVisibility(View.GONE);
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> {
            if (isSessionActive) {
                showExitConfirmationDialog();
            } else {
                finish();
            }
        });

        btnSettings.setOnClickListener(v -> {
            // TODO: Show session settings (camera position, feedback sensitivity, etc.)
        });

        btnEnableCamera.setOnClickListener(v -> checkCameraPermission());

        btnPause.setOnClickListener(v -> togglePauseSession());

        btnStop.setOnClickListener(v -> showStopConfirmationDialog());

        btnComplete.setOnClickListener(v -> completeSession());

        if (btnGotIt != null) {
            btnGotIt.setOnClickListener(v -> {
                if (layoutPlacementHint != null) {
                    layoutPlacementHint.setVisibility(View.GONE);
                }
            });
        }
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            setupCamera();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.CAMERA },
                    CAMERA_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupCamera();
            } else {
                showCameraPermissionDeniedState();
            }
        }
    }

    private void setupCamera() {
        layoutCameraError.setVisibility(View.GONE);
        cameraPreview.setVisibility(View.VISIBLE);

        // Initialize CameraX
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases();
                startSession();
            } catch (ExecutionException | InterruptedException e) {
                android.util.Log.e("ExerciseSession", "Error setting up camera: " + e.getMessage());
                Toast.makeText(this, "Failed to initialize camera", Toast.LENGTH_SHORT).show();
                showCameraPermissionDeniedState();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraUseCases() {
        if (cameraProvider == null)
            return;

        // Unbind all use cases before rebinding
        cameraProvider.unbindAll();

        // Set up camera selector (front camera for self-monitoring)
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build();

        // Set up preview use case
        preview = new Preview.Builder()
                .setTargetResolution(new android.util.Size(640, 480))
                .build();
        preview.setSurfaceProvider(cameraPreview.getSurfaceProvider());

        // Set up image analysis use case for ENHANCED video-based motion tracking
        imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setTargetResolution(new android.util.Size(640, 480))
                .build();

        // NEW: Connect to our advanced video exercise tracker
        imageAnalysis.setAnalyzer(java.util.concurrent.Executors.newSingleThreadExecutor(),
                imageProxy -> {
                    // Pass frames to our advanced video exercise tracker
                    if (exerciseTracker != null) {
                        exerciseTracker.processFrame(imageProxy);
                    } else {
                        // Fallback to legacy motion tracking
                        if (motionTrackingService != null && motionTrackingService.isTrackingActive()) {
                            ((MotionTrackingServiceImpl) motionTrackingService).analyzeFrame(imageProxy);
                        }
                        imageProxy.close();
                    }
                });

        // Bind use cases to camera
        try {
            cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageAnalysis);

            android.util.Log.d("ExerciseSession", "Camera bound successfully with REAL-TIME motion tracking");
        } catch (Exception e) {
            android.util.Log.e("ExerciseSession", "Error binding camera: " + e.getMessage());
        }
    }

    private void showCameraPermissionDeniedState() {
        layoutCameraError.setVisibility(View.VISIBLE);
        cameraPreview.setVisibility(View.GONE);

        // Allow session to continue without motion tracking
        startSession();
    }

    private void startSession() {
        if (!isSessionActive) {
            // Create session in session manager
            String patientId = "patient_001"; // TODO: Get from user session
            String exerciseId = currentExercise != null ? currentExercise.getId() : "default";

            sessionManager.startSession(patientId, exerciseId)
                    .thenAccept(sessionId -> {
                        runOnUiThread(() -> {
                            currentSessionId = sessionId;
                            isSessionActive = true;
                            isSessionPaused = false;
                            sessionStartTime = System.currentTimeMillis();
                            pausedDuration = 0;

                            startTimer();
                            updateSessionControls();

                            // Start motion tracking if camera is available
                            if (cameraPreview.getVisibility() == View.VISIBLE && exerciseTracker != null) {
                                // NEW: Use our advanced video exercise tracker
                                if (currentExerciseTemplate != null) {
                                    exerciseTracker.startTracking(currentExerciseTemplate,
                                            ExerciseSessionActivity.this);
                                    android.util.Log.d("ExerciseSession", "Advanced video tracking started for: "
                                            + currentExerciseTemplate.getExerciseName());
                                }
                            }

                            // Start IMU sensor tracking
                            if (sensorTracker != null) {
                                sensorTracker.start((reps, rom, smoothness) -> {
                                    this.sensorReps = reps;
                                    this.sensorRom = rom;
                                    this.sensorSmoothness = smoothness;

                                    // Calculate hybrid score
                                    double hybridScore = PerformanceScorer.calculateSensorScore(
                                            rom, 80.0f, // Target ROM 80 deg
                                            reps, 10, // Target 10 reps
                                            smoothness, 100.0f);

                                    if (reps > 0 || rom > 10) {
                                        runOnUiThread(() -> {
                                            currentAccuracyScore = hybridScore;
                                            if (txtFormAccuracy != null) {
                                                txtFormAccuracy.setText(String.format("%.0f%%", currentAccuracyScore));
                                            }
                                        });
                                    }
                                });

                                // Show placement hint
                                if (txtFormFeedback != null) {
                                    txtFormFeedback.setText("PLACE PHONE ON " + getPlacementHint(exerciseId));
                                }
                            }

                            // Start exercise video if paused or handled by fragment
                            if (videoPlayerFragment != null) {
                                // Fragment usually auto-plays, but ensuring resume
                                videoPlayerFragment.resumeVideo();
                            }

                            Toast.makeText(this, "Exercise session started", Toast.LENGTH_SHORT).show();
                        });
                    })
                    .exceptionally(throwable -> {
                        runOnUiThread(() -> {
                            android.util.Log.e("ExerciseSession", "Failed to start session: " + throwable.getMessage());
                            Toast.makeText(this, "Failed to start session", Toast.LENGTH_SHORT).show();
                        });
                        return null;
                    });
        }
    }

    private void togglePauseSession() {
        if (!isSessionActive || currentSessionId == null)
            return;

        if (isSessionPaused) {
            // Resume session
            sessionManager.resumeSession(currentSessionId);
            isSessionPaused = false;
            sessionStartTime = System.currentTimeMillis() - pausedDuration;
            startTimer();

            // Resume motion tracking
            if (cameraPreview.getVisibility() == View.VISIBLE) {
                // NEW: Use advanced video tracker if available
                if (exerciseTracker != null && currentExerciseTemplate != null) {
                    exerciseTracker.startTracking(currentExerciseTemplate, ExerciseSessionActivity.this);
                }
            }

            if (sensorTracker != null) {
                // Sensor start (callback is usually kept or needs re-registration)
                // For simplicity, we restart it
                startSession(); // Or just start the tracker
            }

            // Resume animation
            if (videoPlayerFragment != null) {
                videoPlayerFragment.resumeVideo();
            }

            Toast.makeText(this, "Session resumed", Toast.LENGTH_SHORT).show();
        } else {
            // Pause session
            sessionManager.pauseSession(currentSessionId);
            isSessionPaused = true;
            pausedDuration = System.currentTimeMillis() - sessionStartTime;
            stopTimer();

            // Pause motion tracking (stop and restart when resumed)
            if (exerciseTracker != null && exerciseTracker.isTracking()) {
                exerciseTracker.stopTracking();
            }

            if (sensorTracker != null) {
                sensorTracker.stop();
            }

            // Pause animation
            if (videoPlayerFragment != null) {
                videoPlayerFragment.pauseVideo();
            }

            Toast.makeText(this, "Session paused", Toast.LENGTH_SHORT).show();
        }

        updateSessionControls();
    }

    private void stopSession() {
        if (isSessionActive && currentSessionId != null) {
            isSessionActive = false;
            isSessionPaused = false;
            stopTimer();

            // Stop motion tracking and get results
            if (exerciseTracker != null && exerciseTracker.isTracking()) {
                exerciseTracker.stopTracking();
                android.util.Log.d("ExerciseSession", "Advanced video tracking stopped");
            }

            if (sensorTracker != null) {
                sensorTracker.stop();
            }
        } else if (motionTrackingService != null && motionTrackingService.isTrackingActive()) {
            MotionTrackingService.MotionAnalysisResult result = motionTrackingService.stopTracking();
            android.util.Log.d("ExerciseSession",
                    "Legacy tracking stopped with accuracy: " + result.getOverallAccuracy());
        }

        // Cancel session in session manager
        sessionManager.cancelSession(currentSessionId)
                .thenAccept(cancelled -> {
                    runOnUiThread(() -> {
                        android.util.Log.d("ExerciseSession", "Session cancelled: " + currentSessionId);
                        currentSessionId = null;
                    });
                });

        // Clear motion overlay
        if (motionOverlay != null) {
            motionOverlay.clearOverlay();
        }

        updateSessionControls();
    }

    private String getPlacementHint(String exerciseId) {
        if (exerciseId == null)
            return "BODY";
        String id = exerciseId.toLowerCase();
        if (id.contains("001") || id.contains("002") || id.contains("flexion"))
            return "WRIST (STRAP)";
        if (id.contains("008") || id.contains("knee"))
            return "THIGH (STRAP)";
        if (id.contains("009") || id.contains("010") || id.contains("hip"))
            return "THIGH (STRAP)";
        if (id.contains("shoulder"))
            return "UPPER ARM";
        return "MOVING LIMB";
    }

    private void completeSession() {
        if (isSessionActive && currentSessionId != null) {
            // Stop motion tracking first
            if (exerciseTracker != null && exerciseTracker.isTracking()) {
                exerciseTracker.stopTracking();
            }

            if (sensorTracker != null) {
                sensorTracker.stop();
            }

            // Complete session in session manager
            sessionManager.completeSession(currentSessionId)
                    .thenAccept(result -> {
                        runOnUiThread(() -> {
                            if (result != null) {
                                android.util.Log.d("ExerciseSession", String.format(
                                        "Session completed - Accuracy: %.2f%%, Duration: %ds",
                                        result.getOverallAccuracy() * 100, result.getDurationSeconds()));

                                Toast.makeText(this, String.format(
                                        "Session completed! Accuracy: %.0f%%",
                                        result.getOverallAccuracy() * 100), Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(this, "Exercise session completed!", Toast.LENGTH_SHORT).show();
                            }

                            isSessionActive = false;
                            isSessionPaused = false;
                            // MOVED: currentSessionId = null; // Don't nullify yet, we need it for the
                            // report intent below
                            stopTimer();
                            updateSessionControls();

                            // Clear motion overlay
                            if (motionOverlay != null) {
                                motionOverlay.clearOverlay();
                            }

                            // Navigate to performance report screen
                            Intent reportIntent = new Intent(ExerciseSessionActivity.this,
                                    ExercisePerformanceReportActivity.class);
                            reportIntent.putExtra("session_id", currentSessionId);
                            if (currentExercise != null) {
                                reportIntent.putExtra("exercise_id", currentExercise.getId());
                            }
                            startActivity(reportIntent);

                            // NOW nullify it
                            currentSessionId = null;
                            finish();
                        });
                    })
                    .exceptionally(throwable -> {
                        runOnUiThread(() -> {
                            android.util.Log.e("ExerciseSession",
                                    "Failed to complete session: " + throwable.getMessage());
                            Toast.makeText(this, "Failed to save session data", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                        return null;
                    });
        }
    }

    private void startTimer() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (isSessionActive && !isSessionPaused) {
                    long elapsedTime = System.currentTimeMillis() - sessionStartTime;
                    updateTimerDisplay(elapsedTime);
                    timerHandler.postDelayed(this, 1000);
                }
            }
        };
        timerHandler.post(timerRunnable);
    }

    private void stopTimer() {
        if (timerHandler != null && timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
    }

    private void updateTimerDisplay(long elapsedTimeMs) {
        long seconds = elapsedTimeMs / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;

        String timeString = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        txtSessionTimer.setText(timeString);
    }

    private void updateSessionControls() {
        if (isSessionActive) {
            if (isSessionPaused) {
                btnPause.setText("Resume");
                txtSessionStatus.setText("Paused");
                txtSessionStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_light));
            } else {
                btnPause.setText("Pause");
                txtSessionStatus.setText("Active");
                txtSessionStatus.setTextColor(ContextCompat.getColor(this, R.color.success));
            }
            btnPause.setEnabled(true);
            btnStop.setEnabled(true);
            btnComplete.setEnabled(true);
        } else {
            btnPause.setText("Pause");
            btnPause.setEnabled(false);
            btnStop.setEnabled(false);
            btnComplete.setEnabled(false);
            txtSessionStatus.setText("Ready");
            txtSessionStatus.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        }
    }

    private void showExitConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Exit Exercise Session")
                .setMessage("Are you sure you want to exit? Your progress will be lost.")
                .setPositiveButton("Exit", (dialog, which) -> {
                    stopSession();
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showStopConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Stop Exercise Session")
                .setMessage("Are you sure you want to stop the session? Your progress will be saved.")
                .setPositiveButton("Stop", (dialog, which) -> {
                    stopSession();
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Update form feedback from motion tracking
     */
    private void updateFormFeedback(FormFeedback feedback) {
        if (feedback == null)
            return;

        // Update accuracy display
        if (txtFormAccuracy != null) {
            String accuracyText = String.format(Locale.getDefault(), "%.0f%%", feedback.getAccuracy() * 100);
            txtFormAccuracy.setText(accuracyText);

            // Change color based on accuracy
            if (feedback.getAccuracy() > 0.8f) {
                txtFormAccuracy.setTextColor(ContextCompat.getColor(this, R.color.success));
            } else if (feedback.getAccuracy() > 0.6f) {
                txtFormAccuracy.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_light));
            } else {
                txtFormAccuracy.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_light));
            }
        }

        // Update feedback message
        if (txtFormFeedback != null && feedback.getSpecificFeedback() != null) {
            txtFormFeedback.setText(feedback.getSpecificFeedback());
            txtFormFeedback.setVisibility(View.VISIBLE);

            // Set background color based on form correctness
            if (feedback.isCorrectForm()) {
                txtFormFeedback.setBackgroundResource(R.drawable.rounded_background_success);
            } else {
                txtFormFeedback.setBackgroundResource(R.drawable.rounded_background_dark);
            }

            // Auto-hide feedback after 3 seconds
            txtFormFeedback.postDelayed(() -> {
                if (txtFormFeedback != null) {
                    txtFormFeedback.setVisibility(View.GONE);
                }
            }, 3000);
        }

        // Update motion overlay with joint positions
        if (motionOverlay != null) {
            motionOverlay.updateJointPositions(feedback.getJointPositions());
            motionOverlay.updateAccuracy(feedback.getAccuracy());
        }
    }

    // NEW: VideoExerciseTracker.TrackingCallback implementation

    @Override
    public void onPoseDetected(PoseFrame poseFrame) {
        runOnUiThread(() -> {
            if (feedbackOverlay != null) {
                feedbackOverlay.updatePose(poseFrame.getPose());
            }
        });
    }

    @Override
    public void onAnalysisResult(AnalysisResult result) {
        // Update statistics
        totalAnalysisCount++;

        // Use sensor score if valid, otherwise fallback to camera score
        if (sensorTracker != null && (sensorReps > 0 || sensorRom > 5)) {
            // Already updated in sensor callback, but we sync statistics here
            currentAccuracyScore = PerformanceScorer.calculateSensorScore(
                    sensorRom, 80.0f, sensorReps, 10, sensorSmoothness, 100.0f);
        } else {
            currentAccuracyScore = result.getOverallScore();
        }

        if (currentAccuracyScore >= 75) {
            goodFormCount++;
        }

        // Record motion data in session for reporting
        if (currentSessionId != null) {
            FormFeedback sessionFeedback = new FormFeedback(
                    currentAccuracyScore >= 70,
                    (float) (currentAccuracyScore / 100.0),
                    result.getFeedback().getPrimaryMessage(),
                    null);
            sessionManager.recordMotionData(currentSessionId, sessionFeedback);
        }

        runOnUiThread(() -> {
            // Update accuracy display
            if (txtFormAccuracy != null) {
                // REMOVED: delay check to show score immediately
                txtFormAccuracy.setText(String.format("%.0f%%", currentAccuracyScore));

                // Change color based on accuracy
                if (currentAccuracyScore > 80) {
                    txtFormAccuracy.setTextColor(ContextCompat.getColor(this, R.color.success));
                } else if (currentAccuracyScore > 60) {
                    txtFormAccuracy.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_light));
                } else {
                    txtFormAccuracy.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_light));
                }
            }

            // Update feedback text (SUPPORTIVE HINTS)
            AnalysisResult.FeedbackData feedback = result.getFeedback();
            if (txtFormFeedback != null) {
                String hint = feedback.getPrimaryMessage();
                if (sensorTracker != null && (sensorReps == 0 && sensorRom < 5)) {
                    txtFormFeedback.setText("ADJUST PHONE placement for better tracking...");
                } else {
                    txtFormFeedback.setText(hint); // Guidance only
                }
            }

            // Update feedback overlay
            if (feedbackOverlay != null) {
                feedbackOverlay.setFeedbackColor(feedback.getVisualIndicatorColor());
            }

            // Provide audio feedback (limit frequency)
            if (totalAnalysisCount % 10 == 0) { // Every 10th analysis
                String[] audioInstructions = feedback.getAudioInstructions();
                if (audioInstructions.length > 0) {
                    // Use TTS if available, otherwise just log
                    android.util.Log.d("ExerciseSession", "Audio feedback: " + audioInstructions[0]);
                }
            }
        });
    }

    @Override
    public void onTrackingError(String error) {
        Log.e(TAG, "Video tracking error: " + error);
        runOnUiThread(() -> {
            Toast.makeText(this, "Tracking error: " + error, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onTrackingStarted() {
        Log.d(TAG, "Video exercise tracking started");
        runOnUiThread(() -> {
            if (txtFormFeedback != null) {
                txtFormFeedback.setText("Video tracking active");
            }
        });
    }

    @Override
    public void onTrackingStopped() {
        Log.d(TAG, "Video exercise tracking stopped");
        runOnUiThread(() -> {
            if (txtFormFeedback != null) {
                txtFormFeedback.setText("Tracking stopped");
            }

            // Show final results
            if (totalAnalysisCount > 0) {
                double accuracy = (double) goodFormCount / totalAnalysisCount * 100.0;
                String message = String.format("Session completed!\nAccuracy: %.1f%% (%d/%d good form)",
                        accuracy, goodFormCount, totalAnalysisCount);
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (isSessionActive) {
            showExitConfirmationDialog();
        } else {
            super.onBackPressed();
        }
    }
}