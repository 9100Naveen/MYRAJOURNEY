package com.example.myrajourney.admin.meds;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myrajourney.R;
import com.example.myrajourney.core.network.ApiClient;
import com.example.myrajourney.core.network.ApiService;
import com.example.myrajourney.core.session.SessionManager;
import com.example.myrajourney.data.model.ApiResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ClearMedicationsActivity extends AppCompatActivity {

    private Button btnClearAll;
    private TextView statusText;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clear_medications);

        sessionManager = SessionManager.getInstance(this);
        
        // Only allow admin access
        if (!sessionManager.isSessionValid() || !"ADMIN".equals(sessionManager.getRole())) {
            Toast.makeText(this, "Admin access required", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
    }

    private void initializeViews() {
        btnClearAll = findViewById(R.id.btnClearAll);
        statusText = findViewById(R.id.statusText);
        
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        
        btnClearAll.setOnClickListener(v -> showClearConfirmation());
        
        statusText.setText("This will remove ALL patient medication assignments.\nUse with caution!");
    }

    private void showClearConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Clear All Patient Medications")
                .setMessage("⚠️ WARNING: This will permanently delete ALL medication assignments for ALL patients.\n\nThis action cannot be undone.\n\nAre you absolutely sure?")
                .setPositiveButton("Yes, Clear All", (dialog, which) -> clearAllMedications())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void clearAllMedications() {
        btnClearAll.setEnabled(false);
        statusText.setText("Clearing all patient medications...");
        
        ApiService apiService = ApiClient.getApiService(this);
        Call<ApiResponse<Void>> call = apiService.clearAllPatientMedications();

        call.enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                btnClearAll.setEnabled(true);
                
                if (response.isSuccessful()) {
                    statusText.setText("✅ All patient medications cleared successfully!\n\nPatients will now see empty medication lists until doctors assign new medications.");
                    Toast.makeText(ClearMedicationsActivity.this, 
                            "All patient medications cleared", Toast.LENGTH_SHORT).show();
                    Log.d("ClearMedications", "Successfully cleared all patient medications");
                } else {
                    statusText.setText("❌ Failed to clear medications. HTTP Code: " + response.code());
                    Toast.makeText(ClearMedicationsActivity.this, 
                            "Failed to clear medications", Toast.LENGTH_SHORT).show();
                    Log.e("ClearMedications", "Failed to clear medications. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                btnClearAll.setEnabled(true);
                statusText.setText("❌ Network error: " + t.getMessage());
                Toast.makeText(ClearMedicationsActivity.this, 
                        "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("ClearMedications", "Network error clearing medications", t);
            }
        });
    }
}