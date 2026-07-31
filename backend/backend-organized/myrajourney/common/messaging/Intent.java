package com.example.myrajourney.common.messaging;

/**
 * Represents user intent from message analysis
 */
public class Intent {
    private IntentType type;
    private float confidence;
    
    public Intent(IntentType type, float confidence) {
        this.type = type;
        this.confidence = confidence;
    }
    
    public IntentType getType() {
        return type;
    }
    
    public void setType(IntentType type) {
        this.type = type;
    }
    
    public float getConfidence() {
        return confidence;
    }
    
    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }
}