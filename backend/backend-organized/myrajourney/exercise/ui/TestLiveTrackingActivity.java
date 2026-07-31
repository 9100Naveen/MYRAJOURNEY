package com.example.myrajourney.exercise.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myrajourney.R;

/**
 * Simple test activity to launch LiveTrackingActivity with test data
 */
public class TestLiveTrackingActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_live_tracking);
        
        Button testButton = findViewById(R.id.test_live_tracking_button);
        testButton.setOnClickListener(v -> startLiveTracking());
    }
    
    private void startLiveTracking() {
        Intent intent = new Intent(this, LiveTrackingActivity.class);
        
        // Add test data
        intent.putExtra("exercise_id", "test_wrist_flexion");
        intent.putExtra("exercise_name", "Wrist Flexion/Extension");
        // Don't provide video_url to test the fallback mechanism
        
        startActivity(intent);
        
        Toast.makeText(this, "Starting live tracking test...", Toast.LENGTH_SHORT).show();
    }
}