package com.example.myrajourney.patient.symptoms;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.myrajourney.R;
// ✅ FIX: Import TokenManager
import com.example.myrajourney.core.session.TokenManager;

import androidx.appcompat.app.AppCompatActivity;

// Add Import for SymptomRequest
import com.example.myrajourney.data.model.SymptomRequest;

public class SymptomLogActivity extends AppCompatActivity {

    private SeekBar vasSeekbar, fatigueSeekbar;
    private TextView vasValue, fatigueValue;
    private EditText jointsInput, otherSymptoms, notes;
    private RadioGroup stiffnessGroup;
    private Button submitButton;

    private int vasScore = 0, fatigueScore = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptom_log);

        vasSeekbar = findViewById(R.id.vasSeekbar);
        vasValue = findViewById(R.id.vasValue);
        fatigueSeekbar = findViewById(R.id.fatigueSeekbar);
        fatigueValue = findViewById(R.id.fatigueValue);
        jointsInput = findViewById(R.id.jointsInput);
        otherSymptoms = findViewById(R.id.otherSymptoms);
        notes = findViewById(R.id.notes);
        stiffnessGroup = findViewById(R.id.stiffnessGroup);
        submitButton = findViewById(R.id.submitButton);

        // Update VAS Score
        vasSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                vasScore = progress;
                vasValue.setText("VAS: " + progress);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Update Fatigue
        fatigueSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                fatigueScore = progress;
                fatigueValue.setText("Fatigue: " + progress);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Handle Submit
        submitButton.setOnClickListener(v -> {
            String joints = jointsInput.getText().toString().trim();
            String other = otherSymptoms.getText().toString().trim();
            String note = notes.getText().toString().trim();

            // Validate joint count
            Integer jointCount = null;
            if (!joints.isEmpty()) {
                try {
                    jointCount = Integer.parseInt(joints);
                    if (jointCount < 0 || jointCount > 10) {
                        Toast.makeText(this, "Number of joints must be between 0 and 10", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Please enter a valid number for joints (0-10)", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // Get stiffness level from radio button
            int stiffnessLevel = 0; // Default
            int selectedId = stiffnessGroup.getCheckedRadioButtonId();
            if (selectedId != -1) {
                RadioButton selected = findViewById(selectedId);
                String stiffnessText = selected.getText().toString().toLowerCase();
                // Map stiffness text to level (0-10 scale)
                if (stiffnessText.contains("none") || stiffnessText.contains("no")) {
                    stiffnessLevel = 0;
                } else if (stiffnessText.contains("mild") || stiffnessText.contains("light")) {
                    stiffnessLevel = 3;
                } else if (stiffnessText.contains("moderate") || stiffnessText.contains("medium")) {
                    stiffnessLevel = 6;
                } else if (stiffnessText.contains("severe") || stiffnessText.contains("heavy")) {
                    stiffnessLevel = 9;
                }
            }

            // Combine all notes (excluding joint count as it's now a separate field)
            StringBuilder fullNotes = new StringBuilder();
            if (!other.isEmpty()) {
                fullNotes.append("Other symptoms: ").append(other).append("\n");
            }
            if (!note.isEmpty()) {
                fullNotes.append("Notes: ").append(note);
            }

            // Save to backend
            saveSymptomToBackend(vasScore, stiffnessLevel, fatigueScore, jointCount, fullNotes.toString());
        });
    }

    private void saveSymptomToBackend(int painLevel, int stiffnessLevel, int fatigueLevel, Integer jointCount, String notes) {
        // Get current date
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        String date = sdf.format(new java.util.Date());

        // Get patient ID from TokenManager
        TokenManager tokenManager = TokenManager.getInstance(this);
        String patientId = tokenManager.getUserId();

        if (patientId == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create symptom request with correct field names
        SymptomRequest request = new SymptomRequest(patientId, date, painLevel);
        request.setStiffnessLevel(stiffnessLevel);
        request.setFatigueLevel(fatigueLevel);
        request.setJointCount(jointCount);
        request.setNotes(notes);

        // Save to backend
        com.example.myrajourney.core.network.ApiService apiService = com.example.myrajourney.core.network.ApiClient.getApiService(this);
        retrofit2.Call<com.example.myrajourney.data.model
                .ApiResponse<com.example.myrajourney.data.model
                .Symptom>> call = apiService.createSymptom(request);

        call.enqueue(new retrofit2.Callback<com.example.myrajourney.data.model
                .ApiResponse<com.example.myrajourney.data.model
                .Symptom>>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.myrajourney.data.model
                                           .ApiResponse<com.example.myrajourney.data.model
                                           .Symptom>> call,
                                   retrofit2.Response<com.example.myrajourney.data.model
                                           .ApiResponse<com.example.myrajourney.data.model
                                           .Symptom>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(SymptomLogActivity.this, "Symptom logged successfully! Doctor will be notified.", Toast.LENGTH_LONG).show();
                    finish(); // Close activity
                } else {
                    String errorMsg = "Failed to save symptom";
                    if (response.body() != null && response.body().getError() != null) {
                        errorMsg = response.body().getError().getMessage();
                    }
                    Toast.makeText(SymptomLogActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.myrajourney.data.model
                    .ApiResponse<com.example.myrajourney.data.model
                    .Symptom>> call, Throwable t) {
                Toast.makeText(SymptomLogActivity.this, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}