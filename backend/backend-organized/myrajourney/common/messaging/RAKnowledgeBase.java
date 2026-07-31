package com.example.myrajourney.common.messaging;

import java.util.HashMap;
import java.util.Map;

/**
 * Knowledge base for RA-specific information
 */
public class RAKnowledgeBase {
    
    private Map<String, String> medicationInfo;
    private Map<String, String> exerciseInfo;
    private Map<String, String> symptomInfo;
    
    public RAKnowledgeBase() {
        initializeMedicationInfo();
        initializeExerciseInfo();
        initializeSymptomInfo();
    }
    
    private void initializeMedicationInfo() {
        medicationInfo = new HashMap<>();
        medicationInfo.put("methotrexate", "A DMARD that helps slow RA progression. Take weekly with folic acid.");
        medicationInfo.put("humira", "A biologic that targets TNF-alpha. Injection every other week.");
        medicationInfo.put("prednisone", "A corticosteroid for inflammation control. Take with food.");
        medicationInfo.put("ibuprofen", "An NSAID for pain and inflammation. Take with food to protect stomach.");
        medicationInfo.put("naproxen", "A long-acting NSAID. Take with food, monitor for stomach issues.");
    }
    
    private void initializeExerciseInfo() {
        exerciseInfo = new HashMap<>();
        exerciseInfo.put("range_of_motion", "Gentle movements to maintain joint flexibility. Do daily.");
        exerciseInfo.put("strengthening", "Light resistance exercises 2-3 times per week.");
        exerciseInfo.put("aerobic", "Low-impact cardio like walking or swimming. 150 minutes per week.");
        exerciseInfo.put("flexibility", "Stretching exercises to maintain joint mobility.");
    }
    
    private void initializeSymptomInfo() {
        symptomInfo = new HashMap<>();
        symptomInfo.put("morning_stiffness", "Common RA symptom. Usually lasts 30+ minutes in RA vs <30 in OA.");
        symptomInfo.put("joint_swelling", "Sign of active inflammation. Track and report to doctor.");
        symptomInfo.put("fatigue", "Very common in RA. Can be managed with proper treatment and lifestyle.");
        symptomInfo.put("pain", "Rate 1-10 daily. Track patterns to help optimize treatment.");
    }
    
    public String getMedicationInfo(String medication) {
        return medicationInfo.getOrDefault(medication.toLowerCase(), 
            "Consult your doctor or pharmacist for specific medication information.");
    }
    
    public String getExerciseInfo(String exerciseType) {
        return exerciseInfo.getOrDefault(exerciseType.toLowerCase(),
            "Consult your physical therapist for specific exercise guidance.");
    }
    
    public String getSymptomInfo(String symptom) {
        return symptomInfo.getOrDefault(symptom.toLowerCase(),
            "Track this symptom and discuss with your healthcare provider.");
    }
}