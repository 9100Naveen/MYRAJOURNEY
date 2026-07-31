package com.example.myrajourney.ai.logic;

import java.util.Map;
import android.graphics.Point;
import com.example.myrajourney.rehab.models.FormFeedback;

public interface ExerciseLogic {
    /**
     * Analyze the landmarks and return feedback
     * 
     * @param landmarks Map of landmark names to 2D coordinates (pixels)
     * @return FormFeedback object with accuracy and message
     */
    FormFeedback analyze(Map<String, Point> landmarks);
}
