package com.example.myrajourney.utils;

import android.text.TextUtils;
import java.util.regex.Pattern;

/**
 * Utility class for input validation
 * Provides common validation methods for forms and user input
 */
public class ValidationUtils {
    
    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    // Name pattern (letters only - no numbers or special characters)
    private static final Pattern NAME_PATTERN = Pattern.compile(
        "^[a-zA-Z\\s]{2,50}$"
    );
    
    // Mobile number pattern (exactly 10 digits)
    private static final Pattern MOBILE_PATTERN = Pattern.compile(
        "^[0-9]{10}$"
    );
    
    // Gmail email pattern (must end with @gmail.com)
    private static final Pattern GMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@gmail\\.com$"
    );
    
    // Password pattern (at least 8 chars, uppercase, lowercase, special char)
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,16}$"
    );
    
    /**
     * Validate email address (must be @gmail.com)
     */
    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && GMAIL_PATTERN.matcher(email).matches();
    }
    
    /**
     * Validate mobile number (exactly 10 digits)
     */
    public static boolean isValidMobile(String mobile) {
        if (TextUtils.isEmpty(mobile)) return false;
        
        // Remove spaces, dashes, parentheses
        String cleanMobile = mobile.replaceAll("[\\s\\-\\(\\)]", "");
        return MOBILE_PATTERN.matcher(cleanMobile).matches();
    }
    
    /**
     * Validate phone number (legacy method - redirects to mobile validation)
     */
    public static boolean isValidPhone(String phone) {
        return isValidMobile(phone);
    }
    
    /**
     * Validate password strength
     */
    public static boolean isValidPassword(String password) {
        return !TextUtils.isEmpty(password) && PASSWORD_PATTERN.matcher(password).matches();
    }
    
    /**
     * Validate name (letters and spaces only - no numbers or special characters)
     */
    public static boolean isValidName(String name) {
        return !TextUtils.isEmpty(name) && NAME_PATTERN.matcher(name.trim()).matches();
    }
    
    /**
     * Validate date of birth year (must be reasonable - between 1900 and current year)
     * User must be at least 18 years old
     */
    public static boolean isValidBirthYear(int year) {
        int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        int age = currentYear - year;
        return year >= 1900 && year <= currentYear && age >= 18;
    }
    
    /**
     * Validate age based on birth year (must be at least 18)
     */
    public static boolean isValidAgeFromYear(int birthYear) {
        int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        int age = currentYear - birthYear;
        return age >= 18 && age <= 120;
    }
    
    /**
     * Validate age directly (must be at least 18)
     */
    public static boolean isValidAge(int age) {
        return age >= 18 && age <= 120;
    }
    
    /**
     * Validate age from string (must be at least 18)
     */
    public static boolean isValidAge(String ageStr) {
        try {
            int age = Integer.parseInt(ageStr.trim());
            return isValidAge(age);
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Check if string is not empty after trimming
     */
    public static boolean isNotEmpty(String text) {
        return !TextUtils.isEmpty(text) && !text.trim().isEmpty();
    }
    
    /**
     * Validate minimum length
     */
    public static boolean hasMinLength(String text, int minLength) {
        return !TextUtils.isEmpty(text) && text.trim().length() >= minLength;
    }
    
    /**
     * Validate maximum length
     */
    public static boolean hasMaxLength(String text, int maxLength) {
        return TextUtils.isEmpty(text) || text.trim().length() <= maxLength;
    }
    
    /**
     * Validate length range
     */
    public static boolean hasValidLength(String text, int minLength, int maxLength) {
        return hasMinLength(text, minLength) && hasMaxLength(text, maxLength);
    }
    
    /**
     * Get password strength score (0-4)
     */
    public static int getPasswordStrength(String password) {
        if (TextUtils.isEmpty(password)) return 0;
        
        int score = 0;
        
        // Length check
        if (password.length() >= 8) score++;
        
        // Uppercase check
        if (password.matches(".*[A-Z].*")) score++;
        
        // Lowercase check
        if (password.matches(".*[a-z].*")) score++;
        
        // Number check
        if (password.matches(".*[0-9].*")) score++;
        
        // Special character check
        if (password.matches(".*[@#$%^&+=!].*")) score++;
        
        return Math.min(score, 4);
    }
    
    /**
     * Get password strength description
     */
    public static String getPasswordStrengthDescription(String password) {
        int strength = getPasswordStrength(password);
        
        switch (strength) {
            case 0:
            case 1:
                return "Very Weak";
            case 2:
                return "Weak";
            case 3:
                return "Good";
            case 4:
                return "Strong";
            default:
                return "Unknown";
        }
    }
    
    /**
     * Validate email format and provide specific error message (must be @gmail.com)
     */
    public static String validateEmailWithMessage(String email) {
        if (TextUtils.isEmpty(email)) {
            return "Email is required";
        }
        
        if (!email.toLowerCase().endsWith("@gmail.com")) {
            return "Email must end with @gmail.com";
        }
        
        if (!isValidEmail(email)) {
            return "Please enter a valid Gmail address";
        }
        
        return null; // Valid
    }
    
    /**
     * Validate mobile number and provide specific error message (exactly 10 digits)
     */
    public static String validateMobileWithMessage(String mobile) {
        if (TextUtils.isEmpty(mobile)) {
            return "Mobile number is required";
        }
        
        String cleanMobile = mobile.replaceAll("[\\s\\-\\(\\)]", "");
        
        if (cleanMobile.length() != 10) {
            return "Mobile number must be exactly 10 digits";
        }
        
        if (!cleanMobile.matches("^[0-9]+$")) {
            return "Mobile number can only contain digits";
        }
        
        return null; // Valid
    }
    
    /**
     * Validate address/place and provide specific error message
     */
    public static String validateAddressWithMessage(String address) {
        if (TextUtils.isEmpty(address)) {
            return "Address is required";
        }
        
        String trimmedAddress = address.trim();
        
        if (trimmedAddress.isEmpty()) {
            return "Address cannot be empty";
        }
        
        if (trimmedAddress.length() < 5) {
            return "Address must be at least 5 characters";
        }
        
        if (trimmedAddress.length() > 200) {
            return "Address must be no more than 200 characters";
        }
        
        return null; // Valid
    }
    
    /**
     * Validate password and provide specific error message
     */
    public static String validatePasswordWithMessage(String password) {
        if (TextUtils.isEmpty(password)) {
            return "Password is required";
        }
        
        if (password.length() < 8) {
            return "Password must be at least 8 characters";
        }
        
        if (password.length() > 16) {
            return "Password must be no more than 16 characters";
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
        
        return null; // Valid
    }
    
    /**
     * Validate name and provide specific error message (letters only)
     */
    public static String validateNameWithMessage(String name) {
        if (TextUtils.isEmpty(name)) {
            return "Name is required";
        }
        
        String trimmedName = name.trim();
        
        if (trimmedName.length() < 2) {
            return "Name must be at least 2 characters";
        }
        
        if (trimmedName.length() > 50) {
            return "Name must be no more than 50 characters";
        }
        
        if (!NAME_PATTERN.matcher(trimmedName).matches()) {
            return "Name can only contain letters and spaces (no numbers or special characters)";
        }
        
        return null; // Valid
    }
    
    /**
     * Validate date of birth year and provide specific error message (must be at least 18 years old)
     */
    public static String validateBirthYearWithMessage(int year) {
        int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        
        if (year < 1900) {
            return "Birth year cannot be before 1900";
        }
        
        if (year > currentYear) {
            return "Birth year cannot be in the future";
        }
        
        int age = currentYear - year;
        if (age < 18) {
            return "You must be at least 18 years old to register";
        }
        
        if (age > 120) {
            return "Age cannot be more than 120 years";
        }
        
        return null; // Valid
    }
    
    /**
     * Validate date of birth year from string (must be at least 18 years old)
     */
    public static String validateBirthYearWithMessage(String yearStr) {
        if (TextUtils.isEmpty(yearStr)) {
            return "Birth year is required";
        }
        
        try {
            int year = Integer.parseInt(yearStr.trim());
            return validateBirthYearWithMessage(year);
        } catch (NumberFormatException e) {
            return "Please enter a valid year";
        }
    }
    
    /**
     * Validate age and provide specific error message (must be at least 18)
     */
    public static String validateAgeWithMessage(String ageStr) {
        if (TextUtils.isEmpty(ageStr)) {
            return "Age is required";
        }
        
        try {
            int age = Integer.parseInt(ageStr.trim());
            if (age < 18) {
                return "You must be at least 18 years old to register";
            }
            if (age > 120) {
                return "Age cannot be more than 120 years";
            }
            return null; // Valid
        } catch (NumberFormatException e) {
            return "Please enter a valid age";
        }
    }
    
    /**
     * Validate phone and provide specific error message (redirects to mobile validation)
     */
    public static String validatePhoneWithMessage(String phone) {
        return validateMobileWithMessage(phone);
    }
    
    /**
     * Validate role-based password requirements
     */
    public static String validateRolePasswordWithMessage(String password, String role) {
        if (TextUtils.isEmpty(password)) {
            return "Password is required";
        }
        
        String expectedPassword = "";
        switch (role.toUpperCase()) {
            case "DOCTOR":
                expectedPassword = "Patrol@987";
                break;
            case "PATIENT":
                expectedPassword = "Welcome@456";
                break;
            case "ADMIN":
                expectedPassword = "Admini@765";
                break;
            default:
                return "Invalid user role";
        }
        
        if (!password.equals(expectedPassword)) {
            return "Password must be: " + expectedPassword;
        }
        
        return null; // Valid
    }
    
    /**
     * Get expected password for role
     */
    public static String getExpectedPasswordForRole(String role) {
        switch (role.toUpperCase()) {
            case "DOCTOR":
                return "Patrol@987";
            case "PATIENT":
                return "Welcome@456";
            case "ADMIN":
                return "Admini@765";
            default:
                return "";
        }
    }
}