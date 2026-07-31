package com.example.myrajourney.patient.rehab;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myrajourney.R;
import com.example.myrajourney.rehab.models.RAExercise;
import java.util.List;

/**
 * Adapter for displaying patient's assigned exercises
 */
public class PatientExerciseAdapter extends RecyclerView.Adapter<PatientExerciseAdapter.ViewHolder> {
    
    private final List<RAExercise> exercises;
    private final OnExerciseActionListener listener;
    
    public interface OnExerciseActionListener {
        void onStartExercise(RAExercise exercise);
        void onViewInstructions(RAExercise exercise);
        void onRemoveExercise(RAExercise exercise);
    }
    
    public PatientExerciseAdapter(List<RAExercise> exercises, OnExerciseActionListener listener) {
        this.exercises = exercises;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_patient_exercise, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RAExercise exercise = exercises.get(position);
        holder.bind(exercise);
    }
    
    @Override
    public int getItemCount() {
        return exercises.size();
    }
    
    class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgExerciseIcon;
        private final TextView txtExerciseName;
        private final TextView txtExerciseCategory;
        private final TextView txtExerciseDescription;
        private final TextView txtRaBenefits;
        private final Button btnViewInstructions;
        private final Button btnStartExercise;
        private final ImageView imgCompletionStatus;
        private final LinearLayout layoutLastCompleted;
        private final TextView txtLastCompleted;
        private final ImageView btnRemoveExercise;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgExerciseIcon = itemView.findViewById(R.id.imgExerciseIcon);
            txtExerciseName = itemView.findViewById(R.id.txtExerciseName);
            txtExerciseCategory = itemView.findViewById(R.id.txtExerciseCategory);
            txtExerciseDescription = itemView.findViewById(R.id.txtExerciseDescription);
            txtRaBenefits = itemView.findViewById(R.id.txtRaBenefits);
            btnViewInstructions = itemView.findViewById(R.id.btnViewInstructions);
            btnStartExercise = itemView.findViewById(R.id.btnStartExercise);
            imgCompletionStatus = itemView.findViewById(R.id.imgCompletionStatus);
            layoutLastCompleted = itemView.findViewById(R.id.layoutLastCompleted);
            txtLastCompleted = itemView.findViewById(R.id.txtLastCompleted);
            btnRemoveExercise = itemView.findViewById(R.id.btnRemoveExercise);
        }
        
        public void bind(RAExercise exercise) {
            txtExerciseName.setText(exercise.getName());
            txtExerciseDescription.setText(exercise.getDescription());
            
            // Set category and difficulty
            String categoryText = exercise.getCategory().getDisplayName() + " • Level " + exercise.getDifficultyLevel();
            txtExerciseCategory.setText(categoryText);
            
            // Set exercise icon based on category
            int iconResource;
            switch (exercise.getCategory()) {
                case WRIST:
                    iconResource = R.drawable.wrist;
                    break;
                case THUMB:
                    iconResource = R.drawable.thumb;
                    break;
                case FINGER:
                    iconResource = R.drawable.finger;
                    break;
                case KNEE:
                    iconResource = R.drawable.quad;
                    break;
                case HIP:
                    iconResource = R.drawable.glute;
                    break;
                default:
                    iconResource = R.drawable.ic_rehab;
                    break;
            }
            imgExerciseIcon.setImageResource(iconResource);
            
            // Set RA benefits
            if (exercise.getRaSpecificBenefits() != null && !exercise.getRaSpecificBenefits().isEmpty()) {
                StringBuilder benefits = new StringBuilder();
                for (String benefit : exercise.getRaSpecificBenefits()) {
                    benefits.append("• ").append(benefit).append("\n");
                }
                // Remove last newline
                if (benefits.length() > 0) {
                    benefits.setLength(benefits.length() - 1);
                }
                txtRaBenefits.setText(benefits.toString());
            }
            
            // Set button listeners
            btnViewInstructions.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewInstructions(exercise);
                }
            });
            
            btnStartExercise.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onStartExercise(exercise);
                }
            });
            
            // Set remove button listener
            if (btnRemoveExercise != null) {
                btnRemoveExercise.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onRemoveExercise(exercise);
                    }
                });
                
                // Style the remove button as red delete icon
                btnRemoveExercise.setImageResource(R.drawable.ic_delete);
                btnRemoveExercise.setColorFilter(android.graphics.Color.RED);
                btnRemoveExercise.setVisibility(View.VISIBLE);
            }
            
            // TODO: Set completion status and last completed time based on actual session data
            // For now, hiding these elements
            imgCompletionStatus.setVisibility(View.GONE);
            layoutLastCompleted.setVisibility(View.GONE);
        }
    }
}