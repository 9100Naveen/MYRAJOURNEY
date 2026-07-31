package com.example.myrajourney.admin.utils;

import android.content.Context;
import android.util.Log;

import com.example.myrajourney.core.network.ApiClient;
import com.example.myrajourney.core.network.ApiService;
import com.example.myrajourney.data.model.ApiResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Utility class for clearing all medication assignments
 */
public class MedicationClearanceUtil {
    private static final String TAG = "MedicationClearanceUtil";
    
    /**
     * Clear all medication assignments for all patients
     */
    public static boolean clearAllMedicationAssignments(Context context) {
        try {
            ApiService apiService = ApiClient.getApiService(context);
            
            Log.i(TAG, "Starting to clear all medication assignments...");
            
            // Method 1: Try using the admin endpoint to clear all at once
            Call<ApiResponse<Void>> clearAllCall = apiService.clearAllPatientMedications();
            Response<ApiResponse<Void>> clearAllResponse = clearAllCall.execute();
            
            if (clearAllResponse.isSuccessful() && clearAllResponse.body() != null && clearAllResponse.body().isSuccess()) {
                Log.i(TAG, "Successfully cleared all medication assignments using admin endpoint");
                return true;
            }
            
            Log.w(TAG, "Admin endpoint not available, falling back to individual deletion...");
            
            // Method 2: Fallback - Get all medications and delete them individually
            Call<ApiResponse<List<Map<String, Object>>>> getAllCall = apiService.getAllPatientMedications();
            Response<ApiResponse<List<Map<String, Object>>>> getAllResponse = getAllCall.execute();
            
            if (getAllResponse.isSuccessful() && getAllResponse.body() != null && getAllResponse.body().isSuccess()) {
                List<Map<String, Object>> medications = getAllResponse.body().getData();
                
                if (medications == null || medications.isEmpty()) {
                    Log.i(TAG, "No medication assignments found to clear");
                    return true;
                }
                
                Log.i(TAG, "Found " + medications.size() + " medication assignments to clear");
                
                int deletedCount = 0;
                int failedCount = 0;
                
                for (Map<String, Object> medication : medications) {
                    String medicationId = (String) medication.get("id");
                    if (medicationId != null) {
                        try {
                            Call<ApiResponse<Void>> deleteCall = apiService.deleteMedication(medicationId);
                            Response<ApiResponse<Void>> deleteResponse = deleteCall.execute();
                            
                            if (deleteResponse.isSuccessful() && deleteResponse.body() != null && deleteResponse.body().isSuccess()) {
                                deletedCount++;
                                Log.d(TAG, "Deleted medication assignment: " + medicationId);
                            } else {
                                failedCount++;
                                Log.w(TAG, "Failed to delete medication assignment: " + medicationId);
                            }
                        } catch (Exception e) {
                            failedCount++;
                            Log.e(TAG, "Error deleting medication assignment: " + medicationId, e);
                        }
                    }
                }
                
                Log.i(TAG, "Medication clearance completed. Deleted: " + deletedCount + ", Failed: " + failedCount);
                return failedCount == 0;
            }
            
            Log.e(TAG, "Failed to get medication assignments for clearing");
            return false;
            
        } catch (Exception e) {
            Log.e(TAG, "Error clearing medication assignments", e);
            return false;
        }
    }
    
    /**
     * Get count of current medication assignments
     */
    public static int getMedicationAssignmentCount(Context context) {
        try {
            ApiService apiService = ApiClient.getApiService(context);
            
            Call<ApiResponse<List<Map<String, Object>>>> getAllCall = apiService.getAllPatientMedications();
            Response<ApiResponse<List<Map<String, Object>>>> getAllResponse = getAllCall.execute();
            
            if (getAllResponse.isSuccessful() && getAllResponse.body() != null && getAllResponse.body().isSuccess()) {
                List<Map<String, Object>> medications = getAllResponse.body().getData();
                return medications != null ? medications.size() : 0;
            }
            
            return -1; // Error occurred
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting medication assignment count", e);
            return -1;
        }
    }
    
    /**
     * Clear medication assignments for a specific patient
     */
    public static boolean clearPatientMedications(Context context, int patientId) {
        try {
            ApiService apiService = ApiClient.getApiService(context);
            
            Log.i(TAG, "Clearing medications for patient: " + patientId);
            
            // Get patient medications
            Call<ApiResponse<List<Map<String, Object>>>> getCall = apiService.getPatientMedicationsRaw(patientId);
            Response<ApiResponse<List<Map<String, Object>>>> getResponse = getCall.execute();
            
            if (getResponse.isSuccessful() && getResponse.body() != null && getResponse.body().isSuccess()) {
                List<Map<String, Object>> medications = getResponse.body().getData();
                
                if (medications == null || medications.isEmpty()) {
                    Log.i(TAG, "No medications found for patient: " + patientId);
                    return true;
                }
                
                Log.i(TAG, "Found " + medications.size() + " medications for patient: " + patientId);
                
                int deletedCount = 0;
                for (Map<String, Object> medication : medications) {
                    String medicationId = (String) medication.get("id");
                    if (medicationId != null) {
                        try {
                            Call<ApiResponse<Void>> deleteCall = apiService.deleteMedication(medicationId);
                            Response<ApiResponse<Void>> deleteResponse = deleteCall.execute();
                            
                            if (deleteResponse.isSuccessful() && deleteResponse.body() != null && deleteResponse.body().isSuccess()) {
                                deletedCount++;
                                Log.d(TAG, "Deleted medication: " + medicationId + " for patient: " + patientId);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error deleting medication: " + medicationId, e);
                        }
                    }
                }
                
                Log.i(TAG, "Cleared " + deletedCount + " medications for patient: " + patientId);
                return deletedCount == medications.size();
            }
            
            return false;
            
        } catch (Exception e) {
            Log.e(TAG, "Error clearing patient medications", e);
            return false;
        }
    }
}