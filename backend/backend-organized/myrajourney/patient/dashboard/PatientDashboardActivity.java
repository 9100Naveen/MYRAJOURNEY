package com.example.myrajourney.patient.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myrajourney.R;
import com.example.myrajourney.common.messaging.ChatAdapter;
import com.example.myrajourney.common.messaging.ChatBot;
import com.example.myrajourney.common.messaging.ChatMessage;
import com.example.myrajourney.core.navigation.NavigationManager;
import com.example.myrajourney.core.session.SessionManager;
import com.example.myrajourney.core.ui.ThemeManager;
import com.example.myrajourney.data.model.Appointment;

import com.example.myrajourney.patient.appointments.PatientAppointmentsActivity;
import com.example.myrajourney.patient.education.EducationHubActivity;
import com.example.myrajourney.patient.medications.PatientMedicationsActivity;
import com.example.myrajourney.patient.rehab.PatientRehabilitationActivity;
import com.example.myrajourney.patient.reports.ReportList;
import com.example.myrajourney.patient.symptoms.SymptomLogActivity;
import com.example.myrajourney.admin.dashboard.SettingsActivity;

import java.util.ArrayList;
import java.util.List;

public class PatientDashboardActivity extends AppCompatActivity {

    private Button yesBtn, noBtn;
    private ImageView menuIcon, logoutBtn;
    private DrawerLayout drawerLayout;
    private View navDrawerContainer;

