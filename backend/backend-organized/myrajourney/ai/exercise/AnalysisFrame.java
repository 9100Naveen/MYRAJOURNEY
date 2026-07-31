package com.example.myrajourney.ai.exercise;

/**
 * Result of analyzing a single pose frame
 * Contains form correctness, specific feedback, and scoring information
 */
public class AnalysisFrame {

    private boolean isFormCorrect;
    private double formAccuracy; // 0.0 to 1.0
    private String feedback;
    private String instruction;
    private FormIssue primaryIssue;
    private long timestamp;

    // Joint-specific feedback
    private String targetJoint;
    private double targetJointAngle;
    private double idealJointAngle;
    private double angleDeviation;

    public AnalysisFrame(long timestamp) {
        this.timestamp = timestamp;
        this.formAccuracy = 1.0;
        this.isFormCorrect = true;
    }

    // Getters and setters

    public boolean isFormCorrect() {
        return isFormCorrect;
    }

    public void setFormCorrect(boolean formCorrect) {
        isFormCorrect = formCorrect;
    }

    public double getFormAccuracy() {
        return formAccuracy;
    }

    public void setFormAccuracy(double formAccuracy) {
        this.formAccuracy = Math.max(0.0, Math.min(1.0, formAccuracy));
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public FormIssue getPrimaryIssue() {
        return primaryIssue;
    }

    public void setPrimaryIssue(FormIssue primaryIssue) {
        this.primaryIssue = primaryIssue;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getTargetJoint() {
        return targetJoint;
    }

    public void setTargetJoint(String targetJoint) {
        this.targetJoint = targetJoint;
    }

    public double getTargetJointAngle() {
        return targetJointAngle;
    }

    public void setTargetJointAngle(double targetJointAngle) {
        this.targetJointAngle = targetJointAngle;
    }

    public double getIdealJointAngle() {
        return idealJointAngle;
    }

    public void setIdealJointAngle(double idealJointAngle) {
        this.idealJointAngle = idealJointAngle;
    }

    public double getAngleDeviation() {
        return angleDeviation;
    }

    public void setAngleDeviation(double angleDeviation) {
        this.angleDeviation = angleDeviation;
    }

    /**
     * Types of form issues that can be detected
     */
    public enum FormIssue {
        INSUFFICIENT_RANGE, // Not moving joint enough
        EXCESSIVE_RANGE, // Moving joint too much
        INCORRECT_ANGLE, // Joint at wrong angle
        TOO_FAST, // Moving too quickly
        TOO_SLOW, // Moving too slowly
        INCOMPLETE_MOVEMENT, // Not completing full motion
        WRONG_DIRECTION, // Moving in wrong direction
        POOR_STABILITY, // Unstable posture
        NONE // No issue detected
    }
}
