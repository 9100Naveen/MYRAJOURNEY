package com.example.myrajourney.common.messaging;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Analyzes user messages to determine intent for intelligent responses
 */
public class IntentAnalyzer {
    
    // Pain-related keywords
    private static final List<String> PAIN_KEYWORDS = Arrays.asList(
        "pain", "hurt", "ache", "sore", "painful", "aching", "throbbing", 
        "sharp pain", "dull pain", "burning", "stiff", "stiffness", "tender"
    );
    
    // Medication keywords
    private static final List<String> MEDICATION_KEYWORDS = Arrays.asList(
        "medication", "medicine", "pill", "drug", "dose", "dosage", "side effect",
        "methotrexate", "humira", "prednisone", "nsaid", "ibuprofen", "naproxen",
        "missed dose", "forgot", "when to take", "timing", "prescription"
    );
    
    // Exercise keywords
    private static final List<String> EXERCISE_KEYWORDS = Arrays.asList(
        "exercise", "workout", "stretch", "stretching", "movement", "activity",
        "physical therapy", "rehabilitation", "yoga", "walking", "swimming",
        "strength training", "range of motion", "flexibility"
    );
    
    // Symptom tracking keywords
    private static final List<String> SYMPTOM_KEYWORDS = Arrays.asList(
        "symptom", "track", "log", "record", "monitor", "diary", "journal",
        "flare", "flare up", "inflammation", "swelling", "fatigue", "tired"
    );
    
    // Appointment keywords
    private static final List<String> APPOINTMENT_KEYWORDS = Arrays.asList(
        "appointment", "doctor", "visit", "checkup", "rheumatologist", 
        "physician", "clinic", "hospital", "schedule", "next visit"
    );
    
    // Diet/nutrition keywords
    private static final List<String> DIET_KEYWORDS = Arrays.asList(
        "diet", "food", "nutrition", "eat", "eating", "meal", "anti-inflammatory",
        "omega-3", "fish", "vegetables", "fruits", "supplements", "vitamin"
    );
    
    // Emotional support keywords
    private static final List<String> EMOTIONAL_KEYWORDS = Arrays.asList(
        "sad", "depressed", "anxious", "worried", "scared", "frustrated",
        "angry", "overwhelmed", "stressed", "emotional", "mental health",
        "support", "help", "cope", "coping", "feel", "feeling"
    );
    
    // Emergency keywords
    private static final List<String> EMERGENCY_KEYWORDS = Arrays.asList(
        "emergency", "urgent", "severe", "unbearable", "can't move", "fever",
        "high fever", "chest pain", "breathing", "vision", "emergency room",
        "911", "help", "crisis", "serious"
    );
    
    // General RA info keywords
    private static final List<String> RA_INFO_KEYWORDS = Arrays.asList(
        "what is", "explain", "rheumatoid arthritis", "ra", "autoimmune",
        "cause", "causes", "prognosis", "future", "progression", "joints"
    );
    
    public Intent analyzeIntent(String message) {
        if (message == null || message.trim().isEmpty()) {
            return new Intent(IntentType.GENERAL_CONVERSATION, 0.5f);
        }
        
        String lowerMessage = message.toLowerCase().trim();
        
        // Check for emergency first (highest priority)
        float emergencyScore = calculateKeywordScore(lowerMessage, EMERGENCY_KEYWORDS);
        if (emergencyScore > 0.3f) {
            return new Intent(IntentType.EMERGENCY_SITUATION, emergencyScore);
        }
        
        // Check other intents
        float painScore = calculateKeywordScore(lowerMessage, PAIN_KEYWORDS);
        float medicationScore = calculateKeywordScore(lowerMessage, MEDICATION_KEYWORDS);
        float exerciseScore = calculateKeywordScore(lowerMessage, EXERCISE_KEYWORDS);
        float symptomScore = calculateKeywordScore(lowerMessage, SYMPTOM_KEYWORDS);
        float appointmentScore = calculateKeywordScore(lowerMessage, APPOINTMENT_KEYWORDS);
        float dietScore = calculateKeywordScore(lowerMessage, DIET_KEYWORDS);
        float emotionalScore = calculateKeywordScore(lowerMessage, EMOTIONAL_KEYWORDS);
        float raInfoScore = calculateKeywordScore(lowerMessage, RA_INFO_KEYWORDS);
        
        // Find the highest scoring intent
        float maxScore = Math.max(painScore, Math.max(medicationScore, 
                        Math.max(exerciseScore, Math.max(symptomScore,
                        Math.max(appointmentScore, Math.max(dietScore,
                        Math.max(emotionalScore, raInfoScore)))))));
        
        if (maxScore < 0.2f) {
            return new Intent(IntentType.GENERAL_CONVERSATION, 0.5f);
        }
        
        if (maxScore == painScore) {
            return new Intent(IntentType.PAIN_MANAGEMENT, painScore);
        } else if (maxScore == medicationScore) {
            return new Intent(IntentType.MEDICATION_QUERY, medicationScore);
        } else if (maxScore == exerciseScore) {
            return new Intent(IntentType.EXERCISE_GUIDANCE, exerciseScore);
        } else if (maxScore == symptomScore) {
            return new Intent(IntentType.SYMPTOM_TRACKING, symptomScore);
        } else if (maxScore == appointmentScore) {
            return new Intent(IntentType.APPOINTMENT_QUERY, appointmentScore);
        } else if (maxScore == dietScore) {
            return new Intent(IntentType.NUTRITION_QUERY, dietScore);
        } else if (maxScore == emotionalScore) {
            return new Intent(IntentType.EMOTIONAL_SUPPORT, emotionalScore);
        } else if (maxScore == raInfoScore) {
            return new Intent(IntentType.GENERAL_RA_INFO, raInfoScore);
        }
        
        return new Intent(IntentType.GENERAL_CONVERSATION, 0.5f);
    }
    
    private float calculateKeywordScore(String message, List<String> keywords) {
        int matches = 0;
        int totalWords = message.split("\\s+").length;
        
        for (String keyword : keywords) {
            if (message.contains(keyword)) {
                matches++;
                // Give extra weight to exact matches
                if (message.equals(keyword)) {
                    matches += 2;
                }
            }
        }
        
        // Calculate score based on matches and message length
        return Math.min(1.0f, (float) matches / Math.max(1, totalWords / 3));
    }
}