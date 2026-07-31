package com.example.myrajourney.exercise.tracking;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.example.myrajourney.exercise.models.AnalysisResult;
import com.example.myrajourney.exercise.models.ExerciseTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Generates real-time feedback for exercise tracking using fuzzy logic
 */
public class FeedbackGenerator {
    private static final String TAG = "FeedbackGenerator";
    
    private Context context;
    private TextToSpeech textToSpeech;
    private boolean ttsInitialized = false;
    private long lastAudioFeedbackTime = 0;
    private static final long MIN_AUDIO_FEEDBACK_INTERVAL = 3000; // 3 seconds between audio cues
    
    // Feedback history to avoid repetition
    private List<String> recentFeedback = new ArrayList<>();
    private static final int MAX_RECENT_FEEDBACK = 5;
    
    public FeedbackGenerator(Context context) {
        this.context = context;
        initializeTextToSpeech();
    }
    
    /**
     * Initialize Text-to-Speech engine
     */
    private void initializeTextToSpeech() {
        textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e(TAG, "TTS language not supported");
                    } else {
                        ttsInitialized = true;
                        textToSpeech.setSpeechRate(0.9f); // Slightly slower for clarity
                        Log.d(TAG, "TTS initialized successfully");
                    }
                } else {
                    Log.e(TAG, "TTS initialization failed");
                }
            }
        });
    }
    
    /**
     * Generate comprehensive feedback using fuzzy logic inference
     */
    public void generateFeedback(AnalysisResult result, ExerciseTemplate exerciseTemplate) {
        if (result == null) return;
        
        // Generate visual feedback
        generateVisualFeedback(result);
        
        // Generate audio feedback (with throttling)
        generateAudioFeedback(result, exerciseTemplate);
        
        // Generate text instructions
        generateTextInstructions(result, exerciseTemplate);
        
        // Generate joint-specific feedback
        generateJointSpecificFeedback(result);
    }
    
    /**
     * Generate visual feedback indicators using fuzzy logic
     */
    private void generateVisualFeedback(AnalysisResult result) {
        AnalysisResult.FeedbackData feedback = result.getFeedback();
        double overallScore = result.getOverallScore();
        
        // Fuzzy logic for visual indicators
        if (overallScore >= 90) {
            feedback.setVisualIndicatorColor("#4CAF50"); // Bright Green - Excellent
            feedback.setLevel(AnalysisResult.FeedbackData.FeedbackLevel.EXCELLENT);
        } else if (overallScore >= 80) {
            feedback.setVisualIndicatorColor("#8BC34A"); // Light Green - Good
            feedback.setLevel(AnalysisResult.FeedbackData.FeedbackLevel.GOOD);
        } else if (overallScore >= 65) {
            feedback.setVisualIndicatorColor("#FFC107"); // Amber - Needs Improvement
            feedback.setLevel(AnalysisResult.FeedbackData.FeedbackLevel.NEEDS_IMPROVEMENT);
        } else if (overallScore >= 50) {
            feedback.setVisualIndicatorColor("#FF9800"); // Orange - Poor
            feedback.setLevel(AnalysisResult.FeedbackData.FeedbackLevel.NEEDS_IMPROVEMENT);
        } else {
            feedback.setVisualIndicatorColor("#F44336"); // Red - Incorrect
            feedback.setLevel(AnalysisResult.FeedbackData.FeedbackLevel.INCORRECT);
        }
        
        feedback.setPrimaryMessage(feedback.getLevel().getMessage());
    }
    
    /**
     * Generate audio feedback with intelligent prioritization
     */
    private void generateAudioFeedback(AnalysisResult result, ExerciseTemplate exerciseTemplate) {
        long currentTime = System.currentTimeMillis();
        
        // Throttle audio feedback to avoid overwhelming user
        if (currentTime - lastAudioFeedbackTime < MIN_AUDIO_FEEDBACK_INTERVAL) {
            return;
        }
        
        List<String> audioInstructions = new ArrayList<>();
        double overallScore = result.getOverallScore();
        
        // Priority-based feedback generation
        if (overallScore >= 85) {
            // Positive reinforcement
            String[] positiveMessages = {
                "Great form!", "Excellent!", "Keep it up!", "Perfect execution!"
            };
            audioInstructions.add(getRandomMessage(positiveMessages));
            
        } else if (overallScore >= 70) {
            // Minor corrections
            generateMinorCorrectionFeedback(result, audioInstructions, exerciseTemplate);
            
        } else {
            // Major corrections needed
            generateMajorCorrectionFeedback(result, audioInstructions, exerciseTemplate);
        }
        
        // Limit to 2 audio instructions to avoid overwhelming
        if (audioInstructions.size() > 2) {
            audioInstructions = audioInstructions.subList(0, 2);
        }
        
        // Set audio instructions in result
        result.getFeedback().setAudioInstructions(audioInstructions.toArray(new String[0]));
        
        // Speak the first instruction
        if (!audioInstructions.isEmpty() && !isRecentFeedback(audioInstructions.get(0))) {
            speakFeedback(audioInstructions.get(0));
            addToRecentFeedback(audioInstructions.get(0));
            lastAudioFeedbackTime = currentTime;
        }
    }
    
    /**
     * Generate minor correction feedback
     */
    private void generateMinorCorrectionFeedback(AnalysisResult result, List<String> audioInstructions, ExerciseTemplate exerciseTemplate) {
        Map<String, AnalysisResult.JointAnalysis> jointDetails = result.getJointDetails();
        
        // Find the joint with the lowest accuracy for targeted feedback
        String worstJoint = null;
        double lowestAccuracy = 100.0;
        
        for (Map.Entry<String, AnalysisResult.JointAnalysis> entry : jointDetails.entrySet()) {
            if (entry.getValue().getAccuracyScore() < lowestAccuracy) {
                lowestAccuracy = entry.getValue().getAccuracyScore();
                worstJoint = entry.getKey();
            }
        }
        
        if (worstJoint != null && lowestAccuracy < 75) {
            String correction = generateJointCorrection(worstJoint, jointDetails.get(worstJoint));
            if (correction != null) {
                audioInstructions.add(correction);
            }
        }
        
        // Movement quality feedback
        AnalysisResult.MovementQualityMetrics quality = result.getQualityMetrics();
        if (quality.getStability() < 70) {
            audioInstructions.add("Move more smoothly");
        } else if (quality.getRangeOfMotion() < 70) {
            audioInstructions.add("Complete the full range of motion");
        }
    }
    
    /**
     * Generate major correction feedback
     */
    private void generateMajorCorrectionFeedback(AnalysisResult result, List<String> audioInstructions, ExerciseTemplate exerciseTemplate) {
        // Focus on the most critical issues
        if (result.getPoseSimilarity() < 50) {
            audioInstructions.add("Check your posture and positioning");
        }
        
        if (result.getJointAccuracy() < 50) {
            audioInstructions.add("Focus on proper joint alignment");
        }
        
        // Exercise-specific guidance
        if (exerciseTemplate != null) {
            String exerciseSpecificFeedback = getExerciseSpecificFeedback(exerciseTemplate.getExerciseType(), result);
            if (exerciseSpecificFeedback != null) {
                audioInstructions.add(exerciseSpecificFeedback);
            }
        }
    }
    
    /**
     * Generate joint-specific correction instructions
     */
    private String generateJointCorrection(String jointName, AnalysisResult.JointAnalysis jointAnalysis) {
        double userAngle = jointAnalysis.getUserAngle();
        double refAngle = jointAnalysis.getReferenceAngle();
        double difference = userAngle - refAngle;
        
        String side = jointName.contains("LEFT") ? "left" : "right";
        
        switch (jointName.replace("LEFT_", "").replace("RIGHT_", "")) {
            case "ELBOW":
                if (difference > 15) {
                    return "Straighten your " + side + " elbow slightly";
                } else if (difference < -15) {
                    return "Bend your " + side + " elbow more";
                }
                break;
                
            case "SHOULDER":
                if (difference > 15) {
                    return "Lower your " + side + " shoulder";
                } else if (difference < -15) {
                    return "Raise your " + side + " shoulder";
                }
                break;
                
            case "KNEE":
                if (difference > 15) {
                    return "Straighten your " + side + " knee";
                } else if (difference < -15) {
                    return "Bend your " + side + " knee more";
                }
                break;
        }
        
        return null;
    }
    
    /**
     * Generate exercise-specific feedback
     */
    private String getExerciseSpecificFeedback(ExerciseTemplate.ExerciseType exerciseType, AnalysisResult result) {
        if (exerciseType == null) return null;
        
        switch (exerciseType) {
            case FINGER_FLEXION:
                return "Focus on smooth finger movements";
                
            case SHOULDER_ROLL:
                if (result.getQualityMetrics().getSymmetry() < 60) {
                    return "Keep both shoulders moving together";
                }
                return "Make complete circular motions";
                
            case ANKLE_CIRCLE:
                return "Draw full circles with your ankles";
                
            case NECK_STRETCH:
                return "Move slowly and hold the stretch";
                
            case WRIST_ROTATION:
                return "Rotate your wrists in complete circles";
                
            case ARM_RAISE:
                return "Keep your arms straight and controlled";
                
            default:
                return "Follow the reference movement";
        }
    }
    
    /**
     * Generate text instructions for display
     */
    private void generateTextInstructions(AnalysisResult result, ExerciseTemplate exerciseTemplate) {
        List<String> textInstructions = new ArrayList<>();
        
        // Add detailed written guidance
        if (result.getOverallScore() < 70) {
            textInstructions.add("Watch the reference video and match the movements");
            
            if (result.getPoseSimilarity() < 60) {
                textInstructions.add("Focus on body positioning and posture");
            }
            
            if (result.getJointAccuracy() < 60) {
                textInstructions.add("Pay attention to joint angles and alignment");
            }
            
            if (result.getMovementQuality() < 60) {
                textInstructions.add("Move more slowly and smoothly");
            }
        }
        
        result.getFeedback().setTextInstructions(textInstructions.toArray(new String[0]));
    }
    
    /**
     * Generate joint-specific feedback for detailed analysis
     */
    private void generateJointSpecificFeedback(AnalysisResult result) {
        Map<String, String> jointFeedback = new HashMap<>();
        
        for (Map.Entry<String, AnalysisResult.JointAnalysis> entry : result.getJointDetails().entrySet()) {
            String jointName = entry.getKey();
            AnalysisResult.JointAnalysis analysis = entry.getValue();
            
            if (analysis.getAccuracyScore() < 70) {
                String feedback = String.format("%.0f° (target: %.0f°)", 
                    analysis.getUserAngle(), analysis.getReferenceAngle());
                jointFeedback.put(jointName, feedback);
            }
        }
        
        result.getFeedback().setJointSpecificFeedback(jointFeedback);
    }
    
    /**
     * Speak feedback using TTS
     */
    private void speakFeedback(String message) {
        if (ttsInitialized && textToSpeech != null && message != null && !message.isEmpty()) {
            textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null, null);
            Log.d(TAG, "Speaking feedback: " + message);
        }
    }
    
    /**
     * Check if feedback was recently given to avoid repetition
     */
    private boolean isRecentFeedback(String feedback) {
        return recentFeedback.contains(feedback);
    }
    
    /**
     * Add feedback to recent history
     */
    private void addToRecentFeedback(String feedback) {
        recentFeedback.add(feedback);
        if (recentFeedback.size() > MAX_RECENT_FEEDBACK) {
            recentFeedback.remove(0);
        }
    }
    
    /**
     * Get random message from array
     */
    private String getRandomMessage(String[] messages) {
        if (messages.length == 0) return "";
        int index = (int) (Math.random() * messages.length);
        return messages[index];
    }
    
    /**
     * Enable/disable audio feedback
     */
    public void setAudioFeedbackEnabled(boolean enabled) {
        if (!enabled && textToSpeech != null) {
            textToSpeech.stop();
        }
    }
    
    /**
     * Set TTS speech rate
     */
    public void setSpeechRate(float rate) {
        if (ttsInitialized && textToSpeech != null) {
            textToSpeech.setSpeechRate(rate);
        }
    }
    
    /**
     * Clear recent feedback history
     */
    public void clearRecentFeedback() {
        recentFeedback.clear();
    }
    
    /**
     * Clean up resources
     */
    public void cleanup() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
}