package com.example.myrajourney.rehab.motion;

import android.graphics.Point;
import com.example.myrajourney.rehab.models.FormFeedback;
import java.util.Map;

/**
 * Generates real-time feedback for exercise form corrections
 */
public class FeedbackGenerator {
    
    private long lastFeedbackTime = 0;
    private String lastFeedbackMessage = "";
    private static final long FEEDBACK_COOLDOWN = 2000; // 2 seconds between similar feedback
    
    // Feedback message categories
    private static final String[] EXCELLENT_MESSAGES = {
        "Excellent form! Keep it up!",
        "Perfect technique!",
        "Outstanding movement!",
        "Great job maintaining form!"
    };
    
    private static final String[] GOOD_MESSAGES = {
        "Good form! Stay consistent.",
        "Nice movement! Keep going.",
        "Well done! Maintain this position.",
        "Good technique! Focus on control."
    };
    
    private static final String[] IMPROVEMENT_MESSAGES = {
        "Form needs improvement. Slow down.",
        "Focus on proper positioning.",
        "Adjust your technique slightly.",
        "Pay attention to form details."
    };
    
    private static final String[] CORRECTION_MESSAGES = {
        "Poor form detected. Review instructions.",
        "Stop and check your positioning.",
        "Form is incorrect. Please adjust.",
        "Take a moment to reset your form."
    };
    
    /**
     * Generate feedback based on validation result
     */
    public FormFeedback generateFeedback(FormValidator.ValidationResult validation, 
                                       Map<String, Point> jointPositions) {
        
        if (validation == null) {
            return createErrorFeedback();
        }
        
        float accuracy = validation.getAccuracy();
        boolean isCorrectForm = validation.isCorrectForm();
        String specificFeedback = validation.getFeedback();
        
        // Generate enhanced feedback message
        String enhancedMessage = enhanceFeedbackMessage(accuracy, specificFeedback);
        
        // Apply feedback cooldown to prevent spam
        if (shouldThrottleFeedback(enhancedMessage)) {
            enhancedMessage = null; // Don't show repetitive feedback
        }
        
        return new FormFeedback(
            isCorrectForm,
            accuracy,
            enhancedMessage,
            jointPositions
        );
    }
    
    /**
     * Enhance feedback message based on accuracy level
     */
    private String enhanceFeedbackMessage(float accuracy, String specificFeedback) {
        String enhancedMessage;
        
        if (accuracy > 0.9f) {
            enhancedMessage = getRandomMessage(EXCELLENT_MESSAGES);
        } else if (accuracy > 0.75f) {
            enhancedMessage = getRandomMessage(GOOD_MESSAGES);
        } else if (accuracy > 0.5f) {
            enhancedMessage = specificFeedback != null ? specificFeedback : getRandomMessage(IMPROVEMENT_MESSAGES);
        } else {
            enhancedMessage = specificFeedback != null ? specificFeedback : getRandomMessage(CORRECTION_MESSAGES);
        }
        
        // Add accuracy percentage for context
        if (accuracy > 0.6f) {
            enhancedMessage += String.format(" (%.0f%% accuracy)", accuracy * 100);
        }
        
        return enhancedMessage;
    }
    
    /**
     * Check if feedback should be throttled to prevent spam
     */
    private boolean shouldThrottleFeedback(String message) {
        long currentTime = System.currentTimeMillis();
        
        // Don't throttle if enough time has passed
        if (currentTime - lastFeedbackTime > FEEDBACK_COOLDOWN) {
            lastFeedbackTime = currentTime;
            lastFeedbackMessage = message;
            return false;
        }
        
        // Throttle if message is similar to last one
        if (message != null && message.equals(lastFeedbackMessage)) {
            return true;
        }
        
        // Allow different messages even within cooldown period
        lastFeedbackTime = currentTime;
        lastFeedbackMessage = message;
        return false;
    }
    
    /**
     * Get random message from array
     */
    private String getRandomMessage(String[] messages) {
        int index = (int) (Math.random() * messages.length);
        return messages[index];
    }
    
