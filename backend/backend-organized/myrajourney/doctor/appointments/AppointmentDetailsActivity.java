package com.example.myrajourney.doctor.appointments;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myrajourney.R;

public class AppointmentDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_details);

        // Back button
        ImageView back = findViewById(R.id.back_button);
        if (back != null) back.setOnClickListener(v -> finish());

        // Updated fields from XML
        TextView personName = findViewById(R.id.person_name);
        TextView title = findViewById(R.id.title);
        TextView date = findViewById(R.id.date);
        TextView timeSlot = findViewById(R.id.time_slot);
        TextView details = findViewById(R.id.details);
        TextView status = findViewById(R.id.status);

        // Read ALL incoming extras
        String nameVal = getIntent().getStringExtra("person_name");
        String titleVal = getIntent().getStringExtra("title");
        String dateVal = getIntent().getStringExtra("date");
        String timeVal = getIntent().getStringExtra("time_slot");
        String detailsVal = getIntent().getStringExtra("details");
        String statusVal = getIntent().getStringExtra("status");

        // BACKWARD COMPATIBILITY
        // If old sender sends "datetime", split it into date + time
        String oldDateTime = getIntent().getStringExtra("datetime");
        if ((dateVal == null || timeVal == null) && oldDateTime != null) {
            try {
                // Expected format: "Dec 15, 2024 - 10:30 AM"
                String[] parts = oldDateTime.split(" - ");
                if (parts.length == 2) {
                    dateVal = parts[0];
                    timeVal = parts[1];
                }
            } catch (Exception ignored) {}
        }

        // Set values safely
        if (personName != null && nameVal != null) personName.setText(nameVal);
        if (title != null && titleVal != null) title.setText(titleVal);
        if (date != null && dateVal != null) date.setText(dateVal);
        if (timeSlot != null && timeVal != null) timeSlot.setText(timeVal);
        if (details != null && detailsVal != null) details.setText(detailsVal);
        if (status != null && statusVal != null) status.setText(statusVal);
    }
}
