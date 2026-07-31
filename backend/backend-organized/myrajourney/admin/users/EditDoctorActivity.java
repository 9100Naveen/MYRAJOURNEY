package com.example.myrajourney.admin.users;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

// --- ADDED IMPORTS ---
import com.example.myrajourney.R;
import com.example.myrajourney.core.ui.ThemeManager;
// ---------------------

public class EditDoctorActivity extends AppCompatActivity {

    private EditText etDoctorId, etName, etMobile, etAge, etEmail, etAddress, etSpecialization;
    private ImageView imgProfilePic;
    private Button btnUploadPic, btnUpdateDoctor;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before setting content view
        ThemeManager.applyTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_doctor);

        // Initialize views
        etDoctorId = findViewById(R.id.etDoctorId);
        etName = findViewById(R.id.etName);
        etMobile = findViewById(R.id.etMobile);
        etAge = findViewById(R.id.etAge);
        etEmail = findViewById(R.id.etEmail);
        etAddress = findViewById(R.id.etAddress);
        etSpecialization = findViewById(R.id.etSpecialization);
        imgProfilePic = findViewById(R.id.imgProfilePic);
        btnUploadPic = findViewById(R.id.btnUploadPic);
        btnUpdateDoctor = findViewById(R.id.btnUpdateDoctor);

        // Image picker
        ActivityResultLauncher<String> pickImageLauncher =
                registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                    if (uri != null) {
                        imageUri = uri;
                        imgProfilePic.setImageURI(uri);
                    }
                });

        btnUploadPic.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        // Update Doctor Button
        btnUpdateDoctor.setOnClickListener(v -> {
            String doctorId = etDoctorId.getText().toString().trim();

            if (TextUtils.isEmpty(doctorId)) {
                Toast.makeText(EditDoctorActivity.this, "Enter Doctor ID first", Toast.LENGTH_SHORT).show();
                return;
            }

            // Collect only non-empty fields
            String name = etName.getText().toString().trim();
            String mobile = etMobile.getText().toString().trim();
            String age = etAge.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String address = etAddress.getText().toString().trim();
            String specialization = etSpecialization.getText().toString().trim();

            StringBuilder updatedFields = new StringBuilder("Updated: ");

            if (!TextUtils.isEmpty(name)) updatedFields.append("Name, ");
            if (!TextUtils.isEmpty(mobile)) updatedFields.append("Mobile, ");
            if (!TextUtils.isEmpty(age)) updatedFields.append("Age, ");
            if (!TextUtils.isEmpty(email)) updatedFields.append("Email, ");
            if (!TextUtils.isEmpty(address)) updatedFields.append("Address, ");
            if (!TextUtils.isEmpty(specialization)) updatedFields.append("Specialization, ");
            if (imageUri != null) updatedFields.append("Profile Picture, ");

            if (updatedFields.toString().equals("Updated: ")) {
                Toast.makeText(EditDoctorActivity.this, "No fields updated", Toast.LENGTH_SHORT).show();
                return;
            }

            // Trim last comma
            String msg = updatedFields.substring(0, updatedFields.length() - 2);

            // In real app: Update DB with only provided fields
            Toast.makeText(EditDoctorActivity.this, msg + " for Doctor ID: " + doctorId, Toast.LENGTH_LONG).show();
        });
    }
}