package com.example.myrajourney.common.messaging;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages conversation context and history for enhanced chatbot interactions
 * Provides memory and context awareness across conversation sessions
 */
public class ContextManager {
    
    private Map<String, ConversationHistory> sessionHistories;
    private Map<String, PatientContext> patientContexts;
    
    // Configuration
    private static final int MAX_HISTORY_SIZE = 20;
    private static final long SESSION_TIMEOUT = 30 * 60 * 1000; // 30 minutes
    
    public ContextManager() {
        this.sessionHistories = new ConcurrentHashMap<>();
        this.patientContexts = new ConcurrentHashMap<>();
    }
    
    /**
     * Add message to conversation history
     */
    public void addToHistory(String sessionId, String userMessage, String botResponse) {
        ConversationHistory history = sessionHistories.computeIfAbsent(sessionId, 
            k -> new ConversationHistory(sessionId));
        
        ConversationTurn turn = new ConversationTurn(
            System.currentTimeMillis(),
            userMessage,
            botResponse
        );
        
        history.addTurn(turn);
        
        // Cleanup old sessions
        cleanupExpiredSessions();
    }
    
    /**
     * Get conversation history for context
     */
    public ConversationHistory getHistory(String sessionId) {
        return sessionHistories.get(sessionId);
    }
    
    /**
     * Clear history for specific session
     */
    public void clearHistory(String sessionId) {
        sessionHistories.remove(sessionId);
        patientContexts.remove(sessionId);
    }
    
    /**
     * Clear all histories (for memory management)
     */
    public void clearAllHistories() {
        sessionHistories.clear();
        patientContexts.clear();
    }
    
    /**
     * Get recent conversation context for better responses
     */
    public String getRecentContext(String sessionId, int maxTurns) {
        ConversationHistory history = sessionHistories.get(sessionId);
        if (history == null || history.getTurns().isEmpty()) {
            return "";
        }
        
        StringBuilder context = new StringBuilder();
        List<ConversationTurn> recentTurns = history.getRecentTurns(maxTurns);
        
        for (ConversationTurn turn : recentTurns) {
            context.append("User: ").append(turn.getUserMessage()).append("\n");
            context.append("Bot: ").append(turn.getBotResponse()).append("\n\n");
        }
        
        return context.toString();
    }
    
    /**
     * Analyze conversation patterns for insights
     */
    public ConversationAnalysis analyzeConversation(String sessionId) {
        ConversationHistory history = sessionHistories.get(sessionId);
        if (history == null) {
            return new ConversationAnalysis();
        }
        
        ConversationAnalysis analysis = new ConversationAnalysis();
        List<ConversationTurn> turns = history.getTurns();
        
        // Analyze conversation patterns
        analysis.setTotalTurns(turns.size());
        analysis.setSessionDuration(history.getSessionDuration());
        
        // Analyze topics discussed
        Map<String, Integer> topicFrequency = new HashMap<>();
        for (ConversationTurn turn : turns) {
            String userMessage = turn.getUserMessage().toLowerCase();
            
            if (userMessage.contains("pain")) {
                topicFrequency.put("pain", topicFrequency.getOrDefault("pain", 0) + 1);
            }
            if (userMessage.contains("medication")) {
                topicFrequency.put("medication", topicFrequency.getOrDefault("medication", 0) + 1);
            }
            if (userMessage.contains("exercise")) {
                topicFrequency.put("exercise", topicFrequency.getOrDefault("exercise", 0) + 1);
            }
            if (userMessage.contains("symptom")) {
                topicFrequency.put("symptoms", topicFrequency.getOrDefault("symptoms", 0) + 1);
            }
        }
        
        analysis.setTopicFrequency(topicFrequency);
        
        // Determine primary concern
        String primaryConcern = topicFrequency.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("general");
        
        analysis.setPrimaryConcern(primaryConcern);
        
        return analysis;
    }
    
    /**
     * Store patient context for session
     */
    public void storePatientContext(String sessionId, PatientContext context) {
        patientContexts.put(sessionId, context);
    }
    
    /**
     * Get patient context for session
     */
    public PatientContext getPatientContext(String sessionId) {
        return patientContexts.get(sessionId);
    }
    
    /**
     * Check if user is asking repeated questions
     */
    public boolean isRepeatedQuestion(String sessionId, String userMessage) {
        ConversationHistory history = sessionHistories.get(sessionId);
        if (history == null) return false;
        
        String normalizedMessage = normalizeMessage(userMessage);
        
        return history.getTurns().stream()
            .anyMatch(turn -> {
                String normalizedPrevious = normalizeMessage(turn.getUserMessage());
                return calculateSimilarity(normalizedMessage, normalizedPrevious) > 0.8;
            });
    }
    
