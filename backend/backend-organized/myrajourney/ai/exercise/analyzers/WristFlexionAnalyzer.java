package com.example.myrajourney.ai.exercise.analyzers;

import com.example.myrajourney.ai.exercise.AnalysisFrame;
import com.example.myrajourney.ai.exercise.BaseExerciseAnalyzer;
import com.example.myrajourney.ai.exercise.ExerciseConfig;
import com.example.myrajourney.exercise.models.PoseFrame;
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;

import java.util.List;

/**
 * Analyzer for Wrist Flexion/Extension exercise (ex_001)
 * Tracks wrist bending up and down movements
 */
public class WristFlexionAnalyzer extends BaseExerciseAnalyzer {

    private static final String TAG = "WristFlexionAnalyzer";

    // Wrist state tracking
    private enum WristState {
        NEUTRAL,
        FLEXED,
        EXTENDED
    }

    private WristState currentState = WristState.NEUTRAL;
    private WristState previousState = WristState.NEUTRAL;
    private long stateStartTime = 0;
    private boolean countedThisRep = false;

    // Angle thresholds (degrees)
    // Angle thresholds (degrees) - WIDENED for leniency
    private static final double FLEXION_MIN_ANGLE = 50.0; // Was 60
    private static final double FLEXION_MAX_ANGLE = 95.0; // Was 80
    private static final double EXTENSION_MIN_ANGLE = 50.0; // Was 60
    private static final double EXTENSION_MAX_ANGLE = 80.0; // Was 70
    private static final double NEUTRAL_ANGLE = 90.0;

    public WristFlexionAnalyzer() {
        super(createConfig());
    }

    private static ExerciseConfig createConfig() {
        ExerciseConfig config = new ExerciseConfig(
                "ex_001",
                "Wrist Flexion/Extension",
                ExerciseConfig.ExerciseType.WRIST_FLEXION);

        config.setExpectedRepetitions(10);
        config.setMinimumRepetitions(5);
        config.setMinimumHoldTimeMs(1000); // reduced from 2000
        config.setAngleTolerance(30.0); // was 15.0

        // Add target joints
        config.getTargetJoints().add("WRIST");
        config.getRequiredLandmarks().add("LEFT_WRIST");
        config.getRequiredLandmarks().add("LEFT_INDEX");
        config.getRequiredLandmarks().add("LEFT_ELBOW");

        // Add angle ranges
        config.addAngleRange("WRIST_FLEXION", FLEXION_MIN_ANGLE, FLEXION_MAX_ANGLE);
        config.addAngleRange("WRIST_EXTENSION", EXTENSION_MIN_ANGLE, EXTENSION_MAX_ANGLE);

        // Add instructions
        config.addInstruction("BEND_UP", "Bend your wrist up more");
        config.addInstruction("HOLD_TOP", "Hold at the top position");
        config.addInstruction("STRAIGHTEN", "Straighten your wrist fully");
        config.addInstruction("HOLD_BOTTOM", "Hold at the bottom position");
        config.addInstruction("GOOD_FORM", "Good form! Keep going");
        config.addInstruction("TOO_FAST", "Slow down your movement");
        config.addInstruction("INCOMPLETE", "Complete the full range of motion");

        return config;
    }

    @Override
    protected AnalysisFrame performAnalysis(PoseFrame userPose, long timestamp) {
        AnalysisFrame frame = new AnalysisFrame(timestamp);

        // Get wrist landmarks from MediaPipe result
        if (userPose.getPose() == null) {
            frame.setFormCorrect(false);
            frame.setInstruction("Position your hand in view of the camera");
            return frame;
        }

        // Access MediaPipe landmarks via PoseFrame methods
        if (!userPose.isValidForAnalysis()) {
            frame.setFormCorrect(false);
            frame.setInstruction("Position your hand in view of the camera");
            return frame;
        }

        // Calculate wrist angle
        // Using ELBOW -> WRIST -> INDEX_FINGER_MCP
        double wristAngle = calculateWristAngle(userPose);

        if (wristAngle < 0) {
            frame.setFormCorrect(false);
            frame.setInstruction("Position your hand clearly in view");
            return frame;
        }

        frame.setTargetJoint("WRIST");
        frame.setTargetJointAngle(wristAngle);

        // Determine current wrist state
        WristState newState = determineWristState(wristAngle);

        // Check for state transitions (rep counting)
        if (newState != currentState) {
            if (currentState == WristState.FLEXED && newState == WristState.EXTENDED) {
                // Completed one full rep (flexion -> extension)
                if (!countedThisRep) {
                    repetitionCount++;
                    countedThisRep = true;
                    frame.setInstruction("Great! Rep " + repetitionCount + " complete");
                    frame.setFormCorrect(true);
                    frame.setFormAccuracy(1.0);
                }
            } else if (newState == WristState.FLEXED) {
                countedThisRep = false; // Reset for next rep
            }

            previousState = currentState;
            currentState = newState;
            stateStartTime = timestamp;
        }

        // Generate feedback based on current state and angle
        generateFeedback(frame, wristAngle, newState, timestamp);

        // Calculate form accuracy
        double formAccuracy = calculateFormAccuracy(wristAngle, newState);
        frame.setFormAccuracy(formAccuracy);

        // Update range of motion tracking
        totalRangeOfMotion += Math.abs(wristAngle - NEUTRAL_ANGLE);

        return frame;
    }

