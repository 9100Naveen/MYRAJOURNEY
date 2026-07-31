package com.example.myrajourney.exercise.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;

import com.example.myrajourney.exercise.models.PoseFrame;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;

import java.util.List;

/**
 * Custom view for displaying pose feedback overlay
 */
public class FeedbackOverlayView extends View {
    private static final String TAG = "FeedbackOverlayView";

    // Paint objects for drawing
    private Paint posePaint;
    private Paint jointPaint;
    private Paint connectionPaint;
    private Paint feedbackPaint;

    // Current pose data
    private PoseFrame currentPoseFrame;
    private Pose currentPose; // Kept for backward compatibility if needed
    private String feedbackColor = "#4CAF50"; // Default green
    private boolean isTrackingActive = false;

    // Drawing parameters
    private static final float LANDMARK_RADIUS = 8.0f;
    private static final float CONNECTION_THICKNESS = 5.0f;
    private static final float FEEDBACK_STROKE_WIDTH = 10.0f;

    // Pose connections for drawing skeleton
    private static final int[][] POSE_CONNECTIONS = {
            // Face
            { PoseLandmark.LEFT_EYE, PoseLandmark.RIGHT_EYE },
            { PoseLandmark.LEFT_EYE, PoseLandmark.LEFT_EAR },
            { PoseLandmark.RIGHT_EYE, PoseLandmark.RIGHT_EAR },
            { PoseLandmark.LEFT_EAR, PoseLandmark.LEFT_SHOULDER },
            { PoseLandmark.RIGHT_EAR, PoseLandmark.RIGHT_SHOULDER },

            // Torso
            { PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER },
            { PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_HIP },
            { PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_HIP },
            { PoseLandmark.LEFT_HIP, PoseLandmark.RIGHT_HIP },

            // Left arm
            { PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_ELBOW },
            { PoseLandmark.LEFT_ELBOW, PoseLandmark.LEFT_WRIST },
            { PoseLandmark.LEFT_WRIST, PoseLandmark.LEFT_PINKY },
            { PoseLandmark.LEFT_WRIST, PoseLandmark.LEFT_INDEX },
            { PoseLandmark.LEFT_WRIST, PoseLandmark.LEFT_THUMB },

            // Right arm
            { PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_ELBOW },
            { PoseLandmark.RIGHT_ELBOW, PoseLandmark.RIGHT_WRIST },
            { PoseLandmark.RIGHT_WRIST, PoseLandmark.RIGHT_PINKY },
            { PoseLandmark.RIGHT_WRIST, PoseLandmark.RIGHT_INDEX },
            { PoseLandmark.RIGHT_WRIST, PoseLandmark.RIGHT_THUMB },

            // Left leg
            { PoseLandmark.LEFT_HIP, PoseLandmark.LEFT_KNEE },
            { PoseLandmark.LEFT_KNEE, PoseLandmark.LEFT_ANKLE },
            { PoseLandmark.LEFT_ANKLE, PoseLandmark.LEFT_HEEL },
            { PoseLandmark.LEFT_ANKLE, PoseLandmark.LEFT_FOOT_INDEX },

            // Right leg
            { PoseLandmark.RIGHT_HIP, PoseLandmark.RIGHT_KNEE },
            { PoseLandmark.RIGHT_KNEE, PoseLandmark.RIGHT_ANKLE },
            { PoseLandmark.RIGHT_ANKLE, PoseLandmark.RIGHT_HEEL },
            { PoseLandmark.RIGHT_ANKLE, PoseLandmark.RIGHT_FOOT_INDEX }
    };

    public FeedbackOverlayView(Context context) {
        super(context);
        init();
    }

    public FeedbackOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FeedbackOverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * Initialize paint objects
     */
    private void init() {
        // Pose landmarks paint
        posePaint = new Paint();
        posePaint.setColor(Color.WHITE);
        posePaint.setStyle(Paint.Style.FILL);
        posePaint.setAntiAlias(true);

        // Joint landmarks paint (highlighted)
        jointPaint = new Paint();
        jointPaint.setColor(Color.YELLOW);
        jointPaint.setStyle(Paint.Style.FILL);
        jointPaint.setAntiAlias(true);

        // Connection lines paint
        connectionPaint = new Paint();
        connectionPaint.setColor(Color.WHITE);
        connectionPaint.setStyle(Paint.Style.STROKE);
        connectionPaint.setStrokeWidth(CONNECTION_THICKNESS);
        connectionPaint.setAntiAlias(true);

        // Feedback border paint
        feedbackPaint = new Paint();
        feedbackPaint.setStyle(Paint.Style.STROKE);
        feedbackPaint.setStrokeWidth(FEEDBACK_STROKE_WIDTH);
        feedbackPaint.setAntiAlias(true);
        updateFeedbackColor();
    }

    /**
     * Update current pose for drawing
     */
    public void updatePose(Pose pose) {
        this.currentPose = pose;
        this.currentPoseFrame = new PoseFrame(pose, System.currentTimeMillis());
        invalidate();
    }

    public void updatePoseFrame(PoseFrame frame) {
        this.currentPoseFrame = frame;
        invalidate();
    }

