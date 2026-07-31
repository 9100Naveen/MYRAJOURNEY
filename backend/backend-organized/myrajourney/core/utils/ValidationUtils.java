package com.example.myrajourney.core.utils;

import android.text.TextUtils;
import android.util.Patterns;
import java.util.regex.Pattern;

public class ValidationUtils {
    
    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    
    public static boolean isValidPhone(String phone) {
        if (TextUtils.isEmpty(phone)) return false;
        String phonePattern = "^[+]?[0-9]{10,15}$";
        return Pattern.matches(phonePattern, phone.replaceAll("\\s", ""));
    }
    
    public static boolean isValidPassword(String password) {
        return !TextUtils.isEmpty(password) && password.length() >= 6;
    }
    
    public static boolean isValidAge(String age) {
        if (TextUtils.isEmpty(age)) return false;
        try {
            int ageInt = Integer.parseInt(age);
            return ageInt > 0 && ageInt <= 120;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    public static boolean isValidName(String name) {
        return !TextUtils.isEmpty(name) && name.trim().length() >= 2;
    }
    
    public static boolean isValidAddress(String address) {
        return !TextUtils.isEmpty(address) && address.trim().length() >= 10;
    }
    
    public static String getValidationError(String field, String value) {
        switch (field.toLowerCase()) {
            case "email":
                return isValidEmail(value) ? null : "Please enter a valid email address";
            case "phone":
            case "mobile":
                return isValidPhone(value) ? null : "Please enter a valid phone number";
            case "password":
                return isValidPassword(value) ? null : "Password must be at least 6 characters";
            case "age":
                return isValidAge(value) ? null : "Please enter a valid age (1-120)";
            case "name":
                return isValidName(value) ? null : "Name must be at least 2 characters";
            case "address":
                return isValidAddress(value) ? null : "Address must be at least 10 characters";
            default:
                return TextUtils.isEmpty(value) ? "This field is required" : null;
        }
    }
}






