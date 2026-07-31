package com.example.myrajourney.core.session;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF = "mj_session_prefs";

    private static final String KEY_ONBOARD = "onboarding_done";
    private static final String KEY_NAME = "user_name";
    private static final String KEY_EMAIL = "user_email";
    private static final String KEY_ROLE = "user_role";
    private static final String KEY_USER_ID = "user_id";   // ⭐ ADDED
    private static final String KEY_PROFILE_PICTURE = "profile_picture";   // ⭐ ADDED

    private final SharedPreferences prefs;
    private final Context appContext;
    
    private static SessionManager instance;

    private SessionManager(Context context) {
        this.appContext = context.getApplicationContext();
        this.prefs = appContext.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }
    
    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
        return instance;
    }

    // --- ONBOARDING ---
    public void setOnboardingCompleted(boolean done) {
        prefs.edit().putBoolean(KEY_ONBOARD, done).apply();
    }

    public boolean isOnboardingCompleted() {
        return prefs.getBoolean(KEY_ONBOARD, false);
    }

    // --- SESSION MANAGEMENT ---
    // ⭐ UPDATED: Now stores user_id too
    public void createSession(String name, String email, String role, String userId) {
        prefs.edit()
                .putString(KEY_NAME, name)
                .putString(KEY_EMAIL, email)
                .putString(KEY_ROLE, role)
                .putString(KEY_USER_ID, userId)   // ⭐ ADDED
                .apply();
    }

    // Backward compatibility for old calls without userId
    public void createSession(String name, String email, String role) {
        prefs.edit()
                .putString(KEY_NAME, name)
                .putString(KEY_EMAIL, email)
                .putString(KEY_ROLE, role)
                .apply();
    }

    // Check if Token exists via TokenManager
    public boolean isSessionValid() {
        return TokenManager.getInstance(appContext).getToken() != null;
    }

    public boolean isLoggedIn() {
        return isSessionValid();
    }

    // --- GETTERS ---
    public String getRole() {
        return prefs.getString(KEY_ROLE, null);
    }

    public String getUserName() {
        return prefs.getString(KEY_NAME, "User");
    }

    public String getUserEmail() {
        return prefs.getString(KEY_EMAIL, "");
    }

    // ⭐ ADDED: Get logged-in user ID (critical for doctor/patient flows)
    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }
    
    // Get current user as User object
    public com.example.myrajourney.data.model.User getCurrentUser() {
        String userId = getUserId();
        String name = getUserName();
        String email = getUserEmail();
        String role = getRole();
        
        if (userId != null && name != null && email != null && role != null) {
            return new com.example.myrajourney.data.model.User(userId, name, email, role);
        }
        return null;
    }
    
    // ⭐ ADDED: Profile picture methods
    public void setProfilePicture(String fileName) {
        prefs.edit().putString(KEY_PROFILE_PICTURE, fileName).apply();
    }
    
    public String getProfilePicture() {
        return prefs.getString(KEY_PROFILE_PICTURE, null);
    }

    // --- LOGOUT ---
    public void logout() {
        boolean onboard = isOnboardingCompleted();

        // Clear all session data
        prefs.edit().clear().apply();

        // Clear Auth Token
        TokenManager.getInstance(appContext).clear();

        // Restore onboarding status
        setOnboardingCompleted(onboard);
    }
}