    /**
     * Set feedback color
     */
    public void setFeedbackColor(String colorHex) {
        this.feedbackColor = colorHex;
        updateFeedbackColor();
        invalidate();
    }

    /**
     * Update feedback paint color
     */
    private void updateFeedbackColor() {
        try {
            int color = Color.parseColor(feedbackColor);
            feedbackPaint.setColor(color);

            // Also update connection color to match feedback
            connectionPaint.setColor(color);
        } catch (IllegalArgumentException e) {
            // Fallback to green if color parsing fails
            feedbackPaint.setColor(Color.GREEN);
            connectionPaint.setColor(Color.GREEN);
        }
    }

    /**
     * Set tracking active state
     */
    public void setTrackingActive(boolean active) {
        this.isTrackingActive = active;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!isTrackingActive) {
            drawInstructions(canvas);
            return;
        }

        // Draw feedback border
        drawFeedbackBorder(canvas);

        // Draw pose if available
        if (currentPoseFrame != null) {
            drawPoseFrame(canvas);
        } else {
            drawNoPoseMessage(canvas);
        }
    }

    /**
     * Draw instructions when not tracking
     */
    private void drawInstructions(Canvas canvas) {
        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(48);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);

        String instruction = "Position yourself in front of the camera\\nPress Start to begin exercise";

        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;

        // Draw text with shadow for better visibility
        textPaint.setShadowLayer(4, 2, 2, Color.BLACK);

        String[] lines = instruction.split("\\n");
        float lineHeight = textPaint.getTextSize() * 1.2f;
        float startY = centerY - (lines.length - 1) * lineHeight / 2f;

        for (int i = 0; i < lines.length; i++) {
            canvas.drawText(lines[i], centerX, startY + i * lineHeight, textPaint);
        }
    }

    /**
     * Draw feedback border around the view
     */
    private void drawFeedbackBorder(Canvas canvas) {
        float margin = FEEDBACK_STROKE_WIDTH / 2f;
        canvas.drawRect(margin, margin,
                getWidth() - margin, getHeight() - margin,
                feedbackPaint);
    }

    /**
     * Draw the detected pose from PoseFrame
     */
    private void drawPoseFrame(Canvas canvas) {
        // Draw connections first
        for (int[] connection : POSE_CONNECTIONS) {
            PointF startPointRaw = currentPoseFrame.getPoint(connection[0]);
            PointF endPointRaw = currentPoseFrame.getPoint(connection[1]);

            if (startPointRaw != null && endPointRaw != null &&
                    currentPoseFrame.getConfidence(connection[0]) > 0.5f &&
                    currentPoseFrame.getConfidence(connection[1]) > 0.5f) {

                PointF startPoint = translatePoint(startPointRaw);
                PointF endPoint = translatePoint(endPointRaw);

                canvas.drawLine(startPoint.x, startPoint.y,
                        endPoint.x, endPoint.y, connectionPaint);
            }
        }

        // Key joints to highlight
        int[] keyJoints = { 11, 12, 13, 14, 15, 16 }; // Shoulders, Elbows, Wrists

        // Draw all landmarks (simplified to 33 standards)
        for (int i = 0; i < 33; i++) {
            PointF pointRaw = currentPoseFrame.getPoint(i);
            if (pointRaw != null && currentPoseFrame.getConfidence(i) > 0.5f) {
                PointF point = translatePoint(pointRaw);
                Paint paint = isKeyJoint(i, keyJoints) ? jointPaint : posePaint;
                float radius = isKeyJoint(i, keyJoints) ? LANDMARK_RADIUS * 1.5f : LANDMARK_RADIUS;
                canvas.drawCircle(point.x, point.y, radius, paint);
            }
        }
    }

    /**
     * Check if landmark is a key joint
     */
    private boolean isKeyJoint(int landmarkType, int[] keyJoints) {
        for (int keyJoint : keyJoints) {
            if (landmarkType == keyJoint) {
                return true;
            }
        }
        return false;
    }

    /**
     * Translate pose coordinates to view coordinates
     */
    private PointF translatePoint(PointF posePoint) {
        // MediaPipe coordinates are normalized (0-1)
        // Need to scale to view dimensions and flip horizontally for front camera
        float x = (1.0f - posePoint.x) * getWidth(); // Flip horizontally
        float y = posePoint.y * getHeight();

        return new PointF(x, y);
    }

    /**
     * Draw message when no pose is detected
     */
    private void drawNoPoseMessage(Canvas canvas) {
        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(36);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);
        textPaint.setShadowLayer(4, 2, 2, Color.BLACK);

        String message = "Move into camera view\\nEnsure good lighting";

        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;

        String[] lines = message.split("\\n");
        float lineHeight = textPaint.getTextSize() * 1.2f;
        float startY = centerY - (lines.length - 1) * lineHeight / 2f;

        for (int i = 0; i < lines.length; i++) {
            canvas.drawText(lines[i], centerX, startY + i * lineHeight, textPaint);
        }
    }

    /**
     * Clear the overlay
     */
    public void clear() {
        currentPose = null;
        currentPoseFrame = null;
        invalidate();
    }
}