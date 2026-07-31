package com.example.myrajourney.doctor.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myrajourney.R;
import com.example.myrajourney.data.model.SymptomHistory;
import java.util.List;

public class SymptomHistoryAdapter extends RecyclerView.Adapter<SymptomHistoryAdapter.SymptomViewHolder> {

    private List<SymptomHistory> symptoms;
    private Context context;

    public SymptomHistoryAdapter(Context context, List<SymptomHistory> symptoms) {
        this.context = context;
        this.symptoms = symptoms;
    }

    @NonNull
    @Override
    public SymptomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_symptom_history, parent, false);
        return new SymptomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SymptomViewHolder holder, int position) {
        SymptomHistory symptom = symptoms.get(position);
        
        // Set date
        holder.dateText.setText(symptom.getFormattedDate());
        
        // Set pain level with color coding
        holder.painText.setText(symptom.getPainLevelText());
        setPainLevelColor(holder.painText, symptom.getPainLevel());
        
        // Set stiffness level
        holder.stiffnessText.setText(symptom.getStiffnessLevelText());
        
        // Set fatigue level
        holder.fatigueText.setText(symptom.getFatigueLevelText());
        
        // Set joint count
        holder.jointCountText.setText(symptom.getJointCountText());
        
        // Set notes (show/hide based on content)
        if (symptom.getNotes() != null && !symptom.getNotes().trim().isEmpty()) {
            holder.notesText.setText(symptom.getNotes());
            holder.notesText.setVisibility(View.VISIBLE);
        } else {
            holder.notesText.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return symptoms != null ? symptoms.size() : 0;
    }

    public void updateSymptoms(List<SymptomHistory> newSymptoms) {
        this.symptoms = newSymptoms;
        notifyDataSetChanged();
    }

    private void setPainLevelColor(TextView textView, int painLevel) {
        int color;
        if (painLevel == 0) {
            color = context.getResources().getColor(R.color.green, null); // No pain - green
        } else if (painLevel <= 3) {
            color = context.getResources().getColor(R.color.yellow, null); // Mild - yellow
        } else if (painLevel <= 6) {
            color = context.getResources().getColor(R.color.orange, null); // Moderate - orange
        } else {
            color = context.getResources().getColor(R.color.red, null); // Severe - red
        }
        textView.setTextColor(color);
    }

    static class SymptomViewHolder extends RecyclerView.ViewHolder {
        TextView dateText;
        TextView painText;
        TextView stiffnessText;
        TextView fatigueText;
        TextView jointCountText;
        TextView notesText;

        public SymptomViewHolder(@NonNull View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.symptomDate);
            painText = itemView.findViewById(R.id.symptomPain);
            stiffnessText = itemView.findViewById(R.id.symptomStiffness);
            fatigueText = itemView.findViewById(R.id.symptomFatigue);
            jointCountText = itemView.findViewById(R.id.symptomJointCount);
            notesText = itemView.findViewById(R.id.symptomNotes);
        }
    }
}