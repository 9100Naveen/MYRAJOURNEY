package com.example.myrajourney.core.network;

import android.content.Context;
import android.util.Log;

import com.example.myrajourney.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String TAG = "ApiClient";
    private static volatile Retrofit retrofit = null;
    private static volatile ApiService apiService = null;
    private static String currentBaseUrl = null;

    public static Retrofit getRetrofit(Context context) {
        String baseUrl = NetworkConfigHelper.getApiBaseUrl(context);
        
        // Recreate retrofit if base URL changed
        if (retrofit == null || !baseUrl.equals(currentBaseUrl)) {
            synchronized (ApiClient.class) {
                if (retrofit == null || !baseUrl.equals(currentBaseUrl)) {
                    Log.i(TAG, "Creating new Retrofit instance with base URL: " + baseUrl);
                    
                    currentBaseUrl = baseUrl;
                    
                    Gson gson = new GsonBuilder()
                            .setLenient()
                            .create();

                    // Create HTTP logging interceptor
                    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                    logging.setLevel(HttpLoggingInterceptor.Level.BODY);

                    // Build OkHttp client with optimized settings for PHP server
                    OkHttpClient client = new OkHttpClient.Builder()
                            // Add PHP server fix interceptor first
                            .addInterceptor(new PhpServerFixInterceptor())
                            
                            // Add connection fix interceptor
                            .addInterceptor(new ConnectionFixInterceptor())
                            
                            // Add authentication interceptor
                            .addInterceptor(new AuthInterceptor(context))
                            
                            // Add error logging interceptor
                            .addInterceptor(new ErrorLoggingInterceptor())
                            
                            // Add retry interceptor
                            .addInterceptor(new RetryInterceptor())
                            
                            // Add HTTP logging interceptor last
                            .addInterceptor(logging)
                            
                            // Configure longer timeouts to prevent connection closed errors
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(30, TimeUnit.SECONDS)
                            .writeTimeout(30, TimeUnit.SECONDS)
                            .callTimeout(60, TimeUnit.SECONDS)
                            
                            // Configure retry on connection failure
                            .retryOnConnectionFailure(true)
                            
                            // Disable connection pooling to prevent connection reuse issues
                            .connectionPool(new okhttp3.ConnectionPool(0, 1, TimeUnit.NANOSECONDS))
                            
                            // Force HTTP/1.1 to avoid HTTP/2 issues
                            .protocols(java.util.Arrays.asList(
                                okhttp3.Protocol.HTTP_1_1
                            ))
                            
                            // Add headers for better PHP server compatibility
                            .addNetworkInterceptor(chain -> {
                                okhttp3.Request request = chain.request().newBuilder()
                                    .addHeader("Connection", "close")  // Force connection close
                                    .addHeader("Cache-Control", "no-cache")
                                    .addHeader("User-Agent", "MyRAJourney-Android/1.0")
                                    .build();
                                return chain.proceed(request);
                            })
                            
                            .build();

                    retrofit = new Retrofit.Builder()
                            .baseUrl(baseUrl)
                            .addConverterFactory(GsonConverterFactory.create(gson))
                            .client(client)
                            .build();
                    
                    // Reset API service when retrofit changes
                    apiService = null;
                }
            }
        }
        return retrofit;
    }

    public static ApiService getApiService(Context context) {
        if (apiService == null) {
            synchronized (ApiClient.class) {
                if (apiService == null) {
                    apiService = getRetrofit(context).create(ApiService.class);
                }
            }
        }
        return apiService;
    }

    /**
     * Force recreation of the Retrofit instance with a new base URL
     */
    public static void updateBaseUrl(Context context, String newBaseUrl) {
        synchronized (ApiClient.class) {
            Log.i(TAG, "Updating base URL from " + currentBaseUrl + " to " + newBaseUrl);
            
            // Update the configuration
            // Note: This would typically update SharedPreferences or the network_config.xml
            // For now, we'll just force recreation on next getRetrofit call
            currentBaseUrl = null;
            retrofit = null;
            apiService = null;
        }
    }

    /**
     * Get the current base URL being used
     */
    public static String getCurrentBaseUrl(Context context) {
        if (currentBaseUrl == null) {
            currentBaseUrl = NetworkConfigHelper.getApiBaseUrl(context);
        }
        return currentBaseUrl;
    }

    /**
     * Check if the API client is properly configured
     */
    public static boolean isConfigured(Context context) {
        return NetworkConfigHelper.isConfigurationValid(context);
    }

    /**
     * Run network diagnostics for the current configuration
     */
    public static void runDiagnostics(Context context, NetworkDiagnosticsManager.DiagnosticsCallback callback) {
        NetworkDiagnosticsManager.getInstance().runFullDiagnostics(context, callback);
    }
}