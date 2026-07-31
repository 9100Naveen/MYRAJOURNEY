package com.example.myrajourney.core.navigation;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.myrajourney.admin.dashboard.AdminDashboardActivity;
import com.example.myrajourney.doctor.dashboard.DoctorDashboardActivity;
import com.example.myrajourney.auth.LoginActivity;
import com.example.myrajourney.auth.OnboardingActivity;
import com.example.myrajourney.patient.dashboard.PatientDashboardActivity;

public final class NavigationManager {

    private static final String TAG = "NavigationManager";

    private NavigationManager() {}

    public static void goToOnboarding(@NonNull Context context) {
        Intent i = new Intent(context, OnboardingActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(i);
    }

    public static void goToLogin(@NonNull Context context) {
        Intent i = new Intent(context, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(i);
    }

    public static void goToDashboardForRole(@NonNull Context context, String role) {

        if (role == null) {
            Log.e(TAG, "Null role â†’ Sending to Login!!");
            goToLogin(context);
            return;
        }

        Intent intent;
        switch (role.toLowerCase()) {
            case "patient":
                intent = new Intent(context, PatientDashboardActivity.class);
                break;
            case "doctor":
                intent = new Intent(context, DoctorDashboardActivity.class);
                break;
            case "admin":
                intent = new Intent(context, AdminDashboardActivity.class);
                break;
            default:
                Log.e(TAG, "Unknown role: " + role);
                goToLogin(context);
                return;
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
}






