package com.example.myrajourney.ai.exercise;

import com.example.myrajourney.ai.exercise.analyzers.*;

/**
 * Factory class to create appropriate exercise analyzer based on exercise ID
 */
public class ExerciseAnalyzerFactory {

    /**
     * Create analyzer for given exercise ID
     * 
     * @param exerciseId Exercise ID (ex_001 through ex_010)
     * @return Appropriate ExerciseAnalyzer instance
     */
    public static ExerciseAnalyzer createAnalyzer(String exerciseId) {
        if (exerciseId == null) {
            throw new IllegalArgumentException("Exercise ID cannot be null");
        }

        switch (exerciseId) {
            case "ex_001":
                // Wrist Flexion/Extension
                return new WristFlexionAnalyzer();

            case "ex_002":
                // Wrist Rotation
                return new WristRotationAnalyzer();

            case "ex_003":
                // Thumb Opposition
                return new HandExerciseAnalyzer("ex_003", "Thumb Opposition", 10);

            case "ex_004":
                // Thumb Flexion/Extension
                return new HandExerciseAnalyzer("ex_004", "Thumb Flexion", 10);

            case "ex_005":
                // Finger Flexion (Making a Fist)
                return new HandExerciseAnalyzer("ex_005", "Finger Flexion", 10);

            case "ex_006":
                // Finger Extension/Spreading
                return new HandExerciseAnalyzer("ex_006", "Finger Extension", 10);

            case "ex_007":
                // Finger Pinch Strengthening
                return new HandExerciseAnalyzer("ex_007", "Finger Pinch", 15);

            case "ex_008":
                // Knee Flexion/Extension
                return new KneeFlexionAnalyzer();

            case "ex_009":
                // Hip Flexion
                return new HipFlexionAnalyzer();

            case "ex_010":
                // Hip Abduction
                return new HipAbductionAnalyzer();

            default:
                // Default to a generic analyzer for unknown exercises
                return new HandExerciseAnalyzer(exerciseId, "Unknown Exercise", 10);
        }
    }

    /**
     * Check if exercise ID is supported
     */
    public static boolean isSupported(String exerciseId) {
        return exerciseId != null &&
                exerciseId.matches("ex_00[1-9]|ex_010");
    }

    /**
     * Get exercise type from ID
     */
    public static String getExerciseType(String exerciseId) {
        switch (exerciseId) {
            case "ex_001":
            case "ex_002":
                return "WRIST";
            case "ex_003":
            case "ex_004":
                return "THUMB";
            case "ex_005":
            case "ex_006":
            case "ex_007":
                return "FINGER";
            case "ex_008":
                return "KNEE";
            case "ex_009":
            case "ex_010":
                return "HIP";
            default:
                return "UNKNOWN";
        }
    }
}
