package com.example.myrajourney.doctor.patients;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.myrajourney.core.session.SessionManager;
import android.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myrajourney.R;
import com.example.myrajourney.admin.users.CreatePatientActivity;
import com.example.myrajourney.core.network.ApiClient;
import com.example.myrajourney.core.network.ApiService;
import com.example.myrajourney.core.session.TokenManager;
import com.example.myrajourney.core.ui.ThemeManager;
import com.example.myrajourney.data.model.ApiResponse;
import com.example.myrajourney.data.model.Patient;
import com.example.myrajourney.data.model.User;
import com.example.myrajourney.doctor.appointments.DoctorScheduleActivity;
import com.example.myrajourney.doctor.reports.DoctorReportsActivity;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AllPatientsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private LinearLayout emptyView;
    private ExtendedFloatingActionButton fabAddPatient;

    private List<Patient> patientList, filteredList;
    private PatientsAdapter adapter;

    private DrawerLayout drawerLayout;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_patients);

        sessionManager = SessionManager.getInstance(this);
        initViews();
        setupNavigation();
        setupNavigationDrawer();

        // Initialize List
        patientList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new PatientsAdapter(this, filteredList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Load Data
        loadPatientsFromBackend();

        // Load Data
        loadPatientsFromBackend();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.all_patients_recycler);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.empty_view);
        fabAddPatient = findViewById(R.id.fab_add_patient);
        drawerLayout = findViewById(R.id.drawer_layout);

        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        fabAddPatient.setOnClickListener(v -> startActivity(new Intent(this, CreatePatientActivity.class)));
    }

    private void loadPatientsFromBackend() {
        progressBar.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);

        ApiService apiService = ApiClient.getApiService(this);

        // ✅ CORRECTED: Use getAllPatients() to fetch assigned patients
        // This maps to GET /api/v1/patients which is allowed for Doctors
        Call<ApiResponse<List<User>>> call = apiService.getAllPatients();

        call.enqueue(new Callback<ApiResponse<List<User>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<User>>> call,
                    @NonNull Response<ApiResponse<List<User>>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<User> users = response.body().getData();
                    patientList.clear();

                    if (users != null) {
                        for (User user : users) {
                            if ("PATIENT".equalsIgnoreCase(user.getRole())) {
                                String age = (user.getAge() != null && !user.getAge().isEmpty()) ? user.getAge()
                                        : "N/A";

                                Patient p = new Patient(
                                        user.getId(),
                                        user.getName(),
                                        user.getEmail(),
                                        age);
                                patientList.add(p);
                            }
                        }
                    }

                    filteredList.clear();
                    filteredList.addAll(patientList);
                    adapter.notifyDataSetChanged();
                    updateEmptyView();
                } else {
                    String errorMsg = "Failed to load patients";
                    if (response.code() == 403)
                        errorMsg = "Access Denied: Unauthorized";
                    Toast.makeText(AllPatientsActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    updateEmptyView();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<User>>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AllPatientsActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                updateEmptyView();
            }
        });
    }

    private void updateEmptyView() {
        if (filteredList.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void setupNavigationDrawer() {
        // Find views in the new custom drawer layout
        TextView navHeaderName = findViewById(R.id.navHeaderDoctorName);
        TextView navHeaderEmail = findViewById(R.id.navHeaderDoctorEmail);

        String name = sessionManager.getUserName();
        String email = sessionManager.getUserEmail();

        if (navHeaderName != null && name != null)
            navHeaderName.setText("Dr. " + name);
        if (navHeaderEmail != null && email != null)
            navHeaderEmail.setText(email);

        // Map menu items to click listeners
        View.OnClickListener menuClickListener = v -> {
            int id = v.getId();

            if (id == R.id.menu_add_patient) {
                startActivity(new Intent(this, CreatePatientActivity.class));
            } else if (id == R.id.menu_all_patients) {
                // Already on all patients
            } else if (id == R.id.menu_schedule) {
                startActivity(new Intent(this, DoctorScheduleActivity.class));
            } else if (id == R.id.menu_add_appointment) {
                // Borrowing AddAppointmentActivity
                startActivity(
                        new Intent(this, com.example.myrajourney.common.appointments.AddAppointmentActivity.class));
            } else if (id == R.id.menu_reports) {
                startActivity(new Intent(this, DoctorReportsActivity.class));
            } else if (id == R.id.menu_assign_exercises) {
                // Use the same AllPatientsActivity or similar
                startActivity(new Intent(this, AllPatientsActivity.class));
            } else if (id == R.id.menu_settings) {
                startActivity(new Intent(this, com.example.myrajourney.admin.dashboard.SettingsActivity.class));
            } else if (id == R.id.menu_dark_theme) {
                ThemeManager.toggleTheme(this);
                recreate();
            } else if (id == R.id.menu_logout || id == R.id.nav_logout_icon) {
                showLogoutDialog();
            }

            if (drawerLayout != null) {
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        };

        // Assign listeners
        int[] itemIds = {
                R.id.menu_add_patient, R.id.menu_all_patients, R.id.menu_schedule,
                R.id.menu_add_appointment, R.id.menu_assign_exercises, R.id.menu_reports,
                R.id.menu_settings, R.id.menu_dark_theme, R.id.menu_logout, R.id.nav_logout_icon
        };

        for (int id : itemIds) {
            View itemView = findViewById(id);
            if (itemView != null) {
                itemView.setOnClickListener(menuClickListener);
            }
        }

        highlightNavMenuItem(R.id.menu_all_patients);
    }

    private void highlightNavMenuItem(int selectedId) {
        int[] itemIds = {
                R.id.menu_add_patient, R.id.menu_all_patients, R.id.menu_schedule,
                R.id.menu_add_appointment, R.id.menu_assign_exercises, R.id.menu_reports,
                R.id.menu_settings, R.id.menu_dark_theme, R.id.menu_logout
        };

        for (int id : itemIds) {
            View itemView = findViewById(id);
            if (itemView != null) {
                if (id == selectedId) {
                    itemView.setBackgroundResource(R.drawable.nav_item_selected_bg);
                    int padding = (int) (8 * getResources().getDisplayMetrics().density);
                    itemView.setPadding(padding, padding, padding, padding);
                } else {
                    itemView.setBackgroundResource(0);
                    itemView.setPadding(0, 0, 0, 0); // Rely on Style margin
                }
            }
        }
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    sessionManager.logout();
                    Intent intent = new Intent(this, com.example.myrajourney.auth.LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void setupNavigation() {
        // Legacy NavigationView listener removed as we use custom drawer now.
        // If this activity needs a drawer, it should be updated to use the custom
        // template.
        // For now, removing to prevent crashes.

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START))
                    drawerLayout.closeDrawer(GravityCompat.START);
                else
                    finish();
            }
        });
    }
}