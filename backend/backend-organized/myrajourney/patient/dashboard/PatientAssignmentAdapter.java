package com.example.myrajourney.patient.dashboard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// --- CRITICAL IMPORT FIX ---
import com.example.myrajourney.R;
// ---------------------------

import com.example.myrajourney.data.model.Doctor;
import com.example.myrajourney.data.model.User;

import java.util.ArrayList;
import java.util.List;

public class PatientAssignmentAdapter extends RecyclerView.Adapter<PatientAssignmentAdapter.ViewHolder> {

    private List<User> patients;
    private List<Doctor> doctors;
    private OnAssignListener listener;

    public interface OnAssignListener {
        void onAssign(int patientId, Integer doctorId);
    }

    public PatientAssignmentAdapter(List<User> patients, List<Doctor> doctors, OnAssignListener listener) {
        this.patients = patients;
        this.doctors = doctors;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_patient_assignment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User patient = patients.get(position);
        holder.bind(patient);
    }

    @Override
    public int getItemCount() {
        return patients.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView patientName, patientEmail, currentDoctor;
        Spinner doctorSpinner;
        Button assignButton;

        // Use Integer since doctor ID can be null or int
        private Integer selectedDoctorId = null;

        ViewHolder(View itemView) {
            super(itemView);
            patientName = itemView.findViewById(R.id.patientName);
            patientEmail = itemView.findViewById(R.id.patientEmail);
            currentDoctor = itemView.findViewById(R.id.currentDoctor);
            doctorSpinner = itemView.findViewById(R.id.doctorSpinner);
            assignButton = itemView.findViewById(R.id.assignButton);
        }

        void bind(User patient) {
            patientName.setText(patient.getName() != null ? patient.getName() : "Patient #" + patient.getId());
            patientEmail.setText(patient.getEmail());

            // Show current doctor if assigned
            String currentDoctorText = "Not assigned";
            if (patient.getAssignedDoctorId() != null && patient.getAssignedDoctorId() > 0) {
                for (Doctor doc : doctors) {
                    // Compare IDs (int vs Integer) safely
                    if (doc.getId() == patient.getAssignedDoctorId()) {
                        currentDoctorText = "Assigned to: " + doc.getName();
                        break;
                    }
                }
            }
            currentDoctor.setText(currentDoctorText);

            // Setup doctor spinner
            List<String> doctorNames = new ArrayList<>();
            doctorNames.add("Select Doctor");
            for (Doctor doctor : doctors) {
                String name = doctor.getName() != null ? doctor.getName() : "Doctor #" + doctor.getId();
                if (doctor.getSpecialization() != null && !doctor.getSpecialization().isEmpty()) {
                    name += " (" + doctor.getSpecialization() + ")";
                }
                doctorNames.add(name);
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(itemView.getContext(), android.R.layout.simple_spinner_item, doctorNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            doctorSpinner.setAdapter(adapter);

            // Set current selection if patient is assigned
            if (patient.getAssignedDoctorId() != null && patient.getAssignedDoctorId() > 0) {
                for (int i = 0; i < doctors.size(); i++) {
                    if (doctors.get(i).getId() == patient.getAssignedDoctorId()) {
                        doctorSpinner.setSelection(i + 1); // +1 because of "Select Doctor" at position 0
                        break;
                    }
                }
            }

            doctorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position == 0) {
                        selectedDoctorId = null;
                        assignButton.setEnabled(false);
                    } else {
                        // Get ID from doctor list (offset by 1 due to "Select Doctor" header)
                        selectedDoctorId = doctors.get(position - 1).getId();
                        assignButton.setEnabled(true);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    selectedDoctorId = null;
                    assignButton.setEnabled(false);
                }
            });

            assignButton.setOnClickListener(v -> {
                if (selectedDoctorId != null) {
                    listener.onAssign(patient.getId(), selectedDoctorId);
                }
            });
        }
    }
}