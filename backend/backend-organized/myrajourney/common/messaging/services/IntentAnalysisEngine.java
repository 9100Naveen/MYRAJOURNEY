package com.example.myrajourney.common.messaging.services;

import android.content.Context;
import android.util.Log;

import com.example.myrajourney.common.messaging.models.Message;
import com.example.myrajourney.common.messaging.models.ConversationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Analyzes user queries to understand intent and extract relevant entities
 */
public class IntentAnalysisEngine {
    private static final String TAG = "IntentAnalysisEngine";
    
    private Context context;
    private Map<String, IntentPattern> intentPatterns;
    
    public IntentAnalysisEngine(Context context) {
        this.context = context;
        initializeIntentPatterns();
    }
    
    /**
     * Analyzes user message to determine intent and extract entities
     */
    public void analyzeIntent(Message message, ConversationContext context) {
        try {
            String userMessage = message.getContent().toLowerCase();
            
            // Find matching intent
            IntentResult result = classifyIntent(userMessage);
            
            // Set intent and confidence
            message.setIntent(result.intent);
            message.setConfidence(result.confidence);
            
            // Extract entities
            Map<String, Object> entities = extractEntities(userMessage, result.intent);
            message.setEntities(entities);
            
            Log.d(TAG, "Intent: " + result.intent + ", Confidence: " + result.confidence);
            
        } catch (Exception e) {
            Log.e(TAG, "Error analyzing intent", e);
            message.setIntent("unknown");
            message.setConfidence(0.0);
        }
    }
    
    /**
     * Classifies the intent of a user message
     */
    private IntentResult classifyIntent(String message) {
        double maxConfidence = 0.0;
        String bestIntent = "general_inquiry";
        
        for (Map.Entry<String, IntentPattern> entry : intentPatterns.entrySet()) {
            double confidence = calculateConfidence(message, entry.getValue());
            if (confidence > maxConfidence) {
                maxConfidence = confidence;
                bestIntent = entry.getKey();
            }
        }
        
        return new IntentResult(bestIntent, maxConfidence);
    }
    
    /**
     * Calculates confidence score for an intent pattern
     */
    private double calculateConfidence(String message, IntentPattern pattern) {
        int matches = 0;
        int totalKeywords = pattern.keywords.length;
        
        for (String keyword : pattern.keywords) {
            if (message.contains(keyword.toLowerCase())) {
                matches++;
            }
        }
        
        // Check regex patterns if available
        if (pattern.regexPattern != null) {
            Matcher matcher = pattern.regexPattern.matcher(message);
            if (matcher.find()) {
                matches += 2; // Regex matches get higher weight
            }
        }
        
        return Math.min(1.0, (double) matches / totalKeywords);
    }
    
    /**
     * Extracts entities from user message based on intent
     */
    private Map<String, Object> extractEntities(String message, String intent) {
        Map<String, Object> entities = new HashMap<>();
        
        switch (intent) {
            case "appointment_inquiry":
                extractAppointmentEntities(message, entities);
                break;
            case "medication_inquiry":
                extractMedicationEntities(message, entities);
                break;
            case "symptom_report":
                extractSymptomEntities(message, entities);
                break;
            case "emergency":
                extractEmergencyEntities(message, entities);
                break;
        }
        
        return entities;
    }
    
    private void extractAppointmentEntities(String message, Map<String, Object> entities) {
        // Extract date/time references
        if (message.contains("today")) entities.put("timeframe", "today");
        if (message.contains("tomorrow")) entities.put("timeframe", "tomorrow");
        if (message.contains("next week")) entities.put("timeframe", "next_week");
        if (message.contains("schedule") || message.contains("book")) entities.put("action", "schedule");
        if (message.contains("cancel")) entities.put("action", "cancel");
        if (message.contains("reschedule")) entities.put("action", "reschedule");
    }
    
