package com.example.myrajourney.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myrajourney.R;
import com.example.myrajourney.core.network.ApiService;
import com.example.myrajourney.data.model.ApiResponse;
import com.example.myrajourney.utils.ValidationUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity for handling forgot password functionality
 * Provides email-based password reset with validation
 */
public class ForgotPasswordActivity extends AppCompatActivity {
    
    private static final String TAG = "ForgotPasswordActivity";
    
    private EditText etEmail;
    private Button btnResetPassword;
    private Button btnBackToLogin;
    private ProgressBar progressBar;
    private TextView tvInstructions;
    private TextView tvStatus;
    
    private ApiService apiService;
    private boolean isResetSent = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        
        initializeViews();
        initializeApiService();
        setupClickListeners();
    }
    
    private void initializeViews() {
        etEmail = findViewById(R.id.et_email);
        btnResetPassword = findViewById(R.id.btn_reset_password);
        btnBackToLogin = findViewById(R.id.btn_back_to_login);
        progressBar = findViewById(R.id.progress_bar);
        tvInstructions = findViewById(R.id.tv_instructions);
        tvStatus = findViewById(R.id.tv_status);
    }
    
    private void initializeApiService() {
        apiService = com.example.myrajourney.core.network.ApiClient.getApiService(this);
    }
    
    private void setupClickListeners() {
        btnResetPassword.setOnClickListener(v -> handlePasswordReset());
        btnBackToLogin.setOnClickListener(v -> finish());
    }
    
    private void handlePasswordReset() {
        if (isResetSent) {
            // If reset was already sent, allow resending
            resendPasswordReset();
            return;
        }
        
        String email = etEmail.getText().toString().trim();
        
        // Validate email
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }
        
        if (!ValidationUtils.isValidEmail(email)) {
            etEmail.setError("Please enter a valid email address");
            etEmail.requestFocus();
            return;
        }
        
        // Send password reset request
        sendPasswordResetRequest(email);
    }
    
    private void sendPasswordResetRequest(String email) {
        showLoading(true);
        
        // Create request body
        ForgotPasswordRequest request = new ForgotPasswordRequest(email);
        
        apiService.forgotPassword(request).enqueue(new Callback<ApiResponse<ForgotPasswordResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<ForgotPasswordResponse>> call, 
                                 Response<ApiResponse<ForgotPasswordResponse>> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<ForgotPasswordResponse> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess()) {
                        handleResetSuccess(apiResponse.getData());
                    } else {
                        handleResetError(apiResponse.getMessage());
                    }
                } else {
                    handleResetError("Failed to send reset request. Please try again.");
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<ForgotPasswordResponse>> call, Throwable t) {
                showLoading(false);
                handleResetError("Network error. Please check your connection and try again.");
            }
        });
    }
    
    private void handleResetSuccess(ForgotPasswordResponse response) {
        isResetSent = true;
        
        // Navigate to ResetPasswordActivity for direct password reset
        Toast.makeText(this, "Email verified! Please enter your new password.", Toast.LENGTH_LONG).show();
        
        Intent intent = new Intent(this, com.example.myrajourney.admin.users.ResetPasswordActivity.class);
        intent.putExtra("email", etEmail.getText().toString().trim());
        startActivity(intent);
        finish();
    }
    
    private void handleResetError(String errorMessage) {
        tvStatus.setText(errorMessage);
        tvStatus.setTextColor(getResources().getColor(R.color.error_color));
        tvStatus.setVisibility(View.VISIBLE);
        
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }
    
    private void resendPasswordReset() {
        String email = etEmail.getText().toString().trim();
        
        tvStatus.setText("Resending reset link...");
        tvStatus.setTextColor(getResources().getColor(R.color.info_color));
        
        sendPasswordResetRequest(email);
    }
    
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnResetPassword.setEnabled(!show);
        btnBackToLogin.setEnabled(!show);
        
        if (show) {
            btnResetPassword.setText("Sending...");
        } else {
            btnResetPassword.setText(isResetSent ? "Resend Reset Link" : "Send Reset Link");
        }
    }
    
    // Data classes for API requests/responses
    public static class ForgotPasswordRequest {
        private String email;
        
        public ForgotPasswordRequest(String email) {
            this.email = email;
        }
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
    }
    
    public static class ForgotPasswordResponse {
        private String message;
        private String resetToken;
        private long expiresAt;
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        public String getResetToken() {
            return resetToken;
        }
        
        public void setResetToken(String resetToken) {
            this.resetToken = resetToken;
        }
        
        public long getExpiresAt() {
            return expiresAt;
        }
        
        public void setExpiresAt(long expiresAt) {
            this.expiresAt = expiresAt;
        }
    }
}