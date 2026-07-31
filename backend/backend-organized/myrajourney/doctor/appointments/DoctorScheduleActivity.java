package com.example.myrajourney.doctor.appointments;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myrajourney.R;
import com.example.myrajourney.core.ui.ThemeManager;
import com.example.myrajourney.core.network.ApiClient;
import com.example.myrajourney.core.network.ApiService;
import com.example.myrajourney.core.session.SessionManager; // ⭐ ADDED
import com.example.myrajourney.data.model.ApiResponse;
import com.example.myrajourney.data.model.Appointment;
import com.example.myrajourney.common.appointments.AddAppointmentActivity;
import com.example.myrajourney.patient.appointments.AppointmentsAdapter;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.Collections; // ⭐ ADDED (sorting)
import java.util.Comparator; // ⭐ ADDED
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DoctorScheduleActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ImageView backButton;
    List<Appointment> appointments, filteredList;
    AppointmentsAdapter adapter;

    String doctorId; // ⭐ ADDED

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_schedule);

        recyclerView = findViewById(R.id.schedule_recycler);
        backButton = findViewById(R.id.back_button);

        doctorId = SessionManager.getInstance(this).getUserId(); // ⭐ ADDED

        backButton.setOnClickListener(v -> finish());

        ImageView addButton = findViewById(R.id.add_button);
        if (addButton != null) {
            addButton.setOnClickListener(v -> {
                Intent i = new Intent(this, AddAppointmentActivity.class);
                i.putExtra("context", "DOCTOR");
                i.putExtra("doctor_id", doctorId); // ⭐ ADDED
                startActivity(i);
            });
        }

        appointments = new ArrayList<>();
        filteredList = new ArrayList<>();

        adapter = new AppointmentsAdapter(this, filteredList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadAppointmentsFromBackend();

        backButton.setOnLongClickListener(v -> {
            startActivity(new Intent(this, AddAppointmentActivity.class));
            Toast.makeText(this, "Open Add Appointment", Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    // ⭐ ADDED — reload schedule when coming back from AddAppointment
    @Override
    protected void onResume() {
        super.onResume();
        loadAppointmentsFromBackend();
    }

    private void loadAppointmentsFromBackend() {

        ApiService apiService = ApiClient.getApiService(this);

        // ⭐ FIXED — get only THIS DOCTOR’S appointments
        Call<ApiResponse<List<Appointment>>> call = apiService.getAppointments(null, Integer.valueOf(doctorId));

        call.enqueue(new Callback<ApiResponse<List<Appointment>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Appointment>>> call,
                    Response<ApiResponse<List<Appointment>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Appointment> apiAppointments = response.body().getData();

                    appointments.clear();

                    if (apiAppointments != null) {

                        // ⭐ FIXED — apply sorting by start_time
                        Collections.sort(apiAppointments, new Comparator<Appointment>() {
                            @Override
                            public int compare(Appointment a1, Appointment a2) {
                                try {
                                    SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                                            Locale.getDefault());
                                    Date d1 = f.parse(a1.getStartTime());
                                    Date d2 = f.parse(a2.getStartTime());
                                    return d1.compareTo(d2);
                                } catch (Exception e) {
                                    return 0;
                                }
                            }
                        });

                        appointments.addAll(apiAppointments);

                        filteredList.clear();
                        filteredList.addAll(appointments);
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    appointments.clear();
                    filteredList.clear();
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Appointment>>> call, Throwable t) {
                appointments.clear();
                filteredList.clear();
                adapter.notifyDataSetChanged();
                Toast.makeText(DoctorScheduleActivity.this, "Failed to load appointments", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatDate(String dateTime) {
        if (dateTime == null || dateTime.isEmpty())
            return "Today";
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(dateTime);
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM d", Locale.getDefault());
            return outputFormat.format(date);
        } catch (Exception e) {
            return "Today";
        }
    }

    private String formatTime(String dateTime) {
        if (dateTime == null || dateTime.isEmpty())
            return "10:00 AM";
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(dateTime);
            SimpleDateFormat outputFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
            return outputFormat.format(date);
        } catch (Exception e) {
            return "10:00 AM";
        }
    }
}
