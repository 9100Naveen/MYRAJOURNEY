package com.example.myrajourney.admin.users;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myrajourney.R;
import com.example.myrajourney.auth.LoginActivity;
import com.example.myrajourney.core.network.ApiClient;
import com.example.myrajourney.core.network.ApiService;
import com.example.myrajourney.data.model.ApiResponse;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText etPassword, etConfirmPassword;
    private Button btnReset;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        // Email coming from ForgotPasswordActivity
        email = getIntent().getStringExtra("email");

        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "Invalid reset request", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {

        // Hide email + token views completely (you don’t need them anymore)
        findViewById(R.id.etEmail).setVisibility(android.view.View.GONE);
        findViewById(R.id.tvEmailLabel).setVisibility(android.view.View.GONE);
        findViewById(R.id.etToken).setVisibility(android.view.View.GONE);
        findViewById(R.id.tvTokenLabel).setVisibility(android.view.View.GONE);

        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnReset = findViewById(R.id.btnReset);

        TextView tvInfo = findViewById(R.id.tvInfo);
        tvInfo.setText("Enter your new password (minimum 8 characters).");
    }

    private void setupListeners() {

        btnReset.setOnClickListener(v -> validateAndReset());

        findViewById(R.id.tvBackToLogin).setOnClickListener(v -> {
            Intent i = new Intent(ResetPasswordActivity.this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });
    }

    private void validateAndReset() {
        String pass = etPassword.getText().toString().trim();
        String confirm = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(pass)) {
            Toast.makeText(this, "Enter password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (pass.length() < 8) {
            Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!pass.equals(confirm)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        resetPassword(pass);
    }

    private void resetPassword(String password) {

        ApiService api = ApiClient.getApiService(this);

        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);

        api.resetPassword(body).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call,
                                   Response<ApiResponse<Void>> response) {

                if (response.isSuccessful()
                        && response.body() != null
                        && response.body().isSuccess()) {

                    Toast.makeText(ResetPasswordActivity.this,
                            "Password reset successful!",
                            Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    return;
                }

                String msg = "Reset failed";
                if (response.body() != null && response.body().getError() != null)
                    msg = response.body().getError().getMessage();

                Toast.makeText(ResetPasswordActivity.this, msg, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Toast.makeText(ResetPasswordActivity.this,
                        "Network error, try again",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