    /**
     * Generate exercise-specific feedback based on joint positions
     */
    public FormFeedback generateExerciseSpecificFeedback(String exerciseId, 
                                                       Map<String, Point> jointPositions,
                                                       float accuracy) {
        
        String specificMessage = generateExerciseSpecificMessage(exerciseId, jointPositions, accuracy);
        boolean isCorrectForm = accuracy > 0.6f;
        
        return new FormFeedback(
            isCorrectForm,
            accuracy,
            specificMessage,
            jointPositions
        );
    }
    
    /**
     * Generate exercise-specific feedback messages
     */
    private String generateExerciseSpecificMessage(String exerciseId, 
                                                 Map<String, Point> jointPositions, 
                                                 float accuracy) {
        
        if (exerciseId == null) {
            return "Continue the exercise movement";
        }
        
        switch (exerciseId) {
            case "ex_001": // Wrist Flexion/Extension
                return generateWristFlexionFeedback(jointPositions, accuracy);
            case "ex_002": // Wrist Rotation
                return generateWristRotationFeedback(jointPositions, accuracy);
            case "ex_003": // Thumb Opposition
                return generateThumbOppositionFeedback(jointPositions, accuracy);
            case "ex_004": // Thumb Flexion/Extension
                return generateThumbFlexionFeedback(jointPositions, accuracy);
            case "ex_005": // Finger Flexion
                return generateFingerFlexionFeedback(jointPositions, accuracy);
            case "ex_006": // Finger Extension
                return generateFingerExtensionFeedback(jointPositions, accuracy);
            case "ex_007": // Finger Pinch
                return generateFingerPinchFeedback(jointPositions, accuracy);
            case "ex_008": // Knee Flexion/Extension
                return generateKneeFlexionFeedback(jointPositions, accuracy);
            case "ex_009": // Hip Flexion
                return generateHipFlexionFeedback(jointPositions, accuracy);
            case "ex_010": // Hip Abduction
                return generateHipAbductionFeedback(jointPositions, accuracy);
            default:
                return "Keep moving with control";
        }
    }
    
    private String generateWristFlexionFeedback(Map<String, Point> joints, float accuracy) {
        if (accuracy > 0.8f) {
            return "Excellent wrist movement! Keep the motion smooth.";
        } else if (accuracy > 0.6f) {
            return "Good wrist flexion. Try to increase the range of motion.";
        } else {
            return "Focus on bending and straightening your wrist fully.";
        }
    }
    
    private String generateWristRotationFeedback(Map<String, Point> joints, float accuracy) {
        if (accuracy > 0.8f) {
            return "Perfect wrist rotation! Maintain the circular motion.";
        } else if (accuracy > 0.6f) {
            return "Good rotation. Make the movement more circular.";
        } else {
            return "Rotate your wrist in a smooth, complete circle.";
        }
    }
    
    private String generateThumbOppositionFeedback(Map<String, Point> joints, float accuracy) {
        if (accuracy > 0.8f) {
            return "Great thumb opposition! Touch each fingertip clearly.";
        } else if (accuracy > 0.6f) {
            return "Good thumb movement. Press fingertips together more firmly.";
        } else {
            return "Touch your thumb to each fingertip one by one.";
        }
    }
    
    private String generateThumbFlexionFeedback(Map<String, Point> joints, float accuracy) {
        if (accuracy > 0.8f) {
            return "Excellent thumb flexion! Full range of motion.";
        } else if (accuracy > 0.6f) {
            return "Good thumb movement. Bend and straighten completely.";
        } else {
            return "Make a full fist with your thumb, then extend fully.";
        }
    }
    
    private String generateFingerFlexionFeedback(Map<String, Point> joints, float accuracy) {
        if (accuracy > 0.8f) {
            return "Perfect fist formation! All fingers closed tightly.";
        } else if (accuracy > 0.6f) {
            return "Good fist. Close your fingers tighter together.";
        } else {
            return "Make a tight fist with all fingers curled in.";
        }
    }
    
