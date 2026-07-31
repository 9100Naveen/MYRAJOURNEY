package com.example.myrajourney.common.messaging;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.Arrays;
import java.util.List;

/**
 * Advanced intent classification for RA-specific queries
 * Uses pattern matching and keyword analysis to determine user intent
 */
public class IntentClassifier {
    
    private Map<IntentType, IntentPattern> intentPatterns;
    private static final double CONFIDENCE_THRESHOLD = 0.6;
    
    public IntentClassifier() {
        initializeIntentPatterns();
    }
    
    /**
     * Classify user intent from message
     */
    public IntentResult classifyIntent(String message) {
        String normalizedMessage = message.toLowerCase().trim();
        
        double maxConfidence = 0.0;
        IntentType bestIntent = IntentType.GENERAL_CONVERSATION;
        Map<String, Object> entities = new HashMap<>();
        
        // Check each intent pattern
        for (Map.Entry<IntentType, IntentPattern> entry : intentPatterns.entrySet()) {
            IntentType intent = entry.getKey();
            IntentPattern pattern = entry.getValue();
            
            double confidence = calculateConfidence(normalizedMessage, pattern);
            
            if (confidence > maxConfidence && confidence >= CONFIDENCE_THRESHOLD) {
                maxConfidence = confidence;
                bestIntent = intent;
                entities = extractEntities(normalizedMessage, pattern);
            }
        }
        
        return new IntentResult(bestIntent, maxConfidence, entities);
    }
    
    /**
     * Initialize intent patterns for RA-specific queries
     */
    private void initializeIntentPatterns() {
        intentPatterns = new HashMap<>();
        
        // Medication-related queries
        intentPatterns.put(IntentType.MEDICATION_QUERY, new IntentPattern(
            Arrays.asList("medication", "medicine", "pill", "drug", "dose", "dosage", "side effect", 
                         "methotrexate", "humira", "prednisone", "nsaid", "ibuprofen", "adalimumab",
                         "sulfasalazine", "leflunomide", "hydroxychloroquine", "biologics", "dmard"),
            Arrays.asList("take", "taking", "prescribed", "doctor said", "pharmacy", "refill",
                         "missed dose", "forgot", "when to take", "how much", "interaction"),
            1.0
        ));
        
        // Pain management queries
        intentPatterns.put(IntentType.PAIN_MANAGEMENT, new IntentPattern(
            Arrays.asList("pain", "hurt", "ache", "sore", "painful", "aching", "throbbing", 
                         "stiff", "stiffness", "tender", "swollen", "inflammation", "flare",
                         "joint pain", "morning stiffness", "chronic pain"),
            Arrays.asList("level", "scale", "unbearable", "severe", "mild", "moderate",
                         "relief", "help", "manage", "reduce", "treatment"),
            1.0
        ));
        
        // Exercise and rehabilitation
        intentPatterns.put(IntentType.EXERCISE_GUIDANCE, new IntentPattern(
            Arrays.asList("exercise", "workout", "stretch", "movement", "activity", 
                         "physical therapy", "rehabilitation", "yoga", "walking", "swimming",
                         "strength training", "range of motion", "flexibility"),
            Arrays.asList("safe", "recommended", "avoid", "modify", "gentle", "low impact",
                         "during flare", "when pain", "how often", "how long"),
            1.0
        ));
        
        // Symptom tracking and monitoring
        intentPatterns.put(IntentType.SYMPTOM_TRACKING, new IntentPattern(
            Arrays.asList("symptom", "track", "log", "record", "monitor", "diary", "journal",
                         "fatigue", "tired", "energy", "sleep", "mood", "appetite"),
            Arrays.asList("daily", "weekly", "pattern", "trend", "improvement", "worsening",
                         "frequency", "severity", "duration"),
            1.0
        ));
        
        // Emotional support and mental health
        intentPatterns.put(IntentType.EMOTIONAL_SUPPORT, new IntentPattern(
            Arrays.asList("sad", "depressed", "anxious", "worried", "scared", "frustrated",
                         "angry", "overwhelmed", "stressed", "emotional", "mental health",
                         "support", "help", "cope", "difficult", "challenging"),
            Arrays.asList("feeling", "emotions", "mood", "counseling", "therapy", "support group",
                         "family", "friends", "isolation", "burden"),
            1.0
        ));
        
        // Emergency situations
        intentPatterns.put(IntentType.EMERGENCY_SITUATION, new IntentPattern(
            Arrays.asList("emergency", "urgent", "severe", "unbearable", "can't move", "fever",
                         "high fever", "chest pain", "breathing", "vision", "911", "help",
                         "crisis", "hospital", "ambulance", "immediate"),
            Arrays.asList("now", "immediately", "right away", "asap", "critical", "life threatening"),
            1.2 // Higher weight for emergency detection
        ));
        
        // Appointment and healthcare provider queries
        intentPatterns.put(IntentType.APPOINTMENT_QUERY, new IntentPattern(
            Arrays.asList("appointment", "doctor", "visit", "checkup", "rheumatologist",
                         "physician", "clinic", "hospital", "schedule", "booking"),
            Arrays.asList("when", "next", "upcoming", "reschedule", "cancel", "prepare",
                         "questions", "bring", "lab results"),
            1.0
        ));
        
        // Diet and nutrition
        intentPatterns.put(IntentType.NUTRITION_QUERY, new IntentPattern(
            Arrays.asList("diet", "food", "nutrition", "eat", "meal", "anti-inflammatory",
                         "omega", "fish", "vegetables", "supplements", "vitamin"),
            Arrays.asList("should eat", "avoid", "good for", "bad for", "inflammatory",
                         "healthy", "weight", "calcium", "vitamin d"),
            1.0
        ));
        
        // General RA information
        intentPatterns.put(IntentType.GENERAL_RA_INFO, new IntentPattern(
            Arrays.asList("what is", "explain", "rheumatoid arthritis", "ra", "autoimmune",
                         "cause", "prognosis", "future", "progression", "remission"),
            Arrays.asList("definition", "symptoms", "diagnosis", "treatment", "cure",
                         "life expectancy", "disability", "work", "family"),
            1.0
        ));
        
        // Greetings and social interaction
        intentPatterns.put(IntentType.GREETING, new IntentPattern(
            Arrays.asList("hello", "hi", "hey", "good morning", "good afternoon", 
                         "good evening", "greetings", "how are you"),
            Arrays.asList("there", "assistant", "bot", "help", "start"),
            0.8
        ));
    }
    
