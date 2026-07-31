package com.example.myrajourney.common.messaging.services;

import android.content.Context;
import android.util.Log;

import com.example.myrajourney.common.messaging.models.ConversationContext;

/**
 * Tailors responses based on patient profile and interaction history
 */
public class PersonalizationEngine {
    private static final String TAG = "PersonalizationEngine";
    
    private Context context;
    
    public PersonalizationEngine(Context context) {
        this.context = context;
    }
    
    /**
     * Personalizes a response based on user context and preferences
     */
    public String personalizeResponse(String baseResponse, ConversationContext context) {
        try {
            if (context == null || context.getPreferences() == null) {
                return baseResponse;
            }
            
            String personalizedResponse = baseResponse;
            
            // Adjust communication style
            personalizedResponse = adjustCommunicationStyle(personalizedResponse, context.getPreferences());
            
            // Add personalized context if available
            personalizedResponse = addPersonalizedContext(personalizedResponse, context);
            
            return personalizedResponse;
            
        } catch (Exception e) {
            Log.e(TAG, "Error personalizing response", e);
            return baseResponse;
        }
    }
    
    /**
     * Adjusts response based on user's preferred communication style
     */
    private String adjustCommunicationStyle(String response, ConversationContext.UserPreferences preferences) {
        String style = preferences.getCommunicationStyle();
        
        if ("technical".equals(style)) {
            // Add more medical terminology and detailed explanations
            response = addTechnicalDetails(response);
        } else if ("simple".equals(style)) {
            // Simplify language and avoid medical jargon
            response = simplifyLanguage(response);
        }
        // "detailed" style uses the base response as-is
        
        return response;
    }
    
    /**
     * Adds technical details for users who prefer medical terminology
     */
    private String addTechnicalDetails(String response) {
        // Replace common terms with medical equivalents
        response = response.replace("joint pain", "arthralgia");
        response = response.replace("inflammation", "inflammatory response");
        response = response.replace("RA", "rheumatoid arthritis");
        
        return response;
    }
    
    /**
     * Simplifies language for users who prefer plain language
     */
    private String simplifyLanguage(String response) {
        // Replace medical terms with simpler alternatives
        response = response.replace("rheumatoid arthritis", "RA");
        response = response.replace("medication adherence", "taking your medicines as prescribed");
        response = response.replace("healthcare provider", "doctor");
        response = response.replace("adverse reactions", "side effects");
        
        return response;
    }
    
    /**
     * Adds personalized context based on user's medical history and current situation
     */
    private String addPersonalizedContext(String response, ConversationContext context) {
        StringBuilder personalizedResponse = new StringBuilder(response);
        
        // Add medication-specific context
        if (context.hasActiveMedications() && response.contains("medication")) {
            personalizedResponse.append(" Based on your current medications, ");
            personalizedResponse.append("make sure to follow your prescribed schedule.");
        }
        
        // Add appointment context
        if (context.hasRecentAppointments() && response.contains("appointment")) {
            personalizedResponse.append(" I see you have recent appointments scheduled. ");
            personalizedResponse.append("This is great for monitoring your condition progress.");
        }
        
        // Add risk factor awareness
        if (context.getRiskFactors() != null && !context.getRiskFactors().isEmpty()) {
            if (response.contains("symptom") || response.contains("pain")) {
                personalizedResponse.append(" Given your health profile, ");
                personalizedResponse.append("it's important to track these symptoms carefully.");
            }
        }
        
        return personalizedResponse.toString();
    }
    
    /**
     * Determines if personalization should be applied based on user preferences
     */
    public boolean shouldPersonalize(ConversationContext context) {
        if (context == null || context.getPreferences() == null) {
            return false;
        }
        
        return context.getPreferences().isEnablePersonalization();
    }
    
    /**
     * Gets personalized greeting based on time of day and user context
     */
    public String getPersonalizedGreeting(ConversationContext context) {
        StringBuilder greeting = new StringBuilder();
        
        // Time-based greeting
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        if (hour < 12) {
            greeting.append("Good morning");
        } else if (hour < 17) {
            greeting.append("Good afternoon");
        } else {
            greeting.append("Good evening");
        }
        
        // Add user name if available
        if (context != null && context.getPatientProfile() != null) {
            // greeting.append(", ").append(context.getPatientProfile().getName());
        }
        
        greeting.append("! ");
        
        // Add contextual information
        if (context != null && context.hasRecentAppointments()) {
            greeting.append("I hope your recent appointment went well. ");
        }
        
        greeting.append("How can I assist you with your RA management today?");
        
        return greeting.toString();
    }
}