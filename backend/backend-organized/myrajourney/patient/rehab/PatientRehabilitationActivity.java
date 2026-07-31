package com.example.myrajourney.patient.rehab;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

// --- ADDED IMPORTS ---
import com.example.myrajourney.R;
import com.example.myrajourney.auth.LoginActivity;
import com.example.myrajourney.core.network.ApiClient;
import com.example.myrajourney.core.network.ApiService;
import com.example.myrajourney.core.session.SessionManager;
import com.example.myrajourney.data.model.ApiResponse;
import com.example.myrajourney.data.model.RehabPlan;
import com.example.myrajourney.rehab.models.RAExercise;
import com.example.myrajourney.rehab.models.ExerciseAssignment;
import com.example.myrajourney.rehab.services.ExerciseLibraryService;
import com.example.myrajourney.rehab.services.ExerciseAssignmentService;
import com.example.myrajourney.rehab.services.impl.ExerciseLibraryServiceImpl;
import com.example.myrajourney.rehab.services.impl.ExerciseAssignmentServiceImpl;
// ---------------------

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PatientRehabilitationActivity extends AppCompatActivity {

    private LinearLayout rehabContainer;
    private TextView noRehabText;
    // Using local inner class for UI representation
    private List<LocalRehabExercise> todayExercises;
    private SessionManager sessionManager;

    // New RA exercise system services
    private ExerciseLibraryService exerciseLibraryService;
    private ExerciseAssignmentService assignmentService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_rehabilitation);

        sessionManager = SessionManager.getInstance(this);

        // Initialize new RA exercise services
        exerciseLibraryService = new ExerciseLibraryServiceImpl();
        assignmentService = new ExerciseAssignmentServiceImpl(this);

        // Check if user is logged in
        if (!sessionManager.isSessionValid()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initializeViews();
        loadTodayExercises();

        findViewById(R.id.btnViewHistory).setOnClickListener(v -> {
            startActivity(new Intent(this, ExerciseHistoryActivity.class));
        });
    }

    private void initializeViews() {
        rehabContainer = findViewById(R.id.rehabContainer);
        noRehabText = findViewById(R.id.noRehabText);

        // Back button
        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        // Set today's date dynamically
        TextView todayDateText = findViewById(R.id.todayDate);
        if (todayDateText != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
            String currentDate = dateFormat.format(new Date());
            todayDateText.setText(currentDate);
        }
    }

    private void loadTodayExercises() {
        todayExercises = new ArrayList<>();
        // Load RA exercises from new system first, then fallback to API
        loadRAExercises();
    }

    /**
     * Load RA exercises from the new exercise system
     */
    private void loadRAExercises() {
        // Clear previous exercises to avoid duplication
        todayExercises.clear();

        try {
            if (sessionManager.getCurrentUser() == null) {
                loadRehabFromAPI(); // Fallback to old system
                return;
            }

            String patientId = sessionManager.getCurrentUser().getIdAsString();

            // Get assignments for this patient
            List<ExerciseAssignment> assignments = assignmentService.getPatientAssignments(patientId);

            if (assignments == null || assignments.isEmpty()) {
                // If no new-style assignments, check for old-style rehab plans
                loadRehabFromAPI();
            } else {
                // Load assigned exercises from new system
                for (ExerciseAssignment assignment : assignments) {
                    for (String exerciseId : assignment.getExerciseIds()) {
                        RAExercise raExercise = exerciseLibraryService.getExerciseById(exerciseId);
                        if (raExercise != null) {
                            convertRAExerciseToLocal(raExercise);
                        }
                    }
                }
                displayExercises();
            }

        } catch (Exception e) {
            e.printStackTrace();
            // Fallback to old API system if list is still empty
            if (todayExercises.isEmpty()) {
                loadRehabFromAPI();
            }
        }
    }

    /**
     * Find an exercise in the library by name
     */
    private RAExercise findRAExerciseByName(String name) {
        if (name == null)
            return null;
        List<RAExercise> library = exerciseLibraryService.getAllExercises();
        for (RAExercise ex : library) {
            if (ex.getName().equalsIgnoreCase(name)) {
                return ex;
            }
        }
        return null;
    }

    /**
     * Convert RAExercise to LocalRehabExercise for UI compatibility
     */
    private void convertRAExerciseToLocal(RAExercise raExercise) {
        // Prevent duplication by checking if this exercise ID already exists in the
        // list
        for (LocalRehabExercise existing : todayExercises) {
            if (existing instanceof EnhancedLocalRehabExercise) {
                if (((EnhancedLocalRehabExercise) existing).getExerciseId().equals(raExercise.getId())) {
                    android.util.Log.d("RehabDuplication", "Exercise already in list: " + raExercise.getName());
                    return;
                }
            }
        }

        String videoUrl = raExercise.getVideoUrl() != null ? raExercise.getVideoUrl()
                : "https://www.youtube.com/watch?v=NXbtJ6qCdbs";

        // Calculate progress with enhanced tracking
        int progress = calculateRAExerciseProgress(raExercise.getId());

        // Create enhanced local exercise with RA-specific data
        EnhancedLocalRehabExercise localExercise = new EnhancedLocalRehabExercise(
                raExercise.getId(),
                raExercise.getName(),
                raExercise.getDescription(),
                videoUrl,
                3, // Default sets
                String.valueOf(getRecommendedReps(raExercise.getDifficultyLevel())), // Difficulty-based reps
                progress,
                raExercise.getCategory().getDisplayName(),
                raExercise.getRaSpecificBenefits());

        todayExercises.add(localExercise);
    }

    /**
     * Get recommended reps based on difficulty level
     */
    private int getRecommendedReps(int difficultyLevel) {
        switch (difficultyLevel) {
            case 1:
                return 10; // Beginner
            case 2:
                return 15; // Intermediate
            case 3:
                return 20; // Advanced
            default:
                return 10;
        }
    }

    /**
     * Calculate progress for RA exercise with enhanced tracking
     */
    private int calculateRAExerciseProgress(String exerciseId) {
        SharedPreferences prefs = getSharedPreferences("ra_exercise_progress", MODE_PRIVATE);
        String currentDate = getCurrentDate();

        // Check if completed today
        boolean completedToday = prefs.getBoolean(exerciseId + "_" + currentDate, false);
        if (completedToday) {
            return 100;
        }

        // Calculate weekly progress (completed days in last 7 days)
        int completedDays = 0;
        for (int i = 0; i < 7; i++) {
            String checkDate = getDateDaysAgo(i);
            if (prefs.getBoolean(exerciseId + "_" + checkDate, false)) {
                completedDays++;
            }
        }

        // Return progress as percentage of week completed
        return Math.min((completedDays * 100) / 7, 90); // Max 90% if not completed today
    }

    /**
     * Get date string for days ago
     */
    private String getDateDaysAgo(int daysAgo) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.DAY_OF_YEAR, -daysAgo);
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());
    }

    /**
     * Save RA exercise completion with enhanced tracking
     */
    private void saveRAExerciseCompletion(String exerciseId, boolean completed) {
        SharedPreferences prefs = getSharedPreferences("ra_exercise_progress", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        String currentDate = getCurrentDate();
        String key = exerciseId + "_" + currentDate;

        editor.putBoolean(key, completed);

        if (completed) {
            // Track completion time
            editor.putLong(exerciseId + "_" + currentDate + "_time", System.currentTimeMillis());

            // Update total completions counter
            int totalCompletions = prefs.getInt(exerciseId + "_total_completions", 0);
            editor.putInt(exerciseId + "_total_completions", totalCompletions + 1);

            // Update streak counter
            updateExerciseStreak(editor, prefs, exerciseId);
        }

        editor.apply();
    }

    /**
     * Update exercise streak counter
     */
    private void updateExerciseStreak(SharedPreferences.Editor editor, SharedPreferences prefs, String exerciseId) {
        int currentStreak = prefs.getInt(exerciseId + "_streak", 0);

        // Check if completed yesterday
        String yesterday = getDateDaysAgo(1);
        boolean completedYesterday = prefs.getBoolean(exerciseId + "_" + yesterday, false);

        if (completedYesterday || currentStreak == 0) {
            // Continue or start streak
            editor.putInt(exerciseId + "_streak", currentStreak + 1);
        } else {
            // Reset streak
            editor.putInt(exerciseId + "_streak", 1);
        }

        // Update best streak if needed
        int newStreak = currentStreak + 1;
        int bestStreak = prefs.getInt(exerciseId + "_best_streak", 0);
        if (newStreak > bestStreak) {
            editor.putInt(exerciseId + "_best_streak", newStreak);
        }
    }

    private void loadRehabFromAPI() {
        ApiService apiService = ApiClient.getApiService(this);

        // Get current user ID safely
        Integer patientId = null;
        try {
            String userIdStr = sessionManager.getUserName(); // Or get actual ID if stored in session
            // In a real app, store ID in session. For now, passing null might let backend
            // use token.
        } catch (Exception e) {
            e.printStackTrace();
        }

        Call<ApiResponse<List<RehabPlan>>> call = apiService.getRehabPlans(patientId);

        call.enqueue(new Callback<ApiResponse<List<RehabPlan>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<RehabPlan>>> call,
                    Response<ApiResponse<List<RehabPlan>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<RehabPlan> plans = response.body().getData();

                    if (plans != null && !plans.isEmpty()) {
                        for (RehabPlan plan : plans) {
                            // Check if plan has explicit sub-exercises
                            if (plan.getExercises() != null && !plan.getExercises().isEmpty()) {
                                for (RehabPlan.RehabExercise ex : plan.getExercises()) {
                                    String name = ex.getName() != null ? ex.getName() : "Exercise";

                                    // Check if name already exists in list (Deduplication)
                                    boolean alreadyExists = false;
                                    for (LocalRehabExercise existing : todayExercises) {
                                        if (existing.getName().equalsIgnoreCase(name)) {
                                            alreadyExists = true;
                                            break;
                                        }
                                    }
                                    if (alreadyExists)
                                        continue;

                                    // Try to match with library for live tracking
                                    RAExercise libraryEx = findRAExerciseByName(name);
                                    if (libraryEx != null) {
                                        convertRAExerciseToLocal(libraryEx);
                                        continue;
                                    }

                                    String videoUrl = plan.getVideoUrl() != null ? plan.getVideoUrl()
                                            : "https://www.youtube.com/watch?v=k2kMJ2hHugQ";
                                    int sets = ex.getSets() != null ? ex.getSets() : 3;
                                    String reps = ex.getReps() != null ? ex.getReps() : "10";
                                    int progress = calculateProgress(null, ex);

                                    String desc = ex.getDescription() != null ? ex.getDescription()
                                            : (plan.getDescription() != null ? plan.getDescription()
                                                    : "Rehabilitation exercise");

                                    todayExercises
                                            .add(new LocalRehabExercise(name, desc, videoUrl, sets, reps, progress));
                                }
                            } else {
                                // Fallback: use plan data directly as a single exercise
                                String name = plan.getExerciseName() != null ? plan.getExerciseName()
                                        : (plan.getTitle() != null ? plan.getTitle() : "Exercise");

                                // Check if name already exists in list (Deduplication)
                                boolean alreadyExists = false;
                                for (LocalRehabExercise existing : todayExercises) {
                                    if (existing.getName().equalsIgnoreCase(name)) {
                                        alreadyExists = true;
                                        break;
                                    }
                                }
                                if (alreadyExists)
                                    continue;

                                // Try to match with library for live tracking
                                RAExercise libraryEx = findRAExerciseByName(name);
                                if (libraryEx != null) {
                                    convertRAExerciseToLocal(libraryEx);
                                    continue;
                                }

                                String videoUrl = plan.getVideoUrl() != null ? plan.getVideoUrl()
                                        : "https://www.youtube.com/watch?v=k2kMJ2hHugQ";
                                int sets = plan.getSetsPerDay() != null ? plan.getSetsPerDay() : 3;
                                String reps = plan.getRepsPerSet() != null ? String.valueOf(plan.getRepsPerSet())
                                        : "10";
                                int progress = calculateProgress(plan, null);

                                String desc = plan.getDescription() != null ? plan.getDescription()
                                        : "Rehabilitation exercise";

                                todayExercises.add(new LocalRehabExercise(name, desc, videoUrl, sets, reps, progress));
                            }
                        }
                        displayExercises();
                    } else {
                        displayExercises(); // Show empty state
                    }
                } else {
                    displayExercises();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<RehabPlan>>> call, Throwable t) {
                displayExercises();
            }
        });
    }

    private int calculateProgress(RehabPlan plan, RehabPlan.RehabExercise exercise) {
        SharedPreferences prefs = getSharedPreferences("rehab_status", MODE_PRIVATE);
        String exerciseName = "";

        if (exercise != null) {
            exerciseName = exercise.getName();
        } else if (plan != null) {
            exerciseName = plan.getExerciseName() != null ? plan.getExerciseName() : plan.getTitle();
        }

        String dateKey = exerciseName + "_" + getCurrentDate();
        boolean completed = prefs.getBoolean(dateKey, false);
        return completed ? 100 : 0;
    }

    private void displayExercises() {
        rehabContainer.removeAllViews();

        if (todayExercises.isEmpty()) {
            noRehabText.setVisibility(View.VISIBLE);
            return;
        }

        noRehabText.setVisibility(View.GONE);

        for (LocalRehabExercise exercise : todayExercises) {
            addExerciseCard(exercise);
        }
    }

    private void addExerciseCard(LocalRehabExercise exercise) {
        View cardView = getLayoutInflater().inflate(R.layout.item_patient_rehab, rehabContainer, false);

        TextView exerciseName = cardView.findViewById(R.id.exerciseName);
        TextView exerciseDescription = cardView.findViewById(R.id.exerciseDescription);
        TextView exerciseSets = cardView.findViewById(R.id.exerciseSets);
        TextView exerciseGoals = cardView.findViewById(R.id.exerciseGoals);
        Button statusButton = cardView.findViewById(R.id.statusButton);
        Button watchVideoBtn = cardView.findViewById(R.id.watchVideoBtn);
        ProgressBar progressBar = cardView.findViewById(R.id.progressBar);

        exerciseName.setText(exercise.getName());

        // Enhanced description for RA exercises
        if (exercise instanceof EnhancedLocalRehabExercise) {
            EnhancedLocalRehabExercise raExercise = (EnhancedLocalRehabExercise) exercise;
            String enhancedDesc = exercise.getDescription() + "\n\nRA Benefits:\n" + raExercise.getRaBenefitsText();
            exerciseDescription.setText(enhancedDesc);
            exerciseGoals.setText("Category: " + raExercise.getCategory() + " • Complete daily for best results");
        } else {
            exerciseDescription.setText(exercise.getDescription());
            exerciseGoals.setText("Goal: Complete all sets daily");
        }

        exerciseSets.setText("Sets: " + exercise.getSets() + " × " + exercise.getReps() + " reps");

        progressBar.setProgress(exercise.getProgress());

        if (exercise.isCompleted()) {
            statusButton.setText("✅ Completed");
            statusButton.setBackgroundColor(getColor(android.R.color.holo_green_dark));
        } else {
            statusButton.setText("⏳ Pending");
            statusButton.setBackgroundColor(getColor(android.R.color.holo_orange_light));
        }

        statusButton.setOnClickListener(v -> {
            if (!exercise.isCompleted()) {
                exercise.setCompleted(true);
                statusButton.setText("✅ Completed");
                statusButton.setBackgroundColor(getColor(android.R.color.holo_green_dark));

                // Update progress
                progressBar.setProgress(100);

                // Enhanced tracking for RA exercises
                if (exercise instanceof EnhancedLocalRehabExercise) {
                    EnhancedLocalRehabExercise raExercise = (EnhancedLocalRehabExercise) exercise;
                    saveRAExerciseCompletion(raExercise.getExerciseId(), true);

                    // Show RA-specific completion message
                    String message = "Great job! " + raExercise.getRaBenefits().get(0);
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                } else {
                    // Fallback to old tracking
                    saveExerciseStatus(exercise.getName(), true);
                    Toast.makeText(this, "Exercise completed!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        watchVideoBtn.setOnClickListener(v -> {
            // PRIORITIZE LOCAL VIDEOS - Try local video first
            String localVideoPath = getLocalVideoPathForExercise(exercise);
            if (localVideoPath != null) {
                // Open local video in a dedicated video player activity
                openLocalVideo(exercise, localVideoPath);
            } else {
                // Fallback to YouTube only if no local video available
                openYouTubeVideo(exercise.getVideoUrl());
            }
        });

        // REMOVED: Live tracking button completely removed from layout and code

        rehabContainer.addView(cardView);
    }

    /**
     * Start camera-based motion tracking for exercise
     * ENHANCED: Now uses advanced live tracking with reference video analysis
     */
    private void startCameraTracking(EnhancedLocalRehabExercise exercise) {
        android.util.Log.d("RehabTracking", "Live tracking disabled - using video player instead");

        try {
            // LIVE TRACKING DISABLED - Use regular video player instead
            Toast.makeText(this, "Live tracking is temporarily disabled. Playing exercise video.", Toast.LENGTH_SHORT).show();
            
            // Launch regular video player
            Intent intent = new Intent(this, LocalVideoPlayerActivity.class);
            intent.putExtra("exercise_id", exercise.getExerciseId());
            intent.putExtra("exercise_name", exercise.getName());
            intent.putExtra("exercise_description", exercise.getDescription());
            
            // Pass video URL
            String videoUrl = getExerciseVideoUrl(exercise);
            if (videoUrl != null) {
                intent.putExtra("video_path", videoUrl);
                android.util.Log.d("RehabTracking", "Video URL: " + videoUrl);
            }
            
            startActivity(intent);
            
            /*
            // OLD LIVE TRACKING CODE - DISABLED
            intent.putExtra("video_url", "https://www.youtube.com/watch?v=NXbtJ6qCdbs");
            android.util.Log.d("RehabTracking", "Using fallback video URL");

            // Additional exercise parameters
            intent.putExtra("exercise_sets", exercise.getSets());
            intent.putExtra("exercise_reps", exercise.getReps());
            intent.putExtra("exercise_category", exercise.getCategory());

            android.util.Log.d("RehabTracking", "Launching Live Tracking Activity...");
            startActivity(intent);
            android.util.Log.d("RehabTracking", "Live Tracking Activity launched successfully");
            */

        } catch (Exception e) {
            android.util.Log.e("RehabTracking", "Error starting live tracking: " + e.getMessage());

            // Fallback to original camera tracking
            try {
                Intent fallbackIntent = new Intent(this,
                        com.example.myrajourney.patient.rehab.ExerciseSessionActivity.class);
                fallbackIntent.putExtra("exercise_id", exercise.getExerciseId());
                fallbackIntent.putExtra("exercise_name", exercise.getName());
                fallbackIntent.putExtra("exercise_description", exercise.getDescription());
                fallbackIntent.putExtra("exercise_sets", exercise.getSets());
                fallbackIntent.putExtra("exercise_reps", exercise.getReps());
                fallbackIntent.putExtra("exercise_category", exercise.getCategory());

                startActivity(fallbackIntent);
                Toast.makeText(this, "Using basic camera tracking", Toast.LENGTH_SHORT).show();

            } catch (Exception fallbackError) {
                android.util.Log.e("RehabTracking", "Fallback also failed: " + fallbackError.getMessage());
                Toast.makeText(this, "Error starting camera tracking: " + fallbackError.getMessage(), Toast.LENGTH_LONG)
                        .show();
            }
        }
    }

    /**
     * Get exercise video URL for reference analysis
     */
    private String getExerciseVideoUrl(EnhancedLocalRehabExercise exercise) {
        // 1. Check if the exercise object itself has a valid video URL (prioritized)
        String videoUrl = exercise.getVideoUrl();
        if (videoUrl != null && !videoUrl.isEmpty()) {
            // Ensure local assets are properly formatted for ExoPlayer if needed
            // (LiveTrackingActivity handles the file:///android_asset/ prefix parsing,
            // but we ensure it's passed correctly here)
            return videoUrl;
        }

        // 2. Check local mapping if URL was missing
        String localVideoPath = getLocalVideoPath(exercise.getExerciseId());
        if (localVideoPath != null) {
            return "file:///android_asset/" + localVideoPath;
        }

        // 3. Fallback logic based on name
        String exerciseName = exercise.getName().toLowerCase();
        if (exerciseName.contains("shoulder")) {
            return "https://www.youtube.com/watch?v=NXbtJ6qCdbs";
        } else if (exerciseName.contains("finger") || exerciseName.contains("hand")) {
            return "https://www.youtube.com/watch?v=k2kMJ2hHugQ";
        }

        // 4. Ultimate fallback
        return "https://www.youtube.com/watch?v=NXbtJ6qCdbs";
    }

    /**
     * Get local video path based on exercise ID
     */
    private String getLocalVideoPath(String exerciseId) {
        switch (exerciseId) {
            case "ex_001":
                return "exercise_videos/ex_001_wrist_flexion.mp4";
            case "ex_002":
                return "exercise_videos/ex_002_wrist_rotation.mp4";
            case "ex_003":
                return "exercise_videos/ex_003_thumb_opposition.mp4";
            case "ex_004":
                return "exercise_videos/ex_004_thumb_flexion.mp4";
            case "ex_005":
                return "exercise_videos/ex_005_finger_flexion.mp4";
            case "ex_006":
                return "exercise_videos/ex_006_finger_extension.mp4";
            case "ex_007":
                return "exercise_videos/ex_007_finger_pinch.mp4";
            case "ex_008":
                return "exercise_videos/ex_008_knee_flexion.mp4";
            case "ex_009":
                return "exercise_videos/ex_009_hip_flexion.mp4";
            case "ex_010":
                return "exercise_videos/ex_010_hip_abduction.mp4";
            default:
                return null;
        }
    }

    /**
     * Get local video path for exercise
     */
    private String getLocalVideoPathForExercise(LocalRehabExercise exercise) {
        if (exercise instanceof EnhancedLocalRehabExercise) {
            EnhancedLocalRehabExercise raExercise = (EnhancedLocalRehabExercise) exercise;
            return getLocalVideoPath(raExercise.getExerciseId());
        }
        return null;
    }

    /**
     * Open local video in dedicated video player
     */
    private void openLocalVideo(LocalRehabExercise exercise, String localVideoPath) {
        try {
            Intent intent = new Intent(this, LocalVideoPlayerActivity.class);
            intent.putExtra("video_path", localVideoPath);
            intent.putExtra("exercise_name", exercise.getName());
            intent.putExtra("exercise_description", exercise.getDescription());

            if (exercise instanceof EnhancedLocalRehabExercise) {
                EnhancedLocalRehabExercise raExercise = (EnhancedLocalRehabExercise) exercise;
                intent.putExtra("exercise_id", raExercise.getExerciseId());
                intent.putExtra("exercise_category", raExercise.getCategory());
            }

            startActivity(intent);

        } catch (Exception e) {
            android.util.Log.e("LocalVideo", "Error opening local video: " + e.getMessage());
            // Fallback to YouTube
            openYouTubeVideo(exercise.getVideoUrl());
        }
    }

    /**
     * Fallback to YouTube video (when local video not available)
     */
    private void openYouTubeVideo(String videoUrl) {
        if (videoUrl == null || videoUrl.isEmpty())
            return;
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Unable to open video", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveExerciseStatus(String exerciseName, boolean completed) {
        SharedPreferences prefs = getSharedPreferences("rehab_status", MODE_PRIVATE);
        prefs.edit().putBoolean(exerciseName + "_" + getCurrentDate(), completed).apply();
    }

    private String getCurrentDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    // Renamed inner class to avoid conflict with RehabPlan.RehabExercise
    private static class LocalRehabExercise {
        private String name;
        private String description;
        private String videoUrl;
        private int sets;
        private String reps;
        private int progress;
        private boolean completed;

        public LocalRehabExercise(String name, String description, String videoUrl, int sets, String reps,
                int progress) {
            this.name = name;
            this.description = description;
            this.videoUrl = videoUrl;
            this.sets = sets;
            this.reps = reps;
            this.progress = progress;
            this.completed = (progress >= 100);
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public String getVideoUrl() {
            return videoUrl;
        }

        public int getSets() {
            return sets;
        }

        public String getReps() {
            return reps;
        }

        public int getProgress() {
            return progress;
        }

        public boolean isCompleted() {
            return completed;
        }

        public void setCompleted(boolean completed) {
            this.completed = completed;
        }
    }

    /**
     * Enhanced LocalRehabExercise with RA-specific tracking
     */
    private static class EnhancedLocalRehabExercise extends LocalRehabExercise {
        private String exerciseId;
        private String category;
        private List<String> raBenefits;

        public EnhancedLocalRehabExercise(String exerciseId, String name, String description,
                String videoUrl, int sets, String reps, int progress,
                String category, List<String> raBenefits) {
            super(name, description, videoUrl, sets, reps, progress);
            this.exerciseId = exerciseId;
            this.category = category;
            this.raBenefits = raBenefits != null ? raBenefits : new ArrayList<>();
        }

        public String getExerciseId() {
            return exerciseId;
        }

        public String getCategory() {
            return category;
        }

        public List<String> getRaBenefits() {
            return raBenefits;
        }

        public String getRaBenefitsText() {
            if (raBenefits.isEmpty())
                return "Helps with RA symptom management";

            StringBuilder benefits = new StringBuilder();
            for (String benefit : raBenefits) {
                benefits.append("• ").append(benefit).append("\n");
            }
            return benefits.toString().trim();
        }
    }
}