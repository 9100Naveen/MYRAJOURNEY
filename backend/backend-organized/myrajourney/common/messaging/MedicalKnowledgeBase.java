package com.example.myrajourney.common.messaging;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Arrays;

/**
 * Comprehensive medical knowledge base for RA-specific information
 * Contains evidence-based medical information for generating accurate responses
 */
public class MedicalKnowledgeBase {
    
    private Map<String, MedicationInfo> medications;
    private Map<String, SymptomManagement> symptomManagement;
    private Map<String, ExerciseGuideline> exerciseGuidelines;
    private Map<String, NutritionAdvice> nutritionAdvice;
    private Map<String, EmergencyProtocol> emergencyProtocols;
    
    public MedicalKnowledgeBase() {
        this.medications = new HashMap<>();
        this.symptomManagement = new HashMap<>();
        this.exerciseGuidelines = new HashMap<>();
        this.nutritionAdvice = new HashMap<>();
        this.emergencyProtocols = new HashMap<>();
    }
    
    /**
     * Load RA medication information
     */
    public void loadRAMedications() {
        // DMARDs (Disease-Modifying Antirheumatic Drugs)
        medications.put("methotrexate", new MedicationInfo(
            "Methotrexate",
            "DMARD",
            Arrays.asList("Nausea (take with food)", "Fatigue", "Mouth sores", "Hair thinning", "Liver function changes"),
            "Take as soon as remembered unless it's almost time for next dose. Never double dose.",
            "Take with folic acid to reduce side effects. Monitor liver function regularly.",
            Arrays.asList("Folic acid supplementation", "Regular blood tests", "Avoid alcohol")
        ));
        
        medications.put("sulfasalazine", new MedicationInfo(
            "Sulfasalazine",
            "DMARD",
            Arrays.asList("Nausea", "Headache", "Rash", "Orange urine (harmless)", "Decreased sperm count"),
            "Take missed dose as soon as remembered unless close to next dose.",
            "Take with food to reduce stomach upset. Drink plenty of fluids.",
            Arrays.asList("Take with food", "Stay hydrated", "Regular blood monitoring")
        ));
        
        medications.put("leflunomide", new MedicationInfo(
            "Leflunomide",
            "DMARD",
            Arrays.asList("Diarrhea", "Hair loss", "Rash", "Liver problems", "High blood pressure"),
            "Take missed dose as soon as remembered. Do not double dose.",
            "Monitor blood pressure and liver function regularly.",
            Arrays.asList("Regular BP monitoring", "Liver function tests", "Avoid pregnancy")
        ));
        
        // Biologics
        medications.put("adalimumab", new MedicationInfo(
            "Adalimumab (Humira)",
            "Biologic",
            Arrays.asList("Injection site reactions", "Upper respiratory infections", "Headache", "Rash"),
            "Take missed injection as soon as remembered, then resume regular schedule.",
            "Rotate injection sites. Store in refrigerator. Watch for signs of infection.",
            Arrays.asList("Infection monitoring", "Proper injection technique", "Refrigerate medication")
        ));
        
        medications.put("etanercept", new MedicationInfo(
            "Etanercept (Enbrel)",
            "Biologic",
            Arrays.asList("Injection site reactions", "Infections", "Headache", "Dizziness"),
            "Take missed injection as soon as remembered within 3 days.",
            "Allow medication to reach room temperature before injection.",
            Arrays.asList("Room temperature injection", "Infection precautions", "Proper storage")
        ));
        
        // NSAIDs
        medications.put("ibuprofen", new MedicationInfo(
            "Ibuprofen",
            "NSAID",
            Arrays.asList("Stomach upset", "Heartburn", "Dizziness", "Kidney problems", "High blood pressure"),
            "Take missed dose as soon as remembered unless close to next dose.",
            "Take with food to protect stomach. Use lowest effective dose.",
            Arrays.asList("Take with food", "Monitor blood pressure", "Kidney function checks")
        ));
        
        // Corticosteroids
        medications.put("prednisone", new MedicationInfo(
            "Prednisone",
            "Corticosteroid",
            Arrays.asList("Weight gain", "Mood changes", "High blood sugar", "Bone thinning", "Increased infection risk"),
            "Take missed dose as soon as remembered unless close to next dose.",
            "Take with food. Do not stop suddenly - must taper gradually.",
            Arrays.asList("Take with food", "Gradual tapering", "Bone density monitoring", "Blood sugar monitoring")
        ));
    }
    
