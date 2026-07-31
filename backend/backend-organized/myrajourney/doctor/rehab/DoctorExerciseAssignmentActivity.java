package com.example.myrajourney.doctor.rehab;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myrajourney.R;
import com.example.myrajourney.core.network.ApiClient;
import com.example.myrajourney.core.network.ApiService;
import com.example.myrajourney.rehab.models.ExerciseCategory;
import com.example.myrajourney.rehab.models.RAExercise;
import com.example.myrajourney.rehab.services.ExerciseLibraryService;
import com.example.myrajourney.data.model.User;
import com.example.myrajourney.core.session.SessionManager;
import com.example.myrajourney.rehab.services.ExerciseAssignmentService;
import com.example.myrajourney.rehab.services.impl.ExerciseLibraryServiceImpl;
import com.example.myrajourney.rehab.services.impl.ExerciseAssignmentServiceImpl;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity for doctors to assign RA exercises to patients
 */
public class DoctorExerciseAssignmentActivity extends AppCompatActivity {

    private ImageView btnBack;
    private Spinner spinnerPatients;
    private RecyclerView recyclerExercises;
    private RecyclerView recyclerSelectedExercises;
    private LinearLayout layoutSelectedExercises;
    private EditText editNotes;
    private Button btnAssignExercises;
    private Button btnCancel;

    // Category filter buttons
    private Button btnFilterAll, btnFilterWrist, btnFilterThumb, btnFilterFinger, btnFilterKnee, btnFilterHip;

    private ExerciseLibraryService exerciseLibraryService;
    private ExerciseAssignmentService assignmentService;
    private SessionManager sessionManager;

    private ExerciseAssignmentAdapter exerciseAdapter;
    private SelectedExerciseAdapter selectedExerciseAdapter;

