package com.example.myrajourney.doctor.meds;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myrajourney.R;
import com.example.myrajourney.core.network.ApiClient;
import com.example.myrajourney.core.network.ApiService;
import com.example.myrajourney.core.session.SessionManager;
import com.example.myrajourney.data.model.ApiResponse;
import com.example.myrajourney.data.model.Medication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DoctorAssignMedicationActivity extends AppCompatActivity {

    private LinearLayout medicationChecklistContainer;
    private ProgressBar progress;
    private Button btnAssign;
    private TextView tvPatientInfo;
    private EditText etDosage, etFrequencyPerDay, etInstructions, etDuration;
    private CheckBox cbMorning, cbAfternoon, cbNight;
    private RadioGroup rgFoodRelation;

    private int patientId = 0;
    private List<MedicationCheckItem> medicationCheckItems = new ArrayList<>();

    /**
     * Simple medication check item class
     */
    public static class MedicationCheckItem {
        private String name;
        private String defaultDosage;
        private int defaultFrequency;
        private boolean isSelected;
        private CheckBox checkBox;

        public MedicationCheckItem(String name, String defaultDosage, int defaultFrequency) {
            this.name = name;
            this.defaultDosage = defaultDosage;
            this.defaultFrequency = defaultFrequency;
            this.isSelected = false;
        }

        // Getters and setters
        public String getName() {
            return name;
        }

        public String getDefaultDosage() {
            return defaultDosage;
        }

        public int getDefaultFrequency() {
            return defaultFrequency;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(boolean selected) {
            this.isSelected = selected;
        }

        public CheckBox getCheckBox() {
            return checkBox;
        }

        public void setCheckBox(CheckBox checkBox) {
            this.checkBox = checkBox;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_assign_medication_simple);

        patientId = getIntent().getIntExtra("patient_id", 0);

        Log.d("AssignMedication", "Received patient_id: " + patientId);

        if (patientId == 0) {
            Log.e("AssignMedication", "Invalid patient ID received!");
            Toast.makeText(this, "Invalid patient ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Validate session
        SessionManager sessionManager = SessionManager.getInstance(this);
        if (!sessionManager.isSessionValid()) {
            Log.e("AssignMedication", "Session invalid");
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String userRole = sessionManager.getRole();
        if (!"DOCTOR".equals(userRole)) {
            Log.e("AssignMedication", "Non-doctor user trying to assign medications");
            Toast.makeText(this, "Access denied. Doctor role required.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupMedicationChecklist();
    }

    private void initViews() {
        medicationChecklistContainer = findViewById(R.id.medication_checklist_container);
        progress = findViewById(R.id.progress);
        btnAssign = findViewById(R.id.btn_assign);
        tvPatientInfo = findViewById(R.id.tv_patient_info);
        etDosage = findViewById(R.id.et_dosage);
        etFrequencyPerDay = findViewById(R.id.et_frequency_per_day);
        etInstructions = findViewById(R.id.et_instructions);
        etDuration = findViewById(R.id.et_duration);
        cbMorning = findViewById(R.id.cb_morning);
        cbAfternoon = findViewById(R.id.cb_afternoon);
        cbNight = findViewById(R.id.cb_night);
        rgFoodRelation = findViewById(R.id.rg_food_relation);

        // Set patient info
        tvPatientInfo.setText("Assigning medications to Patient ID: " + patientId);

        btnAssign.setOnClickListener(v -> assignSelectedMedications());

        // Back button
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void setupMedicationChecklist() {
        // Clear existing items
        medicationCheckItems.clear();
        medicationChecklistContainer.removeAllViews();

        // Add comprehensive list of RA medications
        addMedicationToChecklist("Methotrexate", "15mg", 1);
        addMedicationToChecklist("Hydroxychloroquine", "200mg", 2);
        addMedicationToChecklist("Sulfasalazine", "500mg", 2);
        addMedicationToChecklist("Leflunomide", "20mg", 1);
        addMedicationToChecklist("Adalimumab", "40mg", 1);
        addMedicationToChecklist("Etanercept", "50mg", 1);
        addMedicationToChecklist("Infliximab", "5mg", 1);
        addMedicationToChecklist("Rituximab", "1000mg", 1);
        addMedicationToChecklist("Tocilizumab", "8mg", 1);
        addMedicationToChecklist("Prednisolone", "5mg", 1);
        addMedicationToChecklist("Prednisone", "10mg", 1);
        addMedicationToChecklist("Dexamethasone", "2mg", 1);
        addMedicationToChecklist("Methylprednisolone", "4mg", 1);
        addMedicationToChecklist("Hydrocortisone", "10mg", 2);
        addMedicationToChecklist("Deflazacort", "3mg", 1);
        addMedicationToChecklist("Betamethasone", "0.25mg", 1);
        addMedicationToChecklist("Triamcinolone", "2mg", 1);
        addMedicationToChecklist("Budesonide", "3mg", 1);
        addMedicationToChecklist("Cortisone", "12.5mg", 1);
        addMedicationToChecklist("Ibuprofen", "400mg", 3);
        addMedicationToChecklist("Naproxen", "250mg", 2);
        addMedicationToChecklist("Diclofenac", "50mg", 2);
        addMedicationToChecklist("Celecoxib", "100mg", 2);
        addMedicationToChecklist("Meloxicam", "7.5mg", 1);
        addMedicationToChecklist("Indomethacin", "25mg", 3);
        addMedicationToChecklist("Aspirin", "75mg", 1);
        addMedicationToChecklist("Paracetamol", "500mg", 3);
        addMedicationToChecklist("Tramadol", "50mg", 2);
        addMedicationToChecklist("Codeine", "30mg", 2);
        addMedicationToChecklist("Folic Acid", "5mg", 1);
        addMedicationToChecklist("Calcium", "500mg", 2);
        addMedicationToChecklist("Vitamin D", "1000IU", 1);
        addMedicationToChecklist("Omeprazole", "20mg", 1);
        addMedicationToChecklist("Lansoprazole", "15mg", 1);
        addMedicationToChecklist("Ranitidine", "150mg", 2);
    }

    private void addMedicationToChecklist(String name, String defaultDosage, int defaultFrequency) {
        MedicationCheckItem item = new MedicationCheckItem(name, defaultDosage, defaultFrequency);
        medicationCheckItems.add(item);

        // Create checkbox view
        CheckBox checkBox = new CheckBox(this);
        checkBox.setText(name + " (" + defaultDosage + ", " + defaultFrequency + "x daily)");
        checkBox.setTextSize(16);
        checkBox.setPadding(16, 12, 16, 12);

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setSelected(isChecked);
            updateAssignButtonState();
        });

        item.setCheckBox(checkBox);
        medicationChecklistContainer.addView(checkBox);
    }

    private void updateAssignButtonState() {
        boolean hasSelection = false;
        for (MedicationCheckItem item : medicationCheckItems) {
            if (item.isSelected()) {
                hasSelection = true;
                break;
            }
        }
        btnAssign.setEnabled(hasSelection);
        btnAssign.setText(hasSelection ? "Assign Selected Medications" : "Select Medications to Assign");
    }

    private void assignSelectedMedications() {
        List<MedicationCheckItem> selectedItems = new ArrayList<>();
        for (MedicationCheckItem item : medicationCheckItems) {
            if (item.isSelected()) {
                selectedItems.add(item);
            }
        }

        if (selectedItems.isEmpty()) {
            Toast.makeText(this, "Please select at least one medication", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get custom dosage and frequency if provided
        String customDosage = etDosage.getText().toString().trim();
        String customFrequency = etFrequencyPerDay.getText().toString().trim();
        String instructions = etInstructions.getText().toString().trim();
        String duration = etDuration.getText().toString().trim();
        int isMorning = cbMorning.isChecked() ? 1 : 0;
        int isAfternoon = cbAfternoon.isChecked() ? 1 : 0;
        int isNight = cbNight.isChecked() ? 1 : 0;

        // Food relation
        String foodRelation = "";
        int checkedId = rgFoodRelation.getCheckedRadioButtonId();
        if (checkedId == R.id.rb_before_food)
            foodRelation = "Before Food";
        else if (checkedId == R.id.rb_after_food)
            foodRelation = "After Food";

        progress.setVisibility(View.VISIBLE);
        btnAssign.setEnabled(false);

        Toast.makeText(this, "Assigning with Reason: " + instructions + ", Food: " + foodRelation, Toast.LENGTH_LONG)
                .show();

        // Assign medications one by one
        assignMedicationsSequentially(selectedItems, customDosage, customFrequency, instructions, duration, isMorning,
                isAfternoon, isNight, foodRelation, 0);
    }

    private void assignMedicationsSequentially(List<MedicationCheckItem> selectedItems,
            String customDosage, String customFrequency, String instructions, String duration,
            int isMorning, int isAfternoon, int isNight, String foodRelation, int index) {
        if (index >= selectedItems.size()) {
            // All medications assigned successfully
            progress.setVisibility(View.GONE);
            btnAssign.setEnabled(true);

            Toast.makeText(this, "Successfully assigned " + selectedItems.size() + " medications!",
                    Toast.LENGTH_LONG).show();

            // Clear selections
            clearAllSelections();
            return;
        }

        MedicationCheckItem item = selectedItems.get(index);

        // Use custom values if provided, otherwise use defaults
        String dosage = !customDosage.isEmpty() ? customDosage : item.getDefaultDosage();
        int frequency = !customFrequency.isEmpty() ? Integer.parseInt(customFrequency) : item.getDefaultFrequency();

        // Inject Doctor ID from session to ensure persistence
        SessionManager session = SessionManager.getInstance(this);
        Map<String, Object> body = new HashMap<>();
        body.put("patient_id", String.valueOf(patientId));
        body.put("doctor_id", String.valueOf(session.getUserId()));
        body.put("name_override", item.getName());
        body.put("dosage", dosage);
        body.put("frequency_per_day", String.valueOf(frequency));
        body.put("instructions", instructions != null ? instructions : "");
        body.put("description", instructions != null ? instructions : ""); // Redundant key for safety
        body.put("duration", duration != null ? duration : "");
        body.put("is_morning", String.valueOf(isMorning));
        body.put("is_afternoon", String.valueOf(isAfternoon));
        body.put("is_night", String.valueOf(isNight));
        body.put("food_relation", foodRelation != null ? foodRelation : "");
        body.put("foodRelation", foodRelation != null ? foodRelation : ""); // Redundant key for safety

        Log.d("AssignMedication", "Final Payload: " + body.toString());
        Log.d("AssignMedication",
                "Assigning: " + item.getName() + " to Patient: " + patientId + " by Doctor: " + session.getUserId());

        ApiService api = ApiClient.getApiService(this);
        Call<ApiResponse<Map<String, Object>>> call = api.doctorAssignMedication(body);

        call.enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call,
                    Response<ApiResponse<Map<String, Object>>> response) {

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Log.d("AssignMedication", "Successfully assigned: " + item.getName());
                    // Continue with next medication
                    assignMedicationsSequentially(selectedItems, customDosage, customFrequency, instructions, duration,
                            isMorning, isAfternoon, isNight, foodRelation, index + 1);
                } else {
                    // Handle error
                    String errorMsg = "Failed to assign " + item.getName();
                    if (response.body() != null && response.body().getError() != null) {
                        errorMsg = response.body().getError().getMessage();

                        if (errorMsg.contains("Duplicate medication")) {
                            Log.w("AssignMedication", "Duplicate medication: " + item.getName() + ", skipping...");
                            // Skip this medication and continue with next
                            assignMedicationsSequentially(selectedItems, customDosage, customFrequency, instructions,
                                    duration, isMorning, isAfternoon, isNight, foodRelation, index + 1);
                            return;
                        }
                    }

                    progress.setVisibility(View.GONE);
                    btnAssign.setEnabled(true);
                    Toast.makeText(DoctorAssignMedicationActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                progress.setVisibility(View.GONE);
                btnAssign.setEnabled(true);

                String errorMessage = "Network error assigning " + item.getName() + ": " + t.getMessage();
                Toast.makeText(DoctorAssignMedicationActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void clearAllSelections() {
        for (MedicationCheckItem item : medicationCheckItems) {
            item.setSelected(false);
            if (item.getCheckBox() != null) {
                item.getCheckBox().setChecked(false);
            }
        }
        etDosage.setText("");
        etFrequencyPerDay.setText("");
        etInstructions.setText("");
        etDuration.setText("");
        cbMorning.setChecked(false);
        cbAfternoon.setChecked(false);
        cbNight.setChecked(false);
        rgFoodRelation.clearCheck();
        updateAssignButtonState();
    }
}
