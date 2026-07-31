package com.example.myrajourney.patient.appointments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.example.myrajourney.R;
import com.example.myrajourney.data.model.Appointment;

public class AppointmentsAdapter extends RecyclerView.Adapter<AppointmentsAdapter.ViewHolder> {

    private Context context;
    private List<Appointment> appointments;

    public AppointmentsAdapter(Context context, List<Appointment> appointments) {
        this.context = context;
        this.appointments = appointments;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment a = appointments.get(position);

        // Doctor schedule -> show patient name
        if (a.getPatientName() != null) {
            holder.patientName.setText(a.getPatientName());
        } else {
            holder.patientName.setText("Patient");
        }

        // formatted fields
        holder.date.setText(a.getFormattedDate());
        holder.time.setText(a.getFormattedTimeSlot());

        // title / type / reason
        if (a.getAppointmentType() != null) holder.type.setText(a.getAppointmentType());
        else if (a.getReason() != null) holder.type.setText(a.getReason());
        else holder.type.setText(a.getTitle());
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView patientName, time, type, date;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // ✅ FIXED — correct ID from XML
            patientName = itemView.findViewById(R.id.appointment_person);

            time = itemView.findViewById(R.id.appointment_time);
            type = itemView.findViewById(R.id.appointment_type);
            date = itemView.findViewById(R.id.appointment_date);
        }
    }
}
