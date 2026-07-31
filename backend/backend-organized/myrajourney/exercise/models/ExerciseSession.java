package com.example.myrajourney.exercise.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a complete exercise tracking session
 */
public class ExerciseSession {
    private String sessionId;
    private int userId;
    private String exerciseTemplateId;
    private long sessionStartTime;
    private long sessionEndTime;
    private int durationSeconds;
    private boolean completed;
    
    // Scoring data
    private double overallScore;
    private double averagePoseSimilarity;
    private double averageJointAccuracy;
    private double averageMovementQuality;
    private double averageTimingAccuracy;
    
    // Session statistics
    private int totalFramesAnalyzed;
    private int validFramesCount;
    private double bestScore;
    private double worstScore;
    private Map<String, Double> jointAccuracyMap;
    
    // Progress tracking
    private List<AnalysisResult> analysisHistory;
    private List<SessionMilestone> milestones;
    
    // User feedback and notes
    private String userNotes;
    private int userRating; // 1-5 stars
    private List<String> feedbackProvided;
    
    /**
     * Represents a milestone achieved during the session
     */
    public static class SessionMilestone {
        private long timestamp;
        private String milestoneName;
        private String description;
        private double scoreAtMilestone;
        
        public SessionMilestone(long timestamp, String milestoneName, String description, double score) {
            this.timestamp = timestamp;
            this.milestoneName = milestoneName;
            this.description = description;
            this.scoreAtMilestone = score;
        }
        
        // Getters
        public long getTimestamp() { return timestamp; }
        public String getMilestoneName() { return milestoneName; }
        public String getDescription() { return description; }
        public double getScoreAtMilestone() { return scoreAtMilestone; }
    }
    
    /**
     * Session statistics summary
     */
    public static class SessionStats {
        private double improvementRate;
        private double consistencyScore;
        private int excellentFormCount;
        private int goodFormCount;
        private int needsImprovementCount;
        private int incorrectFormCount;
        private Map<String, Integer> commonIssues;
        
        public SessionStats() {
            this.commonIssues = new HashMap<>();
        }
        
        // Getters and setters
        public double getImprovementRate() { return improvementRate; }
        public void setImprovementRate(double improvementRate) { this.improvementRate = improvementRate; }
        
        public double getConsistencyScore() { return consistencyScore; }
        public void setConsistencyScore(double consistencyScore) { this.consistencyScore = consistencyScore; }
        
        public int getExcellentFormCount() { return excellentFormCount; }
        public void setExcellentFormCount(int excellentFormCount) { this.excellentFormCount = excellentFormCount; }
        
        public int getGoodFormCount() { return goodFormCount; }
        public void setGoodFormCount(int goodFormCount) { this.goodFormCount = goodFormCount; }
        
        public int getNeedsImprovementCount() { return needsImprovementCount; }
        public void setNeedsImprovementCount(int needsImprovementCount) { this.needsImprovementCount = needsImprovementCount; }
        
        public int getIncorrectFormCount() { return incorrectFormCount; }
        public void setIncorrectFormCount(int incorrectFormCount) { this.incorrectFormCount = incorrectFormCount; }
        
        public Map<String, Integer> getCommonIssues() { return commonIssues; }
        public void setCommonIssues(Map<String, Integer> commonIssues) { this.commonIssues = commonIssues; }
    }
    
    public ExerciseSession() {
        this.sessionId = generateSessionId();
        this.sessionStartTime = System.currentTimeMillis();
        this.analysisHistory = new ArrayList<>();
        this.milestones = new ArrayList<>();
        this.jointAccuracyMap = new HashMap<>();
        this.feedbackProvided = new ArrayList<>();
        this.completed = false;
        this.totalFramesAnalyzed = 0;
        this.validFramesCount = 0;
        this.bestScore = 0.0;
        this.worstScore = 100.0;
    }
    
    /**
     * Add analysis result to session
     */
    public void addAnalysisResult(AnalysisResult result) {
        if (result == null) return;
        
        analysisHistory.add(result);
        totalFramesAnalyzed++;
        
        if (result.getOverallScore() > 0) {
            validFramesCount++;
            
            // Update best and worst scores
            double score = result.getOverallScore();
            if (score > bestScore) {
                bestScore = score;
            }
            if (score < worstScore) {
                worstScore = score;
            }
            
            // Update joint accuracy tracking
            updateJointAccuracyTracking(result);
            
            // Check for milestones
            checkForMilestones(result);
        }
    }
    
    /**
     * Complete the session and calculate final statistics
     */
    public void completeSession() {
        this.sessionEndTime = System.currentTimeMillis();
        this.durationSeconds = (int) ((sessionEndTime - sessionStartTime) / 1000);
        this.completed = true;
        
        calculateFinalScores();
    }
    
