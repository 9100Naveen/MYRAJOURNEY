package com.example.myrajourney.patient.reports;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.example.myrajourney.R;
import com.example.myrajourney.core.network.ApiClient;
import com.example.myrajourney.core.network.ApiService;
import com.example.myrajourney.data.model.ApiResponse;
import com.example.myrajourney.data.model.Report;
import com.example.myrajourney.core.session.TokenManager;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * UploadReportActivity
 * - safer imports
 * - robust file reading from URI (either via ParcelFileDescriptor.statSize or stream fallback)
 * - get filename via ContentResolver/OpenableColumns
 */
public class UploadReportActivity extends AppCompatActivity {

    private TextInputEditText reportName;
    private TextView reportDate, uploadText;
    private Button submitButton;

    private Calendar selectedDateTime = Calendar.getInstance();
    private Uri selectedFileUri;

    // ActivityResultLauncher for file picker
    private final ActivityResultLauncher<String[]> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(),
                    uri -> {
                        if (uri != null) {
                            // Persist permission so the URI stays accessible across restarts (optional)
                            final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                            try {
                                getContentResolver().takePersistableUriPermission(uri, takeFlags);
                            } catch (Exception ignored) {}

                            selectedFileUri = uri;
                            String name = getFileNameFromUri(this, uri);
                            uploadText.setText("File selected: " + (name != null ? name : uri.getLastPathSegment()));
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_report);

        reportName = findViewById(R.id.reportName);
        reportDate = findViewById(R.id.reportDate);
        uploadText = findViewById(R.id.uploadText);
        submitButton = findViewById(R.id.submitButton);

        // Date & Time picker
        reportDate.setOnClickListener(v -> showDateTimePicker());

        // Upload area click
        findViewById(R.id.uploadArea).setOnClickListener(v -> openFilePicker());

        // Submit button
        submitButton.setOnClickListener(v -> submitReport());
    }

    private void showDateTimePicker() {
        Calendar now = Calendar.getInstance();

        DatePickerDialog datePicker = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    selectedDateTime.set(year, month, dayOfMonth);

                    TimePickerDialog timePicker = new TimePickerDialog(this,
                            (timeView, hourOfDay, minute) -> {
                                selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                selectedDateTime.set(Calendar.MINUTE, minute);

                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                                reportDate.setText(sdf.format(selectedDateTime.getTime()));
                            },
                            now.get(Calendar.HOUR_OF_DAY),
                            now.get(Calendar.MINUTE),
                            true);
                    timePicker.show();
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH));

        datePicker.show();
    }

    private void openFilePicker() {
        // Allow PDF or images
        filePickerLauncher.launch(new String[]{"application/pdf", "image/*"});
    }

    private void submitReport() {
        String name = reportName.getText().toString().trim();
        String date = reportDate.getText().toString().trim();

        if (name.isEmpty()) {
            reportName.setError("Report name required");
            return;
        }

        if (date.isEmpty()) {
            Toast.makeText(this, "Please select date and time", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedFileUri == null) {
            Toast.makeText(this, "Please select a file", Toast.LENGTH_SHORT).show();
            return;
        }

        uploadReportToBackend(name, date);
    }

    private void uploadReportToBackend(String title, String date) {
        TokenManager tokenManager = TokenManager.getInstance(this);
        String patientId = tokenManager.getUserId();

        if (patientId == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            ContentResolver resolver = getContentResolver();
            String mimeType = resolver.getType(selectedFileUri);
            if (mimeType == null) mimeType = "application/octet-stream";

            // Attempt to read size via ParcelFileDescriptor; fall back to streaming read
            byte[] fileBytes = null;
            long sizeBytes = -1L;
            try (ParcelFileDescriptor pfd = resolver.openFileDescriptor(selectedFileUri, "r")) {
                if (pfd != null) {
                    try {
                        long statSize = pfd.getStatSize();
                        if (statSize > 0) {
                            sizeBytes = statSize;
                            FileInputStream fis = new FileInputStream(pfd.getFileDescriptor());
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            byte[] buffer = new byte[8192];
                            int read;
                            while ((read = fis.read(buffer)) != -1) {
                                baos.write(buffer, 0, read);
                            }
                            fileBytes = baos.toByteArray();
                            fis.close();
                        }
                    } catch (Exception ignored) {
                        // fall through to stream read below
                    }
                }
            } catch (Exception ignored) {
                // ignore here and try stream read
            }

            if (fileBytes == null) {
                // stream fallback
                try (InputStream in = resolver.openInputStream(selectedFileUri);
                     ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[8192];
                    int read;
                    while (in != null && (read = in.read(buffer)) != -1) {
                        baos.write(buffer, 0, read);
                    }
                    fileBytes = baos.toByteArray();
                    sizeBytes = fileBytes.length;
                }
            }

            if (fileBytes == null) {
                Toast.makeText(this, "Could not read selected file", Toast.LENGTH_SHORT).show();
                return;
            }

            String fileName = getFileNameFromUri(this, selectedFileUri);
            if (fileName == null || fileName.isEmpty()) {
                fileName = "report_" + System.currentTimeMillis();
                // ensure extension from mime
                if ("application/pdf".equals(mimeType)) fileName += ".pdf";
            }

            RequestBody patientIdBody = RequestBody.create(MediaType.parse("text/plain"), patientId);
            RequestBody titleBody = RequestBody.create(MediaType.parse("text/plain"), title);
            RequestBody descriptionBody = RequestBody.create(MediaType.parse("text/plain"), "Uploaded on " + date);

            RequestBody fileBody = RequestBody.create(MediaType.parse(mimeType), fileBytes);
            MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", fileName, fileBody);

            ApiService apiService = ApiClient.getApiService(this);
            Call<ApiResponse<Report>> call = apiService.createReport(patientIdBody, titleBody, descriptionBody, filePart);

            submitButton.setEnabled(false);
            submitButton.setText("Uploading...");

            call.enqueue(new Callback<ApiResponse<Report>>() {
                @Override
                public void onResponse(Call<ApiResponse<Report>> call, Response<ApiResponse<Report>> response) {
                    submitButton.setEnabled(true);
                    submitButton.setText("Submit");

                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(UploadReportActivity.this, "Report uploaded successfully! Doctor will be notified.", Toast.LENGTH_LONG).show();
                        setResult(RESULT_OK); // notify callers
                        finish();
                    } else {
                        String errorMsg = "Failed to upload report";
                        if (response.body() != null && response.body().getError() != null) {
                            errorMsg = response.body().getError().getMessage();
                        }
                        Toast.makeText(UploadReportActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Report>> call, Throwable t) {
                    submitButton.setEnabled(true);
                    submitButton.setText("Submit");
                    Toast.makeText(UploadReportActivity.this, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Error reading file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Helper: extract filename from Uri via OpenableColumns if possible
     */
    private static String getFileNameFromUri(Context ctx, Uri uri) {
        if (uri == null) return null;
        ContentResolver cr = ctx.getContentResolver();
        String result = null;
        Cursor cursor = null;
        try {
            cursor = cr.query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx != -1) result = cursor.getString(idx);
            }
        } catch (Exception ignored) {
        } finally {
            if (cursor != null) cursor.close();
        }

        if (result == null) {
            String path = uri.getLastPathSegment();
            if (path != null) result = path;
        }
        return result;
    }
}
