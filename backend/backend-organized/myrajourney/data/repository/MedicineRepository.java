package com.example.myrajourney.data.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Comprehensive medicine repository for Rheumatoid Arthritis medications
 * Contains detailed information about RA drugs for quick doctor access
 */
public class MedicineRepository {
    
    private static MedicineRepository instance;
    private List<Medicine> medicines;
    
    private MedicineRepository() {
        initializeMedicineDatabase();
    }
    
    public static synchronized MedicineRepository getInstance() {
        if (instance == null) {
            instance = new MedicineRepository();
        }
        return instance;
    }
    
    /**
     * Initialize comprehensive RA medicine database
     */
    private void initializeMedicineDatabase() {
        medicines = new ArrayList<>();
        
        // DMARDs (Disease-Modifying Antirheumatic Drugs)
        medicines.add(new Medicine("Methotrexate", "DMARD", "10-25mg", "Weekly", 
            "Take with folic acid. Monitor liver function.", "Tablet/Injection"));
        
        medicines.add(new Medicine("Sulfasalazine", "DMARD", "500mg-2g", "Morning & Night", 
            "Take with food. Monitor blood counts.", "Tablet"));
        
        medicines.add(new Medicine("Leflunomide", "DMARD", "10-20mg", "Daily", 
            "Monitor liver function. Avoid pregnancy.", "Tablet"));
        
        medicines.add(new Medicine("Hydroxychloroquine", "DMARD", "200-400mg", "Daily", 
            "Regular eye exams required. Take with food.", "Tablet"));
        
        // Biologics
        medicines.add(new Medicine("Adalimumab (Humira)", "Biologic", "40mg", "Every 2 weeks", 
            "Subcutaneous injection. Monitor for infections.", "Injection"));
        
        medicines.add(new Medicine("Etanercept (Enbrel)", "Biologic", "25-50mg", "Weekly/Twice weekly", 
            "Subcutaneous injection. Avoid live vaccines.", "Injection"));
        
        medicines.add(new Medicine("Infliximab (Remicade)", "Biologic", "3-10mg/kg", "Every 6-8 weeks", 
            "IV infusion. Premedication required.", "IV Infusion"));
        
        medicines.add(new Medicine("Rituximab (MabThera)", "Biologic", "1000mg", "Every 6 months", 
            "IV infusion. Monitor B-cell counts.", "IV Infusion"));
        
        medicines.add(new Medicine("Tocilizumab (Actemra)", "Biologic", "8mg/kg", "Every 4 weeks", 
            "IV infusion or subcutaneous. Monitor cholesterol.", "Injection/IV"));
        
        // JAK Inhibitors
        medicines.add(new Medicine("Tofacitinib (Xeljanz)", "JAK Inhibitor", "5mg", "Twice daily", 
            "Monitor blood counts and liver function.", "Tablet"));
        
        medicines.add(new Medicine("Baricitinib (Olumiant)", "JAK Inhibitor", "2-4mg", "Daily", 
            "Monitor for thrombosis risk.", "Tablet"));
        
        // NSAIDs
        medicines.add(new Medicine("Ibuprofen", "NSAID", "400-800mg", "3 times daily", 
            "Take with food. Monitor kidney function.", "Tablet"));
        
        medicines.add(new Medicine("Naproxen", "NSAID", "250-500mg", "Twice daily", 
            "Take with food. Lower GI risk than other NSAIDs.", "Tablet"));
        
        medicines.add(new Medicine("Diclofenac", "NSAID", "50mg", "2-3 times daily", 
            "Take with food. Monitor cardiovascular risk.", "Tablet"));
        
        medicines.add(new Medicine("Celecoxib (Celebrex)", "COX-2 Inhibitor", "100-200mg", "Daily/Twice daily", 
            "Lower GI risk. Monitor cardiovascular effects.", "Capsule"));
        
        // Corticosteroids
        medicines.add(new Medicine("Prednisolone", "Corticosteroid", "5-60mg", "Daily (morning)", 
            "Taper gradually. Monitor bone density.", "Tablet"));
        
        medicines.add(new Medicine("Methylprednisolone", "Corticosteroid", "4-48mg", "Daily", 
            "Short-term use preferred. Monitor glucose.", "Tablet"));
        
        medicines.add(new Medicine("Hydrocortisone", "Corticosteroid", "10-30mg", "Daily (divided)", 
            "Physiological replacement dose.", "Tablet"));
        
        // Supplements and Supportive Medications
        medicines.add(new Medicine("Folic Acid", "Supplement", "5mg", "Weekly (day after MTX)", 
            "Reduces methotrexate side effects.", "Tablet"));
        
        medicines.add(new Medicine("Calcium + Vitamin D", "Supplement", "1000mg + 800IU", "Daily", 
            "Bone protection with steroids.", "Tablet"));
        
        medicines.add(new Medicine("Vitamin D3", "Supplement", "1000-4000IU", "Daily", 
            "Monitor 25(OH)D levels.", "Tablet/Drops"));
        
        medicines.add(new Medicine("Omega-3 Fish Oil", "Supplement", "1-3g", "Daily", 
            "Anti-inflammatory effects. Take with meals.", "Capsule"));
        
        // Pain Management
        medicines.add(new Medicine("Paracetamol", "Analgesic", "500mg-1g", "Up to 4 times daily", 
            "Safe with most RA medications. Max 4g/day.", "Tablet"));
        
        medicines.add(new Medicine("Tramadol", "Opioid Analgesic", "50-100mg", "Up to 4 times daily", 
            "For severe pain. Monitor for dependence.", "Tablet"));
        
        medicines.add(new Medicine("Codeine", "Opioid Analgesic", "15-60mg", "Up to 4 times daily", 
            "Short-term use. Constipation common.", "Tablet"));
        
        // Gastroprotection
        medicines.add(new Medicine("Omeprazole", "PPI", "20-40mg", "Daily (morning)", 
            "Gastroprotection with NSAIDs/steroids.", "Capsule"));
        
        medicines.add(new Medicine("Lansoprazole", "PPI", "15-30mg", "Daily (morning)", 
            "Take before meals. Long-term monitoring needed.", "Capsule"));
        
        medicines.add(new Medicine("Ranitidine", "H2 Blocker", "150mg", "Twice daily", 
            "Alternative to PPIs for gastroprotection.", "Tablet"));
        
        // Topical Preparations
        medicines.add(new Medicine("Diclofenac Gel", "Topical NSAID", "1%", "3-4 times daily", 
            "Apply to affected joints. Lower systemic absorption.", "Gel"));
        
        medicines.add(new Medicine("Capsaicin Cream", "Topical Analgesic", "0.025-0.075%", "3-4 times daily", 
            "May cause initial burning sensation.", "Cream"));
        
        // Newer Medications
        medicines.add(new Medicine("Upadacitinib (Rinvoq)", "JAK Inhibitor", "15mg", "Daily", 
            "Extended-release tablet. Monitor lipids.", "Tablet"));
        
        medicines.add(new Medicine("Filgotinib (Jyseleca)", "JAK Inhibitor", "100-200mg", "Daily", 
            "Monitor for male fertility effects.", "Tablet"));
        
        medicines.add(new Medicine("Sarilumab (Kevzara)", "Biologic", "150-200mg", "Every 2 weeks", 
            "IL-6 receptor antagonist. Monitor neutrophils.", "Injection"));
    }
    
