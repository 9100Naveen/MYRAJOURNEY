package com.example.myrajourney.ai.exercise.analyzers;

import com.example.myrajourney.ai.exercise.AnalysisFrame;
import com.example.myrajourney.ai.exercise.BaseExerciseAnalyzer;
import com.example.myrajourney.ai.exercise.ExerciseConfig;
import com.example.myrajourney.exercise.models.PoseFrame;

/**
 * Analyzer for Hip Flexion exercise (ex_009)
 * Tracks hip lifting (knee to chest) movements
 */
public class HipFlexionAnalyzer extends BaseExerciseAnalyzer {

    private enum HipState {
        LOWERED,
        RAISED,
        TRANSITIONING
    }

    private HipState currentState = HipState.LOWERED;
    private long stateStartTime = 0;
    private boolean countedThisRep = false;

    // Angle thresholds (hip angle from vertical)
    private static final double RAISED_MIN_ANGLE = 100.0; // Was 120
    private static final double RAISED_MAX_ANGLE = 170.0; // Was 170
    private static final double LOWERED_MAX_ANGLE = 160.0; // Was 170
    private static final double MIN_MOVEMENT_RANGE = 20.0; // Was 30

    public HipFlexionAnalyzer() {
        super(createConfig());
    }

    private static ExerciseConfig createConfig() {
        ExerciseConfig config = new ExerciseConfig(
                "ex_009",
                "Hip Flexion",
                ExerciseConfig.ExerciseType.HIP_FLEXION);

        config.setExpectedRepetitions(10);
        config.setMinimumRepetitions(5);
        config.setMinimumHoldTimeMs(1000); // Was 2000
        config.setAngleTolerance(25.0); // Was 15.0

        config.getTargetJoints().add("HIP");
        config.getRequiredLandmarks().add("LEFT_SHOULDER");
        config.getRequiredLandmarks().add("LEFT_HIP");
        config.getRequiredLandmarks().add("LEFT_KNEE");

        config.addAngleRange("HIP_RAISED", RAISED_MIN_ANGLE, RAISED_MAX_ANGLE);

        config.addInstruction("LIFT_HIGHER", "Lift your knee higher");
        config.addInstruction("HOLD", "Hold this position");
        config.addInstruction("LOWER_SLOW", "Lower slowly");
        config.addInstruction("GOOD", "Great form!");

        return config;
    }

    @Override
    protected AnalysisFrame performAnalysis(PoseFrame userPose, long timestamp) {
        AnalysisFrame frame = new AnalysisFrame(timestamp);

        if (!userPose.isValidForAnalysis()) {
            frame.setFormCorrect(false);
            frame.setInstruction("Stand in view of the camera");
            return frame;
        }

        // Calculate hip flexion angle
        double hipAngle = calculateHipFlexionAngle(userPose);

        if (hipAngle < 0) {
            frame.setFormCorrect(false);
            frame.setInstruction("Position yourself fully in view");
            return frame;
        }

        frame.setTargetJoint("HIP");
        frame.setTargetJointAngle(hipAngle);
        frame.setIdealJointAngle(67.5); // Mid-range

        // Determine state
        HipState newState = determineHipState(hipAngle);

        // Rep counting
        if (newState != currentState) {
            if (currentState == HipState.RAISED && newState == HipState.LOWERED) {
                if (!countedThisRep) {
                    repetitionCount++;
                    countedThisRep = true;
                    frame.setInstruction("Rep " + repetitionCount + " complete!");
                    frame.setFormCorrect(true);
                    frame.setFormAccuracy(1.0);
                }
            } else if (newState == HipState.RAISED) {
                countedThisRep = false;
            }

            currentState = newState;
            stateStartTime = timestamp;
        }

        // Generate feedback
        generateFeedback(frame, hipAngle, newState, timestamp);

        // Calculate accuracy
        double formAccuracy = calculateFormAccuracy(hipAngle, newState);
        frame.setFormAccuracy(formAccuracy);

        return frame;
    }

    private double calculateHipFlexionAngle(PoseFrame poseFrame) {
        try {
            // SHOULDER (11), HIP (23), KNEE (25)
            android.graphics.PointF shoulder = poseFrame.getPoint(11);
            android.graphics.PointF hip = poseFrame.getPoint(23);
            android.graphics.PointF knee = poseFrame.getPoint(25);

            if (shoulder == null || hip == null || knee == null) {
                return -1.0;
            }

            double[] p1 = { shoulder.x, shoulder.y };
            double[] p2 = { hip.x, hip.y };
            double[] p3 = { knee.x, knee.y };

            return calculateAngle(p1, p2, p3);
        } catch (Exception e) {
            return -1.0;
        }
    }

    private HipState determineHipState(double angle) {
        if (angle >= RAISED_MIN_ANGLE && angle <= RAISED_MAX_ANGLE) {
            return HipState.RAISED;
        } else if (angle <= LOWERED_MAX_ANGLE) {
            return HipState.LOWERED;
        } else {
            return HipState.TRANSITIONING;
        }
    }

    private void generateFeedback(AnalysisFrame frame, double angle, HipState state, long timestamp) {
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

        frame.setAngleDeviation(Math.abs(angle - 67.5));
    }

    private double calculateFormAccuracy(double angle, HipState state) {
        if (state == HipState.RAISED) {
            double deviation = Math.abs(angle - 67.5);
            return Math.max(0.0, 1.0 - (deviation / 40.0));
        } else if (state == HipState.TRANSITIONING) {
            return 0.7;
        } else {
            return 0.5;
        }
    }
}
