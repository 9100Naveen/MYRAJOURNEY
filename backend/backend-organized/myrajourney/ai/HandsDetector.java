package com.example.myrajourney.ai;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.SystemClock;
import android.util.Log;
import androidx.camera.core.ImageProxy;

import com.example.myrajourney.rehab.models.FormFeedback;
import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker;
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult;
import com.google.mediapipe.framework.image.BitmapImageBuilder;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import com.google.mediapipe.framework.image.MPImage;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * specialized detector for hand exercises (Wrist Flexion/Extension)
 * Uses MediaPipe Hands for accurate finding of MCP/PIP/DIP joints which ML Kit
 * lacks.
 */
public class HandsDetector {
    private static final String TAG = "HandsDetector";
    private static final String MODEL_NAME = "hand_landmarker.task";
    private HandLandmarker handLandmarker;
    private final Context context;
    private boolean isInitialized = false;

    // Interface for results
    public interface HandDetectionListener {
        void onResult(FormFeedback feedback);

        void onError(String error);
    }

    public HandsDetector(Context context) {
        this.context = context;
        setupHandLandmarker();
    }

    private void setupHandLandmarker() {
        BaseOptions.Builder baseOptionsBuilder = BaseOptions.builder()
                .setModelAssetPath(MODEL_NAME);

        try {
            BaseOptions baseOptions = baseOptionsBuilder.build();
            HandLandmarker.HandLandmarkerOptions options = HandLandmarker.HandLandmarkerOptions.builder()
                    .setBaseOptions(baseOptions)
                    .setMinHandDetectionConfidence(0.35f)
                    .setMinTrackingConfidence(0.35f)
                    .setRunningMode(RunningMode.LIVE_STREAM)
                    .setResultListener(this::returnLivestreamResult)
                    .setErrorListener(this::returnLivestreamError)
                    .build();

            handLandmarker = HandLandmarker.createFromOptions(context, options);
            isInitialized = true;
            android.util.Log.d(TAG, "MediaPipe HandLandmarker initialized successfully");
        } catch (IllegalStateException e) {
            android.util.Log.e(TAG, "MediaPipe failed to initialize: " + e.getMessage());
            isInitialized = false;
        } catch (RuntimeException e) {
            android.util.Log.e(TAG, "MediaPipe failed to load model: " + e.getMessage());
            isInitialized = false;
        }
    }

    // Changing approach: Use a singleton listener or interface passed to
    // constructor?
    // The previous AdvancedMotionTracker code used a per-call listener.
    // MediaPipe callbacks are global to the detector instance.
    // We'll add a volatile listener member.

    private volatile HandDetectionListener currentListener;

    public void process(ImageProxy imageProxy, int rotationDegrees, HandDetectionListener listener) {
        if (!isInitialized || handLandmarker == null) {
            // Try to re-init?
            setupHandLandmarker();
            if (!isInitialized) {
                listener.onError(
                        "Model 'hand_landmarker.task' missing or invalid. Please download it to assets folder.");
                imageProxy.close();
                return;
            }
        }

        this.currentListener = listener;

        long frameTime = SystemClock.uptimeMillis();

        // Convert ImageProxy to MPImage
        Bitmap bitmap = imageProxyToBitmap(imageProxy);
        if (bitmap == null) {
            listener.onError("Failed to convert image");
            imageProxy.close();
            return;
        }

        // Rotate bitmap if needed
        if (rotationDegrees != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotationDegrees);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }

