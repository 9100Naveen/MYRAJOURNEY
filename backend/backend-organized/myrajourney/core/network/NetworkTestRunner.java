package com.example.myrajourney.core.network;

import android.content.Context;
import android.util.Log;

/**
 * Standalone network test runner to diagnose and fix network issues
 */
public class NetworkTestRunner {
    private static final String TAG = "NetworkTestRunner";

    public static void runImmediateDiagnostics(Context context) {
        Log.i(TAG, "=== STARTING IMMEDIATE NETWORK DIAGNOSTICS ===");
        
        // Log current configuration
        NetworkDiagnosticsHelper.logNetworkConfiguration(context);
        
        // Run full diagnostics
        NetworkDiagnosticsManager.getInstance().runFullDiagnostics(context, 
            new NetworkDiagnosticsManager.DiagnosticsCallback() {
                @Override
                public void onDiagnosticsComplete(DiagnosticsResult result) {
                    Log.i(TAG, "=== DIAGNOSTICS COMPLETE ===");
                    Log.i(TAG, "All Healthy: " + result.isAllHealthy());
                    Log.i(TAG, "Network Available: " + result.isNetworkAvailable());
                    Log.i(TAG, "IP Reachable: " + result.isIpReachable());
                    Log.i(TAG, "Port Open: " + result.isPortOpen());
                    Log.i(TAG, "API Responding: " + result.isApiResponding());
                    
                    if (result.getResponseTime() > 0) {
                        Log.i(TAG, "Response Time: " + result.getResponseTime() + "ms");
                    }
                    
                    Log.i(TAG, "Current IP: " + result.getCurrentIpAddress());
                    Log.i(TAG, "Current Port: " + result.getCurrentPort());
                    Log.i(TAG, "API URL: " + result.getApiBaseUrl());
                    
                    if (!result.getIssues().isEmpty()) {
                        Log.w(TAG, "Issues found:");
                        for (String issue : result.getIssues()) {
                            Log.w(TAG, "  - " + issue);
                        }
                    }
                    
                    if (result.getRecommendedAction() != null) {
                        Log.i(TAG, "Recommended Action: " + result.getRecommendedAction());
                    }
                    
                    Log.i(TAG, "Summary: " + result.getSummary());
                    Log.i(TAG, "=== END DIAGNOSTICS ===");
                }
                
                @Override
                public void onDiagnosticsProgress(String message) {
                    Log.i(TAG, "Progress: " + message);
                }
            });
    }

    public static void testSpecificEndpoint(Context context, String endpoint) {
        Log.i(TAG, "Testing specific endpoint: " + endpoint);
        
        ApiService api = ApiClient.getApiService(context);
        
        // Test the auth/me endpoint which should return 401 if working
        api.getCurrentUser().enqueue(new retrofit2.Callback<com.example.myrajourney.data.model.ApiResponse<com.example.myrajourney.data.model.UserResponse>>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.myrajourney.data.model.ApiResponse<com.example.myrajourney.data.model.UserResponse>> call, 
                                 retrofit2.Response<com.example.myrajourney.data.model.ApiResponse<com.example.myrajourney.data.model.UserResponse>> response) {
                Log.i(TAG, "Endpoint test response: HTTP " + response.code());
                if (response.code() == 401) {
                    Log.i(TAG, "✅ API is working (401 Unauthorized is expected without token)");
                } else if (response.code() == 200) {
                    Log.i(TAG, "✅ API is working (200 OK)");
                } else {
                    Log.w(TAG, "⚠️ Unexpected response code: " + response.code());
                }
            }
            
            @Override
            public void onFailure(retrofit2.Call<com.example.myrajourney.data.model.ApiResponse<com.example.myrajourney.data.model.UserResponse>> call, 
                                Throwable t) {
                Log.e(TAG, "❌ Endpoint test failed: " + t.getMessage(), t);
                
                // Analyze the error
                if (t instanceof java.net.ConnectException) {
                    Log.e(TAG, "Connection refused - server not running or wrong IP/port");
                } else if (t instanceof java.net.SocketTimeoutException) {
                    Log.e(TAG, "Connection timeout - server too slow or network issues");
                } else if (t instanceof java.net.UnknownHostException) {
                    Log.e(TAG, "Unknown host - DNS resolution failed or wrong hostname");
                } else if (t instanceof retrofit2.HttpException) {
                    retrofit2.HttpException httpEx = (retrofit2.HttpException) t;
                    Log.e(TAG, "HTTP error: " + httpEx.code() + " - " + httpEx.message());
                    
                    if (httpEx.code() == 400) {
                        Log.e(TAG, "🔍 400 Bad Request detected! This is likely the issue you're experiencing.");
                        try {
                            String errorBody = httpEx.response().errorBody().string();
                            Log.e(TAG, "Error response body: " + errorBody);
                        } catch (Exception e) {
                            Log.e(TAG, "Could not read error body", e);
                        }
                    }
                }
            }
        });
    }
}