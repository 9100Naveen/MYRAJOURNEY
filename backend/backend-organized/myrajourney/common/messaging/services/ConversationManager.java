package com.example.myrajourney.common.messaging.services;

import android.content.Context;
import com.example.myrajourney.common.messaging.ContextManager;
import com.example.myrajourney.core.session.SessionManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service class for managing conversation sessions and context
 * Handles conversation lifecycle and provides session management capabilities
 */
public class ConversationManager {
    
    private Context context;
    private SessionManager sessionManager;
    private ContextManager contextManager;
    private Map<String, ConversationSession> activeSessions;
    
    // Configuration
    private static final long SESSION_TIMEOUT = 30 * 60 * 1000; // 30 minutes
    private static final int MAX_ACTIVE_SESSIONS = 50;
    
    public ConversationManager(Context context) {
        this.context = context;
        this.sessionManager = SessionManager.getInstance(context);
        this.contextManager = new ContextManager();
        this.activeSessions = new ConcurrentHashMap<>();
    }
    
    /**
     * Start a new conversation session
     */
    public String startSession(String userId) {
        String sessionId = generateSessionId(userId);
        
        ConversationSession session = new ConversationSession(
            sessionId,
            userId,
            System.currentTimeMillis()
        );
        
        activeSessions.put(sessionId, session);
        
        // Cleanup old sessions if needed
        cleanupExpiredSessions();
        
        android.util.Log.d("ConversationManager", "Started new session: " + sessionId + " for user: " + userId);
        
        return sessionId;
    }
    
    /**
     * End a conversation session
     */
    public void endSession(String sessionId) {
        ConversationSession session = activeSessions.remove(sessionId);
        
        if (session != null) {
            session.setEndTime(System.currentTimeMillis());
            contextManager.clearHistory(sessionId);
            
            android.util.Log.d("ConversationManager", 
                "Ended session: " + sessionId + " (duration: " + session.getDuration() + "ms)");
        }
    }
    
    /**
     * Get active conversation session
     */
    public ConversationSession getSession(String sessionId) {
        ConversationSession session = activeSessions.get(sessionId);
        
        if (session != null && !session.isExpired(SESSION_TIMEOUT)) {
            session.updateLastActivity();
            return session;
        } else if (session != null) {
            // Session expired, remove it
            endSession(sessionId);
        }
        
        return null;
    }
    
    /**
     * Check if session is active
     */
    public boolean isSessionActive(String sessionId) {
        ConversationSession session = activeSessions.get(sessionId);
        return session != null && !session.isExpired(SESSION_TIMEOUT);
    }
    
    /**
     * Update session activity
     */
    public void updateSessionActivity(String sessionId) {
        ConversationSession session = activeSessions.get(sessionId);
        if (session != null) {
            session.updateLastActivity();
        }
    }
    
    /**
     * Get conversation context manager
     */
    public ContextManager getContextManager() {
        return contextManager;
    }
    
    /**
     * Get session statistics
     */
    public SessionStatistics getSessionStatistics() {
        cleanupExpiredSessions();
        
        SessionStatistics stats = new SessionStatistics();
        stats.activeSessions = activeSessions.size();
        stats.maxSessions = MAX_ACTIVE_SESSIONS;
        stats.sessionTimeoutMs = SESSION_TIMEOUT;
        
        // Calculate average session duration for active sessions
        if (!activeSessions.isEmpty()) {
            long totalDuration = 0;
            int completedSessions = 0;
            
            for (ConversationSession session : activeSessions.values()) {
                if (session.getEndTime() > 0) {
                    totalDuration += session.getDuration();
                    completedSessions++;
                }
            }
            
            if (completedSessions > 0) {
                stats.averageSessionDurationMs = totalDuration / completedSessions;
            }
        }
        
        return stats;
    }
    
    /**
     * Clean up expired sessions
     */
    private void cleanupExpiredSessions() {
        long currentTime = System.currentTimeMillis();
        int removedCount = 0;
        
        // Create array to avoid concurrent modification
        String[] sessionIds = activeSessions.keySet().toArray(new String[0]);
        
        for (String sessionId : sessionIds) {
            ConversationSession session = activeSessions.get(sessionId);
            if (session != null && session.isExpired(SESSION_TIMEOUT)) {
                endSession(sessionId);
                removedCount++;
            }
        }
        
        if (removedCount > 0) {
            android.util.Log.d("ConversationManager", "Cleaned up " + removedCount + " expired sessions");
        }
    }
    
    /**
     * Generate unique session ID
     */
    private String generateSessionId(String userId) {
        return "conv_" + userId + "_" + System.currentTimeMillis() + "_" + 
               Integer.toHexString((int) (Math.random() * 0x10000));
    }
    
    /**
     * Get all active session IDs for a user
     */
    public String[] getUserSessions(String userId) {
        return activeSessions.values().stream()
            .filter(session -> userId.equals(session.getUserId()))
            .map(ConversationSession::getSessionId)
            .toArray(String[]::new);
    }
    
    /**
     * Force cleanup of all sessions (for memory management)
     */
    public void forceCleanup() {
        int sessionCount = activeSessions.size();
        
        for (String sessionId : activeSessions.keySet().toArray(new String[0])) {
            endSession(sessionId);
        }
        
        contextManager.clearAllHistories();
        
        android.util.Log.d("ConversationManager", "Force cleanup completed. Removed " + sessionCount + " sessions");
    }
    
    /**
     * Conversation session data class
     */
    public static class ConversationSession {
        private String sessionId;
        private String userId;
        private long startTime;
        private long lastActivity;
        private long endTime;
        private int messageCount;
        
        public ConversationSession(String sessionId, String userId, long startTime) {
            this.sessionId = sessionId;
            this.userId = userId;
            this.startTime = startTime;
            this.lastActivity = startTime;
            this.endTime = 0;
            this.messageCount = 0;
        }
        
        public void updateLastActivity() {
            this.lastActivity = System.currentTimeMillis();
        }
        
        public void incrementMessageCount() {
            this.messageCount++;
        }
        
        public boolean isExpired(long timeoutMs) {
            return (System.currentTimeMillis() - lastActivity) > timeoutMs;
        }
        
        public long getDuration() {
            long end = endTime > 0 ? endTime : System.currentTimeMillis();
            return end - startTime;
        }
        
        // Getters and setters
        public String getSessionId() { return sessionId; }
        public String getUserId() { return userId; }
        public long getStartTime() { return startTime; }
        public long getLastActivity() { return lastActivity; }
        public long getEndTime() { return endTime; }
        public void setEndTime(long endTime) { this.endTime = endTime; }
        public int getMessageCount() { return messageCount; }
        
        @Override
        public String toString() {
            return String.format("ConversationSession{id='%s', user='%s', duration=%dms, messages=%d}",
                sessionId, userId, getDuration(), messageCount);
        }
    }
    
    /**
     * Session statistics for monitoring
     */
    public static class SessionStatistics {
        public int activeSessions;
        public int maxSessions;
        public long sessionTimeoutMs;
        public long averageSessionDurationMs;
        
        @Override
        public String toString() {
            return String.format(
                "SessionStatistics{active=%d/%d, timeout=%dms, avgDuration=%dms}",
                activeSessions, maxSessions, sessionTimeoutMs, averageSessionDurationMs
            );
        }
    }
}