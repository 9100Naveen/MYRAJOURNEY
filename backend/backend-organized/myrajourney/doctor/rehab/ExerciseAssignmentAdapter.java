package com.example.myrajourney.doctor.rehab;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myrajourney.R;
import com.example.myrajourney.rehab.models.RAExercise;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter for displaying exercises in the assignment interface
 */
public class ExerciseAssignmentAdapter extends RecyclerView.Adapter<ExerciseAssignmentAdapter.ViewHolder> {
    
    private final List<RAExercise> exercises;
    private final OnExerciseSelectionListener listener;
    private final Map<String, Boolean> selectionStates;
    
    public interface OnExerciseSelectionListener {
        void onExerciseSelected(RAExercise exercise, boolean isSelected);
    }
    
    public ExerciseAssignmentAdapter(List<RAExercise> exercises, OnExerciseSelectionListener listener) {
        this.exercises = exercises;
        this.listener = listener;
        this.selectionStates = new HashMap<>();
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise_assignment, parent, false);
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
    
    public void updateExerciseSelection(RAExercise exercise, boolean isSelected) {
        selectionStates.put(exercise.getId(), isSelected);
        notifyDataSetChanged();
    }
    
    class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgExerciseIcon;
        private final TextView txtExerciseName;
        private final TextView txtExerciseCategory;
        private final TextView txtExerciseDescription;
        private final TextView txtDifficultyLevel;
        private final CheckBox checkboxSelect;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgExerciseIcon = itemView.findViewById(R.id.imgExerciseIcon);
            txtExerciseName = itemView.findViewById(R.id.txtExerciseName);
            txtExerciseCategory = itemView.findViewById(R.id.txtExerciseCategory);
            txtExerciseDescription = itemView.findViewById(R.id.txtExerciseDescription);
            txtDifficultyLevel = itemView.findViewById(R.id.txtDifficultyLevel);
            checkboxSelect = itemView.findViewById(R.id.checkboxSelect);
        }
        
        public void bind(RAExercise exercise) {
            txtExerciseName.setText(exercise.getName());
            txtExerciseCategory.setText(exercise.getCategory().getDisplayName());
            txtExerciseDescription.setText(exercise.getDescription());
            
            // Set difficulty level
            String difficultyText;
            int difficultyColor;
            switch (exercise.getDifficultyLevel()) {
                case 1:
                    difficultyText = "Beginner";
                    difficultyColor = R.color.success;
                    break;
                case 2:
                    difficultyText = "Intermediate";
                    difficultyColor = R.color.warning;
                    break;
                case 3:
                    difficultyText = "Advanced";
                    difficultyColor = R.color.error;
                    break;
                default:
                    difficultyText = "Unknown";
                    difficultyColor = R.color.text_secondary;
                    break;
            }
            txtDifficultyLevel.setText(difficultyText);
            txtDifficultyLevel.setTextColor(itemView.getContext().getResources().getColor(difficultyColor, null));
            
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
                    iconResource = R.drawable.quad; // Using quad as knee representation
                    break;
                case HIP:
                    iconResource = R.drawable.glute; // Using glute as hip representation
                    break;
                default:
                    iconResource = R.drawable.ic_rehab;
                    break;
            }
            imgExerciseIcon.setImageResource(iconResource);
            
            // Set checkbox state
            Boolean isSelected = selectionStates.get(exercise.getId());
            checkboxSelect.setChecked(isSelected != null && isSelected);
            
            // Set checkbox listener
            checkboxSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
                selectionStates.put(exercise.getId(), isChecked);
                if (listener != null) {
                    listener.onExerciseSelected(exercise, isChecked);
                }
            });
            
            // Set item click listener
            itemView.setOnClickListener(v -> {
                boolean newState = !checkboxSelect.isChecked();
                checkboxSelect.setChecked(newState);
            });
        }
    }
}