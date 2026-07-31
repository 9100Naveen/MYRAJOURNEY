package com.example.myrajourney.debug;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myrajourney.R;
import com.example.myrajourney.core.network.ApiClient;
import com.example.myrajourney.core.network.ApiService;
import com.example.myrajourney.core.session.SessionManager;
import com.example.myrajourney.core.session.TokenManager;
import com.example.myrajourney.data.model.ApiResponse;
import com.example.myrajourney.data.model.AuthRequest;
import com.example.myrajourney.data.model.AuthResponse;
import com.example.myrajourney.data.model.Medication;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MedicationDebugActivity extends AppCompatActivity {

    private TextView tvResults;
    private Button btnTestPatientLogin, btnTestMedications, btnClearSession;
    
    private static final String TEST_EMAIL = "deepan@gmail.com";
    private static final String TEST_PASSWORD = "Welcome@123";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medication_debug);

        tvResults = findViewById(R.id.tv_results);
        btnTestPatientLogin = findViewById(R.id.btn_test_patient_login);
        btnTestMedications = findViewById(R.id.btn_test_medications);
        btnClearSession = findViewById(R.id.btn_clear_session);

        btnTestPatientLogin.setOnClickListener(v -> testPatientLogin());
        btnTestMedications.setOnClickListener(v -> testMedicationRetrieval());
        btnClearSession.setOnClickListener(v -> clearSession());
        
        updateUI();
    }

    private void testPatientLogin() {
        appendResult("Testing patient login...");
        
        AuthRequest request = new AuthRequest(TEST_EMAIL, TEST_PASSWORD);
        ApiService apiService = ApiClient.getApiService(this);
        
        Call<ApiResponse<AuthResponse>> call = apiService.login(request);
        call.enqueue(new Callback<ApiResponse<AuthResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AuthResponse>> call, Response<ApiResponse<AuthResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    AuthResponse authResponse = response.body().getData();
                    
                    // Save session
                    TokenManager.getInstance(MedicationDebugActivity.this).saveToken(authResponse.getToken());
                    TokenManager.getInstance(MedicationDebugActivity.this).saveUserInfo(
                            String.valueOf(authResponse.getUser().getId()),
                            authResponse.getUser().getEmail(),
                            authResponse.getUser().getRole()
                    );
                    
                    SessionManager.getInstance(MedicationDebugActivity.this).createSession(
                            authResponse.getUser().getName(),
                            authResponse.getUser().getEmail(),
                            authResponse.getUser().getRole(),
                            String.valueOf(authResponse.getUser().getId())
                    );
                    
                    appendResult("✅ Login successful!");
                    appendResult("User: " + authResponse.getUser().getName());
                    appendResult("Role: " + authResponse.getUser().getRole());
                    appendResult("Token: " + authResponse.getToken().substring(0, 20) + "...");
                    
                    updateUI();
                } else {
                    appendResult("❌ Login failed: " + response.code());
                    if (response.body() != null && response.body().getError() != null) {
                        appendResult("Error: " + response.body().getError().getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AuthResponse>> call, Throwable t) {
                appendResult("❌ Login network error: " + t.getMessage());
                Log.e("MedicationDebug", "Login error", t);
            }
        });
    }

    private void testMedicationRetrieval() {
        appendResult("Testing medication retrieval...");
        
        ApiService apiService = ApiClient.getApiService(this);
        Call<ApiResponse<List<Medication>>> call = apiService.getPatientMedications();
        
        call.enqueue(new Callback<ApiResponse<List<Medication>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Medication>>> call, Response<ApiResponse<List<Medication>>> response) {
                appendResult("Response code: " + response.code());
                
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isSuccess()) {
                        List<Medication> medications = response.body().getData();
                        appendResult("✅ Found " + (medications != null ? medications.size() : 0) + " medications");
                        
                        if (medications != null) {
                            for (int i = 0; i < Math.min(medications.size(), 5); i++) {
                                Medication med = medications.get(i);
                                appendResult("- " + med.getName() + " (" + med.getDosage() + ")");
                            }
                            if (medications.size() > 5) {
                                appendResult("... and " + (medications.size() - 5) + " more");
                            }
                        }
                    } else {
                        appendResult("❌ API returned success=false");
                        if (response.body().getError() != null) {
                            appendResult("Error: " + response.body().getError().getMessage());
                        }
                    }
                } else {
                    appendResult("❌ Response not successful: " + response.code());
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            appendResult("Error body: " + errorBody);
                        }
                    } catch (Exception e) {
                        Log.e("MedicationDebug", "Could not read error body", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Medication>>> call, Throwable t) {
                appendResult("❌ Medication network error: " + t.getMessage());
                Log.e("MedicationDebug", "Medication retrieval error", t);
            }
        });
    }

    private void clearSession() {
        TokenManager.getInstance(this).clearSession();
        SessionManager.getInstance(this).logout();
        appendResult("Session cleared");
        updateUI();
    }

    private void appendResult(String text) {
        runOnUiThread(() -> {
            String current = tvResults.getText().toString();
            tvResults.setText(current + "\n" + text);
        });
    }

    private void updateUI() {
        SessionManager sessionManager = SessionManager.getInstance(this);
        boolean isLoggedIn = sessionManager.isSessionValid();
        
        btnTestMedications.setEnabled(isLoggedIn);
        
        if (isLoggedIn) {
            appendResult("Current user: " + sessionManager.getUserName() + " (" + sessionManager.getRole() + ")");
        } else {
            appendResult("Not logged in");
        }
    }
}