package com.example.myrajourney.patient.medications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.example.myrajourney.R;
import com.example.myrajourney.patient.medications.notification.NotificationChannelManager;
import com.example.myrajourney.patient.medications.PatientMedicationsActivity;

public class MedicationReminderReceiver extends BroadcastReceiver {

    private static final String TAG = "MedReminderReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive called - Alarm triggered!");
        
        int medicationId = intent.getIntExtra("medication_id", 0);
        String medicationName = intent.getStringExtra("medication_name");
        String medicationDosage = intent.getStringExtra("medication_dosage");
        
        Log.d(TAG, "Medication ID: " + medicationId);
        Log.d(TAG, "Medication Name: " + medicationName);
        Log.d(TAG, "Medication Dosage: " + medicationDosage);
        
        if (medicationId == 0 || medicationName == null) {
            Log.e(TAG, "Invalid medication data - skipping notification");
            return;
        }
        
        // Show toast for debugging
        Toast.makeText(context, "Alarm triggered for: " + medicationName, Toast.LENGTH_LONG).show();

        // Create notification channels
        NotificationChannelManager.createNotificationChannels(context);

        // Create intent to open medications activity
        Intent openIntent = new Intent(context, PatientMedicationsActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            medicationId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | 
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0)
        );

        // Create "Mark as Taken" action
        Intent takenIntent = new Intent(context, MarkMedicationTakenReceiver.class);
        takenIntent.putExtra("medication_id", medicationId);
        takenIntent.putExtra("medication_name", medicationName);
        
        PendingIntent takenPendingIntent = PendingIntent.getBroadcast(
            context,
            medicationId + 1000,
            takenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | 
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0)
        );

        // Build notification
        String title = "💊 Medication Reminder";
        String message = medicationName + (medicationDosage != null ? " - " + medicationDosage : "");
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
            context,
            NotificationChannelManager.CHANNEL_MEDICATION_REMINDERS
        )
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(new NotificationCompat.BigTextStyle()
                .bigText(message + "\n\nTime to take your medication!"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.logo, "Mark as Taken", takenPendingIntent)
            .setVibrate(new long[]{0, 500, 200, 500})
            .setDefaults(NotificationCompat.DEFAULT_SOUND);

        // Show notification
        NotificationManager notificationManager = 
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        if (notificationManager != null) {
            Log.d(TAG, "Showing notification with ID: " + medicationId);
            notificationManager.notify(medicationId, builder.build());
            Log.d(TAG, "Notification shown successfully!");
        } else {
            Log.e(TAG, "NotificationManager is null!");
        }
    }
}






