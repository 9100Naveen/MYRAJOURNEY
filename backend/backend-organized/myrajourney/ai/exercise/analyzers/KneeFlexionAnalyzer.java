package com.example.myrajourney.ai.exercise.analyzers;

import com.example.myrajourney.ai.exercise.AnalysisFrame;
import com.example.myrajourney.ai.exercise.BaseExerciseAnalyzer;
import com.example.myrajourney.ai.exercise.ExerciseConfig;
import com.example.myrajourney.exercise.models.PoseFrame;

/**
 * Analyzer for Knee Flexion/Extension exercise (ex_008)
 * Tracks seated knee straightening movements
 */
public class KneeFlexionAnalyzer extends BaseExerciseAnalyzer {

    private static final String TAG = "KneeFlexionAnalyzer";

    private enum KneeState {
        BENT,
        EXTENDED,
        TRANSITIONING
    }

    private KneeState currentState = KneeState.BENT;
    private long stateStartTime = 0;
    private boolean countedThisRep = false;

    // Angle thresholds
    private static final double EXTENDED_MIN_ANGLE = 150.0; // Was 160
    private static final double EXTENDED_MAX_ANGLE = 180.0;
    private static final double BENT_MAX_ANGLE = 80.0; // Was 90
    private static final double MIN_MOVEMENT_RANGE = 20.0; // Was 30

    public KneeFlexionAnalyzer() {
        super(createConfig());
    }

    private static ExerciseConfig createConfig() {
        ExerciseConfig config = new ExerciseConfig(
                "ex_008",
                "Knee Flexion/Extension",
                ExerciseConfig.ExerciseType.KNEE_FLEXION);

        config.setExpectedRepetitions(10);
        config.setMinimumRepetitions(5);
        config.setMinimumHoldTimeMs(1000); // Was 3000
        config.setAngleTolerance(25.0); // Was 10.0

        config.getTargetJoints().add("KNEE");
        config.getRequiredLandmarks().add("LEFT_HIP");
        config.getRequiredLandmarks().add("LEFT_KNEE");
        config.getRequiredLandmarks().add("LEFT_ANKLE");

        config.addAngleRange("KNEE_EXTENDED", EXTENDED_MIN_ANGLE, EXTENDED_MAX_ANGLE);

        config.addInstruction("STRAIGHTEN", "Straighten your leg more");
        config.addInstruction("HOLD_TOP", "Hold at the top");
        config.addInstruction("LOWER_SLOW", "Lower slowly");
        config.addInstruction("GOOD", "Excellent form!");

        return config;
    }

    @Override
    protected AnalysisFrame performAnalysis(PoseFrame userPose, long timestamp) {
        AnalysisFrame frame = new AnalysisFrame(timestamp);

        if (!userPose.isValidForAnalysis()) {
            frame.setFormCorrect(false);
            frame.setInstruction("Sit in view of the camera");
            return frame;
        }

        // Calculate knee angle: HIP -> KNEE -> ANKLE
        double kneeAngle = calculateKneeAngle(userPose);

        if (kneeAngle < 0) {
            frame.setFormCorrect(false);
            frame.setInstruction("Position yourself so your full leg is visible");
            return frame;
        }

        frame.setTargetJoint("KNEE");
        frame.setTargetJointAngle(kneeAngle);
        frame.setIdealJointAngle(170.0); // Target angle

        // Determine knee state
        KneeState newState = determineKneeState(kneeAngle);

        // Check for rep completion
        if (newState != currentState) {
            if (currentState == KneeState.EXTENDED && newState == KneeState.BENT) {
                if (!countedThisRep) {
                    repetitionCount++;
                    countedThisRep = true;
                    frame.setInstruction("Rep " + repetitionCount + " complete!");
                    frame.setFormCorrect(true);
                    frame.setFormAccuracy(1.0);
                }
            } else if (newState == KneeState.EXTENDED) {
                countedThisRep = false;
            }

            currentState = newState;
            stateStartTime = timestamp;
        }

        // Generate feedback
        generateFeedback(frame, kneeAngle, newState, timestamp);

        // Calculate form accuracy
        double formAccuracy = calculateFormAccuracy(kneeAngle, newState);
        frame.setFormAccuracy(formAccuracy);

        return frame;
    }

    private double calculateKneeAngle(PoseFrame poseFrame) {
        try {
            // HIP (23), KNEE (25), ANKLE (27)
            android.graphics.PointF hip = poseFrame.getPoint(23);
            android.graphics.PointF knee = poseFrame.getPoint(25);
            android.graphics.PointF ankle = poseFrame.getPoint(27);

            if (hip == null || knee == null || ankle == null) {
                return -1.0;
            }

            double[] p1 = { hip.x, hip.y };
            double[] p2 = { knee.x, knee.y };
            double[] p3 = { ankle.x, ankle.y };

            return calculateAngle(p1, p2, p3);
        } catch (Exception e) {
            return -1.0;
        }
    }

    private KneeState determineKneeState(double angle) {
        if (angle >= EXTENDED_MIN_ANGLE) {
            return KneeState.EXTENDED;
        } else if (angle <= BENT_MAX_ANGLE) {
            return KneeState.BENT;
        } else {
            return KneeState.TRANSITIONING;
        }
    }

    private void generateFeedback(AnalysisFrame frame, double angle, KneeState state, long timestamp) {
        long holdTime = timestamp - stateStartTime;

        switch (state) {
            case EXTENDED:
                if (holdTime < config.getMinimumHoldTimeMs()) {
                    frame.setInstruction(config.getInstructionTemplates().get("HOLD_TOP"));
                    frame.setFormCorrect(false);
                } else {
                    frame.setInstruction(config.getInstructionTemplates().get("GOOD"));
                    frame.setFormCorrect(true);
                }
                break;

            case BENT:
                frame.setInstruction(config.getInstructionTemplates().get("STRAIGHTEN"));
                frame.setFormCorrect(false);
                break;

            case TRANSITIONING:
                if (angle > BENT_MAX_ANGLE) {
                    frame.setInstruction(config.getInstructionTemplates().get("STRAIGHTEN"));
                } else {
                    frame.setInstruction(config.getInstructionTemplates().get("LOWER_SLOW"));
                }
                frame.setFormCorrect(true);
                break;
        }

        double deviation = Math.abs(angle - 170.0);
        frame.setAngleDeviation(deviation);
    }

    private double calculateFormAccuracy(double angle, KneeState state) {
        if (state == KneeState.EXTENDED) {
            double deviation = Math.abs(angle - 170.0);
            return Math.max(0.0, 1.0 - (deviation / 30.0));
        } else if (state == KneeState.TRANSITIONING) {
            return 0.8;
        } else {
            return 0.5;
        }
    }
}