    /**
     * Load symptom management strategies
     */
    public void loadSymptomManagement() {
        symptomManagement.put("joint_pain", new SymptomManagement(
            "Joint Pain",
            Arrays.asList(
                "Apply heat for stiffness (heating pad, warm bath)",
                "Apply cold for acute inflammation (ice pack 15-20 minutes)",
                "Gentle range-of-motion exercises",
                "Take prescribed pain medications as directed",
                "Rest affected joints during flares"
            ),
            Arrays.asList("Sudden severe pain", "Signs of infection", "Loss of function"),
            "Monitor pain levels daily. Contact doctor if pain suddenly worsens or doesn't respond to usual treatments."
        ));
        
        symptomManagement.put("morning_stiffness", new SymptomManagement(
            "Morning Stiffness",
            Arrays.asList(
                "Take warm shower or bath upon waking",
                "Gentle stretching exercises in bed",
                "Allow extra time for morning routine",
                "Take morning medications with breakfast",
                "Use electric blanket or heating pad"
            ),
            Arrays.asList("Stiffness lasting more than 2 hours", "Inability to move joints"),
            "Morning stiffness is common in RA. Duration and severity can indicate disease activity."
        ));
        
        symptomManagement.put("fatigue", new SymptomManagement(
            "Fatigue",
            Arrays.asList(
                "Pace activities throughout the day",
                "Take short rest breaks between activities",
                "Prioritize important tasks",
                "Maintain regular sleep schedule",
                "Light exercise as tolerated"
            ),
            Arrays.asList("Extreme exhaustion", "Inability to perform daily activities"),
            "RA fatigue is different from normal tiredness. It's related to inflammation and disease activity."
        ));
    }
    
    /**
     * Load exercise guidelines
     */
    public void loadExerciseGuidelines() {
        exerciseGuidelines.put("low_impact_cardio", new ExerciseGuideline(
            "Low-Impact Cardiovascular Exercise",
            Arrays.asList("Walking", "Swimming", "Cycling", "Water aerobics"),
            "Start with 10-15 minutes, gradually increase to 30 minutes most days",
            Arrays.asList("Reduces inflammation", "Improves cardiovascular health", "Maintains joint mobility"),
            Arrays.asList("Stop if sharp pain occurs", "Avoid during active flares", "Stay hydrated")
        ));
        
        exerciseGuidelines.put("strength_training", new ExerciseGuideline(
            "Strength Training",
            Arrays.asList("Light weights", "Resistance bands", "Bodyweight exercises"),
            "2-3 times per week, 8-12 repetitions per exercise",
            Arrays.asList("Maintains muscle mass", "Supports joint stability", "Improves bone density"),
            Arrays.asList("Use proper form", "Start with light resistance", "Avoid exercises that cause pain")
        ));
        
        exerciseGuidelines.put("flexibility", new ExerciseGuideline(
            "Flexibility and Range of Motion",
            Arrays.asList("Gentle stretching", "Yoga", "Tai chi", "Joint mobility exercises"),
            "Daily, hold stretches for 15-30 seconds",
            Arrays.asList("Maintains joint flexibility", "Reduces stiffness", "Improves function"),
            Arrays.asList("Never force a stretch", "Warm up before stretching", "Stop if pain increases")
        ));
    }
    
    /**
     * Load nutrition advice
     */
    public void loadNutritionAdvice() {
        nutritionAdvice.put("anti_inflammatory", new NutritionAdvice(
            "Anti-Inflammatory Foods",
            Arrays.asList("Fatty fish (salmon, mackerel)", "Olive oil", "Leafy greens", "Berries", "Nuts and seeds"),
            Arrays.asList("Reduce inflammation", "Support immune system", "Improve overall health"),
            "Include 2-3 servings of anti-inflammatory foods daily"
        ));
        
        nutritionAdvice.put("foods_to_limit", new NutritionAdvice(
            "Foods to Limit",
            Arrays.asList("Processed foods", "Refined sugars", "Trans fats", "Excessive red meat", "High-sodium foods"),
            Arrays.asList("May increase inflammation", "Can worsen RA symptoms"),
            "Limit these foods and focus on whole, unprocessed options"
        ));
    }
    
    /**
     * Load emergency protocols
     */
    public void loadEmergencyProtocols() {
        emergencyProtocols.put("severe_flare", new EmergencyProtocol(
            "Severe RA Flare",
            Arrays.asList("Sudden onset of severe joint pain", "Multiple joints affected", "Fever with joint symptoms"),
            Arrays.asList("Contact rheumatologist immediately", "Take prescribed rescue medications", "Apply ice to swollen joints", "Rest affected joints"),
            "Call doctor's office or after-hours line immediately"
        ));
        
        emergencyProtocols.put("medication_reaction", new EmergencyProtocol(
            "Severe Medication Reaction",
            Arrays.asList("Difficulty breathing", "Severe rash or hives", "Swelling of face/throat", "Severe nausea/vomiting"),
            Arrays.asList("Stop medication immediately", "Call 911 if breathing difficulty", "Contact doctor immediately", "Go to emergency room"),
            "Seek immediate medical attention for severe reactions"
        ));
    }
    
