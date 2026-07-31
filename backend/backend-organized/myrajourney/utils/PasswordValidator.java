package com.example.myrajourney.utils;

import java.util.regex.Pattern;

/**
 * Enhanced password validation utility for MyRA Journey
 * Implements strong password requirements for medical app security
 */
public class PasswordValidator {
    
    // Password requirements: 8-16 characters, at least 1 uppercase, 1 lowercase, 1 special character
    private static final String PASSWORD_PATTERN = 
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,16}$";
    
    private static final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
    
    /**
     * Validate password against security requirements
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        
        return pattern.matcher(password).matches();
    }
    
    /**
     * Get password strength score (0-100)
     */
    public static int getPasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return 0;
        }
        
        int score = 0;
        
        // Length check
        if (password.length() >= 8) score += 25;
        if (password.length() >= 12) score += 10;
        
        // Character type checks
        if (password.matches(".*[a-z].*")) score += 20; // Lowercase
        if (password.matches(".*[A-Z].*")) score += 20; // Uppercase
        if (password.matches(".*[0-9].*")) score += 15; // Numbers
        if (password.matches(".*[@#$%^&+=!].*")) score += 20; // Special characters
        
        return Math.min(100, score);
    }
    
    /**
     * Get password validation message
     */
    public static String getPasswordValidationMessage(String password) {
        if (password == null || password.isEmpty()) {
            return "Password is required";
        }
        
        if (password.length() < 8) {
            return "Password must be at least 8 characters long";
        }
        
        if (password.length() > 16) {
            return "Password must not exceed 16 characters";
        }
        
        if (!password.matches(".*[a-z].*")) {
            return "Password must contain at least one lowercase letter";
        }
        
        if (!password.matches(".*[A-Z].*")) {
            return "Password must contain at least one uppercase letter";
        }
        
        if (!password.matches(".*[@#$%^&+=!].*")) {
            return "Password must contain at least one special character (@#$%^&+=!)";
        }
        
        if (password.contains(" ")) {
            return "Password must not contain spaces";
        }
        
        return "Password is valid";
    }
    
    /**
     * Get default password based on user role
     */
    public static String getDefaultPassword(String userRole) {
        switch (userRole.toLowerCase()) {
            case "patient":
                return "Welcome@123";
            case "doctor":
                return "Patrol@987";
            case "admin":
                return "AD@saveetha";
            default:
                return "Welcome@123";
        }
    }
    
    /**
     * Check if password needs to be changed (is default password)
     */
    public static boolean isDefaultPassword(String password, String userRole) {
        String defaultPassword = getDefaultPassword(userRole);
        return defaultPassword.equals(password);
    }
    
    /**
     * Generate password strength description
     */
    public static String getPasswordStrengthDescription(int strength) {
        if (strength < 30) {
            return "Weak";
        } else if (strength < 60) {
            return "Fair";
        } else if (strength < 80) {
            return "Good";
        } else {
            return "Strong";
        }
    }
    
    /**
     * Get password strength color for UI
     */
    public static int getPasswordStrengthColor(int strength) {
        if (strength < 30) {
            return 0xFFFF4444; // Red
        } else if (strength < 60) {
            return 0xFFFF8800; // Orange
        } else if (strength < 80) {
            return 0xFFFFBB33; // Yellow
        } else {
            return 0xFF00C851; // Green
        }
    }
}