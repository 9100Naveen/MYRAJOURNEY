package com.example.myrajourney.core.network;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;

public class ErrorLoggingInterceptor implements Interceptor {
    private static final String TAG = "ErrorLoggingInterceptor";
    private static final Charset UTF8 = StandardCharsets.UTF_8;

    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        long startTime = System.currentTimeMillis();
        
        Response response;
        try {
            response = chain.proceed(request);
        } catch (Exception e) {
            // Log network exceptions with detailed request information
            logNetworkException(request, e, System.currentTimeMillis() - startTime);
            throw e;
        }
        
        long responseTime = System.currentTimeMillis() - startTime;
        
        // Log errors for HTTP error codes
        if (!response.isSuccessful()) {
            logHttpError(request, response, responseTime);
        } else {
            // Log successful requests for debugging (only in debug builds)
            logSuccessfulRequest(request, response, responseTime);
        }
        
        return response;
    }

    private void logNetworkException(Request request, Exception exception, long responseTime) {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("NETWORK EXCEPTION\n");
        logMessage.append("URL: ").append(request.url()).append("\n");
        logMessage.append("Method: ").append(request.method()).append("\n");
        logMessage.append("Response Time: ").append(responseTime).append("ms\n");
        logMessage.append("Exception: ").append(exception.getClass().getSimpleName()).append("\n");
        logMessage.append("Message: ").append(exception.getMessage()).append("\n");
        
        // Log request headers
        logMessage.append("Request Headers:\n");
        for (String name : request.headers().names()) {
            // Don't log sensitive headers
            if (!isSensitiveHeader(name)) {
                logMessage.append("  ").append(name).append(": ").append(request.headers().get(name)).append("\n");
            }
        }
        
        // Log request body for POST/PUT requests (but not sensitive data)
        if (request.body() != null && shouldLogRequestBody(request)) {
            try {
                String requestBody = getRequestBodyString(request);
                if (requestBody != null && !requestBody.isEmpty()) {
                    logMessage.append("Request Body: ").append(requestBody).append("\n");
                }
            } catch (Exception e) {
                logMessage.append("Request Body: [Could not read body]\n");
            }
        }
        
        Log.e(TAG, logMessage.toString(), exception);
    }

    private void logHttpError(Request request, Response response, long responseTime) {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("HTTP ERROR ").append(response.code()).append("\n");
        logMessage.append("URL: ").append(request.url()).append("\n");
        logMessage.append("Method: ").append(request.method()).append("\n");
        logMessage.append("Response Time: ").append(responseTime).append("ms\n");
        
        // Log request headers
        logMessage.append("Request Headers:\n");
        for (String name : request.headers().names()) {
            if (!isSensitiveHeader(name)) {
                logMessage.append("  ").append(name).append(": ").append(request.headers().get(name)).append("\n");
            }
        }
        
        // Log response headers
        logMessage.append("Response Headers:\n");
        for (String name : response.headers().names()) {
            logMessage.append("  ").append(name).append(": ").append(response.headers().get(name)).append("\n");
        }
        
        // Log request body for error analysis
        if (request.body() != null && shouldLogRequestBody(request)) {
            try {
                String requestBody = getRequestBodyString(request);
                if (requestBody != null && !requestBody.isEmpty()) {
                    logMessage.append("Request Body: ").append(requestBody).append("\n");
                }
            } catch (Exception e) {
                logMessage.append("Request Body: [Could not read body]\n");
            }
        }
        
        // Log response body for error details
        try {
            ResponseBody responseBody = response.body();
            if (responseBody != null) {
                MediaType contentType = responseBody.contentType();
                if (contentType != null) {
                    Charset charset = contentType.charset(UTF8);
                    if (charset != null) {
                        String responseBodyString = responseBody.string();
                        logMessage.append("Response Body: ").append(responseBodyString).append("\n");
                        
                        // Create new response body since we consumed the original
                        ResponseBody newResponseBody = ResponseBody.create(responseBodyString, contentType);
                        response = response.newBuilder().body(newResponseBody).build();
                    }
                }
            }
        } catch (Exception e) {
            logMessage.append("Response Body: [Could not read body]\n");
        }
        
        Log.e(TAG, logMessage.toString());
    }

    private void logSuccessfulRequest(Request request, Response response, long responseTime) {
        // Only log successful requests in debug mode and for slow requests
        if (responseTime > 2000) { // Log requests that take more than 2 seconds
            Log.d(TAG, "SLOW REQUEST: " + request.method() + " " + request.url() + 
                  " (" + responseTime + "ms) -> HTTP " + response.code());
        }
    }

    private boolean isSensitiveHeader(String headerName) {
        String name = headerName.toLowerCase();
        return name.equals("authorization") || 
               name.equals("cookie") || 
               name.equals("set-cookie") ||
               name.contains("token") ||
               name.contains("password");
    }

    private boolean shouldLogRequestBody(Request request) {
        String method = request.method();
        return "POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method);
    }

    private String getRequestBodyString(Request request) {
        try {
            RequestBody requestBody = request.body();
            if (requestBody == null) {
                return null;
            }
            
            Buffer buffer = new Buffer();
            requestBody.writeTo(buffer);
            
            MediaType contentType = requestBody.contentType();
            Charset charset = contentType != null ? contentType.charset(UTF8) : UTF8;
            
            String bodyString = buffer.readString(charset);
            
            // Don't log sensitive data
            if (containsSensitiveData(bodyString)) {
                return "[Request body contains sensitive data]";
            }
            
            return bodyString;
        } catch (Exception e) {
            return "[Could not read request body: " + e.getMessage() + "]";
        }
    }

    private boolean containsSensitiveData(String body) {
        if (body == null) {
            return false;
        }
        
        String lowerBody = body.toLowerCase();
        return lowerBody.contains("password") || 
               lowerBody.contains("token") ||
               lowerBody.contains("secret") ||
               lowerBody.contains("key");
    }
}