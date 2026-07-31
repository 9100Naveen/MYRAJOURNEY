package com.example.myrajourney.rehab.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;
import java.util.Map;

/**
 * Custom view for displaying motion tracking overlays on camera preview
 */
public class MotionOverlayView extends View {

    private Paint jointPaint;
    private Paint connectionPaint;
    private Paint accuracyPaint;

    private Map<String, Point> jointPositions;
    private float currentAccuracy = 0.0f;
    private boolean showJoints = true;

    public MotionOverlayView(Context context) {
        super(context);
        init();
    }

    public MotionOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MotionOverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Paint for drawing joint points
        jointPaint = new Paint();
        jointPaint.setAntiAlias(true);
        jointPaint.setStyle(Paint.Style.FILL);
        jointPaint.setStrokeWidth(8f);

        // Paint for drawing connections between joints
        connectionPaint = new Paint();
        connectionPaint.setAntiAlias(true);
        connectionPaint.setStyle(Paint.Style.STROKE);
        connectionPaint.setStrokeWidth(4f);
        connectionPaint.setColor(Color.WHITE);

        // Paint for accuracy indicator
        accuracyPaint = new Paint();
        accuracyPaint.setAntiAlias(true);
        accuracyPaint.setStyle(Paint.Style.STROKE);
        accuracyPaint.setStrokeWidth(6f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (jointPositions != null && showJoints) {
            drawJointPoints(canvas);
            drawJointConnections(canvas);
        }

        drawAccuracyIndicator(canvas);
    }

    private void drawJointPoints(Canvas canvas) {
        for (Map.Entry<String, Point> entry : jointPositions.entrySet()) {
            String jointName = entry.getKey();
            Point position = entry.getValue();

            // Set color based on joint type and accuracy
            int color = getJointColor(jointName);
            jointPaint.setColor(color);

            // Draw joint point
            canvas.drawCircle(position.x, position.y, 8f, jointPaint);

            // Draw joint label (optional, for debugging)
            if (false) { // Set to true for debugging
                Paint textPaint = new Paint();
                textPaint.setColor(Color.WHITE);
                textPaint.setTextSize(24f);
                canvas.drawText(jointName, position.x + 12, position.y - 12, textPaint);
            }
        }
    }

    private void drawJointConnections(Canvas canvas) {
        if (jointPositions == null)
            return;

        // Define joint connections based on exercise type
        // For now, drawing basic hand/wrist connections
        drawConnection(canvas, "WRIST", "THUMB_CMC");
        drawConnection(canvas, "WRIST", "INDEX_FINGER_MCP");
        drawConnection(canvas, "WRIST", "MIDDLE_FINGER_MCP");
        drawConnection(canvas, "WRIST", "RING_FINGER_MCP");
        drawConnection(canvas, "WRIST", "PINKY_MCP");

        // Thumb connections
        drawConnection(canvas, "THUMB_CMC", "THUMB_MCP");
        drawConnection(canvas, "THUMB_MCP", "THUMB_IP");
        drawConnection(canvas, "THUMB_IP", "THUMB_TIP");

        // Index finger connections
        drawConnection(canvas, "INDEX_FINGER_MCP", "INDEX_FINGER_PIP");
        drawConnection(canvas, "INDEX_FINGER_PIP", "INDEX_FINGER_DIP");
        drawConnection(canvas, "INDEX_FINGER_DIP", "INDEX_FINGER_TIP");
    }

    private void drawConnection(Canvas canvas, String joint1, String joint2) {
        if (jointPositions == null)
            return;

        Point p1 = jointPositions.get(joint1);
        Point p2 = jointPositions.get(joint2);

        if (p1 != null && p2 != null) {
            canvas.drawLine(p1.x, p1.y, p2.x, p2.y, connectionPaint);
        }
    }

    private void drawAccuracyIndicator(Canvas canvas) {
        // Draw accuracy circle in top-right corner
        float centerX = getWidth() - 60f;
        float centerY = 60f;
        float radius = 40f;

        // Background circle
        accuracyPaint.setColor(Color.GRAY);
        canvas.drawCircle(centerX, centerY, radius, accuracyPaint);

        // Accuracy arc
        int accuracyColor = getAccuracyColor(currentAccuracy);
        accuracyPaint.setColor(accuracyColor);

        float sweepAngle = currentAccuracy * 360f;
        canvas.drawArc(centerX - radius, centerY - radius,
                centerX + radius, centerY + radius,
                -90f, sweepAngle, false, accuracyPaint);

        // Accuracy text
        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(16f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        String accuracyText = String.format("%.0f%%", currentAccuracy * 100);
        canvas.drawText(accuracyText, centerX, centerY + 6f, textPaint);
    }

    private int getJointColor(String jointName) {
        // Color joints based on accuracy or type
        if (currentAccuracy > 0.8f) {
            return Color.GREEN;
        } else if (currentAccuracy > 0.6f) {
            return Color.YELLOW;
        } else {
            return Color.RED;
        }
    }

    private int getAccuracyColor(float accuracy) {
        if (accuracy > 0.8f) {
            return Color.GREEN;
        } else if (accuracy > 0.6f) {
            return Color.YELLOW;
        } else if (accuracy > 0.3f) {
            return 0xFFFF8C00; // Orange color
        } else {
            return Color.RED;
        }
    }

    /**
     * Update joint positions from motion tracking
     */
    public void updateJointPositions(Map<String, Point> positions) {
        this.jointPositions = positions;
        invalidate(); // Trigger redraw
    }

    // Compatibility method
    public void updateJoints(Map<String, Point> positions) {
        updateJointPositions(positions);
    }

    /**
     * Update current form accuracy
     */
    public void updateAccuracy(float accuracy) {
        this.currentAccuracy = Math.max(0f, Math.min(1f, accuracy));
        invalidate(); // Trigger redraw
    }

    /**
     * Show or hide joint visualization
     */
    public void setShowJoints(boolean show) {
        this.showJoints = show;
        invalidate();
    }

    /**
     * Clear all overlays
     */
    public void clearOverlay() {
        this.jointPositions = null;
        this.currentAccuracy = 0f;
        invalidate();
    }
}