    /**
     * Search medicines by name or category
     */
    public List<Medicine> searchMedicines(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>(medicines);
        }
        
        String searchQuery = query.toLowerCase().trim();
        
        return medicines.stream()
            .filter(medicine -> 
                medicine.getName().toLowerCase().contains(searchQuery) ||
                medicine.getCategory().toLowerCase().contains(searchQuery) ||
                medicine.getInstructions().toLowerCase().contains(searchQuery)
            )
            .collect(Collectors.toList());
    }
    
    /**
     * Get medicines by category
     */
    public List<Medicine> getMedicinesByCategory(String category) {
        return medicines.stream()
            .filter(medicine -> medicine.getCategory().equalsIgnoreCase(category))
            .collect(Collectors.toList());
    }
    
    /**
     * Get all medicine categories
     */
    public List<String> getAllCategories() {
        return medicines.stream()
            .map(Medicine::getCategory)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }
    
    /**
     * Get medicine by exact name
     */
    public Medicine getMedicineByName(String name) {
        return medicines.stream()
            .filter(medicine -> medicine.getName().equalsIgnoreCase(name))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Get all medicines
     */
    public List<Medicine> getAllMedicines() {
        return new ArrayList<>(medicines);
    }
    
    /**
     * Medicine data class
     */
    public static class Medicine {
        private String name;
        private String category;
        private String dosage;
        private String frequency;
        private String instructions;
        private String formulation;
        
        public Medicine(String name, String category, String dosage, String frequency, 
                       String instructions, String formulation) {
            this.name = name;
            this.category = category;
            this.dosage = dosage;
            this.frequency = frequency;
            this.instructions = instructions;
            this.formulation = formulation;
        }
        
        // Getters
        public String getName() { return name; }
        public String getCategory() { return category; }
        public String getDosage() { return dosage; }
        public String getFrequency() { return frequency; }
        public String getInstructions() { return instructions; }
        public String getFormulation() { return formulation; }
        
        public String getDisplayText() {
            return name + " | " + dosage + " | " + frequency;
        }
        
        public String getDetailedInfo() {
            return "Category: " + category + "\n" +
                   "Dosage: " + dosage + "\n" +
                   "Frequency: " + frequency + "\n" +
                   "Form: " + formulation + "\n" +
                   "Instructions: " + instructions;
        }
    }
}