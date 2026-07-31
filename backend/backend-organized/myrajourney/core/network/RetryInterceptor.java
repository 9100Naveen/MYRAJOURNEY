package com.example.myrajourney.core.network;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class RetryInterceptor implements Interceptor {
    private static final String TAG = "RetryInterceptor";
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long INITIAL_RETRY_DELAY = 1000; // 1 second

    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = null;
        IOException lastException = null;
        
        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                response = chain.proceed(request);
                
                // Check if we should retry based on response code
                if (shouldRetryForStatusCode(response.code())) {
                    if (attempt < MAX_RETRY_ATTEMPTS) {
                        Log.w(TAG, "Retrying request due to HTTP " + response.code() + 
                              " (attempt " + attempt + "/" + MAX_RETRY_ATTEMPTS + ")");
                        
                        response.close(); // Close the response before retrying
                        
                        // Wait before retrying
                        waitBeforeRetry(attempt);
                        continue;
                    }
                }
                
                // Success or non-retryable error
                return response;
                
            } catch (IOException e) {
                lastException = e;
                
                if (shouldRetryForException(e) && attempt < MAX_RETRY_ATTEMPTS) {
                    Log.w(TAG, "Retrying request due to " + e.getClass().getSimpleName() + 
                          " (attempt " + attempt + "/" + MAX_RETRY_ATTEMPTS + "): " + e.getMessage());
                    
                    // Wait before retrying
                    waitBeforeRetry(attempt);
                } else {
                    // Don't retry or max attempts reached
                    throw e;
                }
            }
        }
        
        // If we get here, all retries failed
        if (lastException != null) {
            throw lastException;
        }
        
        return response;
    }

    private boolean shouldRetryForStatusCode(int statusCode) {
        // Retry for server errors (5xx) and some client errors
        switch (statusCode) {
            case 408: // Request Timeout
            case 429: // Too Many Requests
            case 500: // Internal Server Error
            case 502: // Bad Gateway
            case 503: // Service Unavailable
            case 504: // Gateway Timeout
                return true;
            default:
                return false;
        }
    }

    private boolean shouldRetryForException(IOException exception) {
        // Retry for network-related exceptions
        if (exception instanceof SocketTimeoutException) {
            return true; // Connection timeout
        }
        
        if (exception instanceof UnknownHostException) {
            return false; // DNS resolution failed, don't retry
        }
        
        String message = exception.getMessage();
        if (message != null) {
            message = message.toLowerCase();
            
            // Retry for connection issues (including "closed" errors)
            if (message.contains("connection reset") ||
                message.contains("connection refused") ||
                message.contains("connection closed") ||
                message.contains("closed") ||
                message.contains("network is unreachable") ||
                message.contains("timeout") ||
                message.contains("broken pipe") ||
                message.contains("socket closed") ||
                message.contains("unexpected end of stream") ||
                message.contains("software caused connection abort")) {
                return true;
            }
            
            // Don't retry for SSL/TLS issues
            if (message.contains("ssl") || message.contains("certificate")) {
                return false;
            }
        }
        
        return false; // Default: don't retry
    }

    private void waitBeforeRetry(int attemptNumber) {
        try {
            // Exponential backoff: 1s, 2s, 4s
            long delay = INITIAL_RETRY_DELAY * (long) Math.pow(2, attemptNumber - 1);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Log.w(TAG, "Retry delay interrupted", e);
        }
    }
}