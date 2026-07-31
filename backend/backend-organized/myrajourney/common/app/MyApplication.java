package com.example.myrajourney.common.app;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;

// ✅ FIX: Import ThemeManager
import com.example.myrajourney.core.ui.ThemeManager;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Apply theme globally when app starts
        ThemeManager.applyTheme(this);
    }
}