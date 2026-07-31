package com.example.myrajourney.common.messaging.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.myrajourney.common.messaging.models.ConversationSession;
import com.example.myrajourney.common.messaging.models.Message;
import com.example.myrajourney.common.messaging.models.ConversationContext;
import com.example.myrajourney.core.network.ApiService;
import com.example.myrajourney.core.network.ApiClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;

/**
 * Maintains conversation history and user context across sessions
 */
public class ContextMemoryStore {
    private static final String TAG = "ContextMemoryStore";
    private static final String PREFS_NAME = "chatbot_context";
    private static final String KEY_SESSIONS = "sessions";
    private static final String KEY_USER_CONTEXT = "user_context_";
    
    private Context context;
    private SharedPreferences prefs;
    private Gson gson;
    private ApiService apiService;
    private Map<String, ConversationSession> sessionCache;
    
    public ContextMemoryStore(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
        this.apiService = ApiClient.getApiService(context);
        this.sessionCache = new HashMap<>();
        
        loadSessionsFromCache();
    }
    
    /**
     * Saves a conversation session
     */
    public void saveSession(ConversationSession session) {
        try {
            // Cache in memory
            sessionCache.put(session.getSessionId(), session);
            
            // Save to SharedPreferences
            saveSessionsToCache();
            
            // TODO: Save to backend API for persistence
            Log.d(TAG, "Session saved: " + session.getSessionId());
            
        } catch (Exception e) {
            Log.e(TAG, "Error saving session", e);
        }
    }
    
    /**
     * Retrieves a conversation session
     */
    public ConversationSession getSession(String sessionId) {
        try {
            // First check memory cache
            ConversationSession session = sessionCache.get(sessionId);
            if (session != null) {
                return session;
            }
            
            // TODO: Try to load from backend API
            
            return null;
            
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving session", e);
            return null;
        }
    }
    
    /**
     * Saves a message to the conversation
     */
    public void saveMessage(String sessionId, Message message) {
        try {
            ConversationSession session = getSession(sessionId);
            if (session != null) {
                session.addMessage(message);
                saveSession(session);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error saving message", e);
        }
    }
    
    /**
     * Updates session activity timestamp
     */
    public void updateSessionActivity(String sessionId) {
        try {
            ConversationSession session = getSession(sessionId);
            if (session != null) {
                session.setLastActivity(new Date());
                saveSession(session);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error updating session activity", e);
        }
    }
    
    /**
     * Loads user context from various data sources
     */
    public ConversationContext loadUserContext(String userId) {
        try {
            ConversationContext context = new ConversationContext();
            
            // Load from cache first
            String cacheKey = KEY_USER_CONTEXT + userId;
            String cachedContext = prefs.getString(cacheKey, null);
            
            if (cachedContext != null) {
                context = gson.fromJson(cachedContext, ConversationContext.class);
            } else {
                // Load fresh context from various sources
                context = buildUserContext(userId);
                
                // Cache the context
                prefs.edit().putString(cacheKey, gson.toJson(context)).apply();
            }
            
            return context;
            
        } catch (Exception e) {
            Log.e(TAG, "Error loading user context", e);
            return new ConversationContext();
        }
    }
    
    /**
     * Builds user context from patient data, medical history, etc.
     */
    private ConversationContext buildUserContext(String userId) {
        ConversationContext context = new ConversationContext();
        
        try {
            // TODO: Load patient profile from API
            // TODO: Load medical history from API
            // TODO: Load current medications from API
            // TODO: Load recent appointments from API
            
            // For now, create basic context
            ConversationContext.UserPreferences prefs = new ConversationContext.UserPreferences();
            prefs.setCommunicationStyle("simple");
            prefs.setLanguage("en");
            prefs.setEnablePersonalization(true);
            prefs.setShareDataForLearning(true);
            
            context.setPreferences(prefs);
            
        } catch (Exception e) {
            Log.e(TAG, "Error building user context", e);
        }
        
        return context;
    }
    
    /**
     * Gets session with full message history
     */
    public ConversationSession getSessionWithHistory(String sessionId, String userId) {
        try {
            ConversationSession session = getSession(sessionId);
            
            if (session == null) {
                // Create new session if not found
                session = new ConversationSession(sessionId, userId);
                ConversationContext context = loadUserContext(userId);
                session.setContext(context);
            }
            
            return session;
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting session with history", e);
            return null;
        }
    }
    
    /**
     * Logs an escalation event
     */
    public void logEscalationEvent(String sessionId, String reason) {
        try {
            // TODO: Send escalation event to backend API
            Log.d(TAG, "Escalation logged for session: " + sessionId + ", reason: " + reason);
            
        } catch (Exception e) {
            Log.e(TAG, "Error logging escalation event", e);
        }
    }
    
    /**
     * Clears expired sessions from cache
     */
    public void clearExpiredSessions() {
        try {
            long currentTime = System.currentTimeMillis();
            long expirationTime = 24 * 60 * 60 * 1000; // 24 hours
            
            sessionCache.entrySet().removeIf(entry -> {
                ConversationSession session = entry.getValue();
                return (currentTime - session.getLastActivity().getTime()) > expirationTime;
            });
            
            saveSessionsToCache();
            
        } catch (Exception e) {
            Log.e(TAG, "Error clearing expired sessions", e);
        }
    }
    
    /**
     * Saves sessions to SharedPreferences cache
     */
    private void saveSessionsToCache() {
        try {
            String sessionsJson = gson.toJson(sessionCache);
            prefs.edit().putString(KEY_SESSIONS, sessionsJson).apply();
            
        } catch (Exception e) {
            Log.e(TAG, "Error saving sessions to cache", e);
        }
    }
    
    /**
     * Loads sessions from SharedPreferences cache
     */
    private void loadSessionsFromCache() {
        try {
            String sessionsJson = prefs.getString(KEY_SESSIONS, "{}");
            Type type = new TypeToken<Map<String, ConversationSession>>(){}.getType();
            sessionCache = gson.fromJson(sessionsJson, type);
            
            if (sessionCache == null) {
                sessionCache = new HashMap<>();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error loading sessions from cache", e);
            sessionCache = new HashMap<>();
        }
    }
    
    /**
     * Gets all active sessions for a user
     */
    public List<ConversationSession> getActiveSessionsForUser(String userId) {
        List<ConversationSession> userSessions = new ArrayList<>();
        
        for (ConversationSession session : sessionCache.values()) {
            if (userId.equals(session.getUserId()) && session.isActive()) {
                userSessions.add(session);
            }
        }
        
        return userSessions;
    }
    
    /**
     * Invalidates user context cache
     */
    public void invalidateUserContext(String userId) {
        String cacheKey = KEY_USER_CONTEXT + userId;
        prefs.edit().remove(cacheKey).apply();
    }
}