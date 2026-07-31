import SwiftUI

struct AssignMedicationsView: View {
    let patient: Patient
    @Environment(\.dismiss) private var dismiss
    
    // Form fields
    @State private var dosage = ""
    @State private var frequency = ""
    @State private var reason = ""
    @State private var duration = ""
    
    @State private var isMorning = false
    @State private var isAfternoon = false
    @State private var isNight = false
    
    @State private var foodRelation: String? = nil
    
    // Selection
    @State private var selectedMedicationIds: Set<Int> = []
    @State private var isSaving = false
    @State private var showSuccessToast = false
    
    let availableMedications = [
        (id: 1, name: "Methotrexate", defaultDosage: "15mg", defaultFrequency: "1x daily"),
        (id: 2, name: "Hydroxychloroquine", defaultDosage: "200mg", defaultFrequency: "2x daily"),
        (id: 3, name: "Sulfasalazine", defaultDosage: "500mg", defaultFrequency: "2x daily"),
        (id: 4, name: "Leflunomide", defaultDosage: "20mg", defaultFrequency: "1x daily"),
        (id: 5, name: "Adalimumab", defaultDosage: "40mg", defaultFrequency: "1x daily"),
        (id: 6, name: "Etanercept", defaultDosage: "50mg", defaultFrequency: "1x daily"),
        (id: 7, name: "Infliximab", defaultDosage: "5mg", defaultFrequency: "1x daily"),
        (id: 8, name: "Rituximab", defaultDosage: "1000mg", defaultFrequency: "1x daily"),
        (id: 9, name: "Tocilizumab", defaultDosage: "8mg", defaultFrequency: "1x daily"),
        (id: 10, name: "Prednisolone", defaultDosage: "5mg", defaultFrequency: "1x daily"),
        (id: 11, name: "Prednisone", defaultDosage: "10mg", defaultFrequency: "1x daily"),
        (id: 12, name: "Dexamethasone", defaultDosage: "2mg", defaultFrequency: "1x daily"),
        (id: 13, name: "Methylprednisolone", defaultDosage: "4mg", defaultFrequency: "1x daily"),
        (id: 14, name: "Hydrocortisone", defaultDosage: "10mg", defaultFrequency: "2x daily"),
        (id: 15, name: "Deflazacort", defaultDosage: "3mg", defaultFrequency: "1x daily"),
        (id: 16, name: "Betamethasone", defaultDosage: "0.25mg", defaultFrequency: "1x daily"),
        (id: 17, name: "Triamcinolone", defaultDosage: "2mg", defaultFrequency: "1x daily"),
        (id: 18, name: "Budesonide", defaultDosage: "3mg", defaultFrequency: "1x daily"),
        (id: 19, name: "Cortisone", defaultDosage: "12.5mg", defaultFrequency: "1x daily"),
        (id: 20, name: "Ibuprofen", defaultDosage: "400mg", defaultFrequency: "3x daily"),
        (id: 21, name: "Naproxen", defaultDosage: "250mg", defaultFrequency: "2x daily"),
        (id: 22, name: "Diclofenac", defaultDosage: "50mg", defaultFrequency: "2x daily"),
        (id: 23, name: "Celecoxib", defaultDosage: "100mg", defaultFrequency: "2x daily"),
        (id: 24, name: "Meloxicam", defaultDosage: "7.5mg", defaultFrequency: "1x daily"),
        (id: 25, name: "Indomethacin", defaultDosage: "25mg", defaultFrequency: "3x daily"),
        (id: 26, name: "Aspirin", defaultDosage: "75mg", defaultFrequency: "1x daily"),
        (id: 27, name: "Paracetamol", defaultDosage: "500mg", defaultFrequency: "3x daily"),
        (id: 28, name: "Tramadol", defaultDosage: "50mg", defaultFrequency: "2x daily"),
        (id: 29, name: "Codeine", defaultDosage: "30mg", defaultFrequency: "2x daily"),
        (id: 30, name: "Folic Acid", defaultDosage: "5mg", defaultFrequency: "1x daily"),
        (id: 31, name: "Calcium", defaultDosage: "500mg", defaultFrequency: "2x daily"),
        (id: 32, name: "Vitamin D", defaultDosage: "1000IU", defaultFrequency: "1x daily"),
        (id: 33, name: "Omeprazole", defaultDosage: "20mg", defaultFrequency: "1x daily"),
        (id: 34, name: "Lansoprazole", defaultDosage: "15mg", defaultFrequency: "1x daily"),
        (id: 35, name: "Ranitidine", defaultDosage: "150mg", defaultFrequency: "2x daily")
    ]
    
