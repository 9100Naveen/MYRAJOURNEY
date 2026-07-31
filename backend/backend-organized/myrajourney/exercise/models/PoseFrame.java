package com.example.myrajourney.exercise.models;

import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult;
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a single frame of pose data with timestamp and analysis
 */
public class PoseFrame {
    private long timestamp;
    private Pose pose;
    private PoseLandmarkerResult mpResult;
    public Map<String, Double> jointAngles; // Changed from private
    private Map<String, Float> landmarkConfidences;
    private double overallConfidence;

    public PoseFrame(Pose pose, long timestamp) {
        this.pose = pose;
        this.timestamp = timestamp;
        this.jointAngles = new HashMap<>();
        this.landmarkConfidences = new HashMap<>();
        this.overallConfidence = calculateOverallConfidence();
    }

    public PoseFrame(PoseLandmarkerResult result, long timestamp) {
        this.mpResult = result;
        this.timestamp = timestamp;
        this.jointAngles = new HashMap<>();
        this.landmarkConfidences = new HashMap<>();
        this.overallConfidence = calculateMPConfidence();
    }

    /**
     * Calculate overall confidence based on landmark confidences
     */
    private double calculateMPConfidence() {
        if (mpResult == null || mpResult.landmarks().isEmpty()) {
            return 0.0;
        }

        List<NormalizedLandmark> landmarks = mpResult.landmarks().get(0);
        float totalPresence = 0.0f;
        for (NormalizedLandmark landmark : landmarks) {
            totalPresence += landmark.presence().orElse(0.0f);
        }

        return totalPresence / landmarks.size();
    }

    /**
     * Get landmark position by type
     */
    public PoseLandmark getLandmark(int landmarkType) {
        return pose != null ? pose.getPoseLandmark(landmarkType) : null;
    }

    /**
     * Check if pose has minimum required landmarks for analysis
     */
    public boolean isValidForAnalysis() {
        if (pose != null) {
            return isMLKitValid();
        } else if (mpResult != null) {
            return isMediaPipeValid();
        }
        return false;
    }

    private boolean isMLKitValid() {
        int[] required = {
                PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER,
                PoseLandmark.LEFT_ELBOW, PoseLandmark.RIGHT_ELBOW,
                PoseLandmark.LEFT_WRIST, PoseLandmark.RIGHT_WRIST
        };
        int valid = 0;
        for (int type : required) {
            PoseLandmark lm = pose.getPoseLandmark(type);
            if (lm != null && lm.getInFrameLikelihood() >= 0.5f)
                valid++;
        }
        return valid >= 3;
    }

    private boolean isMediaPipeValid() {
        if (mpResult.landmarks().isEmpty())
            return false;
        List<NormalizedLandmark> landmarks = mpResult.landmarks().get(0);
        // MediaPipe indices are same as ML Kit for standard pose
        int[] required = { 11, 12, 13, 14, 15, 16 }; // Shoulders, Elbows, Wrists
        int valid = 0;
        for (int idx : required) {
            if (idx < landmarks.size() && landmarks.get(idx).visibility().orElse(0.0f) >= 0.5f) {
                valid++;
            }
        }
        return valid >= 3;
    }

    // Getters and setters
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Pose getPose() {
        return pose;
    }

    public void setPose(Pose pose) {
        this.pose = pose;
    }

    public Map<String, Double> getJointAngles() {
        return jointAngles;
    }

    public void setJointAngles(Map<String, Double> jointAngles) {
        this.jointAngles = jointAngles;
    }

    public Map<String, Float> getLandmarkConfidences() {
        return landmarkConfidences;
    }

    public void setLandmarkConfidences(Map<String, Float> landmarkConfidences) {
        this.landmarkConfidences = landmarkConfidences;
    }

    public double getOverallConfidence() {
        return overallConfidence;
    }

    public void setOverallConfidence(double overallConfidence) {
        this.overallConfidence = overallConfidence;
    }

    private double calculateOverallConfidence() {
        if (pose == null)
            return calculateMPConfidence();
        // ML Kit style
        float totalLikelihood = 0.0f;
        int count = 0;
        for (int i = 0; i < 33; i++) {
            com.google.mlkit.vision.pose.PoseLandmark lm = pose.getPoseLandmark(i);
            if (lm != null) {
                totalLikelihood += lm.getInFrameLikelihood();
                count++;
            }
        }
        return count > 0 ? totalLikelihood / count : 0.0;
    }

    public android.graphics.PointF getPoint(int landmarkType) {
        if (pose != null) {
            com.google.mlkit.vision.pose.PoseLandmark lm = pose.getPoseLandmark(landmarkType);
            return lm != null ? new android.graphics.PointF(lm.getPosition().x, lm.getPosition().y) : null;
        } else if (mpResult != null && !mpResult.landmarks().isEmpty()) {
            List<NormalizedLandmark> landmarks = mpResult.landmarks().get(0);
            if (landmarkType < landmarks.size()) {
                NormalizedLandmark lm = landmarks.get(landmarkType);
                return new android.graphics.PointF(lm.x(), lm.y());
            }
        }
        return null;
    }

    public float getConfidence(int landmarkType) {
        if (pose != null) {
            com.google.mlkit.vision.pose.PoseLandmark lm = pose.getPoseLandmark(landmarkType);
            return lm != null ? lm.getInFrameLikelihood() : 0f;
        } else if (mpResult != null && !mpResult.landmarks().isEmpty()) {
            List<NormalizedLandmark> landmarks = mpResult.landmarks().get(0);
            if (landmarkType < landmarks.size()) {
                return landmarks.get(landmarkType).visibility().orElse(0.0f);
            }
        }
        return 0f;
    }

    /**
     * Check if there's significant movement compared to previous frame
     */
    public boolean hasSignificantMovement() {
        // Simple movement detection based on pose confidence and landmark positions
        if (!isValidForAnalysis()) {
            return false;
        }
        
        // If we have good confidence and valid landmarks, assume movement
        return getAverageConfidence() > 0.4;
    }

    /**
     * Get average confidence across all key landmarks
     */
    public double getAverageConfidence() {
        if (pose != null) {
            return getOverallConfidence();
        } else if (mpResult != null && !mpResult.landmarks().isEmpty()) {
            List<NormalizedLandmark> landmarks = mpResult.landmarks().get(0);
            
            // Focus on key upper body landmarks for RA exercises
            int[] keyLandmarks = {11, 12, 13, 14, 15, 16}; // Shoulders, elbows, wrists
            float totalConfidence = 0.0f;
            int validCount = 0;
            
            for (int idx : keyLandmarks) {
                if (idx < landmarks.size()) {
                    float confidence = landmarks.get(idx).visibility().orElse(0.0f);
                    totalConfidence += confidence;
                    validCount++;
                }
            }
            
            return validCount > 0 ? totalConfidence / validCount : 0.0;
        }
        
        return 0.0;
    }

    /**
     * Get movement intensity based on landmark positions
     */
    public double getMovementIntensity() {
        // Simple intensity calculation based on confidence and visibility
        double avgConfidence = getAverageConfidence();
        
        if (avgConfidence > 0.7) {
            return 1.0; // High intensity
        } else if (avgConfidence > 0.5) {
            return 0.7; // Medium intensity
        } else if (avgConfidence > 0.3) {
            return 0.4; // Low intensity
        } else {
            return 0.0; // No movement
        }
    }
}