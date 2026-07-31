package com.example.myrajourney.core.network;

import android.content.Context;
import android.util.Log;

import com.example.myrajourney.data.model.ApiResponse;
import com.example.myrajourney.data.model.Symptom;
import com.example.myrajourney.data.model.SymptomRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Helper class to test symptom creation and diagnose connection issues
 */
public class SymptomTestHelper {
    private static final String TAG = "SymptomTestHelper";

    public static void testSymptomCreation(Context context) {
        Log.i(TAG, "Testing symptom creation endpoint...");
        
        // Create a test symptom request
        SymptomRequest testRequest = new SymptomRequest("1", "2024-12-16", 5);
        testRequest.setStiffnessLevel(4);
        testRequest.setFatigueLevel(3);
        testRequest.setNotes("Network connection test");
        
        ApiService api = ApiClient.getApiService(context);
        
        api.createSymptom(testRequest).enqueue(new Callback<ApiResponse<Symptom>>() {
            @Override
            public void onResponse(Call<ApiResponse<Symptom>> call, Response<ApiResponse<Symptom>> response) {
                if (response.isSuccessful()) {
                    Log.i(TAG, "✅ Symptom creation test successful: HTTP " + response.code());
                    if (response.body() != null) {
                        Log.i(TAG, "Response: " + response.body().getMessage());
                    }
                } else {
                    Log.w(TAG, "⚠️ Symptom creation returned HTTP " + response.code());
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.w(TAG, "Error body: " + errorBody);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to read error body", e);
                    }
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Symptom>> call, Throwable t) {
                Log.e(TAG, "❌ Symptom creation test failed: " + t.getMessage(), t);
                
                // Analyze the specific error
                String errorMessage = t.getMessage();
                if (errorMessage != null) {
                    if (errorMessage.toLowerCase().contains("closed")) {
                        Log.e(TAG, "🔍 CONNECTION CLOSED ERROR DETECTED!");
                        Log.e(TAG, "This indicates the server is closing the connection prematurely.");
                        Log.e(TAG, "Possible causes:");
                        Log.e(TAG, "1. Server timeout settings too low");
                        Log.e(TAG, "2. HTTP/2 compatibility issues");
                        Log.e(TAG, "3. Connection pooling problems");
                        Log.e(TAG, "4. Server overload or restart");
                    } else if (errorMessage.toLowerCase().contains("timeout")) {
                        Log.e(TAG, "🔍 TIMEOUT ERROR DETECTED!");
                        Log.e(TAG, "The request is taking too long to complete.");
                    } else if (errorMessage.toLowerCase().contains("refused")) {
                        Log.e(TAG, "🔍 CONNECTION REFUSED ERROR DETECTED!");
                        Log.e(TAG, "The server is not accepting connections on the specified port.");
                    }
                }
                
                // Trigger enhanced error handling
                NetworkDiagnosticsHelper.handleApiError(context, t);
            }
        });
    }

    public static void testSymptomRetrieval(Context context) {
        Log.i(TAG, "Testing symptom retrieval endpoint...");
        
        ApiService api = ApiClient.getApiService(context);
        
        api.getSymptoms().enqueue(new Callback<ApiResponse<java.util.List<Symptom>>>() {
            @Override
            public void onResponse(Call<ApiResponse<java.util.List<Symptom>>> call, 
                                 Response<ApiResponse<java.util.List<Symptom>>> response) {
                if (response.isSuccessful()) {
                    Log.i(TAG, "✅ Symptom retrieval test successful: HTTP " + response.code());
                    if (response.body() != null && response.body().getData() != null) {
                        Log.i(TAG, "Retrieved " + response.body().getData().size() + " symptoms");
                    }
                } else {
                    Log.w(TAG, "⚠️ Symptom retrieval returned HTTP " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<java.util.List<Symptom>>> call, Throwable t) {
                Log.e(TAG, "❌ Symptom retrieval test failed: " + t.getMessage(), t);
            }
        });
    }
}