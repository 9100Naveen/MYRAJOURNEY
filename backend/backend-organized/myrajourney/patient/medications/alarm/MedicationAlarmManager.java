package com.example.myrajourney.patient.medications.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

import com.example.myrajourney.data.model.Medication;
import com.example.myrajourney.patient.medications.MedicationReminderReceiver;

import java.util.Calendar;
import java.util.List;

public class MedicationAlarmManager {
    
    private Context context;
    private AlarmManager alarmManager;
    
    public MedicationAlarmManager(Context context) {
        this.context = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }
    
    public void scheduleExactAlarm(Medication medication) {
        if (!canScheduleExactAlarms()) {
            // Fallback to inexact alarm
            scheduleInexactAlarm(medication);
            return;
        }
        
        long triggerTime = calculateTriggerTime(medication.getFormattedTime());
        
        // Safe ID parsing with fallback
        int medId;
        try {
            medId = Integer.parseInt(medication.getId());
        } catch (NumberFormatException e) {
            // Use hashcode as fallback if ID is not a valid integer
            medId = medication.getName().hashCode();
        }
        
        Intent intent = new Intent(context, MedicationReminderReceiver.class);
        intent.putExtra("medication_id", medId);
        intent.putExtra("medication_name", medication.getName());
        intent.putExtra("medication_dosage", medication.getDosage());
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            medId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | 
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0)
        );
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            );
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            );
        }
    }
    
    public void scheduleSnoozeAlarm(int medicationId, String medicationName, long triggerTime) {
        Intent intent = new Intent(context, MedicationReminderReceiver.class);
        intent.putExtra("medication_id", medicationId);
        intent.putExtra("medication_name", medicationName);
        intent.putExtra("is_snoozed", true);
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            medicationId + 10000, // Different request code for snooze
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | 
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0)
        );
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            );
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            );
        }
    }
    
    public void cancelAlarm(int medicationId) {
        Intent intent = new Intent(context, MedicationReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            medicationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | 
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0)
        );
        
        alarmManager.cancel(pendingIntent);
    }
    
    public boolean canScheduleExactAlarms() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return alarmManager.canScheduleExactAlarms();
        }
        return true;
    }
    
    private void scheduleInexactAlarm(Medication medication) {
        long triggerTime = calculateTriggerTime(medication.getFormattedTime());
        
        // Safe ID parsing with fallback
        int medId;
        try {
            medId = Integer.parseInt(medication.getId());
        } catch (NumberFormatException e) {
            // Use hashcode as fallback if ID is not a valid integer
            medId = medication.getName().hashCode();
        }
        
        Intent intent = new Intent(context, MedicationReminderReceiver.class);
        intent.putExtra("medication_id", medId);
        intent.putExtra("medication_name", medication.getName());
        intent.putExtra("medication_dosage", medication.getDosage());
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            medId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | 
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0)
        );
        
        alarmManager.setWindow(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            5 * 60 * 1000, // 5 minute window
            pendingIntent
        );
    }
    
    private long calculateTriggerTime(String timeString) {
        // Parse time string (e.g., "10:00 AM")
        Calendar calendar = Calendar.getInstance();
        
        try {
            String[] parts = timeString.replace(" AM", "").replace(" PM", "").split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            
            if (timeString.contains("PM") && hour != 12) {
                hour += 12;
            } else if (timeString.contains("AM") && hour == 12) {
                hour = 0;
            }
            
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            
            // If time has passed today, schedule for tomorrow
            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }
        } catch (Exception e) {
            // Default to 1 hour from now if parsing fails
            calendar.add(Calendar.HOUR, 1);
        }
        
        return calendar.getTimeInMillis();
    }
}
