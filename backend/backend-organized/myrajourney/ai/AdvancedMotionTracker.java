package com.example.myrajourney.ai;

import android.graphics.Point;
import android.graphics.PointF;
import android.media.Image;
import androidx.annotation.NonNull;
import androidx.camera.core.ImageProxy;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions;
import com.example.myrajourney.rehab.models.FormFeedback;
import com.example.myrajourney.exercise.tracking.PerformanceScorer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Advanced AI-powered motion tracker with angle detection and exercise counting
 * Uses Google ML Kit Pose Detection for precise RA exercise monitoring
 */
public class AdvancedMotionTracker {

    // AI Model Configuration
    private static final double ANGLE_PRECISION_THRESHOLD = 5.0; // Degrees
    private static final double FORM_ACCURACY_THRESHOLD = 0.85;

    // Exercise-specific angle ranges and targets
    private Map<String, ExerciseAngleConfig> exerciseConfigs;

    // Tracking state
    private String currentExerciseId;
    private List<AngleFrame> angleHistory;
    private ExerciseCounter exerciseCounter;
    private PoseDetector poseDetector;
    private Executor detectorExecutor;

    // Clinical metrics (session-wide)
    private int totalValidFrames = 0;
    private int correctFramesInSession = 0;
    private double smoothedAngle = 0.0;
    private static final double SMOOTHING_FACTOR = 0.6; // EMA factor (0.0 to 1.0)

    // Real-time metrics
    private int totalExerciseCount;
    private double averageFormAccuracy;
    private double currentAngle;

    public interface OnAnalysisResultListener {
        void onAnalysisComplete(FormFeedback feedback);

        void onError(Exception e);
    }

    private HandsDetector handsDetector;
    private android.content.Context context;

    public AdvancedMotionTracker(android.content.Context context) {
        this.context = context;
        initializeExerciseConfigs();
        initializePoseDetector();
        // Initialize HandsDetector for wrist exercises
        this.handsDetector = new HandsDetector(context);

        angleHistory = new ArrayList<>();
        exerciseCounter = new ExerciseCounter();
        detectorExecutor = Executors.newSingleThreadExecutor();
    }

    private void initializePoseDetector() {
        // Use Accurate model for better precision in rehab
        com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions options = new com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions.Builder()
                .setDetectorMode(com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions.STREAM_MODE)
                .build();

        poseDetector = PoseDetection.getClient(options);
    }

    private void initializeExerciseConfigs() {
        exerciseConfigs = new HashMap<>();

        // Elbow Flexion (Standard Curl) - Widened range for better tolerance
        exerciseConfigs.put("ex_002", new ExerciseAngleConfig(
                new int[] { PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_ELBOW, PoseLandmark.LEFT_WRIST },
                new int[] { PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_ELBOW, PoseLandmark.RIGHT_WRIST },
                30.0, 175.0, "elbow_flexion"));

        // Wrist Flexion - Handled by HandsDetector, dummy config for metadata
        exerciseConfigs.put("ex_001", new ExerciseAngleConfig(
                new int[] {}, new int[] {},
                160.0, 180.0, "wrist_flexion"));

        // Knee Flexion - Widened range
        exerciseConfigs.put("ex_008", new ExerciseAngleConfig(
                new int[] { PoseLandmark.LEFT_HIP, PoseLandmark.LEFT_KNEE, PoseLandmark.LEFT_ANKLE },
                new int[] { PoseLandmark.RIGHT_HIP, PoseLandmark.RIGHT_KNEE, PoseLandmark.RIGHT_ANKLE },
                75.0, 180.0, "knee_flexion"));

        // Shoulder Abduction - Widened range
        exerciseConfigs.put("ex_011", new ExerciseAngleConfig(
                new int[] { PoseLandmark.LEFT_HIP, PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_ELBOW },
                new int[] { PoseLandmark.RIGHT_HIP, PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_ELBOW },
                5.0, 100.0, "shoulder_abduction"));
    }

    public void startTracking(String exerciseId) {
        this.currentExerciseId = exerciseId;
        resetTrackingState();

        ExerciseAngleConfig config = exerciseConfigs.get(exerciseId);
        if (config != null) {
            android.util.Log.d("AdvancedTracking", "Started tracking: " + exerciseId);
        } else {
            // Default or fallback?
            android.util.Log.w("AdvancedTracking", "Unknown exercise ID: " + exerciseId);
        }
    }

    /**
     * Process image using ML Kit Pose Detection
     */
    public void processImage(ImageProxy imageProxy, OnAnalysisResultListener listener) {
        if (currentExerciseId == null || poseDetector == null) {
            imageProxy.close();
            return;
        }

        Image mediaImage = imageProxy.getImage();
        if (mediaImage == null) {
            imageProxy.close();
            return;
        }

        InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());