    /**
     * Get suggested follow-up questions based on conversation history
     */
    public List<String> getSuggestedFollowUps(String sessionId) {
        ConversationAnalysis analysis = analyzeConversation(sessionId);
        List<String> suggestions = new ArrayList<>();
        
        String primaryConcern = analysis.getPrimaryConcern();
        
        switch (primaryConcern) {
            case "pain":
                suggestions.add("Would you like tips for managing morning stiffness?");
                suggestions.add("Have you tried heat or cold therapy?");
                suggestions.add("What's your current pain level today?");
                break;
                
            case "medication":
                suggestions.add("Do you have any concerns about side effects?");
                suggestions.add("Would you like medication reminders?");
                suggestions.add("How is your medication adherence?");
                break;
                
            case "exercise":
                suggestions.add("Would you like to try some gentle RA exercises?");
                suggestions.add("Do you need help with exercise modifications?");
                suggestions.add("How are you feeling after your workouts?");
                break;
                
            case "symptoms":
                suggestions.add("Would you like to log your symptoms?");
                suggestions.add("Have you noticed any symptom patterns?");
                suggestions.add("How are your energy levels today?");
                break;
                
            default:
                suggestions.add("How are you feeling today?");
                suggestions.add("Is there anything specific about your RA you'd like to discuss?");
                suggestions.add("Would you like some general RA management tips?");
                break;
        }
        
        return suggestions;
    }
    
    /**
     * Clean up expired sessions to manage memory
     */
    private void cleanupExpiredSessions() {
        long currentTime = System.currentTimeMillis();
        
        sessionHistories.entrySet().removeIf(entry -> {
            ConversationHistory history = entry.getValue();
            return (currentTime - history.getLastActivity()) > SESSION_TIMEOUT;
        });
        
        patientContexts.entrySet().removeIf(entry -> {
            // Remove contexts for sessions that no longer exist
            return !sessionHistories.containsKey(entry.getKey());
        });
    }
    
    /**
     * Normalize message for comparison
     */
    private String normalizeMessage(String message) {
        return message.toLowerCase()
            .replaceAll("[^a-zA-Z0-9\\s]", "")
            .replaceAll("\\s+", " ")
            .trim();
    }
    
    /**
     * Calculate similarity between two messages (simple implementation)
     */
    private double calculateSimilarity(String message1, String message2) {
        String[] words1 = message1.split("\\s+");
        String[] words2 = message2.split("\\s+");
        
        int commonWords = 0;
        for (String word1 : words1) {
            for (String word2 : words2) {
                if (word1.equals(word2)) {
                    commonWords++;
                    break;
                }
            }
        }
        
        int totalWords = Math.max(words1.length, words2.length);
        return totalWords > 0 ? (double) commonWords / totalWords : 0.0;
    }
    
    /**
     * Conversation history for a session
     */
    public static class ConversationHistory {
        private String sessionId;
        private List<ConversationTurn> turns;
        private long startTime;
        private long lastActivity;
        
        public ConversationHistory(String sessionId) {
            this.sessionId = sessionId;
            this.turns = new ArrayList<>();
            this.startTime = System.currentTimeMillis();
            this.lastActivity = startTime;
        }
        
        public void addTurn(ConversationTurn turn) {
            turns.add(turn);
            lastActivity = turn.getTimestamp();
            
            // Limit history size
            if (turns.size() > MAX_HISTORY_SIZE) {
                turns.remove(0);
            }
        }
        
        public List<ConversationTurn> getTurns() {
            return new ArrayList<>(turns);
        }
        
        public List<ConversationTurn> getRecentTurns(int count) {
            int size = turns.size();
            int startIndex = Math.max(0, size - count);
            return new ArrayList<>(turns.subList(startIndex, size));
        }
        
        public long getSessionDuration() {
            return lastActivity - startTime;
        }
        
        public long getLastActivity() {
            return lastActivity;
        }
        
        public String getSessionId() {
            return sessionId;
        }
    }
    
    /**
     * Individual conversation turn
     */
    public static class ConversationTurn {
        private long timestamp;
        private String userMessage;
        private String botResponse;
        
        public ConversationTurn(long timestamp, String userMessage, String botResponse) {
            this.timestamp = timestamp;
            this.userMessage = userMessage;
            this.botResponse = botResponse;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
        
        public String getUserMessage() {
            return userMessage;
        }
        
        public String getBotResponse() {
            return botResponse;
        }
    }
    
    /**
     * Conversation analysis results
     */
    public static class ConversationAnalysis {
        private int totalTurns;
        private long sessionDuration;
        private Map<String, Integer> topicFrequency;
        private String primaryConcern;
        
        public ConversationAnalysis() {
            this.topicFrequency = new HashMap<>();
        }
        
        // Getters and setters
        public int getTotalTurns() { return totalTurns; }
        public void setTotalTurns(int totalTurns) { this.totalTurns = totalTurns; }
        
        public long getSessionDuration() { return sessionDuration; }
        public void setSessionDuration(long sessionDuration) { this.sessionDuration = sessionDuration; }
        
        public Map<String, Integer> getTopicFrequency() { return topicFrequency; }
        public void setTopicFrequency(Map<String, Integer> topicFrequency) { this.topicFrequency = topicFrequency; }
        
        public String getPrimaryConcern() { return primaryConcern; }
        public void setPrimaryConcern(String primaryConcern) { this.primaryConcern = primaryConcern; }
    }
}