package com.example.myrajourney.patient.medications.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;

import com.example.myrajourney.R;
import com.example.myrajourney.data.model.Medication;
import com.example.myrajourney.patient.dashboard.PatientDashboardActivity;
import com.example.myrajourney.patient.medications.alarm.MedicationAlarmManager;
import com.example.myrajourney.patient.medications.notification.NotificationChannelManager;

import java.util.List;

public class MedicationNotificationService extends Service {
    
    private static final int FOREGROUND_NOTIFICATION_ID = 9001;
    private MedicationAlarmManager alarmManager;
    
    @Override
    public void onCreate() {
        super.onCreate();
        alarmManager = new MedicationAlarmManager(this);
        
        // Create notification channels
        NotificationChannelManager.createNotificationChannels(this);
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent != null ? intent.getAction() : null;
        int medicationCount = intent != null ? intent.getIntExtra("medication_count", 0) : 0;
        
        if ("ACTION_START_SERVICE".equals(action)) {
            // Service started with medication count
            showForegroundNotification(medicationCount);
        } else if ("ACTION_RESCHEDULE_ALARMS".equals(action)) {
            // Reschedule all alarms after boot
            showForegroundNotification(0);
        } else {
            // Default start
            showForegroundNotification(0);
        }
        
        return START_STICKY;
    }
    
    private void showForegroundNotification(int pendingCount) {
        Intent notificationIntent = new Intent(this, PatientDashboardActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | 
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0)
        );
        
        String contentText = pendingCount > 0 
            ? pendingCount + " medication reminder(s) scheduled"
            : "Medication reminders active";
        
        Notification notification = new NotificationCompat.Builder(
            this,
            NotificationChannelManager.CHANNEL_SERVICE_STATUS
        )
            .setContentTitle("MyRA Journey")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.logo)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build();
        
        startForeground(FOREGROUND_NOTIFICATION_ID, notification);
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }
}
