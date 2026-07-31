package com.example.myrajourney.patient.rehab;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myrajourney.R;
import org.json.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExerciseHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerHistory;
    private TextView txtEmpty;
    private ExerciseHistoryAdapter adapter;
    private List<HistoryItem> historyItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_history);

        recyclerHistory = findViewById(R.id.recyclerHistory);
        txtEmpty = findViewById(R.id.txtEmpty);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        recyclerHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ExerciseHistoryAdapter(this, historyItems);
        recyclerHistory.setAdapter(adapter);

        loadHistory();
    }

    private void loadHistory() {
        historyItems.clear();
        File dir = new File(getExternalFilesDir(null), "reports");
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
            if (files != null) {
                for (File file : files) {
                    HistoryItem item = parseReport(file);
                    if (item != null) {
                        historyItems.add(item);
                    }
                }
            }
        }

        if (historyItems.isEmpty()) {
            txtEmpty.setVisibility(View.VISIBLE);
        } else {
            txtEmpty.setVisibility(View.GONE);
            // Sort by date descending (newest first)
            Collections.sort(historyItems, (a, b) -> b.date.compareTo(a.date));
            adapter.notifyDataSetChanged();
        }
    }

    private HistoryItem parseReport(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            fis.close();
            String jsonStr = new String(data, StandardCharsets.UTF_8);
            JSONObject json = new JSONObject(jsonStr);

            HistoryItem item = new HistoryItem();
            item.filePath = file.getAbsolutePath();
            item.exerciseName = json.optString("exercise_name", "Exercise");
            item.score = json.optDouble("performance_score", 0);
            item.correctReps = json.optInt("correct_reps", 0);
            item.totalReps = json.optInt("total_reps", 0);
            item.date = json.optString("timestamp", "");
            item.duration = json.optLong("duration_seconds", 0);
            return item;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class HistoryItem {
        public String filePath;
        public String exerciseName;
        public double score;
        public int correctReps;
        public int totalReps;
        public String date;
        public long duration;
    }
}
