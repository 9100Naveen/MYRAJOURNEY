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

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.AppViewHolder> {

    private Context context;
    private List<Appointment> appointments;
    private Appointment nextAppointment;

    public AppointmentAdapter(Context context, List<Appointment> appointments) {
        this.context = context;
        this.appointments = appointments;
    }

    public AppointmentAdapter(Context context, List<Appointment> appointments, Appointment nextAppointment) {
        this.context = context;
        this.appointments = appointments;
        this.nextAppointment = nextAppointment;
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_appointment, parent, false);
        return new AppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        Appointment app = appointments.get(position);

        // Show doctor or patient name
        if (app.getDoctorName() != null) {
            holder.person.setText(app.getDoctorName());
        } else if (app.getPatientName() != null) {
            holder.person.setText(app.getPatientName());
        } else {
            holder.person.setText("Appointment");
        }

        holder.date.setText(app.getFormattedDate());
        holder.time.setText(app.getFormattedTimeSlot());

        if (app.getAppointmentType() != null)
            holder.type.setText(app.getAppointmentType());
        else if (app.getReason() != null)
            holder.type.setText(app.getReason());
        else if (app.getTitle() != null)
            holder.type.setText(app.getTitle());
        else
            holder.type.setText("");

        // highlight next appointment
        if (nextAppointment != null &&
                app.getId() != null &&
                app.getId().equals(nextAppointment.getId())) {

            holder.itemView.setBackgroundColor(0xFFE0F7FA);
        } else {
            holder.itemView.setBackgroundColor(0x00000000);
        }
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    static class AppViewHolder extends RecyclerView.ViewHolder {

        TextView person, date, time, type;

        public AppViewHolder(@NonNull View itemView) {
            super(itemView);

            // FIXED: Correct IDs from XML
            person = itemView.findViewById(R.id.appointment_person);
            type   = itemView.findViewById(R.id.appointment_type);
            date   = itemView.findViewById(R.id.appointment_date);
            time   = itemView.findViewById(R.id.appointment_time);
        }
    }
}
