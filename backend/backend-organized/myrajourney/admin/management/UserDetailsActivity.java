package com.example.myrajourney.admin.management;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myrajourney.R;

/**
 * Activity for viewing user details
 * TODO: Implement full user details view
 */
public class UserDetailsActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);
        
        String userId = getIntent().getStringExtra("user_id");
        
        // TODO: Implement user details view
        Toast.makeText(this, "User details functionality will be implemented", Toast.LENGTH_SHORT).show();
        
        // For now, just finish the activity
        finish();
    }
}