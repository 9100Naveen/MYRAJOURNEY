package com.example.myrajourney.rehab.services;

import com.example.myrajourney.rehab.models.ExerciseAssignment;
import java.util.List;

/**
 * Service interface for managing exercise assignments between doctors and patients
 */
public interface ExerciseAssignmentService {
    
    /**
     * Assign exercises to a patient
     * @param doctorId The doctor's ID
     * @param patientId The patient's ID
     * @param exerciseIds List of exercise IDs to assign
     * @return true if assignment was successful
     */
    boolean assignExercisesToPatient(String doctorId, String patientId, List<String> exerciseIds);
    
    /**
     * Get all exercise assignments for a patient
     * @param patientId The patient's ID
     * @return List of exercise assignments
     */
    List<ExerciseAssignment> getPatientAssignments(String patientId);
    
    /**
     * Get all exercise assignments made by a doctor
     * @param doctorId The doctor's ID
     * @return List of exercise assignments
     */
    List<ExerciseAssignment> getDoctorAssignments(String doctorId);
    
    /**
     * Update an existing assignment
     * @param assignmentId The assignment ID
     * @param exerciseIds New list of exercise IDs
     * @return true if update was successful
     */
    boolean updateAssignment(String assignmentId, List<String> exerciseIds);
    
    /**
     * Remove an assignment
     * @param assignmentId The assignment ID
     * @return true if removal was successful
     */
    boolean removeAssignment(String assignmentId);
}