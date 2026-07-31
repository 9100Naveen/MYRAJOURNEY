package com.example.myrajourney.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.myrajourney.R;
import com.example.myrajourney.core.network.ApiClient;
import com.example.myrajourney.core.network.ApiService;
import com.example.myrajourney.core.ui.ThemeManager;
import com.example.myrajourney.utils.ValidationUtils;
import com.example.myrajourney.data.model.ApiResponse;
import com.example.myrajourney.data.model.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileEditActivity extends AppCompatActivity {

    private EditText etName, etMobile, etAge, etEmail, etAddress;
    private Button btnSaveProfile, btnCancel;
    private SharedPreferences sharedPreferences;
    private String userRole;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Edit Profile");
        }

        // Initialize views
        etName = findViewById(R.id.etName);
        etMobile = findViewById(R.id.etMobile);
        etAge = findViewById(R.id.etAge);
        etEmail = findViewById(R.id.etEmail);
        etAddress = findViewById(R.id.etAddress);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnCancel = findViewById(R.id.btnCancel);

        // Get user data from SharedPreferences
        sharedPreferences = getSharedPreferences("MyRAJourney", MODE_PRIVATE);
        userRole = sharedPreferences.getString("user_role", "");
        userId = sharedPreferences.getInt("user_id", 0);

        // Load current profile data
        loadProfileData();

        // Set click listeners
        btnSaveProfile.setOnClickListener(v -> saveProfile());
        btnCancel.setOnClickListener(v -> finish());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void loadProfileData() {
        // Load data from SharedPreferences
        etName.setText(sharedPreferences.getString("user_name", ""));
        etMobile.setText(sharedPreferences.getString("user_mobile", ""));
        etAge.setText(sharedPreferences.getString("user_age", ""));
        etEmail.setText(sharedPreferences.getString("user_email", ""));
        etAddress.setText(sharedPreferences.getString("user_address", ""));
    }

    private void saveProfile() {
        String name = etName.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String age = etAge.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        // Validate all fields using ValidationUtils
        String nameError = ValidationUtils.validateNameWithMessage(name);
        if (nameError != null) {
            etName.setError(nameError);
            etName.requestFocus();
            return;
        }

        String mobileError = ValidationUtils.validateMobileWithMessage(mobile);
        if (mobileError != null) {
            etMobile.setError(mobileError);
            etMobile.requestFocus();
            return;
        }

        String emailError = ValidationUtils.validateEmailWithMessage(email);
        if (emailError != null) {
            etEmail.setError(emailError);
            etEmail.requestFocus();
            return;
        }

        String addressError = ValidationUtils.validateAddressWithMessage(address);
        if (addressError != null) {
            etAddress.setError(addressError);
            etAddress.requestFocus();
            return;
        }

        // Validate age (must be at least 18)
        String ageError = ValidationUtils.validateAgeWithMessage(age);
        if (ageError != null) {
            etAge.setError(ageError);
            etAge.requestFocus();
            return;
        }

        btnSaveProfile.setEnabled(false);
        btnSaveProfile.setText("Saving...");

        // Update SharedPreferences immediately for better UX
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user_name", name);
        editor.putString("user_mobile", mobile);
        editor.putString("user_age", age);
        editor.putString("user_email", email);
        editor.putString("user_address", address);
        editor.apply();

        // TODO: Implement API call to update profile on server
        // For now, just show success message
        btnSaveProfile.setEnabled(true);
        btnSaveProfile.setText("Save Profile");
        
        Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_LONG).show();
        finish();
    }
}