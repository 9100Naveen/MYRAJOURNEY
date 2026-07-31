import SwiftUI

struct DoctorExerciseAssignmentView: View {
    @State private var patients: [User] = []
    @State private var exercises: [Exercise] = []
    @State private var selectedPatientId: Int?
    @State private var selectedExerciseIds: Set<Int> = []
    @State private var notes: String = ""
    @State private var isLoading = true
    @State private var isSaving = false
    @State private var showSuccess = false
    @State private var errorMessage: String?
    
    var body: some View {
        ZStack {
            Color.white.ignoresSafeArea()
            
            ScrollView {
                VStack(alignment: .leading, spacing: 25) {
                    Text("Assign Exercises")
                        .font(.system(size: 28, weight: .bold))
                        .foregroundColor(.primary)
                    
                    if let error = errorMessage {
                        Text(error).foregroundColor(.red).font(.subheadline)
                    }
                    
                    // Patient Selection
                    VStack(alignment: .leading, spacing: 10) {
                        Text("Select Patient")
                            .font(.headline)
                            .foregroundColor(.secondary)
                        
                        Picker("Patient", selection: $selectedPatientId) {
                            Text("Select a patient...").tag(nil as Int?)
                            ForEach(patients) { patient in
                                Text(patient.name ?? "Unknown").tag(patient.id as Int?)
                            }
                        }
                        .pickerStyle(MenuPickerStyle())
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color.gray.opacity(0.05))
                        .cornerRadius(12)
                    }
                    
                    // Exercise Library
                    VStack(alignment: .leading, spacing: 15) {
                        Text("Exercise Library")
                            .font(.headline)
                            .foregroundColor(.secondary)
                        
                        LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 12) {
                            ForEach(exercises) { exercise in
                                let id = exercise.id ?? 0
                                ExerciseToggleCard(title: exercise.name, isSelected: selectedExerciseIds.contains(id)) {
                                    if selectedExerciseIds.contains(id) {
                                        selectedExerciseIds.remove(id)
                                    } else {
                                        selectedExerciseIds.insert(id)
                                    }
                                }
                            }
                        }
                    }
                    
                    // Notes
                    VStack(alignment: .leading, spacing: 10) {
                        Text("Doctor's Notes")
                            .font(.headline)
                            .foregroundColor(.secondary)
                        
                        TextEditor(text: $notes)
                            .frame(height: 100)
                            .padding(8)
                            .background(Color.gray.opacity(0.05))
                            .cornerRadius(12)
                            .foregroundColor(.primary)
                    }
                    
                    Button(action: assignExercises) {
                        HStack {
                            if isSaving {
                                ProgressView().tint(.white)
                            } else {
                                Text("Assign Exercises")
                                    .fontWeight(.bold)
                            }
                        }
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(selectedExerciseIds.isEmpty || selectedPatientId == nil ? Color.gray : Color.blue)
                        .foregroundColor(.white)
                        .cornerRadius(15)
                        .shadow(radius: 5)
                    }
                    .disabled(isSaving || selectedExerciseIds.isEmpty || selectedPatientId == nil)
                    
                    Spacer(minLength: 50)
                }
                .padding()
            }
            
            if isLoading {
                Color.white.opacity(0.8).ignoresSafeArea()
                ProgressView("Loading Library...").tint(.blue)
            }
        }
        .navigationBarTitleDisplayMode(.inline)
        .onAppear(perform: loadData)
        .alert(isPresented: $showSuccess) {
            Alert(title: Text("Success"), message: Text("Exercises assigned successfully."), dismissButton: .default(Text("OK")) {
                selectedExerciseIds.removeAll()
                notes = ""
            })
        }
    }
    
    private func loadData() {
        isLoading = true
        errorMessage = nil
        
        let group = DispatchGroup()
        
        group.enter()
        DoctorService.shared.getAllPatients { result in
            DispatchQueue.main.async {
                if case .success(let response) = result {
                    self.patients = (response.data ?? []).filter { $0.role?.uppercased() == "PATIENT" }
                }
                group.leave()
            }
        }
        
        group.enter()
        DoctorService.shared.getExercises { result in
            DispatchQueue.main.async {
                if case .success(let response) = result {
                    self.exercises = response.data ?? []
                }
                group.leave()
            }
        }
        
        group.notify(queue: .main) {
            self.isLoading = false
        }
    }
    
    private func assignExercises() {
        guard let patientId = selectedPatientId else { return }
        isSaving = true
        
        // Map selected exercises to the format required by the rehab-plans API
        let rehabExercises: [[String: Any]] = exercises.filter { selectedExerciseIds.contains($0.id ?? 0) }.map { ex in
            return [
                "name": ex.name,
                "description": ex.description ?? "Assigned exercise",
                "sets": 3,
                "reps": "10",
                "frequency_per_week": "Daily"
            ]
        }
        
        DoctorService.shared.createRehabPlan(patientId: patientId, title: "Medical Rehabilitation Plan", exercises: rehabExercises) { result in
            DispatchQueue.main.async {
                isSaving = false
                switch result {
                case .success:
                    showSuccess = true
                    selectedExerciseIds.removeAll()
                    notes = ""
                case .failure(let error):
                    errorMessage = "Failed to assign exercises: \(error.localizedDescription)"
                }
            }
        }
    }
}

struct ExerciseToggleCard: View {
    let title: String
    let isSelected: Bool
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            HStack {
                Text(title)
                    .font(.subheadline)
                    .foregroundColor(.primary)
                Spacer()
                Image(systemName: isSelected ? "checkmark.circle.fill" : "circle")
                    .foregroundColor(isSelected ? .green : .gray.opacity(0.3))
            }
            .padding()
            .background(isSelected ? Color.blue.opacity(0.05) : Color.gray.opacity(0.05))
            .cornerRadius(10)
            .overlay(
                RoundedRectangle(cornerRadius: 10)
                    .stroke(isSelected ? Color.blue : Color.gray.opacity(0.1), lineWidth: 1)
            )
        }
    }
}

#Preview {
    NavigationView {
        DoctorExerciseAssignmentView()
    }
}
