package com.example.myrajourney.rehab.services.impl;

import android.content.Context;
import com.example.myrajourney.rehab.models.ExerciseAssignment;
import com.example.myrajourney.rehab.repository.ExerciseAssignmentRepository;
import com.example.myrajourney.rehab.services.ExerciseAssignmentService;
import java.util.List;

/**
 * Implementation of ExerciseAssignmentService
 */
public class ExerciseAssignmentServiceImpl implements ExerciseAssignmentService {
    
    private final ExerciseAssignmentRepository repository;
    
    public ExerciseAssignmentServiceImpl(Context context) {
        this.repository = ExerciseAssignmentRepository.getInstance(context);
    }
    
    @Override
    public boolean assignExercisesToPatient(String doctorId, String patientId, List<String> exerciseIds) {
        // Validate inputs
        if (doctorId == null || patientId == null || exerciseIds == null || exerciseIds.isEmpty()) {
            return false;
        }
        
        // TODO: Add validation to ensure doctor has permission to assign to this patient
        // This would typically check the doctor-patient relationship in the database
        
        return repository.createAssignment(doctorId, patientId, exerciseIds, null);
    }
    
    @Override
    public List<ExerciseAssignment> getPatientAssignments(String patientId) {
        if (patientId == null) {
            return List.of();
        }
        
        return repository.getPatientAssignments(patientId);
    }
    
    @Override
    public List<ExerciseAssignment> getDoctorAssignments(String doctorId) {
        if (doctorId == null) {
            return List.of();
        }
        
        return repository.getDoctorAssignments(doctorId);
    }
    
    @Override
    public boolean updateAssignment(String assignmentId, List<String> exerciseIds) {
        if (assignmentId == null || exerciseIds == null) {
            return false;
        }
        
        return repository.updateAssignment(assignmentId, exerciseIds);
    }
    
    @Override
    public boolean removeAssignment(String assignmentId) {
        if (assignmentId == null) {
            return false;
        }
        
        return repository.removeAssignment(assignmentId);
    }
    
    /**
     * Additional helper methods
     */
    
    public boolean hasAssignments(String patientId) {
        return repository.hasAssignments(patientId);
    }
    
    public List<String> getAssignedExerciseIds(String patientId) {
        return repository.getAssignedExerciseIds(patientId);
    }
    
    public boolean assignExercisesToPatientWithNotes(String doctorId, String patientId, 
                                                    List<String> exerciseIds, String notes) {
        if (doctorId == null || patientId == null || exerciseIds == null || exerciseIds.isEmpty()) {
            return false;
        }
        
        return repository.createAssignment(doctorId, patientId, exerciseIds, notes);
    }
}