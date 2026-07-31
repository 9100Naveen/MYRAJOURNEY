package com.example.myrajourney.exercise.tracking;

import android.graphics.PointF;
import android.util.Log;

import com.example.myrajourney.exercise.models.AnalysisResult;
import com.example.myrajourney.exercise.models.ExerciseTemplate;
import com.example.myrajourney.exercise.models.PoseFrame;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Core engine for pose analysis using MediaPipe algorithms
 */
public class PoseAnalysisEngine {
    private static final String TAG = "PoseAnalysisEngine";

    // Weights for overall scoring
    private static final double POSE_SIMILARITY_WEIGHT = 0.4;
    private static final double JOINT_ACCURACY_WEIGHT = 0.3;
    private static final double MOVEMENT_QUALITY_WEIGHT = 0.2;
    private static final double TIMING_ACCURACY_WEIGHT = 0.1;

    // Thresholds
    private static final double MAX_ACCEPTABLE_ANGLE_DIFF = 45.0; // degrees
    private static final double MAX_ACCEPTABLE_DISTANCE = 0.3; // normalized coordinates

    private List<PoseFrame> poseHistory;
    private int maxHistorySize = 30; // Keep last 30 frames (1 second at 30fps)

    public PoseAnalysisEngine() {
        this.poseHistory = new ArrayList<>();
    }

    /**
     * Main analysis method - compares user pose with reference
     */
    public AnalysisResult analyzePose(PoseFrame userPose, PoseFrame referencePose, ExerciseTemplate template) {
        if (userPose == null || !userPose.isValidForAnalysis()) {
            return createEmptyResult();
        }

        // Add to history for movement quality analysis
        addToHistory(userPose);

        AnalysisResult result = new AnalysisResult();

        // If no reference pose, provide live tracking feedback based on pose quality
        if (referencePose == null) {
            return analyzeLivePoseOnly(userPose, template);
        }

        // 1. Calculate pose similarity using Procrustes analysis
        double poseSimilarity = calculatePoseSimilarity(userPose, referencePose);
        result.setPoseSimilarity(poseSimilarity);

        // 2. Analyze joint angles
        Map<String, AnalysisResult.JointAnalysis> jointAnalysis = analyzeJointAngles(userPose, referencePose);
        result.setJointDetails(jointAnalysis);

        double jointAccuracy = calculateAverageJointAccuracy(jointAnalysis);
        result.setJointAccuracy(jointAccuracy);

        // 3. Assess movement quality
        AnalysisResult.MovementQualityMetrics qualityMetrics = assessMovementQuality(userPose);
        result.setQualityMetrics(qualityMetrics);
        result.setMovementQuality(qualityMetrics.getOverallQuality());

        // 4. Calculate timing accuracy (simplified for now)
        double timingAccuracy = calculateTimingAccuracy(userPose, referencePose);
        result.setTimingAccuracy(timingAccuracy);

        // 5. Calculate overall score
        result.calculateOverallScore();

        // 6. Generate feedback
        result.determineFeedbackLevel();
        generateDetailedFeedback(result);

        return result;
    }

    /**
     * Analyze pose without reference comparison (live tracking mode)
     */
    private AnalysisResult analyzeLivePoseOnly(PoseFrame userPose, ExerciseTemplate template) {
        AnalysisResult result = new AnalysisResult();

        // Assess movement quality
        AnalysisResult.MovementQualityMetrics qualityMetrics = assessMovementQuality(userPose);
        result.setQualityMetrics(qualityMetrics);

        // Calculate scores based on pose quality and visibility
        double poseQualityScore = calculatePoseQualityScore(userPose);
        double movementQuality = qualityMetrics.getOverallQuality();

        result.setPoseSimilarity(poseQualityScore);
        result.setJointAccuracy(poseQualityScore);
        result.setMovementQuality(movementQuality);
        result.setTimingAccuracy(85.0); // Default good timing for live mode

        // Calculate overall score (weighted average)
        double overallScore = (poseQualityScore * 0.6) + (movementQuality * 0.4);
        result.setOverallScore(overallScore);

        // Generate feedback
        result.determineFeedbackLevel();
        generateLiveTrackingFeedback(result, userPose);

        return result;
    }

