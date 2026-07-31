package com.example.myrajourney.core.network;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Interceptor to fix common connection issues like "connection closed" errors
 */
public class ConnectionFixInterceptor implements Interceptor {
    private static final String TAG = "ConnectionFixInterceptor";

    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        
        // Add basic headers for server compatibility
        Request request = originalRequest.newBuilder()
                .addHeader("Cache-Control", "no-cache")
                .addHeader("User-Agent", "MyRAJourney-Android/1.0")
                .build();

        try {
            Response response = chain.proceed(request);
            
            // Log successful requests for debugging
            Log.d(TAG, "Request successful: " + request.method() + " " + request.url() + 
                  " -> HTTP " + response.code());
            
            return response;
            
        } catch (IOException e) {
            String errorMessage = e.getMessage();
            
            if (errorMessage != null && errorMessage.toLowerCase().contains("closed")) {
                Log.w(TAG, "Connection closed error detected for: " + request.url() + 
                      ". Error: " + errorMessage);
                
                // For connection closed errors, we'll let the RetryInterceptor handle the retry
                // but we log it specifically for debugging
            }
            
            throw e;
        }
    }
}