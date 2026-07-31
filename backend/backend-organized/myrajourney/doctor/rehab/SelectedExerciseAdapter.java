package com.example.myrajourney.doctor.rehab;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myrajourney.R;
import com.example.myrajourney.rehab.models.RAExercise;
import java.util.List;

/**
 * Adapter for displaying selected exercises
 */
public class SelectedExerciseAdapter extends RecyclerView.Adapter<SelectedExerciseAdapter.ViewHolder> {
    
    private final List<RAExercise> selectedExercises;
    private final OnExerciseRemoveListener listener;
    
    public interface OnExerciseRemoveListener {
        void onExerciseRemoved(RAExercise exercise);
    }
    
    public SelectedExerciseAdapter(List<RAExercise> selectedExercises, OnExerciseRemoveListener listener) {
        this.selectedExercises = selectedExercises;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_selected_exercise, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RAExercise exercise = selectedExercises.get(position);
        holder.bind(exercise);
    }
    
    @Override
    public int getItemCount() {
        return selectedExercises.size();
    }
    
    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView txtExerciseName;
        private final TextView txtExerciseCategory;
        private final ImageView btnRemove;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtExerciseName = itemView.findViewById(R.id.txtExerciseName);
            txtExerciseCategory = itemView.findViewById(R.id.txtExerciseCategory);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
        
        public void bind(RAExercise exercise) {
            txtExerciseName.setText(exercise.getName());
            txtExerciseCategory.setText(exercise.getCategory().getDisplayName());
            
            btnRemove.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onExerciseRemoved(exercise);
                }
            });
        }
    }
}