package com.example.myrajourney.core.network;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Specific interceptor to handle PHP built-in server connection issues
 */
public class PhpServerFixInterceptor implements Interceptor {
    private static final String TAG = "PhpServerFixInterceptor";

    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        
        // Special handling for POST requests (like symptom creation)
        if ("POST".equals(originalRequest.method())) {
            Log.d(TAG, "Applying PHP server fix for POST request: " + originalRequest.url());
            
            Request request = originalRequest.newBuilder()
                    // Ensure proper JSON content type for POST
                    .addHeader("Content-Type", "application/json; charset=utf-8")
                    .addHeader("Accept", "application/json")
                    .build();
            
            try {
                Response response = chain.proceed(request);
                Log.d(TAG, "POST request successful: " + response.code());
                return response;
            } catch (IOException e) {
                Log.e(TAG, "POST request failed: " + e.getMessage());
                
                // For connection closed errors, let the RetryInterceptor handle it
                if (e.getMessage() != null && e.getMessage().toLowerCase().contains("closed")) {
                    Log.w(TAG, "Connection closed error detected, will be handled by RetryInterceptor");
                }
                
                throw e;
            }
        }
        
        // For non-POST requests, use standard handling
        return chain.proceed(originalRequest);
    }
}