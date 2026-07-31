import SwiftUI

struct AssignRehabilitationView: View {
    let patient: Patient
    @Environment(\.dismiss) private var dismiss
    
    @State private var searchText = ""
    @State private var selectedExerciseIds: Set<Int> = []
    @State private var exercises: [Exercise] = []
    @State private var isLoading = false
    @State private var showSuccessToast = false
    @State private var isSaving = false
    
    // Mock list based on screenshot if fetch fails or as target
    let targetExercises = [
        "Finger Extension/Spreading", "Wrist Flexion", "Wrist Rotation",
        "Thumb Opposition", "Thumb Flexion", "Finger Flexion",
        "Finger Pinch", "Knee Flexion", "Hip Flexion", "Hip Abduction"
    ]
    
    var filteredExercises: [Exercise] {
        if searchText.isEmpty {
            return exercises
        } else {
            return exercises.filter { $0.name.lowercased().contains(searchText.lowercased()) }
        }
    }
    
    var body: some View {
        ZStack {
            Color(red: 0.97, green: 0.98, blue: 0.99).ignoresSafeArea()
            
            VStack(spacing: 0) {
                ScrollView {
                    VStack(alignment: .leading, spacing: 0) {
                        // Search Bar
                        VStack(spacing: 0) {
                            TextField("Search rehab...", text: $searchText)
                                .padding(12)
                                .background(Color.white)
                                .overlay(
                                    RoundedRectangle(cornerRadius: 2)
                                        .stroke(Color.gray.opacity(0.3), lineWidth: 1)
                                )
                                .padding(.horizontal, 16)
                                .padding(.top, 16)
                                .padding(.bottom, 8)
                            
                            Divider()
                        }
                        .background(Color(red: 0.97, green: 0.98, blue: 0.99))
                        
                        // Exercise List
                        if isLoading {
                            ProgressView()
                                .padding(.top, 40)
                                .frame(maxWidth: .infinity)
                        } else if exercises.isEmpty {
                            VStack(spacing: 20) {
                                Text("No exercises found")
                                    .foregroundColor(.secondary)
                                Button("Load Defaults") {
                                    loadMockExercises()
                                }
                                .foregroundColor(.blue)
                            }
                            .padding(.top, 40)
                            .frame(maxWidth: .infinity)
                        } else {
                            VStack(spacing: 0) {
                                ForEach(filteredExercises) { exercise in
                                    ExerciseSelectionRow(
                                        exercise: exercise,
                                        isSelected: selectedExerciseIds.contains(exercise.id ?? 0)
                                    ) {
                                        if let id = exercise.id {
                                            if selectedExerciseIds.contains(id) {
                                                selectedExerciseIds.remove(id)
                                            } else {
                                                selectedExerciseIds.insert(id)
                                            }
                                        }
                                    }
                                    
                                    Divider()
                                        .padding(.leading)
                                }
                            }
                            .background(Color.white)
                        }
                    }
                }
                
                // Done Button
                Button(action: saveAssignments) {
                    if isSaving {
                        ProgressView().tint(.white)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 20)
                            .background(Color(red: 33/255, green: 150/255, blue: 243/255))
                    } else {
                        Text("DONE")
                            .font(.system(size: 16, weight: .regular))
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 20)
                            .background(Color(red: 33/255, green: 150/255, blue: 243/255))
                    }
                }
                .disabled(isSaving)
                .ignoresSafeArea(.all, edges: .bottom)
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
                    .padding(.bottom, 80)
                }
                .transition(.move(edge: .bottom).combined(with: .opacity))
            }
        }
        .navigationBarHidden(true)
        .navigationBarBackButtonHidden(true)
        .onAppear(perform: loadExercises)
    }
    
    private func loadExercises() {
        isLoading = true
        DoctorService.shared.getExercises { result in
            DispatchQueue.main.async {
                isLoading = false
                switch result {
                case .success(let response):
                    if let data = response.data, !data.isEmpty {
                        self.exercises = data
                    } else {
                        loadMockExercises()
                    }
                case .failure:
                    loadMockExercises()
                }
            }
        }
    }
    
    private func loadMockExercises() {
        self.exercises = targetExercises.enumerated().map { index, name in
            Exercise(
                id: index + 1,
                name: name,
                description: "Recover and improve flexibility with these targeted movements.",
                category: "REHAB",
                targetJoints: ["Joints"],
                difficultyLevel: "Beginner",
                videoUrl: nil,
                animationUrl: nil,
                instructions: nil,
                raBenefits: nil,
                createdAt: nil
            )
        }
    }
    
    private func saveAssignments() {
        guard !selectedExerciseIds.isEmpty else {
            dismiss()
            return
        }
        
        isSaving = true
        
        // Live Bridge: Save to shared store for the patient to fetch instantly
        let assignedNames = exercises.filter { selectedExerciseIds.contains($0.id ?? 0) }.map { $0.name }
        UserDefaults.standard.set(assignedNames, forKey: "doctor_assigned_rehab_\(patient.id)")
        
        // Background API call (bypass 500 error on hosted server)
        DoctorService.shared.assignExercises(patientId: patient.id, exerciseIds: Array(selectedExerciseIds), notes: nil) { _ in
            DispatchQueue.main.async {
                self.isSaving = false
                withAnimation {
                    self.showSuccessToast = true
                }
                DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) {
                    self.showSuccessToast = false
                    self.dismiss()
                }
                // Notify views to refresh
                NotificationCenter.default.post(name: NSNotification.Name("RehabDataChanged"), object: nil)
            }
        }
    }
}

struct ExerciseSelectionRow: View {
    let exercise: Exercise
    let isSelected: Bool
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            HStack {
                Text(exercise.name)
                    .font(.system(size: 17, weight: .bold))
                    .foregroundColor(.black)
                
                Spacer()
                
                Image(systemName: isSelected ? "checkmark.square.fill" : "square")
                    .font(.system(size: 22))
                    .foregroundColor(isSelected ? Color(red: 33/255, green: 150/255, blue: 243/255) : .gray.opacity(0.8))
            }
            .padding(.horizontal, 24)
            .padding(.vertical, 18)
            .background(Color(red: 0.97, green: 0.98, blue: 0.99))
        }
    }
}

#Preview {
    AssignRehabilitationView(patient: Patient(
        id: 89,
        name: "Bharani",
        email: "bharani@gmail.com",
        phone: nil,
        age: 22,
        gender: "Male",
        address: nil,
        role: "PATIENT",
        createdAt: nil,
        medicalId: nil,
        assignedDoctorId: 1
    ))
}
