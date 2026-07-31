package com.example.myrajourney.ai.exercise;

/**
 * Comprehensive performance metrics for an exercise session
 */
public class PerformanceMetrics {

    // Core metrics
    private double overallScore; // 0-100
    private double formAccuracyScore; // 0-100 (40% weight)
    private double repetitionScore; // 0-100 (30% weight)
    private double rangeOfMotionScore; // 0-100 (20% weight)
    private double consistencyScore; // 0-100 (10% weight)

    // Detailed stats
    private int validRepetitions;
    private int totalRepetitions;
    private int expectedRepetitions;
    private double averageFormAccuracy;
    private double averageRangeOfMotion;
    private double formConsistency;

    // Time tracking
    private long sessionDurationMs;
    private int framesAnalyzed;
    private int goodFormFrames;
    private int poorFormFrames;

    // RA-specific adjustments
    private boolean raAdjustmentApplied;
    private double raAdjustmentFactor;

    public PerformanceMetrics() {
        this.expectedRepetitions = 10; // Default
        this.raAdjustmentFactor = 1.0;
    }

    /**
     * Calculate overall score from component scores
     */
    public void calculateOverallScore() {
        overallScore = (formAccuracyScore * 0.4) +
                (repetitionScore * 0.3) +
                (rangeOfMotionScore * 0.2) +
                (consistencyScore * 0.1);

        // Apply RA adjustment if enabled - VERY LENIENT
        if (raAdjustmentApplied) {
            // Shift score up and give a 15% boost for effort
            overallScore = Math.min(100.0, (overallScore * raAdjustmentFactor) + 15.0);
        }

        // Ensure score is in valid range
        overallScore = Math.max(0.0, Math.min(100.0, overallScore));
    }

    /**
     * Apply RA patient adjustment (more lenient scoring)
     */
    public void applyRAdjustment() {
        this.raAdjustmentApplied = true;
        this.raAdjustmentFactor = 1.15; // 15% more lenient for RA patients
    }

    // Getters and setters

    public double getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(double overallScore) {
        this.overallScore = overallScore;
    }

    public double getFormAccuracyScore() {
        return formAccuracyScore;
    }

    public void setFormAccuracyScore(double formAccuracyScore) {
        this.formAccuracyScore = formAccuracyScore;
    }

    public double getRepetitionScore() {
        return repetitionScore;
    }

    public void setRepetitionScore(double repetitionScore) {
        this.repetitionScore = repetitionScore;
    }

    public double getRangeOfMotionScore() {
        return rangeOfMotionScore;
    }

    public void setRangeOfMotionScore(double rangeOfMotionScore) {
        this.rangeOfMotionScore = rangeOfMotionScore;
    }

    public double getConsistencyScore() {
        return consistencyScore;
    }

    public void setConsistencyScore(double consistencyScore) {
        this.consistencyScore = consistencyScore;
    }

    public int getValidRepetitions() {
        return validRepetitions;
    }

    public void setValidRepetitions(int validRepetitions) {
        this.validRepetitions = validRepetitions;
    }

    public int getTotalRepetitions() {
        return totalRepetitions;
    }

    public void setTotalRepetitions(int totalRepetitions) {
        this.totalRepetitions = totalRepetitions;
    }

    public int getExpectedRepetitions() {
        return expectedRepetitions;
    }

    public void setExpectedRepetitions(int expectedRepetitions) {
        this.expectedRepetitions = expectedRepetitions;
    }

    public double getAverageFormAccuracy() {
        return averageFormAccuracy;
    }

    public void setAverageFormAccuracy(double averageFormAccuracy) {
        this.averageFormAccuracy = averageFormAccuracy;
    }

    public double getAverageRangeOfMotion() {
        return averageRangeOfMotion;
    }

    public void setAverageRangeOfMotion(double averageRangeOfMotion) {
        this.averageRangeOfMotion = averageRangeOfMotion;
    }

    public double getFormConsistency() {
        return formConsistency;
    }

    public void setFormConsistency(double formConsistency) {
        this.formConsistency = formConsistency;
    }

    public long getSessionDurationMs() {
        return sessionDurationMs;
    }

    public void setSessionDurationMs(long sessionDurationMs) {
        this.sessionDurationMs = sessionDurationMs;
    }

    public int getFramesAnalyzed() {
        return framesAnalyzed;
    }

    public void setFramesAnalyzed(int framesAnalyzed) {
        this.framesAnalyzed = framesAnalyzed;
    }

    public int getGoodFormFrames() {
        return goodFormFrames;
    }

    public void setGoodFormFrames(int goodFormFrames) {
        this.goodFormFrames = goodFormFrames;
    }

    public int getPoorFormFrames() {
        return poorFormFrames;
    }

    public void setPoorFormFrames(int poorFormFrames) {
        this.poorFormFrames = poorFormFrames;
    }

    public boolean isRaAdjustmentApplied() {
        return raAdjustmentApplied;
    }

    public double getRaAdjustmentFactor() {
        return raAdjustmentFactor;
    }
}
