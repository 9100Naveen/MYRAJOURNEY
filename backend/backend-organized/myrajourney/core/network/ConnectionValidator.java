package com.example.myrajourney.core.network;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConnectionValidator {
    private static final String TAG = "ConnectionValidator";
    private static final int TIMEOUT_MS = 5000;
    private final ExecutorService executor;

    public ConnectionValidator() {
        this.executor = Executors.newCachedThreadPool();
    }

    public CompletableFuture<ValidationResult> validateNetworkConnectivity(Context context) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            try {
                boolean isConnected = NetworkUtils.isNetworkAvailable(context);
                long responseTime = System.currentTimeMillis() - startTime;
                
                if (isConnected) {
                    return new ValidationResult(true, "Network connectivity available", responseTime, null);
                } else {
                    return new ValidationResult(false, "No network connectivity", responseTime, "Check WiFi or mobile data connection");
                }
            } catch (Exception e) {
                long responseTime = System.currentTimeMillis() - startTime;
                Log.e(TAG, "Network connectivity check failed", e);
                return new ValidationResult(false, "Network check failed", responseTime, e.getMessage());
            }
        }, executor);
    }

    public CompletableFuture<ValidationResult> validateIpReachability(String ipAddress) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            try {
                InetAddress inet = InetAddress.getByName(ipAddress);
                boolean reachable = inet.isReachable(TIMEOUT_MS);
                long responseTime = System.currentTimeMillis() - startTime;
                
                if (reachable) {
                    return new ValidationResult(true, "IP address is reachable", responseTime, null);
                } else {
                    return new ValidationResult(false, "IP address unreachable", responseTime, 
                        "Check if server is running and IP address is correct");
                }
            } catch (Exception e) {
                long responseTime = System.currentTimeMillis() - startTime;
                Log.e(TAG, "IP reachability check failed for " + ipAddress, e);
                return new ValidationResult(false, "IP reachability check failed", responseTime, e.getMessage());
            }
        }, executor);
    }

    public CompletableFuture<ValidationResult> validatePortAvailability(String host, int port) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            Socket socket = null;
            try {
                socket = new Socket();
                socket.connect(new java.net.InetSocketAddress(host, port), TIMEOUT_MS);
                long responseTime = System.currentTimeMillis() - startTime;
                return new ValidationResult(true, "Port is open and accepting connections", responseTime, null);
            } catch (Exception e) {
                long responseTime = System.currentTimeMillis() - startTime;
                Log.e(TAG, "Port availability check failed for " + host + ":" + port, e);
                return new ValidationResult(false, "Port is not accessible", responseTime, 
                    "Check if server is running on port " + port);
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        Log.w(TAG, "Failed to close socket", e);
                    }
                }
            }
        }, executor);
    }

    public CompletableFuture<ValidationResult> validateApiEndpoint(String baseUrl) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            HttpURLConnection connection = null;
            try {
                // Test a simple endpoint that should return 401 (unauthorized) if API is working
                URL url = new URL(baseUrl + "auth/me");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(TIMEOUT_MS);
                connection.setReadTimeout(TIMEOUT_MS);
                connection.setRequestProperty("Content-Type", "application/json");
                
                int responseCode = connection.getResponseCode();
                long responseTime = System.currentTimeMillis() - startTime;
                
                // 401 is expected for unauthenticated requests, which means API is working
                if (responseCode == 401 || responseCode == 200) {
                    return new ValidationResult(true, "API endpoint is responding (HTTP " + responseCode + ")", 
                        responseTime, null);
                } else if (responseCode >= 500) {
                    return new ValidationResult(false, "API server error (HTTP " + responseCode + ")", 
                        responseTime, "Server is experiencing issues");
                } else {
                    return new ValidationResult(true, "API endpoint accessible (HTTP " + responseCode + ")", 
                        responseTime, null);
                }
            } catch (Exception e) {
                long responseTime = System.currentTimeMillis() - startTime;
                Log.e(TAG, "API endpoint validation failed for " + baseUrl, e);
                return new ValidationResult(false, "API endpoint not accessible", responseTime, 
                    "Check API server status and URL configuration");
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }, executor);
    }

    public CompletableFuture<ValidationResult> validateAuthentication(String baseUrl, String token) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            HttpURLConnection connection = null;
            try {
                URL url = new URL(baseUrl + "auth/me");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(TIMEOUT_MS);
                connection.setReadTimeout(TIMEOUT_MS);
                connection.setRequestProperty("Content-Type", "application/json");
                
                if (token != null && !token.isEmpty()) {
                    connection.setRequestProperty("Authorization", "Bearer " + token);
                }
                
                int responseCode = connection.getResponseCode();
                long responseTime = System.currentTimeMillis() - startTime;
                
                if (responseCode == 200) {
                    return new ValidationResult(true, "Authentication successful", responseTime, null);
                } else if (responseCode == 401) {
                    return new ValidationResult(false, "Authentication failed", responseTime, 
                        "Token is invalid or expired");
                } else {
                    return new ValidationResult(false, "Authentication check failed (HTTP " + responseCode + ")", 
                        responseTime, "Unexpected server response");
                }
            } catch (Exception e) {
                long responseTime = System.currentTimeMillis() - startTime;
                Log.e(TAG, "Authentication validation failed", e);
                return new ValidationResult(false, "Authentication check failed", responseTime, e.getMessage());
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }, executor);
    }

    public void shutdown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }

    public static class ValidationResult {
        private final boolean success;
        private final String message;
        private final long responseTimeMs;
        private final String errorDetails;

        public ValidationResult(boolean success, String message, long responseTimeMs, String errorDetails) {
            this.success = success;
            this.message = message;
            this.responseTimeMs = responseTimeMs;
            this.errorDetails = errorDetails;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public long getResponseTimeMs() {
            return responseTimeMs;
        }

        public String getErrorDetails() {
            return errorDetails;
        }
    }
}