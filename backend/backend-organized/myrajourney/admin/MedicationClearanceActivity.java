package com.example.myrajourney.admin;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myrajourney.R;
import com.example.myrajourney.admin.utils.MedicationClearanceUtil;

/**
 * Admin activity for clearing all medication assignments
 */
public class MedicationClearanceActivity extends AppCompatActivity {
    private static final String TAG = "MedicationClearance";
    
    private Button btnClearAll;
    private Button btnCheckCount;
    private Button btnBack;
    private TextView tvStatus;
    private TextView tvCount;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medication_clearance);
        
        initializeViews();
        setupListeners();
        checkCurrentCount();
    }
    
    private void initializeViews() {
        btnClearAll = findViewById(R.id.btnClearAll);
        btnCheckCount = findViewById(R.id.btnCheckCount);
        btnBack = findViewById(R.id.btnBack);
        tvStatus = findViewById(R.id.tvStatus);
        tvCount = findViewById(R.id.tvCount);
    }
    
    private void setupListeners() {
        btnClearAll.setOnClickListener(v -> showClearConfirmation());
        btnCheckCount.setOnClickListener(v -> checkCurrentCount());
        btnBack.setOnClickListener(v -> finish());
    }
    
    private void showClearConfirmation() {
        new AlertDialog.Builder(this)
            .setTitle("Clear All Medication Assignments")
            .setMessage("⚠️ WARNING: This will permanently delete ALL medication assignments for ALL patients.\n\nThis action cannot be undone. Are you sure you want to continue?")
            .setPositiveButton("Yes, Clear All", (dialog, which) -> clearAllMedications())
            .setNegativeButton("Cancel", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }
    
    private void clearAllMedications() {
        btnClearAll.setEnabled(false);
        tvStatus.setText("🔄 Clearing all medication assignments...");
        
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                return MedicationClearanceUtil.clearAllMedicationAssignments(MedicationClearanceActivity.this);
            }
            
            @Override
            protected void onPostExecute(Boolean success) {
                btnClearAll.setEnabled(true);
                
                if (success) {
                    tvStatus.setText("✅ Successfully cleared all medication assignments!");
                    Toast.makeText(MedicationClearanceActivity.this, "All medication assignments cleared!", Toast.LENGTH_LONG).show();
                    Log.i(TAG, "All medication assignments cleared successfully");
                } else {
                    tvStatus.setText("❌ Failed to clear some or all medication assignments");
                    Toast.makeText(MedicationClearanceActivity.this, "Failed to clear all assignments", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Failed to clear medication assignments");
                }
                
                // Refresh count
                checkCurrentCount();
            }
        }.execute();
    }
    
    private void checkCurrentCount() {
        btnCheckCount.setEnabled(false);
        tvCount.setText("🔄 Checking...");
        
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... voids) {
                return MedicationClearanceUtil.getMedicationAssignmentCount(MedicationClearanceActivity.this);
            }
            
            @Override
            protected void onPostExecute(Integer count) {
                btnCheckCount.setEnabled(true);
                
                if (count >= 0) {
                    tvCount.setText("📊 Current medication assignments: " + count);
                    
                    if (count == 0) {
                        tvStatus.setText("✅ No medication assignments found - database is clean!");
                    } else {
                        tvStatus.setText("📋 Found " + count + " medication assignments");
                    }
                } else {
                    tvCount.setText("❌ Error checking count");
                    tvStatus.setText("❌ Failed to check medication assignment count");
                }
            }
        }.execute();
    }
}