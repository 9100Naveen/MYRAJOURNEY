package com.example.myrajourney.profile;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.myrajourney.R;
import com.example.myrajourney.utils.ProfilePictureManager;
import com.example.myrajourney.core.session.SessionManager;

/**
 * Activity for selecting and managing profile pictures
 * Supports camera capture and gallery selection with proper permissions
 */
public class ProfilePictureActivity extends AppCompatActivity {
    
    private static final String TAG = "ProfilePictureActivity";
    
    private ImageView profileImageView;
    private Button btnCamera;
    private Button btnGallery;
    private Button btnSave;
    private Button btnCancel;
    
    private ProfilePictureManager pictureManager;
    private SessionManager sessionManager;
    
    private String currentImageFileName;
    private String currentImageBase64;
    
    // Activity result launchers
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private ActivityResultLauncher<String> storagePermissionLauncher;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_picture);
        
        initializeViews();
        initializeManagers();
        initializeActivityLaunchers();
        setupClickListeners();
        loadCurrentProfilePicture();
    }
    
    private void initializeViews() {
        profileImageView = findViewById(R.id.profile_image_view);
        btnCamera = findViewById(R.id.btn_camera);
        btnGallery = findViewById(R.id.btn_gallery);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);
    }
    
    private void initializeManagers() {
        pictureManager = new ProfilePictureManager(this);
        sessionManager = SessionManager.getInstance(this);
    }
    
    private void initializeActivityLaunchers() {
        // Camera launcher
        cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    String photoPath = pictureManager.getCurrentPhotoPath();
                    if (photoPath != null) {
                        Uri photoUri = Uri.parse("file://" + photoPath);
                        processSelectedImage(photoUri);
                    }
                }
            }
        );
        
        // Gallery launcher
        galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        processSelectedImage(selectedImageUri);
                    }
                }
            }
        );
        
        // Camera permission launcher
        cameraPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openCamera();
                } else {
                    showPermissionDeniedDialog("Camera");
                }
            }
        );
        
        // Storage permission launcher
        storagePermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openGallery();
                } else {
                    showPermissionDeniedDialog("Storage");
                }
            }
        );
    }
    
    private void setupClickListeners() {
        btnCamera.setOnClickListener(v -> checkCameraPermissionAndOpen());
        btnGallery.setOnClickListener(v -> checkStoragePermissionAndOpen());
        btnSave.setOnClickListener(v -> saveProfilePicture());
        btnCancel.setOnClickListener(v -> finish());
    }
    
    private void loadCurrentProfilePicture() {
        String currentFileName = sessionManager.getProfilePicture();
        if (currentFileName != null && !currentFileName.isEmpty()) {
            Bitmap bitmap = pictureManager.loadProfilePicture(currentFileName);
            if (bitmap != null) {
                profileImageView.setImageBitmap(bitmap);
                currentImageFileName = currentFileName;
            }
        }
    }
    
    private void checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }
    
    private void checkStoragePermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
                == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            storagePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }
    
    private void openCamera() {
        try {
            Intent cameraIntent = pictureManager.createCameraIntent();
            cameraLauncher.launch(cameraIntent);
        } catch (Exception e) {
            Toast.makeText(this, "Error opening camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void openGallery() {
        Intent galleryIntent = pictureManager.createGalleryIntent();
        galleryLauncher.launch(galleryIntent);
    }
    
    private void processSelectedImage(Uri imageUri) {
        // Show loading
        btnSave.setEnabled(false);
        btnSave.setText("Processing...");
        
        // Validate image first
        ProfilePictureManager.ValidationResult validation = pictureManager.validateImage(imageUri);
        if (!validation.isValid) {
            Toast.makeText(this, validation.message, Toast.LENGTH_LONG).show();
            resetSaveButton();
            return;
        }
        
        // Process image in background thread
        new Thread(() -> {
            ProfilePictureManager.ProcessedImage result = pictureManager.processImage(imageUri);
            
            runOnUiThread(() -> {
                if (result.success) {
                    // Load and display the processed image
                    Bitmap bitmap = pictureManager.loadProfilePicture(result.fileName);
                    if (bitmap != null) {
                        profileImageView.setImageBitmap(bitmap);
                        currentImageFileName = result.fileName;
                        currentImageBase64 = result.base64String;
                        
                        Toast.makeText(this, "Image processed successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error loading processed image", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, result.message, Toast.LENGTH_LONG).show();
                }
                
                resetSaveButton();
            });
        }).start();
    }
    
    private void saveProfilePicture() {
        if (currentImageFileName == null) {
            Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Save to session manager
        sessionManager.setProfilePicture(currentImageFileName);
        
        // TODO: Upload to server if needed
        if (currentImageBase64 != null) {
            uploadProfilePictureToServer(currentImageBase64);
        }
        
        Toast.makeText(this, "Profile picture saved successfully", Toast.LENGTH_SHORT).show();
        
        // Return result
        Intent resultIntent = new Intent();
        resultIntent.putExtra("profile_picture", currentImageFileName);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
    
    private void uploadProfilePictureToServer(String base64Image) {
        // TODO: Implement server upload
        // This would typically involve making an API call to upload the base64 image
        android.util.Log.d(TAG, "Profile picture ready for server upload");
    }
    
    private void resetSaveButton() {
        btnSave.setEnabled(true);
        btnSave.setText("Save");
    }
    
    private void showPermissionDeniedDialog(String permissionType) {
        new AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage(permissionType + " permission is required to select profile pictures. " +
                       "Please grant permission in app settings.")
            .setPositiveButton("Settings", (dialog, which) -> {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up any temporary files if needed
    }
}