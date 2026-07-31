package com.example.myrajourney.admin.users;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myrajourney.R;
import com.example.myrajourney.core.network.ApiClient;
import com.example.myrajourney.core.network.ApiService;
import com.example.myrajourney.core.ui.ThemeManager;
import com.example.myrajourney.utils.ValidationUtils;
import com.example.myrajourney.data.model.ApiResponse;
import com.example.myrajourney.data.model.CreateUserRequest;
import com.example.myrajourney.data.model.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreatePatientActivity extends AppCompatActivity {

    private EditText etName, etMobile, etAge, etEmail, etAddress;
    private TextView tvPatientId, tvUsername, tvPassword;
    private ImageView imgProfilePic;
    private Button btnUploadPic, btnRegisterPatient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_patient);

        // Initialize Views
        imgProfilePic = findViewById(R.id.imgProfilePic);
        btnUploadPic = findViewById(R.id.btnUploadPic);

        etName = findViewById(R.id.etName);
        etMobile = findViewById(R.id.etMobile);
        etAge = findViewById(R.id.etAge);
        etEmail = findViewById(R.id.etEmail);
        etAddress = findViewById(R.id.etAddress);

        tvPatientId = findViewById(R.id.tvPatientId);
        tvUsername = findViewById(R.id.tvUsername);
        tvPassword = findViewById(R.id.tvPassword);

        btnRegisterPatient = findViewById(R.id.btnRegisterPatient);

        btnRegisterPatient.setOnClickListener(v -> createPatient());
    }

    private void createPatient() {
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

        btnRegisterPatient.setEnabled(false);
        btnRegisterPatient.setText("Creating...");

        ApiService apiService = ApiClient.getApiService(this);

        CreateUserRequest request = new CreateUserRequest();
        request.setName(name);
        request.setEmail(email);
        request.setMobile(mobile);
        request.setRole("PATIENT");
        request.setPassword("Welcome@456"); // Updated to match new standard
        request.setAddress(address);
        request.setAge(age);

        Call<ApiResponse<User>> call = apiService.createUser(request);

        call.enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                btnRegisterPatient.setEnabled(true);
                btnRegisterPatient.setText("Register Patient");

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    User user = response.body().getData();
                    if (user != null) {
                        tvPatientId.setText("Patient ID: " + user.getId());
                        tvUsername.setText("Username: " + user.getEmail());
                        tvPassword.setText("Default Password: welcome123");

                        Toast.makeText(CreatePatientActivity.this, "Patient Registered Successfully!", Toast.LENGTH_LONG).show();

                        etName.setText("");
                        etMobile.setText("");
                        etAge.setText("");
                        etEmail.setText("");
                        etAddress.setText("");
                        etEmail.setError(null);
                    }
                } else {
                    String errorMsg = "Registration failed";
                    if (response.body() != null && response.body().getError() != null) {
                        errorMsg = response.body().getError().getMessage();
                    }
                    Toast.makeText(CreatePatientActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                btnRegisterPatient.setEnabled(true);
                btnRegisterPatient.setText("Register Patient");
                Toast.makeText(CreatePatientActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}