    // Getter methods for accessing knowledge base information
    public String getSideEffects(String medicationName) {
        MedicationInfo info = medications.get(medicationName.toLowerCase());
        if (info != null) {
            return String.join("\n• ", info.getSideEffects());
        }
        return "Consult your doctor or pharmacist for specific side effect information.";
    }
    
    public String getMissedDoseAdvice(String medicationName) {
        MedicationInfo info = medications.get(medicationName.toLowerCase());
        if (info != null) {
            return info.getMissedDoseAdvice();
        }
        return "Contact your healthcare provider for missed dose instructions.";
    }
    
    public String getGeneralSideEffects() {
        return "Common RA medication side effects include:\n" +
               "• Nausea and stomach upset\n" +
               "• Fatigue\n" +
               "• Increased infection risk\n" +
               "• Liver function changes\n" +
               "• Injection site reactions (for biologics)";
    }
    
    // Data classes for knowledge base entries
    public static class MedicationInfo {
        private String name;
        private String category;
        private List<String> sideEffects;
        private String missedDoseAdvice;
        private String generalAdvice;
        private List<String> precautions;
        
        public MedicationInfo(String name, String category, List<String> sideEffects, 
                            String missedDoseAdvice, String generalAdvice, List<String> precautions) {
            this.name = name;
            this.category = category;
            this.sideEffects = sideEffects;
            this.missedDoseAdvice = missedDoseAdvice;
            this.generalAdvice = generalAdvice;
            this.precautions = precautions;
        }
        
        // Getters
        public String getName() { return name; }
        public String getCategory() { return category; }
        public List<String> getSideEffects() { return sideEffects; }
        public String getMissedDoseAdvice() { return missedDoseAdvice; }
        public String getGeneralAdvice() { return generalAdvice; }
        public List<String> getPrecautions() { return precautions; }
    }
    
    public static class SymptomManagement {
        private String symptomName;
        private List<String> managementStrategies;
        private List<String> warningSigns;
        private String additionalInfo;
        
        public SymptomManagement(String symptomName, List<String> managementStrategies, 
                               List<String> warningSigns, String additionalInfo) {
            this.symptomName = symptomName;
            this.managementStrategies = managementStrategies;
            this.warningSigns = warningSigns;
            this.additionalInfo = additionalInfo;
        }
        
        // Getters
        public String getSymptomName() { return symptomName; }
        public List<String> getManagementStrategies() { return managementStrategies; }
        public List<String> getWarningSigns() { return warningSigns; }
        public String getAdditionalInfo() { return additionalInfo; }
    }
    
    public static class ExerciseGuideline {
        private String exerciseType;
        private List<String> examples;
        private String frequency;
        private List<String> benefits;
        private List<String> safetyTips;
        
        public ExerciseGuideline(String exerciseType, List<String> examples, String frequency, 
                               List<String> benefits, List<String> safetyTips) {
            this.exerciseType = exerciseType;
            this.examples = examples;
            this.frequency = frequency;
            this.benefits = benefits;
            this.safetyTips = safetyTips;
        }
        
        // Getters
        public String getExerciseType() { return exerciseType; }
        public List<String> getExamples() { return examples; }
        public String getFrequency() { return frequency; }
        public List<String> getBenefits() { return benefits; }
        public List<String> getSafetyTips() { return safetyTips; }
    }
    
    public static class NutritionAdvice {
        private String category;
        private List<String> foods;
        private List<String> benefits;
        private String recommendation;
        
        public NutritionAdvice(String category, List<String> foods, List<String> benefits, String recommendation) {
            this.category = category;
            this.foods = foods;
            this.benefits = benefits;
            this.recommendation = recommendation;
        }
        
        // Getters
        public String getCategory() { return category; }
        public List<String> getFoods() { return foods; }
        public List<String> getBenefits() { return benefits; }
        public String getRecommendation() { return recommendation; }
    }
    
    public static class EmergencyProtocol {
        private String situationType;
        private List<String> warningSigns;
        private List<String> immediateActions;
        private String contactInstructions;
        
        public EmergencyProtocol(String situationType, List<String> warningSigns, 
                               List<String> immediateActions, String contactInstructions) {
            this.situationType = situationType;
            this.warningSigns = warningSigns;
            this.immediateActions = immediateActions;
            this.contactInstructions = contactInstructions;
        }
        
        // Getters
        public String getSituationType() { return situationType; }
        public List<String> getWarningSigns() { return warningSigns; }
        public List<String> getImmediateActions() { return immediateActions; }
        public String getContactInstructions() { return contactInstructions; }
    }
}