package com.example.myrajourney.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import androidx.core.content.FileProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Comprehensive profile picture management system
 * Handles image capture, selection, compression, and storage
 */
public class ProfilePictureManager {
    
    private static final String TAG = "ProfilePictureManager";
    private static final int MAX_IMAGE_SIZE = 1024; // Max width/height in pixels
    private static final int JPEG_QUALITY = 85; // JPEG compression quality
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB max file size
    
    private Context context;
    private String currentPhotoPath;
    
    public ProfilePictureManager(Context context) {
        this.context = context;
    }
    
    /**
     * Create intent for taking photo with camera
     */
    public Intent createCameraIntent() throws IOException {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        
        // Create the File where the photo should go
        File photoFile = createImageFile();
        
        // Continue only if the File was successfully created
        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(context,
                    context.getPackageName() + ".fileprovider",
                    photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        }
        
        return takePictureIntent;
    }
    
    /**
     * Create intent for selecting image from gallery
     */
    public Intent createGalleryIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/jpeg", "image/png", "image/jpg"});
        return intent;
    }
    
    /**
     * Process image from camera or gallery
     */
    public ProcessedImage processImage(Uri imageUri) {
        try {
            // Load and compress the image
            Bitmap bitmap = loadAndCompressBitmap(imageUri);
            
            if (bitmap == null) {
                return new ProcessedImage(false, "Failed to load image", null, null);
            }
            
            // Save compressed image to internal storage
            String fileName = saveImageToInternalStorage(bitmap);
            
            if (fileName == null) {
                return new ProcessedImage(false, "Failed to save image", null, null);
            }
            
            // Convert to base64 for API upload
            String base64String = bitmapToBase64(bitmap);
            
            return new ProcessedImage(true, "Image processed successfully", fileName, base64String);
            
        } catch (Exception e) {
            Log.e(TAG, "Error processing image", e);
            return new ProcessedImage(false, "Error processing image: " + e.getMessage(), null, null);
        }
    }
    
    /**
     * Load and compress bitmap from URI
     */
    private Bitmap loadAndCompressBitmap(Uri imageUri) throws IOException {
        // First, get image dimensions
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        
        InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
        BitmapFactory.decodeStream(inputStream, null, options);
        inputStream.close();
        
        // Calculate sample size for compression
        int sampleSize = calculateSampleSize(options.outWidth, options.outHeight);
        
        // Load the actual bitmap with compression
        options.inJustDecodeBounds = false;
        options.inSampleSize = sampleSize;
        
        inputStream = context.getContentResolver().openInputStream(imageUri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
        inputStream.close();
        
        if (bitmap == null) {
            return null;
        }
        
        // Handle image rotation
        bitmap = handleImageRotation(bitmap, imageUri);
        
        // Final resize if still too large
        bitmap = resizeBitmapIfNeeded(bitmap);
        
        return bitmap;
    }
    
    /**
     * Calculate sample size for image compression
     */
    private int calculateSampleSize(int width, int height) {
        int sampleSize = 1;
        
        if (height > MAX_IMAGE_SIZE || width > MAX_IMAGE_SIZE) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            
            while ((halfHeight / sampleSize) >= MAX_IMAGE_SIZE
                    && (halfWidth / sampleSize) >= MAX_IMAGE_SIZE) {
                sampleSize *= 2;
            }
        }
        
        return sampleSize;
    }
    
    /**
     * Handle image rotation based on EXIF data
     */
    private Bitmap handleImageRotation(Bitmap bitmap, Uri imageUri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            ExifInterface exif = new ExifInterface(inputStream);
            inputStream.close();
            
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            
            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;
                default:
                    return bitmap; // No rotation needed
            }
            
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            
        } catch (Exception e) {
            Log.w(TAG, "Could not handle image rotation", e);
            return bitmap; // Return original if rotation fails
        }
    }
    
    /**
     * Resize bitmap if it's still too large
     */
    private Bitmap resizeBitmapIfNeeded(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        
        if (width <= MAX_IMAGE_SIZE && height <= MAX_IMAGE_SIZE) {
            return bitmap; // No resize needed
        }
        
        float ratio = Math.min((float) MAX_IMAGE_SIZE / width, (float) MAX_IMAGE_SIZE / height);
        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }
    
    /**
     * Save image to internal storage
     */
    private String saveImageToInternalStorage(Bitmap bitmap) {
        try {
            String fileName = "profile_" + System.currentTimeMillis() + ".jpg";
            File directory = new File(context.getFilesDir(), "profile_pictures");
            
            if (!directory.exists()) {
                directory.mkdirs();
            }
            
            File file = new File(directory, fileName);
            FileOutputStream fos = new FileOutputStream(file);
            
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, fos);
            fos.close();
            
            Log.d(TAG, "Image saved: " + file.getAbsolutePath());
            return fileName;
            
        } catch (Exception e) {
            Log.e(TAG, "Error saving image to internal storage", e);
            return null;
        }
    }
    
    /**
     * Convert bitmap to base64 string
     */
    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
    
    /**
     * Load profile picture from internal storage
     */
    public Bitmap loadProfilePicture(String fileName) {
        try {
            File directory = new File(context.getFilesDir(), "profile_pictures");
            File file = new File(directory, fileName);
            
            if (file.exists()) {
                return BitmapFactory.decodeFile(file.getAbsolutePath());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading profile picture", e);
        }
        
        return null;
    }
    
    /**
     * Delete profile picture from internal storage
     */
    public boolean deleteProfilePicture(String fileName) {
        try {
            File directory = new File(context.getFilesDir(), "profile_pictures");
            File file = new File(directory, fileName);
            
            if (file.exists()) {
                return file.delete();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error deleting profile picture", e);
        }
        
        return false;
    }
    
    /**
     * Create image file for camera capture
     */
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
    
    /**
     * Get current photo path (for camera captures)
     */
    public String getCurrentPhotoPath() {
        return currentPhotoPath;
    }
    
    /**
     * Validate image file
     */
    public ValidationResult validateImage(Uri imageUri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            
            if (inputStream == null) {
                return new ValidationResult(false, "Cannot access image file");
            }
            
            // Check file size
            long fileSize = inputStream.available();
            inputStream.close();
            
            if (fileSize > MAX_FILE_SIZE) {
                return new ValidationResult(false, "Image file is too large (max 2MB)");
            }
            
            // Check if it's a valid image
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            
            inputStream = context.getContentResolver().openInputStream(imageUri);
            BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();
            
            if (options.outWidth <= 0 || options.outHeight <= 0) {
                return new ValidationResult(false, "Invalid image format");
            }
            
            return new ValidationResult(true, "Image is valid");
            
        } catch (Exception e) {
            Log.e(TAG, "Error validating image", e);
            return new ValidationResult(false, "Error validating image: " + e.getMessage());
        }
    }
    
    /**
     * Result class for processed images
     */
    public static class ProcessedImage {
        public final boolean success;
        public final String message;
        public final String fileName;
        public final String base64String;
        
        public ProcessedImage(boolean success, String message, String fileName, String base64String) {
            this.success = success;
            this.message = message;
            this.fileName = fileName;
            this.base64String = base64String;
        }
    }
    
    /**
     * Result class for image validation
     */
    public static class ValidationResult {
        public final boolean isValid;
        public final String message;
        
        public ValidationResult(boolean isValid, String message) {
            this.isValid = isValid;
            this.message = message;
        }
    }
}