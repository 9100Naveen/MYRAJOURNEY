package com.example.myrajourney.ai.exercise.analyzers;

import com.example.myrajourney.ai.exercise.AnalysisFrame;
import com.example.myrajourney.ai.exercise.BaseExerciseAnalyzer;
import com.example.myrajourney.ai.exercise.ExerciseConfig;
import com.example.myrajourney.exercise.models.PoseFrame;

import java.util.ArrayList;
import java.util.List;

/**
 * Analyzer for Wrist Rotation exercise (ex_002)
 * Tracks circular wrist movements (clockwise and counterclockwise)
 */
public class WristRotationAnalyzer extends BaseExerciseAnalyzer {

    private static final String TAG = "WristRotationAnalyzer";

    // Track wrist position history for circle detection
    private List<double[]> wristPositionHistory = new ArrayList<>();
    private static final int MAX_HISTORY_SIZE = 30; // ~1 second at 30fps

    // Circle detection
    private double lastAngle = 0.0;
    private double totalRotation = 0.0;
    private boolean isClockwise = true;
    private int clockwiseReps = 0;
    private int counterClockwiseReps = 0;

    public WristRotationAnalyzer() {
        super(createConfig());
    }

    private static ExerciseConfig createConfig() {
        ExerciseConfig config = new ExerciseConfig(
                "ex_002",
                "Wrist Rotation",
                ExerciseConfig.ExerciseType.WRIST_ROTATION);

        config.setExpectedRepetitions(20); // 10 clockwise + 10 counterclockwise
        config.setMinimumRepetitions(10);
        config.setAngleTolerance(20.0);

        config.getTargetJoints().add("WRIST");
        config.getRequiredLandmarks().add("LEFT_WRIST");
        config.getRequiredLandmarks().add("LEFT_INDEX");

        config.addInstruction("SMOOTHER", "Make smoother circles");
        config.addInstruction("FULLER", "Rotate more fully");
        config.addInstruction("GOOD", "Good rotation!");
        config.addInstruction("SWITCH", "Now rotate the other direction");

        return config;
    }

    @Override
    protected AnalysisFrame performAnalysis(PoseFrame userPose, long timestamp) {
        AnalysisFrame frame = new AnalysisFrame(timestamp);

        if (!userPose.isValidForAnalysis()) {
            frame.setFormCorrect(false);
            frame.setInstruction("Position your hand in view");
            return frame;
        }

        // Get wrist position using PoseFrame method
        android.graphics.PointF wrist = userPose.getPoint(15); // LEFT_WRIST
        if (wrist == null) {
            frame.setFormCorrect(false);
            frame.setInstruction("Position your hand in view");
            return frame;
        }
        double[] wristPos = { wrist.x, wrist.y };

        // Add to history
        wristPositionHistory.add(wristPos);
        if (wristPositionHistory.size() > MAX_HISTORY_SIZE) {
            wristPositionHistory.remove(0);
        }

        // Need enough history to detect rotation
        if (wristPositionHistory.size() < 10) {
            frame.setInstruction("Start rotating your wrist");
            frame.setFormCorrect(true);
            frame.setFormAccuracy(0.7); // Higher baseline
            return frame;
        }

        // Detect circular motion
        boolean isCircular = detectCircularMotion();
        double rotationQuality = calculateRotationQuality();

        // Check for complete rotation (360 degrees)
        if (Math.abs(totalRotation) >= 350.0) {
            if (totalRotation > 0) {
                clockwiseReps++;
            } else {
                counterClockwiseReps++;
            }
            repetitionCount = clockwiseReps + counterClockwiseReps;
            totalRotation = 0.0;

            frame.setInstruction("Great! " + repetitionCount + " rotations complete");
            frame.setFormCorrect(true);
            frame.setFormAccuracy(1.0);
        } else if (isCircular) {
            frame.setInstruction(config.getInstructionTemplates().get("GOOD"));
            frame.setFormCorrect(true);
            frame.setFormAccuracy(rotationQuality);
        } else {
            frame.setInstruction(config.getInstructionTemplates().get("SMOOTHER"));
            frame.setFormCorrect(false);
            frame.setFormAccuracy(rotationQuality * 0.7);
        }

        return frame;
    }

    /**
     * Detect if motion is circular
     */
    private boolean detectCircularMotion() {
        if (wristPositionHistory.size() < 10)
            return false;

        // Calculate center of motion
        double centerX = 0, centerY = 0;
        for (double[] pos : wristPositionHistory) {
            centerX += pos[0];
            centerY += pos[1];
        }
        centerX /= wristPositionHistory.size();
        centerY /= wristPositionHistory.size();

        // Calculate variance in radius (circular motion has low variance)
        double avgRadius = 0;
        for (double[] pos : wristPositionHistory) {
            double dx = pos[0] - centerX;
            double dy = pos[1] - centerY;
            avgRadius += Math.sqrt(dx * dx + dy * dy);
        }
        avgRadius /= wristPositionHistory.size();

        double radiusVariance = 0;
        for (double[] pos : wristPositionHistory) {
            double dx = pos[0] - centerX;
            double dy = pos[1] - centerY;
            double radius = Math.sqrt(dx * dx + dy * dy);
            radiusVariance += Math.pow(radius - avgRadius, 2);
        }
        radiusVariance /= wristPositionHistory.size();

        // Low variance indicates circular motion - VERY LENIENT
        return radiusVariance < 0.05; // Was 0.01
    }

    /**
     * Calculate quality of rotation (0.0 to 1.0)
     */
    private double calculateRotationQuality() {
        if (wristPositionHistory.size() < 10)
            return 0.5;

        // Track angle changes
        double[] center = calculateCenter();
        double angleChange = 0;

        for (int i = 1; i < wristPositionHistory.size(); i++) {
            double angle1 = calculateAngleFromCenter(wristPositionHistory.get(i - 1), center);
            double angle2 = calculateAngleFromCenter(wristPositionHistory.get(i), center);
            double delta = angle2 - angle1;

            // Normalize angle difference
            if (delta > 180)
                delta -= 360;
            if (delta < -180)
                delta += 360;

            angleChange += Math.abs(delta);
            totalRotation += delta;
        }

        // Quality based on consistent angular velocity
        double avgAngleChange = angleChange / (wristPositionHistory.size() - 1);
        double quality = Math.min(1.0, avgAngleChange / 10.0); // Expect ~10 degrees per frame

        return quality;
    }

    private double[] calculateCenter() {
        double centerX = 0, centerY = 0;
        for (double[] pos : wristPositionHistory) {
            centerX += pos[0];
            centerY += pos[1];
        }
        return new double[] {
                centerX / wristPositionHistory.size(),
                centerY / wristPositionHistory.size()
        };
    }

    private double calculateAngleFromCenter(double[] pos, double[] center) {
        double dx = pos[0] - center[0];
        double dy = pos[1] - center[1];
        return Math.toDegrees(Math.atan2(dy, dx));
    }

    @Override
    public void reset() {
        super.reset();
        wristPositionHistory.clear();
        totalRotation = 0.0;
        clockwiseReps = 0;
        counterClockwiseReps = 0;
    }
}
