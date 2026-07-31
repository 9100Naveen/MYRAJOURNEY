package com.example.myrajourney.utils;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Utility class to handle status bar and system UI for professional medical app appearance
 */
public class StatusBarUtils {
    
    /**
     * Setup professional status bar for medical app
     */
    public static void setupProfessionalStatusBar(Activity activity) {
        if (activity == null) return;
        
        Window window = activity.getWindow();
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Make status bar translucent
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            
            // Set light status bar for better visibility
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                View decorView = window.getDecorView();
                decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                );
            }
        }
        
        // Apply window insets to root view
        View rootView = activity.findViewById(android.R.id.content);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                // Get status bar height
                int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
                
                // Apply padding to avoid overlap
                v.setPadding(
                    v.getPaddingLeft(),
                    statusBarHeight,
                    v.getPaddingRight(),
                    v.getPaddingBottom()
                );
                
                return insets;
            });
        }
    }
    
    /**
     * Setup header view to avoid status bar overlap
     */
    public static void setupHeaderView(Activity activity, View headerView) {
        if (activity == null || headerView == null) return;
        
        ViewCompat.setOnApplyWindowInsetsListener(headerView, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            
            // Add top margin to header to avoid status bar overlap
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            params.topMargin = statusBarHeight + dpToPx(activity, 8); // Add 8dp extra spacing
            v.setLayoutParams(params);
            
            return insets;
        });
    }
    
    /**
     * Get status bar height
     */
    public static int getStatusBarHeight(Activity activity) {
        int result = 0;
        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = activity.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
    
    /**
     * Convert dp to pixels
     */
    public static int dpToPx(Activity activity, int dp) {
        float density = activity.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
    
    /**
     * Setup medical app theme colors
     */
    public static void setupMedicalAppTheme(Activity activity) {
        if (activity == null) return;
        
        Window window = activity.getWindow();
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Medical app color scheme
            window.setStatusBarColor(Color.parseColor("#1976D2")); // Medical blue
            window.setNavigationBarColor(Color.parseColor("#0D47A1")); // Darker blue
            
            // Light status bar icons for better contrast
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                View decorView = window.getDecorView();
                decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                );
            }
        }
    }
    
    /**
     * Hide system UI for immersive experience (for exercise sessions)
     */
    public static void hideSystemUI(Activity activity) {
        if (activity == null) return;
        
        View decorView = activity.getWindow().getDecorView();
        decorView.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_FULLSCREEN
        );
    }
    
    /**
     * Show system UI (restore normal view)
     */
    public static void showSystemUI(Activity activity) {
        if (activity == null) return;
        
        View decorView = activity.getWindow().getDecorView();
        decorView.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
    }
}