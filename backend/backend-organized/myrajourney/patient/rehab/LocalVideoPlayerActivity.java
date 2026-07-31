package com.example.myrajourney.patient.rehab;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myrajourney.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Activity for playing local exercise videos from assets
 */
public class LocalVideoPlayerActivity extends AppCompatActivity {

    private static final String TAG = "LocalVideoPlayer";

    private VideoView videoView;
    private MediaController mediaController;
    private TextView exerciseNameText;
    private TextView exerciseDescriptionText;
    private ImageView btnBack;
    // REMOVED: btnStartLiveTracking - live tracking disabled

    private String videoPath;
    private String exerciseName;
    private String exerciseDescription;
    private String exerciseId;
    private String exerciseCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_video_player);

        // Get data from intent
        loadIntentData();

        // Initialize views
        initializeViews();

        // Setup video player
        setupVideoPlayer();

        // Setup listeners
        setupListeners();
    }

    private void loadIntentData() {
        videoPath = getIntent().getStringExtra("video_path");
        exerciseName = getIntent().getStringExtra("exercise_name");
        exerciseDescription = getIntent().getStringExtra("exercise_description");
        exerciseId = getIntent().getStringExtra("exercise_id");
        exerciseCategory = getIntent().getStringExtra("exercise_category");

        Log.d(TAG, "Loading video: " + videoPath);
        Log.d(TAG, "Exercise: " + exerciseName);
    }

    private void initializeViews() {
        videoView = findViewById(R.id.video_view);
        exerciseNameText = findViewById(R.id.exercise_name_text);
        exerciseDescriptionText = findViewById(R.id.exercise_description_text);
        btnBack = findViewById(R.id.btn_back);
        // REMOVED: btnStartLiveTracking - live tracking disabled

        // Set exercise info
        if (exerciseNameText != null) {
            exerciseNameText.setText(exerciseName != null ? exerciseName : "Exercise Video");
        }

        if (exerciseDescriptionText != null) {
            String fullDescription = exerciseDescription != null ? exerciseDescription
                    : "Follow the video demonstration";
            if (exerciseCategory != null) {
                fullDescription += "\n\nCategory: " + exerciseCategory;
            }
            exerciseDescriptionText.setText(fullDescription);
        }
    }

    private void setupVideoPlayer() {
        if (videoPath == null || videoPath.isEmpty()) {
            Toast.makeText(this, "Video not available", Toast.LENGTH_SHORT).show();
            // Don't finish immediately, let the user see the error or use other controls
            return;
        }

        try {
            // Check if it's a web URL (backward compatibility)
            if (videoPath.startsWith("http")) {
                videoView.setVideoURI(Uri.parse(videoPath));
            } else {
                // Asset playback via cache file
                File cachedFile = copyAssetToCache(videoPath);
                if (cachedFile != null && cachedFile.exists()) {
                    Log.d(TAG, "Playing local file: " + cachedFile.getAbsolutePath());
                    videoView.setVideoPath(cachedFile.getAbsolutePath());
                } else {
                    Log.e(TAG, "Failed to copy asset: " + videoPath);
                    Toast.makeText(this, "Failed to load video file", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // Setup media controller
            mediaController = new MediaController(this);
            mediaController.setAnchorView(videoView);
            videoView.setMediaController(mediaController);

            // Setup video listeners
            videoView.setOnPreparedListener(mediaPlayer -> {
                Log.d(TAG, "Video prepared successfully");
                mediaPlayer.setLooping(true); // Loop the video
                videoView.start(); // Auto-start the video
            });

            videoView.setOnErrorListener((mediaPlayer, what, extra) -> {
                Log.e(TAG, "Video error: what=" + what + ", extra=" + extra);
                Toast.makeText(this, "Error playing video", Toast.LENGTH_SHORT).show();
                return true;
            });

            videoView.setOnCompletionListener(mediaPlayer -> {
                Log.d(TAG, "Video completed");
                // Video will loop automatically due to setLooping(true)
            });

        } catch (Exception e) {
            Log.e(TAG, "Error setting up video player", e);
            Toast.makeText(this, "Error loading video: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Copy asset file to cache for VideoView compatibility
     */
    private File copyAssetToCache(String assetPath) {
        try {
            File cacheDir = getCacheDir();
            // Extract filename from path (e.g. "exercise_videos/ex_001.mp4" ->
            // "ex_001.mp4")
            String fileName = new File(assetPath).getName();

            // Use specific directory in cache to avoid clutter
            File videoCacheDir = new File(cacheDir, "videos");
            if (!videoCacheDir.exists()) {
                videoCacheDir.mkdirs();
            }

            File outFile = new File(videoCacheDir, fileName);

            // Check if file already exists and has content
            if (outFile.exists() && outFile.length() > 0) {
                return outFile;
            }

            // Copy from assets
            InputStream in = getAssets().open(assetPath);
            FileOutputStream out = new FileOutputStream(outFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }

            in.close();
            out.flush();
            out.close();

            Log.d(TAG, "Copy successful. Size: " + outFile.length());
            return outFile;
        } catch (IOException e) {
            Log.e(TAG, "Failed to copy asset file: " + assetPath, e);
            final String errorMsg = e.getMessage();
            runOnUiThread(() -> Toast
                    .makeText(this, "Asset Copy Failed: " + assetPath + "\n" + errorMsg, Toast.LENGTH_LONG).show());
            return null;
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        // LIVE TRACKING BUTTONS REMOVED - No longer available
    }

    /**
     * Start live tracking with this exercise - DISABLED
     */
    /*
    private void startLiveTracking() {
        try {
            // Launch LiveTrackingActivity for live tracking functionality
            Intent intent = new Intent(this, com.example.myrajourney.exercise.ui.LiveTrackingActivity.class);

            // Pass exercise data using keys expected by LiveTrackingActivity
            intent.putExtra("exercise_id", exerciseId);
            intent.putExtra("exercise_name", exerciseName);

            // Pass valid file path for MediaMetadataRetriever/ExoPlayer in
            // LiveTrackingActivity
            String videoUrl;
            File cacheDir = getCacheDir();
            File videoCacheDir = new File(cacheDir, "videos");
            String fileName = new File(videoPath).getName();
            File cachedFile = new File(videoCacheDir, fileName);

            if (cachedFile.exists() && cachedFile.length() > 0) {
                videoUrl = "file://" + cachedFile.getAbsolutePath();
                Log.d(TAG, "Passing cached file to LiveTrackingActivity: " + videoUrl);
            } else if (videoPath != null && !videoPath.startsWith("http")) {
                videoUrl = "file:///android_asset/" + videoPath;
            } else {
                videoUrl = videoPath;
            }

            intent.putExtra("video_url", videoUrl);

            Log.d(TAG, "Starting LiveTrackingActivity with video: " + videoUrl);
            startActivity(intent);
            finish(); // Release resources/decoder to avoid conflict with next activity

        } catch (Exception e) {
            Log.e(TAG, "Error starting live tracking", e);
            Toast.makeText(this, "Error starting live tracking: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    */

    @Override
    protected void onPause() {
        super.onPause();
        if (videoView != null && videoView.isPlaying()) {
            videoView.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (videoView != null) {
            videoView.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoView != null) {
            videoView.stopPlayback();
        }
    }
}