    /**
     * Calculate wrist angle from pose landmarks
     */
    private double calculateWristAngle(PoseFrame poseFrame) {
        try {
            // Get landmarks using PoseFrame's getPoint method
            // ELBOW (13), WRIST (15), INDEX_FINGER_MCP (19)
            android.graphics.PointF elbow = poseFrame.getPoint(13);
            android.graphics.PointF wrist = poseFrame.getPoint(15);
            android.graphics.PointF index = poseFrame.getPoint(19);

            if (elbow == null || wrist == null || index == null) {
                return -1.0;
            }

            double[] p1 = { elbow.x, elbow.y };
            double[] p2 = { wrist.x, wrist.y };
            double[] p3 = { index.x, index.y };

            return calculateAngle(p1, p2, p3);
        } catch (Exception e) {
            return -1.0; // Error
        }
    }

    /**
     * Determine wrist state based on angle
     */
    private WristState determineWristState(double angle) {
        if (angle >= FLEXION_MIN_ANGLE && angle <= FLEXION_MAX_ANGLE) {
            return WristState.FLEXED;
        } else if (angle >= EXTENSION_MIN_ANGLE && angle <= EXTENSION_MAX_ANGLE) {
            return WristState.EXTENDED;
        } else {
            return WristState.NEUTRAL;
        }
    }

    /**
     * Generate instructional feedback
     */
    private void generateFeedback(AnalysisFrame frame, double angle, WristState state, long timestamp) {
        long holdTime = timestamp - stateStartTime;

        switch (state) {
            case FLEXED:
                if (holdTime < config.getMinimumHoldTimeMs()) {
                    frame.setInstruction(config.getInstructionTemplates().get("HOLD_TOP"));
                    frame.setFormCorrect(false);
                } else {
                    frame.setInstruction(config.getInstructionTemplates().get("GOOD_FORM"));
                    frame.setFormCorrect(true);
                }
                frame.setIdealJointAngle((FLEXION_MIN_ANGLE + FLEXION_MAX_ANGLE) / 2);
                break;

            case EXTENDED:
                if (holdTime < config.getMinimumHoldTimeMs()) {
                    frame.setInstruction(config.getInstructionTemplates().get("HOLD_BOTTOM"));
                    frame.setFormCorrect(false);
                } else {
                    frame.setInstruction(config.getInstructionTemplates().get("GOOD_FORM"));
                    frame.setFormCorrect(true);
                }
                frame.setIdealJointAngle((EXTENSION_MIN_ANGLE + EXTENSION_MAX_ANGLE) / 2);
                break;

            case NEUTRAL:
                if (currentState == WristState.FLEXED) {
                    frame.setInstruction(config.getInstructionTemplates().get("STRAIGHTEN"));
                } else {
                    frame.setInstruction(config.getInstructionTemplates().get("BEND_UP"));
                }
                frame.setFormCorrect(false);
                frame.setIdealJointAngle(NEUTRAL_ANGLE);
                break;
        }

        // Calculate angle deviation
        double deviation = Math.abs(angle - frame.getIdealJointAngle());
        frame.setAngleDeviation(deviation);

        if (deviation > config.getAngleTolerance() * 1.5) {
            frame.setPrimaryIssue(AnalysisFrame.FormIssue.INCORRECT_ANGLE);
        }
    }

    /**
     * Calculate form accuracy for current frame
     */
    private double calculateFormAccuracy(double angle, WristState state) {
        double targetAngle;

        switch (state) {
            case FLEXED:
                targetAngle = (FLEXION_MIN_ANGLE + FLEXION_MAX_ANGLE) / 2;
                break;
            case EXTENDED:
                targetAngle = (EXTENSION_MIN_ANGLE + EXTENSION_MAX_ANGLE) / 2;
                break;
            default:
                return 0.5; // Neutral state gets 50% accuracy
        }

        double deviation = Math.abs(angle - targetAngle);
        double maxDeviation = config.getAngleTolerance() * 2;

        // Linear decay from 1.0 to 0.0 based on deviation
        double accuracy = Math.max(0.0, 1.0 - (deviation / maxDeviation));

        return accuracy;
    }
}
