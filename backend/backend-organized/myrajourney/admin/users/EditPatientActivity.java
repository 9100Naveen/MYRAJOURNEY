package com.example.myrajourney.admin.users;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.example.myrajourney.R;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class EditPatientActivity extends AppCompatActivity {

    private EditText etPatientId, etName, etMobile, etAge, etEmail, etAddress;
    private ImageView imgProfilePic;
    private Button btnUploadPic, btnUpdatePatient;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_patient);

        // Initialize views
        etPatientId = findViewById(R.id.etPatientId);
        etName = findViewById(R.id.etName);
        etMobile = findViewById(R.id.etMobile);
        etAge = findViewById(R.id.etAge);
        etEmail = findViewById(R.id.etEmail);
        etAddress = findViewById(R.id.etAddress);
        imgProfilePic = findViewById(R.id.imgProfilePic);
        btnUploadPic = findViewById(R.id.btnUploadPic);
        btnUpdatePatient = findViewById(R.id.btnUpdatePatient);

        // Image picker
        ActivityResultLauncher<String> pickImageLauncher =
                registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                    if (uri != null) {
                        imageUri = uri;
                        imgProfilePic.setImageURI(uri);
                    }
                });

        btnUploadPic.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        // Update Patient Button
        btnUpdatePatient.setOnClickListener(v -> {
            String patientId = etPatientId.getText().toString().trim();

            if (TextUtils.isEmpty(patientId)) {
                Toast.makeText(EditPatientActivity.this, "Enter Patient ID first", Toast.LENGTH_SHORT).show();
                return;
            }

            // Collect only non-empty fields
            String name = etName.getText().toString().trim();
            String mobile = etMobile.getText().toString().trim();
            String age = etAge.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String address = etAddress.getText().toString().trim();

            StringBuilder updatedFields = new StringBuilder("Updated: ");

            if (!TextUtils.isEmpty(name)) updatedFields.append("Name, ");
            if (!TextUtils.isEmpty(mobile)) updatedFields.append("Mobile, ");
            if (!TextUtils.isEmpty(age)) updatedFields.append("Age, ");
            if (!TextUtils.isEmpty(email)) updatedFields.append("Email, ");
            if (!TextUtils.isEmpty(address)) updatedFields.append("Address, ");
            if (imageUri != null) updatedFields.append("Profile Picture, ");

            if (updatedFields.toString().equals("Updated: ")) {
                Toast.makeText(EditPatientActivity.this, "No fields updated", Toast.LENGTH_SHORT).show();
                return;
            }

            // Trim last comma
            String msg = updatedFields.substring(0, updatedFields.length() - 2);

            // In real app: Update DB with only provided fields
            Toast.makeText(EditPatientActivity.this, msg + " for Patient ID: " + patientId, Toast.LENGTH_LONG).show();
        });
    }
}






