package com.example.myrajourney.admin.dashboard;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

// --- ADDED IMPORTS ---
import com.example.myrajourney.R;
import com.example.myrajourney.core.ui.ThemeManager;
// ---------------------

public class SettingsActivity extends AppCompatActivity {

    private EditText etEmail, etPhone, etAddress, etPassword;
    private Button btnSave;
    private SwitchCompat themeSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before setting content view
        ThemeManager.applyTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize views
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        etPassword = findViewById(R.id.etPassword);
        btnSave = findViewById(R.id.btnSave);
        themeSwitch = findViewById(R.id.themeSwitch);

        // Set theme switch state
        themeSwitch.setChecked(ThemeManager.isDarkMode(this));

        // Theme switch listener
        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ThemeManager.toggleTheme(this);
            // Recreate activity to apply theme
            recreate();
        });

        // Save button click
        btnSave.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String address = etAddress.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // You can save these values to database or shared preferences
            Toast.makeText(this, "Settings Saved Successfully!", Toast.LENGTH_SHORT).show();
        });
    }
}