package com.example.myrajourney.admin.users;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

// --- ADDED IMPORTS ---
import com.example.myrajourney.R;
import com.example.myrajourney.core.network.ApiClient;
import com.example.myrajourney.core.network.ApiService;
import com.example.myrajourney.utils.ValidationUtils;
import com.example.myrajourney.core.ui.ThemeManager;
import com.example.myrajourney.data.model.ApiResponse;
import com.example.myrajourney.data.model.CreateUserRequest;
import com.example.myrajourney.data.model.User;
// ---------------------

import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateDoctorActivity extends AppCompatActivity {

    private EditText etName, etMobile, etAge, etEmail, etAddress, etSpecialization;
    private ImageView imgProfilePic;
    private Button btnUploadPic, btnRegisterDoctor;
    private TextView tvCredentials;

    private Uri imageUri;
    private String defaultPassword = "Patrol@987";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme
        ThemeManager.applyTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_doctor);

        // Initialize views
        etName = findViewById(R.id.etName);
        etMobile = findViewById(R.id.etMobile);
        etAge = findViewById(R.id.etAge);
        etEmail = findViewById(R.id.etEmail);
        etAddress = findViewById(R.id.etAddress);
        etSpecialization = findViewById(R.id.etSpecialization);
        imgProfilePic = findViewById(R.id.imgProfilePic);
        btnUploadPic = findViewById(R.id.btnUploadPic);
        btnRegisterDoctor = findViewById(R.id.btnRegisterDoctor);
        tvCredentials = findViewById(R.id.tvCredentials);

        // Image picker
        ActivityResultLauncher<String> pickImageLauncher =
                registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                    if (uri != null) {
                        imageUri = uri;
                        imgProfilePic.setImageURI(uri);
                    }
                });

        btnUploadPic.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        // Register button click
        btnRegisterDoctor.setOnClickListener(v -> createDoctor());
    }

    private void createDoctor() {
        String name = etName.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String age = etAge.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String specialization = etSpecialization != null ? etSpecialization.getText().toString().trim() : "";

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

        // Validate specialization
        if (specialization.isEmpty()) {
            if (etSpecialization != null) {
                etSpecialization.setError("Specialization is required");
                etSpecialization.requestFocus();
                return;
            }
        }

        btnRegisterDoctor.setEnabled(false);
        btnRegisterDoctor.setText("Creating...");

        ApiService apiService = ApiClient.getApiService(this);

        // Use setters instead of constructor to avoid argument mismatch errors
        CreateUserRequest request = new CreateUserRequest();
        request.setName(name);
        request.setEmail(email);
        request.setMobile(mobile);
        request.setRole("DOCTOR");
        request.setPassword(defaultPassword);
        request.setAddress(address);
        request.setSpecialization(specialization);

        Call<ApiResponse<User>> call = apiService.createUser(request);

        call.enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                btnRegisterDoctor.setEnabled(true);
                btnRegisterDoctor.setText("Register Doctor");

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    User user = response.body().getData();
                    if (user != null) {
                        Toast.makeText(CreateDoctorActivity.this, "Doctor Registered Successfully!", Toast.LENGTH_LONG).show();

                        // Use String.valueOf(user.getId()) to fix symbol error
                        tvCredentials.setText("Doctor ID: " + user.getId() + "\nEmail: " + user.getEmail() + "\nDefault Password: " + defaultPassword);
                        tvCredentials.setVisibility(TextView.VISIBLE);

                        View card = findViewById(R.id.credentialsCard);
                        if (card != null) card.setVisibility(View.VISIBLE);

                        // Clear form
                        etName.setText("");
                        etMobile.setText("");
                        etAge.setText("");
                        etEmail.setText("");
                        etAddress.setText("");
                        if (etSpecialization != null) etSpecialization.setText("");
                    }
                } else {
                    String errorMsg = "Registration failed";
                    if (response.body() != null && response.body().getError() != null) {
                        errorMsg = response.body().getError().getMessage();
                    }
                    Toast.makeText(CreateDoctorActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                btnRegisterDoctor.setEnabled(true);
                btnRegisterDoctor.setText("Register Doctor");
                Toast.makeText(CreateDoctorActivity.this, "Registration failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}