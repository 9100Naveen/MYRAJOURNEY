package com.example.myrajourney.ai.exercise;

import com.example.myrajourney.exercise.models.PoseFrame;

/**
 * Base interface for exercise-specific analyzers
 * Each exercise type (wrist flexion, knee extension, etc.) implements this
 * interface
 * to provide real-time posture verification and instructional feedback
 */
public interface ExerciseAnalyzer {

    /**
     * Analyze a single pose frame from the user
     * 
     * @param userPose  The detected pose from camera
     * @param timestamp Frame timestamp in milliseconds
     * @return Analysis result with form correctness and feedback
     */
    AnalysisFrame analyzeFrame(PoseFrame userPose, long timestamp);

    /**
     * Get current instructional feedback for the user
     * Returns null if no instruction needed (form is correct)
     * 
     * @return Instruction text or null
     */
    String getCurrentInstruction();

    /**
     * Calculate current performance score (0-100)
     * Based on form accuracy, reps completed, range of motion, consistency
     * 
     * @return Score from 0 to 100
     */
    double getCurrentScore();

    /**
     * Get number of valid repetitions completed
     * 
     * @return Repetition count
     */
    int getRepetitionCount();

    /**
     * Get detailed performance metrics
     * 
     * @return Performance metrics object
     */
    PerformanceMetrics getPerformanceMetrics();

    /**
     * Reset analyzer state for new session
     */
    void reset();

    /**
     * Get exercise-specific configuration
     * 
     * @return Exercise configuration
     */
    ExerciseConfig getConfig();

    /**
     * Check if exercise session is complete
     * (e.g., target reps reached, time elapsed)
     * 
     * @return true if session complete
     */
    boolean isSessionComplete();
}
