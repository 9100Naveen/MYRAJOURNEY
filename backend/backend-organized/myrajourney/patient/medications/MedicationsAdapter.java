package com.example.myrajourney.patient.medications;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// --- ADDED IMPORTS ---
import com.example.myrajourney.R;
import com.example.myrajourney.data.model.Medication;
// ---------------------

import java.util.List;

public class MedicationsAdapter extends RecyclerView.Adapter<MedicationsAdapter.ViewHolder> {

    private Context context;
    private List<Medication> medicationList;
    private boolean isDoctorView = false;
    private OnMedicationDeleteListener deleteListener;

    public interface OnMedicationDeleteListener {
        void onDelete(Medication medication);
    }

    public void setDoctorView(boolean isDoctorView) {
        this.isDoctorView = isDoctorView;
        notifyDataSetChanged();
    }

    public void setOnDeleteListener(OnMedicationDeleteListener listener) {
        this.deleteListener = listener;
    }

    public MedicationsAdapter(Context context, List<Medication> medicationList) {
        this.context = context;
        this.medicationList = medicationList;
    }

    public void filterList(List<Medication> filteredList) {
        this.medicationList = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_medication, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Medication medication = medicationList.get(position);
        holder.name.setText(medication.getName());

        // Safe checks for null or empty values
        String dosage = (medication.getDosage() != null && !medication.getDosage().trim().isEmpty())
                ? medication.getDosage()
                : "N/A";
        String frequency = (medication.getFrequency() != null && !medication.getFrequency().trim().isEmpty())
                ? medication.getFrequency()
                : "N/A";
        String instructions = (medication.getInstructions() != null && !medication.getInstructions().trim().isEmpty())
                ? medication.getInstructions()
                : "N/A";
        String foodRelation = (medication.getFoodRelation() != null && !medication.getFoodRelation().trim().isEmpty())
                ? medication.getFoodRelation()
                : "N/A";

        holder.dosage.setText(dosage);
        holder.frequency.setText(frequency);
        holder.instructions.setText(instructions);
        holder.foodRelation.setText(foodRelation);

        // Timing formatting
        java.util.List<String> times = new java.util.ArrayList<>();
        if (medication.isMorning())
            times.add("Morning");
        if (medication.isAfternoon())
            times.add("Afternoon");
        if (medication.isNight())
            times.add("Night");

        if (!times.isEmpty()) {
            StringBuilder timeStr = new StringBuilder();
            for (int i = 0; i < times.size(); i++) {
                timeStr.append(times.get(i));
                if (i < times.size() - 1)
                    timeStr.append("/");
            }
            holder.timing.setText(timeStr.toString());
        } else {
            holder.timing.setText("N/A");
        }

        if (isDoctorView) {
            holder.deleteBtn.setVisibility(View.VISIBLE);
            holder.deleteBtn.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDelete(medication);
                }
            });
        } else {
            holder.deleteBtn.setVisibility(View.GONE);
            holder.deleteBtn.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return medicationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, dosage, frequency, instructions, timing, foodRelation;
        android.widget.ImageView deleteBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // These IDs must exist in res/layout/item_medication.xml
            name = itemView.findViewById(R.id.med_name);
            dosage = itemView.findViewById(R.id.med_dosage);
            frequency = itemView.findViewById(R.id.med_frequency);
            instructions = itemView.findViewById(R.id.med_instructions);
            timing = itemView.findViewById(R.id.med_timing);
            foodRelation = itemView.findViewById(R.id.med_food_relation);
            deleteBtn = itemView.findViewById(R.id.btn_delete_med);
        }
    }
}