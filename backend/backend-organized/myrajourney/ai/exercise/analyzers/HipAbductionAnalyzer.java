package com.example.myrajourney.ai.exercise.analyzers;

import com.example.myrajourney.ai.exercise.AnalysisFrame;
import com.example.myrajourney.ai.exercise.BaseExerciseAnalyzer;
import com.example.myrajourney.ai.exercise.ExerciseConfig;
import com.example.myrajourney.exercise.models.PoseFrame;

/**
 * Analyzer for Hip Abduction exercise (ex_010)
 * Tracks side leg lifting movements
 */
public class HipAbductionAnalyzer extends BaseExerciseAnalyzer {

    private enum LegState {
        LOWERED,
        RAISED,
        TRANSITIONING
    }

    private LegState currentState = LegState.LOWERED;
    private long stateStartTime = 0;
    private boolean countedThisRep = false;

    // Angle thresholds (leg angle from vertical)
    private static final double RAISED_MIN_ANGLE = 25.0; // Was 30
    private static final double RAISED_MAX_ANGLE = 45.0; // Was 45
    private static final double LOWERED_MAX_ANGLE = 10.0; // Was 5
    private static final double MIN_MOVEMENT_RANGE = 10.0; // Was 15

    public HipAbductionAnalyzer() {
        super(createConfig());
    }

    private static ExerciseConfig createConfig() {
        ExerciseConfig config = new ExerciseConfig(
                "ex_010",
                "Hip Abduction",
                ExerciseConfig.ExerciseType.HIP_ABDUCTION);

        config.setExpectedRepetitions(10);
        config.setMinimumRepetitions(5);
        config.setMinimumHoldTimeMs(1000); // Was 3000
        config.setAngleTolerance(15.0); // Was 10.0

        config.getTargetJoints().add("HIP");
        config.getRequiredLandmarks().add("LEFT_HIP");
        config.getRequiredLandmarks().add("LEFT_KNEE");
        config.getRequiredLandmarks().add("LEFT_ANKLE");

        config.addAngleRange("LEG_RAISED", RAISED_MIN_ANGLE, RAISED_MAX_ANGLE);

        config.addInstruction("LIFT_HIGHER", "Lift your leg higher");
        config.addInstruction("KEEP_STRAIGHT", "Keep your leg straight");
        config.addInstruction("HOLD", "Hold this position");
        config.addInstruction("LOWER_SLOW", "Lower slowly");
        config.addInstruction("GOOD", "Excellent!");

        return config;
    }

    @Override
    protected AnalysisFrame performAnalysis(PoseFrame userPose, long timestamp) {
        AnalysisFrame frame = new AnalysisFrame(timestamp);

        if (!userPose.isValidForAnalysis()) {
            frame.setFormCorrect(false);
            frame.setInstruction("Position yourself in view");
            return frame;
        }

        // Calculate leg abduction angle (distance from body midline)
        double abductionAngle = calculateAbductionAngle(userPose);

        if (abductionAngle < 0) {
            frame.setFormCorrect(false);
            frame.setInstruction("Stand sideways to the camera");
            return frame;
        }

        frame.setTargetJoint("HIP");
        frame.setTargetJointAngle(abductionAngle);
        frame.setIdealJointAngle(37.5);

        // Determine state
        LegState newState = determineLegState(abductionAngle);

        // Rep counting
        if (newState != currentState) {
            if (currentState == LegState.RAISED && newState == LegState.LOWERED) {
                if (!countedThisRep) {
                    repetitionCount++;
                    countedThisRep = true;
                    frame.setInstruction("Rep " + repetitionCount + " complete!");
                    frame.setFormCorrect(true);
                    frame.setFormAccuracy(1.0);
                }
            } else if (newState == LegState.RAISED) {
                countedThisRep = false;
            }

            currentState = newState;
            stateStartTime = timestamp;
        }

        // Generate feedback
        generateFeedback(frame, abductionAngle, newState, timestamp);

        // Calculate accuracy
        double formAccuracy = calculateFormAccuracy(abductionAngle, newState);
        frame.setFormAccuracy(formAccuracy);

        return frame;
    }

    private double calculateAbductionAngle(PoseFrame poseFrame) {
        try {
            // Calculate angle of leg from vertical
            // Using HIP (23), ANKLE (27)
            android.graphics.PointF hip = poseFrame.getPoint(23);
            android.graphics.PointF ankle = poseFrame.getPoint(27);

            if (hip == null || ankle == null) {
                return -1.0;
            }

            // Calculate angle from vertical
            double legDx = ankle.x - hip.x;
            double legDy = ankle.y - hip.y;
            double angleFromVertical = Math.abs(Math.toDegrees(Math.atan2(legDx, legDy)));

            return angleFromVertical;
        } catch (Exception e) {
            return -1.0;
        }
    }

    private LegState determineLegState(double angle) {
        if (angle >= RAISED_MIN_ANGLE && angle <= RAISED_MAX_ANGLE) {
            return LegState.RAISED;
        } else if (angle <= LOWERED_MAX_ANGLE) {
            return LegState.LOWERED;
        } else {
            return LegState.TRANSITIONING;
        }
    }

    private void generateFeedback(AnalysisFrame frame, double angle, LegState state, long timestamp) {
        long holdTime = timestamp - stateStartTime;

        switch (state) {
            case RAISED:
                if (holdTime < config.getMinimumHoldTimeMs()) {
                    frame.setInstruction(config.getInstructionTemplates().get("HOLD"));
                    frame.setFormCorrect(false);
                } else {
                    frame.setInstruction(config.getInstructionTemplates().get("GOOD"));
                    frame.setFormCorrect(true);
                }
                break;

            case LOWERED:
                frame.setInstruction(config.getInstructionTemplates().get("LIFT_HIGHER"));
                frame.setFormCorrect(false);
                break;

            case TRANSITIONING:
                if (angle < RAISED_MIN_ANGLE) {
                    frame.setInstruction(config.getInstructionTemplates().get("LIFT_HIGHER"));
                } else {
                    frame.setInstruction(config.getInstructionTemplates().get("LOWER_SLOW"));
                }
                frame.setFormCorrect(true);
                break;
        }

        frame.setAngleDeviation(Math.abs(angle - 37.5));
    }

    private double calculateFormAccuracy(double angle, LegState state) {
        if (state == LegState.RAISED) {
            double deviation = Math.abs(angle - 37.5);
            return Math.max(0.0, 1.0 - (deviation / 30.0));
        } else if (state == LegState.TRANSITIONING) {
            return 0.7;
        } else {
            return 0.5;
        }
    }
}
