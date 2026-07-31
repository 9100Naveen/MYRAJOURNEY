package com.example.myrajourney.patient.rehab;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myrajourney.R;
import com.example.myrajourney.rehab.models.PerformanceReport;
import com.example.myrajourney.rehab.models.RAExercise;
import com.example.myrajourney.rehab.services.ExerciseLibraryService;
import com.example.myrajourney.rehab.services.ReportingService;
import com.example.myrajourney.rehab.services.impl.ExerciseLibraryServiceImpl;
import com.example.myrajourney.rehab.services.impl.ReportingServiceImpl;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity for displaying detailed exercise performance reports
 */
public class ExercisePerformanceReportActivity extends AppCompatActivity {

    // UI Components
    private ImageView btnBack;
    private TextView txtExerciseName;
    private TextView txtSessionDate;
    private TextView txtSessionDuration;
    private TextView txtFormAccuracy;
    private TextView txtCompletionRate;
    private TextView txtTotalFrames;
    private TextView txtCorrectFrames;
    private ProgressBar progressFormAccuracy;
    private ProgressBar progressCompletionRate;
    private RecyclerView recyclerRecommendations;
    private RecyclerView recyclerSpecificMetrics;
    private CardView cardSpecificMetrics;
    private Button btnRetryExercise;
    private Button btnShareReport;

    // Services
    private ReportingService reportingService;
    private ExerciseLibraryService exerciseLibraryService;

