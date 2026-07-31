package com.example.myrajourney.core.network;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;

import retrofit2.HttpException;

public class ErrorHandlingManager {
    private static final String TAG = "ErrorHandlingManager";
    private static ErrorHandlingManager instance;
    private final Gson gson;

    public interface ErrorCallback {
        void onErrorResolved();
        void onRetryRequested();
        void onUserActionRequired(String action);
    }

    private ErrorHandlingManager() {
        this.gson = new Gson();
    }

    public static synchronized ErrorHandlingManager getInstance() {
        if (instance == null) {
            instance = new ErrorHandlingManager();
        }
        return instance;
    }

    public void handleApiError(Throwable error, Context context, ErrorCallback callback) {
        ErrorInformation errorInfo = createErrorInformation(error);
        
        Log.e(TAG, "API Error: " + errorInfo.getDetailedErrorMessage(), error);
        
        // Show appropriate error handling based on error type
        if (errorInfo.getNetworkError() == NetworkError.NO_INTERNET_CONNECTION) {
            handleNoInternetError(context, callback);
        } else if (errorInfo.getNetworkError() == NetworkError.AUTHENTICATION_FAILED ||
                   errorInfo.getNetworkError() == NetworkError.UNAUTHORIZED_401) {
            handleAuthenticationError(context, callback);
        } else if (errorInfo.getNetworkError() == NetworkError.BAD_REQUEST_400) {
            handleBadRequestError(context, errorInfo, callback);
        } else if (errorInfo.getNetworkError() == NetworkError.SERVER_UNREACHABLE ||
                   errorInfo.getNetworkError() == NetworkError.CONNECTION_TIMEOUT) {
            handleServerUnreachableError(context, errorInfo, callback);
        } else {
            handleGenericError(context, errorInfo, callback);
        }
    }

    private ErrorInformation createErrorInformation(Throwable error) {
        ErrorInformation errorInfo;
        
        if (error instanceof HttpException) {
            HttpException httpException = (HttpException) error;
            int code = httpException.code();
            String message = extractErrorMessage(httpException);
            
            errorInfo = new ErrorInformation(code, message);
            errorInfo.setRequestUrl(httpException.response() != null ? 
                httpException.response().raw().request().url().toString() : null);
            
            try {
                if (httpException.response() != null && httpException.response().errorBody() != null) {
                    String responseBody = httpException.response().errorBody().string();
                    errorInfo.setResponseBody(responseBody);
                }
            } catch (IOException e) {
                Log.w(TAG, "Failed to read error response body", e);
            }
        } else {
            errorInfo = new ErrorInformation(error);
        }
        
        return errorInfo;
    }

    private String extractErrorMessage(HttpException httpException) {
        try {
            if (httpException.response() != null && httpException.response().errorBody() != null) {
                String errorBody = httpException.response().errorBody().string();
                
                // Try to parse JSON error response
                try {
                    JsonObject jsonError = gson.fromJson(errorBody, JsonObject.class);
                    if (jsonError.has("message")) {
                        return jsonError.get("message").getAsString();
                    } else if (jsonError.has("error")) {
                        return jsonError.get("error").getAsString();
                    }
                } catch (Exception e) {
                    // If JSON parsing fails, return the raw error body
                    return errorBody;
                }
            }
        } catch (IOException e) {
            Log.w(TAG, "Failed to extract error message", e);
        }
        
        return "HTTP " + httpException.code() + " error";
    }

    private void handleNoInternetError(Context context, ErrorCallback callback) {
        new AlertDialog.Builder(context)
            .setTitle("No Internet Connection")
            .setMessage("Please check your WiFi or mobile data connection and try again.")
            .setPositiveButton("Retry", (dialog, which) -> {
                if (callback != null) {
                    callback.onRetryRequested();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void handleAuthenticationError(Context context, ErrorCallback callback) {
        new AlertDialog.Builder(context)
            .setTitle("Session Expired")
            .setMessage("Your session has expired. Please log in again to continue.")
            .setPositiveButton("Login", (dialog, which) -> {
                if (callback != null) {
                    callback.onUserActionRequired("login");
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void handleBadRequestError(Context context, ErrorInformation errorInfo, ErrorCallback callback) {
        String message = errorInfo.getUserFriendlyMessage();
        if (errorInfo.getResponseBody() != null) {
            try {
                JsonObject jsonError = gson.fromJson(errorInfo.getResponseBody(), JsonObject.class);
                if (jsonError.has("message")) {
                    message = jsonError.get("message").getAsString();
                }
            } catch (Exception e) {
                // Use default message if JSON parsing fails
            }
        }

        new AlertDialog.Builder(context)
            .setTitle("Request Error")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .setNeutralButton("Details", (dialog, which) -> {
                showErrorDetails(context, errorInfo);
            })
            .show();
    }

    private void handleServerUnreachableError(Context context, ErrorInformation errorInfo, ErrorCallback callback) {
        new AlertDialog.Builder(context)
            .setTitle("Connection Problem")
            .setMessage("Cannot connect to the server. This might be a network configuration issue.")
            .setPositiveButton("Retry", (dialog, which) -> {
                if (callback != null) {
                    callback.onRetryRequested();
                }
            })
            .setNeutralButton("Run Diagnostics", (dialog, which) -> {
                if (callback != null) {
                    callback.onUserActionRequired("diagnostics");
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void handleGenericError(Context context, ErrorInformation errorInfo, ErrorCallback callback) {
        new AlertDialog.Builder(context)
            .setTitle(errorInfo.getNetworkError().getTitle())
            .setMessage(errorInfo.getUserFriendlyMessage())
            .setPositiveButton(errorInfo.isRetryable() ? "Retry" : "OK", (dialog, which) -> {
                if (errorInfo.isRetryable() && callback != null) {
                    callback.onRetryRequested();
                }
            })
            .setNeutralButton("Details", (dialog, which) -> {
                showErrorDetails(context, errorInfo);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showErrorDetails(Context context, ErrorInformation errorInfo) {
        new AlertDialog.Builder(context)
            .setTitle("Error Details")
            .setMessage(errorInfo.getDetailedErrorMessage())
            .setPositiveButton("OK", null)
            .show();
    }

    public void showNetworkErrorDialog(Context context, NetworkError error) {
        new AlertDialog.Builder(context)
            .setTitle(error.getTitle())
            .setMessage(error.getMessage())
            .setPositiveButton("OK", null)
            .show();
    }

    public boolean shouldRetryRequest(Throwable error, int attemptCount) {
        if (attemptCount >= 3) {
            return false; // Max 3 attempts
        }

        ErrorInformation errorInfo = createErrorInformation(error);
        return errorInfo.isRetryable();
    }

    public long getRetryDelay(int attemptCount) {
        // Exponential backoff: 1s, 2s, 4s
        return (long) Math.pow(2, attemptCount - 1) * 1000;
    }

    public void showQuickErrorToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public void showQuickErrorToast(Context context, Throwable error) {
        ErrorInformation errorInfo = createErrorInformation(error);
        Toast.makeText(context, errorInfo.getUserFriendlyMessage(), Toast.LENGTH_SHORT).show();
    }
}