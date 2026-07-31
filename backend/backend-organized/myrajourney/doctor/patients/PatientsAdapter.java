package com.example.myrajourney.doctor.patients;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myrajourney.R;
import com.example.myrajourney.data.model.Patient;

import java.util.List;

public class PatientsAdapter extends RecyclerView.Adapter<PatientsAdapter.PatientViewHolder> {

    private Context context;
    private List<Patient> patientList;

    public PatientsAdapter(Context context, List<Patient> patientList) {
        this.context = context;
        this.patientList = patientList;
    }

    @NonNull
    @Override
    public PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_patient, parent, false);
        return new PatientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PatientViewHolder holder, int position) {
        Patient patient = patientList.get(position);

        String name = (patient.getName() != null) ? patient.getName() : "Unknown";
        holder.name.setText(name);

        // ✅ FIXED AGE — always show the actual value coming from API
        String age = (patient.getAge() > 0) 
                ? String.valueOf(patient.getAge())
                : "N/A";

        // Build the details string
        StringBuilder details = new StringBuilder();
        details.append("Age: ").append(age);

        if (patient.getEmail() != null) {
            details.append(" | ").append(patient.getEmail());
        }

        holder.details.setText(details.toString());
        holder.image.setImageResource(R.drawable.ic_person_default);

        // PASS AGE CORRECTLY TO NEXT SCREEN
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PatientDetailsActivity.class);
            intent.putExtra("patient_id", patient.getId());
            intent.putExtra("patient_name", name);
            intent.putExtra("patient_age", age);   // ← ALWAYS CORRECT NOW
            intent.putExtra("patient_email", patient.getEmail());
            intent.putExtra("patient_image", R.drawable.ic_person_default);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return patientList.size();
    }

    static class PatientViewHolder extends RecyclerView.ViewHolder {
        TextView name, details;
        ImageView image;

        public PatientViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.patient_name);
            details = itemView.findViewById(R.id.patient_age_condition);
            image = itemView.findViewById(R.id.patient_image);
        }
    }
}
