package com.example.myrajourney.rehab.models;

import java.util.List;
import java.util.Map;

/**
 * Helper class for managing exercise metadata and RA-specific benefits
 */
public class ExerciseMetadata {
    
    /**
     * Standard difficulty levels for RA exercises
     */
    public static class DifficultyLevel {
        public static final int BEGINNER = 1;
        public static final int INTERMEDIATE = 2;
        public static final int ADVANCED = 3;
    }

    /**
     * Common target joints for RA exercises
     */
    public static class TargetJoints {
        public static final String WRIST = "wrist";
        public static final String THUMB = "thumb";
        public static final String INDEX_FINGER = "index_finger";
        public static final String MIDDLE_FINGER = "middle_finger";
        public static final String RING_FINGER = "ring_finger";
        public static final String PINKY_FINGER = "pinky_finger";
        public static final String KNEE = "knee";
        public static final String HIP = "hip";
        public static final String ANKLE = "ankle";
        public static final String SHOULDER = "shoulder";
    }

    /**
     * Common RA-specific benefits for exercises
     */
    public static class RABenefits {
        public static final String REDUCES_JOINT_STIFFNESS = "Reduces joint stiffness";
        public static final String IMPROVES_RANGE_OF_MOTION = "Improves range of motion";
        public static final String STRENGTHENS_MUSCLES = "Strengthens supporting muscles";
        public static final String REDUCES_INFLAMMATION = "Helps reduce inflammation";
        public static final String IMPROVES_GRIP_STRENGTH = "Improves grip strength";
        public static final String ENHANCES_FLEXIBILITY = "Enhances joint flexibility";
        public static final String PREVENTS_DEFORMITY = "Helps prevent joint deformity";
        public static final String IMPROVES_CIRCULATION = "Improves blood circulation";
        public static final String REDUCES_PAIN = "Helps reduce joint pain";
        public static final String MAINTAINS_FUNCTION = "Maintains joint function";
    }

    /**
     * Exercise duration recommendations in minutes
     */
    public static class Duration {
        public static final int SHORT = 5;      // 5 minutes
        public static final int MEDIUM = 10;    // 10 minutes
        public static final int LONG = 15;      // 15 minutes
    }

    /**
     * Frequency recommendations per week
     */
    public static class Frequency {
        public static final int DAILY = 7;
        public static final int FREQUENT = 5;   // 5 times per week
        public static final int MODERATE = 3;   // 3 times per week
        public static final int LIGHT = 2;      // 2 times per week
    }

    /**
     * Validate exercise metadata completeness
     */
    public static boolean isValidExercise(RAExercise exercise) {
        return exercise != null &&
               exercise.getId() != null && !exercise.getId().trim().isEmpty() &&
               exercise.getName() != null && !exercise.getName().trim().isEmpty() &&
               exercise.getDescription() != null && !exercise.getDescription().trim().isEmpty() &&
               exercise.getCategory() != null &&
               exercise.getTargetJoints() != null && !exercise.getTargetJoints().isEmpty() &&
               exercise.getDifficultyLevel() >= DifficultyLevel.BEGINNER &&
               exercise.getDifficultyLevel() <= DifficultyLevel.ADVANCED &&
               exercise.getInstructions() != null && !exercise.getInstructions().isEmpty() &&
               exercise.getRaSpecificBenefits() != null && !exercise.getRaSpecificBenefits().isEmpty();
    }

    /**
     * Get difficulty level description
     */
    public static String getDifficultyDescription(int level) {
        switch (level) {
            case DifficultyLevel.BEGINNER:
                return "Beginner - Gentle movements suitable for all RA patients";
            case DifficultyLevel.INTERMEDIATE:
                return "Intermediate - Moderate intensity for patients with some mobility";
            case DifficultyLevel.ADVANCED:
                return "Advanced - Higher intensity for patients with good joint function";
            default:
                return "Unknown difficulty level";
        }
    }

    /**
     * Get recommended duration based on difficulty
     */
    public static int getRecommendedDuration(int difficultyLevel) {
        switch (difficultyLevel) {
            case DifficultyLevel.BEGINNER:
                return Duration.SHORT;
            case DifficultyLevel.INTERMEDIATE:
                return Duration.MEDIUM;
            case DifficultyLevel.ADVANCED:
                return Duration.LONG;
            default:
                return Duration.SHORT;
        }
    }

    /**
     * Get recommended frequency based on difficulty
     */
    public static int getRecommendedFrequency(int difficultyLevel) {
        switch (difficultyLevel) {
            case DifficultyLevel.BEGINNER:
                return Frequency.DAILY;
            case DifficultyLevel.INTERMEDIATE:
                return Frequency.FREQUENT;
            case DifficultyLevel.ADVANCED:
                return Frequency.MODERATE;
            default:
                return Frequency.MODERATE;
        }
    }

    /**
     * Create exercise metadata map for storage
     */
    public static Map<String, Object> createMetadataMap(RAExercise exercise) {
        return Map.of(
            "id", exercise.getId(),
            "name", exercise.getName(),
            "category", exercise.getCategory().name(),
            "difficulty", exercise.getDifficultyLevel(),
            "targetJoints", exercise.getTargetJoints(),
            "raSpecificBenefits", exercise.getRaSpecificBenefits(),
            "recommendedDuration", getRecommendedDuration(exercise.getDifficultyLevel()),
            "recommendedFrequency", getRecommendedFrequency(exercise.getDifficultyLevel())
        );
    }
}