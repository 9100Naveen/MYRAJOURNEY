package com.example.myrajourney.exercise.tracking;

/**
 * Calculates performance scores based on reps, accuracy, and ROM.
 */
public class PerformanceScorer {

    private static final int MIN_VALID_FRAMES = 5;

    /**
     * Calculate score using Hybrid IMU model
     */
    public static double calculateSensorScore(float currentRom, float targetRom, int actualReps, int targetReps,
            float smoothness, float tempoScore) {
        // ROM: 40% (Targeting ROM percent directly)
        double romPercent = Math.min(100.0, (currentRom / targetRom) * 100.0);

        // Reps: 35% (Production weights)
        double repScore = Math.min(100.0, (actualReps * 100.0) / targetReps);

        // Smoothness: 25% (Production weights)
        // smoothness is already 0-100 from SensorTracker

        double totalScore = (0.40 * romPercent) + (0.35 * repScore) + (0.25 * smoothness);

        // Give a baseline of 50% if they are active, as requested for RA patients
        if (actualReps > 0 || romPercent > 20) {
            totalScore = Math.max(50.0, totalScore);
        }

        return Math.max(0.0, Math.min(100.0, totalScore));
    }

    /**
     * Calculate score using the clinical formula:
     * Score = (correctFrames / totalValidFrames) * 100
     * 
     * @param correctFrames    Number of frames where posture was correct
     * @param totalValidFrames Number of frames where detection confidence was
     *                         sufficient
     * @return Performance score 0-100
     */
    public static double calculateScore(int correctFrames, int totalValidFrames) {
        if (totalValidFrames < 2) {
            return 30.0; // Baseline score for attempting exercise
        }

        double score = ((double) correctFrames / totalValidFrames) * 100.0;

        // Give a 15% boost to make it more encouraging as requested
        score += 15.0;

        // Ensure minimum score for active participation
        if (totalValidFrames >= 2) {
            score = Math.max(35.0, score);
        }

        return Math.max(0.0, Math.min(100.0, score));
    }

    /**
     * Legacy support or alternative scoring if needed.
     * Keep logic aligned with clinical requirements.
     */
    public static double calculateScore(int correctReps, int totalReps, double avgAngleAccuracy, double romQuality) {
        if (totalReps == 0 && avgAngleAccuracy == 0) {
            return 25.0; // Baseline score for attempting exercise
        }
        
        if (totalReps == 0) {
            // Even if 0 reps, give a score based on form accuracy
            double baseScore = Math.max(30.0, avgAngleAccuracy * 0.8);
            return Math.min(100.0, baseScore);
        }
        
        // Calculate comprehensive score
        double repAccuracy = Math.min(100.0, (double) correctReps / totalReps * 100.0);
        double formScore = Math.max(0.0, Math.min(100.0, avgAngleAccuracy));
        double romScore = Math.max(0.0, Math.min(100.0, romQuality));
        
        // Weighted combination
        double finalScore = (repAccuracy * 0.4) + (formScore * 0.4) + (romScore * 0.2);
        
        // Ensure minimum score for active participation
        if (correctReps > 0 || avgAngleAccuracy > 30) {
            finalScore = Math.max(40.0, finalScore);
        }
        
        return Math.max(0.0, Math.min(100.0, finalScore));
    }
}