        // SELECT STRATEGY BASED ON EXERCISE
        if ("ex_001".equals(currentExerciseId)) {
            // Use MediaPipe Hands for Wrist Flexion
            handsDetector.process(imageProxy, imageProxy.getImageInfo().getRotationDegrees(),
                    new HandsDetector.HandDetectionListener() {
                        @Override
                        public void onResult(FormFeedback feedback) {
                            listener.onAnalysisComplete(feedback);
                            imageProxy.close();
                        }

                        @Override
                        public void onError(String error) {
                            // Fallback to Pose? Or just log
                            android.util.Log.e("AdvancedTracking", "Hands Error: " + error);
                            // If hand fails, we might want to try body pose, but for now just close
                            listener.onError(new Exception(error));
                            imageProxy.close();
                        }
                    });
            return;
        }

        // Default: ML Kit Pose Detection
        poseDetector.process(image)
                .addOnSuccessListener(detectorExecutor, pose -> {
                    try {
                        FormFeedback feedback = analyzePose(pose, image.getWidth(), image.getHeight());
                        listener.onAnalysisComplete(feedback);
                    } catch (Exception e) {
                        listener.onError(e);
                    } finally {
                        imageProxy.close();
                    }
                })
                .addOnFailureListener(detectorExecutor, e -> {
                    listener.onError(e);
                    imageProxy.close();
                });
    }

    private FormFeedback analyzePose(Pose pose, int width, int height) {
        ExerciseAngleConfig config = exerciseConfigs.get(currentExerciseId);
        if (config == null)
            return createDefaultFeedback();

        // Determine best side (Left vs Right)
        int[] targetLandmarks = null;
        String detectedSide = "None";
        double leftConfidence = getAverageConfidence(pose, config.leftLandmarks);
        double rightConfidence = getAverageConfidence(pose, config.rightLandmarks);

        if (leftConfidence > rightConfidence && leftConfidence > 0.5) {
            targetLandmarks = config.leftLandmarks;
            detectedSide = "Left";
        } else if (rightConfidence > 0.5) {
            targetLandmarks = config.rightLandmarks;
            detectedSide = "Right";
        }

        if (targetLandmarks == null) {
            android.util.Log.d("AdvancedTracker", String.format("No valid side detected. L:%.2f R:%.2f",
                    leftConfidence, rightConfidence));
            return new FormFeedback(false, 0.0f, "Ensure your body is visible", new HashMap<>());
        }

        PoseLandmark p1 = pose.getPoseLandmark(targetLandmarks[0]);
        PoseLandmark p2 = pose.getPoseLandmark(targetLandmarks[1]);
        PoseLandmark p3 = pose.getPoseLandmark(targetLandmarks[2]);

        // Increment session valid frames
        totalValidFrames++;

        // Calculate & Smooth Angle
        double rawAngle = calculateAngle(p1, p2, p3);
        smoothedAngle = (SMOOTHING_FACTOR * rawAngle) + ((1.0 - SMOOTHING_FACTOR) * smoothedAngle);
        currentAngle = smoothedAngle;

        // Analyze Form
        boolean inRange = currentAngle >= config.minAngle && currentAngle <= config.maxAngle;
        if (inRange) {
            correctFramesInSession++;
        }
        double accuracy = PerformanceScorer.calculateScore(correctFramesInSession, totalValidFrames);

        // Debug logging
        android.util.Log.d("AdvancedTracker", String.format(
                "[%s] Side:%s Angle:%.1f° Range:[%.0f-%.0f] InRange:%b Correct:%d/%d Score:%.0f%%",
                currentExerciseId, detectedSide, currentAngle, config.minAngle, config.maxAngle,
                inRange, correctFramesInSession, totalValidFrames, accuracy));

        // Update History
        updateHistory(currentAngle, inRange, accuracy);

        // Update Count
        boolean countIncreased = exerciseCounter.update(inRange);
        if (countIncreased)
            totalExerciseCount++;

        String feedbackMsg = generateFeedbackMessage(accuracy, inRange, countIncreased);

        // Extract all landmarks for UI overlay
        Map<String, Point> skeleton = new HashMap<>();
        for (PoseLandmark lm : pose.getAllPoseLandmarks()) {
            skeleton.put(String.valueOf(lm.getLandmarkType()),
                    new Point((int) lm.getPosition().x, (int) lm.getPosition().y));
        }

        return new FormFeedback(inRange, (float) accuracy, feedbackMsg, skeleton);
    }

    private double getAverageConfidence(Pose pose, int[] landmarks) {
        if (landmarks == null || landmarks.length == 0)
            return 0.0;
        double sum = 0;
        for (int l : landmarks) {
            PoseLandmark pl = pose.getPoseLandmark(l);
            if (pl != null)
                sum += pl.getInFrameLikelihood();
        }
        return sum / landmarks.length;
    }

    private double calculateAngle(PoseLandmark first, PoseLandmark middle, PoseLandmark last) {
        PointF p1 = first.getPosition();
        PointF p2 = middle.getPosition();
        PointF p3 = last.getPosition();

        double result = Math.toDegrees(Math.atan2(p3.y - p2.y, p3.x - p2.x) -
                Math.atan2(p1.y - p2.y, p1.x - p2.x));
        result = Math.abs(result);
        if (result > 180) {
            result = 360.0 - result;
        }
        return result;
    }

    private double calculateAccuracy(double angle, ExerciseAngleConfig config) {
        if (angle >= config.minAngle && angle <= config.maxAngle)
            return 1.0;
        double mid = (config.minAngle + config.maxAngle) / 2;
        double dist = Math.abs(angle - mid);
        double range = config.maxAngle - config.minAngle;
        // Simple decay function
        return Math.max(0.0, 1.0 - (dist / range));
    }

    private void updateHistory(double angle, boolean inRange, double accuracy) {
        angleHistory.add(new AngleFrame(System.currentTimeMillis(), angle, accuracy, inRange));
        if (angleHistory.size() > 100)
            angleHistory.remove(0);

        // Update average
        if (!angleHistory.isEmpty()) {
            averageFormAccuracy = angleHistory.stream().mapToDouble(f -> f.accuracy).average().orElse(0.0);
        }
    }

    private String generateFeedbackMessage(double accuracy, boolean inRange, boolean countInc) {
        ExerciseAngleConfig config = exerciseConfigs.get(currentExerciseId);

        if (countInc)
            return String.format("Good! Rep counted. Angle: %.0f\u00b0", currentAngle);

        if (inRange)
            return String.format("Perfect form! Angle: %.0f\u00b0", currentAngle);

        // Provide guidance based on current angle
        if (config != null) {
            if (currentAngle < config.minAngle) {
                double diff = config.minAngle - currentAngle;
                return String.format("Bend more! Current: %.0f\u00b0 (need %.0f\u00b0+)", currentAngle,
                        config.minAngle);
            } else if (currentAngle > config.maxAngle) {
                double diff = currentAngle - config.maxAngle;
                return String.format("Straighten less! Current: %.0f\u00b0 (max %.0f\u00b0)", currentAngle,
                        config.maxAngle);
            }
        }

        return String.format("Adjust form. Angle: %.0f\u00b0", currentAngle);
    }

    private void resetTrackingState() {
        angleHistory.clear();
        totalExerciseCount = 0;
        averageFormAccuracy = 0.0;
        currentAngle = 0.0;
        smoothedAngle = 0.0;
        totalValidFrames = 0;
        correctFramesInSession = 0;
        exerciseCounter.reset();
    }

    private FormFeedback createDefaultFeedback() {
        return new FormFeedback(false, 0.0f, "Align yourself in frame", new HashMap<>());
    }

    public void stop() {
        if (poseDetector != null) {
            poseDetector.close();
        }
        // stop executor?
    }

    // Config classes
    private static class ExerciseAngleConfig {
        int[] leftLandmarks;
        int[] rightLandmarks;
        double minAngle, maxAngle;
        String type;

        public ExerciseAngleConfig(int[] leftLandmarks, int[] rightLandmarks, double min, double max, String type) {
            this.leftLandmarks = leftLandmarks;
            this.rightLandmarks = rightLandmarks;
            this.minAngle = min;
            this.maxAngle = max;
            this.type = type;
        }
    }

    private static class AngleFrame {
        long timestamp;
        double angle, accuracy;
        boolean inRange;

        public AngleFrame(long t, double ang, double acc, boolean range) {
            this.timestamp = t;
            this.angle = ang;
            this.accuracy = acc;
            this.inRange = range;
        }
    }

    // Simple state machine for counting (Open state -> Closed state -> Open state)
    private static class ExerciseCounter {
        private boolean wasInRange = false;

        public void reset() {
            wasInRange = false;
        }

        public boolean update(boolean inRange) {
            boolean counted = false;
            if (inRange && !wasInRange) {
                // Entered range
                wasInRange = true;
            } else if (!inRange && wasInRange) {
                // Completed rep (exited range) - simplified logic
                // Or maybe count when holding? Let's count when *entering* target zone for
                // simplicity
                counted = true;
                wasInRange = false;
            }
            return counted;
        }
    }

    // Reporting
    public ExercisePerformanceReport getPerformanceReport() {
        ExercisePerformanceReport report = new ExercisePerformanceReport();
        report.exerciseId = currentExerciseId;
        report.totalCount = totalExerciseCount;
        report.averageAccuracy = averageFormAccuracy;
        report.currentAngle = currentAngle;
        report.sessionDuration = angleHistory.isEmpty() ? 0
                : System.currentTimeMillis() - angleHistory.get(0).timestamp;

        // Calculate max/min
        if (!angleHistory.isEmpty()) {
            report.maxAngle = angleHistory.stream().mapToDouble(f -> f.angle).max().orElse(0.0);
            report.minAngle = angleHistory.stream().mapToDouble(f -> f.angle).min().orElse(0.0);
            report.correctFormPercentage = averageFormAccuracy * 100;
        }
        return report;
    }

    public static class ExercisePerformanceReport {
        public String exerciseId;
        public int totalCount;
        public double averageAccuracy;
        public double currentAngle;
        public long sessionDuration;
        public double maxAngle;
        public double minAngle;
        public double correctFormPercentage;

        public String getFormattedReport() {
            return String.format(
                    "Exercise: %s\nTotal Count: %d\nAccuracy: %.1f%%\nDuration: %d s",
                    exerciseId, totalCount, averageAccuracy * 100, sessionDuration / 1000);
        }
    }
}