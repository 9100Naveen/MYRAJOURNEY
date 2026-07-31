package com.example.myrajourney.core.network;

import android.content.Context;
import android.util.Log;

import com.example.myrajourney.core.session.TokenManager;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Simple API client that bypasses complex interceptors to avoid connection issues
 */
public class SimpleApiClient {
    private static final String TAG = "SimpleApiClient";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
    
    public static void assignMedication(Context context, Map<String, Object> medicationData, ApiCallback<String> callback) {
        String baseUrl = NetworkConfigHelper.getServerUrl(context);
        String url = baseUrl + "/api/v1/patient-medications";
        String token = TokenManager.getInstance(context).getToken();
        
        if (token == null) {
            callback.onError("No authentication token found");
            return;
        }
        
        Log.d(TAG, "Assigning medication to: " + url);
        Log.d(TAG, "Data: " + medicationData.toString());
        
        // Create simple OkHttp client with minimal configuration
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(45, TimeUnit.SECONDS)
                .readTimeout(45, TimeUnit.SECONDS)
                .writeTimeout(45, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
        
        // Convert data to JSON
        String json = new Gson().toJson(medicationData);
        RequestBody body = RequestBody.create(json, JSON);
        
        // Build request with fresh connection headers
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Connection", "close")
                .addHeader("Cache-Control", "no-cache")
                .addHeader("User-Agent", "MyRAJourney-Simple/1.0")
                .build();
        
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String error = "Network error: " + e.getMessage();
                Log.e(TAG, error, e);
                callback.onError(error);
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    int code = response.code();
                    String responseBody = response.body() != null ? response.body().string() : "{}";
                    
                    Log.d(TAG, "Response code: " + code);
                    Log.d(TAG, "Response body: " + responseBody);
                    
                    if (code == 201) {
                        callback.onSuccess("Medication assigned successfully!");
                    } else if (code == 409) {
                        // Parse error message from response
                        try {
                            Map<String, Object> errorResponse = new Gson().fromJson(responseBody, Map.class);
                            Map<String, Object> error = (Map<String, Object>) errorResponse.get("error");
                            String message = error != null ? (String) error.get("message") : "Duplicate medication";
                            callback.onError("Duplicate: " + message);
                        } catch (Exception e) {
                            callback.onError("This medication already exists for this patient");
                        }
                    } else if (code == 403) {
                        callback.onError("Access denied. Please check your permissions.");
                    } else if (code >= 500) {
                        callback.onError("Server error. Please try again later.");
                    } else {
                        callback.onError("Assignment failed with HTTP " + code);
                    }
                } finally {
                    response.close();
                }
            }
        });
    }
    
    public static void testConnection(Context context, ApiCallback<String> callback) {
        String baseUrl = NetworkConfigHelper.getServerUrl(context);
        String url = baseUrl + "/api/v1/auth/login";
        
        Log.d(TAG, "Testing connection to: " + url);
        
        // Create simple client
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
        
        // Test request
        String json = "{\"email\":\"test\",\"password\":\"test\"}";
        RequestBody body = RequestBody.create(json, JSON);
        
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Connection", "close")
                .build();
        
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String error = "Connection test failed: " + e.getMessage();
                Log.e(TAG, error, e);
                callback.onError(error);
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    int code = response.code();
                    String message = "Connection test successful! Server responded with HTTP " + code;
                    Log.d(TAG, message);
                    callback.onSuccess(message);
                } finally {
                    response.close();
                }
            }
        });
    }
}