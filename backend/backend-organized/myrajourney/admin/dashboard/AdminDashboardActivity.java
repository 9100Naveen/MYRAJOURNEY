package com.example.myrajourney.admin.dashboard;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import android.view.View;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.myrajourney.R;
import com.example.myrajourney.admin.assignments.AssignPatientToDoctorActivity;
import com.example.myrajourney.admin.users.CreateDoctorActivity;
import com.example.myrajourney.admin.users.CreatePatientActivity;
import com.example.myrajourney.admin.users.EditDoctorActivity;
import com.example.myrajourney.admin.users.EditPatientActivity;
import com.example.myrajourney.admin.utils.MedicationClearanceRunner;
import com.example.myrajourney.auth.LoginActivity;
import com.example.myrajourney.core.session.SessionManager;
import com.example.myrajourney.core.ui.ThemeManager;
import com.example.myrajourney.doctor.patients.AllPatientsActivity;
import com.example.myrajourney.doctor.appointments.DoctorScheduleActivity;
import com.example.myrajourney.common.appointments.AddAppointmentActivity;
import com.example.myrajourney.doctor.reports.DoctorReportsActivity;

public class AdminDashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private View navDrawerContainer;

    private ImageView iconMenu, logoutBtn;

    private LinearLayout navUpdatePatient, navUpdateDoctor, navSettings;
    private Button btnCreatePatient, btnCreateDoctor, btnAssignPatients, btnViewAllPatients, btnClearMedications;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);

        // Fix status bar overlap
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.primary_color));
        }

        setContentView(R.layout.activity_admin_dashboard);

        sessionManager = SessionManager.getInstance(this);

        // Redirect if session is invalid
        if (!sessionManager.isLoggedIn() || !sessionManager.isSessionValid()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initViews();
        setupDrawer();
        setupButtons();
        setupBackPressHandler();
    }

    private void initViews() {

        drawerLayout = findViewById(R.id.drawerLayout);
        navDrawerContainer = findViewById(R.id.navDrawerContainer);

        iconMenu = findViewById(R.id.iconMenu);
        logoutBtn = findViewById(R.id.logoutBtn);

        btnCreatePatient = findViewById(R.id.btnCreatePatient);
        btnCreateDoctor = findViewById(R.id.btnCreateDoctor);
        btnAssignPatients = findViewById(R.id.btnAssignPatients);
        btnViewAllPatients = findViewById(R.id.btnViewAllPatients);
        btnClearMedications = findViewById(R.id.btnClearMedications);

        navUpdatePatient = findViewById(R.id.navUpdatePatient);
        navUpdateDoctor = findViewById(R.id.navUpdateDoctor);
        navSettings = findViewById(R.id.navSettings);
    }

    private void setupDrawer() {
        iconMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // Find views in the new custom drawer layout
        TextView navHeaderName = findViewById(R.id.navHeaderAdminName);
        TextView navHeaderEmail = findViewById(R.id.navHeaderAdminEmail);

        String name = sessionManager.getUserName();
        String email = sessionManager.getUserEmail();

        if (navHeaderName != null && name != null)
            navHeaderName.setText(name);
        if (navHeaderEmail != null && email != null)
            navHeaderEmail.setText(email);

        // Map menu items to click listeners
        View.OnClickListener menuClickListener = v -> {
            int id = v.getId();

            if (id == R.id.menu_add_patient) {
                startActivity(new Intent(this, CreatePatientActivity.class));
            } else if (id == R.id.menu_all_patients) {
                startActivity(new Intent(this, AllPatientsActivity.class));
            } else if (id == R.id.menu_schedule) {
                // Borrowing Doctor's activities as before
                startActivity(new Intent(this, DoctorScheduleActivity.class));
            } else if (id == R.id.menu_add_appointment) {
                startActivity(new Intent(this, AddAppointmentActivity.class));
            } else if (id == R.id.menu_reports) {
                startActivity(new Intent(this, DoctorReportsActivity.class));
            } else if (id == R.id.menu_assign_exercises) {
                startActivity(new Intent(this, AllPatientsActivity.class));
            } else if (id == R.id.menu_settings) {
                Toast.makeText(this, "Settings coming soon", Toast.LENGTH_SHORT).show();
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
                R.id.menu_settings, R.id.menu_dark_theme, R.id.menu_logout
        };

        for (int id : itemIds) {
            View itemView = findViewById(id);
            if (itemView != null) {
                itemView.setOnClickListener(menuClickListener);
            }
        }

        View logoutHeaderIcon = findViewById(R.id.nav_logout_icon);
        if (logoutHeaderIcon != null) {
            logoutHeaderIcon.setOnClickListener(menuClickListener);
        }

        logoutBtn.setOnClickListener(v -> showLogoutDialog());

        highlightNavMenuItem(-1); // Admin doesn't have a "Dashboard" menu item in the drawer currently, or I
                                  // missed it.
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

    private void setupButtons() {

        btnCreatePatient.setOnClickListener(v -> startActivity(new Intent(this, CreatePatientActivity.class)));

        btnCreateDoctor.setOnClickListener(v -> startActivity(new Intent(this, CreateDoctorActivity.class)));

        btnAssignPatients.setOnClickListener(v -> startActivity(new Intent(this, AssignPatientToDoctorActivity.class)));

        btnViewAllPatients.setOnClickListener(v -> startActivity(new Intent(this, AllPatientsActivity.class)));

        btnClearMedications.setOnClickListener(v -> showClearMedicationsDialog());

        navUpdatePatient.setOnClickListener(v -> startActivity(new Intent(this, EditPatientActivity.class)));

        navUpdateDoctor.setOnClickListener(v -> startActivity(new Intent(this, EditDoctorActivity.class)));

        navSettings.setOnClickListener(v -> {
            // Handle settings - for now just show a toast
            Toast.makeText(this, "Settings coming soon", Toast.LENGTH_SHORT).show();
        });
    }

    private void showClearMedicationsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("⚠️ Clear All Medication Assignments")
                .setMessage(
                        "This will permanently delete ALL medication assignments for ALL patients.\n\nThis action cannot be undone and will reset the medication system for fresh assignments.\n\nAre you sure you want to continue?")
                .setPositiveButton("Yes, Clear All", (dialog, which) -> {
                    Toast.makeText(this, "Clearing all medication assignments...", Toast.LENGTH_LONG).show();

                    // Execute clearance in background
                    MedicationClearanceRunner.executeWithCallback(this,
                            new MedicationClearanceRunner.ClearanceCallback() {
                                @Override
                                public void onComplete(boolean success, String message) {
                                    runOnUiThread(() -> {
                                        if (success) {
                                            new AlertDialog.Builder(AdminDashboardActivity.this)
                                                    .setTitle("✅ Success")
                                                    .setMessage(
                                                            "All medication assignments have been cleared successfully!\n\nThe system is now ready for fresh medication assignments.")
                                                    .setPositiveButton("OK", null)
                                                    .show();
                                        } else {
                                            new AlertDialog.Builder(AdminDashboardActivity.this)
                                                    .setTitle("❌ Error")
                                                    .setMessage(
                                                            "Failed to clear all medication assignments:\n\n" + message)
                                                    .setPositiveButton("OK", null)
                                                    .show();
                                        }
                                    });
                                }
                            });
                })
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void setupBackPressHandler() {

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return;
                }

                finishAffinity();
            }
        });
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    sessionManager.logout();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }
}