    var body: some View {
        ZStack {
            Color(red: 0.96, green: 0.96, blue: 0.96).ignoresSafeArea()
            
            ScrollView {
                VStack(alignment: .leading, spacing: 0) {
                    // Integrated Header
                    HStack {
                        Button(action: { dismiss() }) {
                            HStack(spacing: 4) {
                                Image(systemName: "arrow.left")
                                Text("BACK")
                                    .fontWeight(.bold)
                            }
                            .foregroundColor(.black)
                        }
                        
                        Spacer()
                        
                        Text("Assign Medications")
                            .font(.system(size: 18, weight: .bold))
                        
                        Spacer()
                        
                        // Small spacer to balance the back button width
                        Color.clear.frame(width: 40)
                    }
                    .padding(.horizontal)
                    .padding(.vertical, 12)
                    .background(Color.white)
                    
                    VStack(alignment: .leading, spacing: 15) {
                        // Patient ID Header
                        Text("Assigning medications to Patient ID: \(patient.id)")
                            .font(.system(size: 14, weight: .medium))
                            .padding(.vertical, 10)
                            .padding(.horizontal)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .background(Color.white)
                            .padding(.bottom, 10)
                        
                        // Custom Dosage & Frequency
                        VStack(alignment: .leading, spacing: 10) {
                            Text("Custom Dosage & Frequency (Optional)")
                                .font(.system(size: 15, weight: .bold))
                            
                            Text("Leave empty to use default values for each medication")
                                .font(.system(size: 12))
                                .foregroundColor(.secondary)
                            
                            HStack(spacing: 12) {
                                TextField("Dosage (e.g. 500mg)", text: $dosage)
                                    .padding(12)
                                    .background(Color.white)
                                    .cornerRadius(4)
                                
                                TextField("Times daily (e.g. 3)", text: $frequency)
                                    .padding(12)
                                    .background(Color.white)
                                    .cornerRadius(4)
                            }
                        }
                        .padding(.horizontal)
                        
                        // Reason for Medication
                        TextField("Reason for Medication (e.g. for pain / swelling)", text: $reason)
                            .padding(12)
                            .background(Color.white)
                            .cornerRadius(4)
                            .padding(.horizontal)
                        
                        // Duration
                        TextField("Duration (e.g. for 5 days / Ongoing)", text: $duration)
                            .padding(12)
                            .background(Color.white)
                            .cornerRadius(4)
                            .padding(.horizontal)
                        
                        // Intake Time
                        VStack(alignment: .leading, spacing: 10) {
                            Text("Intake Time (Select all that apply)")
                                .font(.system(size: 14, weight: .bold))
                            
                            HStack(spacing: 20) {
                                CheckboxView(isSelected: $isMorning, label: "Morning")
                                CheckboxView(isSelected: $isAfternoon, label: "Afternoon")
                                CheckboxView(isSelected: $isNight, label: "Night")
                            }
                        }
                        .padding(.horizontal)
                        
                        // Food Relation
                        VStack(alignment: .leading, spacing: 10) {
                            Text("Food Relation")
                                .font(.system(size: 14, weight: .bold))
                            
                            HStack(spacing: 30) {
                                RadioButtonView(isSelected: foodRelation == "Before Food", label: "Before Food") {
                                    foodRelation = "Before Food"
                                }
                                RadioButtonView(isSelected: foodRelation == "After Food", label: "After Food") {
                                    foodRelation = "After Food"
                                }
                            }
                        }
                        .padding(.horizontal)
                        
                        // Select Medications
                        VStack(alignment: .leading, spacing: 10) {
                            Text("Select Medications to Assign")
                                .font(.system(size: 15, weight: .bold))
                            
                            VStack(spacing: 1) {
                                ForEach(availableMedications, id: \.id) { med in
                                    MedicationSelectRow(
                                        name: med.name,
                                        detail: "(\(med.defaultDosage), \(med.defaultFrequency))",
                                        isSelected: selectedMedicationIds.contains(med.id)
                                    ) {
                                        if selectedMedicationIds.contains(med.id) {
                                            selectedMedicationIds.remove(med.id)
                                        } else {
                                            selectedMedicationIds.insert(med.id)
                                        }
                                    }
                                }
                            }
                            .background(Color.white)
                        }
                        .padding(.horizontal)
                        
                        // Assign Button
                        Button(action: saveAssignments) {
                            if isSaving {
                                ProgressView().tint(.black)
                                    .padding(14)
                                    .frame(maxWidth: .infinity)
                                    .background(Color(red: 0.85, green: 0.85, blue: 0.85))
                            } else {
                                Text("SELECT MEDICATIONS TO ASSIGN")
                                    .font(.system(size: 15, weight: .bold))
                                    .foregroundColor(.black.opacity(0.8))
                                    .padding(14)
                                    .frame(maxWidth: .infinity)
                                    .background(Color(red: 0.85, green: 0.85, blue: 0.85))
                            }
                        }
                        .padding(.horizontal)
                        .padding(.top, 10)
                        
                        // Instructions
                        VStack(alignment: .leading, spacing: 2) {
                            Text("Instructions:")
                                .font(.system(size: 13, weight: .bold))
                            Text("• Check the medications you want to assign")
                                .font(.system(size: 11))
                            Text("• Use custom dosage/frequency or leave empty for defaults")
                                .font(.system(size: 11))
                            Text("• Duplicate medications will be skipped automatically")
                                .font(.system(size: 11))
                        }
                        .foregroundColor(.secondary)
                        .padding(.horizontal)
                        .padding(.bottom, 30)
                    }
                }
            }
            
            // Toast Overlay
            if showSuccessToast {
                VStack {
                    Spacer()
                    HStack {
                        Image(systemName: "checkmark.circle.fill")
                        Text("API Response: 200")
                            .fontWeight(.bold)
                    }
                    .padding()
                    .background(Color.black.opacity(0.8))
                    .foregroundColor(.white)
                    .cornerRadius(12)
                    .padding(.bottom, 50)
                }
                .transition(.move(edge: .bottom).combined(with: .opacity))
            }
        }
        .navigationBarHidden(true)
    }
    
