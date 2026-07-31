package com.example.myrajourney.core.network;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * Helper class to easily run network diagnostics and handle common network errors
 */
public class NetworkDiagnosticsHelper {
    private static final String TAG = "NetworkDiagnosticsHelper";

    /**
     * Show a quick network diagnostic dialog
     */
    public static void showQuickDiagnostics(Context context) {
        NetworkDiagnosticsManager.getInstance().runQuickConnectivityCheck(context, 
            new NetworkDiagnosticsManager.ConnectivityCallback() {
                @Override
                public void onConnectivityResult(boolean isConnected, String message) {
                    if (isConnected) {
                        Toast.makeText(context, "✅ Network is working: " + message, Toast.LENGTH_SHORT).show();
                    } else {
                        new AlertDialog.Builder(context)
                            .setTitle("Network Issue")
                            .setMessage("❌ " + message + "\n\nWould you like to run full diagnostics?")
                            .setPositiveButton("Run Diagnostics", (dialog, which) -> {
                                openFullDiagnostics(context);
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                    }
                }
            });
    }

    /**
     * Open the full network diagnostics activity
     */
    public static void openFullDiagnostics(Context context) {
        Intent intent = new Intent(context, NetworkDiagnosticsActivity.class);
        context.startActivity(intent);
    }

    /**
     * Handle API errors with appropriate user feedback and diagnostic options
     */
    public static void handleApiError(Context context, Throwable error) {
        ErrorHandlingManager.getInstance().handleApiError(error, context, 
            new ErrorHandlingManager.ErrorCallback() {
                @Override
                public void onErrorResolved() {
                    Toast.makeText(context, "Error resolved", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onRetryRequested() {
                    Toast.makeText(context, "Retrying request...", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onUserActionRequired(String action) {
                    if ("diagnostics".equals(action)) {
                        openFullDiagnostics(context);
                    } else if ("login".equals(action)) {
                        // Handle login redirect
                        Toast.makeText(context, "Please log in again", Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }

    /**
     * Check if the current network configuration is working
     */
    public static void validateCurrentConfiguration(Context context, ValidationCallback callback) {
        NetworkDiagnosticsManager.getInstance().runFullDiagnostics(context, 
            new NetworkDiagnosticsManager.DiagnosticsCallback() {
                @Override
                public void onDiagnosticsComplete(DiagnosticsResult result) {
                    callback.onValidationComplete(result.isAllHealthy(), result.getSummary());
                }

                @Override
                public void onDiagnosticsProgress(String message) {
                    // Optional: show progress
                }
            });
    }

    public interface ValidationCallback {
        void onValidationComplete(boolean isValid, String message);
    }

    /**
     * Show network status information
     */
    public static void showNetworkStatus(Context context) {
        String networkInfo = NetworkUtils.getNetworkStatusDescription(context);
        String detailedInfo = NetworkUtils.getNetworkInfo(context);
        
        new AlertDialog.Builder(context)
            .setTitle("Network Status")
            .setMessage(networkInfo + "\n\n" + detailedInfo)
            .setPositiveButton("OK", null)
            .setNeutralButton("Run Diagnostics", (dialog, which) -> {
                openFullDiagnostics(context);
            })
            .show();
    }

    /**
     * Quick method to log current network configuration for debugging
     */
    public static void logNetworkConfiguration(Context context) {
        try {
            String baseUrl = ApiClient.getCurrentBaseUrl(context);
            String networkStatus = NetworkUtils.getNetworkStatusDescription(context);
            
            Log.i(TAG, "=== NETWORK CONFIGURATION ===");
            Log.i(TAG, "Base URL: " + baseUrl);
            Log.i(TAG, "Network Status: " + networkStatus);
            Log.i(TAG, "API Configured: " + ApiClient.isConfigured(context));
            Log.i(TAG, "=============================");
        } catch (Exception e) {
            Log.e(TAG, "Failed to log network configuration", e);
        }
    }
}