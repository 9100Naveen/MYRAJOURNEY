package com.example.myrajourney.exercise.tracking;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Tracks physical movement using Phone Sensors (Accelerometer + Gyroscope).
 * Designed for Rehab exercises where the phone is attached to the moving limb.
 */
public class SensorTracker implements SensorEventListener {
    private static final String TAG = "SensorTracker";

    private final SensorManager sensorManager;
    private final Sensor accelerometer;
    private final Sensor gyroscope;

    // Configuration
    private String exerciseId;
    private static final float PEAK_THRESHOLD = 1.2f;
    private static final long MIN_REP_INTERVAL_MS = 600;

    // State
    private float lastAccelY = 0;
    private boolean movingUp = false;
    private long lastRepTime = 0;

    private int repCount = 0;
    private float maxAngle = 0;
    private float currentAngle = 0;
    private float smoothnessSum = 0;
    private int smoothnessSamples = 0;

    // Filtering (kept for potential use, but following user logic)
    private float[] gravity = new float[3];
    private long lastTimestamp = 0;

    // Result callback
    public interface SensorCallback {
        void onSensorUpdate(int reps, float rom, float smoothness);
    }

    private SensorCallback callback;

    public SensorTracker(Context context, String exerciseId) {
        this.exerciseId = exerciseId;
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    }

    public void start(SensorCallback callback) {
        this.callback = callback;
        reset();

        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        }
        if (gyroscope != null) {
            sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_GAME);
        }
        Log.d(TAG, "Sensor tracking started for: " + exerciseId);
    }

    public void stop() {
        sensorManager.unregisterListener(this);
        Log.d(TAG, "Sensor tracking stopped");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            handleAcceleration(event.values[1]);
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            handleGyro(event.values[1]);
        }

        if (callback != null) {
            callback.onSensorUpdate(repCount, maxAngle, getSmoothness());
        }
    }

    private void handleAcceleration(float accelY) {
        float delta = accelY - lastAccelY;

        // Repetition detection (Slope/Peak Detection)
        if (delta > PEAK_THRESHOLD && !movingUp) {
            movingUp = true;
        }

        if (delta < -PEAK_THRESHOLD && movingUp) {
            long now = System.currentTimeMillis();
            if (now - lastRepTime > MIN_REP_INTERVAL_MS) {
                repCount++;
                lastRepTime = now;
                Log.d(TAG, "Rep detected: " + repCount);
            }
            movingUp = false;
        }

        smoothnessSum += Math.abs(delta);
        smoothnessSamples++;
        lastAccelY = accelY;
    }

    private void handleGyro(float gyroY) {
        // Simple integration as provided in production logic
        currentAngle += gyroY * 0.02f;
        maxAngle = Math.max(maxAngle, Math.abs(currentAngle));
    }

    public void reset() {
        repCount = 0;
        maxAngle = 0;
        currentAngle = 0;
        smoothnessSum = 0;
        smoothnessSamples = 0;
        lastRepTime = System.currentTimeMillis();
        movingUp = false;
        lastAccelY = 0;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public int getRepCount() {
        return repCount;
    }

    public float getMaxAngle() {
        return maxAngle;
    }

    public float getSmoothness() {
        if (smoothnessSamples == 0)
            return 100.0f;
        float avgJerk = smoothnessSum / smoothnessSamples;
        return Math.max(0, Math.min(100, 100 - avgJerk * 15));
    }
}
