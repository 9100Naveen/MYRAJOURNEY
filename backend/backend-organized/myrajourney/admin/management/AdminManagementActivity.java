package com.example.myrajourney.admin.management;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.myrajourney.R;
import com.example.myrajourney.core.network.ApiService;
import com.example.myrajourney.data.model.ApiResponse;
import com.example.myrajourney.data.model.User;
import com.example.myrajourney.admin.management.adapters.UserManagementAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Admin management activity for managing doctors and patients
 * Provides comprehensive user management capabilities
 */
public class AdminManagementActivity extends AppCompatActivity implements UserManagementAdapter.OnUserActionListener {
    
    private static final String TAG = "AdminManagementActivity";
    
    private RecyclerView recyclerViewDoctors;
    private RecyclerView recyclerViewPatients;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView tvDoctorCount;
    private TextView tvPatientCount;
    private TextView tvTotalUsers;
    private Button btnAddDoctor;
    private Button btnAddPatient;
    private Button btnExportData;
    private Button btnSystemSettings;
    
    private UserManagementAdapter doctorsAdapter;
    private UserManagementAdapter patientsAdapter;
    private ApiService apiService;
    
    private List<User> doctorsList;
    private List<User> patientsList;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_management);
        
        initializeViews();
        initializeApiService();
        setupRecyclerViews();
        setupClickListeners();
        loadUserData();
    }
    
    private void initializeViews() {
        recyclerViewDoctors = findViewById(R.id.recycler_view_doctors);
        recyclerViewPatients = findViewById(R.id.recycler_view_patients);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        tvDoctorCount = findViewById(R.id.tv_doctor_count);
        tvPatientCount = findViewById(R.id.tv_patient_count);
        tvTotalUsers = findViewById(R.id.tv_total_users);
        btnAddDoctor = findViewById(R.id.btn_add_doctor);
        btnAddPatient = findViewById(R.id.btn_add_patient);
        btnExportData = findViewById(R.id.btn_export_data);
        btnSystemSettings = findViewById(R.id.btn_system_settings);
    }
    
    private void initializeApiService() {
        apiService = com.example.myrajourney.core.network.ApiClient.getApiService(this);
    }
    
    private void setupRecyclerViews() {
        doctorsList = new ArrayList<>();
        patientsList = new ArrayList<>();
        
        doctorsAdapter = new UserManagementAdapter(this, doctorsList, "DOCTOR", this);
        patientsAdapter = new UserManagementAdapter(this, patientsList, "PATIENT", this);
        
        recyclerViewDoctors.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewDoctors.setAdapter(doctorsAdapter);
        
        recyclerViewPatients.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPatients.setAdapter(patientsAdapter);
    }
    
    private void setupClickListeners() {
        swipeRefreshLayout.setOnRefreshListener(this::loadUserData);
        
        btnAddDoctor.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddUserActivity.class);
            intent.putExtra("user_type", "DOCTOR");
            startActivity(intent);
        });
        
        btnAddPatient.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddUserActivity.class);
            intent.putExtra("user_type", "PATIENT");
            startActivity(intent);
        });
        
        btnExportData.setOnClickListener(v -> exportUserData());
        btnSystemSettings.setOnClickListener(v -> openSystemSettings());
    }
    
    private void loadUserData() {
        swipeRefreshLayout.setRefreshing(true);
        
        // Load doctors
        apiService.getAllUsers().enqueue(new Callback<ApiResponse<List<User>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<User>>> call, Response<ApiResponse<List<User>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<User>> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        processUserData(apiResponse.getData());
                    } else {
                        showError("Failed to load user data: " + apiResponse.getMessage());
                    }
                } else {
                    showError("Failed to load user data");
                }
                swipeRefreshLayout.setRefreshing(false);
            }
            
            @Override
            public void onFailure(Call<ApiResponse<List<User>>> call, Throwable t) {
                showError("Network error: " + t.getMessage());
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }
    
    private void processUserData(List<User> users) {
        doctorsList.clear();
        patientsList.clear();
        
        for (User user : users) {
            if ("DOCTOR".equals(user.getRole())) {
                doctorsList.add(user);
            } else if ("PATIENT".equals(user.getRole())) {
                patientsList.add(user);
            }
        }
        
        doctorsAdapter.notifyDataSetChanged();
        patientsAdapter.notifyDataSetChanged();
        
        updateStatistics();
    }
    
    private void updateStatistics() {
        int doctorCount = doctorsList.size();
        int patientCount = patientsList.size();
        int totalCount = doctorCount + patientCount;
        
        tvDoctorCount.setText(String.valueOf(doctorCount));
        tvPatientCount.setText(String.valueOf(patientCount));
        tvTotalUsers.setText(String.valueOf(totalCount));
    }
    
    @Override
    public void onEditUser(User user) {
        Intent intent = new Intent(this, EditUserActivity.class);
        intent.putExtra("user_id", user.getId());
        intent.putExtra("user_type", user.getRole());
        startActivity(intent);
    }
    
    @Override
    public void onDeleteUser(User user) {
        new AlertDialog.Builder(this)
            .setTitle("Delete User")
            .setMessage("Are you sure you want to delete " + user.getName() + "? This action cannot be undone.")
            .setPositiveButton("Delete", (dialog, which) -> performDeleteUser(user))
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    @Override
    public void onViewUserDetails(User user) {
        Intent intent = new Intent(this, UserDetailsActivity.class);
        intent.putExtra("user_id", user.getId());
        startActivity(intent);
    }
    
    @Override
    public void onToggleUserStatus(User user) {
        String action = user.isActive() ? "deactivate" : "activate";
        
        new AlertDialog.Builder(this)
            .setTitle("Change User Status")
            .setMessage("Are you sure you want to " + action + " " + user.getName() + "?")
            .setPositiveButton("Confirm", (dialog, which) -> performToggleUserStatus(user))
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void performDeleteUser(User user) {
        apiService.deleteUser(user.getId()).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Void> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess()) {
                        Toast.makeText(AdminManagementActivity.this, 
                                     "User deleted successfully", Toast.LENGTH_SHORT).show();
                        loadUserData(); // Refresh data
                    } else {
                        showError("Failed to delete user: " + apiResponse.getMessage());
                    }
                } else {
                    showError("Failed to delete user");
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                showError("Network error: " + t.getMessage());
            }
        });
    }
    
    private void performToggleUserStatus(User user) {
        UpdateUserStatusRequest request = new UpdateUserStatusRequest(!user.isActive());
        
        apiService.updateUserStatus(user.getId(), request).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Void> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess()) {
                        String status = !user.isActive() ? "activated" : "deactivated";
                        Toast.makeText(AdminManagementActivity.this, 
                                     "User " + status + " successfully", Toast.LENGTH_SHORT).show();
                        loadUserData(); // Refresh data
                    } else {
                        showError("Failed to update user status: " + apiResponse.getMessage());
                    }
                } else {
                    showError("Failed to update user status");
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                showError("Network error: " + t.getMessage());
            }
        });
    }
    
    private void exportUserData() {
        new AlertDialog.Builder(this)
            .setTitle("Export User Data")
            .setMessage("Choose export format:")
            .setPositiveButton("CSV", (dialog, which) -> exportDataAsCSV())
            .setNeutralButton("JSON", (dialog, which) -> exportDataAsJSON())
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void exportDataAsCSV() {
        // TODO: Implement CSV export
        Toast.makeText(this, "CSV export functionality will be implemented", Toast.LENGTH_SHORT).show();
    }
    
    private void exportDataAsJSON() {
        // TODO: Implement JSON export
        Toast.makeText(this, "JSON export functionality will be implemented", Toast.LENGTH_SHORT).show();
    }
    
    private void openSystemSettings() {
        Intent intent = new Intent(this, SystemSettingsActivity.class);
        startActivity(intent);
    }
    
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning from other activities
        loadUserData();
    }
    
    // Data classes for API requests
    public static class UpdateUserStatusRequest {
        private boolean active;
        
        public UpdateUserStatusRequest(boolean active) {
            this.active = active;
        }
        
        public boolean isActive() {
            return active;
        }
        
        public void setActive(boolean active) {
            this.active = active;
        }
    }
}