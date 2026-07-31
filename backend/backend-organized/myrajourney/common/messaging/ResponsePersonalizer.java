package com.example.myrajourney.common.messaging;

import java.util.Map;
import java.util.List;

/**
 * Personalizes chatbot responses based on patient context and preferences
 * Adapts language, tone, and content to individual patient needs
 */
public class ResponsePersonalizer {
    
    /**
     * Personalize response based on patient context and intent
     */
    public String personalizeResponse(String baseResponse, PatientContext context, IntentResult intentResult) {
        if (context == null) {
            return baseResponse;
        }
        
        StringBuilder personalizedResponse = new StringBuilder();
        
        // Add personalized greeting if appropriate
        if (shouldAddPersonalizedGreeting(baseResponse, context)) {
            personalizedResponse.append(getPersonalizedGreeting(context)).append("\n\n");
        }
        
        // Adjust language complexity based on preferences
        String adjustedResponse = adjustLanguageComplexity(baseResponse, context);
        
        // Add context-specific information
        adjustedResponse = addContextSpecificInfo(adjustedResponse, context, intentResult);
        
        // Adjust response length based on preferences
        adjustedResponse = adjustResponseLength(adjustedResponse, context);
        
        // Add personalized encouragement or support
        adjustedResponse = addPersonalizedSupport(adjustedResponse, context, intentResult);
        
        personalizedResponse.append(adjustedResponse);
        
        return personalizedResponse.toString();
    }
    
    /**
     * Check if personalized greeting should be added
     */
    private boolean shouldAddPersonalizedGreeting(String response, PatientContext context) {
        return context.getPatientName() != null && 
               !response.toLowerCase().contains("hello") && 
               !response.toLowerCase().contains("hi");
    }
    
    /**
     * Get personalized greeting based on context
     */
    private String getPersonalizedGreeting(PatientContext context) {
        if (context.getPatientName() != null) {
            if (context.isNewlyDiagnosed()) {
                return "Hi " + context.getPatientName() + "! I'm here to help you navigate your RA journey.";
            } else {
                return "Hello " + context.getPatientName() + "!";
            }
        }
        return "Hello!";
    }
    
    /**
     * Adjust language complexity based on patient preferences
     */
    private String adjustLanguageComplexity(String response, PatientContext context) {
        if (context.prefersSimpleLanguage()) {
            // Simplify medical terminology
            response = response.replace("rheumatoid arthritis", "RA");
            response = response.replace("inflammation", "swelling");
            response = response.replace("anti-inflammatory", "swelling-reducing");
            response = response.replace("cardiovascular", "heart");
            response = response.replace("gastrointestinal", "stomach");
            response = response.replace("immunosuppressive", "immune-lowering");
            response = response.replace("Disease-Modifying Antirheumatic Drugs", "RA medications");
            response = response.replace("DMARDs", "RA medications");
        }
        
        return response;
    }
    
    /**
     * Add context-specific information based on patient data
     */
    private String addContextSpecificInfo(String response, PatientContext context, IntentResult intentResult) {
        StringBuilder enhanced = new StringBuilder(response);
        
        // Add medication-specific context
        if (intentResult.getPrimaryIntent() == IntentType.MEDICATION_QUERY && 
            context.getCurrentMedications() != null && !context.getCurrentMedications().isEmpty()) {
            
            enhanced.append("\n\n**Your current medications:**\n");
            for (com.example.myrajourney.data.model.Medication med : context.getCurrentMedications()) {
                enhanced.append("• ").append(med.getName()).append(" - ").append(med.getDosage()).append("\n");
            }
        }
        
        // Add symptom-specific context
        if (intentResult.getPrimaryIntent() == IntentType.PAIN_MANAGEMENT && 
            context.hasHighSymptomActivity()) {
            
            enhanced.append("\n\n💡 **Note:** I see you've been experiencing higher symptom activity recently. ");
            enhanced.append("Consider discussing this pattern with your doctor at your next appointment.");
        }
        
        // Add experience-based context
        if (context.isNewlyDiagnosed()) {
            enhanced.append("\n\n🌟 **New to RA?** Remember that managing RA is a journey. ");
            enhanced.append("It takes time to find the right treatment plan, and I'm here to support you every step of the way.");
        } else if (context.getRADurationYears() > 5) {
            enhanced.append("\n\n👏 **You're experienced with RA management!** ");
            enhanced.append("Your knowledge and experience are valuable assets in your health journey.");
        }
        
        return enhanced.toString();
    }
    
    /**
     * Adjust response length based on patient preferences
     */
    private String adjustResponseLength(String response, PatientContext context) {
        String preferredLength = context.getPreferredResponseLength();
        
        switch (preferredLength) {
            case "short":
                return condenseResponse(response);
            case "long":
                return expandResponse(response, context);
            default: // medium
                return response;
        }
    }
    
