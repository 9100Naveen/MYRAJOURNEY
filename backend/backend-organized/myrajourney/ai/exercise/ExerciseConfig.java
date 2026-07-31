package com.example.myrajourney.ai.exercise;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration for exercise-specific analysis
 * Defines target joints, ideal angles, repetition expectations, etc.
 */
public class ExerciseConfig {

    private String exerciseId;
    private String exerciseName;
    private ExerciseType exerciseType;

    // Target joints and landmarks
    private List<String> targetJoints;
    private List<String> requiredLandmarks;

    // Angle ranges (joint name -> [min, max] degrees)
    private Map<String, AngleRange> idealAngleRanges;

    // Repetition expectations
    private int expectedRepetitions;
    private int minimumRepetitions;

    // Timing requirements
    private long minimumHoldTimeMs; // Minimum time to hold position
    private long maximumRepDurationMs; // Maximum time for one rep

    // Tolerance settings
    private double angleTolerance; // Degrees of acceptable deviation
    private double positionTolerance; // Position deviation tolerance

    // Instruction templates
    private Map<String, String> instructionTemplates;

    public ExerciseConfig(String exerciseId, String exerciseName, ExerciseType type) {
        this.exerciseId = exerciseId;
        this.exerciseName = exerciseName;
        this.exerciseType = type;
        this.targetJoints = new ArrayList<>();
        this.requiredLandmarks = new ArrayList<>();
        this.idealAngleRanges = new HashMap<>();
        this.instructionTemplates = new HashMap<>();

        // Defaults
        this.expectedRepetitions = 10;
        this.minimumRepetitions = 5;
        this.minimumHoldTimeMs = 2000; // 2 seconds
        this.maximumRepDurationMs = 10000; // 10 seconds
        this.angleTolerance = 15.0; // 15 degrees
        this.positionTolerance = 0.1; // 10% of screen
    }

    /**
     * Add ideal angle range for a joint
     */
    public void addAngleRange(String joint, double minAngle, double maxAngle) {
        idealAngleRanges.put(joint, new AngleRange(minAngle, maxAngle));
    }

    /**
     * Add instruction template
     */
    public void addInstruction(String key, String template) {
        instructionTemplates.put(key, template);
    }

    // Getters and setters

    public String getExerciseId() {
        return exerciseId;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public ExerciseType getExerciseType() {
        return exerciseType;
    }

    public List<String> getTargetJoints() {
        return targetJoints;
    }

    public List<String> getRequiredLandmarks() {
        return requiredLandmarks;
    }

    public Map<String, AngleRange> getIdealAngleRanges() {
        return idealAngleRanges;
    }

    public int getExpectedRepetitions() {
        return expectedRepetitions;
    }

    public void setExpectedRepetitions(int expectedRepetitions) {
        this.expectedRepetitions = expectedRepetitions;
    }

    public int getMinimumRepetitions() {
        return minimumRepetitions;
    }

    public void setMinimumRepetitions(int minimumRepetitions) {
        this.minimumRepetitions = minimumRepetitions;
    }

    public long getMinimumHoldTimeMs() {
        return minimumHoldTimeMs;
    }

    public void setMinimumHoldTimeMs(long minimumHoldTimeMs) {
        this.minimumHoldTimeMs = minimumHoldTimeMs;
    }

    public long getMaximumRepDurationMs() {
        return maximumRepDurationMs;
    }

    public void setMaximumRepDurationMs(long maximumRepDurationMs) {
        this.maximumRepDurationMs = maximumRepDurationMs;
    }

    public double getAngleTolerance() {
        return angleTolerance;
    }

    public void setAngleTolerance(double angleTolerance) {
        this.angleTolerance = angleTolerance;
    }

    public double getPositionTolerance() {
        return positionTolerance;
    }

    public void setPositionTolerance(double positionTolerance) {
        this.positionTolerance = positionTolerance;
    }

    public Map<String, String> getInstructionTemplates() {
        return instructionTemplates;
    }

    /**
     * Angle range definition
     */
    public static class AngleRange {
        public final double min;
        public final double max;

        public AngleRange(double min, double max) {
            this.min = min;
            this.max = max;
        }

        public boolean isInRange(double angle) {
            return angle >= min && angle <= max;
        }

        public double getDeviation(double angle) {
            if (angle < min)
                return min - angle;
            if (angle > max)
                return angle - max;
            return 0.0;
        }
    }

    /**
     * Exercise types
     */
    public enum ExerciseType {
        WRIST_FLEXION,
        WRIST_ROTATION,
        THUMB_OPPOSITION,
        THUMB_FLEXION,
        FINGER_FLEXION,
        FINGER_EXTENSION,
        FINGER_PINCH,
        KNEE_FLEXION,
        HIP_FLEXION,
        HIP_ABDUCTION
    }
}
