package com.example.myrajourney.rehab.models;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Model class representing exercise assignments from doctor to patient
 */
public class ExerciseAssignment {
    private String id;
    private String doctorId;
    private String patientId;
    private List<String> exerciseIds;
    private LocalDateTime assignedDate;
    private String notes;

    public ExerciseAssignment() {}

    public ExerciseAssignment(String id, String doctorId, String patientId, 
                             List<String> exerciseIds, LocalDateTime assignedDate, String notes) {
        this.id = id;
        this.doctorId = doctorId;
        this.patientId = patientId;
        this.exerciseIds = exerciseIds;
        this.assignedDate = assignedDate;
        this.notes = notes;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public List<String> getExerciseIds() { return exerciseIds; }
    public void setExerciseIds(List<String> exerciseIds) { this.exerciseIds = exerciseIds; }

    public LocalDateTime getAssignedDate() { return assignedDate; }
    public void setAssignedDate(LocalDateTime assignedDate) { this.assignedDate = assignedDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}