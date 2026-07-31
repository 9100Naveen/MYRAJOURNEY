package com.example.myrajourney.core.network;

import java.util.HashMap;
import java.util.Map;

public class ErrorInformation {
    private int httpStatusCode;
    private String errorMessage;
    private String userFriendlyMessage;
    private String suggestedAction;
    private boolean isRetryable;
    private long timestamp;
    private String requestUrl;
    private Map<String, String> requestHeaders;
    private String responseBody;
    private NetworkError networkError;

    public ErrorInformation() {
        this.requestHeaders = new HashMap<>();
        this.timestamp = System.currentTimeMillis();
    }

    public ErrorInformation(NetworkError networkError) {
        this();
        this.networkError = networkError;
        this.errorMessage = networkError.getTitle();
        this.userFriendlyMessage = networkError.getMessage();
        this.isRetryable = networkError.isRetryable();
    }

    public ErrorInformation(int httpStatusCode, String errorMessage) {
        this();
        this.httpStatusCode = httpStatusCode;
        this.errorMessage = errorMessage;
        this.networkError = NetworkError.fromHttpCode(httpStatusCode);
        this.userFriendlyMessage = networkError.getMessage();
        this.isRetryable = networkError.isRetryable();
    }

    public ErrorInformation(Throwable throwable) {
        this();
        this.networkError = NetworkError.fromException(throwable);
        this.errorMessage = throwable.getMessage();
        this.userFriendlyMessage = networkError.getMessage();
        this.isRetryable = networkError.isRetryable();
    }

    // Getters and setters
    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getUserFriendlyMessage() {
        return userFriendlyMessage;
    }

    public void setUserFriendlyMessage(String userFriendlyMessage) {
        this.userFriendlyMessage = userFriendlyMessage;
    }

    public String getSuggestedAction() {
        return suggestedAction;
    }

    public void setSuggestedAction(String suggestedAction) {
        this.suggestedAction = suggestedAction;
    }

    public boolean isRetryable() {
        return isRetryable;
    }

    public void setRetryable(boolean retryable) {
        isRetryable = retryable;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public Map<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    public void setRequestHeaders(Map<String, String> requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public NetworkError getNetworkError() {
        return networkError;
    }

    public void setNetworkError(NetworkError networkError) {
        this.networkError = networkError;
    }

    public String getDetailedErrorMessage() {
        StringBuilder details = new StringBuilder();
        details.append("Error: ").append(errorMessage != null ? errorMessage : "Unknown error").append("\n");
        
        if (httpStatusCode > 0) {
            details.append("HTTP Status: ").append(httpStatusCode).append("\n");
        }
        
        if (requestUrl != null) {
            details.append("URL: ").append(requestUrl).append("\n");
        }
        
        if (responseBody != null && !responseBody.isEmpty()) {
            details.append("Response: ").append(responseBody).append("\n");
        }
        
        details.append("Time: ").append(new java.util.Date(timestamp));
        
        return details.toString();
    }
}