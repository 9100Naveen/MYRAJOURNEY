package com.example.myrajourney.core.network;

public enum NetworkError {
    CONNECTION_TIMEOUT("Connection timed out", "The server is taking too long to respond. Check your internet connection and try again.", true),
    SERVER_UNREACHABLE("Server unreachable", "Cannot connect to the server. Please check if the server is running and your network configuration is correct.", true),
    AUTHENTICATION_FAILED("Authentication failed", "Your session has expired or credentials are invalid. Please log in again.", false),
    BAD_REQUEST_400("Bad request", "The request contains invalid data. Please check your input and try again.", false),
    UNAUTHORIZED_401("Unauthorized", "You don't have permission to access this resource. Please log in again.", false),
    FORBIDDEN_403("Access forbidden", "You don't have permission to perform this action.", false),
    NOT_FOUND_404("Resource not found", "The requested resource was not found on the server.", false),
    SERVER_ERROR_500("Server error", "The server encountered an internal error. Please try again later.", true),
    BAD_GATEWAY_502("Bad gateway", "The server received an invalid response. Please try again later.", true),
    SERVICE_UNAVAILABLE_503("Service unavailable", "The server is temporarily unavailable. Please try again later.", true),
    GATEWAY_TIMEOUT_504("Gateway timeout", "The server timed out waiting for a response. Please try again later.", true),
    NO_INTERNET_CONNECTION("No internet connection", "Please check your WiFi or mobile data connection and try again.", true),
    UNKNOWN_ERROR("Unknown error", "An unexpected error occurred. Please try again.", true);

    private final String title;
    private final String message;
    private final boolean retryable;

    NetworkError(String title, String message, boolean retryable) {
        this.title = title;
        this.message = message;
        this.retryable = retryable;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public boolean isRetryable() {
        return retryable;
    }

    public static NetworkError fromHttpCode(int httpCode) {
        switch (httpCode) {
            case 400:
                return BAD_REQUEST_400;
            case 401:
                return UNAUTHORIZED_401;
            case 403:
                return FORBIDDEN_403;
            case 404:
                return NOT_FOUND_404;
            case 500:
                return SERVER_ERROR_500;
            case 502:
                return BAD_GATEWAY_502;
            case 503:
                return SERVICE_UNAVAILABLE_503;
            case 504:
                return GATEWAY_TIMEOUT_504;
            default:
                if (httpCode >= 400 && httpCode < 500) {
                    return BAD_REQUEST_400;
                } else if (httpCode >= 500) {
                    return SERVER_ERROR_500;
                } else {
                    return UNKNOWN_ERROR;
                }
        }
    }

    public static NetworkError fromException(Throwable throwable) {
        if (throwable == null) {
            return UNKNOWN_ERROR;
        }

        String message = throwable.getMessage();
        if (message == null) {
            message = "";
        }

        // Check for specific exception types
        if (throwable instanceof java.net.SocketTimeoutException ||
            throwable instanceof java.net.ConnectException ||
            message.contains("timeout")) {
            return CONNECTION_TIMEOUT;
        }

        if (throwable instanceof java.net.UnknownHostException ||
            throwable instanceof java.net.NoRouteToHostException ||
            message.contains("unreachable") ||
            message.contains("host")) {
            return SERVER_UNREACHABLE;
        }

        if (throwable instanceof java.net.SocketException ||
            message.contains("network") ||
            message.contains("connection")) {
            return NO_INTERNET_CONNECTION;
        }

        return UNKNOWN_ERROR;
    }
}