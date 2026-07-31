package com.example.myrajourney.data.repository;

import com.example.myrajourney.core.network.ApiClient;
import com.example.myrajourney.core.network.ApiService;
import com.example.myrajourney.core.session.SessionManager;
import com.example.myrajourney.core.session.TokenManager;
import com.example.myrajourney.data.model.AuthResponse;
import com.example.myrajourney.data.model
.ApiResponse; // Using existing generic wrapper
import com.example.myrajourney.data.model
.AuthRequest; // Using existing request model

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {
    private final ApiService apiService;
    private final TokenManager tokenManager;
    private final SessionManager sessionManager;

    public AuthRepository(ApiService apiService, TokenManager tokenManager, SessionManager sessionManager) {
        this.apiService = apiService;
        this.tokenManager = tokenManager;
        this.sessionManager = sessionManager;
    }

    public interface LoginCallback {
        void onSuccess(String role);
        void onError(String message);
    }

    public void login(String email, String password, LoginCallback callback) {
        AuthRequest request = new AuthRequest(email, password);

        apiService.login(request).enqueue(new Callback<ApiResponse<AuthResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AuthResponse>> call, Response<ApiResponse<AuthResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    AuthResponse data = response.body().getData();
                    if (data != null && data.getUser() != null && data.getToken() != null) {

                        // 1. Save Secure Token
                        tokenManager.saveToken(data.getToken());
                        tokenManager.saveUserInfo(
                                data.getUser().getIdString(),
                                data.getUser().getEmail(),
                                data.getUser().getRole()
                        );

                        // 2. Update Session
                        sessionManager.createSession(
                                data.getUser().getName(),
                                data.getUser().getEmail(),
                                data.getUser().getRole()
                        );

                        // 3. Return Role for Navigation
                        callback.onSuccess(data.getUser().getRole());
                    } else {
                        callback.onError("Invalid server response");
                    }
                } else {
                    callback.onError("Login failed. Please check credentials.");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AuthResponse>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
}






