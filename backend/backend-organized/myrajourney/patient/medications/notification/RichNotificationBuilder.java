package com.example.myrajourney.patient.medications.notification;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import com.example.myrajourney.R;
import com.example.myrajourney.data.model.Medication;
import com.example.myrajourney.patient.medications.model.DailyProgress;

public class RichNotificationBuilder {
    
    private Context context;
    
    public RichNotificationBuilder(Context context) {
        this.context = context;
    }
    
    public Notification buildMedicationNotification(Medication medication, DailyProgress progress) {
        String contentText = medication.getDosage() + " - " + medication.getFormattedTime();
        String bigText = "💊 " + medication.getName() + "\n" +
                        "📋 " + medication.getDosage() + "\n" +
                        "⏰ " + medication.getFormattedTime() + "\n" +
                        "📊 Progress: " + progress.getProgressText() + " (" + 
                        String.format("%.0f%%", progress.getAdherencePercentage()) + ")";
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
            context,
            NotificationChannelManager.CHANNEL_MEDICATION_REMINDERS
        )
            .setSmallIcon(R.drawable.logo)
            .setContentTitle("💊 Time to take " + medication.getName())
            .setContentText(contentText)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setVibrate(new long[]{0, 500, 200, 500});
        
        // Add "Take Now" action
        Intent takeNowIntent = new Intent(context, NotificationActionReceiver.class);
        takeNowIntent.setAction("ACTION_TAKE_NOW");
        takeNowIntent.putExtra("medication_id", medication.getId());
        takeNowIntent.putExtra("medication_name", medication.getName());
        
        int medId = Integer.parseInt(medication.getId());
        
        PendingIntent takeNowPendingIntent = PendingIntent.getBroadcast(
            context,
            medId,
            takeNowIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | 
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0)
        );
        
        builder.addAction(
            R.drawable.logo,
            "✓ Take Now",
            takeNowPendingIntent
        );
        
        // Add "Snooze 15 min" action
        Intent snoozeIntent = new Intent(context, NotificationActionReceiver.class);
        snoozeIntent.setAction("ACTION_SNOOZE");
        snoozeIntent.putExtra("medication_id", medication.getId());
        snoozeIntent.putExtra("medication_name", medication.getName());
        snoozeIntent.putExtra("snooze_minutes", 15);
        
        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            medId + 1000,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | 
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0)
        );
        
        builder.addAction(
            R.drawable.logo,
            "⏰ Snooze 15m",
            snoozePendingIntent
        );
        
        return builder.build();
    }
}
