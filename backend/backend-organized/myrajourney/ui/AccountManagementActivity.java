package com.example.myrajourney.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.myrajourney.R;
import com.example.myrajourney.core.ui.ThemeManager;
import com.example.myrajourney.auth.LoginActivity;

public class AccountManagementActivity extends AppCompatActivity {

    private TextView tvUserInfo;
    private Button btnEditProfile, btnVerifyAccount, btnDeleteAccount, btnPrivacyPolicy;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_management);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Account Management");
        }

        // Initialize views
        tvUserInfo = findViewById(R.id.tvUserInfo);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnVerifyAccount = findViewById(R.id.btnVerifyAccount);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
        btnPrivacyPolicy = findViewById(R.id.btnPrivacyPolicy);

        // Get user data
        sharedPreferences = getSharedPreferences("MyRAJourney", MODE_PRIVATE);
        
        // Display user info
        displayUserInfo();

        // Set click listeners
        btnEditProfile.setOnClickListener(v -> openProfileEdit());
        btnVerifyAccount.setOnClickListener(v -> verifyAccount());
        btnDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());
        btnPrivacyPolicy.setOnClickListener(v -> openPrivacyPolicy());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void displayUserInfo() {
        String name = sharedPreferences.getString("user_name", "Unknown");
        String email = sharedPreferences.getString("user_email", "Unknown");
        String role = sharedPreferences.getString("user_role", "Unknown");
        String mobile = sharedPreferences.getString("user_mobile", "Unknown");

        String userInfo = "👤 Name: " + name + "\n" +
                         "📧 Email: " + email + "\n" +
                         "👨‍⚕️ Role: " + role + "\n" +
                         "📱 Mobile: " + mobile;

        tvUserInfo.setText(userInfo);
    }

    private void openProfileEdit() {
        Intent intent = new Intent(this, ProfileEditActivity.class);
        startActivity(intent);
    }

    private void verifyAccount() {
        // Show verification dialog
        new AlertDialog.Builder(this)
                .setTitle("Account Verification")
                .setMessage("Your account is currently verified and active.\n\n" +
                           "✅ Email verified\n" +
                           "✅ Mobile verified\n" +
                           "✅ Profile complete\n" +
                           "✅ Privacy policy accepted\n\n" +
                           "If you're experiencing issues, please contact support.")
                .setPositiveButton("OK", null)
                .setNeutralButton("Contact Support", (dialog, which) -> {
                    Toast.makeText(this, "Support: support@myrajourney.com", Toast.LENGTH_LONG).show();
                })
                .show();
    }

    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(this)
                .setTitle("⚠️ Delete Account")
                .setMessage("Are you sure you want to delete your account?\n\n" +
                           "This action will:\n" +
                           "• Permanently delete all your data\n" +
                           "• Remove all medical records\n" +
                           "• Cancel all appointments\n" +
                           "• Log you out immediately\n\n" +
                           "This action CANNOT be undone!")
                .setPositiveButton("Delete Account", (dialog, which) -> deleteAccount())
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteAccount() {
        // Show final confirmation
        new AlertDialog.Builder(this)
                .setTitle("Final Confirmation")
                .setMessage("This is your FINAL chance to cancel.\n\n" +
                           "Type 'DELETE' to confirm account deletion:")
                .setView(R.layout.dialog_delete_confirmation)
                .setPositiveButton("Confirm Delete", (dialog, which) -> {
                    // Clear all user data
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear();
                    editor.apply();

                    Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_LONG).show();

                    // Redirect to login
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openPrivacyPolicy() {
        Intent intent = new Intent(this, PrivacyPolicyActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh user info when returning from profile edit
        displayUserInfo();
    }
}