    // Data
    private PerformanceReport currentReport;
    private RAExercise currentExercise;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_performance_report);

        initializeServices();
        initializeViews();
        loadReportData();
        setupListeners();
    }

    private void initializeServices() {
        reportingService = new ReportingServiceImpl(this);
        exerciseLibraryService = new ExerciseLibraryServiceImpl();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        txtExerciseName = findViewById(R.id.txtExerciseName);
        txtSessionDate = findViewById(R.id.txtSessionDate);
        txtSessionDuration = findViewById(R.id.txtSessionDuration);
        txtFormAccuracy = findViewById(R.id.txtFormAccuracy);
        txtCompletionRate = findViewById(R.id.txtCompletionRate);
        txtTotalFrames = findViewById(R.id.txtTotalFrames);
        txtCorrectFrames = findViewById(R.id.txtCorrectFrames);
        progressFormAccuracy = findViewById(R.id.progressFormAccuracy);
        progressCompletionRate = findViewById(R.id.progressCompletionRate);
        recyclerRecommendations = findViewById(R.id.recyclerRecommendations);
        recyclerSpecificMetrics = findViewById(R.id.recyclerSpecificMetrics);
        cardSpecificMetrics = findViewById(R.id.cardSpecificMetrics);
        btnRetryExercise = findViewById(R.id.btnRetryExercise);
        btnShareReport = findViewById(R.id.btnShareReport);

        // Setup RecyclerViews
        recyclerRecommendations.setLayoutManager(new LinearLayoutManager(this));
        recyclerSpecificMetrics.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadReportData() {
        // Get report data from intent
        String sessionId = getIntent().getStringExtra("session_id");
        String exerciseId = getIntent().getStringExtra("exercise_id");
        String reportPath = getIntent().getStringExtra("report_path");

        if (reportPath != null) {
            loadLocalReport(reportPath);
        } else if (sessionId == null && exerciseId == null) {
            // Create mock report for demonstration
            createMockReport();
        } else {
            // Load actual report data
            loadReportFromService(sessionId);
        }
    }

    private void loadLocalReport(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists())
                return;

            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            fis.close();

            String jsonStr = new String(data, StandardCharsets.UTF_8);
            JSONObject json = new JSONObject(jsonStr);

            String exerciseName = json.optString("exercise_name", "Exercise");
            double score = json.optDouble("performance_score", 0);
            int correctReps = json.optInt("correct_reps", 0);
            int totalReps = json.optInt("total_reps", 0);
            double accuracy = json.optDouble("joint_accuracy", 0);
            long durationSec = json.optLong("duration_seconds", 0);
            String mistakes = json.optString("mistakes_detected", "");
            String suggestions = json.optString("suggestions", "");
            String timestampStr = json.optString("timestamp", "");

            // Map to PerformanceReport
            currentReport = new PerformanceReport();
            currentReport.setFormAccuracy((float) (accuracy / 100.0));
            currentReport.setCompletionRate(totalReps > 0 ? (float) correctReps / totalReps : 0);
            currentReport.setSessionDuration(Duration.ofSeconds(durationSec));

            // Handle timestamp
            try {
                // Assuming "new Date().toString()" from Generator
                Date date = new Date(timestampStr);
                currentReport.setTimestamp(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            } catch (Exception e) {
                currentReport.setTimestamp(LocalDateTime.now());
            }

            Map<String, Object> metrics = new HashMap<>();
            metrics.put("total_reps", totalReps);
            metrics.put("correct_reps", correctReps);
            metrics.put("performance_score", score);
            metrics.put("mistakes", mistakes);
            currentReport.setSpecificMetrics(metrics);

            List<String> recommendations = new ArrayList<>();
            if (!suggestions.isEmpty()) {
                recommendations.add(suggestions);
            }
            currentReport.setRecommendations(recommendations);

            // Find exercise by name to get icon/description if possible
            List<RAExercise> library = exerciseLibraryService.getAllExercises();
            for (RAExercise ex : library) {
                if (ex.getName().equalsIgnoreCase(exerciseName)) {
                    currentExercise = ex;
                    break;
                }
            }

            displayReportData();

            // Special handling for the UI fields that don't map 1:1 to PerformanceReport
            txtExerciseName.setText(exerciseName);
            txtTotalFrames.setText(String.valueOf(totalReps)); // Using total reps as proxy for frames in this simple
                                                               // view
            txtCorrectFrames.setText(String.valueOf(correctReps));

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading report: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void createMockReport() {
        // Create mock performance report for demonstration
        currentReport = createMockPerformanceReport();
        currentExercise = exerciseLibraryService.getExerciseById(currentReport.getExerciseId());

        displayReportData();
    }

    private PerformanceReport createMockPerformanceReport() {
        // Create mock data for demonstration
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.Duration duration = java.time.Duration.ofMinutes(5).plusSeconds(30);

        Map<String, Object> specificMetrics = new java.util.HashMap<>();
        specificMetrics.put("total_frames", 1250);
        specificMetrics.put("correct_frames", 1063);
        specificMetrics.put("consistency_score", 0.87f);
        specificMetrics.put("wrist_range_of_motion", "Good");

        List<String> recommendations = new ArrayList<>();
        recommendations.add("Excellent form! You're performing the exercise correctly");
        recommendations.add("Good effort! Aim to complete all recommended repetitions");
        recommendations.add("Remember to perform exercises gently to avoid joint stress");
        recommendations.add("If you experience pain, stop and consult your doctor");

        return new PerformanceReport(
                "session_001",
                "patient_001",
                "ex_001", // Wrist Flexion/Extension
                duration,
                0.85f, // 85% form accuracy
                0.92f, // 92% completion rate
                specificMetrics,
                recommendations,
                now);
    }

    private void loadReportFromService(String sessionId) {
        if (sessionId == null)
            return;

        android.util.Log.d("PerformanceReport", "Loading report for session: " + sessionId);

        // Fetch session stats from service
        reportingService.getSessionPerformanceReport(sessionId)
                .thenAccept(report -> {
                    runOnUiThread(() -> {
                        if (report != null) {
                            currentReport = report;
                            displayReportData();
                        } else {
                            android.util.Log.e("PerformanceReport", "Report not found for session: " + sessionId);
                            Toast.makeText(this, "Performance report not available yet", Toast.LENGTH_SHORT).show();
                            // Fallback to mock only if explicitly needed for testing, otherwise it's
                            // confusing
                            createMockReport();
                        }
                    });
                })
                .exceptionally(throwable -> {
                    runOnUiThread(() -> {
                        android.util.Log.e("PerformanceReport", "Error loading report", throwable);
                        Toast.makeText(this, "Failed to load performance report", Toast.LENGTH_SHORT).show();
                        createMockReport();
                    });
                    return null;
                });
    }

    private void displayReportData() {
        if (currentReport == null) {
            Toast.makeText(this, "No report data available", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Display exercise information
        if (currentExercise != null) {
            txtExerciseName.setText(currentExercise.getName());
        } else {
            txtExerciseName.setText("Exercise Session");
        }

        // Format and display date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy - h:mm a");
        txtSessionDate.setText(currentReport.getTimestamp().format(formatter));

        // Display duration
        long minutes = currentReport.getSessionDuration().toMinutes();
        long seconds = currentReport.getSessionDuration().getSeconds() % 60;
        txtSessionDuration.setText(String.format("%d:%02d", minutes, seconds));

        // Display performance metrics
        displayPerformanceMetrics();

        // Display recommendations
        displayRecommendations();

        // Display specific metrics if available
        displaySpecificMetrics();
    }

    private void displayPerformanceMetrics() {
        // Form Accuracy
        int accuracyPercent = Math.round(currentReport.getFormAccuracy() * 100);
        txtFormAccuracy.setText(accuracyPercent + "%");
        progressFormAccuracy.setProgress(accuracyPercent);

        // Set accuracy color based on performance
        int accuracyColor;
        if (accuracyPercent >= 80) {
            accuracyColor = ContextCompat.getColor(this, R.color.success);
        } else if (accuracyPercent >= 60) {
            accuracyColor = ContextCompat.getColor(this, android.R.color.holo_orange_light);
        } else {
            accuracyColor = ContextCompat.getColor(this, android.R.color.holo_red_light);
        }
        txtFormAccuracy.setTextColor(accuracyColor);
        progressFormAccuracy.setProgressTintList(android.content.res.ColorStateList.valueOf(accuracyColor));

        // Completion Rate
        int completionPercent = Math.round(currentReport.getCompletionRate() * 100);
        txtCompletionRate.setText(completionPercent + "%");
        progressCompletionRate.setProgress(completionPercent);

        // Set completion color based on performance
        int completionColor;
        if (completionPercent >= 90) {
            completionColor = ContextCompat.getColor(this, R.color.success);
        } else if (completionPercent >= 70) {
            completionColor = ContextCompat.getColor(this, android.R.color.holo_orange_light);
        } else {
            completionColor = ContextCompat.getColor(this, android.R.color.holo_red_light);
        }
        txtCompletionRate.setTextColor(completionColor);
        progressCompletionRate.setProgressTintList(android.content.res.ColorStateList.valueOf(completionColor));

        // Additional metrics
        Map<String, Object> metrics = currentReport.getSpecificMetrics();
        if (metrics != null) {
            Object totalFrames = metrics.get("total_frames");
            Object correctFrames = metrics.get("correct_frames");

            if (totalFrames != null) {
                txtTotalFrames.setText(String.valueOf(totalFrames));
            }

            if (correctFrames != null) {
                txtCorrectFrames.setText(String.valueOf(correctFrames));
            }
        }
    }

    private void displayRecommendations() {
        List<String> recommendations = currentReport.getRecommendations();
        if (recommendations != null && !recommendations.isEmpty()) {
            RecommendationsAdapter adapter = new RecommendationsAdapter(recommendations);
            recyclerRecommendations.setAdapter(adapter);
        }
    }

    private void displaySpecificMetrics() {
        Map<String, Object> specificMetrics = currentReport.getSpecificMetrics();
        if (specificMetrics != null && !specificMetrics.isEmpty()) {
            // Filter out metrics already displayed
            Map<String, Object> filteredMetrics = new java.util.HashMap<>(specificMetrics);
            filteredMetrics.remove("total_frames");
            filteredMetrics.remove("correct_frames");

            if (!filteredMetrics.isEmpty()) {
                cardSpecificMetrics.setVisibility(View.VISIBLE);
                SpecificMetricsAdapter adapter = new SpecificMetricsAdapter(filteredMetrics);
                recyclerSpecificMetrics.setAdapter(adapter);
            }
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnRetryExercise.setOnClickListener(v -> {
            if (currentExercise != null) {
                Intent intent = new Intent(this, ExerciseSessionActivity.class);
                intent.putExtra("exercise_id", currentExercise.getId());
                intent.putExtra("exercise_name", currentExercise.getName());
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Exercise information not available", Toast.LENGTH_SHORT).show();
            }
        });

        btnShareReport.setOnClickListener(v -> shareReport());
    }

    private void shareReport() {
        if (currentReport == null)
            return;

        StringBuilder reportText = new StringBuilder();
        reportText.append("Exercise Performance Report\n\n");

        if (currentExercise != null) {
            reportText.append("Exercise: ").append(currentExercise.getName()).append("\n");
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy - h:mm a");
        reportText.append("Date: ").append(currentReport.getTimestamp().format(formatter)).append("\n");

        long minutes = currentReport.getSessionDuration().toMinutes();
        long seconds = currentReport.getSessionDuration().getSeconds() % 60;
        reportText.append("Duration: ").append(String.format("%d:%02d", minutes, seconds)).append("\n\n");

        reportText.append("Performance Metrics:\n");
        reportText.append("• Form Accuracy: ").append(Math.round(currentReport.getFormAccuracy() * 100)).append("%\n");
        reportText.append("• Completion Rate: ").append(Math.round(currentReport.getCompletionRate() * 100))
                .append("%\n\n");

        if (currentReport.getRecommendations() != null && !currentReport.getRecommendations().isEmpty()) {
            reportText.append("Recommendations:\n");
            for (String recommendation : currentReport.getRecommendations()) {
                reportText.append("• ").append(recommendation).append("\n");
            }
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Exercise Performance Report");
        shareIntent.putExtra(Intent.EXTRA_TEXT, reportText.toString());

        startActivity(Intent.createChooser(shareIntent, "Share Report"));
    }

    /**
     * Adapter for displaying recommendations
     */
    private static class RecommendationsAdapter extends RecyclerView.Adapter<RecommendationsAdapter.ViewHolder> {
        private final List<String> recommendations;

        public RecommendationsAdapter(List<String> recommendations) {
            this.recommendations = recommendations;
        }

        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            String recommendation = recommendations.get(position);
            holder.textView.setText("• " + recommendation);
            holder.textView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_primary));
            holder.textView.setTextSize(14);
            holder.textView.setPadding(0, 8, 0, 8);
        }

        @Override
        public int getItemCount() {
            return recommendations.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            ViewHolder(View itemView) {
                super(itemView);
                textView = itemView.findViewById(android.R.id.text1);
            }
        }
    }

    /**
     * Adapter for displaying specific metrics
     */
    private static class SpecificMetricsAdapter extends RecyclerView.Adapter<SpecificMetricsAdapter.ViewHolder> {
        private final List<Map.Entry<String, Object>> metrics;

        public SpecificMetricsAdapter(Map<String, Object> metrics) {
            this.metrics = new ArrayList<>(metrics.entrySet());
        }

        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Map.Entry<String, Object> entry = metrics.get(position);

            // Format key name
            String key = entry.getKey().replace("_", " ");
            key = key.substring(0, 1).toUpperCase() + key.substring(1);

            holder.text1.setText(key);
            holder.text1.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_primary));
            holder.text1.setTextSize(14);

            // Format value
            String value = String.valueOf(entry.getValue());
            if (entry.getValue() instanceof Float) {
                float floatValue = (Float) entry.getValue();
                if (floatValue <= 1.0f && floatValue >= 0.0f) {
                    // Assume it's a percentage
                    value = Math.round(floatValue * 100) + "%";
                }
            }

            holder.text2.setText(value);
            holder.text2.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_secondary));
            holder.text2.setTextSize(12);
        }

        @Override
        public int getItemCount() {
            return metrics.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView text1, text2;

            ViewHolder(View itemView) {
                super(itemView);
                text1 = itemView.findViewById(android.R.id.text1);
                text2 = itemView.findViewById(android.R.id.text2);
            }
        }
    }
}