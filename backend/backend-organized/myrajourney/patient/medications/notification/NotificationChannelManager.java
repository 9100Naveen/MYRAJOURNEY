package com.example.myrajourney.patient.medications.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class NotificationChannelManager {

    // Channel IDs
    public static final String CHANNEL_MEDICATION_REMINDERS = "medication_reminders";
    public static final String CHANNEL_SERVICE_STATUS = "service_status";
    public static final String CHANNEL_ADHERENCE_ALERTS = "adherence_alerts";

    /**
     * Create all notification channels
     */
    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = 
                context.getSystemService(NotificationManager.class);

            // Medication Reminders Channel (HIGH importance)
            NotificationChannel medicationChannel = new NotificationChannel(
                CHANNEL_MEDICATION_REMINDERS,
                "Medication Reminders",
                NotificationManager.IMPORTANCE_HIGH
            );
            medicationChannel.setDescription("Reminders to take your medications");
            medicationChannel.enableVibration(true);
            medicationChannel.setVibrationPattern(new long[]{0, 500, 200, 500});
            medicationChannel.setShowBadge(true);
            notificationManager.createNotificationChannel(medicationChannel);

            // Service Status Channel (LOW importance)
            NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_SERVICE_STATUS,
                "Service Status",
                NotificationManager.IMPORTANCE_LOW
            );
            serviceChannel.setDescription("Medication reminder service status");
            serviceChannel.enableVibration(false);
            serviceChannel.setShowBadge(false);
            notificationManager.createNotificationChannel(serviceChannel);

            // Adherence Alerts Channel (DEFAULT importance)
            NotificationChannel adherenceChannel = new NotificationChannel(
                CHANNEL_ADHERENCE_ALERTS,
                "Adherence Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            );
            adherenceChannel.setDescription("Alerts about medication adherence");
            adherenceChannel.setShowBadge(true);
            notificationManager.createNotificationChannel(adherenceChannel);
        }
    }

    /**
     * Check if notifications are enabled
     */
    public static boolean areNotificationsEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED;
        }
        return true; // Pre-Android 13, notifications are enabled by default
    }
}
