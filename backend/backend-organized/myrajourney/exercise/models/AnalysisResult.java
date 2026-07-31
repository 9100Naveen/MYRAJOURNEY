package com.example.myrajourney.exercise.models;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains the results of pose analysis and comparison
 */
public class AnalysisResult {
    private double overallScore;
    private double poseSimilarity;
    private double jointAccuracy;
    private double movementQuality;
    private double timingAccuracy;
    private Map<String, JointAnalysis> jointDetails;
    private MovementQualityMetrics qualityMetrics;
    private FeedbackData feedback;
    private java.util.List<String> feedbackMessages;
    private long analysisTimestamp;

    /**
     * Detailed analysis for individual joints
     */
    public static class JointAnalysis {
        private String jointName;
        private double userAngle;
        private double referenceAngle;
        private double angleDifference;
        private double accuracyScore;
        private boolean withinAcceptableRange;

        public JointAnalysis(String jointName, double userAngle, double referenceAngle) {
            this.jointName = jointName;
            this.userAngle = userAngle;
            this.referenceAngle = referenceAngle;
            this.angleDifference = Math.abs(userAngle - referenceAngle);
            this.accuracyScore = calculateAccuracyScore();
            this.withinAcceptableRange = angleDifference <= 30.0; // 30 degree tolerance
        }

        private double calculateAccuracyScore() {
            double maxAcceptableDiff = 45.0; // degrees
            return Math.max(0.0, 1.0 - (angleDifference / maxAcceptableDiff)) * 100.0;
        }

        // Getters
        public String getJointName() {
            return jointName;
        }

        public double getUserAngle() {
            return userAngle;
        }

        public double getReferenceAngle() {
            return referenceAngle;
        }

        public double getAngleDifference() {
            return angleDifference;
        }

        public double getAccuracyScore() {
            return accuracyScore;
        }

        public boolean isWithinAcceptableRange() {
            return withinAcceptableRange;
        }
    }

    /**
     * Movement quality metrics
     */
    public static class MovementQualityMetrics {
        private double stability; // 0-100, higher = more stable
        private double smoothness; // 0-100, higher = smoother
        private double rangeOfMotion; // 0-100, higher = better ROM
        private double symmetry; // 0-100, higher = more symmetric
        private double consistency; // 0-100, higher = more consistent

        public MovementQualityMetrics() {
            this.stability = 0.0;
            this.smoothness = 0.0;
            this.rangeOfMotion = 0.0;
            this.symmetry = 0.0;
            this.consistency = 0.0;
        }

        public double getOverallQuality() {
            return (stability * 0.3 + smoothness * 0.2 + rangeOfMotion * 0.3 +
                    symmetry * 0.1 + consistency * 0.1);
        }

        // Getters and setters
        public double getStability() {
            return stability;
        }

        public void setStability(double stability) {
            this.stability = stability;
        }

        public double getSmoothness() {
            return smoothness;
        }

        public void setSmoothness(double smoothness) {
            this.smoothness = smoothness;
        }

        public double getRangeOfMotion() {
            return rangeOfMotion;
        }

        public void setRangeOfMotion(double rangeOfMotion) {
            this.rangeOfMotion = rangeOfMotion;
        }

        public double getSymmetry() {
            return symmetry;
        }

        public void setSymmetry(double symmetry) {
            this.symmetry = symmetry;
        }

        public double getConsistency() {
            return consistency;
        }

        public void setConsistency(double consistency) {
            this.consistency = consistency;
        }
    }

    /**
     * Feedback data for user guidance
     */
    public static class FeedbackData {
        private FeedbackLevel level;
        private String primaryMessage;
        private String[] audioInstructions;
        private String[] textInstructions;
        private String visualIndicatorColor;
        private Map<String, String> jointSpecificFeedback;

        public enum FeedbackLevel {
            EXCELLENT("Excellent form!"),
            GOOD("Good form"),
            NEEDS_IMPROVEMENT("Needs improvement"),
            INCORRECT("Incorrect form");

            private final String message;

            FeedbackLevel(String message) {
                this.message = message;
            }

            public String getMessage() {
                return message;
            }
        }

        public FeedbackData() {
            this.jointSpecificFeedback = new HashMap<>();
            this.audioInstructions = new String[0];
            this.textInstructions = new String[0];
        }

        // Getters and setters
        public FeedbackLevel getLevel() {
            return level;
        }