    private String generateFingerExtensionFeedback(Map<String, Point> joints, float accuracy) {
        if (accuracy > 0.8f) {
            return "Excellent finger extension! Fingers spread wide.";
        } else if (accuracy > 0.6f) {
            return "Good extension. Spread your fingers wider apart.";
        } else {
            return "Straighten and spread all fingers as wide as possible.";
        }
    }
    
    private String generateFingerPinchFeedback(Map<String, Point> joints, float accuracy) {
        if (accuracy > 0.8f) {
            return "Strong pinch! Good thumb and finger coordination.";
        } else if (accuracy > 0.6f) {
            return "Good pinch. Press thumb and finger together more firmly.";
        } else {
            return "Pinch your thumb and index finger tips together tightly.";
        }
    }
    
    private String generateKneeFlexionFeedback(Map<String, Point> joints, float accuracy) {
        if (accuracy > 0.8f) {
            return "Excellent knee movement! Full flexion and extension.";
        } else if (accuracy > 0.6f) {
            return "Good knee exercise. Bend and straighten more completely.";
        } else {
            return "Bend your knee fully, then straighten it completely.";
        }
    }
    
    private String generateHipFlexionFeedback(Map<String, Point> joints, float accuracy) {
        if (accuracy > 0.8f) {
            return "Perfect hip flexion! Knee lifted high toward chest.";
        } else if (accuracy > 0.6f) {
            return "Good hip movement. Lift your knee higher.";
        } else {
            return "Lift your knee up toward your chest as high as comfortable.";
        }
    }
    
    private String generateHipAbductionFeedback(Map<String, Point> joints, float accuracy) {
        if (accuracy > 0.8f) {
            return "Excellent hip abduction! Leg moved well to the side.";
        } else if (accuracy > 0.6f) {
            return "Good side movement. Move your leg further out.";
        } else {
            return "Move your leg out to the side as far as comfortable.";
        }
    }
    
    /**
     * Generate motivational feedback based on session progress
     */
    public FormFeedback generateMotivationalFeedback(int correctFrames, int totalFrames) {
        if (totalFrames == 0) {
            return new FormFeedback(true, 1.0f, "Let's begin the exercise!", null);
        }
        
        float sessionAccuracy = (float) correctFrames / totalFrames;
        String motivationalMessage;
        
        if (sessionAccuracy > 0.9f) {
            motivationalMessage = "Outstanding session! You're doing amazingly well!";
        } else if (sessionAccuracy > 0.8f) {
            motivationalMessage = "Great progress! Keep up the excellent work!";
        } else if (sessionAccuracy > 0.7f) {
            motivationalMessage = "Good job! You're improving with each movement!";
        } else if (sessionAccuracy > 0.5f) {
            motivationalMessage = "Keep going! Focus on form and you'll improve!";
        } else {
            motivationalMessage = "Take your time. Quality over speed!";
        }
        
        return new FormFeedback(true, sessionAccuracy, motivationalMessage, null);
    }
    
    /**
     * Create error feedback when analysis fails
     */
    private FormFeedback createErrorFeedback() {
        return new FormFeedback(
            false,
            0.0f,
            "Unable to analyze movement. Check camera position and lighting.",
            null
        );
    }
    
    /**
     * Generate completion feedback for finished exercises
     */
    public FormFeedback generateCompletionFeedback(float finalAccuracy, long sessionDuration) {
        String completionMessage;
        
        if (finalAccuracy > 0.9f) {
            completionMessage = "Excellent session completed! Perfect form throughout!";
        } else if (finalAccuracy > 0.8f) {
            completionMessage = "Great session! You maintained good form consistently.";
        } else if (finalAccuracy > 0.7f) {
            completionMessage = "Good session completed! Keep practicing to improve.";
        } else if (finalAccuracy > 0.5f) {
            completionMessage = "Session completed! Focus on form in your next session.";
        } else {
            completionMessage = "Session completed! Remember to move slowly and focus on technique.";
        }
        
        long minutes = sessionDuration / 60000;
        long seconds = (sessionDuration % 60000) / 1000;
        completionMessage += String.format(" Duration: %d:%02d", minutes, seconds);
        
        return new FormFeedback(true, finalAccuracy, completionMessage, null);
    }
}