    /**
     * Calculate confidence score for intent match
     */
    private double calculateConfidence(String message, IntentPattern pattern) {
        double keywordScore = calculateKeywordScore(message, pattern.getKeywords());
        double contextScore = calculateContextScore(message, pattern.getContextWords());
        
        // Weighted combination
        double confidence = (keywordScore * 0.7) + (contextScore * 0.3);
        
        // Apply pattern weight
        confidence *= pattern.getWeight();
        
        // Boost confidence for exact phrase matches
        for (String keyword : pattern.getKeywords()) {
            if (message.contains(keyword)) {
                confidence += 0.1;
            }
        }
        
        return Math.min(confidence, 1.0);
    }
    
    /**
     * Calculate keyword matching score
     */
    private double calculateKeywordScore(String message, List<String> keywords) {
        int matches = 0;
        
        for (String keyword : keywords) {
            if (message.contains(keyword)) {
                matches++;
            }
        }
        
        return keywords.isEmpty() ? 0.0 : (double) matches / keywords.size();
    }
    
    /**
     * Calculate context word matching score
     */
    private double calculateContextScore(String message, List<String> contextWords) {
        int matches = 0;
        
        for (String contextWord : contextWords) {
            if (message.contains(contextWord)) {
                matches++;
            }
        }
        
        return contextWords.isEmpty() ? 0.0 : (double) matches / contextWords.size();
    }
    
    /**
     * Extract entities from message based on pattern
     */
    private Map<String, Object> extractEntities(String message, IntentPattern pattern) {
        Map<String, Object> entities = new HashMap<>();
        
        // Extract pain level
        String painLevel = extractPainLevel(message);
        if (painLevel != null) {
            entities.put("pain_level", Integer.parseInt(painLevel));
        }
        
        // Extract medication names
        List<String> medications = extractMedications(message);
        if (!medications.isEmpty()) {
            entities.put("medications", medications);
        }
        
        // Extract time references
        String timeReference = extractTimeReference(message);
        if (timeReference != null) {
            entities.put("time_reference", timeReference);
        }
        
        // Extract body parts/joints
        List<String> bodyParts = extractBodyParts(message);
        if (!bodyParts.isEmpty()) {
            entities.put("body_parts", bodyParts);
        }
        
        return entities;
    }
    
