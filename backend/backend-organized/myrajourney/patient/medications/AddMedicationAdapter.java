package com.example.myrajourney.patient.medications;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// --- ADDED IMPORTS ---
import com.example.myrajourney.R;
import com.example.myrajourney.data.model.Medication;
// ---------------------

import java.util.List;

public class AddMedicationAdapter extends RecyclerView.Adapter<AddMedicationAdapter.MedViewHolder> {

    private Context context;
    private List<Medication> medList;

    public AddMedicationAdapter(Context context, List<Medication> medList) {
        this.context = context;
        this.medList = medList;
    }

    @NonNull
    @Override
    public MedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_add_medication, parent, false);
        return new MedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedViewHolder holder, int position) {
        Medication med = medList.get(position);

        holder.name.setText(med.getName());
        // Updated string concatenation to be safe if fields are null
        String detailsText = (med.getDosage() != null ? med.getDosage() : "") + ", " +
                (med.getFrequency() != null ? med.getFrequency() : "") + ", " +
                (med.getDuration() != null ? med.getDuration() : "");
        holder.details.setText(detailsText);

        // Handle checkbox state safely
        holder.checkbox.setOnCheckedChangeListener(null); // Remove listener to avoid recycling bugs
        holder.checkbox.setChecked(med.isTakenToday());

        holder.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            med.setTakenToday(isChecked);
        });
    }

    @Override
    public int getItemCount() {
        return medList.size();
    }

    static class MedViewHolder extends RecyclerView.ViewHolder {
        TextView name, details;
        CheckBox checkbox;

        public MedViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.med_name);
            details = itemView.findViewById(R.id.med_details);
            checkbox = itemView.findViewById(R.id.med_checkbox);
        }
    }
}