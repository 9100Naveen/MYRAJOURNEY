package com.example.myrajourney.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myrajourney.R;
import com.example.myrajourney.core.navigation.NavigationManager;
import com.example.myrajourney.core.network.ApiClient;
import com.example.myrajourney.core.network.ApiService;
import com.example.myrajourney.core.network.NetworkDiagnosticsHelper;
import com.example.myrajourney.core.session.SessionManager;
import com.example.myrajourney.core.session.TokenManager;
import com.example.myrajourney.core.ui.LoadingDialog;
import com.example.myrajourney.data.model.ApiResponse;
import com.example.myrajourney.data.model.AuthRequest;
import com.example.myrajourney.data.model.AuthResponse;
import com.example.myrajourney.data.model.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText emailET, passET;
    private Button loginBtn;
    private TextView tvForgotPassword;
    private LoadingDialog dialog;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_login);

        session = SessionManager.getInstance(this);
        dialog = new LoadingDialog(this);

        // Bind Views
        emailET = findViewById(R.id.etUsername);
        passET = findViewById(R.id.etPassword);
        loginBtn = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        // Listeners
        loginBtn.setOnClickListener(v -> login());
        tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        });
        
        // Add long-click listener for network diagnostics (debug feature)
        loginBtn.setOnLongClickListener(v -> {
            NetworkDiagnosticsHelper.showQuickDiagnostics(LoginActivity.this);
            return true;
        });
    }

    private void login() {
        String email = emailET.getText().toString().trim();
        String pass = passET.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailET.setError("Email is required");
            return;
        }
        if (TextUtils.isEmpty(pass)) {
            passET.setError("Password is required");
            return;
        }

        // Log network configuration for debugging
        NetworkDiagnosticsHelper.logNetworkConfiguration(this);

        dialog.show("Authenticating...");

        ApiService api = ApiClient.getApiService(this);
        AuthRequest req = new AuthRequest(email, pass);

        api.login(req).enqueue(new Callback<ApiResponse<AuthResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AuthResponse>> call,
                                   Response<ApiResponse<AuthResponse>> res) {

                dialog.dismiss();

                try {
                    if (!res.isSuccessful()) {
                        // Handle HTTP error responses (like 400, 401, 500, etc.)
                        String errorMsg = "Login failed (HTTP " + res.code() + ")";
                        if (res.errorBody() != null) {
                            try {
                                String errorBody = res.errorBody().string();
                                Log.e("LoginActivity", "Error response: " + errorBody);
                                errorMsg += ": " + errorBody;
                            } catch (Exception e) {
                                Log.e("LoginActivity", "Failed to read error body", e);
                            }
                        }
                        Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                        return;
                    }
                    
                    if (res.body() == null) {
                        Toast.makeText(LoginActivity.this, "Login failed: No response from server", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    if (!res.body().isSuccess()) {
                        String errorMsg = res.body().getMessage();
                        if (errorMsg == null || errorMsg.isEmpty()) {
                            errorMsg = "Invalid credentials";
                        }
                        Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    AuthResponse auth = res.body().getData();
                    if (auth == null) {
                        Toast.makeText(LoginActivity.this, "Login failed: Invalid response format", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    User u = auth.getUser();
                    if (u == null) {
                        Toast.makeText(LoginActivity.this, "Login failed: User data missing", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Save token securely
                    String token = auth.getToken();
                    if (token == null || token.isEmpty()) {
                        Toast.makeText(LoginActivity.this, "Login failed: No authentication token received", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    TokenManager.getInstance(LoginActivity.this).saveToken(token);

                    // Save user info in TokenManager (used by other features)
                    TokenManager.getInstance(LoginActivity.this)
                            .saveUserInfo(String.valueOf(u.getId()), u.getEmail(), u.getRole());

                    // Save complete session including user_id
                    String name = (u.getName() == null || u.getName().isEmpty()) ? u.getEmail() : u.getName();

                    session.createSession(
                            name,
                            u.getEmail(),
                            u.getRole(),
                            String.valueOf(u.getId())
                    );

                    // Navigate based on role with error handling
                    try {
                        NavigationManager.goToDashboardForRole(LoginActivity.this, u.getRole());
                        finish();
                    } catch (Exception navError) {
                        Log.e("LoginActivity", "Navigation error", navError);
                        Toast.makeText(LoginActivity.this, 
                            "Login successful but dashboard failed to load: " + navError.getMessage(), 
                            Toast.LENGTH_LONG).show();
                    }
                    
                } catch (Exception e) {
                    Log.e("LoginActivity", "Login processing error", e);
                    Toast.makeText(LoginActivity.this, 
                        "Login processing error: " + e.getMessage(), 
                        Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AuthResponse>> call, Throwable t) {
                dialog.dismiss();
                
                Log.e("LoginActivity", "Login request failed", t);
                
                // Provide detailed error message
                String errorMessage = "Login failed: ";
                if (t instanceof java.net.ConnectException) {
                    errorMessage += "Cannot connect to server. Please check your network connection.";
                } else if (t instanceof java.net.SocketTimeoutException) {
                    errorMessage += "Connection timeout. Please try again.";
                } else if (t instanceof java.net.UnknownHostException) {
                    errorMessage += "Server not found. Please check the server address.";
                } else {
                    errorMessage += t.getMessage();
                }
                
                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                
                // Use enhanced error handling for diagnostics
                NetworkDiagnosticsHelper.handleApiError(LoginActivity.this, t);
            }
        });
    }
}
