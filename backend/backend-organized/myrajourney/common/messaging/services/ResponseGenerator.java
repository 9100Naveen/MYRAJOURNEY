package com.example.myrajourney.common.messaging.services;

import android.content.Context;
import android.util.Log;

import com.example.myrajourney.common.messaging.models.ConversationSession;
import com.example.myrajourney.common.messaging.models.Message;
import com.example.myrajourney.common.messaging.models.ResponseMetadata;
import com.example.myrajourney.core.network.ApiService;
import com.example.myrajourney.core.network.ApiClient;

import java.util.Map;

/**
 * Creates contextually appropriate responses using multiple data sources
 */
public class ResponseGenerator {
    private static final String TAG = "ResponseGenerator";
    
    private Context context;
    private ApiService apiService;
    private PersonalizationEngine personalizationEngine;
    private EscalationRulesEngine escalationEngine;
    
    public ResponseGenerator(Context context) {
        this.context = context;
        this.apiService = ApiClient.getApiService(context);
        this.personalizationEngine = new PersonalizationEngine(context);
        this.escalationEngine = new EscalationRulesEngine(context);
    }
    
    /**
     * Generates a response based on user message and conversation context
     */
    public String generateResponse(Message userMessage, ConversationSession session) {
        try {
            String intent = userMessage.getIntent();
            Map<String, Object> entities = userMessage.getEntities();
            
            // Check if escalation is needed
            if (escalationEngine.shouldEscalate(userMessage, session)) {
                return handleEscalation(userMessage, session);
            }
            
            // Generate response based on intent
            String response = generateIntentBasedResponse(intent, entities, session);
            
            // Personalize the response
            response = personalizationEngine.personalizeResponse(response, session.getContext());
            
            // Add navigation buttons if appropriate
            ResponseMetadata metadata = createResponseMetadata(intent, entities);
            userMessage.setResponseMetadata(metadata);
            
            return response;
            
        } catch (Exception e) {
            Log.e(TAG, "Error generating response", e);
            return getFallbackResponse();
        }
    }
    
    /**
     * Generates response based on detected intent
     */
    private String generateIntentBasedResponse(String intent, Map<String, Object> entities, ConversationSession session) {
        switch (intent) {
            case "appointment_inquiry":
                return generateAppointmentResponse(entities, session);
                
            case "medication_inquiry":
                return generateMedicationResponse(entities, session);
                
            case "symptom_report":
                return generateSymptomResponse(entities, session);
                
            case "navigation_request":
                return generateNavigationResponse(entities, session);
                
            case "health_inquiry":
                return generateHealthInformationResponse(entities, session);
                
            case "greeting":
                return generateGreetingResponse(session);
                
            default:
                return generateGeneralResponse(entities, session);
        }
    }
    
    private String generateAppointmentResponse(Map<String, Object> entities, ConversationSession session) {
        String action = (String) entities.get("action");
        String timeframe = (String) entities.get("timeframe");
        
        if ("schedule".equals(action)) {
            return "I can help you schedule an appointment. Let me redirect you to the appointments page where you can see available slots and book with your doctor.";
        } else if ("cancel".equals(action)) {
            return "To cancel an appointment, please go to your appointments page where you can manage your scheduled visits.";
        } else if (timeframe != null) {
            return "Let me check your appointments for " + timeframe + ". You can view all your scheduled appointments in the appointments section.";
        } else {
            return "I can help you with appointment scheduling, cancellations, or viewing your upcoming visits. Would you like me to take you to the appointments page?";
        }
    }
    
    private String generateMedicationResponse(Map<String, Object> entities, ConversationSession session) {
        String issue = (String) entities.get("issue");
        String concern = (String) entities.get("concern");
        
        if ("missed_dose".equals(issue)) {
            return "If you missed a medication dose, you can report it to your doctor through the medications section. This helps your healthcare team track your medication adherence and adjust your treatment if needed. I can take you directly to the medications page where you'll find the 'Report Missed Dose' option.";
        } else if ("side_effects".equals(issue)) {
            return "Side effects should be reported to your healthcare provider immediately. You can use the missed dose reporting feature in the medications section to document any adverse reactions, or contact your doctor directly for urgent concerns.";
        } else if ("dosage".equals(concern)) {
            return "For dosage questions, please consult your medication list in the medications section or contact your healthcare provider. Never adjust dosages without medical supervision. Would you like me to take you to your medications page?";
        } else {
            return "I can help you with medication information, missed dose reporting, and medication management. The medications section has tools to track your doses, set reminders, and report any missed doses to your doctor. Would you like me to take you there?";
        }
    }
    
