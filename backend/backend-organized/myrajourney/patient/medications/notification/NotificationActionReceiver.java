package com.example.myrajourney.patient.medications.notification;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.example.myrajourney.core.network.ApiClient;
import com.example.myrajourney.core.network.ApiService;
import com.example.myrajourney.data.model.ApiResponse;
import com.example.myrajourney.data.model.MedicationLog;
import com.example.myrajourney.data.model.MedicationLogRequest;
import com.example.myrajourney.patient.medications.alarm.MedicationAlarmManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationActionReceiver extends BroadcastReceiver {
    
    private static final String PREFS_NAME = "MedicationProgress";
    private static final String KEY_TAKEN_TODAY = "taken_today";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        int medicationId = intent.getIntExtra("medication_id", 0);
        String medicationName = intent.getStringExtra("medication_name");
        
        if (action == null || medicationId == 0) return;
        
        switch (action) {
            case "ACTION_TAKE_NOW":
                handleTakeNow(context, medicationId, medicationName);
                break;
            case "ACTION_SNOOZE":
                int snoozeMinutes = intent.getIntExtra("snooze_minutes", 15);
                handleSnooze(context, medicationId, medicationName, snoozeMinutes);
                break;
        }
    }
    
    private void handleTakeNow(Context context, int medicationId, String medicationName) {
        // Dismiss notification
        NotificationManager notificationManager = 
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(medicationId);
        
        // Log to backend
        logMedicationIntake(context, medicationId, medicationName);
        
        // Update progress
        updateDailyProgress(context);
        
        // Show confirmation
        Toast.makeText(context, 
            "✓ " + medicationName + " marked as taken", 
            Toast.LENGTH_SHORT).show();
    }
    
    private void handleSnooze(Context context, int medicationId, String medicationName, 
                             int snoozeMinutes) {
        // Dismiss current notification
        NotificationManager notificationManager = 
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(medicationId);
        
        // Reschedule alarm
        MedicationAlarmManager alarmManager = new MedicationAlarmManager(context);
        long snoozeTime = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000);
        alarmManager.scheduleSnoozeAlarm(medicationId, medicationName, snoozeTime);
        
        // Show confirmation
        Toast.makeText(context, 
            "⏰ Reminder snoozed for " + snoozeMinutes + " minutes", 
            Toast.LENGTH_SHORT).show();
    }
    
    private void logMedicationIntake(Context context, int medicationId, String medicationName) {
        ApiService api = ApiClient.getApiService(context);
        
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            .format(new Date());
        
        MedicationLogRequest request = new MedicationLogRequest(
            String.valueOf(medicationId),
            timestamp
        );
        
        api.logMedicationIntake(request).enqueue(new Callback<ApiResponse<MedicationLog>>() {
            @Override
            public void onResponse(Call<ApiResponse<MedicationLog>> call, 
                                 Response<ApiResponse<MedicationLog>> response) {
                // Success - already showed toast
            }
            
            @Override
            public void onFailure(Call<ApiResponse<MedicationLog>> call, Throwable t) {
                // Failed - could retry later
            }
        });
    }
    
    private void updateDailyProgress(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int takenToday = prefs.getInt(KEY_TAKEN_TODAY, 0);
        prefs.edit().putInt(KEY_TAKEN_TODAY, takenToday + 1).apply();
    }
}
