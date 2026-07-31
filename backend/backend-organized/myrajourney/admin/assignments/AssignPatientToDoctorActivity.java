package com.example.myrajourney.admin.assignments;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myrajourney.R;
import com.example.myrajourney.core.network.ApiClient;
import com.example.myrajourney.core.network.ApiService;
import com.example.myrajourney.core.ui.ThemeManager;
import com.example.myrajourney.data.model.ApiResponse;
import com.example.myrajourney.data.model.Doctor;
import com.example.myrajourney.data.model.User;
import com.example.myrajourney.patient.dashboard.PatientAssignmentAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AssignPatientToDoctorActivity extends AppCompatActivity {

    private RecyclerView patientsRecyclerView;
    private ProgressBar progressBar;
    private TextView emptyView;

    private List<User> patientsList = new ArrayList<>();
    private List<Doctor> doctorsList = new ArrayList<>();
    private PatientAssignmentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign_patient_to_doctor);

        // Initialize views
        patientsRecyclerView = findViewById(R.id.patientsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);

        // Setup RecyclerView
        patientsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load data via Retrofit
        loadDoctors();
    }

    private void loadDoctors() {
        ApiService api = ApiClient.getApiService(this);
        api.getAllDoctors().enqueue(new Callback<ApiResponse<List<Doctor>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Doctor>>> call, Response<ApiResponse<List<Doctor>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    doctorsList = response.body().getData();
                    // Once doctors are loaded, load patients
                    loadPatients();
                } else {
                    Toast.makeText(AssignPatientToDoctorActivity.this, "Failed to load doctors", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Doctor>>> call, Throwable t) {
                Toast.makeText(AssignPatientToDoctorActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPatients() {
        progressBar.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);

        ApiService api = ApiClient.getApiService(this);
        api.getAllPatients().enqueue(new Callback<ApiResponse<List<User>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<User>>> call, Response<ApiResponse<List<User>>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    patientsList = response.body().getData();

                    if (patientsList == null || patientsList.isEmpty()) {
                        emptyView.setVisibility(View.VISIBLE);
                        emptyView.setText("No patients found");
                    } else {
                        // Filter out non-patients just in case backend sends mixed users
                        List<User> filteredList = new ArrayList<>();
                        for (User u : patientsList) {
                            if ("PATIENT".equalsIgnoreCase(u.getRole())) {
                                filteredList.add(u);
                            }
                        }

                        adapter = new PatientAssignmentAdapter(filteredList, doctorsList,
                                AssignPatientToDoctorActivity.this::assignPatient);
                        patientsRecyclerView.setAdapter(adapter);
                    }
                } else {
                    emptyView.setVisibility(View.VISIBLE);
                    emptyView.setText("Failed to load patients");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<User>>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
                emptyView.setText("Network Error: " + t.getMessage());
            }
        });
    }

    private void assignPatient(int patientId, Integer doctorId) {
        ApiService api = ApiClient.getApiService(this);

        Map<String, Integer> request = new HashMap<>();
        request.put("patient_id", patientId);
        if (doctorId != null) {
            request.put("doctor_id", doctorId);
        }

        api.assignPatientToDoctor(request).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(AssignPatientToDoctorActivity.this, "Patient assigned successfully!", Toast.LENGTH_LONG).show();
                    // Refresh list to show new status
                    loadPatients();
                } else {
                    String msg = "Assignment failed";
                    if (response.body() != null && response.body().getError() != null) {
                        msg = response.body().getError().getMessage();
                    }
                    Toast.makeText(AssignPatientToDoctorActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Toast.makeText(AssignPatientToDoctorActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}