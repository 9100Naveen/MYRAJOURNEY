package com.example.myrajourney.patient.rehab;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myrajourney.R;
import com.example.myrajourney.rehab.models.ExerciseAssignment;
import com.example.myrajourney.rehab.models.RAExercise;
import com.example.myrajourney.rehab.services.ExerciseAssignmentService;
import com.example.myrajourney.rehab.services.ExerciseLibraryService;
import com.example.myrajourney.rehab.services.impl.ExerciseAssignmentServiceImpl;
import com.example.myrajourney.rehab.services.impl.ExerciseLibraryServiceImpl;
import com.example.myrajourney.core.session.SessionManager;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity for patients to view their assigned RA exercises
 */
public class PatientExerciseListActivity extends AppCompatActivity {
    
    private ImageView btnBack;
    private ImageView btnRefresh;
    private LinearLayout layoutAssignmentInfo;
    private TextView txtDoctorName;
    private TextView txtAssignedDate;
    private LinearLayout layoutNotes;
    private TextView txtNotes;
    private TextView txtTotalExercises;
    private TextView txtCompletedToday;
    private RecyclerView recyclerExercises;
    private LinearLayout layoutNoExercises;
    
    private ExerciseAssignmentService assignmentService;
    private ExerciseLibraryService exerciseLibraryService;
    private SessionManager sessionManager;
    
