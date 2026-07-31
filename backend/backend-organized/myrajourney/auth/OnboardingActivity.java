package com.example.myrajourney.auth;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

// ✅ FIX 1: Import R
import com.example.myrajourney.R;
// ✅ FIX 2: Import OnboardingItem model
import com.example.myrajourney.common.models.OnboardingItem;
// ✅ FIX 3: Import OnboardingAdapter
import com.example.myrajourney.common.adapters.OnboardingAdapter;

import com.example.myrajourney.core.navigation.NavigationManager;
import com.example.myrajourney.core.session.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 pager;
    private Button nextButton;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        session = SessionManager.getInstance(this);

        pager = findViewById(R.id.viewPager);
        nextButton = findViewById(R.id.btnNext);

        setup();

        nextButton.setOnClickListener(v -> {
            if (pager.getCurrentItem() + 1 < pager.getAdapter().getItemCount()) {
                pager.setCurrentItem(pager.getCurrentItem() + 1);
            } else {
                finishOnboard();
            }
        });
    }

    private void finishOnboard() {
        session.setOnboardingCompleted(true);
        NavigationManager.goToLogin(this);
        finish();
    }

    private void setup() {
        List<OnboardingItem> items = new ArrayList<>();
        items.add(new OnboardingItem(R.drawable.about1, "Secure Access", "Access safely."));
        items.add(new OnboardingItem(R.drawable.about2, "Track Symptoms", "Manage RA easily."));
        items.add(new OnboardingItem(R.drawable.about3, "Insights", "Analytics at a glance."));

        pager.setAdapter(new OnboardingAdapter(items));

        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int pos) {
                nextButton.setText((pos == items.size() - 1) ? "Get Started" : "Next");
            }
        });
    }
}