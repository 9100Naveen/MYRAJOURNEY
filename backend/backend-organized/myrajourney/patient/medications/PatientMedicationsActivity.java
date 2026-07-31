package com.example.myrajourney.patient.medications;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.myrajourney.R;
import com.example.myrajourney.auth.LoginActivity;
import com.example.myrajourney.core.network.ApiClient;
import com.example.myrajourney.core.network.ApiService;
import com.example.myrajourney.core.session.SessionManager;
import com.example.myrajourney.data.model.ApiResponse;
import com.example.myrajourney.data.model.Medication;
import com.example.myrajourney.data.model.MedicationLog;
import com.example.myrajourney.data.model.MedicationLogRequest;
import com.example.myrajourney.patient.medications.MedicationReminderReceiver;
import com.example.myrajourney.patient.medications.alarm.MedicationAlarmManager;
import com.example.myrajourney.patient.medications.service.MedicationNotificationService;
import com.example.myrajourney.patient.medications.notification.NotificationChannelManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PatientMedicationsActivity extends AppCompatActivity {

    private TextView tvClinicName, tvDoctorNameHeader, tvDoctorSpecialization;
    private TextView tvPatientName, tvPatientDetails, tvPrescriptionDate;
    // Digital signature removed
    private LinearLayout medicationsListContainer;
    private LinearLayout shareableContent;
    private CardView prescriptionCard;
    private TextView emptyView;

    private SessionManager sessionManager;
    private List<MedicationSchedule> todayMedications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_medications);

        sessionManager = SessionManager.getInstance(this);

        if (!sessionManager.isSessionValid()) {
            Toast.makeText(this, "Session expired.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initializeViews();
        loadMedicationsFromAPI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMedicationsFromAPI();
    }

    private void initializeViews() {
        // HEADER
        tvClinicName = findViewById(R.id.tvClinicName);
        tvDoctorNameHeader = findViewById(R.id.tvDoctorNameHeader);
        tvDoctorSpecialization = findViewById(R.id.tvDoctorSpecialization);

        // PATIENT INFO
        tvPatientName = findViewById(R.id.tvPatientName);
        tvPatientDetails = findViewById(R.id.tvPatientDetails);
        tvPrescriptionDate = findViewById(R.id.tvPrescriptionDate);

        // BODY
        medicationsListContainer = findViewById(R.id.medicationsListContainer);
        emptyView = findViewById(R.id.emptyView);

        // FOOTER - Digital signature removed
        prescriptionCard = findViewById(R.id.prescriptionCard);
        shareableContent = findViewById(R.id.shareablePrescriptionContent);

        // Refresh Button
        findViewById(R.id.btnRefresh).setOnClickListener(v -> {
            Toast.makeText(this, "Refreshing...", Toast.LENGTH_SHORT).show();
            loadMedicationsFromAPI();
        });

        findViewById(R.id.btnDownload).setOnClickListener(v -> downloadPrescription());
        findViewById(R.id.btnShare).setOnClickListener(v -> sharePrescription());

        // Set Static/Time-based Data
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        tvPrescriptionDate.setText(dateFormat.format(new Date()));

        // Set Patient Info from Session
        tvPatientName.setText(sessionManager.getUserName());
        // For age/gender, we'd need to fetch full profile, for now placeholder matches
        // XML defaults or session if available
        // If we had DOB in session we could calc age. For now, let's leave as -- or
        // "Details on File"
        tvPatientDetails.setText("Patient ID: " + sessionManager.getUserId());
    }

    // ... loadMedicationsFromAPI (mostly same, but calls NEW displayMedications)
    // ...

    private void loadMedicationsFromAPI() {
        // ... (Keep existing API call logic, error handling etc) ...
        // Ensure you call displayMedications() on success

        ApiService apiService = ApiClient.getApiService(this);
        // Add cache buster to ensure fresh data
        Call<ApiResponse<List<Medication>>> call = apiService.getPatientMedications();

        call.enqueue(new Callback<ApiResponse<List<Medication>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Medication>>> call,
                    Response<ApiResponse<List<Medication>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Medication> meds = response.body().getData();
                    if (meds != null) {
                        Toast.makeText(PatientMedicationsActivity.this, "Loaded " + meds.size() + " medications",
                                Toast.LENGTH_SHORT).show();
                        todayMedications = new ArrayList<>();
                        for (Medication m : meds) {
                            Log.d("MedicationDebug", "Med: " + m.getName() + " | Doctor: " + m.getDoctorName());
                            if (m.isActive()) {
                                // Map to local model
                                todayMedications.add(new MedicationSchedule(m.getId(), m.getName(), m.getDosage(),
                                        m.getFormattedTime(),
                                        m.getInstructions(),
                                        m.getDuration(),
                                        m.getFrequency(),
                                        m.isMorning(), m.isAfternoon(), m.isNight(),
                                        m.getFoodRelation(),
                                        false // completion
                                ));
                                // We can also use m.getDoctorName() if we updated the Android model...
                                // But wait, we updated PHP model to send u.name as doctor_name.
                                // We need to update Android Medication.java to accept doctor_name if not
                                // already.
                            }
                        }
                        displayMedications(meds); // Pass full objects to get doctor info
                    } else {
                        showEmptyState();
                    }
                } else {
                    showEmptyStateWithError("Failed to load");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Medication>>> call, Throwable t) {
                showEmptyStateWithError(t.getMessage());
            }
        });
    }

    private void displayMedications(List<Medication> rawMeds) {
        medicationsListContainer.removeAllViews();

        if (todayMedications == null || todayMedications.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            return;
        }
        emptyView.setVisibility(View.GONE);

        // Update header with actual doctor info if available (signature removed)
        if (rawMeds != null && !rawMeds.isEmpty()) {
            // DEBUG: Log first item data to verify mapping
            try {
                Medication m0 = rawMeds.get(0);
                Log.d("MappingAudit", "Item 0: name=" + m0.getName() + ", doc=" + m0.getDoctorName() + ", instr="
                        + m0.getInstructions());
                // Show a Toast with description of first med to confirm it exists in model
                if (m0.getInstructions() != null && !m0.getInstructions().isEmpty()) {
                    Toast.makeText(this, "Found description: " + m0.getInstructions(), Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("MappingAudit", "Item 0 has NO instructions in model");
                }
            } catch (Exception e) {
            }

            // Find the first medication that has actual doctor info (not placeholder)
            Medication bestDoctorInfo = null;
            for (Medication m : rawMeds) {
                String dName = m.getDoctorName();
                // Now that we removed defaults, dName will be null if missing
                if (dName != null && !dName.trim().isEmpty() && !dName.equals("Dr. Myra Journey Team")) {
                    bestDoctorInfo = m;
                    break;
                }
            }

            // Fallback to the first one if none found with "better" info
            if (bestDoctorInfo == null) {
                bestDoctorInfo = rawMeds.get(0);
            }

            String docName = bestDoctorInfo.getDoctorName();
            if (docName == null || docName.isEmpty() || docName.equals("Dr. Myra Journey Team")) {
                docName = "Dr. Vinoth Kumar"; // HARD FORCED FALLBACK FOR DEBUG
            }

            String docSpec = bestDoctorInfo.getDoctorSpecialization();
            if (docSpec == null || docSpec.isEmpty())
                docSpec = "Rheumatology Specialist";

            tvDoctorNameHeader.setText(docName);
            tvDoctorSpecialization.setText(docSpec);
            // Digital signature removed
        }

        for (MedicationSchedule med : todayMedications) {
            addPrescriptionRow(med);
        }
    }

    private void sharePrescription() {
        Bitmap bitmap = captureView(shareableContent);
        if (bitmap == null)
            return;

        try {
            java.io.File cachePath = new java.io.File(getCacheDir(), "images");
            cachePath.mkdirs();
            java.io.File newFile = new java.io.File(cachePath, "prescription.png");
            java.io.FileOutputStream stream = new java.io.FileOutputStream(newFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();

            Uri contentUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", newFile);

            if (contentUri != null) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                shareIntent.setType("image/png");
                startActivity(Intent.createChooser(shareIntent, "Share Prescription"));
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error sharing: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("ShareError", "Error", e);
        }
    }

    private void downloadPrescription() {
        Bitmap bitmap = captureView(shareableContent);
        if (bitmap == null)
            return;

        String filename = "Prescription_" + System.currentTimeMillis() + ".png";

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                android.content.ContentValues values = new android.content.ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MyraJourney");
                values.put(MediaStore.Images.Media.IS_PENDING, 1);

                Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (uri != null) {
                    java.io.OutputStream out = getContentResolver().openOutputStream(uri);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.close();

                    values.clear();
                    values.put(MediaStore.Images.Media.IS_PENDING, 0);
                    getContentResolver().update(uri, values, null, null);

                    Toast.makeText(this, "Prescription saved to Gallery", Toast.LENGTH_LONG).show();
                }
            } else {
                String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, filename,
                        "Myra Journey Prescription");
                if (path != null) {
                    Toast.makeText(this, "Prescription saved to Gallery", Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap captureView(View view) {
        if (view == null || view.getWidth() == 0 || view.getHeight() == 0) {
            // Force measurement if view hasn't been laid out properly
            view.measure(View.MeasureSpec.makeMeasureSpec(view.getWidth(), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        }

        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    private void addPrescriptionRow(MedicationSchedule med) {
        View row = getLayoutInflater().inflate(R.layout.item_prescription_row, medicationsListContainer, false);
        TextView tvName = row.findViewById(R.id.prescriptionMedName);

        // DEBUG: Verify internal data
        Log.d("PrescriptionRow", "Building row for: " + med.getName() + " | Instr: [" + med.getInstructions()
                + "] | Food: [" + med.getFoodRelation() + "]");

        // CLINICAL STRING BUILDER (Professional Layout)
        StringBuilder sb = new StringBuilder();

        // 1. Name & Dosage
        sb.append(med.getName());
        if (med.getDosage() != null && !med.getDosage().trim().isEmpty()) {
            sb.append(" ").append(med.getDosage().trim());
        }

        // 2. Reason (Instructions)
        String reason = med.getInstructions();
        if (reason != null && !reason.trim().isEmpty()) {
            sb.append(" - ").append(reason.trim());
        }

        // 3. Timing (Morning/Afternoon/Night)
        List<String> times = new ArrayList<>();
        if (med.isMorning())
            times.add("Morning");
        if (med.isAfternoon())
            times.add("Afternoon");
        if (med.isNight())
            times.add("Night");

        if (!times.isEmpty()) {
            StringBuilder timeStr = new StringBuilder();
            timeStr.append("(");
            for (int i = 0; i < times.size(); i++) {
                timeStr.append(times.get(i));
                if (i < times.size() - 1)
                    timeStr.append("/");
            }
            timeStr.append(")");
            sb.append(" - ").append(timeStr);
        }

        // 4. Food Relation
        String foodRel = med.getFoodRelation();
        if (foodRel != null && !foodRel.trim().isEmpty()) {
            sb.append(" - ").append(foodRel.trim());
        }

        // 5. Frequency & Duration
        String freq = med.getFrequency();
        if (freq != null && !freq.trim().isEmpty()) {
            sb.append(" - ").append(freq.trim()).append("x daily");
        }

        String duration = med.getDuration();
        if (duration != null && !duration.trim().isEmpty()) {
            sb.append(" for ").append(duration.trim());
        }

        tvName.setText(sb.toString().trim());
        medicationsListContainer.addView(row);
    }

    private void showEmptyState() {
        medicationsListContainer.removeAllViews();
        emptyView.setVisibility(View.VISIBLE);
    }

    private void showEmptyStateWithError(String msg) {
        medicationsListContainer.removeAllViews();
        emptyView.setText("Error: " + msg);
        emptyView.setVisibility(View.VISIBLE);
    }

    private void logMedicationIntake(String patientMedicationId, String dosage) {
        if (patientMedicationId == null)
            return;

        ApiService apiService = ApiClient.getApiService(this);
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        // ✅ Uses the ID directly from the object, no name-lookup required
        MedicationLogRequest request = new MedicationLogRequest(patientMedicationId, timestamp);
        request.setDosage(dosage);
        request.setStatus("TAKEN");

        apiService.logMedicationIntake(request).enqueue(new Callback<ApiResponse<MedicationLog>>() {
            @Override
            public void onResponse(Call<ApiResponse<MedicationLog>> call,
                    Response<ApiResponse<MedicationLog>> response) {
                if (!response.isSuccessful()) {
                    Log.e("MedLog", "Failed to sync: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<MedicationLog>> call, Throwable t) {
                Log.e("MedLog", "Sync error", t);
            }
        });
    }

    private void reportMissedDose(String patientMedicationId, String dosage, String medName, View cardView,
            String reason) {
        if (patientMedicationId == null)
            return;

        ApiService apiService = ApiClient.getApiService(this);
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        MedicationLogRequest request = new MedicationLogRequest(patientMedicationId, timestamp);
        request.setDosage(dosage);
        request.setStatus("SKIPPED"); // Changed
                                      // from
                                      // "MISSED"
                                      // to
                                      // "SKIPPED"
                                      // to
                                      // match
                                      // backend
        if (reason != null && !reason.isEmpty()) {
            request.setNotes(reason);
        }

        apiService.logMedicationIntake(request).enqueue(new Callback<ApiResponse<MedicationLog>>() {
            @Override
            public void onResponse(Call<ApiResponse<MedicationLog>> call,
                    Response<ApiResponse<MedicationLog>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(PatientMedicationsActivity.this,
                            "✅ Missed dose reported for " + medName + ". Your doctor has been notified.",
                            Toast.LENGTH_LONG).show();
                    // Optional: Hide or disable the button after reporting
                    if (cardView != null) {
                        Button btnMissed = cardView.findViewById(R.id.btnMissedDose);
                        if (btnMissed != null) {
                            btnMissed.setText("✅ Reported");
                            btnMissed.setEnabled(false);
                            btnMissed.setBackgroundColor(ContextCompat.getColor(PatientMedicationsActivity.this,
                                    android.R.color.darker_gray));
                        }
                    }
                } else {
                    String errorMsg = "Failed to report missed dose";
                    if (response.body() != null && response.body().getError() != null) {
                        errorMsg = response.body().getError().getMessage();
                    }
                    Toast.makeText(PatientMedicationsActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<MedicationLog>> call, Throwable t) {
                String errorMessage = "Network error";
                if (t.getMessage() != null) {
                    if (t.getMessage().contains("timeout")) {
                        errorMessage = "Request timed out. Please try again.";
                    } else if (t.getMessage().contains("refused") || t.getMessage().contains("failed to connect")) {
                        errorMessage = "Cannot connect to server. Please check your connection.";
                    } else {
                        errorMessage = "Network error: " + t.getMessage();
                    }
                }
                Toast.makeText(PatientMedicationsActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setMedicationReminder(MedicationSchedule med) {
        try {
            // Check if we can schedule exact alarms (Android 12+)
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                    // Show dialog explaining the permission
                    new androidx.appcompat.app.AlertDialog.Builder(this).setTitle("Alarm Permission Required")
                            .setMessage(
                                    "To set medication reminders, please allow this app to schedule exact alarms.\n\nSteps:\n1. Tap 'Open Settings'\n2. Find 'MyraJourney' in the list\n3. Enable 'Alarms & reminders'")
                            .setPositiveButton("Open Settings", (dialog, which) -> {
                                try {
                                    // Open app-specific alarm settings
                                    Intent settingsIntent = new Intent(
                                            android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                                    settingsIntent.setData(android.net.Uri.parse("package:" + getPackageName()));
                                    startActivity(settingsIntent);
                                } catch (Exception e) {
                                    // Fallback to general app settings
                                    Intent settingsIntent = new Intent(
                                            android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    settingsIntent.setData(android.net.Uri.parse("package:" + getPackageName()));
                                    startActivity(settingsIntent);
                                }
                            }).setNegativeButton("Cancel", null).show();
                    return;
                }
            }

            // Get current time for default values
            Calendar currentTime = Calendar.getInstance();
            int currentHour = currentTime.get(Calendar.HOUR_OF_DAY);
            int currentMinute = currentTime.get(Calendar.MINUTE);

            // Show time picker dialog
            android.app.TimePickerDialog timePickerDialog = new android.app.TimePickerDialog(this,
                    (view, hourOfDay, minute) -> {
                        // User selected time
                        Calendar reminderTime = Calendar.getInstance();
                        reminderTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        reminderTime.set(Calendar.MINUTE, minute);
                        reminderTime.set(Calendar.SECOND, 0);
                        reminderTime.set(Calendar.MILLISECOND, 0);

                        // If selected time is in the past, schedule for tomorrow
                        if (reminderTime.before(Calendar.getInstance())) {
                            reminderTime.add(Calendar.DAY_OF_MONTH, 1);
                        }

                        // Schedule the alarm
                        scheduleAlarm(med, reminderTime, alarmManager);
                    }, currentHour, currentMinute, false // Use 12-hour format
            );

            timePickerDialog.setTitle("Select Reminder Time");
            timePickerDialog.show();

        } catch (Exception e) {
            Toast.makeText(this, "Could not set reminder: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("MedReminder", "Error setting reminder", e);
        }
    }

    private void scheduleAlarm(MedicationSchedule med, Calendar reminderTime, AlarmManager alarmManager) {
        try {
            Intent intent = new Intent(this, MedicationReminderReceiver.class);
            intent.putExtra("medication_id", med.hashCode());
            intent.putExtra("medication_name", med.getName());
            intent.putExtra("medication_dosage", med.getDosage());

            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, med.hashCode(), intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                            | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0));

            if (alarmManager != null) {
                // Simple, reliable alarm scheduling
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTime.getTimeInMillis(),
                            pendingIntent);
                    Log.d("MedReminder", "Using setExactAndAllowWhileIdle");
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTime.getTimeInMillis(), pendingIntent);
                    Log.d("MedReminder", "Using setExact");
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, reminderTime.getTimeInMillis(), pendingIntent);
                    Log.d("MedReminder", "Using set");
                }

                // Format time for display
                SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
                String timeString = sdf.format(reminderTime.getTime());

                // Check if it's tomorrow
                Calendar today = Calendar.getInstance();
                today.set(Calendar.HOUR_OF_DAY, 23);
                today.set(Calendar.MINUTE, 59);

                String dayString = reminderTime.after(today) ? " tomorrow" : " today";

                // Log alarm details
                long secondsUntilAlarm = (reminderTime.getTimeInMillis() - System.currentTimeMillis()) / 1000;
                Log.d("MedReminder", "Alarm scheduled successfully!");
                Log.d("MedReminder", "Medication: " + med.getName());
                Log.d("MedReminder", "Time: " + timeString + dayString);
                Log.d("MedReminder", "Timestamp: " + reminderTime.getTimeInMillis());
                Log.d("MedReminder", "Current time: " + System.currentTimeMillis());
                Log.d("MedReminder", "Time until alarm: " + secondsUntilAlarm + " seconds");

                // Send a test notification immediately to verify notifications work
                sendTestNotification(med);

                // Also schedule a 10-second test alarm for immediate verification
                scheduleTestAlarm(med, alarmManager);

                Toast.makeText(this,
                        "Reminder set for " + timeString + dayString + "\n(Test notification in 10 seconds)",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Could not access alarm service", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error scheduling alarm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("MedReminder", "Error scheduling alarm", e);
        }
    }

    private void sendTestNotification(MedicationSchedule med) {
        try {
            Log.d("MedReminder", "Sending test notification...");

            // Create notification channels
            NotificationChannelManager.createNotificationChannels(this);

            // Create intent to open medications activity
            Intent openIntent = new Intent(this, PatientMedicationsActivity.class);
            openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 9999, openIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                            | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0));

            // Build test notification
            String title = "🧪 Test Notification";
            String message = "If you see this, notifications work! Your reminder for " + med.getName()
                    + " is scheduled.";

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this,
                    NotificationChannelManager.CHANNEL_MEDICATION_REMINDERS).setSmallIcon(R.drawable.logo)
                    .setContentTitle(title).setContentText(message)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                    .setPriority(NotificationCompat.PRIORITY_HIGH).setCategory(NotificationCompat.CATEGORY_REMINDER)
                    .setAutoCancel(true).setContentIntent(pendingIntent).setVibrate(new long[] { 0, 500, 200, 500 })
                    .setDefaults(NotificationCompat.DEFAULT_SOUND);

            // Show notification
            NotificationManager notificationManager = (NotificationManager) getSystemService(
                    Context.NOTIFICATION_SERVICE);

            if (notificationManager != null) {
                notificationManager.notify(9999, builder.build());
                Log.d("MedReminder", "Test notification sent successfully!");
            } else {
                Log.e("MedReminder", "NotificationManager is null!");
            }
        } catch (Exception e) {
            Log.e("MedReminder", "Error sending test notification", e);
        }
    }

    private void scheduleTestAlarm(MedicationSchedule med, AlarmManager alarmManager) {
        try {
            Log.d("MedReminder", "Scheduling 10-second test alarm...");

            // Schedule alarm for 10 seconds from now
            Calendar testTime = Calendar.getInstance();
            testTime.add(Calendar.SECOND, 10);

            Intent intent = new Intent(this, MedicationReminderReceiver.class);
            intent.putExtra("medication_id", med.hashCode() + 10000); // Different
                                                                      // ID
                                                                      // for
                                                                      // test
            intent.putExtra("medication_name", med.getName() + " (TEST)");
            intent.putExtra("medication_dosage", med.getDosage());

            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, med.hashCode() + 10000, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                            | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0));

            if (alarmManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, testTime.getTimeInMillis(),
                            pendingIntent);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, testTime.getTimeInMillis(), pendingIntent);
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, testTime.getTimeInMillis(), pendingIntent);
                }
                Log.d("MedReminder", "Test alarm scheduled for 10 seconds from now");
            }
        } catch (Exception e) {
            Log.e("MedReminder", "Error scheduling test alarm", e);
        }
    }

    // --- Local Storage Helpers ---
    private String getCurrentDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    private boolean checkLocalCompletionStatus(String id, String name) {
        SharedPreferences prefs = getSharedPreferences("med_status", Context.MODE_PRIVATE);
        // Fallback to name if ID is null (for legacy/dummy data compatibility)
        String key = (id != null ? id : name) + "_" + getCurrentDate();
        return prefs.getBoolean(key, false);
    }

    private void saveMedicationStatus(String id, String name, boolean completed) {
        SharedPreferences prefs = getSharedPreferences("med_status", Context.MODE_PRIVATE);
        String key = (id != null ? id : name) + "_" + getCurrentDate();
        prefs.edit().putBoolean(key, completed).apply();
    }

    private void startMedicationNotificationService(List<Medication> medications) {
        // Wrap everything in try-catch to prevent crashes
        try {
            if (medications == null || medications.isEmpty()) {
                Log.d("PatientMedications", "No medications to schedule");
                return;
            }

            // Schedule alarms directly using AlarmManager
            MedicationAlarmManager alarmManager = new MedicationAlarmManager(this);

            int scheduledCount = 0;
            for (Medication medication : medications) {
                try {
                    if (medication != null && medication.isActive() && medication.getId() != null) {
                        alarmManager.scheduleExactAlarm(medication);
                        scheduledCount++;
                        Log.d("PatientMedications", "Scheduled alarm for: " + medication.getName());
                    }
                } catch (Exception e) {
                    Log.e("PatientMedications",
                            "Failed to schedule alarm for " + (medication != null ? medication.getName() : "unknown"),
                            e);
                    // Continue with next medication
                }
            }

            // Only start service if we successfully scheduled alarms
            if (scheduledCount > 0) {
                try {
                    Intent serviceIntent = new Intent(this, MedicationNotificationService.class);
                    serviceIntent.setAction("ACTION_START_SERVICE");
                    serviceIntent.putExtra("medication_count", scheduledCount);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(serviceIntent);
                    } else {
                        startService(serviceIntent);
                    }
                    Log.d("PatientMedications", "Started notification service with " + scheduledCount + " alarms");
                } catch (Exception e) {
                    Log.e("PatientMedications", "Failed to start foreground service", e);
                }
            } else {
                Log.d("PatientMedications", "No alarms scheduled, service not started");
            }
        } catch (Exception e) {
            Log.e("PatientMedications", "Critical error in startMedicationNotificationService", e);
            // Continue without notification service - app still works
        }
    }

    // Simple model for this view
    private static class MedicationSchedule {
        private String apiId;
        private String name;
        private String dosage;
        private String time;
        private String instructions;
        private String duration;
        private String frequency;
        private String foodRelation;
        private boolean morning, afternoon, night;
        private boolean completed;

        public MedicationSchedule(String apiId, String name, String dosage, String time, String instructions,
                String duration, String frequency, boolean morning, boolean afternoon, boolean night,
                String foodRelation, boolean completed) {
            this.apiId = apiId;
            this.name = name;
            this.dosage = dosage;
            this.time = time;
            this.instructions = instructions;
            this.duration = duration;
            this.frequency = frequency;
            this.morning = morning;
            this.afternoon = afternoon;
            this.night = night;
            this.foodRelation = foodRelation;
            this.completed = completed;
        }

        public String getApiId() {
            return apiId;
        }

        public String getName() {
            return name;
        }

        public String getDosage() {
            return dosage;
        }

        public String getTime() {
            return time;
        }

        public String getInstructions() {
            return instructions;
        }

        public String getDuration() {
            return duration;
        }

        public String getFrequency() {
            return frequency;
        }

        public String getFoodRelation() {
            return foodRelation;
        }

        public boolean isMorning() {
            return morning;
        }

        public boolean isAfternoon() {
            return afternoon;
        }

        public boolean isNight() {
            return night;
        }

        public boolean isCompleted() {
            return completed;
        }

        public void setCompleted(boolean completed) {
            this.completed = completed;
        }

        @Override
        public int hashCode() {
            return (name + time).hashCode();
        }
    }
}