    private PatientDashboardViewModel viewModel;

    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
    private ChatBot chatBot;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);

        // Fix status bar overlap
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.primary_color));
        }

        setContentView(R.layout.activity_patient_dashboard_new);

        sessionManager = SessionManager.getInstance(this);

        if (!sessionManager.isSessionValid()) {
            NavigationManager.goToLogin(this);
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(PatientDashboardViewModel.class);

        initializeViews();
        initializeChat();
        setupClickListeners();
        observeViewModel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.refreshAppointments();
    }

    private void observeViewModel() {

        viewModel.getCurrentUser().observe(this, name -> {
            TextView welcomeText = findViewById(R.id.welcomeText);
            if (welcomeText != null) {
                welcomeText.setText(name != null ? "Welcome, " + name + "!" : "Welcome Back!");
            }
        });

        // Removed health metrics observer (UI removed)

        viewModel.getUpcomingAppointments().observe(this, this::updateAppointmentCards);

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null)
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        });
    }

    private void updateAppointmentCards(List<Appointment> appointments) {

        TextView consultationTitle = findViewById(R.id.consultationTitle);
        TextView consultationDate = findViewById(R.id.consultationDate);
        TextView followupTitle = findViewById(R.id.followupTitle);
        TextView followupDate = findViewById(R.id.followupDate);
        View consultationCard = findViewById(R.id.consultationCard);
        View followupCard = findViewById(R.id.followupCard);

        // Always hide follow-up card first
        if (followupCard != null)
            followupCard.setVisibility(View.GONE);

        if (appointments == null || appointments.isEmpty()) {
            if (consultationCard != null)
                consultationCard.setVisibility(View.GONE);
            return;
        }

        List<Appointment> upcoming = new ArrayList<>(appointments);

        // Show only the first appointment
        if (!upcoming.isEmpty()) {
            Appointment first = upcoming.get(0);

            if (consultationTitle != null)
                consultationTitle.setText(first.getTitle() != null ? first.getTitle() : "Appointment");

            if (consultationDate != null)
                consultationDate.setText(first.getFormattedDate() + "  " + first.getFormattedTimeSlot());

            if (consultationCard != null)
                consultationCard.setVisibility(View.VISIBLE);
        } else {
            if (consultationCard != null)
                consultationCard.setVisibility(View.GONE);
        }

        // Removed: Second appointment display logic
        // Only show one appointment at a time
    }

    private void initializeViews() {
        yesBtn = findViewById(R.id.yesBtn);
        noBtn = findViewById(R.id.noBtn);
        menuIcon = findViewById(R.id.menu_icon);
        logoutBtn = findViewById(R.id.logoutBtn);
        drawerLayout = findViewById(R.id.drawer_layout);
        navDrawerContainer = findViewById(R.id.navDrawerContainer);

        setupNavigationDrawer();
    }

    private void initializeChat() {
        chatBot = new ChatBot(this);
        chatMessages = new ArrayList<>();
        chatMessages.add(new ChatMessage(
                "Hello! I'm your AI-powered RA health assistant. I use advanced medical knowledge to help you manage your rheumatoid arthritis. How can I help you today?",
                false));
        chatAdapter = new ChatAdapter(this, chatMessages);
    }

    private void setupClickListeners() {

        if (yesBtn != null) {
            yesBtn.setOnClickListener(v -> {
                viewModel.setTaskCompleted(true);
                Toast.makeText(this, "Great! Task completed ✔", Toast.LENGTH_SHORT).show();
            });
        }

        if (noBtn != null) {
            noBtn.setOnClickListener(
                    v -> Toast.makeText(this, "Please complete your task ❌", Toast.LENGTH_SHORT).show());
        }

        View chatCard = findViewById(R.id.chatCard);
        if (chatCard != null)
            chatCard.setOnClickListener(v -> showChatDialog());

        // Removed: health stats click listener (UI deleted)

        View consultationBtn = findViewById(R.id.consultationDetailsBtn);
        if (consultationBtn != null)
            consultationBtn.setOnClickListener(v -> startActivity(new Intent(this, PatientAppointmentsActivity.class)));

        View followupBtn = findViewById(R.id.followupDetailsBtn);
        if (followupBtn != null)
            followupBtn.setOnClickListener(v -> startActivity(new Intent(this, PatientAppointmentsActivity.class)));

        if (menuIcon != null) {
            menuIcon.setOnClickListener(v -> {
                if (drawerLayout != null) {
                    if (drawerLayout.isDrawerOpen(GravityCompat.START))
                        drawerLayout.closeDrawer(GravityCompat.START);
                    else
                        drawerLayout.openDrawer(GravityCompat.START);
                }
            });
        }

        if (logoutBtn != null)
            logoutBtn.setOnClickListener(v -> logout());

        setupQuickActions();
    }

    private void setupQuickActions() {

        View symptomBtn = findViewById(R.id.symptomLogBtn);
        if (symptomBtn != null)
            symptomBtn.setOnClickListener(v -> startActivity(new Intent(this, SymptomLogActivity.class)));

        View medsBtn = findViewById(R.id.medicationsBtn);
        if (medsBtn != null)
            medsBtn.setOnClickListener(v -> startActivity(new Intent(this, PatientMedicationsActivity.class)));

        View rehabBtn = findViewById(R.id.rehabBtn);
        if (rehabBtn != null)
            rehabBtn.setOnClickListener(v -> startActivity(new Intent(this, PatientRehabilitationActivity.class)));
    }

    private void showChatDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_chat, null);

        // Initialize views with safe casting
        chatRecyclerView = dialogView.findViewById(R.id.chatRecyclerView);
        EditText messageInput = dialogView.findViewById(R.id.messageInput);
        Button sendBtn = dialogView.findViewById(R.id.sendBtn);
        Button closeBtn = dialogView.findViewById(R.id.closeBtn);
        View typingIndicator = dialogView.findViewById(R.id.typingIndicator);

        // Setup RecyclerView
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        AlertDialog dialog = builder.setView(dialogView)
                .setCancelable(true)
                .create();

        // Make dialog full width
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        // Send message functionality
        Runnable sendMessage = () -> {
            String message = messageInput.getText().toString().trim();
            if (!TextUtils.isEmpty(message)) {
                sendChatMessage(message, messageInput, typingIndicator);
            }
        };

        sendBtn.setOnClickListener(v -> sendMessage.run());

        // Handle enter key in input (simplified)
        messageInput.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_SEND);
        messageInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEND) {
                sendMessage.run();
                return true;
            }
            return false;
        });

        closeBtn.setOnClickListener(v -> dialog.dismiss());

        dialog.show();

        // Scroll to bottom
        if (!chatMessages.isEmpty()) {
            chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
        }
    }

    private void sendChatMessage(String message,
            EditText messageInput,
            View typingIndicator) {

        // Add user message
        addChatMessage(message, true);
        messageInput.setText("");

        // Show typing indicator
        typingIndicator.setVisibility(View.VISIBLE);

        // Scroll to bottom
        chatRecyclerView.scrollToPosition(chatMessages.size() - 1);

        // Get AI response asynchronously
        chatBot.getResponseAsync(message, response -> {
            runOnUiThread(() -> {
                // Hide typing indicator
                typingIndicator.setVisibility(View.GONE);

                // Add bot response
                addChatMessage(response, false);
                chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
            });
        });
    }

    private void addChatMessage(String message, boolean fromUser) {
        chatMessages.add(new ChatMessage(message, fromUser));
        if (chatAdapter != null)
            chatAdapter.notifyItemInserted(chatMessages.size() - 1);
    }

    private void logout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    sessionManager.logout();
                    NavigationManager.goToLogin(this);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void setupNavigationDrawer() {
        // Find views in the new custom drawer layout
        TextView navHeaderName = findViewById(R.id.navHeaderPatientName);
        TextView navHeaderEmail = findViewById(R.id.navHeaderPatientEmail);

        String userName = sessionManager.getUserName();
        String userEmail = sessionManager.getUserEmail();

        if (navHeaderName != null && userName != null)
            navHeaderName.setText(userName);

        if (navHeaderEmail != null && userEmail != null)
            navHeaderEmail.setText(userEmail);

        // Map menu items to click listeners
        View.OnClickListener menuClickListener = v -> {
            int id = v.getId();

            if (id == R.id.menu_dashboard) {
                // Already on dashboard
            } else if (id == R.id.menu_medications) {
                startActivity(new Intent(this, PatientMedicationsActivity.class));
            } else if (id == R.id.menu_rehab) {
                startActivity(new Intent(this, PatientRehabilitationActivity.class));
            } else if (id == R.id.menu_education) {
                startActivity(new Intent(this, EducationHubActivity.class));
            } else if (id == R.id.menu_appointments) {
                startActivity(new Intent(this, PatientAppointmentsActivity.class));
            } else if (id == R.id.menu_symptoms) {
                startActivity(new Intent(this, SymptomLogActivity.class));
            } else if (id == R.id.menu_reports) {
                startActivity(new Intent(this, ReportList.class));
            } else if (id == R.id.menu_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
            } else if (id == R.id.menu_dark_theme) {
                ThemeManager.toggleTheme(this);
                recreate();
            } else if (id == R.id.menu_logout || id == R.id.nav_logout_icon) {
                logout();
            }

            if (drawerLayout != null) {
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        };

        // Assign listeners
        int[] itemIds = {
                R.id.menu_dashboard, R.id.menu_medications, R.id.menu_rehab,
                R.id.menu_education, R.id.menu_appointments, R.id.menu_symptoms,
                R.id.menu_reports, R.id.menu_settings, R.id.menu_dark_theme, R.id.menu_logout
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

        highlightNavMenuItem(R.id.menu_dashboard);
    }

    private void highlightNavMenuItem(int selectedId) {
        int[] itemIds = {
                R.id.menu_dashboard, R.id.menu_medications, R.id.menu_rehab,
                R.id.menu_education, R.id.menu_appointments, R.id.menu_symptoms,
                R.id.menu_reports, R.id.menu_settings, R.id.menu_dark_theme, R.id.menu_logout
        };

        for (int id : itemIds) {
            View itemView = findViewById(id);
            if (itemView != null) {
                if (id == selectedId) {
                    itemView.setBackgroundResource(R.drawable.nav_item_selected_bg);
                    // Add 8dp internal padding for the selected state inset
                    int padding = (int) (8 * getResources().getDisplayMetrics().density);
                    itemView.setPadding(padding, padding, padding, padding);
                } else {
                    itemView.setBackgroundResource(0); // Transparent/Default
                    itemView.setPadding(0, 0, 0, 0); // Rely on Style margin for spacing
                }
            }
        }
    }
}