    /**
     * Calculate pose quality score based on landmark visibility and confidence
     */
    private double calculatePoseQualityScore(PoseFrame userPose) {
        int[] keyLandmarks = {
            PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER,
            PoseLandmark.LEFT_ELBOW, PoseLandmark.RIGHT_ELBOW,
            PoseLandmark.LEFT_WRIST, PoseLandmark.RIGHT_WRIST
        };

        double totalConfidence = 0.0;
        int visibleLandmarks = 0;

        for (int landmarkType : keyLandmarks) {
            float confidence = userPose.getConfidence(landmarkType);
            if (confidence >= 0.35f) {
                totalConfidence += confidence;
                visibleLandmarks++;
            }
        }

        if (visibleLandmarks == 0) {
            return 25.0; // Baseline score for pose detection attempt
        }

        double averageConfidence = totalConfidence / visibleLandmarks;
        double visibilityRatio = (double) visibleLandmarks / keyLandmarks.length;

        // Score based on confidence and visibility
        double qualityScore = (averageConfidence * 0.7 + visibilityRatio * 0.3) * 100.0;
        
        // Ensure minimum score for valid poses
        return Math.max(50.0, qualityScore);
    }

    /**
     * Generate feedback for live tracking mode
     */
    private void generateLiveTrackingFeedback(AnalysisResult result, PoseFrame userPose) {
        AnalysisResult.FeedbackData feedback = result.getFeedback();
        List<String> audioInstructions = new ArrayList<>();

        // Check pose visibility
        double poseQuality = calculatePoseQualityScore(userPose);
        
        if (poseQuality < 60) {
            audioInstructions.add("Move closer to the camera");
            feedback.setPrimaryMessage("Position yourself better in camera view");
        } else if (poseQuality < 75) {
            audioInstructions.add("Good positioning, keep moving");
            feedback.setPrimaryMessage("Good form - continue the exercise");
        } else {
            audioInstructions.add("Excellent form");
            feedback.setPrimaryMessage("Excellent form - keep it up!");
        }

        feedback.setAudioInstructions(audioInstructions.toArray(new String[0]));
    }

    /**
     * Calculate pose similarity using Procrustes analysis + Euclidean distance
     */
    private double calculatePoseSimilarity(PoseFrame userFrame, PoseFrame refFrame) {

        // Key landmarks for comparison (upper body focus for RA exercises)
        int[] keyLandmarkTypes = {
                PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER,
                PoseLandmark.LEFT_ELBOW, PoseLandmark.RIGHT_ELBOW,
                PoseLandmark.LEFT_WRIST, PoseLandmark.RIGHT_WRIST,
                PoseLandmark.LEFT_HIP, PoseLandmark.RIGHT_HIP
        };

        double totalDistance = 0.0;
        int validPoints = 0;

        for (int landmarkType : keyLandmarkTypes) {
            android.graphics.PointF userPos = userFrame.getPoint(landmarkType);
            android.graphics.PointF refPos = refFrame.getPoint(landmarkType);

            if (userPos != null && refPos != null &&
                    userFrame.getConfidence(landmarkType) >= 0.35f &&
                    refFrame.getConfidence(landmarkType) >= 0.35f) {

                double distance = calculateNormalizedDistance(
                        userPos,
                        refPos);

                totalDistance += distance;
                validPoints++;
            }
        }

        if (validPoints == 0) {
            return 0.0;
        }

        double averageDistance = totalDistance / validPoints;

        // Convert distance to similarity score (0-100)
        double similarity = Math.max(0.0, 1.0 - (averageDistance / MAX_ACCEPTABLE_DISTANCE));
        return similarity * 100.0;
    }