    private String generateSymptomResponse(Map<String, Object> entities, ConversationSession session) {
        String symptomType = (String) entities.get("symptom_type");
        String severity = (String) entities.get("severity");
        
        StringBuilder response = new StringBuilder();
        
        if ("pain".equals(symptomType)) {
            response.append("I understand you're experiencing pain. ");
            
            if ("severe".equals(severity) || "unbearable".equals(severity)) {
                response.append("Severe pain should be evaluated by a healthcare professional. ");
                response.append("Please contact your doctor or seek immediate medical attention if the pain is sudden or worsening rapidly.");
            } else {
                response.append("For pain management, you can log your symptoms in the symptom tracker to help your doctor understand patterns and adjust your treatment.");
            }
        } else {
            response.append("Thank you for sharing your symptoms. Tracking symptoms helps your healthcare team provide better care. ");
            response.append("You can log detailed symptom information in the symptom tracker section.");
        }
        
        return response.toString();
    }
    
    private String generateNavigationResponse(Map<String, Object> entities, ConversationSession session) {
        return "I can help you navigate to different sections of your dashboard. What would you like to access? I can take you to appointments, medications, symptom tracking, or other areas of your health management.";
    }
    
    private String generateHealthInformationResponse(Map<String, Object> entities, ConversationSession session) {
        return "I'm here to provide general health information and help you manage your RA journey. For specific medical advice, always consult with your healthcare provider. What would you like to know about managing your condition?";
    }
    
    private String generateGreetingResponse(ConversationSession session) {
        String userName = "";
        if (session.getContext() != null && session.getContext().getPatientProfile() != null) {
            // userName = session.getContext().getPatientProfile().getName();
        }
        
        if (!userName.isEmpty()) {
            return "Hello " + userName + "! I'm your RA assistant. How can I help you today? I can assist with appointments, medications, symptom tracking, or answer questions about managing your condition.";
        } else {
            return "Hello! I'm your RA assistant. How can I help you today? I can assist with appointments, medications, symptom tracking, or answer questions about managing your condition.";
        }
    }
    
    private String generateGeneralResponse(Map<String, Object> entities, ConversationSession session) {
        return "I'm here to help you manage your RA journey. I can assist with appointments, medications, symptom tracking, and provide general health information. What would you like help with today?";
    }
    
    /**
     * Handles escalation scenarios
     */
    private String handleEscalation(Message userMessage, ConversationSession session) {
        String intent = userMessage.getIntent();
        
        if ("emergency".equals(intent)) {
            return "This sounds like a medical emergency. Please call emergency services (911) immediately or go to the nearest emergency room. For urgent but non-emergency concerns, contact your rheumatologist's office.";
        } else {
            return "This seems like a complex medical question that requires professional attention. I recommend contacting your healthcare provider for personalized medical advice. Would you like me to help you find your doctor's contact information?";
        }
    }
    
    /**
     * Creates response metadata with navigation buttons and actions
     */
    private ResponseMetadata createResponseMetadata(String intent, Map<String, Object> entities) {
        ResponseMetadata metadata = new ResponseMetadata();
        
        switch (intent) {
            case "appointment_inquiry":
                metadata.setResponseType(ResponseMetadata.ResponseType.NAVIGATION);
                metadata.addNavigationButton(new ResponseMetadata.NavigationButton(
                    "View Appointments", 
                    "appointments", 
                    "See your scheduled appointments and book new ones",
                    "calendar"
                ));
                break;
                
            case "medication_inquiry":
                metadata.setResponseType(ResponseMetadata.ResponseType.NAVIGATION);
                metadata.addNavigationButton(new ResponseMetadata.NavigationButton(
                    "Manage Medications", 
                    "medications", 
                    "View your medications and report missed doses",
                    "pill"
                ));
                break;
                
            case "symptom_report":
                metadata.setResponseType(ResponseMetadata.ResponseType.NAVIGATION);
                metadata.addNavigationButton(new ResponseMetadata.NavigationButton(
                    "Log Symptoms", 
                    "symptoms", 
                    "Track your symptoms and pain levels",
                    "health"
                ));
                break;
                
            case "emergency":
                metadata.setResponseType(ResponseMetadata.ResponseType.ESCALATION);
                metadata.setEscalationRequired(true);
                break;
                
            default:
                metadata.setResponseType(ResponseMetadata.ResponseType.INFORMATIONAL);
                break;
        }
        
        return metadata;
    }
    
    /**
     * Returns a fallback response when generation fails
     */
    private String getFallbackResponse() {
        return "I'm here to help with your RA management. I can assist with appointments, medications, symptom tracking, and general health questions. What would you like help with?";
    }
}