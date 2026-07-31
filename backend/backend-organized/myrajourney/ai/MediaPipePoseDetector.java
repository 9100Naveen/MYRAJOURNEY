package com.example.myrajourney.ai;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.SystemClock;
import android.util.Log;
import androidx.camera.core.ImageProxy;

import com.google.mediapipe.framework.image.BitmapImageBuilder;
import com.google.mediapipe.framework.image.MPImage;
import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker;
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * MediaPipe Pose Landmarker implementation for real-time exercise tracking.
 */
public class MediaPipePoseDetector {
    private static final String TAG = "MediaPipePoseDetector";
    private static final String MODEL_NAME = "pose_landmarker.task";

    private PoseLandmarker poseLandmarker;
    private final Context context;
    private boolean isInitialized = false;
    private PoseDetectionListener listener;
    private final ExecutorService backgroundExecutor;

    public interface PoseDetectionListener {
        void onPoseResult(PoseLandmarkerResult result, MPImage inputImage);

        void onError(String error);
    }

    public MediaPipePoseDetector(Context context, PoseDetectionListener listener) {
        this.context = context;
        this.listener = listener;
        this.backgroundExecutor = Executors.newSingleThreadExecutor();
        setupPoseLandmarker();
    }

    private void setupPoseLandmarker() {
        backgroundExecutor.execute(() -> {
            try {
                BaseOptions baseOptions = BaseOptions.builder()
                        .setModelAssetPath(MODEL_NAME)
                        .build();

                PoseLandmarker.PoseLandmarkerOptions options = PoseLandmarker.PoseLandmarkerOptions.builder()
                        .setBaseOptions(baseOptions)
                        .setMinPoseDetectionConfidence(0.35f)
                        .setMinPosePresenceConfidence(0.35f)
                        .setMinTrackingConfidence(0.35f)
                        .setRunningMode(RunningMode.LIVE_STREAM)
                        .setResultListener(this::processResult)
                        .setErrorListener(this::processError)
                        .build();

                poseLandmarker = PoseLandmarker.createFromOptions(context, options);
                isInitialized = true;
                Log.d(TAG, "MediaPipe PoseLandmarker initialized successfully");
            } catch (Exception e) {
                isInitialized = false;
                Log.e(TAG, "Failed to initialize PoseLandmarker: " + e.getMessage());
                if (listener != null) {
                    listener.onError("Model initialization failed: " + e.getMessage());
                }
            }
        });
    }

    public void detect(ImageProxy imageProxy) {
        if (!isInitialized || poseLandmarker == null) {
            imageProxy.close();
            return;
        }

        long frameTime = SystemClock.uptimeMillis();
        Bitmap bitmap = imageProxyToBitmap(imageProxy);

        if (bitmap == null) {
            imageProxy.close();
            return;
        }

        // Rotate bitmap based on imageProxy rotation
        int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
        if (rotationDegrees != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotationDegrees);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }

        MPImage mpImage = new BitmapImageBuilder(bitmap).build();
        poseLandmarker.detectAsync(mpImage, frameTime);

        // Important: close the imageProxy after starting detection
        imageProxy.close();
    }

    private void processResult(PoseLandmarkerResult result, MPImage inputImage) {
        if (listener != null) {
            listener.onPoseResult(result, inputImage);
        }
    }

    private void processError(RuntimeException e) {
        Log.e(TAG, "MediaPipe PoseLandmarker error: " + e.getMessage());
        if (listener != null) {
            listener.onError(e.getMessage());
        }
    }

    public void close() {
        if (poseLandmarker != null) {
            poseLandmarker.close();
        }
        backgroundExecutor.shutdown();
    }

    private Bitmap imageProxyToBitmap(ImageProxy image) {
        if (image.getFormat() != android.graphics.ImageFormat.YUV_420_888) {
            Log.e(TAG, "Unsupported format: " + image.getFormat());
            return null;
        }

        java.nio.ByteBuffer yBuffer = image.getPlanes()[0].getBuffer();
        java.nio.ByteBuffer uBuffer = image.getPlanes()[1].getBuffer();
        java.nio.ByteBuffer vBuffer = image.getPlanes()[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        byte[] nv21 = new byte[ySize + uSize + vSize];

        // U and V are swapped
        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        android.graphics.YuvImage yuvImage = new android.graphics.YuvImage(nv21, android.graphics.ImageFormat.NV21,
                image.getWidth(), image.getHeight(), null);
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        yuvImage.compressToJpeg(new android.graphics.Rect(0, 0, image.getWidth(), image.getHeight()), 100, out);
        byte[] imageBytes = out.toByteArray();
        return android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }
}