        public void setLevel(FeedbackLevel level) {
            this.level = level;
        }

        public String getPrimaryMessage() {
            return primaryMessage;
        }

        public void setPrimaryMessage(String primaryMessage) {
            this.primaryMessage = primaryMessage;
        }

        public String[] getAudioInstructions() {
            return audioInstructions;
        }

        public void setAudioInstructions(String[] audioInstructions) {
            this.audioInstructions = audioInstructions;
        }

        public String[] getTextInstructions() {
            return textInstructions;
        }

        public void setTextInstructions(String[] textInstructions) {
            this.textInstructions = textInstructions;
        }

        public String getVisualIndicatorColor() {
            return visualIndicatorColor;
        }

        public void setVisualIndicatorColor(String visualIndicatorColor) {
            this.visualIndicatorColor = visualIndicatorColor;
        }

        public Map<String, String> getJointSpecificFeedback() {
            return jointSpecificFeedback;
        }

        public void setJointSpecificFeedback(Map<String, String> jointSpecificFeedback) {
            this.jointSpecificFeedback = jointSpecificFeedback;
        }
    }

    public AnalysisResult() {
        this.jointDetails = new HashMap<>();
        this.qualityMetrics = new MovementQualityMetrics();
        this.feedback = new FeedbackData();
        this.feedbackMessages = new java.util.ArrayList<>();
        this.analysisTimestamp = System.currentTimeMillis();
    }

    /**
     * Calculate overall score from component scores
     */
    public void calculateOverallScore() {
        this.overallScore = (poseSimilarity * 0.4) +
                (jointAccuracy * 0.3) +
                (movementQuality * 0.2) +
                (timingAccuracy * 0.1);
    }

    /**
     * Determine feedback level based on overall score
     */
    public void determineFeedbackLevel() {
        if (overallScore >= 90) {
            feedback.setLevel(FeedbackData.FeedbackLevel.EXCELLENT);
            feedback.setVisualIndicatorColor("#4CAF50"); // Green
        } else if (overallScore >= 75) {
            feedback.setLevel(FeedbackData.FeedbackLevel.GOOD);
            feedback.setVisualIndicatorColor("#8BC34A"); // Light Green
        } else if (overallScore >= 60) {
            feedback.setLevel(FeedbackData.FeedbackLevel.NEEDS_IMPROVEMENT);
            feedback.setVisualIndicatorColor("#FF9800"); // Orange
        } else {
            feedback.setLevel(FeedbackData.FeedbackLevel.INCORRECT);
            feedback.setVisualIndicatorColor("#F44336"); // Red
        }

        feedback.setPrimaryMessage(feedback.getLevel().getMessage());
    }

    // Getters and setters
    public double getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(double overallScore) {
        this.overallScore = overallScore;
    }

    public double getPoseSimilarity() {
        return poseSimilarity;
    }

    public void setPoseSimilarity(double poseSimilarity) {
        this.poseSimilarity = poseSimilarity;
    }

    public double getJointAccuracy() {
        return jointAccuracy;
    }

    public void setJointAccuracy(double jointAccuracy) {
        this.jointAccuracy = jointAccuracy;
    }

    public double getMovementQuality() {
        return movementQuality;
    }

    public void setMovementQuality(double movementQuality) {
        this.movementQuality = movementQuality;
    }

    public double getTimingAccuracy() {
        return timingAccuracy;
    }

    public void setTimingAccuracy(double timingAccuracy) {
        this.timingAccuracy = timingAccuracy;
    }

    public Map<String, JointAnalysis> getJointDetails() {
        return jointDetails;
    }

    public void setJointDetails(Map<String, JointAnalysis> jointDetails) {
        this.jointDetails = jointDetails;
    }

    public MovementQualityMetrics getQualityMetrics() {
        return qualityMetrics;
    }

    public void setQualityMetrics(MovementQualityMetrics qualityMetrics) {
        this.qualityMetrics = qualityMetrics;
    }

    public FeedbackData getFeedback() {
        return feedback;
    }

    public void setFeedback(FeedbackData feedback) {
        this.feedback = feedback;
    }

    public long getAnalysisTimestamp() {
        return analysisTimestamp;
    }

    public void setAnalysisTimestamp(long analysisTimestamp) {
        this.analysisTimestamp = analysisTimestamp;
    }

    public java.util.List<String> getFeedbackMessages() {
        return feedbackMessages != null ? feedbackMessages : new java.util.ArrayList<>();
    }
}