    private List<RAExercise> allExercises;
    private List<RAExercise> filteredExercises;
    private List<RAExercise> selectedExercises;
    private List<User> patients;
    private User selectedPatient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_exercise_assignment);

        initializeServices();
        initializeViews();
        setupRecyclerViews();
        setupCategoryFilters();
        loadData();
        setupListeners();
    }

    private void initializeServices() {
        exerciseLibraryService = new ExerciseLibraryServiceImpl();
        assignmentService = new ExerciseAssignmentServiceImpl(this);
        sessionManager = SessionManager.getInstance(this);
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        spinnerPatients = findViewById(R.id.spinnerPatients);
        recyclerExercises = findViewById(R.id.recyclerExercises);
        recyclerSelectedExercises = findViewById(R.id.recyclerSelectedExercises);
        layoutSelectedExercises = findViewById(R.id.layoutSelectedExercises);
        editNotes = findViewById(R.id.editNotes);
        btnAssignExercises = findViewById(R.id.btnAssignExercises);
        btnCancel = findViewById(R.id.btnCancel);

        // Category filter buttons
        btnFilterAll = findViewById(R.id.btnFilterAll);
        btnFilterWrist = findViewById(R.id.btnFilterWrist);
        btnFilterThumb = findViewById(R.id.btnFilterThumb);
        btnFilterFinger = findViewById(R.id.btnFilterFinger);
        btnFilterKnee = findViewById(R.id.btnFilterKnee);
        btnFilterHip = findViewById(R.id.btnFilterHip);

        // Initialize lists
        allExercises = new ArrayList<>();
        filteredExercises = new ArrayList<>();
        selectedExercises = new ArrayList<>();
        patients = new ArrayList<>();
    }

    private void setupRecyclerViews() {
        // Exercise library recycler view
        exerciseAdapter = new ExerciseAssignmentAdapter(filteredExercises, this::onExerciseSelected);
        recyclerExercises.setLayoutManager(new LinearLayoutManager(this));
        recyclerExercises.setAdapter(exerciseAdapter);

        // Selected exercises recycler view
        selectedExerciseAdapter = new SelectedExerciseAdapter(selectedExercises, this::onExerciseDeselected);
        recyclerSelectedExercises.setLayoutManager(new LinearLayoutManager(this));
        recyclerSelectedExercises.setAdapter(selectedExerciseAdapter);
    }

    private void setupCategoryFilters() {
        btnFilterAll.setOnClickListener(v -> filterExercises(null));
        btnFilterWrist.setOnClickListener(v -> filterExercises(ExerciseCategory.WRIST));
        btnFilterThumb.setOnClickListener(v -> filterExercises(ExerciseCategory.THUMB));
        btnFilterFinger.setOnClickListener(v -> filterExercises(ExerciseCategory.FINGER));
        btnFilterKnee.setOnClickListener(v -> filterExercises(ExerciseCategory.KNEE));
        btnFilterHip.setOnClickListener(v -> filterExercises(ExerciseCategory.HIP));

        // Set initial filter
        updateFilterButtonStates(null);
    }

    private void loadData() {
        // Load exercises
        allExercises = exerciseLibraryService.getAllExercises();

        // Debug: Log the number of exercises loaded
        android.util.Log.d("ExerciseAssignment", "Loaded " + allExercises.size() + " exercises");
        for (RAExercise exercise : allExercises) {
            android.util.Log.d("ExerciseAssignment", "Exercise: " + exercise.getName());
        }

        filteredExercises.clear();
        filteredExercises.addAll(allExercises);
        exerciseAdapter.notifyDataSetChanged();

        // Load patients (mock data for now - should come from API)
        loadPatients();
    }

    private void loadPatients() {
        ApiService apiService = ApiClient.getApiService(this);
        apiService.getAllPatients()
                .enqueue(new retrofit2.Callback<com.example.myrajourney.data.model.ApiResponse<List<User>>>() {
                    @Override
                    public void onResponse(
                            retrofit2.Call<com.example.myrajourney.data.model.ApiResponse<List<User>>> call,
                            retrofit2.Response<com.example.myrajourney.data.model.ApiResponse<List<User>>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            List<User> list = response.body().getData();
                            if (list != null) {
                                patients.clear();
                                for (User u : list) {
                                    if ("PATIENT".equalsIgnoreCase(u.getRole())) {
                                        patients.add(u);
                                    }
                                }

                                List<String> patientNames = new ArrayList<>();
                                patientNames.add("Select a patient...");
                                for (User patient : patients) {
                                    patientNames.add(patient.getName());
                                }

                                ArrayAdapter<String> adapter = new ArrayAdapter<>(DoctorExerciseAssignmentActivity.this,
                                        android.R.layout.simple_spinner_item, patientNames);
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spinnerPatients.setAdapter(adapter);
                            }
                        } else {
                            Toast.makeText(DoctorExerciseAssignmentActivity.this, "Failed to load patients",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            retrofit2.Call<com.example.myrajourney.data.model.ApiResponse<List<User>>> call,
                            Throwable t) {
                        Toast.makeText(DoctorExerciseAssignmentActivity.this, "Network Error", Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnCancel.setOnClickListener(v -> finish());

        btnAssignExercises.setOnClickListener(v -> assignExercises());

        spinnerPatients.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    selectedPatient = patients.get(position - 1);
                    updateAssignButtonState();
                } else {
                    selectedPatient = null;
                    updateAssignButtonState();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedPatient = null;
                updateAssignButtonState();
            }
        });
    }

    private void filterExercises(ExerciseCategory category) {
        filteredExercises.clear();

        if (category == null) {
            filteredExercises.addAll(allExercises);
        } else {
            filteredExercises.addAll(exerciseLibraryService.getExercisesByCategory(category));
        }

        exerciseAdapter.notifyDataSetChanged();
        updateFilterButtonStates(category);
    }

    private void updateFilterButtonStates(ExerciseCategory selectedCategory) {
        // Reset all buttons
        resetFilterButton(btnFilterAll);
        resetFilterButton(btnFilterWrist);
        resetFilterButton(btnFilterThumb);
        resetFilterButton(btnFilterFinger);
        resetFilterButton(btnFilterKnee);
        resetFilterButton(btnFilterHip);

        // Highlight selected button
        if (selectedCategory == null) {
            highlightFilterButton(btnFilterAll);
        } else {
            switch (selectedCategory) {
                case WRIST:
                    highlightFilterButton(btnFilterWrist);
                    break;
                case THUMB:
                    highlightFilterButton(btnFilterThumb);
                    break;
                case FINGER:
                    highlightFilterButton(btnFilterFinger);
                    break;
                case KNEE:
                    highlightFilterButton(btnFilterKnee);
                    break;
                case HIP:
                    highlightFilterButton(btnFilterHip);
                    break;
            }
        }
    }

    private void resetFilterButton(Button button) {
        button.setBackgroundResource(R.drawable.bg_button_outline);
        button.setTextColor(getResources().getColor(R.color.primary, null));
    }

    private void highlightFilterButton(Button button) {
        button.setBackgroundResource(R.drawable.bg_button_primary);
        button.setTextColor(getResources().getColor(R.color.white, null));
    }

    private void onExerciseSelected(RAExercise exercise, boolean isSelected) {
        if (isSelected) {
            if (!selectedExercises.contains(exercise)) {
                selectedExercises.add(exercise);
                selectedExerciseAdapter.notifyItemInserted(selectedExercises.size() - 1);
            }
        } else {
            int index = selectedExercises.indexOf(exercise);
            if (index != -1) {
                selectedExercises.remove(index);
                selectedExerciseAdapter.notifyItemRemoved(index);
            }
        }

        updateSelectedExercisesVisibility();
        updateAssignButtonState();
    }

    private void onExerciseDeselected(RAExercise exercise) {
        int index = selectedExercises.indexOf(exercise);
        if (index != -1) {
            selectedExercises.remove(index);
            selectedExerciseAdapter.notifyItemRemoved(index);

            // Update the checkbox in the main list
            exerciseAdapter.updateExerciseSelection(exercise, false);
        }

        updateSelectedExercisesVisibility();
        updateAssignButtonState();
    }

    private void updateSelectedExercisesVisibility() {
        if (selectedExercises.isEmpty()) {
            layoutSelectedExercises.setVisibility(View.GONE);
        } else {
            layoutSelectedExercises.setVisibility(View.VISIBLE);
        }
    }

    private void updateAssignButtonState() {
        boolean canAssign = selectedPatient != null && !selectedExercises.isEmpty();
        btnAssignExercises.setEnabled(canAssign);
    }

    private void assignExercises() {
        if (selectedPatient == null || selectedExercises.isEmpty()) {
            Toast.makeText(this, "Please select a patient and at least one exercise", Toast.LENGTH_SHORT).show();
            return;
        }

        String doctorId = sessionManager.getCurrentUser().getIdAsString();
        String patientId = selectedPatient.getIdAsString();
        String notes = editNotes.getText().toString().trim();

        List<String> exerciseIds = new ArrayList<>();
        for (RAExercise exercise : selectedExercises) {
            exerciseIds.add(exercise.getId());
        }

        boolean success;
        if (notes.isEmpty()) {
            success = assignmentService.assignExercisesToPatient(doctorId, patientId, exerciseIds);
        } else {
            success = ((ExerciseAssignmentServiceImpl) assignmentService)
                    .assignExercisesToPatientWithNotes(doctorId, patientId, exerciseIds, notes);
        }

        if (success) {
            Toast.makeText(this, "Exercises assigned successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to assign exercises. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}