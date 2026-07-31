package com.example.myrajourney.core.network;

import java.util.ArrayList;
import java.util.List;

public class DiagnosticsResult {
    private boolean networkAvailable;
    private boolean ipReachable;
    private boolean portOpen;
    private boolean apiResponding;
    private String recommendedAction;
    private List<String> issues;
    private long responseTime;
    private String currentIpAddress;
    private int currentPort;
    private String apiBaseUrl;

    public DiagnosticsResult() {
        this.issues = new ArrayList<>();
        this.responseTime = -1;
    }

    // Getters and setters
    public boolean isNetworkAvailable() {
        return networkAvailable;
    }

    public void setNetworkAvailable(boolean networkAvailable) {
        this.networkAvailable = networkAvailable;
    }

    public boolean isIpReachable() {
        return ipReachable;
    }

    public void setIpReachable(boolean ipReachable) {
        this.ipReachable = ipReachable;
    }

    public boolean isPortOpen() {
        return portOpen;
    }

    public void setPortOpen(boolean portOpen) {
        this.portOpen = portOpen;
    }

    public boolean isApiResponding() {
        return apiResponding;
    }

    public void setApiResponding(boolean apiResponding) {
        this.apiResponding = apiResponding;
    }

    public String getRecommendedAction() {
        return recommendedAction;
    }

    public void setRecommendedAction(String recommendedAction) {
        this.recommendedAction = recommendedAction;
    }

    public List<String> getIssues() {
        return issues;
    }

    public void addIssue(String issue) {
        this.issues.add(issue);
    }

    public long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }

    public String getCurrentIpAddress() {
        return currentIpAddress;
    }

    public void setCurrentIpAddress(String currentIpAddress) {
        this.currentIpAddress = currentIpAddress;
    }

    public int getCurrentPort() {
        return currentPort;
    }

    public void setCurrentPort(int currentPort) {
        this.currentPort = currentPort;
    }

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public void setApiBaseUrl(String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
    }

    public boolean isAllHealthy() {
        return networkAvailable && ipReachable && portOpen && apiResponding;
    }

    public String getSummary() {
        if (isAllHealthy()) {
            return "All network diagnostics passed. API is reachable and responding.";
        }

        StringBuilder summary = new StringBuilder("Network Issues Detected:\n");
        if (!networkAvailable) {
            summary.append("• No internet connection\n");
        }
        if (!ipReachable) {
            summary.append("• Server IP address unreachable\n");
        }
        if (!portOpen) {
            summary.append("• Server port not responding\n");
        }
        if (!apiResponding) {
            summary.append("• API endpoint not responding\n");
        }

        if (recommendedAction != null) {
            summary.append("\nRecommended Action: ").append(recommendedAction);
        }

        return summary.toString();
    }
}