        MPImage mpImage = new BitmapImageBuilder(bitmap).build();
        handLandmarker.detectAsync(mpImage, frameTime);
        // Note: Do NOT close imageProxy here immediately if using async?
        // Actually, for MP async, we pass a bitmap copy. We can close the proxy.
        // The original code managed closing in the listener wrapper or forgot it.
        // We closed it in AdvancedMotionTracker, so we are good.
    }

    private void returnLivestreamError(RuntimeException e) {
        if (currentListener != null) {
            currentListener.onError(e.getMessage());
        }
    }

    // Result Processor
    private void returnLivestreamResult(HandLandmarkerResult result, MPImage inputImage) {
        if (currentListener == null)
            return;

        if (result.landmarks().isEmpty()) {
            currentListener.onResult(new FormFeedback(false, 0.0f, "Hand not visible", Collections.emptyMap()));
            return;
        }

        // Extract landmarks for the first hand
        java.util.List<com.google.mediapipe.tasks.components.containers.NormalizedLandmark> landmarks = result
                .landmarks().get(0);

        Map<String, android.graphics.Point> skeleton = new HashMap<>();

        // Helper to add point
        addPoint(skeleton, landmarks, 0, "WRIST", inputImage.getWidth(), inputImage.getHeight());

        // Thumb
        addPoint(skeleton, landmarks, 1, "THUMB_CMC", inputImage.getWidth(), inputImage.getHeight());
        addPoint(skeleton, landmarks, 2, "THUMB_MCP", inputImage.getWidth(), inputImage.getHeight());
        addPoint(skeleton, landmarks, 3, "THUMB_IP", inputImage.getWidth(), inputImage.getHeight());
        addPoint(skeleton, landmarks, 4, "THUMB_TIP", inputImage.getWidth(), inputImage.getHeight());

        // Index
        addPoint(skeleton, landmarks, 5, "INDEX_FINGER_MCP", inputImage.getWidth(), inputImage.getHeight());
        addPoint(skeleton, landmarks, 6, "INDEX_FINGER_PIP", inputImage.getWidth(), inputImage.getHeight());
        addPoint(skeleton, landmarks, 7, "INDEX_FINGER_DIP", inputImage.getWidth(), inputImage.getHeight());
        addPoint(skeleton, landmarks, 8, "INDEX_FINGER_TIP", inputImage.getWidth(), inputImage.getHeight());

        // Middle
        addPoint(skeleton, landmarks, 9, "MIDDLE_FINGER_MCP", inputImage.getWidth(), inputImage.getHeight());
        addPoint(skeleton, landmarks, 10, "MIDDLE_FINGER_PIP", inputImage.getWidth(), inputImage.getHeight());
        addPoint(skeleton, landmarks, 11, "MIDDLE_FINGER_DIP", inputImage.getWidth(), inputImage.getHeight());
        addPoint(skeleton, landmarks, 12, "MIDDLE_FINGER_TIP", inputImage.getWidth(), inputImage.getHeight());

        // Ring
        addPoint(skeleton, landmarks, 13, "RING_FINGER_MCP", inputImage.getWidth(), inputImage.getHeight());
        addPoint(skeleton, landmarks, 14, "RING_FINGER_PIP", inputImage.getWidth(), inputImage.getHeight());
        addPoint(skeleton, landmarks, 15, "RING_FINGER_DIP", inputImage.getWidth(), inputImage.getHeight());
        addPoint(skeleton, landmarks, 16, "RING_FINGER_TIP", inputImage.getWidth(), inputImage.getHeight());

        // Pinky
        addPoint(skeleton, landmarks, 17, "PINKY_MCP", inputImage.getWidth(), inputImage.getHeight());
        addPoint(skeleton, landmarks, 18, "PINKY_PIP", inputImage.getWidth(), inputImage.getHeight());
        addPoint(skeleton, landmarks, 19, "PINKY_DIP", inputImage.getWidth(), inputImage.getHeight());
        addPoint(skeleton, landmarks, 20, "PINKY_TIP", inputImage.getWidth(), inputImage.getHeight());

        // Calculate Score (Middle finger straightness as proxy for extension)
        double angle = calculateFingerAngle(landmarks, 9, 10, 11); // Middle Finger MCP-PIP-DIP
        boolean isStraight = angle > 150.0;

        // Map 150-180 to 0-100 score
        float score = (float) Math.max(0.0, Math.min(100.0, (angle - 140) * 2.5));

        // Pass back skeleton
        String msg = isStraight ? "Good extension" : "Straighten fingers";
        FormFeedback feedback = new FormFeedback(isStraight, score, msg, skeleton);
        currentListener.onResult(feedback);
    }

    /**
     * Calculate angle between three landmarks (p1-p2-p3)
     */
    private double calculateFingerAngle(
            java.util.List<com.google.mediapipe.tasks.components.containers.NormalizedLandmark> landmarks,
            int p1Idx, int p2Idx, int p3Idx) {
        if (p1Idx >= landmarks.size() || p2Idx >= landmarks.size() || p3Idx >= landmarks.size())
            return 0.0;

        com.google.mediapipe.tasks.components.containers.NormalizedLandmark p1 = landmarks.get(p1Idx);
        com.google.mediapipe.tasks.components.containers.NormalizedLandmark p2 = landmarks.get(p2Idx);
        com.google.mediapipe.tasks.components.containers.NormalizedLandmark p3 = landmarks.get(p3Idx);

        double a1 = Math.atan2(p1.y() - p2.y(), p1.x() - p2.x());
        double a2 = Math.atan2(p3.y() - p2.y(), p3.x() - p2.x());

        double angle = Math.abs(Math.toDegrees(a1 - a2));
        if (angle > 180)
            angle = 360 - angle;

        return angle;
    }

    private void addPoint(Map<String, android.graphics.Point> skeleton,
            java.util.List<com.google.mediapipe.tasks.components.containers.NormalizedLandmark> landmarks,
            int index, String name, int width, int height) {
        if (index < landmarks.size()) {
            skeleton.put(name, new android.graphics.Point(
                    (int) (landmarks.get(index).x() * width),
                    (int) (landmarks.get(index).y() * height)));
        }
    }

    // Helper to convert ImageProxy (YUV) to Bitmap (ARGB)
    private Bitmap imageProxyToBitmap(ImageProxy image) {
        if (image.getFormat() != ImageFormat.YUV_420_888) {
            android.util.Log.e(TAG, "Unsupported format: " + image.getFormat());
            return null;
        }

        ByteBuffer yBuffer = image.getPlanes()[0].getBuffer();
        ByteBuffer uBuffer = image.getPlanes()[1].getBuffer();
        ByteBuffer vBuffer = image.getPlanes()[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        byte[] nv21 = new byte[ySize + uSize + vSize];

        // U and V are swapped
        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, image.getWidth(), image.getHeight()), 100, out);
        byte[] imageBytes = out.toByteArray();
        return android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }
}
