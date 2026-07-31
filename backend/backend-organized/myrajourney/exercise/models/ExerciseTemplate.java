package com.example.myrajourney.exercise.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Template containing reference data for an exercise
 */
public class ExerciseTemplate {
    private String exerciseId;
    private String exerciseName;
    private String description;
    private String videoUrl;
    private ExerciseType exerciseType;
    private DifficultyLevel difficultyLevel;
    private List<PoseFrame> referencePoses;
    private Map<String, JointAngleRange> jointAngleRanges;
    private int expectedDurationSeconds;
    private List<String> targetJoints;
    private List<ExercisePhase> exercisePhases;

    public enum ExerciseType {
        FINGER_FLEXION("Finger Flexion/Extension"),
        SHOULDER_ROLL("Shoulder Rolls"),
        ANKLE_CIRCLE("Ankle Circles"),
        NECK_STRETCH("Neck Stretches"),
        WRIST_ROTATION("Wrist Rotations"),
        ARM_RAISE("Arm Raises");

        private final String displayName;

        ExerciseType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum DifficultyLevel {
        BEGINNER(1, "Beginner"),
        INTERMEDIATE(2, "Intermediate"),
        ADVANCED(3, "Advanced");

        private final int level;
        private final String displayName;

        DifficultyLevel(int level, String displayName) {
            this.level = level;
            this.displayName = displayName;
        }

        public int getLevel() {
            return level;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Represents acceptable range for joint angles
     */
    public static class JointAngleRange {
        private double minAngle;
        private double maxAngle;
        private double optimalAngle;
        private double tolerance;

        public JointAngleRange(double minAngle, double maxAngle, double optimalAngle) {
            this.minAngle = minAngle;
            this.maxAngle = maxAngle;
            this.optimalAngle = optimalAngle;
            this.tolerance = Math.max(optimalAngle - minAngle, maxAngle - optimalAngle);
        }

        public boolean isWithinRange(double angle) {
            return angle >= minAngle && angle <= maxAngle;
        }

        public double getDeviationScore(double angle) {
            if (isWithinRange(angle)) {
                double deviation = Math.abs(angle - optimalAngle);
                return Math.max(0.0, 1.0 - (deviation / tolerance));
            }
            return 0.0;
        }

        // Getters
        public double getMinAngle() {
            return minAngle;
        }

        public double getMaxAngle() {
            return maxAngle;
        }

        public double getOptimalAngle() {
            return optimalAngle;
        }

        public double getTolerance() {
            return tolerance;
        }
    }

    /**
     * Represents a phase in the exercise (start, middle, end positions)
     */
    public static class ExercisePhase {
        private String phaseName;
        private int startFrameIndex;
        private int endFrameIndex;
        private String description;
        private Map<String, Double> keyJointAngles;

        public ExercisePhase(String phaseName, int startFrameIndex, int endFrameIndex, String description) {
            this.phaseName = phaseName;
            this.startFrameIndex = startFrameIndex;
            this.endFrameIndex = endFrameIndex;
            this.description = description;
            this.keyJointAngles = new HashMap<>();
        }

        // Getters and setters
        public String getPhaseName() {
            return phaseName;
        }

        public void setPhaseName(String phaseName) {
            this.phaseName = phaseName;
        }

        public int getStartFrameIndex() {
            return startFrameIndex;
        }

        public void setStartFrameIndex(int startFrameIndex) {
            this.startFrameIndex = startFrameIndex;
        }

        public int getEndFrameIndex() {
            return endFrameIndex;
        }

        public void setEndFrameIndex(int endFrameIndex) {
            this.endFrameIndex = endFrameIndex;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Map<String, Double> getKeyJointAngles() {
            return keyJointAngles;
        }

        public void setKeyJointAngles(Map<String, Double> keyJointAngles) {
            this.keyJointAngles = keyJointAngles;
        }
    }

    public ExerciseTemplate() {
        this.referencePoses = new ArrayList<>();
        this.jointAngleRanges = new HashMap<>();
        this.targetJoints = new ArrayList<>();
        this.exercisePhases = new ArrayList<>();
    }

    public ExerciseTemplate(String exerciseId, String exerciseName, List<PoseFrame> referencePoses,
            int expectedDurationSeconds, String description) {
        this();
        this.exerciseId = exerciseId;
        this.exerciseName = exerciseName;
        this.referencePoses = referencePoses;
        this.expectedDurationSeconds = expectedDurationSeconds;
        this.description = description;
    }

    /**
     * Get reference pose at specific time in exercise
     */
    public PoseFrame getReferenceAtTime(long elapsedTimeMs) {
        if (referencePoses.isEmpty()) {
            return null;
        }

        // Find closest reference pose by timestamp
        PoseFrame closestPose = referencePoses.get(0);
        long minTimeDiff = Math.abs(elapsedTimeMs - closestPose.getTimestamp());

        for (PoseFrame pose : referencePoses) {
            long timeDiff = Math.abs(elapsedTimeMs - pose.getTimestamp());
            if (timeDiff < minTimeDiff) {
                minTimeDiff = timeDiff;
                closestPose = pose;
            }
        }

        return closestPose;
    }

    /**
     * Get current exercise phase based on elapsed time
     */
    public ExercisePhase getCurrentPhase(long elapsedTimeMs) {
        if (exercisePhases.isEmpty()) {
            return null;
        }

        // Convert time to frame index (assuming 30 FPS)
        int currentFrameIndex = (int) (elapsedTimeMs * 30 / 1000);

        for (ExercisePhase phase : exercisePhases) {
            if (currentFrameIndex >= phase.getStartFrameIndex() &&
                    currentFrameIndex <= phase.getEndFrameIndex()) {
                return phase;
            }
        }

        return exercisePhases.get(exercisePhases.size() - 1); // Return last phase if beyond
    }

    // Getters and setters
    public String getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(String exerciseId) {
        this.exerciseId = exerciseId;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public ExerciseType getExerciseType() {
        return exerciseType;
    }

    public void setExerciseType(ExerciseType exerciseType) {
        this.exerciseType = exerciseType;
    }

    public DifficultyLevel getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(DifficultyLevel difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public List<PoseFrame> getReferencePoses() {
        return referencePoses;
    }

    public void setReferencePoses(List<PoseFrame> referencePoses) {
        this.referencePoses = referencePoses;
    }

    public Map<String, JointAngleRange> getJointAngleRanges() {
        return jointAngleRanges;
    }

    public void setJointAngleRanges(Map<String, JointAngleRange> jointAngleRanges) {
        this.jointAngleRanges = jointAngleRanges;
    }

    public int getExpectedDurationSeconds() {
        return expectedDurationSeconds;
    }

    public void setExpectedDurationSeconds(int expectedDurationSeconds) {
        this.expectedDurationSeconds = expectedDurationSeconds;
    }

    public List<String> getTargetJoints() {
        return targetJoints;
    }

    public void setTargetJoints(List<String> targetJoints) {
        this.targetJoints = targetJoints;
    }

    public List<ExercisePhase> getExercisePhases() {
        return exercisePhases;
    }

    public void setExercisePhases(List<ExercisePhase> exercisePhases) {
        this.exercisePhases = exercisePhases;
    }
}