    /**
     * Calculate final session scores
     */
    private void calculateFinalScores() {
        if (analysisHistory.isEmpty()) {
            return;
        }
        
        double totalOverallScore = 0.0;
        double totalPoseSimilarity = 0.0;
        double totalJointAccuracy = 0.0;
        double totalMovementQuality = 0.0;
        double totalTimingAccuracy = 0.0;
        
        int validResults = 0;
        
        for (AnalysisResult result : analysisHistory) {
            if (result.getOverallScore() > 0) {
                totalOverallScore += result.getOverallScore();
                totalPoseSimilarity += result.getPoseSimilarity();
                totalJointAccuracy += result.getJointAccuracy();
                totalMovementQuality += result.getMovementQuality();
                totalTimingAccuracy += result.getTimingAccuracy();
                validResults++;
            }
        }
        
        if (validResults > 0) {
            this.overallScore = totalOverallScore / validResults;
            this.averagePoseSimilarity = totalPoseSimilarity / validResults;
            this.averageJointAccuracy = totalJointAccuracy / validResults;
            this.averageMovementQuality = totalMovementQuality / validResults;
            this.averageTimingAccuracy = totalTimingAccuracy / validResults;
        }
    }
    
    /**
     * Update joint accuracy tracking
     */
    private void updateJointAccuracyTracking(AnalysisResult result) {
        Map<String, AnalysisResult.JointAnalysis> jointDetails = result.getJointDetails();
        
        for (Map.Entry<String, AnalysisResult.JointAnalysis> entry : jointDetails.entrySet()) {
            String jointName = entry.getKey();
            double accuracy = entry.getValue().getAccuracyScore();
            
            // Update running average for this joint
            Double currentAvg = jointAccuracyMap.get(jointName);
            if (currentAvg == null) {
                jointAccuracyMap.put(jointName, accuracy);
            } else {
                // Simple moving average
                double newAvg = (currentAvg + accuracy) / 2.0;
                jointAccuracyMap.put(jointName, newAvg);
            }
        }
    }
    
    /**
     * Check for session milestones
     */
    private void checkForMilestones(AnalysisResult result) {
        double score = result.getOverallScore();
        long timestamp = result.getAnalysisTimestamp();
        
        // First excellent score
        if (score >= 90 && !hasMilestone("FIRST_EXCELLENT")) {
            addMilestone(timestamp, "FIRST_EXCELLENT", "First excellent form achieved!", score);
        }
        
        // Consistent good form (5 consecutive good scores)
        if (score >= 80 && hasConsecutiveGoodScores(5) && !hasMilestone("CONSISTENT_FORM")) {
            addMilestone(timestamp, "CONSISTENT_FORM", "Maintained good form consistently!", score);
        }
        
        // Improvement milestone (20 point improvement from worst)
        if (score >= worstScore + 20 && !hasMilestone("SIGNIFICANT_IMPROVEMENT")) {
            addMilestone(timestamp, "SIGNIFICANT_IMPROVEMENT", "Significant improvement achieved!", score);
        }
        
        // Session completion milestones
        int sessionProgress = (int) ((timestamp - sessionStartTime) / 1000.0 / 60.0); // minutes
        if (sessionProgress >= 5 && !hasMilestone("HALFWAY_COMPLETE")) {
            addMilestone(timestamp, "HALFWAY_COMPLETE", "Halfway through the session!", score);
        }
    }
    
    /**
     * Add milestone to session
     */
    private void addMilestone(long timestamp, String name, String description, double score) {
        SessionMilestone milestone = new SessionMilestone(timestamp, name, description, score);
        milestones.add(milestone);
    }
    
