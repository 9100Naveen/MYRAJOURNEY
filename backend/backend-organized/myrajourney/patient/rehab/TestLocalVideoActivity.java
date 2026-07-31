package com.example.myrajourney.patient.rehab;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myrajourney.R;

/**
 * Test activity to verify local video functionality
 */
public class TestLocalVideoActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_local_video);
        
        LinearLayout buttonContainer = findViewById(R.id.button_container);
        
        // Create test buttons for each exercise video
        String[] exercises = {
            "ex_001:Wrist Flexion",
            "ex_002:Wrist Rotation", 
            "ex_003:Thumb Opposition",
            "ex_004:Thumb Flexion",
            "ex_005:Finger Flexion",
            "ex_006:Finger Extension",
            "ex_007:Finger Pinch",
            "ex_008:Knee Flexion",
            "ex_009:Hip Flexion",
            "ex_010:Hip Abduction"
        };
        
        for (String exercise : exercises) {
            String[] parts = exercise.split(":");
            String exerciseId = parts[0];
            String exerciseName = parts[1];
            
            Button testButton = new Button(this);
            testButton.setText("Test " + exerciseName);
            testButton.setOnClickListener(v -> testLocalVideo(exerciseId, exerciseName));
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(16, 8, 16, 8);
            testButton.setLayoutParams(params);
            
            buttonContainer.addView(testButton);
        }
    }
    
    private void testLocalVideo(String exerciseId, String exerciseName) {
        try {
            String videoPath = getLocalVideoPath(exerciseId);
            if (videoPath != null) {
                Intent intent = new Intent(this, LocalVideoPlayerActivity.class);
                intent.putExtra("video_path", videoPath);
                intent.putExtra("exercise_name", exerciseName);
                intent.putExtra("exercise_description", "Test video for " + exerciseName + " exercise");
                intent.putExtra("exercise_id", exerciseId);
                intent.putExtra("exercise_category", "Test Category");
                
                startActivity(intent);
            } else {
                Toast.makeText(this, "Video not found for " + exerciseName, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private String getLocalVideoPath(String exerciseId) {
        switch (exerciseId) {
            case "ex_001": return "exercise_videos/ex_001_wrist_flexion.mp4";
            case "ex_002": return "exercise_videos/ex_002_wrist_rotation.mp4";
            case "ex_003": return "exercise_videos/ex_003_thumb_opposition.mp4";
            case "ex_004": return "exercise_videos/ex_004_thumb_flexion.mp4";
            case "ex_005": return "exercise_videos/ex_005_finger_flexion.mp4";
            case "ex_006": return "exercise_videos/ex_006_finger_extension.mp4";
            case "ex_007": return "exercise_videos/ex_007_finger_pinch.mp4";
            case "ex_008": return "exercise_videos/ex_008_knee_flexion.mp4";
            case "ex_009": return "exercise_videos/ex_009_hip_flexion.mp4";
            case "ex_010": return "exercise_videos/ex_010_hip_abduction.mp4";
            default: return null;
        }
    }
}