package com.example.myrajourney.debug;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myrajourney.R;
import com.example.myrajourney.core.network.ApiClient;
import com.example.myrajourney.core.network.ApiService;
import com.example.myrajourney.core.network.NetworkConfigHelper;
import com.example.myrajourney.data.model.ApiResponse;
import com.example.myrajourney.data.model.AuthRequest;
import com.example.myrajourney.data.model.AuthResponse;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NetworkTestActivity extends AppCompatActivity {

    private TextView tvResults;
    private Button btnTestConnection, btnTestLogin, btnTestMedicationAssign;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_test);

        tvResults = findViewById(R.id.tv_results);
        btnTestConnection = findViewById(R.id.btn_test_connection);
        btnTestLogin = findViewById(R.id.btn_test_login);
        btnTestMedicationAssign = findViewById(R.id.btn_test_medication_assign);

        btnTestConnection.setOnClickListener(v -> testConnection());
        btnTestLogin.setOnClickListener(v -> testLogin());
        btnTestMedicationAssign.setOnClickListener(v -> testMedicationAssign());

        // Show current network configuration
        showNetworkConfig();
    }

    private void showNetworkConfig() {
        StringBuilder config = new StringBuilder();
        config.append("=== Network Configuration ===\n");
        config.append("IP: ").append(NetworkConfigHelper.getBaseIp(this)).append("\n");
        config.append("Port: ").append(NetworkConfigHelper.getApiPort(this)).append("\n");
        config.append("Base URL: ").append(NetworkConfigHelper.getApiBaseUrl(this)).append("\n");
        config.append("Server URL: ").append(NetworkConfigHelper.getServerUrl(this)).append("\n");
        config.append("Config Valid: ").append(NetworkConfigHelper.isConfigurationValid(this)).append("\n\n");

        tvResults.setText(config.toString());
    }

    private void testConnection() {
        appendResult("=== Testing Basic Connection ===");
        
        // Test a simple endpoint that doesn't require auth
        ApiService apiService = ApiClient.getApiService(this);
        
        // We'll test the login endpoint with invalid credentials to see if we can reach the server
        AuthRequest request = new AuthRequest("test@test.com", "invalid");
        Call<ApiResponse<AuthResponse>> call = apiService.login(request);
        
        call.enqueue(new Callback<ApiResponse<AuthResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AuthResponse>> call, Response<ApiResponse<AuthResponse>> response) {
                appendResult("✅ Connection successful!");
                appendResult("HTTP Code: " + response.code());
                appendResult("URL: " + call.request().url());
                
                if (response.body() != null) {
                    appendResult("Response received (expected failure): " + response.body().isSuccess());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AuthResponse>> call, Throwable t) {
                appendResult("❌ Connection failed!");
                appendResult("Error: " + t.getClass().getSimpleName());
                appendResult("Message: " + t.getMessage());
                appendResult("URL: " + call.request().url());
                
                Log.e("NetworkTest", "Connection test failed", t);
            }
        });
    }

    private void testLogin() {
        appendResult("\n=== Testing Login ===");
        
        AuthRequest request = new AuthRequest("doctor@myrajourney.com", "Patrol@987");
        ApiService apiService = ApiClient.getApiService(this);
        
        Call<ApiResponse<AuthResponse>> call = apiService.login(request);
        call.enqueue(new Callback<ApiResponse<AuthResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AuthResponse>> call, Response<ApiResponse<AuthResponse>> response) {
                appendResult("Login HTTP Code: " + response.code());
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    appendResult("✅ Login successful!");
                    appendResult("Token received: " + response.body().getData().getToken().substring(0, 20) + "...");
                } else {
                    appendResult("❌ Login failed");
                    if (response.body() != null && response.body().getError() != null) {
                        appendResult("Error: " + response.body().getError().getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AuthResponse>> call, Throwable t) {
                appendResult("❌ Login network error!");
                appendResult("Error: " + t.getMessage());
                Log.e("NetworkTest", "Login test failed", t);
            }
        });
    }

    private void testMedicationAssign() {
        appendResult("\n=== Testing Medication Assignment ===");
        
        // First login to get token
        AuthRequest request = new AuthRequest("doctor@myrajourney.com", "Patrol@987");
        ApiService apiService = ApiClient.getApiService(this);
        
        Call<ApiResponse<AuthResponse>> call = apiService.login(request);
        call.enqueue(new Callback<ApiResponse<AuthResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AuthResponse>> call, Response<ApiResponse<AuthResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    appendResult("Login for medication test: ✅");
                    
                    // Now test medication assignment
                    Map<String, Object> medicationData = new HashMap<>();
                    medicationData.put("patient_id", 25);
                    medicationData.put("name_override", "Network Test Medication");
                    medicationData.put("dosage", "Test Dose");
                    medicationData.put("frequency_per_day", 1);
                    
                    Call<ApiResponse<Map<String, Object>>> medCall = apiService.doctorAssignMedication(medicationData);
                    medCall.enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                            appendResult("Medication assign HTTP Code: " + response.code());
                            appendResult("URL: " + call.request().url());
                            
                            if (response.isSuccessful() && response.body() != null) {
                                if (response.body().isSuccess()) {
                                    appendResult("✅ Medication assignment successful!");
                                } else {
                                    appendResult("❌ Medication assignment failed (API error)");
                                    if (response.body().getError() != null) {
                                        appendResult("Error: " + response.body().getError().getMessage());
                                    }
                                }
                            } else {
                                appendResult("❌ Medication assignment failed (HTTP error)");
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                            appendResult("❌ Medication assignment network error!");
                            appendResult("Error type: " + t.getClass().getSimpleName());
                            appendResult("Error message: " + t.getMessage());
                            appendResult("URL: " + call.request().url());
                            
                            Log.e("NetworkTest", "Medication assignment test failed", t);
                        }
                    });
                } else {
                    appendResult("❌ Login failed for medication test");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AuthResponse>> call, Throwable t) {
                appendResult("❌ Login network error for medication test!");
                appendResult("Error: " + t.getMessage());
            }
        });
    }

    private void appendResult(String text) {
        runOnUiThread(() -> {
            String current = tvResults.getText().toString();
            tvResults.setText(current + text + "\n");
        });
    }
}