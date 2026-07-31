package com.example.myrajourney.admin.management;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myrajourney.R;

/**
 * Activity for adding new users (doctors/patients)
 * TODO: Implement full user creation functionality
 */
public class AddUserActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);
        
        String userType = getIntent().getStringExtra("user_type");
        
        // TODO: Implement user creation form
        Toast.makeText(this, "Add " + userType + " functionality will be implemented", Toast.LENGTH_SHORT).show();
        
        // For now, just finish the activity
        finish();
    }
}