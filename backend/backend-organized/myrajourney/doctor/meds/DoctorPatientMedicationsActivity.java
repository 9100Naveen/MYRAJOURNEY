package com.example.myrajourney.doctor.meds;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.myrajourney.R;
import com.example.myrajourney.core.network.ApiClient;
import com.example.myrajourney.core.network.ApiService;
import com.example.myrajourney.core.session.SessionManager;
import com.example.myrajourney.data.model.ApiResponse;
import com.example.myrajourney.data.model.Medication;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DoctorPatientMedicationsActivity extends AppCompatActivity {

    private LinearLayout medicationsContainer;
    private TextView noMedicationsText, patientNameText;
    private Button btnAddMedication, btnRefresh;
    private List<Medication> medications;
    private int patientId;
    private String patientName;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_patient_medications);

        patientId = getIntent().getIntExtra("patient_id", 0);
        patientName = getIntent().getStringExtra("patient_name");
        
        if (patientId == 0) {
            Toast.makeText(this, "Invalid patient ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        sessionManager = SessionManager.getInstance(this);
        if (!sessionManager.isSessionValid() || !"DOCTOR".equals(sessionManager.getRole())) {
            Toast.makeText(this, "Access denied", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        loadMedications();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMedications(); // Refresh when returning from assign activity
    }

    private void initializeViews() {
        medicationsContainer = findViewById(R.id.medicationsContainer);
        noMedicationsText = findViewById(R.id.noMedicationsText);
        patientNameText = findViewById(R.id.patientNameText);
        btnAddMedication = findViewById(R.id.btnAddMedication);
        btnRefresh = findViewById(R.id.btnRefresh);
        
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        
        patientNameText.setText(patientName != null ? patientName + "'s Medications" : "Patient Medications");
        
        btnAddMedication.setOnClickListener(v -> {
            Intent intent = new Intent(this, DoctorAssignMedicationActivity.class);
            intent.putExtra("patient_id", patientId);
            startActivity(intent);
        });

        btnRefresh.setOnClickListener(v -> {
            Toast.makeText(this, "Refreshing medications...", Toast.LENGTH_SHORT).show();
            loadMedications();
        });
    }

    private void loadMedications() {
        medications = new ArrayList<>();
        ApiService apiService = ApiClient.getApiService(this);

        Call<ApiResponse<List<Medication>>> call = apiService.getPatientMedications(patientId);

        call.enqueue(new Callback<ApiResponse<List<Medication>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Medication>>> call,
                    Response<ApiResponse<List<Medication>>> response) {
                
                Log.d("DoctorPatientMeds", "API Response Code: " + response.code());
                
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        if (response.body().isSuccess()) {
                            List<Medication> meds = response.body().getData();
                            Log.d("DoctorPatientMeds", "Received " + (meds != null ? meds.size() : 0) + " medications");

                            if (meds != null && !meds.isEmpty()) {
                                medications.addAll(meds);
                                displayMedications();
                            } else {
                                showEmptyState();
                            }
                        } else {
                            Log.e("DoctorPatientMeds", "API returned success=false: " + response.body().getError());
                            showEmptyState();
                        }
                    } else {
                        Log.e("DoctorPatientMeds", "Response not successful. Code: " + response.code());
                        showEmptyState();
                    }
                } catch (Exception e) {
                    Log.e("DoctorPatientMeds", "Exception processing API response", e);
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Medication>>> call, Throwable t) {
                Log.e("DoctorPatientMeds", "Network error loading medications", t);
                Toast.makeText(DoctorPatientMedicationsActivity.this, 
                        "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showEmptyState();
            }
        });
    }

    private void showEmptyState() {
        medicationsContainer.removeAllViews();
        noMedicationsText.setVisibility(View.VISIBLE);
        noMedicationsText.setText("No medications assigned to " + (patientName != null ? patientName : "this patient") + 
                                 "\n\nTap 'Add Medication' to assign medications to their treatment plan.");
    }

    private void displayMedications() {
        try {
            medicationsContainer.removeAllViews();
            if (medications == null || medications.isEmpty()) {
                noMedicationsText.setVisibility(View.VISIBLE);
                return;
            }
            noMedicationsText.setVisibility(View.GONE);

            // Add header with medication count
            TextView headerText = new TextView(this);
            headerText.setText("Assigned Medications (" + medications.size() + ")");
            headerText.setTextSize(16);
            headerText.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            headerText.setTypeface(null, android.graphics.Typeface.BOLD);
            headerText.setPadding(0, 0, 0, 16);
            medicationsContainer.addView(headerText);

            for (Medication med : medications) {
                if (med != null) {
                    addMedicationCard(med);
                }
            }
            
            Log.d("DoctorPatientMeds", "Displayed " + medications.size() + " medications for patient " + patientName);
            
        } catch (Exception e) {
            Log.e("DoctorPatientMeds", "Error displaying medications", e);
            showEmptyState();
        }
    }

    private void addMedicationCard(Medication med) {
        try {
            View cardView = getLayoutInflater().inflate(R.layout.item_doctor_patient_medication, medicationsContainer, false);

            TextView medName = cardView.findViewById(R.id.medicineName);
            TextView medDosage = cardView.findViewById(R.id.medicineDosage);
            TextView medFrequency = cardView.findViewById(R.id.medicineFrequency);
            TextView medStatus = cardView.findViewById(R.id.medicineStatus);
            ImageView btnRemove = cardView.findViewById(R.id.btnRemove); // Changed to ImageView

            // Debug: Check if btnRemove is found
            if (btnRemove == null) {
                Log.e("DoctorPatientMeds", "ERROR: btnRemove ImageView not found in layout!");
            } else {
                Log.d("DoctorPatientMeds", "SUCCESS: btnRemove ImageView found");
            }

            // Safely set text with null checks
            String name = med.getName() != null ? med.getName() : "Unknown Medication";
            String dosage = med.getDosage() != null && !med.getDosage().isEmpty() ? med.getDosage() : "Not specified";
            String frequency = med.getFrequency() != null && !med.getFrequency().isEmpty() ? med.getFrequency() : "Not specified";
            
            medName.setText(name);
            medDosage.setText("Dosage: " + dosage);
            
            // Enhanced frequency display
            if (!"Not specified".equals(frequency)) {
                if (frequency.matches("\\d+")) {
                    medFrequency.setText("Frequency: " + frequency + "x per day");
                } else {
                    medFrequency.setText("Frequency: " + frequency);
                }
            } else {
                medFrequency.setText("Frequency: Not specified");
            }
            
            // Status with better styling
            if (med.isActive()) {
                medStatus.setText("Active");
                medStatus.setTextColor(ContextCompat.getColor(this, android.R.color.white));
                medStatus.setBackgroundResource(R.drawable.status_badge_background);
            } else {
                medStatus.setText("Inactive");
                medStatus.setTextColor(ContextCompat.getColor(this, android.R.color.white));
                medStatus.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            }

            // Remove button click handler (now ImageView)
            if (btnRemove != null) {
                btnRemove.setVisibility(View.VISIBLE); // Ensure it's visible
                btnRemove.setOnClickListener(v -> {
                    Log.d("DoctorPatientMeds", "Delete icon clicked for medication: " + name);
                    showRemoveConfirmation(med);
                });
                
                // Make sure the icon is properly tinted
                btnRemove.setColorFilter(ContextCompat.getColor(this, R.color.error_color));
                
                Log.d("DoctorPatientMeds", "Delete icon configured for medication: " + name);
            } else {
                Log.e("DoctorPatientMeds", "CRITICAL: Delete icon not found for medication: " + name);
            }

            // Add some visual feedback on card
            cardView.setOnLongClickListener(v -> {
                Toast.makeText(this, "Medication ID: " + med.getId(), Toast.LENGTH_SHORT).show();
                return true;
            });

            medicationsContainer.addView(cardView);
            
            Log.d("DoctorPatientMeds", "Added medication card: " + name + " (ID: " + med.getId() + ")");
            
        } catch (Exception e) {
            Log.e("DoctorPatientMeds", "Error adding medication card for: " +
                    (med != null ? med.getName() : "null"), e);
        }
    }

    private void showRemoveConfirmation(Medication med) {
        String medName = med.getName() != null ? med.getName() : "Unknown Medication";
        String dosage = med.getDosage() != null && !med.getDosage().isEmpty() ? med.getDosage() : "No dosage";
        
        new AlertDialog.Builder(this)
                .setTitle("⚠️ Remove Medication")
                .setMessage("Are you sure you want to remove this medication from " + patientName + "'s treatment plan?\n\n" +
                           "Medication: " + medName + "\n" +
                           "Dosage: " + dosage + "\n\n" +
                           "This action cannot be undone and the patient will be notified.")
                .setPositiveButton("Remove", (dialog, which) -> removeMedication(med))
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void removeMedication(Medication med) {
        if (med.getId() == null || med.getId().isEmpty()) {
            Toast.makeText(this, "Cannot remove medication: Invalid ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress
        String medName = med.getName() != null ? med.getName() : "Unknown Medication";
        Toast.makeText(this, "Removing " + medName + "...", Toast.LENGTH_SHORT).show();

        ApiService apiService = ApiClient.getApiService(this);
        Call<ApiResponse<Void>> call = apiService.deleteMedication(med.getId());

        call.enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        if (response.body().isSuccess()) {
                            // Success - medication removed
                            Toast.makeText(DoctorPatientMedicationsActivity.this, 
                                    "✅ " + medName + " removed successfully", Toast.LENGTH_SHORT).show();
                            
                            // Remove from local list and refresh UI
                            medications.remove(med);
                            displayMedications();
                            
                            Log.d("DoctorPatientMeds", "Successfully removed medication: " + medName);
                        } else {
                            // API returned success=false
                            String errorMsg = "Failed to remove medication";
                            if (response.body().getError() != null) {
                                errorMsg = response.body().getError().getMessage();
                            }
                            Toast.makeText(DoctorPatientMedicationsActivity.this, 
                                    "❌ " + errorMsg, Toast.LENGTH_LONG).show();
                            Log.e("DoctorPatientMeds", "API error removing medication: " + errorMsg);
                        }
                    } else {
                        // HTTP error
                        String errorMsg = "Server error (HTTP " + response.code() + ")";
                        Toast.makeText(DoctorPatientMedicationsActivity.this, 
                                "❌ " + errorMsg, Toast.LENGTH_LONG).show();
                        Log.e("DoctorPatientMeds", "HTTP error removing medication. Code: " + response.code());
                        
                        // Try to read error body
                        if (response.errorBody() != null) {
                            try {
                                String errorBody = response.errorBody().string();
                                Log.e("DoctorPatientMeds", "Error body: " + errorBody);
                            } catch (Exception e) {
                                Log.e("DoctorPatientMeds", "Could not read error body", e);
                            }
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(DoctorPatientMedicationsActivity.this, 
                            "❌ Error processing response", Toast.LENGTH_SHORT).show();
                    Log.e("DoctorPatientMeds", "Exception processing remove response", e);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                String errorMessage = "Network error";
                if (t.getMessage() != null) {
                    String msg = t.getMessage().toLowerCase();
                    if (msg.contains("timeout")) {
                        errorMessage = "Request timed out. Please try again.";
                    } else if (msg.contains("refused") || msg.contains("failed to connect")) {
                        errorMessage = "Cannot connect to server. Please check your connection.";
                    } else {
                        errorMessage = "Network error: " + t.getMessage();
                    }
                }
                
                Toast.makeText(DoctorPatientMedicationsActivity.this, 
                        "❌ " + errorMessage, Toast.LENGTH_LONG).show();
                Log.e("DoctorPatientMeds", "Network error removing medication", t);
            }
        });
    }
}