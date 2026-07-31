package com.example.myrajourney.debug;

import android.content.Context;
import android.util.Log;

import com.example.myrajourney.core.network.NetworkConfigHelper;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SimpleNetworkTest {
    private static final String TAG = "SimpleNetworkTest";
    
    public interface TestCallback {
        void onResult(boolean success, String message);
    }
    
    public static void testConnection(Context context, TestCallback callback) {
        String baseUrl = NetworkConfigHelper.getServerUrl(context);
        String testUrl = baseUrl + "/api/v1/auth/login";
        
        Log.d(TAG, "Testing connection to: " + testUrl);
        
        // Create a simple OkHttp client with minimal configuration
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
        
        // Create test request
        String json = "{\"email\":\"test\",\"password\":\"test\"}";
        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
        
        Request request = new Request.Builder()
                .url(testUrl)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Connection", "close")
                .build();
        
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String error = "Connection failed: " + e.getMessage();
                Log.e(TAG, error, e);
                callback.onResult(false, error);
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    int code = response.code();
                    String responseBody = response.body() != null ? response.body().string() : "No body";
                    
                    String message = "Connection successful! HTTP " + code + "\nResponse: " + responseBody.substring(0, Math.min(100, responseBody.length()));
                    Log.d(TAG, message);
                    callback.onResult(true, message);
                } finally {
                    response.close();
                }
            }
        });
    }
    
    public static void testMedicationAssignment(Context context, String token, TestCallback callback) {
        String baseUrl = NetworkConfigHelper.getServerUrl(context);
        String testUrl = baseUrl + "/api/v1/patient-medications";
        
        Log.d(TAG, "Testing medication assignment to: " + testUrl);
        
        // Create a simple OkHttp client
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
        
        // Create unique medication
        String uniqueName = "AndroidTest" + System.currentTimeMillis();
        String json = String.format(
            "{\"patient_id\":25,\"name_override\":\"%s\",\"dosage\":\"5mg\",\"frequency_per_day\":1,\"start_date\":\"%s\"}",
            uniqueName, java.time.LocalDate.now().toString()
        );
        
        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
        
        Request request = new Request.Builder()
                .url(testUrl)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Connection", "close")
                .build();
        
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String error = "Medication assignment failed: " + e.getMessage();
                Log.e(TAG, error, e);
                callback.onResult(false, error);
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    int code = response.code();
                    String responseBody = response.body() != null ? response.body().string() : "No body";
                    
                    boolean success = code == 201;
                    String message = String.format("Medication assignment: HTTP %d\nMedication: %s\nResponse: %s", 
                            code, uniqueName, responseBody.substring(0, Math.min(200, responseBody.length())));
                    
                    Log.d(TAG, message);
                    callback.onResult(success, message);
                } finally {
                    response.close();
                }
            }
        });
    }
}