    /**
     * Check if milestone already exists
     */
    private boolean hasMilestone(String milestoneName) {
        for (SessionMilestone milestone : milestones) {
            if (milestone.getMilestoneName().equals(milestoneName)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check for consecutive good scores
     */
    private boolean hasConsecutiveGoodScores(int count) {
        if (analysisHistory.size() < count) return false;
        
        int consecutiveCount = 0;
        for (int i = analysisHistory.size() - 1; i >= 0; i--) {
            if (analysisHistory.get(i).getOverallScore() >= 80) {
                consecutiveCount++;
                if (consecutiveCount >= count) {
                    return true;
                }
            } else {
                break;
            }
        }
        return false;
    }
    
    /**
     * Generate session statistics
     */
    public SessionStats generateSessionStats() {
        SessionStats stats = new SessionStats();
        
        if (analysisHistory.isEmpty()) {
            return stats;
        }
        
        // Count feedback levels
        int excellent = 0, good = 0, needsImprovement = 0, incorrect = 0;
        Map<String, Integer> issues = new HashMap<>();
        
        for (AnalysisResult result : analysisHistory) {
            AnalysisResult.FeedbackData.FeedbackLevel level = result.getFeedback().getLevel();
            if (level != null) {
                switch (level) {
                    case EXCELLENT: excellent++; break;
                    case GOOD: good++; break;
                    case NEEDS_IMPROVEMENT: needsImprovement++; break;
                    case INCORRECT: incorrect++; break;
                }
            }
            
            // Track common issues
            String[] textInstructions = result.getFeedback().getTextInstructions();
            for (String instruction : textInstructions) {
                issues.put(instruction, issues.getOrDefault(instruction, 0) + 1);
            }
        }
        
        stats.setExcellentFormCount(excellent);
        stats.setGoodFormCount(good);
        stats.setNeedsImprovementCount(needsImprovement);
        stats.setIncorrectFormCount(incorrect);
        stats.setCommonIssues(issues);
        
        // Calculate improvement rate
        if (analysisHistory.size() >= 10) {
            double firstHalfAvg = 0.0, secondHalfAvg = 0.0;
            int halfPoint = analysisHistory.size() / 2;
            
            for (int i = 0; i < halfPoint; i++) {
                firstHalfAvg += analysisHistory.get(i).getOverallScore();
            }
            firstHalfAvg /= halfPoint;
            
            for (int i = halfPoint; i < analysisHistory.size(); i++) {
                secondHalfAvg += analysisHistory.get(i).getOverallScore();
            }
            secondHalfAvg /= (analysisHistory.size() - halfPoint);
            
            stats.setImprovementRate(secondHalfAvg - firstHalfAvg);
        }
        
        // Calculate consistency score (lower variance = higher consistency)
        double variance = calculateScoreVariance();
        stats.setConsistencyScore(Math.max(0.0, 100.0 - variance));
        
        return stats;
    }
    
    /**
     * Calculate score variance for consistency measurement
     */
    private double calculateScoreVariance() {
        if (analysisHistory.size() < 2) return 0.0;
        
        double mean = overallScore;
        double sumSquaredDiffs = 0.0;
        
        for (AnalysisResult result : analysisHistory) {
            double diff = result.getOverallScore() - mean;
            sumSquaredDiffs += diff * diff;
        }
        
        return sumSquaredDiffs / analysisHistory.size();
    }
    
    /**
     * Generate unique session ID
     */
    private String generateSessionId() {
        return "session_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }
    
    // Getters and setters
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getExerciseTemplateId() { return exerciseTemplateId; }
    public void setExerciseTemplateId(String exerciseTemplateId) { this.exerciseTemplateId = exerciseTemplateId; }
    
    public long getSessionStartTime() { return sessionStartTime; }
    public void setSessionStartTime(long sessionStartTime) { this.sessionStartTime = sessionStartTime; }
    
    public long getSessionEndTime() { return sessionEndTime; }
    public void setSessionEndTime(long sessionEndTime) { this.sessionEndTime = sessionEndTime; }
    
    public int getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(int durationSeconds) { this.durationSeconds = durationSeconds; }
    
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    
    public double getOverallScore() { return overallScore; }
    public void setOverallScore(double overallScore) { this.overallScore = overallScore; }
    
    public double getAveragePoseSimilarity() { return averagePoseSimilarity; }
    public void setAveragePoseSimilarity(double averagePoseSimilarity) { this.averagePoseSimilarity = averagePoseSimilarity; }
    
    public double getAverageJointAccuracy() { return averageJointAccuracy; }
    public void setAverageJointAccuracy(double averageJointAccuracy) { this.averageJointAccuracy = averageJointAccuracy; }
    
    public double getAverageMovementQuality() { return averageMovementQuality; }
    public void setAverageMovementQuality(double averageMovementQuality) { this.averageMovementQuality = averageMovementQuality; }
    
    public double getAverageTimingAccuracy() { return averageTimingAccuracy; }
    public void setAverageTimingAccuracy(double averageTimingAccuracy) { this.averageTimingAccuracy = averageTimingAccuracy; }
    
    public int getTotalFramesAnalyzed() { return totalFramesAnalyzed; }
    public void setTotalFramesAnalyzed(int totalFramesAnalyzed) { this.totalFramesAnalyzed = totalFramesAnalyzed; }
    
    public int getValidFramesCount() { return validFramesCount; }
    public void setValidFramesCount(int validFramesCount) { this.validFramesCount = validFramesCount; }
    
    public double getBestScore() { return bestScore; }
    public void setBestScore(double bestScore) { this.bestScore = bestScore; }
    
    public double getWorstScore() { return worstScore; }
    public void setWorstScore(double worstScore) { this.worstScore = worstScore; }
    
    public Map<String, Double> getJointAccuracyMap() { return jointAccuracyMap; }
    public void setJointAccuracyMap(Map<String, Double> jointAccuracyMap) { this.jointAccuracyMap = jointAccuracyMap; }
    
    public List<AnalysisResult> getAnalysisHistory() { return analysisHistory; }
    public void setAnalysisHistory(List<AnalysisResult> analysisHistory) { this.analysisHistory = analysisHistory; }
    
    public List<SessionMilestone> getMilestones() { return milestones; }
    public void setMilestones(List<SessionMilestone> milestones) { this.milestones = milestones; }
    
    public String getUserNotes() { return userNotes; }
    public void setUserNotes(String userNotes) { this.userNotes = userNotes; }
    
    public int getUserRating() { return userRating; }
    public void setUserRating(int userRating) { this.userRating = userRating; }
    
    public List<String> getFeedbackProvided() { return feedbackProvided; }
    public void setFeedbackProvided(List<String> feedbackProvided) { this.feedbackProvided = feedbackProvided; }
}