package com.example.myrajourney.patient.rehab;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myrajourney.R;
import java.util.List;

public class ExerciseHistoryAdapter extends RecyclerView.Adapter<ExerciseHistoryAdapter.ViewHolder> {

    private Context context;
    private List<ExerciseHistoryActivity.HistoryItem> items;

    public ExerciseHistoryAdapter(Context context, List<ExerciseHistoryActivity.HistoryItem> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_exercise_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExerciseHistoryActivity.HistoryItem item = items.get(position);
        holder.txtExerciseName.setText(item.exerciseName);
        holder.txtScore.setText(String.format("%.1f%%", item.score));
        holder.txtDate.setText(item.date);
        holder.txtReps.setText("Reps: " + item.correctReps + "/" + item.totalReps);

        long minutes = item.duration / 60;
        long seconds = item.duration % 60;
        holder.txtDuration.setText(String.format("Duration: %d:%02d", minutes, seconds));

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ExercisePerformanceReportActivity.class);
            intent.putExtra("report_path", item.filePath);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtExerciseName, txtScore, txtDate, txtReps, txtDuration;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtExerciseName = itemView.findViewById(R.id.txtExerciseName);
            txtScore = itemView.findViewById(R.id.txtScore);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtReps = itemView.findViewById(R.id.txtReps);
            txtDuration = itemView.findViewById(R.id.txtDuration);
        }
    }
}
