package com.example.myrajourney.admin.meds;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myrajourney.R;
import com.example.myrajourney.core.network.ApiClient;
import com.example.myrajourney.core.network.ApiService;
import com.example.myrajourney.core.session.SessionManager;
import com.example.myrajourney.core.ui.ThemeManager;
import com.example.myrajourney.data.model.ApiResponse;
import com.example.myrajourney.data.model.Medication;
import com.example.myrajourney.patient.medications.MedicationsAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AllMedicationsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    MedicationsAdapter adapter;
    List<Medication> medList;
    TextView statusText;
    Button btnClearAll, btnRefresh;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_medications);

        sessionManager = SessionManager.getInstance(this);

        // Only allow admin access
        if (!sessionManager.isSessionValid() || !"ADMIN".equals(sessionManager.getRole())) {
            Toast.makeText(this, "Admin access required", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        loadAllPatientMedications();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recycler_view_medications);
        statusText = findViewById(R.id.statusText);
        btnClearAll = findViewById(R.id.btnClearAll);
        btnRefresh = findViewById(R.id.btnRefresh);

        medList = new ArrayList<>();
        adapter = new MedicationsAdapter(this, medList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        btnClearAll.setOnClickListener(v -> {
            Intent intent = new Intent(this, ClearMedicationsActivity.class);
            startActivity(intent);
        });

        btnRefresh.setOnClickListener(v -> loadAllPatientMedications());
    }

    private void loadAllPatientMedications() {
        statusText.setText("Loading all patient medications...");
        btnRefresh.setEnabled(false);

        ApiService apiService = ApiClient.getApiService(this);
        Call<ApiResponse<List<Map<String, Object>>>> call = apiService.getAllPatientMedications();

        call.enqueue(new Callback<ApiResponse<List<Map<String, Object>>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Map<String, Object>>>> call,
                    Response<ApiResponse<List<Map<String, Object>>>> response) {

                btnRefresh.setEnabled(true);

                try {
                    if (response.isSuccessful() && response.body() != null) {
                        if (response.body().isSuccess()) {
                            List<Map<String, Object>> rawMeds = response.body().getData();

                            if (rawMeds != null && !rawMeds.isEmpty()) {
                                medList.clear();

                                // Convert raw data to Medication objects
                                for (Map<String, Object> rawMed : rawMeds) {
                                    try {
                                        Medication med = new Medication();
                                        med.setId(String.valueOf(rawMed.get("id")));
                                        med.setName(String.valueOf(rawMed.get("name")));
                                        med.setDosage(String.valueOf(rawMed.get("dosage")));
                                        med.setFrequency(String.valueOf(rawMed.get("frequency")));
                                        med.setActive("1".equals(String.valueOf(rawMed.get("active"))));

                                        medList.add(med);
                                    } catch (Exception e) {
                                        Log.e("AllMedications", "Error processing medication", e);
                                    }
                                }

                                adapter.notifyDataSetChanged();
                                statusText.setText("Showing " + medList.size() + " patient medication assignments");
                            } else {
                                medList.clear();
                                adapter.notifyDataSetChanged();
                                statusText.setText("No patient medications found. System is clean.");
                            }
                        } else {
                            statusText.setText("API Error: " + response.body().getError());
                        }
                    } else {
                        statusText.setText("HTTP Error: " + response.code());
                    }
                } catch (Exception e) {
                    Log.e("AllMedications", "Error processing response", e);
                    statusText.setText("Error processing response: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Map<String, Object>>>> call, Throwable t) {
                btnRefresh.setEnabled(true);
                statusText.setText("Network error: " + t.getMessage());
                Log.e("AllMedications", "Network error", t);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAllPatientMedications(); // Refresh when returning from clear activity
    }
}