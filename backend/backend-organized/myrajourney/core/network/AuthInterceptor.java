package com.example.myrajourney.core.network;

import android.content.Context;
import androidx.annotation.NonNull;

import com.example.myrajourney.core.session.TokenManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private final Context context;

    public AuthInterceptor(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        Request.Builder requestBuilder = original.newBuilder();

        // 1. Get token from the new Secure TokenManager
        String token = TokenManager.getInstance(context).getToken();

        // 2. Add Authorization header if token exists
        if (token != null && !token.isEmpty()) {
            requestBuilder.header("Authorization", "Bearer " + token);
        }

        // 3. Standard Headers
        requestBuilder.header("Content-Type", "application/json");
        requestBuilder.header("Accept", "application/json");

        Request request = requestBuilder.build();
        return chain.proceed(request);
    }
}






