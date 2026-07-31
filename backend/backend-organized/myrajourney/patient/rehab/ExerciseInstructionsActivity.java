package com.example.myrajourney.patient.rehab;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myrajourney.R;
import com.example.myrajourney.rehab.models.RAExercise;
import com.example.myrajourney.rehab.services.ExerciseLibraryService;
import com.example.myrajourney.rehab.services.impl.ExerciseLibraryServiceImpl;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity to display detailed exercise instructions
 */
public class ExerciseInstructionsActivity extends AppCompatActivity {
    
    private ImageView btnBack;
    private TextView txtExerciseName;
    private TextView txtExerciseDescription;
    private TextView txtExerciseCategory;
    private RecyclerView recyclerInstructions;
    private RecyclerView recyclerBenefits;
    
    private ExerciseLibraryService exerciseLibraryService;
    private RAExercise exercise;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_instructions);
        
        initializeServices();
        initializeViews();
        loadExerciseData();
        setupListeners();
    }
    
    private void initializeServices() {
        exerciseLibraryService = new ExerciseLibraryServiceImpl();
    }
    
    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        txtExerciseName = findViewById(R.id.txtExerciseName);
        txtExerciseDescription = findViewById(R.id.txtExerciseDescription);
        txtExerciseCategory = findViewById(R.id.txtExerciseCategory);
        recyclerInstructions = findViewById(R.id.recyclerInstructions);
        recyclerBenefits = findViewById(R.id.recyclerBenefits);
    }
    
    private void loadExerciseData() {
        String exerciseId = getIntent().getStringExtra("exercise_id");
        if (exerciseId != null) {
            exercise = exerciseLibraryService.getExerciseById(exerciseId);
            if (exercise != null) {
                displayExerciseInfo();
            }
        }
    }
    
    private void displayExerciseInfo() {
        txtExerciseName.setText(exercise.getName());
        txtExerciseDescription.setText(exercise.getDescription());
        txtExerciseCategory.setText(exercise.getCategory().getDisplayName() + " • Level " + exercise.getDifficultyLevel());
        
        // Setup instructions list
        if (exercise.getInstructions() != null) {
            InstructionAdapter instructionAdapter = new InstructionAdapter(exercise.getInstructions());
            recyclerInstructions.setLayoutManager(new LinearLayoutManager(this));
            recyclerInstructions.setAdapter(instructionAdapter);
        }
        
        // Setup benefits list
        if (exercise.getRaSpecificBenefits() != null) {
            BenefitAdapter benefitAdapter = new BenefitAdapter(exercise.getRaSpecificBenefits());
            recyclerBenefits.setLayoutManager(new LinearLayoutManager(this));
            recyclerBenefits.setAdapter(benefitAdapter);
        }
    }
    
    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
    }
    
    // Simple adapter for instructions
    private static class InstructionAdapter extends RecyclerView.Adapter<InstructionAdapter.ViewHolder> {
        private final List<String> instructions;
        
        public InstructionAdapter(List<String> instructions) {
            this.instructions = instructions != null ? instructions : new ArrayList<>();
        }
        
        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            android.view.View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            String instruction = instructions.get(position);
            holder.textView.setText((position + 1) + ". " + instruction);
        }
        
        @Override
        public int getItemCount() {
            return instructions.size();
        }
        
        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;
            
            ViewHolder(android.view.View itemView) {
                super(itemView);
                textView = itemView.findViewById(android.R.id.text1);
            }
        }
    }
    
    // Simple adapter for benefits
    private static class BenefitAdapter extends RecyclerView.Adapter<BenefitAdapter.ViewHolder> {
        private final List<String> benefits;
        
        public BenefitAdapter(List<String> benefits) {
            this.benefits = benefits != null ? benefits : new ArrayList<>();
        }
        
        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            android.view.View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            String benefit = benefits.get(position);
            holder.textView.setText("• " + benefit);
        }
        
        @Override
        public int getItemCount() {
            return benefits.size();
        }
        
        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;
            
            ViewHolder(android.view.View itemView) {
                super(itemView);
                textView = itemView.findViewById(android.R.id.text1);
            }
        }
    }
}