    /**
     * Extract pain level from message
     */
    private String extractPainLevel(String message) {
        Pattern pattern = Pattern.compile("(\\d+)\\s*(?:out of|/|\\s)\\s*10|pain.*?(\\d+)|level.*?(\\d+)");
        java.util.regex.Matcher matcher = pattern.matcher(message);
        
        if (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                if (matcher.group(i) != null) {
                    int level = Integer.parseInt(matcher.group(i));
                    if (level >= 0 && level <= 10) {
                        return String.valueOf(level);
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Extract medication names from message
     */
    private List<String> extractMedications(String message) {
        List<String> medications = Arrays.asList(
            "methotrexate", "humira", "adalimumab", "prednisone", "sulfasalazine",
            "leflunomide", "hydroxychloroquine", "ibuprofen", "naproxen", "celecoxib"
        );
        
        return medications.stream()
            .filter(med -> message.contains(med.toLowerCase()))
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Extract time references from message
     */
    private String extractTimeReference(String message) {
        if (message.contains("morning")) return "morning";
        if (message.contains("evening") || message.contains("night")) return "evening";
        if (message.contains("afternoon")) return "afternoon";
        if (message.contains("today")) return "today";
        if (message.contains("yesterday")) return "yesterday";
        if (message.contains("tomorrow")) return "tomorrow";
        if (message.contains("week")) return "week";
        if (message.contains("month")) return "month";
        
        return null;
    }
    
    /**
     * Extract body parts/joints from message
     */
    private List<String> extractBodyParts(String message) {
        List<String> bodyParts = Arrays.asList(
            "hand", "hands", "wrist", "wrists", "finger", "fingers", "thumb",
            "knee", "knees", "ankle", "ankles", "foot", "feet", "shoulder",
            "shoulders", "elbow", "elbows", "hip", "hips", "neck", "back"
        );
        
        return bodyParts.stream()
            .filter(part -> message.contains(part))
            .collect(java.util.stream.Collectors.toList());
    }
}

/**
 * Intent enumeration for RA-specific queries
 */
enum IntentType {
    MEDICATION_QUERY,
    PAIN_MANAGEMENT,
    EXERCISE_GUIDANCE,
    SYMPTOM_TRACKING,
    EMOTIONAL_SUPPORT,
    EMERGENCY_SITUATION,
    APPOINTMENT_QUERY,
    NUTRITION_QUERY,
    GENERAL_RA_INFO,
    GREETING,
    GENERAL_CONVERSATION
}

/**
 * Intent pattern for matching user queries
 */
class IntentPattern {
    private List<String> keywords;
    private List<String> contextWords;
    private double weight;
    
    public IntentPattern(List<String> keywords, List<String> contextWords, double weight) {
        this.keywords = keywords;
        this.contextWords = contextWords;
        this.weight = weight;
    }
    
    public List<String> getKeywords() { return keywords; }
    public List<String> getContextWords() { return contextWords; }
    public double getWeight() { return weight; }
}

/**
 * Intent classification result
 */
class IntentResult {
    private IntentType primaryIntent;
    private double confidence;
    private Map<String, Object> entities;
    
    public IntentResult(IntentType primaryIntent, double confidence, Map<String, Object> entities) {
        this.primaryIntent = primaryIntent;
        this.confidence = confidence;
        this.entities = entities;
    }
    
    public IntentType getPrimaryIntent() { return primaryIntent; }
    public double getConfidence() { return confidence; }
    public Map<String, Object> getEntities() { return entities; }
    
    public boolean isHighConfidence() {
        return confidence >= 0.8;
    }
    
    public boolean hasEntity(String entityName) {
        return entities.containsKey(entityName);
    }
    
    public Object getEntity(String entityName) {
        return entities.get(entityName);
    }
}