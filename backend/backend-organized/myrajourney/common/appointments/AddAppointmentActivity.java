package com.example.myrajourney.common.appointments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myrajourney.R;
import com.example.myrajourney.core.network.ApiClient;
import com.example.myrajourney.core.network.ApiService;
import com.example.myrajourney.core.session.TokenManager;
import com.example.myrajourney.data.model.ApiResponse;
import com.example.myrajourney.data.model.Appointment;
import com.example.myrajourney.data.model.AppointmentRequest;
import com.example.myrajourney.data.model.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddAppointmentActivity extends AppCompatActivity {

    private Spinner spinnerPatient, spinnerDoctor, spinnerTimeSlot;
    private Button btnPickDate, btnAddAppointment;
    private TextView tvSelectedDate;
    private String selectedDate = "";

    // Backing data lists (keep models to get IDs)
    private List<User> patients = new ArrayList<>();
    private List<User> doctors = new ArrayList<>();

    private final List<String> defaultTimeSlots = Arrays.asList(
            "10:00 AM - 10:30 AM",
            "10:30 AM - 11:00 AM",
            "11:00 AM - 11:30 AM",
            "11:30 AM - 12:00 PM",
            "02:00 PM - 02:30 PM",
            "02:30 PM - 03:00 PM",
            "03:00 PM - 03:30 PM"


    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_appointment);

        spinnerPatient = findViewById(R.id.spinner_patient);
        spinnerDoctor = findViewById(R.id.spinner_doctor);
        spinnerTimeSlot = findViewById(R.id.spinner_timeslot);
        btnPickDate = findViewById(R.id.btn_pick_date);
        btnAddAppointment = findViewById(R.id.btn_add_appointment);
        tvSelectedDate = findViewById(R.id.tv_selected_date);

        // load timeslots into spinner
        ArrayAdapter<String> tsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, defaultTimeSlots);
        tsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTimeSlot.setAdapter(tsAdapter);

        // Date picker
        btnPickDate.setOnClickListener(v -> showDatePicker());

        // Load doctors and patients from backend
        loadDoctors();
        loadPatients();

        btnAddAppointment.setOnClickListener(v -> {
            if (selectedDate.isEmpty()) {
                Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
                return;
            }
            String timeSlot = (String) spinnerTimeSlot.getSelectedItem();
            if (timeSlot == null || timeSlot.trim().isEmpty()) {
                Toast.makeText(this, "Select a time slot", Toast.LENGTH_SHORT).show();
                return;
            }

            // Determine selected IDs based on role and spinners
            TokenManager tokenManager = TokenManager.getInstance(this);
            String role = tokenManager.getUserRole();
            String currentUserId = tokenManager.getUserId();

            String patientId = null;
            String doctorId = null;

            // If current user is PATIENT -> patientId = currentUserId
            if ("PATIENT".equalsIgnoreCase(role)) {
                patientId = currentUserId;
                // pick selected doctor from spinner
                int docPos = spinnerDoctor.getSelectedItemPosition();
                if (docPos >= 0 && docPos < doctors.size()) {
                    doctorId = String.valueOf(doctors.get(docPos).getId());
                }
            } else if ("DOCTOR".equalsIgnoreCase(role)) {
                doctorId = currentUserId;
                // pick selected patient from spinner
                int patPos = spinnerPatient.getSelectedItemPosition();
                if (patPos >= 0 && patPos < patients.size()) {
                    patientId = String.valueOf(patients.get(patPos).getId());
                }
            } else {
                // ADMIN or other -> both must be chosen
                int patPos = spinnerPatient.getSelectedItemPosition();
                int docPos = spinnerDoctor.getSelectedItemPosition();
                if (patPos >= 0 && patPos < patients.size()) {
                    patientId = String.valueOf(patients.get(patPos).getId());
                }
                if (docPos >= 0 && docPos < doctors.size()) {
                    doctorId = String.valueOf(doctors.get(docPos).getId());
                }
            }

            if (patientId == null || doctorId == null) {
                Toast.makeText(this, "Please select both doctor and patient", Toast.LENGTH_LONG).show();
                return;
            }

            // Compose start & end datetime strings
            String[] times = timeSlot.split(" - ");
            String startTime = parseDateTime(selectedDate, times[0].trim());
            String endTime = times.length > 1 ? parseDateTime(selectedDate, times[1].trim()) : startTime;

            // Title & reason simple defaults (could be from an input field)
            String title = "Consultation";
            String reason = "General Checkup";

            // Build request
            AppointmentRequest req = new AppointmentRequest(
                    patientId,
                    doctorId,
                    title,
                    reason,
                    startTime,
                    endTime
            );

            createAppointment(req);
        });
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, monthOfYear, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, monthOfYear, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                    selectedDate = sdf.format(selected.getTime());
                    tvSelectedDate.setText(selectedDate);
                }, mYear, mMonth, mDay);

        // Disable past dates
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void loadDoctors() {
        ApiService api = ApiClient.getApiService(this);
        Call<ApiResponse<List<User>>> call = api.getDoctors();
        call.enqueue(new Callback<ApiResponse<List<User>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<User>>> call, Response<ApiResponse<List<User>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<User> list = response.body().getData();
                    if (list != null) {
                        doctors.clear();
                        doctors.addAll(list);
                        List<String> names = new ArrayList<>();
                        for (User u : doctors) names.add(u.getName() != null ? u.getName() : "Dr. " + u.getId());
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(AddAppointmentActivity.this,
                                android.R.layout.simple_spinner_item, names);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerDoctor.setAdapter(adapter);
                    }
                } else {
                    // keep empty adapter
                    spinnerDoctor.setAdapter(new ArrayAdapter<>(AddAppointmentActivity.this, android.R.layout.simple_spinner_item, new ArrayList<>()));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<User>>> call, Throwable t) {
                spinnerDoctor.setAdapter(new ArrayAdapter<>(AddAppointmentActivity.this, android.R.layout.simple_spinner_item, new ArrayList<>()));
                Toast.makeText(AddAppointmentActivity.this, "Failed to load doctors", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPatients() {
        ApiService api = ApiClient.getApiService(this);
        Call<ApiResponse<List<User>>> call = api.getAllPatients();
        call.enqueue(new Callback<ApiResponse<List<User>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<User>>> call, Response<ApiResponse<List<User>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<User> list = response.body().getData();
                    if (list != null) {
                        patients.clear();
                        // Only keep ROLE == PATIENT
                        for (User u : list) {
                            if ("PATIENT".equalsIgnoreCase(u.getRole())) patients.add(u);
                        }
                        List<String> names = new ArrayList<>();
                        for (User u : patients) names.add(u.getName() != null ? u.getName() : "Patient " + u.getId());
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(AddAppointmentActivity.this,
                                android.R.layout.simple_spinner_item, names);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerPatient.setAdapter(adapter);
                    }
                } else {
                    spinnerPatient.setAdapter(new ArrayAdapter<>(AddAppointmentActivity.this, android.R.layout.simple_spinner_item, new ArrayList<>()));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<User>>> call, Throwable t) {
                spinnerPatient.setAdapter(new ArrayAdapter<>(AddAppointmentActivity.this, android.R.layout.simple_spinner_item, new ArrayList<>()));
                Toast.makeText(AddAppointmentActivity.this, "Failed to load patients", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createAppointment(AppointmentRequest req) {
        ApiService api = ApiClient.getApiService(this);
        Call<ApiResponse<Appointment>> call = api.createAppointment(req);
        btnAddAppointment.setEnabled(false);
        call.enqueue(new Callback<ApiResponse<Appointment>>() {
            @Override
            public void onResponse(Call<ApiResponse<Appointment>> call, Response<ApiResponse<Appointment>> response) {
                btnAddAppointment.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(AddAppointmentActivity.this, "Appointment created successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String err = "Failed to create appointment";
                    if (response.body() != null && response.body().getError() != null) {
                        err = response.body().getError().getMessage();
                    }
                    Toast.makeText(AddAppointmentActivity.this, err, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Appointment>> call, Throwable t) {
                btnAddAppointment.setEnabled(true);
                Toast.makeText(AddAppointmentActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private String parseDateTime(String dateStr, String timeStr) {
        try {
            // dateStr: dd-MM-yyyy
            // timeStr: h:mm a
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            Date date = dateFormat.parse(dateStr);

            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
            Date time = timeFormat.parse(timeStr);

            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            Calendar timeCal = Calendar.getInstance();
            timeCal.setTime(time);
            cal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
            cal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
            cal.set(Calendar.SECOND, 0);

            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            return outputFormat.format(cal.getTime());
        } catch (Exception e) {
            // fallback: now
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            return format.format(new Date());
        }
    }
}
