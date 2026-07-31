import SwiftUI

struct NewMedicationView: View {
    @Environment(\.dismiss) private var dismiss
    @State private var name = ""
    @State private var dosage = ""
    @State private var frequency = "1"
    @State private var instructions = ""
    @State private var duration = ""
    @State private var foodRelation = "With food"
    
    @State private var isMorning = true
    @State private var isAfternoon = false
    @State private var isNight = true
    
    @State private var startDate = Date()
    @State private var isSaving = false
    @State private var errorMessage: String?
    
    var patientId: Int? = nil
    var onSaveComplete: () -> Void
    
    let foodOptions = ["With food", "Before food", "After food", "Empty stomach"]
    
    var body: some View {
        NavigationView {
            Form {
                Section(header: Text("Medication Details")) {
                    TextField("Medication Name", text: $name)
                        .autocapitalization(.words)
                    TextField("Dosage (e.g. 500mg)", text: $dosage)
                    TextField("Frequency (times per day)", text: $frequency)
                        .keyboardType(.numberPad)
                }
                
                Section(header: Text("Timing")) {
                    Toggle("Morning", isOn: $isMorning)
                    Toggle("Afternoon", isOn: $isAfternoon)
                    Toggle("Night", isOn: $isNight)
                    
                    Picker("Relation to Food", selection: $foodRelation) {
                        ForEach(foodOptions, id: \.self) { option in
                            Text(option).tag(option)
                        }
                    }
                }
                
                Section(header: Text("Additional Info")) {
                    TextField("Duration (e.g. 7 days)", text: $duration)
                    TextField("Instructions (Optional)", text: $instructions)
                    DatePicker("Start Date", selection: $startDate, displayedComponents: .date)
                }
                
                if let error = errorMessage {
                    Section {
                        Text(error)
                            .foregroundColor(.red)
                            .font(.callout)
                    }
                }
                
                Section {
                    Button(action: saveMedication) {
                        if isSaving {
                            HStack {
                                Spacer()
                                ProgressView()
                                Spacer()
                            }
                        } else {
                            Text("Add Medication")
                                .frame(maxWidth: .infinity)
                                .fontWeight(.bold)
                                .foregroundColor(.white)
                                .padding(.vertical, 8)
                        }
                    }
                    .listRowBackground(name.isEmpty || isSaving ? Color.gray : Color.blue)
                    .disabled(name.isEmpty || isSaving)
                }
            }
            .navigationTitle("New Medication")
            .navigationBarItems(leading: Button("Cancel") { dismiss() })
        }
    }
    
    private func saveMedication() {
        isSaving = true
        errorMessage = nil
        
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "YYYY-MM-dd"
        
        let finalPatientId = patientId ?? Int(SessionManager.shared.userId ?? "0") ?? 0
        
        let parameters: [String: Any] = [
            "patient_id": finalPatientId,
            "medication_name": name,
            "dosage": dosage,
            "instructions": instructions,
            "duration": duration,
            "is_morning": isMorning ? 1 : 0,
            "is_afternoon": isAfternoon ? 1 : 0,
            "is_night": isNight ? 1 : 0,
            "food_relation": foodRelation,
            "frequency_per_day": Int(frequency) ?? 1,
            "start_date": dateFormatter.string(from: startDate)
        ]
        
        PatientService.shared.assignMedication(parameters: parameters) { result in
            DispatchQueue.main.async {
                isSaving = false
                switch result {
                case .success(let response):
                    if response.success {
                        onSaveComplete()
                        dismiss()
                    } else {
                        errorMessage = response.error?.message ?? response.message ?? "Failed to add medication"
                    }
                case .failure(let error):
                    errorMessage = error.localizedDescription
                }
            }
        }
    }
}

#Preview {
    NewMedicationView(onSaveComplete: {})
}
