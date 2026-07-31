package com.example.myrajourney.common.messaging.models;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

/**
 * Represents a conversation session between user and chatbot
 */
public class ConversationSession {
    private String sessionId;
    private String userId;
    private Date startTime;
    private Date lastActivity;
    private SessionStatus status;
    private ConversationContext context;
    private List<Message> messages;

    public enum SessionStatus {
        ACTIVE,
        ENDED,
        ESCALATED
    }

    public ConversationSession() {
        this.messages = new ArrayList<>();
        this.startTime = new Date();
        this.lastActivity = new Date();
        this.status = SessionStatus.ACTIVE;
    }

    public ConversationSession(String sessionId, String userId) {
        this();
        this.sessionId = sessionId;
        this.userId = userId;
    }

    // Getters and Setters
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(Date lastActivity) {
        this.lastActivity = lastActivity;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
    }

    public ConversationContext getContext() {
        return context;
    }

    public void setContext(ConversationContext context) {
        this.context = context;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public void addMessage(Message message) {
        this.messages.add(message);
        this.lastActivity = new Date();
    }

    public boolean isActive() {
        return status == SessionStatus.ACTIVE;
    }

    public void endSession() {
        this.status = SessionStatus.ENDED;
        this.lastActivity = new Date();
    }

    public void escalateSession() {
        this.status = SessionStatus.ESCALATED;
        this.lastActivity = new Date();
    }
}