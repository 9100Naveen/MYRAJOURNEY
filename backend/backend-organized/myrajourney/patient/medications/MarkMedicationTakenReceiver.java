package com.example.myrajourney.patient.medications;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class MarkMedicationTakenReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String medicationName = intent.getStringExtra("medication_name");

        if (medicationName != null) {
            // Update medication status in SharedPreferences
            android.content.SharedPreferences prefs = context.getSharedPreferences("medication_status", Context.MODE_PRIVATE);
            String today = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());
            prefs.edit().putBoolean(medicationName + "_" + today, true).apply();

            // Show confirmation toast
            Toast.makeText(context, medicationName + " marked as taken!", Toast.LENGTH_SHORT).show();

            // Cancel the reminder notification
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(1001); // Same ID as in MedicationReminderReceiver
        }
    }
}






