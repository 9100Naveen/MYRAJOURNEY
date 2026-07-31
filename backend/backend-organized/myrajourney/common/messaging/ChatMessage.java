package com.example.myrajourney.common.messaging;

import com.example.myrajourney.common.messaging.models.ResponseMetadata;

import java.util.List;

public class ChatMessage {
    private String message;
    private boolean fromUser;
    private long timestamp;
    private List<ResponseMetadata.NavigationButton> navigationButtons;
    
    public ChatMessage(String message, boolean fromUser) {
        this.message = message;
        this.fromUser = fromUser;
        this.timestamp = System.currentTimeMillis();
    }
    
    public ChatMessage(String message, boolean fromUser, List<ResponseMetadata.NavigationButton> navigationButtons) {
        this.message = message;
        this.fromUser = fromUser;
        this.timestamp = System.currentTimeMillis();
        this.navigationButtons = navigationButtons;
    }
    
    public String getMessage() {
        return message;
    }
    
    public boolean isFromUser() {
        return fromUser;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public List<ResponseMetadata.NavigationButton> getNavigationButtons() {
        return navigationButtons;
    }

    public void setNavigationButtons(List<ResponseMetadata.NavigationButton> navigationButtons) {
        this.navigationButtons = navigationButtons;
    }

    public boolean hasNavigationButtons() {
        return navigationButtons != null && !navigationButtons.isEmpty();
    }
}