    private func saveAssignments() {
        guard !selectedMedicationIds.isEmpty else { return }
        
        isSaving = true
        
        // Simulating multiple API calls since the backend seems to expect one medication at a time 
        // based on PatientService.assignMedication
        
        let dispatchGroup = DispatchGroup()
        
        for medId in selectedMedicationIds {
            if let med = availableMedications.first(where: { $0.id == medId }) {
                dispatchGroup.enter()
                
                let params: [String: Any] = [
                    "patient_id": patient.id,
                    "medication_name": med.name,
                    "dosage": dosage.isEmpty ? med.defaultDosage : dosage,
                    "instructions": reason,
                    "duration": duration,
                    "is_morning": isMorning ? 1 : 0,
                    "is_afternoon": isAfternoon ? 1 : 0,
                    "is_night": isNight ? 1 : 0,
                    "food_relation": foodRelation ?? "With food",
                    "frequency_per_day": Int(frequency) ?? (med.defaultFrequency.contains("2x") ? 2 : (med.defaultFrequency.contains("3x") ? 3 : 1)),
                    "start_date": DateFormatter.backendDateFormatter.string(from: Date())
                ]
                
                PatientService.shared.assignMedication(parameters: params) { _ in
                    dispatchGroup.leave()
                }
            }
        }
        
        dispatchGroup.notify(queue: .main) {
            isSaving = false
            withAnimation {
                showSuccessToast = true
            }
            DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                withAnimation {
                    showSuccessToast = false
                }
                dismiss()
            }
        }
    }
}

struct CheckboxView: View {
    @Binding var isSelected: Bool
    let label: String
    
    var body: some View {
        Button(action: { isSelected.toggle() }) {
            HStack(spacing: 8) {
                Image(systemName: isSelected ? "checkmark.square.fill" : "square")
                    .foregroundColor(isSelected ? .blue : .gray)
                Text(label)
                    .foregroundColor(.black)
            }
        }
    }
}

struct RadioButtonView: View {
    let isSelected: Bool
    let label: String
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            HStack(spacing: 8) {
                Image(systemName: isSelected ? "largecircle.fill.circle" : "circle")
                    .foregroundColor(isSelected ? .blue : .gray)
                Text(label)
                    .foregroundColor(.black)
            }
        }
    }
}

struct MedicationSelectRow: View {
    let name: String
    let detail: String
    let isSelected: Bool
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            HStack {
                Image(systemName: isSelected ? "checkmark.square.fill" : "square")
                    .foregroundColor(isSelected ? .blue : .gray)
                
                Text(name)
                    .foregroundColor(.black)
                
                Text(detail)
                    .foregroundColor(.gray)
                    .font(.system(size: 14))
                
                Spacer()
            }
            .padding()
            .background(Color.white)
        }
    }
}

extension DateFormatter {
    static var backendDateFormatter: DateFormatter {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        return formatter
    }
}

#Preview {
    AssignMedicationsView(patient: Patient(
        id: 89,
        name: "Test Patient",
        email: "test@example.com",
        phone: nil,
        age: 30,
        gender: "Male",
        address: nil,
        role: "PATIENT",
        createdAt: nil,
        medicalId: nil,
        assignedDoctorId: 1
    ))
}
