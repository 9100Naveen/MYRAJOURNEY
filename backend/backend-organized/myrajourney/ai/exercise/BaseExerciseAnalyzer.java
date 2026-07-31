package com.example.myrajourney.ai.exercise;

import com.example.myrajourney.exercise.models.PoseFrame;
import java.util.ArrayList;
import java.util.List;

/**
 * Base abstract class for exercise analyzers
 * Provides common functionality for all exercise types
 */
public abstract class BaseExerciseAnalyzer implements ExerciseAnalyzer {

    protected ExerciseConfig config;
    protected PerformanceMetrics metrics;
    protected List<AnalysisFrame> analysisHistory;

    protected long sessionStartTime;
    protected String currentInstruction;
    protected int repetitionCount;
    protected boolean sessionComplete;

    // Frame tracking
    protected int totalFrames;
    protected int goodFormFrames;
    protected int poorFormFrames;

    // Scoring accumulators
    protected double totalFormAccuracy;
    protected double totalRangeOfMotion;

    public BaseExerciseAnalyzer(ExerciseConfig config) {
        this.config = config;
        this.metrics = new PerformanceMetrics();
        this.analysisHistory = new ArrayList<>();
        this.repetitionCount = 0;
        this.sessionComplete = false;
        this.totalFrames = 0;
        this.goodFormFrames = 0;
        this.poorFormFrames = 0;
        this.totalFormAccuracy = 0.0;
        this.totalRangeOfMotion = 0.0;
    }

    @Override
    public AnalysisFrame analyzeFrame(PoseFrame userPose, long timestamp) {
        if (sessionStartTime == 0) {
            sessionStartTime = timestamp;
        }

        // Perform exercise-specific analysis
        AnalysisFrame frame = performAnalysis(userPose, timestamp);

        // Update statistics
        totalFrames++;
        if (frame.isFormCorrect()) {
            goodFormFrames++;
        } else {
            poorFormFrames++;
        }

        totalFormAccuracy += frame.getFormAccuracy();

        // Store in history
        analysisHistory.add(frame);

        // Update current instruction
        currentInstruction = frame.getInstruction();

        // Update metrics
        updateMetrics();

        return frame;
    }

    /**
     * Exercise-specific analysis logic
     * Must be implemented by each exercise analyzer
     */
    protected abstract AnalysisFrame performAnalysis(PoseFrame userPose, long timestamp);

    /**
     * Update performance metrics based on current state
     */
    protected void updateMetrics() {
        metrics.setFramesAnalyzed(totalFrames);
        metrics.setGoodFormFrames(goodFormFrames);
        metrics.setPoorFormFrames(poorFormFrames);
        metrics.setValidRepetitions(repetitionCount);
        metrics.setExpectedRepetitions(config.getExpectedRepetitions());

        // Calculate average form accuracy
        if (totalFrames > 0) {
            double avgAccuracy = (totalFormAccuracy / totalFrames) * 100.0;
            metrics.setAverageFormAccuracy(avgAccuracy);
            metrics.setFormAccuracyScore(avgAccuracy);
        }

        // Calculate repetition score - MORE LENIENT
        double repScore = 0.0;
        if (config.getExpectedRepetitions() > 0) {
            // Give a baseline rep score if they have ANY reps
            double actualRepRatio = (double) repetitionCount / config.getExpectedRepetitions();
            repScore = Math.min(100.0, (actualRepRatio * 100.0) + (repetitionCount > 0 ? 10.0 : 0.0));
        }
        metrics.setRepetitionScore(repScore);

        // Calculate consistency score (based on form stability)
        double consistencyScore = calculateConsistencyScore();
        metrics.setConsistencyScore(consistencyScore);

        // Calculate overall score
        metrics.calculateOverallScore();

        // Boost score slightly to be encouraging, as requested by user
        double boostedScore = metrics.getOverallScore();
        if (totalFrames > 10) {
            boostedScore = Math.min(100.0, boostedScore + 5.0);
            metrics.setOverallScore(boostedScore);
        }

        // Check if session is complete
        if (repetitionCount >= config.getExpectedRepetitions()) {
            sessionComplete = true;
        }
    }

    /**
     * Calculate consistency score based on form stability
     */
    protected double calculateConsistencyScore() {
        if (analysisHistory.size() < 20) {
            return 70.0; // Start with a fair baseline instead of 100 or 0
        }

        // Calculate variance in form accuracy
        List<Double> recentAccuracies = new ArrayList<>();
        int startIdx = Math.max(0, analysisHistory.size() - 30); // Last 30 frames

        for (int i = startIdx; i < analysisHistory.size(); i++) {
            recentAccuracies.add(analysisHistory.get(i).getFormAccuracy());
        }

        // Calculate standard deviation
        double mean = recentAccuracies.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        double variance = recentAccuracies.stream()
                .mapToDouble(acc -> Math.pow(acc - mean, 2))
                .average()
                .orElse(0.0);

        double stdDev = Math.sqrt(variance);

        // Convert to score (lower variance = higher consistency)
        // Assume stdDev of 0.2 or less is excellent (100 points)
        double consistencyScore = Math.max(0.0, 100.0 - (stdDev * 500.0));

        return Math.min(100.0, consistencyScore);
    }

    @Override
    public String getCurrentInstruction() {
        return currentInstruction;
    }

    @Override
    public double getCurrentScore() {
        return metrics.getOverallScore();
    }

    @Override
    public int getRepetitionCount() {
        return repetitionCount;
    }

    @Override
    public PerformanceMetrics getPerformanceMetrics() {
        // Apply RA adjustment before returning
        metrics.applyRAdjustment();
        metrics.calculateOverallScore();
        return metrics;
    }

    @Override
    public void reset() {
        sessionStartTime = 0;
        currentInstruction = null;
        repetitionCount = 0;
        sessionComplete = false;
        totalFrames = 0;
        goodFormFrames = 0;
        poorFormFrames = 0;
        totalFormAccuracy = 0.0;
        totalRangeOfMotion = 0.0;
        analysisHistory.clear();
        metrics = new PerformanceMetrics();
    }

    @Override
    public ExerciseConfig getConfig() {
        return config;
    }

    @Override
    public boolean isSessionComplete() {
        return sessionComplete;
    }

    /**
     * Helper: Calculate angle between three points
     */
    protected double calculateAngle(double[] p1, double[] p2, double[] p3) {
        // Vector from p2 to p1
        double v1x = p1[0] - p2[0];
        double v1y = p1[1] - p2[1];

        // Vector from p2 to p3
        double v2x = p3[0] - p2[0];
        double v2y = p3[1] - p2[1];

        // Calculate angle using dot product
        double dot = v1x * v2x + v1y * v2y;
        double mag1 = Math.sqrt(v1x * v1x + v1y * v1y);
        double mag2 = Math.sqrt(v2x * v2x + v2y * v2y);

        if (mag1 == 0 || mag2 == 0) {
            return 0.0;
        }

        double cosAngle = dot / (mag1 * mag2);
        cosAngle = Math.max(-1.0, Math.min(1.0, cosAngle)); // Clamp to valid range

        return Math.toDegrees(Math.acos(cosAngle));
    }

    /**
     * Helper: Calculate distance between two points
     */
    protected double calculateDistance(double[] p1, double[] p2) {
        double dx = p2[0] - p1[0];
        double dy = p2[1] - p1[1];
        return Math.sqrt(dx * dx + dy * dy);
    }
}
