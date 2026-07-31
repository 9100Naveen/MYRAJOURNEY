package com.example.myrajourney.ai.logic;

import android.graphics.Point;
import com.example.myrajourney.rehab.models.FormFeedback;
import java.util.Map;

public class WristFlexionLogic implements ExerciseLogic {

    @Override
    public FormFeedback analyze(Map<String, Point> landmarks) {
        if (!landmarks.containsKey("WRIST") || !landmarks.containsKey("MIDDLE_FINGER_MCP")
                || !landmarks.containsKey("MIDDLE_FINGER_TIP")) {
            return new FormFeedback(false, 0.0f, "Hand not fully visible", null);
        }

        Point wrist = landmarks.get("WRIST");
        Point mcp = landmarks.get("MIDDLE_FINGER_MCP");
        Point tip = landmarks.get("MIDDLE_FINGER_TIP");

        // Calculate vectors
        // Vector 1: Wrist -> MCP (Palm direction)
        double v1x = mcp.x - wrist.x;
        double v1y = mcp.y - wrist.y;

        // Vector 2: MCP -> Tip (Finger direction)
        double v2x = tip.x - mcp.x;
        double v2y = tip.y - mcp.y;

        // Calculate angle between vectors
        double angleRad = Math.atan2(v2y, v2x) - Math.atan2(v1y, v1x);
        double angleDeg = Math.abs(Math.toDegrees(angleRad));
        if (angleDeg > 180)
            angleDeg = 360 - angleDeg; // Normalize to 0-180

        // Interpretation:
        // 0-20 degrees deviation: Straight / Neutral
        // > 45 degrees: Flexion (Curling)

        // This measures FINGER flexion relative to Palm.
        // For WRIST flexion, we'd need forearm.
        // Proxies: "Wrist Flexion" exercise might effectively be tracked by hand/finger
        // movement if arm is fixed.
        // We'll assume the user is performing the motion and return feedback on
        // "Movement".

        // Let's return accuracy based on range of motion?
        // Or just "Good Form" if smooth?
        // Dynamic analysis requires history. Snapshot analysis:

        float accuracy = (float) (1.0 - (angleDeg / 180.0)); // 1.0 = Straight, 0.0 = Folded back

        String feedbackMsg;
        if (angleDeg < 30) {
            feedbackMsg = "Good Extension (Straight)";
        } else {
            feedbackMsg = "Flexing...";
        }

        return new FormFeedback(true, accuracy, feedbackMsg, landmarks);
    }
}
