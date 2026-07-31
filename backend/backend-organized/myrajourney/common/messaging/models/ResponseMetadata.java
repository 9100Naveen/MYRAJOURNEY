package com.example.myrajourney.common.messaging.models;

import java.util.List;
import java.util.ArrayList;

/**
 * Metadata associated with chatbot responses
 */
public class ResponseMetadata {
    private ResponseType responseType;
    private double confidenceScore;
    private List<String> dataSources;
    private List<Action> suggestedActions;
    private List<NavigationButton> navigationButtons;
    private boolean escalationRequired;

    public enum ResponseType {
        INFORMATIONAL,
        ACTIONABLE,
        ESCALATION,
        CLARIFICATION,
        NAVIGATION
    }

    public ResponseMetadata() {
        this.dataSources = new ArrayList<>();
        this.suggestedActions = new ArrayList<>();
        this.navigationButtons = new ArrayList<>();
        this.escalationRequired = false;
    }

    // Getters and Setters
    public ResponseType getResponseType() {
        return responseType;
    }

    public void setResponseType(ResponseType responseType) {
        this.responseType = responseType;
    }

    public double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public List<String> getDataSources() {
        return dataSources;
    }

    public void setDataSources(List<String> dataSources) {
        this.dataSources = dataSources;
    }

    public List<Action> getSuggestedActions() {
        return suggestedActions;
    }

    public void setSuggestedActions(List<Action> suggestedActions) {
        this.suggestedActions = suggestedActions;
    }

    public List<NavigationButton> getNavigationButtons() {
        return navigationButtons;
    }

    public void setNavigationButtons(List<NavigationButton> navigationButtons) {
        this.navigationButtons = navigationButtons;
    }

    public boolean isEscalationRequired() {
        return escalationRequired;
    }

    public void setEscalationRequired(boolean escalationRequired) {
        this.escalationRequired = escalationRequired;
    }

    // Helper methods
    public void addDataSource(String source) {
        this.dataSources.add(source);
    }

    public void addSuggestedAction(Action action) {
        this.suggestedActions.add(action);
    }

    public void addNavigationButton(NavigationButton button) {
        this.navigationButtons.add(button);
    }

    public boolean hasNavigationButtons() {
        return navigationButtons != null && !navigationButtons.isEmpty();
    }

    public boolean hasSuggestedActions() {
        return suggestedActions != null && !suggestedActions.isEmpty();
    }

    /**
     * Represents a suggested action for the user
     */
    public static class Action {
        private String label;
        private String actionType;
        private String data;

        public Action() {}

        public Action(String label, String actionType, String data) {
            this.label = label;
            this.actionType = actionType;
            this.data = data;
        }

        // Getters and setters
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public String getActionType() { return actionType; }
        public void setActionType(String actionType) { this.actionType = actionType; }
        public String getData() { return data; }
        public void setData(String data) { this.data = data; }
    }

    /**
     * Represents a navigation button for page redirection
     */
    public static class NavigationButton {
        private String label;
        private String route;
        private String description;
        private String icon;

        public NavigationButton() {}

        public NavigationButton(String label, String route, String description, String icon) {
            this.label = label;
            this.route = route;
            this.description = description;
            this.icon = icon;
        }

        // Getters and setters
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public String getRoute() { return route; }
        public void setRoute(String route) { this.route = route; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
    }
}