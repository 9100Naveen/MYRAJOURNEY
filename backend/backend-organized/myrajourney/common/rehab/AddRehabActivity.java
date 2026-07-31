package com.example.myrajourney.common.rehab;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myrajourney.R;
import com.example.myrajourney.core.ui.ThemeManager;
import com.example.myrajourney.data.model.Rehab;
import com.example.myrajourney.rehab.models.RAExercise;
import com.example.myrajourney.rehab.services.ExerciseLibraryService;
import com.example.myrajourney.rehab.services.impl.ExerciseLibraryServiceImpl;

import java.util.ArrayList;
import java.util.List;

public class AddRehabActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AddRehabAdapter adapter;

    private List<Rehab> rehabList; // FULL LIST
    private List<Rehab> filteredList; // FILTERED LIST

    private EditText searchBar;
    private Button doneButton;

    private ExerciseLibraryService exerciseLibraryService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_rehab);

        recyclerView = findViewById(R.id.add_rehab_recycler);
        searchBar = findViewById(R.id.search_bar);
        doneButton = findViewById(R.id.done_button);

        rehabList = new ArrayList<>();
        filteredList = new ArrayList<>();

        // Initialize exercise library service
        exerciseLibraryService = new ExerciseLibraryServiceImpl();

        // Load RA-specific exercises from the new exercise library
        loadRAExercises();

        adapter = new AddRehabAdapter(this, rehabList, filteredList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }
        });

        doneButton.setOnClickListener(v -> {
            ArrayList<Rehab> selected = new ArrayList<>();
            for (Rehab r : rehabList) {
                if (r.isSelected())
                    selected.add(r);
            }

            Intent resultIntent = new Intent();
            resultIntent.putParcelableArrayListExtra("selected_rehab", selected);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    private void filter(String text) {
        filteredList.clear();

        if (text.trim().isEmpty()) {
            filteredList.addAll(rehabList);
        } else {
            for (Rehab rehab : rehabList) {
                if (rehab.getName().toLowerCase().contains(text.toLowerCase())) {
                    filteredList.add(rehab);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    /**
     * Load RA-specific exercises from the new exercise library
     */
    private void loadRAExercises() {
        List<RAExercise> raExercises = exerciseLibraryService.getAllExercises();

        for (RAExercise raExercise : raExercises) {
            // Convert RAExercise to Rehab model for compatibility
            Rehab rehabExercise = new Rehab(
                    raExercise.getName(),
                    raExercise.getDescription(),
                    "10-15 reps", // Default reps
                    "Daily", // Default frequency
                    raExercise.getVideoUrl() != null ? raExercise.getVideoUrl()
                            : "https://www.youtube.com/watch?v=NXbtJ6qCdbs",
                    generateThumbnailUrl(raExercise.getVideoUrl()) // Generate thumbnail from video URL
            );

            rehabList.add(rehabExercise);
        }

        // Initially filteredList == full list
        filteredList.addAll(rehabList);

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * Generate YouTube thumbnail URL from video URL
     */
    private String generateThumbnailUrl(String videoUrl) {
        if (videoUrl != null && videoUrl.contains("youtube.com/watch?v=")) {
            String videoId = videoUrl.substring(videoUrl.indexOf("v=") + 2);
            if (videoId.contains("&")) {
                videoId = videoId.substring(0, videoId.indexOf("&"));
            }
            return "https://img.youtube.com/vi/" + videoId + "/0.jpg";
        }
        return "";
    }
}
