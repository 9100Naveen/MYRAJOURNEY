package com.example.myrajourney.rehab.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.myrajourney.R;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Fragment for managing camera preview with CameraX
 */
public class CameraPreviewFragment extends Fragment {

    private static final String ARG_CAMERA_FACING = "camera_facing";
    private static final int CAMERA_FACING_FRONT = 0;
    private static final int CAMERA_FACING_BACK = 1;

    private PreviewView previewView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ProcessCameraProvider cameraProvider;
    private Preview preview;
    private ImageAnalysis imageAnalysis;
    private int cameraFacing = CAMERA_FACING_FRONT;

    // Callback interface for image analysis
    public interface ImageAnalysisCallback {
        void onImageAnalysis(androidx.camera.core.ImageProxy imageProxy);
    }

    private ImageAnalysisCallback imageAnalysisCallback;

    public static CameraPreviewFragment newInstance(boolean useFrontCamera) {
        CameraPreviewFragment fragment = new CameraPreviewFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_CAMERA_FACING, useFrontCamera ? CAMERA_FACING_FRONT : CAMERA_FACING_BACK);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            cameraFacing = getArguments().getInt(ARG_CAMERA_FACING, CAMERA_FACING_FRONT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera_preview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        previewView = view.findViewById(R.id.previewView);

        if (hasCameraPermission()) {
            setupCamera();
        } else {
            showPermissionError();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void setupCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases();
            } catch (ExecutionException | InterruptedException e) {
                android.util.Log.e("CameraPreview", "Error setting up camera: " + e.getMessage());
                showCameraError();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void bindCameraUseCases() {
        if (cameraProvider == null)
            return;

        // Unbind all use cases before rebinding
        cameraProvider.unbindAll();

        // Set up camera selector
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(cameraFacing == CAMERA_FACING_FRONT ? CameraSelector.LENS_FACING_FRONT
                        : CameraSelector.LENS_FACING_BACK)
                .build();

        // Set up preview use case
        preview = new Preview.Builder()
                .build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // Set up image analysis use case
        imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        // Set analyzer if callback is provided
        if (imageAnalysisCallback != null) {
            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(requireContext()),
                    imageProxy -> {
                        // Pass proxy to callback. IMPORTANT: Callback MUST close the proxy when done!
                        imageAnalysisCallback.onImageAnalysis(imageProxy);
                        // Do NOT close here, as the callback might process it asynchronously (e.g., ML
                        // Kit)
                    });
        }

        // Bind use cases to camera
        try {
            cameraProvider.bindToLifecycle(
                    getViewLifecycleOwner(),
                    cameraSelector,
                    preview,
                    imageAnalysis);

            android.util.Log.d("CameraPreview", "Camera bound successfully");
        } catch (Exception e) {
            android.util.Log.e("CameraPreview", "Error binding camera: " + e.getMessage());
            showCameraError();
        }
    }

    private void showPermissionError() {
        if (getContext() != null) {
            Toast.makeText(getContext(), "Camera permission required for motion tracking",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void showCameraError() {
        if (getContext() != null) {
            Toast.makeText(getContext(), "Failed to initialize camera", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Set callback for image analysis
     */
    public void setImageAnalysisCallback(ImageAnalysisCallback callback) {
        this.imageAnalysisCallback = callback;

        // Update analyzer if camera is already set up
        if (imageAnalysis != null && callback != null) {
            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(requireContext()),
                    imageProxy -> {
                        callback.onImageAnalysis(imageProxy);
                        imageProxy.close();
                    });
        }
    }

    /**
     * Switch between front and back camera
     */
    public void switchCamera() {
        cameraFacing = (cameraFacing == CAMERA_FACING_FRONT) ? CAMERA_FACING_BACK : CAMERA_FACING_FRONT;
        if (cameraProvider != null) {
            bindCameraUseCases();
        }
    }

    /**
     * Get the preview view for external access
     */
    public PreviewView getPreviewView() {
        return previewView;
    }

    /**
     * Check if camera is currently active
     */
    public boolean isCameraActive() {
        return cameraProvider != null && preview != null;
    }

    /**
     * Restart camera if needed
     */
    public void restartCamera() {
        if (hasCameraPermission()) {
            setupCamera();
        } else {
            showPermissionError();
        }
    }
}