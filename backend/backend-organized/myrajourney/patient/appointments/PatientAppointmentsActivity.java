package com.example.myrajourney.patient.appointments;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myrajourney.R;
import com.example.myrajourney.core.network.ApiClient;
import com.example.myrajourney.core.network.ApiService;
import com.example.myrajourney.core.session.SessionManager;
import com.example.myrajourney.data.model.ApiResponse;
import com.example.myrajourney.data.model.Appointment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PatientAppointmentsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    AppointmentAdapter adapter;
    String patientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_appointments);

        recyclerView = findViewById(R.id.recycler_appointments);

        patientId = SessionManager.getInstance(this).getUserId();

        if (patientId == null || patientId.trim().isEmpty()) {
            Toast.makeText(this, "Invalid patient session", Toast.LENGTH_LONG).show();
            showEmptyList();
            return;
        }

        loadAppointmentsFromAPI();
    }

    private void loadAppointmentsFromAPI() {

        ApiService apiService = ApiClient.getApiService(this);

        // ✔ FIXED — Use the correct patient-only API
        Call<ApiResponse<List<Appointment>>> call =
                apiService.getPatientAppointments(Integer.parseInt(patientId));

        call.enqueue(new Callback<ApiResponse<List<Appointment>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Appointment>>> call,
                                   Response<ApiResponse<List<Appointment>>> response) {

                if (!response.isSuccessful() || response.body() == null) {
                    showEmptyList();
                    Toast.makeText(PatientAppointmentsActivity.this,
                            "API Error", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!response.body().isSuccess()) {
                    showEmptyList();
                    Toast.makeText(PatientAppointmentsActivity.this,
                            "No appointments found", Toast.LENGTH_SHORT).show();
                    return;
                }

                List<Appointment> list = response.body().getData();

                if (list == null || list.isEmpty()) {
                    showEmptyList();
                    return;
                }

                // Sort by start_time safely
                Collections.sort(list, new Comparator<Appointment>() {
                    @Override
                    public int compare(Appointment a1, Appointment a2) {
                        try {
                            SimpleDateFormat f = new SimpleDateFormat(
                                    "yyyy-MM-dd HH:mm:ss", Locale.getDefault()
                            );
                            return f.parse(a1.getStartTime())
                                    .compareTo(f.parse(a2.getStartTime()));
                        } catch (Exception e) {
                            return 0;
                        }
                    }
                });

                Appointment next = getNextAppointment(list);

                adapter = new AppointmentAdapter(
                        PatientAppointmentsActivity.this,
                        list,
                        next
                );

                recyclerView.setLayoutManager(
                        new LinearLayoutManager(PatientAppointmentsActivity.this)
                );
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Appointment>>> call, Throwable t) {
                showEmptyList();
                Toast.makeText(PatientAppointmentsActivity.this,
                        "Network Error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEmptyList() {
        adapter = new AppointmentAdapter(
                PatientAppointmentsActivity.this,
                new ArrayList<>(),
                null
        );
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private Appointment getNextAppointment(List<Appointment> list) {
        if (list == null || list.isEmpty()) return null;

        long now = System.currentTimeMillis();
        long closest = Long.MAX_VALUE;
        Appointment next = null;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()
        );

        for (Appointment appt : list) {
            try {
                long time = sdf.parse(appt.getStartTime()).getTime();
                long diff = time - now;

                if (diff > 0 && diff < closest) {
                    closest = diff;
                    next = appt;
                }

            } catch (Exception ignored) {}
        }

        return next;
    }
}
