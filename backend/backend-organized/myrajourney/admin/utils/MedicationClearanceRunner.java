package com.example.myrajourney.admin.utils;

import android.content.Context;
import android.util.Log;

/**
 * Simple runner to clear all medication assignments
 * Can be called from any activity or service
 */
public class MedicationClearanceRunner {
    private static final String TAG = "MedicationClearanceRunner";
    
    /**
     * Execute medication clearance immediately
     */
    public static void executeNow(Context context) {
        Log.i(TAG, "=== STARTING MEDICATION CLEARANCE ===");
        
        // Check current count
        int currentCount = MedicationClearanceUtil.getMedicationAssignmentCount(context);
        Log.i(TAG, "Current medication assignments: " + currentCount);
        
        if (currentCount == 0) {
            Log.i(TAG, "No medication assignments found - database is already clean!");
            return;
        }
        
        if (currentCount < 0) {
            Log.e(TAG, "Error checking medication count - aborting clearance");
            return;
        }
        
        // Clear all medications
        Log.i(TAG, "Clearing " + currentCount + " medication assignments...");
        boolean success = MedicationClearanceUtil.clearAllMedicationAssignments(context);
        
        if (success) {
            Log.i(TAG, "✅ SUCCESS: All medication assignments cleared!");
            
            // Verify clearance
            int finalCount = MedicationClearanceUtil.getMedicationAssignmentCount(context);
            Log.i(TAG, "Final medication count: " + finalCount);
            
            if (finalCount == 0) {
                Log.i(TAG, "✅ VERIFIED: Database is now clean - ready for fresh medication assignments!");
            } else {
                Log.w(TAG, "⚠️ WARNING: " + finalCount + " medication assignments still remain");
            }
        } else {
            Log.e(TAG, "❌ FAILED: Could not clear all medication assignments");
        }
        
        Log.i(TAG, "=== MEDICATION CLEARANCE COMPLETE ===");
    }
    
    /**
     * Execute clearance with callback
     */
    public static void executeWithCallback(Context context, ClearanceCallback callback) {
        new Thread(() -> {
            try {
                executeNow(context);
                if (callback != null) {
                    callback.onComplete(true, "Medication clearance completed successfully");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error during medication clearance", e);
                if (callback != null) {
                    callback.onComplete(false, "Error: " + e.getMessage());
                }
            }
        }).start();
    }
    
    public interface ClearanceCallback {
        void onComplete(boolean success, String message);
    }
}