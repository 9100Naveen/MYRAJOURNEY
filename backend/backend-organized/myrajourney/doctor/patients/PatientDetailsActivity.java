package com.example.myrajourney.doctor.patients;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable; // Added this import
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myrajourney.R;
import com.example.myrajourney.admin.users.EditPatientActivity;
import com.example.myrajourney.core.network.ApiClient;
import com.example.myrajourney.core.network.ApiService;
import com.example.myrajourney.data.model.ApiResponse;
import com.example.myrajourney.data.model.Medication;
import com.example.myrajourney.data.model.Report;
import com.example.myrajourney.patient.medications.MedicationsAdapter;
import com.example.myrajourney.patient.rehab.RehabAdapter;
import com.example.myrajourney.patient.appointments.AppointmentAdapter;
import com.example.myrajourney.patient.reports.ReportDetailsActivity;
import com.example.myrajourney.patient.reports.ReportsAdapter;
import com.example.myrajourney.data.model.Appointment;
import com.example.myrajourney.data.model.Rehab;
import com.example.myrajourney.data.model.CrpData;
import com.example.myrajourney.data.model.SymptomHistory;
import com.example.myrajourney.doctor.adapters.SymptomHistoryAdapter;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.formatter.ValueFormatter;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PatientDetailsActivity extends AppCompatActivity {

    private static final int REQ_ADD_REHAB = 2001;

    private TextView patientName, patientAge, patientId;
    private ImageView patientImage;
    private RecyclerView medsRecycler, rehabRecycler, appointmentRecycler, reportsRecycler, symptomHistoryRecycler;
    private EditText alertMessage, etDiagnosis, etSuggestions;
    private LineChart crpChart;
    private TextView crpEmptyState, symptomEmptyState, symptomCount;

    private Button sendAlert, editPatientBtn, btnAssignMedication, btnAddRehab, btnSaveDiagnosis, btnLoadMoreSymptoms;

    private List<Medication> medicationsList;
    private List<Rehab> rehabList;
    private List<Appointment> appointmentList;
    private List<Report> reportsList;
    private List<SymptomHistory> symptomHistoryList;

    private MedicationsAdapter medicationsAdapter;
    private RehabAdapter rehabAdapter;
    private AppointmentAdapter appointmentAdapter;
    private ReportsAdapter reportsAdapter;
    private SymptomHistoryAdapter symptomHistoryAdapter;

    private int currentPatientId;
    private String currentPatientName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_details);

        initViews();
        setupPatientInfo();
        setupRecyclerViews();
        setupButtons();

        loadReportsFromBackend();
        loadMedicationsFromBackend();
        loadRehabFromBackend();
        loadAppointmentsFromBackend();
        loadCrpHistory();
        loadSymptomHistory();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to screen
        loadMedicationsFromBackend();
        loadRehabFromBackend();
        loadAppointmentsFromBackend();
        loadCrpHistory();
        loadSymptomHistory();
    }

    // ✅ FIXED: Removed syntax error in parameters
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_ADD_REHAB && resultCode == RESULT_OK && data != null) {
            // Get the list of exercises the doctor selected
            ArrayList<Rehab> selectedExercises = data.getParcelableArrayListExtra("selected_rehab");

            // Send them to the backend
            if (selectedExercises != null && !selectedExercises.isEmpty()) {
                saveRehabPlanToBackend(selectedExercises);
            } else {
                Toast.makeText(this, "No exercises selected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveRehabPlanToBackend(List<Rehab> exercises) {
        // Debug logging
        Log.d("RehabAssignment", "Saving rehab plan for patient ID: " + currentPatientId);
        Log.d("RehabAssignment", "Number of exercises: " + exercises.size());

        // 1. Prepare the Payload (JSON Body)
        Map<String, Object> payload = new HashMap<>();
        payload.put("patient_id", currentPatientId);
        payload.put("title", "Assigned Therapy Plan");
        payload.put("description", "Exercises assigned by Doctor for " + currentPatientName);

        // Convert List<Rehab> to List<Map> for JSON
        List<Map<String, Object>> exercisesList = new ArrayList<>();
        for (Rehab r : exercises) {
            Map<String, Object> exMap = new HashMap<>();
            exMap.put("name", r.getName());
            exMap.put("description", r.getDescription());

            // Clean reps to be numeric if possible, just in case DB is still INT
            String repsStr = r.getReps();
            if (repsStr != null) {
                // Extract first number if it contains non-numeric chars (e.g., "10-15 reps" ->
                // "10")
                String onlyDigits = repsStr.replaceAll("[^0-9].*", "");
                if (!onlyDigits.isEmpty()) {
                    exMap.put("reps", onlyDigits);
                } else {
                    exMap.put("reps", "0");
                }
            } else {
                exMap.put("reps", "0");
            }

            exMap.put("frequency_per_week", r.getFrequency());
            exMap.put("sets", 3); // Default if not provided

            exercisesList.add(exMap);
        }
        payload.put("exercises", exercisesList);

        Log.d("RehabAssignment", "Payload: " + payload.toString());
        Toast.makeText(this, "Saving plan...", Toast.LENGTH_SHORT).show();

        // 2. Call the API
        ApiService api = ApiClient.getApiService(this);
        api.createRehabPlan(payload).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call,
                    Response<ApiResponse<Map<String, Object>>> response) {
                Log.d("RehabAssignment", "Response code: " + response.code());
                Log.d("RehabAssignment", "Response body: " + response.body());

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(PatientDetailsActivity.this, "Rehab Plan Assigned Successfully!", Toast.LENGTH_LONG)
                            .show();

                    // Refresh the list on screen
                    loadRehabFromBackend();
                } else {
                    String error = "Failed to save";
                    if (response.body() != null && response.body().getError() != null) {
                        error = response.body().getError().getMessage();
                    }
                    Log.e("RehabAssignment", "Assignment failed: " + error);
                    Toast.makeText(PatientDetailsActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                Log.e("RehabAssignment", "Network error: " + t.getMessage());
                Toast.makeText(PatientDetailsActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    private void initViews() {
        patientName = findViewById(R.id.patientName);
        patientAge = findViewById(R.id.patientAge);
        patientId = findViewById(R.id.patientId);
        patientImage = findViewById(R.id.patientImage);

        medsRecycler = findViewById(R.id.medsRecycler);
        rehabRecycler = findViewById(R.id.rehabRecycler);
        appointmentRecycler = findViewById(R.id.appointmentRecycler);
        reportsRecycler = findViewById(R.id.reportsRecycler);
        symptomHistoryRecycler = findViewById(R.id.symptomHistoryRecycler);

        alertMessage = findViewById(R.id.alertMessage);
        sendAlert = findViewById(R.id.sendAlert);
        editPatientBtn = findViewById(R.id.editPatientBtn);

        btnAssignMedication = findViewById(R.id.btnAssignMedication);
        btnAddRehab = findViewById(R.id.btnAddRehab);
        etDiagnosis = findViewById(R.id.etDiagnosis);
        etSuggestions = findViewById(R.id.etSuggestions);
        btnSaveDiagnosis = findViewById(R.id.btnSaveDiagnosis);
        btnLoadMoreSymptoms = findViewById(R.id.btnLoadMoreSymptoms);

        crpChart = findViewById(R.id.crp_progress_chart);
        crpEmptyState = findViewById(R.id.crp_empty_state);
        symptomEmptyState = findViewById(R.id.symptomEmptyState);
        symptomCount = findViewById(R.id.symptomCount);
    }

    private void setupPatientInfo() {
        Intent intent = getIntent();
        currentPatientId = intent.getIntExtra("patient_id", 0);
        currentPatientName = intent.getStringExtra("patient_name");
        String ageRaw = intent.getStringExtra("patient_age");
        int imageRes = intent.getIntExtra("patient_image", R.drawable.ic_person_default);

        // Debug logging
        Log.d("PatientDetails", "Received patient_id: " + currentPatientId);
        Log.d("PatientDetails", "Received patient_name: " + currentPatientName);

        if (currentPatientId == 0) {
            Log.e("PatientDetails", "Invalid patient ID received!");
            Toast.makeText(this, "Invalid patient ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        patientName.setText(currentPatientName != null ? currentPatientName : "Unknown");

        patientId.setText("ID: " + (currentPatientId == 0 ? "--" : currentPatientId));

        if (ageRaw != null && !ageRaw.trim().isEmpty()) {
            patientAge.setText("Age: " + ageRaw.replace("Years", "").trim() + " Years");
        } else {
            patientAge.setText("Age: Not Provided");
        }

        patientImage.setImageResource(imageRes);
    }

    private void setupRecyclerViews() {
        medicationsList = new ArrayList<>();
        rehabList = new ArrayList<>();
        appointmentList = new ArrayList<>();
        reportsList = new ArrayList<>();
        symptomHistoryList = new ArrayList<>();

        medicationsAdapter = new MedicationsAdapter(this, medicationsList);
        medicationsAdapter.setDoctorView(true);
        medicationsAdapter.setOnDeleteListener(medication -> {
            deleteMedication(medication);
        });
        medsRecycler.setLayoutManager(new LinearLayoutManager(this));
        medsRecycler.setAdapter(medicationsAdapter);

        rehabAdapter = new RehabAdapter(this, rehabList);
        rehabAdapter.setDoctorView(true);
        rehabAdapter.setOnDeleteClickListener(rehab -> {
            deleteRehabExercise(rehab);
        });
        rehabRecycler.setLayoutManager(new LinearLayoutManager(this));
        rehabRecycler.setAdapter(rehabAdapter);

        appointmentAdapter = new AppointmentAdapter(this, appointmentList);
        appointmentRecycler.setLayoutManager(new LinearLayoutManager(this));
        appointmentRecycler.setAdapter(appointmentAdapter);

        reportsAdapter = new ReportsAdapter(this, reportsList);
        reportsRecycler.setLayoutManager(new LinearLayoutManager(this));
        reportsRecycler.setAdapter(reportsAdapter);

        // Setup symptom history RecyclerView
        symptomHistoryAdapter = new SymptomHistoryAdapter(this, symptomHistoryList);
        symptomHistoryRecycler.setLayoutManager(new LinearLayoutManager(this));
        symptomHistoryRecycler.setAdapter(symptomHistoryAdapter);
    }

    private void setupButtons() {

        editPatientBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditPatientActivity.class);
            intent.putExtra("patient_id", currentPatientId);
            intent.putExtra("patient_name", currentPatientName);
            intent.putExtra("patient_age", patientAge.getText().toString().replace("Age: ", ""));
            startActivity(intent);
        });

        btnAssignMedication.setOnClickListener(v -> {
            if (currentPatientId == 0) {
                Toast.makeText(this, "Invalid patient", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, com.example.myrajourney.doctor.meds.DoctorAssignMedicationActivity.class);
            intent.putExtra("patient_id", currentPatientId);
            intent.putExtra("patient_name", currentPatientName);
            startActivity(intent);
        });

        btnAddRehab.setOnClickListener(v -> {
            if (currentPatientId == 0) {
                Toast.makeText(this, "Invalid patient", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, com.example.myrajourney.common.rehab.AddRehabActivity.class);
            startActivityForResult(intent, REQ_ADD_REHAB);
        });

        sendAlert.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(alertMessage.getText().toString().trim())) {
                Toast.makeText(this, "Alert sent", Toast.LENGTH_SHORT).show();
                alertMessage.setText("");
            }
        });

        btnSaveDiagnosis.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(etDiagnosis.getText().toString().trim())) {
                Toast.makeText(this, "Diagnosis saved", Toast.LENGTH_SHORT).show();
                etDiagnosis.setText("");
                etSuggestions.setText("");
            }
        });

        reportsAdapter.setOnReportClickListener(report -> {
            Intent intent = new Intent(this, ReportDetailsActivity.class);
            intent.putExtra("patient_name", currentPatientName);
            intent.putExtra("report_type", report.getTitle());
            intent.putExtra("report_date", report.getCreatedAt());
            intent.putExtra("report_status", report.getStatus());
            intent.putExtra("report_id", report.getId());
            intent.putExtra("report_file", report.getFileUrl());
            startActivity(intent);
        });

        // Load more symptoms button
        btnLoadMoreSymptoms.setOnClickListener(v -> {
            loadMoreSymptoms();
        });
    }

    // ---------- APPOINTMENTS ----------
    private void loadAppointmentsFromBackend() {
        appointmentList.clear();
        appointmentAdapter.notifyDataSetChanged();

        if (currentPatientId == 0)
            return;

        ApiService api = ApiClient.getApiService(this);

        api.getAppointments(currentPatientId, null).enqueue(new Callback<ApiResponse<List<Appointment>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Appointment>>> call,
                    Response<ApiResponse<List<Appointment>>> response) {

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Appointment> data = response.body().getData();
                    if (data != null)
                        appointmentList.addAll(data);
                }
                appointmentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Appointment>>> call, Throwable t) {
                appointmentAdapter.notifyDataSetChanged();
            }
        });
    }

    // ---------- MEDICATIONS ----------
    private void loadMedicationsFromBackend() {
        if (currentPatientId == 0) {
            medicationsList.clear();
            medicationsAdapter.notifyDataSetChanged();
            return;
        }

        ApiService api = ApiClient.getApiService(this);

        api.getPatientMedications(currentPatientId)
                .enqueue(new Callback<ApiResponse<List<Medication>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<Medication>>> call,
                            Response<ApiResponse<List<Medication>>> response) {

                        medicationsList.clear();

                        if (response.isSuccessful() &&
                                response.body() != null &&
                                response.body().isSuccess()) {

                            List<Medication> list = response.body().getData();
                            if (list != null)
                                medicationsList.addAll(list);
                        }
                        medicationsAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<Medication>>> call, Throwable t) {
                        medicationsList.clear();
                        medicationsAdapter.notifyDataSetChanged();
                    }
                });
    }

    // ---------- REHAB ----------
    private void loadRehabFromBackend() {
        rehabList.clear();
        rehabAdapter.notifyDataSetChanged();

        if (currentPatientId == 0)
            return;

        ApiService api = ApiClient.getApiService(this);

        api.getRehabPlans(currentPatientId)
                .enqueue(new Callback<ApiResponse<List<com.example.myrajourney.data.model.RehabPlan>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<com.example.myrajourney.data.model.RehabPlan>>> call,
                            Response<ApiResponse<List<com.example.myrajourney.data.model.RehabPlan>>> response) {

                        if (response.isSuccessful() &&
                                response.body() != null &&
                                response.body().isSuccess()) {

                            List<com.example.myrajourney.data.model.RehabPlan> plans = response.body().getData();

                            if (plans != null) {
                                for (com.example.myrajourney.data.model.RehabPlan plan : plans) {

                                    List<com.example.myrajourney.data.model.RehabPlan.RehabExercise> exercises = plan
                                            .getExercises();

                                    if (exercises != null && !exercises.isEmpty()) {
                                        for (com.example.myrajourney.data.model.RehabPlan.RehabExercise ex : exercises) {
                                            rehabList.add(
                                                    new Rehab(
                                                            String.valueOf(ex.getId()),
                                                            ex.getName(),
                                                            ex.getDescription(),
                                                            ex.getReps(),
                                                            ex.getFrequencyPerWeek(),
                                                            plan.getVideoUrl(),
                                                            ""));
                                        }
                                    }
                                }
                            }
                        }

                        rehabAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<com.example.myrajourney.data.model.RehabPlan>>> call,
                            Throwable t) {
                        rehabAdapter.notifyDataSetChanged();
                    }
                });
    }

    // ---------- REPORTS ----------
    private void loadReportsFromBackend() {
        reportsList.clear();
        reportsAdapter.notifyDataSetChanged();

        if (currentPatientId == 0)
            return;

        ApiService api = ApiClient.getApiService(this);

        api.getReports().enqueue(new Callback<ApiResponse<List<Report>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Report>>> call,
                    Response<ApiResponse<List<Report>>> response) {

                if (response.isSuccessful() &&
                        response.body() != null &&
                        response.body().getData() != null) {

                    for (Report r : response.body().getData()) {

                        if (String.valueOf(r.getPatientId()).equals(String.valueOf(currentPatientId))) {

                            Report model = new Report();
                            model.setId(r.getId());
                            model.setPatientId(r.getPatientId());
                            model.setPatientName(currentPatientName);
                            model.setTitle(r.getTitle());
                            model.setFileUrl(r.getFileUrl());
                            model.setStatus(r.getStatus());
                            model.setCreatedAt(r.getCreatedAt());

                            reportsList.add(model);
                        }
                    }
                    reportsAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Report>>> call, Throwable t) {
            }
        });
    }

    // ---------- CRP PROGRESS ----------
    private void loadCrpHistory() {
        if (currentPatientId == 0) {
            showEmptyState();
            return;
        }

        ApiService api = ApiClient.getApiService(this);

        api.getCrpHistory(currentPatientId).enqueue(new Callback<ApiResponse<List<CrpData>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<CrpData>>> call,
                    Response<ApiResponse<List<CrpData>>> response) {

                Log.d("CRP_API", "CRP API response received. HTTP code: " + response.code());

                if (response.isSuccessful() &&
                        response.body() != null &&
                        response.body().isSuccess()) {

                    List<CrpData> data = response.body().getData();
                    Log.d("CRP_API", "CRP data received: " + (data != null ? data.size() : 0) + " items");
                    
                    if (data != null && !data.isEmpty()) {
                        // Log first few data points for debugging
                        for (int i = 0; i < Math.min(3, data.size()); i++) {
                            CrpData crp = data.get(i);
                            Log.d("CRP_API", "CRP data " + i + ": value=" + crp.getCrpValue() + 
                                  ", date=" + crp.getMeasurementDate() + 
                                  ", unit=" + crp.getMeasurementUnit());
                        }
                        displayCrpGraph(data);
                    } else {
                        Log.d("CRP_API", "No CRP data available, showing empty state");
                        showEmptyState();
                    }
                } else {
                    Log.e("CRP_API", "CRP API request failed. Success: " + 
                          (response.body() != null ? response.body().isSuccess() : "null body"));
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<CrpData>>> call, Throwable t) {
                Log.e("CRP_API", "CRP API request failed with exception: " + t.getMessage());
                showEmptyState();
            }
        });
    }

    private void displayCrpGraph(List<CrpData> data) {
        // Add debugging logs
        Log.d("CRP_GRAPH", "displayCrpGraph called with " + data.size() + " data points");
        
        crpChart.setVisibility(android.view.View.VISIBLE);
        crpEmptyState.setVisibility(android.view.View.GONE);

        List<Entry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        for (int i = 0; i < data.size(); i++) {
            CrpData crp = data.get(i);
            float crpValue = crp.getCrpValue();
            
            // Debug logging for each data point
            Log.d("CRP_GRAPH", "Data point " + i + ": CRP value = " + crpValue + ", Date = " + crp.getMeasurementDate());
            
            entries.add(new Entry(i, crpValue));

            // Color based on value
            colors.add(crp.getColorInt());
        }

        // Debug the entries that will be plotted
        Log.d("CRP_GRAPH", "Created " + entries.size() + " entries for graph");
        for (int i = 0; i < entries.size(); i++) {
            Entry entry = entries.get(i);
            Log.d("CRP_GRAPH", "Entry " + i + ": X=" + entry.getX() + ", Y=" + entry.getY());
        }

        LineDataSet dataSet = new LineDataSet(entries, "CRP Levels (mg/L)");
        dataSet.setCircleColors(colors);
        dataSet.setCircleRadius(5f);
        dataSet.setLineWidth(2f);
        dataSet.setValueTextSize(10f);
        dataSet.setColor(Color.BLUE);
        dataSet.setDrawValues(true);

        // Highlight last point
        if (entries.size() > 0) {
            dataSet.setCircleRadius(5f);
            dataSet.setHighlightEnabled(true);
        }

        LineData lineData = new LineData(dataSet);
        crpChart.setData(lineData);

        // Add reference line at 10 mg/L
        LimitLine normalLine = new LimitLine(10f, "Normal Threshold");
        normalLine.setLineColor(Color.GRAY);
        normalLine.setLineWidth(1f);
        normalLine.setTextColor(Color.GRAY);
        normalLine.setTextSize(10f);

        YAxis leftAxis = crpChart.getAxisLeft();
        leftAxis.removeAllLimitLines();
        leftAxis.addLimitLine(normalLine);
        leftAxis.setAxisMinimum(0f);
        float maxCrp = getMaxCrp(data);
        float axisMax = Math.max(100f, maxCrp + 20);
        leftAxis.setAxisMaximum(axisMax);
        
        // Debug axis settings
        Log.d("CRP_GRAPH", "Y-axis: min=0, max=" + axisMax + ", maxCrp=" + maxCrp);

        // Disable right axis
        crpChart.getAxisRight().setEnabled(false);

        // X-axis labels (dates)
        XAxis xAxis = crpChart.getXAxis();
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < data.size()) {
                    String formattedDate = data.get(index).getFormattedDate();
                    Log.d("CRP_GRAPH", "X-axis label for index " + index + ": " + formattedDate);
                    return formattedDate;
                }
                return "";
            }
        });
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        crpChart.getDescription().setEnabled(false);
        crpChart.getLegend().setEnabled(true);
        crpChart.invalidate();
        
        Log.d("CRP_GRAPH", "Graph setup complete, invalidate() called");
    }

    private float getMaxCrp(List<CrpData> data) {
        float max = 0;
        for (CrpData crp : data) {
            if (crp.getCrpValue() > max)
                max = crp.getCrpValue();
        }
        return max;
    }

    private void deleteMedication(Medication medication) {
        if (medication.getId() == null || medication.getId().isEmpty()) {
            Toast.makeText(this, "Invalid medication ID", Toast.LENGTH_SHORT).show();
            return;
        }

        new android.app.AlertDialog.Builder(this)
                .setTitle("Remove Medication")
                .setMessage("Are you sure you want to remove " + medication.getName() + "?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    ApiService api = ApiClient.getApiService(this);
                    // Trying REST endpoint first
                    api.deleteMedication(medication.getId()).enqueue(new Callback<ApiResponse<Void>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(PatientDetailsActivity.this, "Medication removed", Toast.LENGTH_SHORT)
                                        .show();
                                loadMedicationsFromBackend();
                            } else {
                                // Fallback to PHP endpoint if REST fails 404/etc?
                                // Or just log error.
                                Toast.makeText(PatientDetailsActivity.this, "Failed to remove medication",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                            Toast.makeText(PatientDetailsActivity.this, "Network error: " + t.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteRehabExercise(Rehab rehab) {
        if (rehab.getId() == null)
            return;

        try {
            int exId = Integer.parseInt(rehab.getId());
            ApiService api = ApiClient.getApiService(this);
            api.deleteRehabExercise(exId).enqueue(new Callback<ApiResponse<Void>>() {
                @Override
                public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(PatientDetailsActivity.this, "Exercise removed", Toast.LENGTH_SHORT).show();
                        loadRehabFromBackend();
                    } else {
                        Toast.makeText(PatientDetailsActivity.this, "Failed to remove exercise", Toast.LENGTH_SHORT)
                                .show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                    Toast.makeText(PatientDetailsActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (NumberFormatException e) {
            Log.e("PatientDetails", "Invalid exercise ID: " + rehab.getId());
        }
    }

    private void showEmptyState() {
        crpChart.setVisibility(android.view.View.GONE);
        crpEmptyState.setVisibility(android.view.View.VISIBLE);
    }

    // ---------- SYMPTOM HISTORY ----------
    private int currentSymptomPage = 1;
    private final int SYMPTOMS_PER_PAGE = 10;
    private boolean isLoadingSymptoms = false;

    private void loadSymptomHistory() {
        currentSymptomPage = 1;
        symptomHistoryList.clear();
        loadSymptomsFromApi(currentSymptomPage, false);
    }

    private void loadMoreSymptoms() {
        if (!isLoadingSymptoms) {
            currentSymptomPage++;
            loadSymptomsFromApi(currentSymptomPage, true);
        }
    }

    private void loadSymptomsFromApi(int page, boolean isLoadMore) {
        if (currentPatientId == 0) {
            updateSymptomUI();
            return;
        }

        isLoadingSymptoms = true;
        if (isLoadMore) {
            btnLoadMoreSymptoms.setText("Loading...");
            btnLoadMoreSymptoms.setEnabled(false);
        }

        ApiService api = ApiClient.getApiService(this);

        api.getSymptomHistory(currentPatientId).enqueue(new Callback<ApiResponse<List<SymptomHistory>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<SymptomHistory>>> call,
                    Response<ApiResponse<List<SymptomHistory>>> response) {

                isLoadingSymptoms = false;
                btnLoadMoreSymptoms.setText("Load More Symptoms");
                btnLoadMoreSymptoms.setEnabled(true);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<SymptomHistory> newSymptoms = response.body().getData();
                    
                    if (newSymptoms != null && !newSymptoms.isEmpty()) {
                        if (!isLoadMore) {
                            symptomHistoryList.clear();
                        }
                        
                        // Add new symptoms to the list
                        symptomHistoryList.addAll(newSymptoms);
                        
                        // Sort by date (most recent first)
                        symptomHistoryList.sort((s1, s2) -> {
                            try {
                                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                                java.util.Date date1 = sdf.parse(s1.getDate());
                                java.util.Date date2 = sdf.parse(s2.getDate());
                                return date2.compareTo(date1); // Descending order (recent first)
                            } catch (Exception e) {
                                return 0;
                            }
                        });
                        
                        // Show/hide load more button based on results
                        if (newSymptoms.size() < SYMPTOMS_PER_PAGE) {
                            btnLoadMoreSymptoms.setVisibility(android.view.View.GONE);
                        } else {
                            btnLoadMoreSymptoms.setVisibility(android.view.View.VISIBLE);
                        }
                    } else {
                        if (isLoadMore) {
                            btnLoadMoreSymptoms.setVisibility(android.view.View.GONE);
                        }
                    }
                } else {
                    Log.e("SymptomHistory", "Failed to load symptoms: " + response.message());
                    if (isLoadMore) {
                        currentSymptomPage--; // Revert page increment on failure
                    }
                }
                
                updateSymptomUI();
            }

            @Override
            public void onFailure(Call<ApiResponse<List<SymptomHistory>>> call, Throwable t) {
                isLoadingSymptoms = false;
                btnLoadMoreSymptoms.setText("Load More Symptoms");
                btnLoadMoreSymptoms.setEnabled(true);
                
                Log.e("SymptomHistory", "Network error loading symptoms: " + t.getMessage());
                
                if (isLoadMore) {
                    currentSymptomPage--; // Revert page increment on failure
                }
                
                updateSymptomUI();
            }
        });
    }

    private void updateSymptomUI() {
        if (symptomHistoryList.isEmpty()) {
            // Show empty state
            symptomEmptyState.setVisibility(android.view.View.VISIBLE);
            symptomHistoryRecycler.setVisibility(android.view.View.GONE);
            btnLoadMoreSymptoms.setVisibility(android.view.View.GONE);
            symptomCount.setText("0 entries");
        } else {
            // Show symptom list
            symptomEmptyState.setVisibility(android.view.View.GONE);
            symptomHistoryRecycler.setVisibility(android.view.View.VISIBLE);
            
            // Update count
            int count = symptomHistoryList.size();
            symptomCount.setText(count + (count == 1 ? " entry" : " entries"));
            
            // Update adapter
            symptomHistoryAdapter.updateSymptoms(symptomHistoryList);
        }
    }
}