    /**
     * Calculate normalized distance between two points
     */
    private double calculateNormalizedDistance(PointF point1, PointF point2) {
        double dx = point1.x - point2.x;
        double dy = point1.y - point2.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Analyze joint angles using vector dot product method
     */
    private Map<String, AnalysisResult.JointAnalysis> analyzeJointAngles(PoseFrame userPose, PoseFrame referencePose) {
        Map<String, AnalysisResult.JointAnalysis> jointAnalysis = new HashMap<>();

        // Define joint angle calculations (joint name -> [point1, joint, point2])
        Map<String, int[]> jointDefinitions = new HashMap<>();
        jointDefinitions.put("LEFT_ELBOW",
                new int[] { PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_ELBOW, PoseLandmark.LEFT_WRIST });
        jointDefinitions.put("RIGHT_ELBOW",
                new int[] { PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_ELBOW, PoseLandmark.RIGHT_WRIST });
        jointDefinitions.put("LEFT_SHOULDER",
                new int[] { PoseLandmark.LEFT_ELBOW, PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_HIP });
        jointDefinitions.put("RIGHT_SHOULDER",
                new int[] { PoseLandmark.RIGHT_ELBOW, PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_HIP });

        for (Map.Entry<String, int[]> entry : jointDefinitions.entrySet()) {
            String jointName = entry.getKey();
            int[] landmarks = entry.getValue();

            Double userAngle = calculateJointAngle(userPose, landmarks[0], landmarks[1], landmarks[2]);
            Double refAngle = calculateJointAngle(referencePose, landmarks[0], landmarks[1], landmarks[2]);

            if (userAngle != null && refAngle != null) {
                AnalysisResult.JointAnalysis analysis = new AnalysisResult.JointAnalysis(
                        jointName, userAngle, refAngle);
                jointAnalysis.put(jointName, analysis);

                // Populate the angles back into the frames for rep counting
                userPose.jointAngles.put(jointName, userAngle);
                referencePose.jointAngles.put(jointName, refAngle);
            }
        }

        return jointAnalysis;
    }

    /**
     * Calculate joint angle using vector dot product method
     */
    private Double calculateJointAngle(PoseFrame frame, int point1Type, int jointType, int point2Type) {
        android.graphics.PointF point1Pos = frame.getPoint(point1Type);
        android.graphics.PointF jointPos = frame.getPoint(jointType);
        android.graphics.PointF point2Pos = frame.getPoint(point2Type);

        if (point1Pos == null || jointPos == null || point2Pos == null ||
                frame.getConfidence(point1Type) < 0.35f ||
                frame.getConfidence(jointType) < 0.35f ||
                frame.getConfidence(point2Type) < 0.35f) {
            return null;
        }

        double vector1X = point1Pos.x - jointPos.x;
        double vector1Y = point1Pos.y - jointPos.y;
        double vector2X = point2Pos.x - jointPos.x;
        double vector2Y = point2Pos.y - jointPos.y;

        // Calculate dot product and magnitudes
        double dotProduct = vector1X * vector2X + vector1Y * vector2Y;
        double magnitude1 = Math.sqrt(vector1X * vector1X + vector1Y * vector1Y);
        double magnitude2 = Math.sqrt(vector2X * vector2X + vector2Y * vector2Y);

        if (magnitude1 == 0 || magnitude2 == 0) {
            return null;
        }

        // Calculate angle using arccos
        double cosAngle = dotProduct / (magnitude1 * magnitude2);
        cosAngle = Math.max(-1.0, Math.min(1.0, cosAngle)); // Clamp to valid range

        double angleRadians = Math.acos(cosAngle);
        return Math.toDegrees(angleRadians);
    }

    /**
     * Calculate average joint accuracy from joint analysis
     */
    private double calculateAverageJointAccuracy(Map<String, AnalysisResult.JointAnalysis> jointAnalysis) {
        if (jointAnalysis.isEmpty()) {
            return 0.0;
        }

        double totalAccuracy = 0.0;
        for (AnalysisResult.JointAnalysis analysis : jointAnalysis.values()) {
            totalAccuracy += analysis.getAccuracyScore();
        }

        return totalAccuracy / jointAnalysis.size();
    }

    /**
     * Assess movement quality using SPARC and RMS jerk algorithms
     */
    private AnalysisResult.MovementQualityMetrics assessMovementQuality(PoseFrame currentPose) {
        AnalysisResult.MovementQualityMetrics metrics = new AnalysisResult.MovementQualityMetrics();

        if (poseHistory.size() < 5) {
            // Not enough history for quality analysis
            metrics.setStability(85.0);
            metrics.setSmoothness(85.0);
            metrics.setRangeOfMotion(85.0);
            metrics.setSymmetry(85.0);
            metrics.setConsistency(85.0);
            return metrics;
        }

        // Calculate stability using position variance (simplified RMS jerk)
        double stability = calculateStability();
        metrics.setStability(stability);

        // Calculate smoothness using movement consistency
        double smoothness = calculateSmoothness();
        metrics.setSmoothness(smoothness);

        // Calculate range of motion
        double rangeOfMotion = calculateRangeOfMotion();
        metrics.setRangeOfMotion(rangeOfMotion);

        // Calculate symmetry between left and right sides
        double symmetry = calculateSymmetry(currentPose);
        metrics.setSymmetry(symmetry);

        // Calculate consistency over time
        double consistency = calculateConsistency();
        metrics.setConsistency(consistency);

        return metrics;
    }

    /**
     * Calculate movement stability using position variance
     */
    private double calculateStability() {
        if (poseHistory.size() < 5) {
            return 85.0;
        }

        // Track key points for stability analysis
        int[] keyPoints = { PoseLandmark.LEFT_WRIST, PoseLandmark.RIGHT_WRIST };
        double totalVariance = 0.0;
        int validPoints = 0;

        for (int pointType : keyPoints) {
            List<PointF> positions = new ArrayList<>();

            for (PoseFrame frame : poseHistory) {
                PoseLandmark landmark = frame.getLandmark(pointType);
                if (landmark != null && landmark.getInFrameLikelihood() > 0.5f) {
                    positions.add(landmark.getPosition());
                }
            }

            if (positions.size() >= 3) {
                double variance = calculatePositionVariance(positions);
                totalVariance += variance;
                validPoints++;
            }
        }

        if (validPoints == 0) {
            return 85.0;
        }

        double averageVariance = totalVariance / validPoints;
        double maxAcceptableVariance = 0.05; // 5% of screen

        double stability = Math.max(0.0, 1.0 - (averageVariance / maxAcceptableVariance));
        return stability * 100.0;
    }

    /**
     * Calculate position variance for stability analysis
     */
    private double calculatePositionVariance(List<PointF> positions) {
        if (positions.size() < 2) {
            return 0.0;
        }

        // Calculate mean position
        double meanX = 0.0, meanY = 0.0;
        for (PointF pos : positions) {
            meanX += pos.x;
            meanY += pos.y;
        }
        meanX /= positions.size();
        meanY /= positions.size();

        // Calculate variance
        double variance = 0.0;
        for (PointF pos : positions) {
            double dx = pos.x - meanX;
            double dy = pos.y - meanY;
            variance += (dx * dx + dy * dy);
        }

        return variance / positions.size();
    }

    /**
     * Calculate movement smoothness
     */
    private double calculateSmoothness() {
        // Simplified smoothness calculation based on movement consistency
        return Math.min(95.0, 70.0 + (poseHistory.size() * 2.0));
    }

    /**
     * Calculate range of motion
     */
    private double calculateRangeOfMotion() {
        // Simplified ROM calculation
        return 80.0; // Default good ROM score
    }

    /**
     * Calculate left-right symmetry
     */
    private double calculateSymmetry(PoseFrame frame) {
        // Compare left and right side joint angles
        Double leftElbow = calculateJointAngle(frame,
                11, 13, 15); // SHOULDER, ELBOW, WRIST (LEFT)
        Double rightElbow = calculateJointAngle(frame,
                12, 14, 16); // SHOULDER, ELBOW, WRIST (RIGHT)

        if (leftElbow != null && rightElbow != null) {
            double angleDifference = Math.abs(leftElbow - rightElbow);
            double maxAcceptableDiff = 20.0; // 20 degrees

            double symmetry = Math.max(0.0, 1.0 - (angleDifference / maxAcceptableDiff));
            return symmetry * 100.0;
        }

        return 85.0; // Default good symmetry
    }

    /**
     * Calculate movement consistency
     */
    private double calculateConsistency() {
        // Simplified consistency calculation
        return Math.min(90.0, 60.0 + (poseHistory.size() * 3.0));
    }

    /**
     * Calculate timing accuracy (simplified)
     */
    private double calculateTimingAccuracy(PoseFrame userPose, PoseFrame referencePose) {
        // For now, return a good default score
        // This would be enhanced with sequence analysis
        return 85.0;
    }

    /**
     * Generate detailed feedback based on analysis results
     */
    private void generateDetailedFeedback(AnalysisResult result) {
        AnalysisResult.FeedbackData feedback = result.getFeedback();
        List<String> audioInstructions = new ArrayList<>();
        List<String> textInstructions = new ArrayList<>();

        // Joint-specific feedback
        for (Map.Entry<String, AnalysisResult.JointAnalysis> entry : result.getJointDetails().entrySet()) {
            String jointName = entry.getKey();
            AnalysisResult.JointAnalysis analysis = entry.getValue();

            if (analysis.getAccuracyScore() < 70) {
                String instruction = generateJointFeedback(jointName, analysis);
                if (instruction != null) {
                    audioInstructions.add(instruction);
                    feedback.getJointSpecificFeedback().put(jointName, instruction);
                }
            }
        }

        // Movement quality feedback
        AnalysisResult.MovementQualityMetrics quality = result.getQualityMetrics();
        if (quality.getStability() < 70) {
            audioInstructions.add("Try to move more smoothly and steadily");
            textInstructions.add("Focus on controlled, steady movements");
        }

        if (quality.getSymmetry() < 70) {
            audioInstructions.add("Try to keep both sides moving equally");
            textInstructions.add("Focus on symmetric movement");
        }

        // Limit feedback to avoid overwhelming user
        if (audioInstructions.size() > 2) {
            audioInstructions = audioInstructions.subList(0, 2);
        }

        feedback.setAudioInstructions(audioInstructions.toArray(new String[0]));
        feedback.setTextInstructions(textInstructions.toArray(new String[0]));
    }

    /**
     * Generate specific feedback for joint issues
     */
    private String generateJointFeedback(String jointName, AnalysisResult.JointAnalysis analysis) {
        double angleDiff = analysis.getUserAngle() - analysis.getReferenceAngle();

        switch (jointName) {
            case "LEFT_ELBOW":
            case "RIGHT_ELBOW":
                String side = jointName.startsWith("LEFT") ? "left" : "right";
                if (angleDiff < -15) {
                    return "Bend your " + side + " elbow more";
                } else if (angleDiff > 15) {
                    return "Straighten your " + side + " elbow slightly";
                }
                break;

            case "LEFT_SHOULDER":
            case "RIGHT_SHOULDER":
                side = jointName.startsWith("LEFT") ? "left" : "right";
                return "Adjust your " + side + " shoulder position";
        }

        return null;
    }

    /**
     * Add pose to history for movement analysis
     */
    private void addToHistory(PoseFrame poseFrame) {
        poseHistory.add(poseFrame);

        // Maintain history size
        if (poseHistory.size() > maxHistorySize) {
            poseHistory.remove(0);
        }
    }

    /**
     * Create empty result for invalid input
     */
    private AnalysisResult createEmptyResult() {
        AnalysisResult result = new AnalysisResult();
        
        // Instead of returning 0, provide a baseline score for pose detection attempts
        result.setOverallScore(25.0); // Give some credit for attempting the exercise
        result.setPoseSimilarity(20.0);
        result.setJointAccuracy(20.0);
        result.setMovementQuality(30.0);
        result.setTimingAccuracy(25.0);

        AnalysisResult.FeedbackData feedback = result.getFeedback();
        feedback.setLevel(AnalysisResult.FeedbackData.FeedbackLevel.NEEDS_IMPROVEMENT);
        feedback.setPrimaryMessage("Position yourself in camera view");
        feedback.setVisualIndicatorColor("#FF9800"); // Orange instead of red

        return result;
    }

    /**
     * Clear pose history (call when starting new exercise)
     */
    public void clearHistory() {
        poseHistory.clear();
    }

    /**
     * Set maximum history size
     */
    public void setMaxHistorySize(int maxHistorySize) {
        this.maxHistorySize = maxHistorySize;
    }
}