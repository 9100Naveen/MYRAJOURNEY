package com.example.myrajourney.ai.exercise.analyzers;

import com.example.myrajourney.ai.exercise.AnalysisFrame;
import com.example.myrajourney.ai.exercise.BaseExerciseAnalyzer;
import com.example.myrajourney.ai.exercise.ExerciseConfig;
import com.example.myrajourney.exercise.models.PoseFrame;

/**
 * Simple analyzer for hand/finger exercises that use MediaPipe Hands
 * Used for: Thumb Opposition (ex_003), Thumb Flexion (ex_004),
 * Finger Flexion (ex_005), Finger Extension (ex_006), Finger Pinch (ex_007)
 * 
 * These exercises rely on the existing HandsDetector and WristFlexionLogic
 * which already provide feedback through FormFeedback
 */
public class HandExerciseAnalyzer extends BaseExerciseAnalyzer {

    private String exerciseId;
    private String exerciseName;
    private int targetReps;

    public HandExerciseAnalyzer(String exerciseId, String exerciseName, int targetReps) {
        super(createConfig(exerciseId, exerciseName, targetReps));
        this.exerciseId = exerciseId;
        this.exerciseName = exerciseName;
        this.targetReps = targetReps;
    }

    private static ExerciseConfig createConfig(String id, String name, int reps) {
        ExerciseConfig config = new ExerciseConfig(
                id,
                name,
                ExerciseConfig.ExerciseType.FINGER_FLEXION // Generic type
        );

        config.setExpectedRepetitions(reps);
        config.setMinimumRepetitions(reps / 2);
        config.setMinimumHoldTimeMs(2000);

        // Add generic hand instructions
        config.addInstruction("GOOD", "Good form!");
        config.addInstruction("CONTINUE", "Continue the movement");
        config.addInstruction("POSITION", "Position your hand clearly in view");

        return config;
    }

    @Override
    protected AnalysisFrame performAnalysis(PoseFrame userPose, long timestamp) {
        // For hand exercises, the actual analysis is done by HandsDetector
        // This analyzer just tracks reps and provides basic feedback

        AnalysisFrame frame = new AnalysisFrame(timestamp);
        frame.setFormCorrect(true);
        frame.setFormAccuracy(0.8); // Default good accuracy
        frame.setInstruction("Follow the reference video");

        return frame;
    }

    /**
     * Update rep count externally (called from HandsDetector feedback)
     */
    public void incrementRepCount() {
        repetitionCount++;
        if (repetitionCount >= config.getExpectedRepetitions()) {
            sessionComplete = true;
        }
    }

    /**
     * Update form accuracy externally (called from HandsDetector feedback)
     */
    public void updateFormAccuracy(double accuracy) {
        totalFormAccuracy += accuracy;
        totalFrames++;
        updateMetrics();
    }
}