    private void extractMedicationEntities(String message, Map<String, Object> entities) {
        // Extract medication-related entities
        if (message.contains("missed") || message.contains("forgot")) entities.put("issue", "missed_dose");
        if (message.contains("side effect")) entities.put("issue", "side_effects");
        if (message.contains("dosage")) entities.put("concern", "dosage");
        if (message.contains("when") && message.contains("take")) entities.put("question_type", "timing");
    }
    
    private void extractSymptomEntities(String message, Map<String, Object> entities) {
        // Extract symptom-related entities
        String[] painKeywords = {"pain", "ache", "hurt", "sore", "tender"};
        String[] severityKeywords = {"mild", "moderate", "severe", "intense", "unbearable"};
        
        for (String keyword : painKeywords) {
            if (message.contains(keyword)) {
                entities.put("symptom_type", "pain");
                break;
            }
        }
        
        for (String keyword : severityKeywords) {
            if (message.contains(keyword)) {
                entities.put("severity", keyword);
                break;
            }
        }
    }
    
    private void extractEmergencyEntities(String message, Map<String, Object> entities) {
        entities.put("urgency", "high");
        if (message.contains("chest pain")) entities.put("emergency_type", "cardiac");
        if (message.contains("can't breathe") || message.contains("difficulty breathing")) {
            entities.put("emergency_type", "respiratory");
        }
        if (message.contains("severe bleeding")) entities.put("emergency_type", "bleeding");
    }
    
    /**
     * Initialize intent patterns for classification
     */
    private void initializeIntentPatterns() {
        intentPatterns = new HashMap<>();
        
        // Appointment-related intents
        intentPatterns.put("appointment_inquiry", new IntentPattern(
            new String[]{"appointment", "schedule", "book", "visit", "consultation", "doctor", "when is my"},
            Pattern.compile("(when|what time).*(appointment|visit|consultation)")
        ));
        
        // Medication-related intents
        intentPatterns.put("medication_inquiry", new IntentPattern(
            new String[]{"medication", "medicine", "pill", "dose", "dosage", "take", "missed", "forgot"},
            Pattern.compile("(when|how).*(take|medication|medicine|pill)")
        ));
        
        // Symptom reporting
        intentPatterns.put("symptom_report", new IntentPattern(
            new String[]{"pain", "hurt", "ache", "feel", "symptom", "sore", "swollen", "stiff"},
            Pattern.compile("(i feel|i have|experiencing).*(pain|ache|symptom)")
        ));
        
        // Emergency situations
        intentPatterns.put("emergency", new IntentPattern(
            new String[]{"emergency", "urgent", "help", "chest pain", "can't breathe", "severe", "bleeding"},
            Pattern.compile("(emergency|urgent|help|chest pain|can't breathe)")
        ));
        
        // Navigation requests
        intentPatterns.put("navigation_request", new IntentPattern(
            new String[]{"go to", "show me", "navigate", "open", "view", "see my"},
            Pattern.compile("(go to|show me|navigate to|open|view).*(page|section|dashboard)")
        ));
        
        // General health inquiry
        intentPatterns.put("health_inquiry", new IntentPattern(
            new String[]{"how", "what", "why", "explain", "tell me", "information", "about"},
            Pattern.compile("(how|what|why|tell me about).*(ra|arthritis|health|condition)")
        ));
        
        // General greeting/conversation
        intentPatterns.put("greeting", new IntentPattern(
            new String[]{"hello", "hi", "hey", "good morning", "good afternoon", "thanks", "thank you"},
            null
        ));
    }
    
    /**
     * Intent pattern class
     */
    private static class IntentPattern {
        String[] keywords;
        Pattern regexPattern;
        
        IntentPattern(String[] keywords, Pattern regexPattern) {
            this.keywords = keywords;
            this.regexPattern = regexPattern;
        }
    }
    
    /**
     * Intent classification result
     */
    private static class IntentResult {
        String intent;
        double confidence;
        
        IntentResult(String intent, double confidence) {
            this.intent = intent;
            this.confidence = confidence;
        }
    }
}