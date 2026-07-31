package com.example.myrajourney.rehab.ui;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.example.myrajourney.R;
import com.example.myrajourney.rehab.models.RAExercise;

import java.io.IOException;

/**
 * Local video player for exercise demonstrations using MP4 files stored in assets
 */
public class LocalVideoPlayer extends LinearLayout {
    
    private static final String TAG = "LocalVideoPlayer";
    
    private VideoView videoView;
    private ImageView animationView;
    private TextView txtVideoTitle;
    private TextView txtVideoInstructions;
    private TextView txtErrorMessage;
    private Button btnPlayPause;
    private Button btnRestart;
    private Button btnUseAnimation;
    private LinearLayout layoutVideoError;
    private LinearLayout layoutVideoControls;
    
    private RAExercise currentExercise;
    private boolean isVideoLoaded = false;
    private boolean isPlaying = false;
    
    public LocalVideoPlayer(Context context) {
        super(context);
        init();
    }
    
    public LocalVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        inflate(getContext(), R.layout.local_video_player_layout, this);
        initializeViews();
        setupVideoPlayer();
        setupListeners();
    }
    
    private void initializeViews() {
        videoView = findViewById(R.id.videoView);
        animationView = findViewById(R.id.animationView);
        txtVideoTitle = findViewById(R.id.txtVideoTitle);
        txtVideoInstructions = findViewById(R.id.txtVideoInstructions);
        txtErrorMessage = findViewById(R.id.txtErrorMessage);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnRestart = findViewById(R.id.btnRestart);
        btnUseAnimation = findViewById(R.id.btnUseAnimation);
        layoutVideoError = findViewById(R.id.layoutVideoError);
        layoutVideoControls = findViewById(R.id.layoutVideoControls);
    }
    
    private void setupVideoPlayer() {
        if (videoView != null) {
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    Log.d(TAG, "Video prepared successfully");
                    isVideoLoaded = true;
                    showVideoControls();
                    
                    // Set video to loop
                    mp.setLooping(true);
                    
                    // Auto-start the video
                    videoView.start();
                    isPlaying = true;
                    updatePlayPauseButton();
                }
            });
            
            videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Log.e(TAG, "Video error: what=" + what + ", extra=" + extra);
                    showVideoError("Video failed to load. Using animation instead.");
                    return true;
                }
            });
            
            videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    Log.d(TAG, "Video completed");
                    // Video will loop automatically due to setLooping(true)
                }
            });
        }
    }
    
    private void setupListeners() {
        if (btnPlayPause != null) {
            btnPlayPause.setOnClickListener(v -> togglePlayPause());
        }
        
        if (btnRestart != null) {
            btnRestart.setOnClickListener(v -> restartVideo());
        }
        
        if (btnUseAnimation != null) {
            btnUseAnimation.setOnClickListener(v -> showAnimation());
        }
    }
    
    /**
     * Load exercise with local video file
     */
    public void loadExercise(RAExercise exercise) {
        this.currentExercise = exercise;
        
        if (exercise == null) {
            showVideoError("No exercise selected");
            return;
        }
        
        Log.d(TAG, "Loading exercise: " + exercise.getName());
        
        // Update title and instructions
        if (txtVideoTitle != null) {
            txtVideoTitle.setText(exercise.getName() + " - Video Demonstration");
        }
        if (txtVideoInstructions != null) {
            txtVideoInstructions.setText("Follow the video demonstration for proper form and technique");
        }
        
        // Try to load local video file
        String videoFileName = getVideoFileName(exercise.getId());
        if (loadLocalVideo(videoFileName)) {
            Log.d(TAG, "Local video loaded: " + videoFileName);
        } else {
            Log.w(TAG, "Local video not found: " + videoFileName + ", showing animation");
            showAnimation();
        }
    }
    
    /**
     * Get video file name based on exercise ID
     */
    private String getVideoFileName(String exerciseId) {
        switch (exerciseId) {
            case "ex_001": return "ex_001_wrist_flexion.mp4";
            case "ex_002": return "ex_002_wrist_rotation.mp4";
            case "ex_003": return "ex_003_thumb_opposition.mp4";
            case "ex_004": return "ex_004_thumb_flexion.mp4";
            case "ex_005": return "ex_005_finger_flexion.mp4";
            case "ex_006": return "ex_006_finger_extension.mp4";
            case "ex_007": return "ex_007_finger_pinch.mp4";
            case "ex_008": return "ex_008_knee_flexion.mp4";
            case "ex_009": return "ex_009_hip_flexion.mp4";
            case "ex_010": return "ex_010_hip_abduction.mp4";
            default: return null;
        }
    }
    
    /**
     * Load local video file from assets
     */
    private boolean loadLocalVideo(String fileName) {
        if (fileName == null || videoView == null) {
            return false;
        }
        
        try {
            // Create URI for asset file
            Uri videoUri = Uri.parse("android.resource://" + getContext().getPackageName() + "/raw/" + 
                                   fileName.replace(".mp4", ""));
            
            Log.d(TAG, "Attempting to load video: " + videoUri);
            
            videoView.setVideoURI(videoUri);
            showVideoView();
            
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Error loading local video: " + fileName, e);
            
            // Try alternative approach with assets
            try {
                String assetPath = "android.asset://videos/" + fileName;
                Uri assetUri = Uri.parse(assetPath);
                videoView.setVideoURI(assetUri);
                showVideoView();
                return true;
                
            } catch (Exception e2) {
                Log.e(TAG, "Error loading video from assets: " + fileName, e2);
                return false;
            }
        }
    }
    
    /**
     * Show video view and hide others
     */
    private void showVideoView() {
        if (videoView != null) {
            videoView.setVisibility(View.VISIBLE);
        }
        if (animationView != null) {
            animationView.setVisibility(View.GONE);
        }
        if (layoutVideoError != null) {
            layoutVideoError.setVisibility(View.GONE);
        }
    }
    
    /**
     * Show video controls
     */
    private void showVideoControls() {
        if (layoutVideoControls != null) {
            layoutVideoControls.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * Toggle play/pause
     */
    private void togglePlayPause() {
        if (videoView != null && isVideoLoaded) {
            if (isPlaying) {
                videoView.pause();
                isPlaying = false;
            } else {
                videoView.start();
                isPlaying = true;
            }
            updatePlayPauseButton();
        }
    }
    
    /**
     * Restart video from beginning
     */
    private void restartVideo() {
        if (videoView != null && isVideoLoaded) {
            videoView.seekTo(0);
            videoView.start();
            isPlaying = true;
            updatePlayPauseButton();
        }
    }
    
    /**
     * Update play/pause button text
     */
    private void updatePlayPauseButton() {
        if (btnPlayPause != null) {
            btnPlayPause.setText(isPlaying ? "⏸️ Pause" : "▶️ Play");
        }
    }
    
    /**
     * Show animation fallback
     */
    private void showAnimation() {
        if (videoView != null) {
            videoView.setVisibility(View.GONE);
        }
        if (layoutVideoControls != null) {
            layoutVideoControls.setVisibility(View.GONE);
        }
        if (layoutVideoError != null) {
            layoutVideoError.setVisibility(View.GONE);
        }
        
        if (animationView != null && currentExercise != null) {
            animationView.setVisibility(View.VISIBLE);
            
            // Load animation based on exercise
            int animationResource = getAnimationResource(currentExercise.getId());
            if (animationResource != 0) {
                animationView.setImageResource(animationResource);
            }
        }
    }
    
    /**
     * Get animation resource for exercise
     */
    private int getAnimationResource(String exerciseId) {
        switch (exerciseId) {
            case "ex_001": return R.drawable.animation_wrist_flex;
            case "ex_002": return R.drawable.animation_wrist_rotation;
            case "ex_003": return R.drawable.animation_thumb_opposition;
            case "ex_004": return R.drawable.thumb;
            case "ex_005": return R.drawable.animation_finger_flex;
            case "ex_006": return R.drawable.animation_finger_extension;
            case "ex_007": return R.drawable.finger;
            case "ex_008": return R.drawable.animation_knee_flex;
            case "ex_009": return R.drawable.glute;
            case "ex_010": return R.drawable.glute;
            default: return 0;
        }
    }
    
    /**
     * Show error message with alternatives
     */
    private void showVideoError(String errorMessage) {
        if (videoView != null) {
            videoView.setVisibility(View.GONE);
        }
        if (layoutVideoControls != null) {
            layoutVideoControls.setVisibility(View.GONE);
        }
        if (animationView != null) {
            animationView.setVisibility(View.GONE);
        }
        if (layoutVideoError != null) {
            layoutVideoError.setVisibility(View.VISIBLE);
        }
        
        StringBuilder fullMessage = new StringBuilder();
        fullMessage.append("🎥 ").append(errorMessage).append("\n\n");
        
        if (currentExercise != null) {
            fullMessage.append("📺 Exercise: ").append(currentExercise.getName()).append("\n\n");
            
            // Add exercise instructions
            if (currentExercise.getInstructions() != null) {
                fullMessage.append("📋 Instructions:\n");
                for (int i = 0; i < currentExercise.getInstructions().size(); i++) {
                    fullMessage.append((i + 1)).append(". ").append(currentExercise.getInstructions().get(i)).append("\n");
                }
                fullMessage.append("\n");
            }
            
            // Add benefits
            if (currentExercise.getRaSpecificBenefits() != null) {
                fullMessage.append("💪 Benefits:\n");
                for (String benefit : currentExercise.getRaSpecificBenefits()) {
                    fullMessage.append("• ").append(benefit).append("\n");
                }
            }
        }
        
        if (txtErrorMessage != null) {
            txtErrorMessage.setText(fullMessage.toString());
        }
    }
    
    /**
     * Pause video when view is not visible
     */
    public void pauseVideo() {
        if (videoView != null && isPlaying) {
            videoView.pause();
            isPlaying = false;
            updatePlayPauseButton();
        }
    }
    
    /**
     * Resume video when view becomes visible
     */
    public void resumeVideo() {
        if (videoView != null && isVideoLoaded && !isPlaying) {
            videoView.start();
            isPlaying = true;
            updatePlayPauseButton();
        }
    }
    
    /**
     * Release video resources
     */
    public void releaseVideo() {
        if (videoView != null) {
            videoView.stopPlayback();
        }
        isVideoLoaded = false;
        isPlaying = false;
    }
}