package com.example.myrajourney.rehab.repository;

import android.content.Context;
import com.example.myrajourney.rehab.models.ExerciseAssignment;
import com.example.myrajourney.core.network.ApiClient;
import com.example.myrajourney.core.network.ApiService;
import com.example.myrajourney.data.model.ApiResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Repository for managing exercise assignments
 */
public class ExerciseAssignmentRepository {
    
    private static ExerciseAssignmentRepository instance;
    private final Context context;
    private final Gson gson;
    
    // Local cache for offline support
    private List<ExerciseAssignment> assignmentCache;
    
    private ExerciseAssignmentRepository(Context context) {
        this.context = context;
        this.gson = new Gson();
        this.assignmentCache = new ArrayList<>();
    }
    
    public static synchronized ExerciseAssignmentRepository getInstance(Context context) {
        if (instance == null) {
            instance = new ExerciseAssignmentRepository(context);
        }
        return instance;
    }
    
    /**
     * Create a new exercise assignment
     */
    public boolean createAssignment(String doctorId, String patientId, List<String> exerciseIds, String notes) {
        try {
            String assignmentId = UUID.randomUUID().toString();
            
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("id", assignmentId);
            requestData.put("doctor_id", doctorId);
            requestData.put("patient_id", patientId);
            requestData.put("exercise_ids", exerciseIds);
            requestData.put("notes", notes);
            requestData.put("assigned_date", LocalDateTime.now().toString());
            
            retrofit2.Call<ApiResponse<Map<String, Object>>> call = ApiClient.getApiService(context).createExerciseAssignment(requestData);
            retrofit2.Response<ApiResponse<Map<String, Object>>> response = call.execute();
            
            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                // Add to local cache
                ExerciseAssignment assignment = new ExerciseAssignment(
                    assignmentId, doctorId, patientId, exerciseIds, LocalDateTime.now(), notes
                );
                assignmentCache.add(assignment);
                return true;
            }
            
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get assignments for a specific patient
     */
    public List<ExerciseAssignment> getPatientAssignments(String patientId) {
        try {
            retrofit2.Call<ApiResponse<List<Map<String, Object>>>> call = ApiClient.getApiService(context).getPatientExerciseAssignments(patientId);
            retrofit2.Response<ApiResponse<List<Map<String, Object>>>> response = call.execute();
            
            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                List<Map<String, Object>> data = response.body().getData();
                List<ExerciseAssignment> assignments = new ArrayList<>();
                
                for (Map<String, Object> item : data) {
                    // Convert map to ExerciseAssignment object
                    String id = (String) item.get("id");
                    String doctorId = (String) item.get("doctor_id");
                    String patId = (String) item.get("patient_id");
                    @SuppressWarnings("unchecked")
                    List<String> exerciseIds = (List<String>) item.get("exercise_ids");
                    String notes = (String) item.get("notes");
                    
                    ExerciseAssignment assignment = new ExerciseAssignment(id, doctorId, patId, exerciseIds, LocalDateTime.now(), notes);
                    assignments.add(assignment);
                }
                
                // Update cache
                assignmentCache.removeIf(a -> a.getPatientId().equals(patientId));
                assignmentCache.addAll(assignments);
                
                return assignments;
            }
            
            // Return cached data if API fails
            return assignmentCache.stream()
                    .filter(a -> a.getPatientId().equals(patientId))
                    .collect(java.util.stream.Collectors.toList());
                    
        } catch (Exception e) {
            e.printStackTrace();
            // Return cached data on error
            return assignmentCache.stream()
                    .filter(a -> a.getPatientId().equals(patientId))
                    .collect(java.util.stream.Collectors.toList());
        }
    }
    
    /**
     * Get assignments made by a specific doctor
     */
    public List<ExerciseAssignment> getDoctorAssignments(String doctorId) {
        try {
            retrofit2.Call<ApiResponse<List<Map<String, Object>>>> call = ApiClient.getApiService(context).getDoctorExerciseAssignments(doctorId);
            retrofit2.Response<ApiResponse<List<Map<String, Object>>>> response = call.execute();
            
            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                List<Map<String, Object>> data = response.body().getData();
                List<ExerciseAssignment> assignments = new ArrayList<>();
                
                for (Map<String, Object> item : data) {
                    // Convert map to ExerciseAssignment object
                    String id = (String) item.get("id");
                    String docId = (String) item.get("doctor_id");
                    String patientId = (String) item.get("patient_id");
                    @SuppressWarnings("unchecked")
                    List<String> exerciseIds = (List<String>) item.get("exercise_ids");
                    String notes = (String) item.get("notes");
                    
                    ExerciseAssignment assignment = new ExerciseAssignment(id, docId, patientId, exerciseIds, LocalDateTime.now(), notes);
                    assignments.add(assignment);
                }
                
                // Update cache
                assignmentCache.removeIf(a -> a.getDoctorId().equals(doctorId));
                assignmentCache.addAll(assignments);
                
                return assignments;
            }
            
            // Return cached data if API fails
            return assignmentCache.stream()
                    .filter(a -> a.getDoctorId().equals(doctorId))
                    .collect(java.util.stream.Collectors.toList());
                    
        } catch (Exception e) {
            e.printStackTrace();
            // Return cached data on error
            return assignmentCache.stream()
                    .filter(a -> a.getDoctorId().equals(doctorId))
                    .collect(java.util.stream.Collectors.toList());
        }
    }
    
    /**
     * Update an existing assignment
     */
    public boolean updateAssignment(String assignmentId, List<String> exerciseIds) {
        try {
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("exercise_ids", exerciseIds);
            
            retrofit2.Call<ApiResponse<Map<String, Object>>> call = ApiClient.getApiService(context).updateExerciseAssignment(assignmentId, requestData);
            retrofit2.Response<ApiResponse<Map<String, Object>>> response = call.execute();
            
            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                // Update local cache
                for (ExerciseAssignment assignment : assignmentCache) {
                    if (assignment.getId().equals(assignmentId)) {
                        assignment.setExerciseIds(exerciseIds);
                        break;
                    }
                }
                return true;
            }
            
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Remove an assignment
     */
    public boolean removeAssignment(String assignmentId) {
        try {
            retrofit2.Call<ApiResponse<Void>> call = ApiClient.getApiService(context).deleteExerciseAssignment(assignmentId);
            retrofit2.Response<ApiResponse<Void>> response = call.execute();
            
            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                // Remove from local cache
                assignmentCache.removeIf(a -> a.getId().equals(assignmentId));
                return true;
            }
            
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Check if patient has any assignments
     */
    public boolean hasAssignments(String patientId) {
        List<ExerciseAssignment> assignments = getPatientAssignments(patientId);
        return !assignments.isEmpty();
    }
    
    /**
     * Get assigned exercise IDs for a patient
     */
    public List<String> getAssignedExerciseIds(String patientId) {
        List<ExerciseAssignment> assignments = getPatientAssignments(patientId);
        List<String> exerciseIds = new ArrayList<>();
        
        for (ExerciseAssignment assignment : assignments) {
            exerciseIds.addAll(assignment.getExerciseIds());
        }
        
        return exerciseIds;
    }
}