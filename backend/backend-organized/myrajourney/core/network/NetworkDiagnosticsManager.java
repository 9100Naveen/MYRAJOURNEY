package com.example.myrajourney.core.network;

import android.content.Context;
import android.util.Log;

import com.example.myrajourney.R;
import com.example.myrajourney.core.session.TokenManager;

import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class NetworkDiagnosticsManager {
    private static final String TAG = "NetworkDiagnosticsManager";
    private static NetworkDiagnosticsManager instance;
    private final ConnectionValidator validator;
    private DiagnosticsResult lastResult;

    public interface DiagnosticsCallback {
        void onDiagnosticsComplete(DiagnosticsResult result);
        void onDiagnosticsProgress(String message);
    }

    public interface ConnectivityCallback {
        void onConnectivityResult(boolean isConnected, String message);
    }

    private NetworkDiagnosticsManager() {
        this.validator = new ConnectionValidator();
    }

    public static synchronized NetworkDiagnosticsManager getInstance() {
        if (instance == null) {
            instance = new NetworkDiagnosticsManager();
        }
        return instance;
    }

    public void runFullDiagnostics(Context context, DiagnosticsCallback callback) {
        if (callback != null) {
            callback.onDiagnosticsProgress("Starting network diagnostics...");
        }

        DiagnosticsResult result = new DiagnosticsResult();
        String baseUrl = context.getString(R.string.api_base_url);
        result.setApiBaseUrl(baseUrl);

        try {
            // Extract IP and port from base URL
            URL url = new URL(baseUrl);
            final String host = url.getHost();
            int portTemp = url.getPort();
            if (portTemp == -1) {
                portTemp = url.getDefaultPort();
            }
            final int port = portTemp;
            
            result.setCurrentIpAddress(host);
            result.setCurrentPort(port);

            // Step 1: Check network connectivity
            if (callback != null) {
                callback.onDiagnosticsProgress("Checking network connectivity...");
            }
            
            validator.validateNetworkConnectivity(context)
                .thenCompose(networkResult -> {
                    result.setNetworkAvailable(networkResult.isSuccess());
                    if (!networkResult.isSuccess()) {
                        result.addIssue("Network connectivity: " + networkResult.getMessage());
                        result.setRecommendedAction("Check your internet connection (WiFi or mobile data)");
                        return CompletableFuture.completedFuture(result);
                    }

                    // Step 2: Check IP reachability
                    if (callback != null) {
                        callback.onDiagnosticsProgress("Testing server reachability...");
                    }
                    
                    return validator.validateIpReachability(host)
                        .thenCompose(ipResult -> {
                            result.setIpReachable(ipResult.isSuccess());
                            if (!ipResult.isSuccess()) {
                                result.addIssue("Server reachability: " + ipResult.getMessage());
                                result.setRecommendedAction("Check if server IP address (" + host + ") is correct");
                                return CompletableFuture.completedFuture(result);
                            }

                            // Step 3: Check port availability
                            if (callback != null) {
                                callback.onDiagnosticsProgress("Testing server port...");
                            }
                            
                            return validator.validatePortAvailability(host, port)
                                .thenCompose(portResult -> {
                                    result.setPortOpen(portResult.isSuccess());
                                    if (!portResult.isSuccess()) {
                                        result.addIssue("Port availability: " + portResult.getMessage());
                                        result.setRecommendedAction("Check if server is running on port " + port);
                                        return CompletableFuture.completedFuture(result);
                                    }

                                    // Step 4: Check API endpoint
                                    if (callback != null) {
                                        callback.onDiagnosticsProgress("Testing API endpoint...");
                                    }
                                    
                                    return validator.validateApiEndpoint(baseUrl)
                                        .thenCompose(apiResult -> {
                                            result.setApiResponding(apiResult.isSuccess());
                                            result.setResponseTime(apiResult.getResponseTimeMs());
                                            
                                            if (!apiResult.isSuccess()) {
                                                result.addIssue("API endpoint: " + apiResult.getMessage());
                                                result.setRecommendedAction("Check API server status and configuration");
                                            } else {
                                                // Step 5: Test authentication if we have a token
                                                String token = TokenManager.getInstance(context).getToken();
                                                if (token != null && !token.isEmpty()) {
                                                    if (callback != null) {
                                                        callback.onDiagnosticsProgress("Testing authentication...");
                                                    }
                                                    
                                                    return validator.validateAuthentication(baseUrl, token)
                                                        .thenApply(authResult -> {
                                                            if (!authResult.isSuccess()) {
                                                                result.addIssue("Authentication: " + authResult.getMessage());
                                                            }
                                                            return result;
                                                        });
                                                } else {
                                                    result.setRecommendedAction("All network checks passed. API is accessible.");
                                                }
                                            }
                                            
                                            return CompletableFuture.completedFuture(result);
                                        });
                                });
                        });
                })
                .whenComplete((finalResult, throwable) -> {
                    if (throwable != null) {
                        Log.e(TAG, "Diagnostics failed", throwable);
                        result.addIssue("Diagnostics error: " + throwable.getMessage());
                        result.setRecommendedAction("Check network configuration and try again");
                    }
                    
                    lastResult = result;
                    
                    if (callback != null) {
                        callback.onDiagnosticsComplete(result);
                    }
                });

        } catch (Exception e) {
            Log.e(TAG, "Failed to parse base URL: " + baseUrl, e);
            result.addIssue("Invalid API URL configuration: " + e.getMessage());
            result.setRecommendedAction("Check API base URL configuration");
            lastResult = result;
            
            if (callback != null) {
                callback.onDiagnosticsComplete(result);
            }
        }
    }

    public void runQuickConnectivityCheck(Context context, ConnectivityCallback callback) {
        validator.validateNetworkConnectivity(context)
            .thenCompose(networkResult -> {
                if (!networkResult.isSuccess()) {
                    return CompletableFuture.completedFuture(networkResult);
                }
                
                // If network is available, do a quick API endpoint check
                String baseUrl = context.getString(R.string.api_base_url);
                return validator.validateApiEndpoint(baseUrl);
            })
            .whenComplete((result, throwable) -> {
                if (callback != null) {
                    if (throwable != null) {
                        callback.onConnectivityResult(false, "Connectivity check failed: " + throwable.getMessage());
                    } else {
                        callback.onConnectivityResult(result.isSuccess(), result.getMessage());
                    }
                }
            });
    }

    public DiagnosticsResult getLastDiagnosticsResult() {
        return lastResult;
    }

    public void shutdown() {
        if (validator != null) {
            validator.shutdown();
        }
    }
}