package com.example.myrajourney.common.messaging.models;

import java.util.Date;
import java.util.Map;
import java.util.HashMap;

/**
 * Represents a single message in a conversation
 */
public class Message {
    private String id;
    private Date timestamp;
    private MessageSender sender;
    private String content;
    private String intent;
    private Map<String, Object> entities;
    private Double confidence;
    private ResponseMetadata responseMetadata;

    public enum MessageSender {
        USER,
        BOT
    }

    public Message() {
        this.timestamp = new Date();
        this.entities = new HashMap<>();
    }

    public Message(String content, MessageSender sender) {
        this();
        this.content = content;
        this.sender = sender;
        this.id = generateMessageId();
    }

    private String generateMessageId() {
        return "msg_" + System.currentTimeMillis() + "_" + Math.random();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public MessageSender getSender() {
        return sender;
    }

    public void setSender(MessageSender sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public Map<String, Object> getEntities() {
        return entities;
    }

    public void setEntities(Map<String, Object> entities) {
        this.entities = entities;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public ResponseMetadata getResponseMetadata() {
        return responseMetadata;
    }

    public void setResponseMetadata(ResponseMetadata responseMetadata) {
        this.responseMetadata = responseMetadata;
    }

    public boolean isFromUser() {
        return sender == MessageSender.USER;
    }

    public boolean isFromBot() {
        return sender == MessageSender.BOT;
    }

    public void addEntity(String key, Object value) {
        this.entities.put(key, value);
    }

    public Object getEntity(String key) {
        return this.entities.get(key);
    }
}