    private PatientExerciseAdapter exerciseAdapter;
    private List<RAExercise> assignedExercises;
    private List<ExerciseAssignment> assignments;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_exercise_list);
        
        initializeServices();
        initializeViews();
        setupRecyclerView();
        loadAssignedExercises();
        setupListeners();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this activity
        loadAssignedExercises();
    }
    
    private void initializeServices() {
        assignmentService = new ExerciseAssignmentServiceImpl(this);
        exerciseLibraryService = new ExerciseLibraryServiceImpl();
        sessionManager = SessionManager.getInstance(this);
    }
    
    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        btnRefresh = findViewById(R.id.btnRefresh);
        layoutAssignmentInfo = findViewById(R.id.layoutAssignmentInfo);
        txtDoctorName = findViewById(R.id.txtDoctorName);
        txtAssignedDate = findViewById(R.id.txtAssignedDate);
        layoutNotes = findViewById(R.id.layoutNotes);
        txtNotes = findViewById(R.id.txtNotes);
        txtTotalExercises = findViewById(R.id.txtTotalExercises);
        txtCompletedToday = findViewById(R.id.txtCompletedToday);
        recyclerExercises = findViewById(R.id.recyclerExercises);
        layoutNoExercises = findViewById(R.id.layoutNoExercises);
        
        assignedExercises = new ArrayList<>();
        assignments = new ArrayList<>();
    }
    
    private void setupRecyclerView() {
        exerciseAdapter = new PatientExerciseAdapter(assignedExercises, new PatientExerciseAdapter.OnExerciseActionListener() {
            @Override
            public void onStartExercise(RAExercise exercise) {
                startExerciseSession(exercise);
            }
            
            @Override
            public void onViewInstructions(RAExercise exercise) {
                showExerciseInstructions(exercise);
            }
            
            @Override
            public void onRemoveExercise(RAExercise exercise) {
                removeExerciseFromList(exercise);
            }
        });
        
        recyclerExercises.setLayoutManager(new LinearLayoutManager(this));
        recyclerExercises.setAdapter(exerciseAdapter);
    }
    
    private void loadAssignedExercises() {
        try {
            if (sessionManager.getCurrentUser() == null) {
                showNoExercisesState();
                return;
            }
            
            String patientId = sessionManager.getCurrentUser().getIdAsString();
            
            // Get assignments for this patient
            assignments = assignmentService.getPatientAssignments(patientId);
        
            // For demo purposes, if no assignments exist, create some mock assignments
            if (assignments.isEmpty()) {
                // Create mock assignment with first 3 exercises for demo
                List<RAExercise> allExercises = exerciseLibraryService.getAllExercises();
                if (!allExercises.isEmpty()) {
                    // Take first 3 exercises for demo
                    List<String> exerciseIds = new ArrayList<>();
                    for (int i = 0; i < Math.min(3, allExercises.size()); i++) {
                        exerciseIds.add(allExercises.get(i).getId());
                    }
                    
                    // Create mock assignment
                    com.example.myrajourney.rehab.models.ExerciseAssignment mockAssignment = 
                        new com.example.myrajourney.rehab.models.ExerciseAssignment(
                            "mock_001",
                            "doctor_001", 
                            patientId,
                            exerciseIds,
                            java.time.LocalDateTime.now(),
                            "Demo exercises for RA management. Start with gentle movements."
                        );
                    assignments.add(mockAssignment);
                } else {
                    showNoExercisesState();
                    return;
                }
            }
        
        // Get the exercises from assignments
        assignedExercises.clear();
        for (ExerciseAssignment assignment : assignments) {
            for (String exerciseId : assignment.getExerciseIds()) {
                RAExercise exercise = exerciseLibraryService.getExerciseById(exerciseId);
                if (exercise != null) {
                    assignedExercises.add(exercise);
                }
            }
        }
        
        if (assignedExercises.isEmpty()) {
            showNoExercisesState();
        } else {
            showExercisesState();
            updateAssignmentInfo();
            updateStatistics();
        }
        
        exerciseAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
            showNoExercisesState();
        }
    }
    
    private void showNoExercisesState() {
        layoutAssignmentInfo.setVisibility(View.GONE);
        recyclerExercises.setVisibility(View.GONE);
        layoutNoExercises.setVisibility(View.VISIBLE);
    }
    
    private void showExercisesState() {
        layoutAssignmentInfo.setVisibility(View.VISIBLE);
        recyclerExercises.setVisibility(View.VISIBLE);
        layoutNoExercises.setVisibility(View.GONE);
    }
    
    private void updateAssignmentInfo() {
        if (!assignments.isEmpty()) {
            ExerciseAssignment latestAssignment = assignments.get(0);
            
            // TODO: Get actual doctor name from user service
            if (txtDoctorName != null) {
                txtDoctorName.setText("Assigned by Dr. Smith");
            }
            
            // Format assigned date
            if (txtAssignedDate != null && latestAssignment.getAssignedDate() != null) {
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
                    String formattedDate = latestAssignment.getAssignedDate().format(formatter);
                    txtAssignedDate.setText("Assigned on " + formattedDate);
                } catch (Exception e) {
                    txtAssignedDate.setText("Recently assigned");
                }
            }
            
            // Show notes if available
            if (latestAssignment.getNotes() != null && !latestAssignment.getNotes().trim().isEmpty()) {
                if (layoutNotes != null) {
                    layoutNotes.setVisibility(View.VISIBLE);
                }
                if (txtNotes != null) {
                    txtNotes.setText(latestAssignment.getNotes());
                }
            } else {
                if (layoutNotes != null) {
                    layoutNotes.setVisibility(View.GONE);
                }
            }
        }
    }
    
    private void updateStatistics() {
        if (txtTotalExercises != null) {
            txtTotalExercises.setText(String.valueOf(assignedExercises.size()));
        }
        
        // TODO: Get actual completion data from session service
        // For now, showing mock data
        if (txtCompletedToday != null) {
            txtCompletedToday.setText("0");
        }
    }
    
    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        btnRefresh.setOnClickListener(v -> {
            loadAssignedExercises();
        });
        
        findViewById(R.id.btnContactDoctor).setOnClickListener(v -> {
            // TODO: Implement contact doctor functionality
            // This could open messaging or appointment booking
        });
    }
    
    private void startExerciseSession(RAExercise exercise) {
        Intent intent = new Intent(this, ExerciseSessionActivity.class);
        intent.putExtra("exercise_id", exercise.getId());
        intent.putExtra("exercise_name", exercise.getName());
        startActivity(intent);
    }
    
    private void showExerciseInstructions(RAExercise exercise) {
        Intent intent = new Intent(this, ExerciseInstructionsActivity.class);
        intent.putExtra("exercise_id", exercise.getId());
        startActivity(intent);
    }
    
    /**
     * Remove exercise from the assigned list
     */
    private void removeExerciseFromList(RAExercise exercise) {
        // Show confirmation dialog
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Remove Exercise")
                .setMessage("Are you sure you want to remove \"" + exercise.getName() + "\" from your therapy plan?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    // Remove from list
                    assignedExercises.remove(exercise);
                    
                    // Remove from assignments
                    for (ExerciseAssignment assignment : assignments) {
                        assignment.getExerciseIds().remove(exercise.getId());
                    }
                    
                    // Update adapter
                    exerciseAdapter.notifyDataSetChanged();
                    
                    // Update statistics
                    updateStatistics();
                    
                    // Show confirmation
                    android.widget.Toast.makeText(this, 
                            "Exercise removed from your therapy plan", 
                            android.widget.Toast.LENGTH_SHORT).show();
                    
                    // Check if list is now empty
                    if (assignedExercises.isEmpty()) {
                        showNoExercisesState();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}