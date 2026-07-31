package com.example.myrajourney.auth;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myrajourney.core.navigation.NavigationManager;
import com.example.myrajourney.core.network.NetworkTestRunner;
import com.example.myrajourney.core.network.SymptomTestHelper;
import com.example.myrajourney.core.session.SessionManager;
import com.example.myrajourney.R;

public class SplashActivity extends AppCompatActivity {

    private static final int DELAY = 1200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(this::decide, DELAY);
    }

    private void decide() {
        SessionManager sm = SessionManager.getInstance(this);

        // Run network diagnostics in background to help identify issues
        NetworkTestRunner.runImmediateDiagnostics(this);
        NetworkTestRunner.testSpecificEndpoint(this, "auth/me");
        
        // Test symptom endpoints specifically (since that's where the error occurs)
        SymptomTestHelper.testSymptomRetrieval(this);

        // 1. Check Onboarding - If not done, go there first.
        if (!sm.isOnboardingCompleted()) {
            NavigationManager.goToOnboarding(this);
            finish();
            return;
        }

        // 2. FORCE LOGOUT - This wipes the saved token/user data every single launch.
        sm.logout();

        // 3. Navigate to Login - User must enter credentials again.
        NavigationManager.goToLogin(this);
        finish();
    }
}