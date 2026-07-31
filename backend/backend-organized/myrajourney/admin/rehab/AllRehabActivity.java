package com.example.myrajourney.admin.rehab;

import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// --- ADDED IMPORTS ---
import com.example.myrajourney.R;
import com.example.myrajourney.core.ui.ThemeManager;
import com.example.myrajourney.data.model.Rehab;
// Importing the adapter from the patient package
import com.example.myrajourney.patient.rehab.RehabAdapter;
// ---------------------

import java.util.ArrayList;
import java.util.List;

public class AllRehabActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    RehabAdapter adapter;
    List<Rehab> rehabList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply Theme
        ThemeManager.applyTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_rehab);

        recyclerView = findViewById(R.id.all_rehab_recycler);

        // Initialize rehab list
        rehabList = new ArrayList<>();
        // Using the constructor: Rehab(id, name, description, reps, frequency,
        // videoUrl, thumbnailUrl)
        rehabList.add(new Rehab("ex_005", "Fist Squeeze", "Squeeze soft ball", "10 reps", "Daily",
                "exercise_videos/ex_005_finger_flexion.mp4",
                "https://img.youtube.com/vi/5qny4scQqHc/0.jpg"));
        rehabList.add(new Rehab("ex_006", "Finger Spread", "Spread fingers apart", "5 reps", "Daily",
                "exercise_videos/ex_006_finger_extension.mp4",
                "https://img.youtube.com/vi/DRr4qzxCSqY/0.jpg"));
        rehabList.add(new Rehab("ex_001", "Wrist Flex", "Flex wrist upward", "10 reps", "Daily",
                "exercise_videos/ex_001_wrist_flexion.mp4",
                "https://img.youtube.com/vi/NXbtJ6qCdbs/0.jpg"));

        // Setup Adapter
        // Ensure RehabAdapter constructor accepts (Context,
        // List<com.example.myrajourney.data.model.Rehab>)
        adapter = new RehabAdapter(this, rehabList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

}