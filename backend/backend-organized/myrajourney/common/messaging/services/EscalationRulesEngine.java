package com.example.myrajourney.common.messaging.services;

import android.content.Context;
import android.util.Log;

import com.example.myrajourney.common.messaging.models.ConversationSession;
import com.example.myrajourney.common.messaging.models.Message;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Determines when to escalate conversations to healthcare professionals
 */
public class EscalationRulesEngine {
    private static final String TAG = "EscalationRulesEngine";
    
    private Context context;
    private List<String> emergencyKeywords;
    private List<String> urgentSymptoms;
    private List<String> medicationEmergencyKeywords;
    
    public EscalationRulesEngine(Context context) {
        this.context = context;
        initializeEscalationRules();
    }
    
    /**
     * Determines if a conversation should be escalated
     */
    public boolean shouldEscalate(Message userMessage, ConversationSession session) {
        try {
            String intent = userMessage.getIntent();
            String content = userMessage.getContent().toLowerCase();
            Map<String, Object> entities = userMessage.getEntities();
            Double confidence = userMessage.getConfidence();
            
            // Check for emergency situations
            if (isEmergencyScenario(intent, content, entities)) {
                Log.d(TAG, "Emergency escalation triggered");
                return true;
            }
            
            // Check for urgent medical symptoms
            if (hasUrgentSymptoms(content)) {
                Log.d(TAG, "Urgent symptoms escalation triggered");
                return true;
            }
            
            // Check for medication emergencies
            if (isMedicationEmergency(content)) {
                Log.d(TAG, "Medication emergency escalation triggered");
                return true;
            }
            
            // Check for low confidence responses
            if (isLowConfidenceScenario(confidence, intent)) {
                Log.d(TAG, "Low confidence escalation triggered");
                return true;
            }
            
            // Check for explicit user request for human help
            if (isUserRequestingHuman(content)) {
                Log.d(TAG, "User requested human assistance");
                return true;
            }
            
            // Check for complex medical decisions
            if (isComplexMedicalDecision(content, entities)) {
                Log.d(TAG, "Complex medical decision escalation triggered");
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            Log.e(TAG, "Error evaluating escalation rules", e);
            // When in doubt, escalate for safety
            return true;
        }
    }
    
    /**
     * Checks for emergency scenarios that require immediate attention
     */
    private boolean isEmergencyScenario(String intent, String content, Map<String, Object> entities) {
        // Direct emergency intent
        if ("emergency".equals(intent)) {
            return true;
        }
        
        // Check for emergency keywords
        for (String keyword : emergencyKeywords) {
            if (content.contains(keyword)) {
                return true;
            }
        }
        
        // Check emergency entities
        if (entities != null) {
            String emergencyType = (String) entities.get("emergency_type");
            if (emergencyType != null) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Checks for urgent symptoms that need medical attention
     */
    private boolean hasUrgentSymptoms(String content) {
        for (String symptom : urgentSymptoms) {
            if (content.contains(symptom)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Checks for medication-related emergencies
     */
    private boolean isMedicationEmergency(String content) {
        for (String keyword : medicationEmergencyKeywords) {
            if (content.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Checks if the AI confidence is too low to provide safe advice
     */
    private boolean isLowConfidenceScenario(Double confidence, String intent) {
        // If confidence is very low and it's a medical question, escalate
        if (confidence != null && confidence < 0.3) {
            return isMedicalIntent(intent);
        }
        return false;
    }
    
    /**
     * Checks if user is explicitly requesting human assistance
     */
    private boolean isUserRequestingHuman(String content) {
        String[] humanRequestKeywords = {
            "talk to doctor", "speak to human", "human help", "real person",
            "doctor please", "need doctor", "contact doctor", "call doctor"
        };
        
        for (String keyword : humanRequestKeywords) {
            if (content.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Checks for complex medical decisions that require professional judgment
     */
    private boolean isComplexMedicalDecision(String content, Map<String, Object> entities) {
        String[] complexDecisionKeywords = {
            "should i stop", "should i start", "change medication", "adjust dose",
            "drug interaction", "medication conflict", "treatment option",
            "surgery decision", "second opinion"
        };
        
        for (String keyword : complexDecisionKeywords) {
            if (content.contains(keyword)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Checks if the intent is medical-related
     */
    private boolean isMedicalIntent(String intent) {
        String[] medicalIntents = {
            "symptom_report", "medication_inquiry", "emergency", "health_inquiry"
        };
        
        return Arrays.asList(medicalIntents).contains(intent);
    }
    
    /**
     * Gets the escalation reason for logging
     */
    public String getEscalationReason(Message userMessage, ConversationSession session) {
        String intent = userMessage.getIntent();
        String content = userMessage.getContent().toLowerCase();
        Map<String, Object> entities = userMessage.getEntities();
        Double confidence = userMessage.getConfidence();
        
        if (isEmergencyScenario(intent, content, entities)) {
            return "Emergency situation detected";
        }
        
        if (hasUrgentSymptoms(content)) {
            return "Urgent symptoms reported";
        }
        
        if (isMedicationEmergency(content)) {
            return "Medication emergency";
        }
        
        if (isLowConfidenceScenario(confidence, intent)) {
            return "Low confidence medical query";
        }
        
        if (isUserRequestingHuman(content)) {
            return "User requested human assistance";
        }
        
        if (isComplexMedicalDecision(content, entities)) {
            return "Complex medical decision required";
        }
        
        return "General escalation";
    }
    
    /**
     * Gets the escalation type for categorization
     */
    public String getEscalationType(Message userMessage) {
        String intent = userMessage.getIntent();
        String content = userMessage.getContent().toLowerCase();
        
        if ("emergency".equals(intent) || isEmergencyScenario(intent, content, userMessage.getEntities())) {
            return "emergency";
        }
        
        if (isComplexMedicalDecision(content, userMessage.getEntities())) {
            return "complex_medical";
        }
        
        if (isUserRequestingHuman(content)) {
            return "user_request";
        }
        
        if (userMessage.getConfidence() != null && userMessage.getConfidence() < 0.3) {
            return "low_confidence";
        }
        
        return "general";
    }
    
    /**
     * Initialize escalation rules and keywords
     */
    private void initializeEscalationRules() {
        emergencyKeywords = Arrays.asList(
            "chest pain", "can't breathe", "difficulty breathing", "severe bleeding",
            "unconscious", "heart attack", "stroke", "emergency", "911",
            "ambulance", "hospital", "urgent care", "severe pain"
        );
        
        urgentSymptoms = Arrays.asList(
            "sudden severe pain", "vision loss", "severe headache", "high fever",
            "severe swelling", "difficulty swallowing", "severe nausea",
            "severe dizziness", "fainting", "seizure", "severe allergic reaction"
        );
        
        medicationEmergencyKeywords = Arrays.asList(
            "allergic reaction", "severe side effect", "overdose", "poisoning",
            "medication error", "wrong medication", "double dose", "severe reaction",
            "anaphylaxis", "medication interaction"
        );
    }
}