    /**
     * Condense response for patients who prefer shorter answers
     */
    private String condenseResponse(String response) {
        // Keep only the most essential information
        String[] lines = response.split("\n");
        StringBuilder condensed = new StringBuilder();
        
        for (String line : lines) {
            // Keep main points, skip detailed explanations
            if (line.trim().startsWith("•") || 
                line.trim().startsWith("**") || 
                line.trim().startsWith("🔹") ||
                line.trim().startsWith("💊") ||
                line.trim().startsWith("⚠️")) {
                condensed.append(line).append("\n");
            } else if (line.trim().length() < 100 && !line.contains("Here are") && !line.contains("strategies")) {
                condensed.append(line).append("\n");
            }
        }
        
        return condensed.toString().trim();
    }
    
    /**
     * Expand response for patients who prefer detailed information
     */
    private String expandResponse(String response, PatientContext context) {
        StringBuilder expanded = new StringBuilder(response);
        
        // Add additional educational content
        if (response.toLowerCase().contains("medication")) {
            expanded.append("\n\n**Additional medication tips:**\n");
            expanded.append("• Keep a medication diary to track effectiveness\n");
            expanded.append("• Set up automatic pharmacy refills\n");
            expanded.append("• Bring all medications to doctor appointments\n");
            expanded.append("• Ask about generic alternatives to save costs");
        }
        
        if (response.toLowerCase().contains("exercise")) {
            expanded.append("\n\n**Exercise safety reminders:**\n");
            expanded.append("• Always warm up before exercising\n");
            expanded.append("• Listen to your body and rest when needed\n");
            expanded.append("• Consider working with a physical therapist\n");
            expanded.append("• Track your exercise tolerance over time");
        }
        
        if (response.toLowerCase().contains("pain")) {
            expanded.append("\n\n**Pain tracking suggestions:**\n");
            expanded.append("• Rate pain on a 1-10 scale daily\n");
            expanded.append("• Note what activities trigger pain\n");
            expanded.append("• Track weather patterns and pain levels\n");
            expanded.append("• Document what provides relief");
        }
        
        return expanded.toString();
    }
    
    /**
     * Add personalized support and encouragement
     */
    private String addPersonalizedSupport(String response, PatientContext context, IntentResult intentResult) {
        StringBuilder supportive = new StringBuilder(response);
        
        // Add encouragement based on context
        if (intentResult.getPrimaryIntent() == IntentType.EMOTIONAL_SUPPORT) {
            if (context.isNewlyDiagnosed()) {
                supportive.append("\n\n💙 **Remember:** You're not alone in this journey. ");
                supportive.append("Many people with RA live full, active lives with proper management.");
            } else {
                supportive.append("\n\n💪 **You've got this!** Your experience managing RA shows your strength and resilience.");
            }
        }
        
        // Add motivational content for exercise queries
        if (intentResult.getPrimaryIntent() == IntentType.EXERCISE_GUIDANCE) {
            supportive.append("\n\n🌟 **Great job** taking an active role in your RA management! ");
            supportive.append("Regular exercise is one of the best things you can do for your joints and overall health.");
        }
        
        // Add supportive content for medication concerns
        if (intentResult.getPrimaryIntent() == IntentType.MEDICATION_QUERY && 
            response.toLowerCase().contains("side effect")) {
            supportive.append("\n\n💙 **It's normal to have concerns about medications.** ");
            supportive.append("Open communication with your healthcare team is key to finding the right balance.");
        }
        
        // Add general encouragement for high symptom activity
        if (context.hasHighSymptomActivity()) {
            supportive.append("\n\n🤗 **Hang in there!** Difficult days are part of the RA journey, ");
            supportive.append("but they don't define your entire experience. Better days are ahead.");
        }
        
        return supportive.toString();
    }
    
    /**
     * Personalize response tone based on patient age and preferences
     */
    private String adjustTone(String response, PatientContext context) {
        // Adjust formality based on age and preferences
        if (context.getAge() > 65) {
            // More formal tone for older patients
            response = response.replace("Hey", "Hello");
            response = response.replace("you're", "you are");
            response = response.replace("don't", "do not");
        } else if (context.getAge() < 35) {
            // More casual tone for younger patients
            response = response.replace("Hello", "Hey");
            response = response.replace("you are", "you're");
        }
        
        return response;
    }
    
    /**
     * Add relevant app feature suggestions based on context
     */
    private String addAppFeatureSuggestions(String response, PatientContext context, IntentResult intentResult) {
        StringBuilder enhanced = new StringBuilder(response);
        
        IntentType primaryIntent = intentResult.getPrimaryIntent();
        
        if (primaryIntent == IntentType.SYMPTOM_TRACKING) {
            enhanced.append("\n\n📱 **App tip:** Use the symptom logging feature to track patterns over time!");
        } else if (primaryIntent == IntentType.MEDICATION_QUERY) {
            enhanced.append("\n\n📱 **App tip:** Set up medication reminders in the app to stay on schedule!");
        } else if (primaryIntent == IntentType.EXERCISE_GUIDANCE) {
            enhanced.append("\n\n📱 **App tip:** Check out the rehabilitation section for guided RA exercises!");
        } else if (primaryIntent == IntentType.APPOINTMENT_QUERY) {
            enhanced.append("\n\n📱 **App tip:** Use the appointment preparation feature to organize your questions!");
        }
        
        return enhanced.toString();
    }
}