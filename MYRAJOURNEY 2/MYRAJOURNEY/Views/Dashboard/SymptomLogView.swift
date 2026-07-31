import SwiftUI

struct SymptomLogView: View {
    @Environment(\.presentationMode) var presentationMode
    @State private var painLevel: Double = 0
    @State private var fatigueLevel: Double = 0
    @State private var morningStiffnessDuration: String = "Less than 30 min"
    @State private var jointCount = ""
    @State private var otherSymptoms = ""
    @State private var notes = ""
    @State private var isLoading = false
    @State private var errorMessage: String?
    @State private var showToast = false
    @State private var toastMessage = ""
    
    var body: some View {
        ZStack {
            Color.appBackground.ignoresSafeArea()
            
            VStack(spacing: 0) {
                // Simplified Header matching screenshot
                HStack {
                    Button(action: { presentationMode.wrappedValue.dismiss() }) {
                        HStack(spacing: 4) {
                            Image(systemName: "chevron.left")
                            Text("Back")
                        }
                        .font(.system(size: 17, weight: .regular))
                        .foregroundColor(.blue)
                    }
                    Spacer()
                    Text("Symptom Log")
                        .font(.system(size: 20, weight: .bold))
                    Spacer()
                    // Hidden balance element
                    Text("Back").foregroundColor(.clear).font(.system(size: 17))
                }
                .padding(.horizontal)
                .padding(.top, 8)
                .padding(.bottom, 12)
                .background(Color.appSurface)
                
                ScrollView(showsIndicators: false) {
                    VStack(spacing: 30) {
                        // Joint Pain Section
                        VStack(spacing: 8) {
                            Text("Joint Pain (VAS Score 0–10)")
                                .font(.system(size: 18, weight: .bold))
                            
                            CustomSlider(value: $painLevel, accentColor: .orange)
                            
                            Text("VAS: \(Int(painLevel))")
                                .font(.system(size: 16))
                                .foregroundColor(.primary)
                        }
                        .padding(.top, 10)
                        
                        // Joints Involved Section
                        VStack(spacing: 8) {
                            Text("Number of Joints Involved")
                                .font(.system(size: 18, weight: .bold))
                            
                            TextField("Enter number", text: $jointCount)
                                .keyboardType(.numberPad)
                                .multilineTextAlignment(.center)
                                .font(.system(size: 20))
                                .padding(.bottom, 4)
                                .overlay(Rectangle().frame(height: 1).foregroundColor(.secondary.opacity(0.3)), alignment: .bottom)
                                .padding(.horizontal, 50)
                        }
                        
                        // Morning Stiffness Section
                        VStack(spacing: 15) {
                            Text("Morning Stiffness Duration")
                                .font(.system(size: 18, weight: .bold))
                            
                            HStack(spacing: 25) {
                                RadioButton(title: "Less than 30 min", isSelected: morningStiffnessDuration == "Less than 30 min") {
                                    morningStiffnessDuration = "Less than 30 min"
                                }
                                
                                RadioButton(title: "More than 30 min", isSelected: morningStiffnessDuration == "More than 30 min") {
                                    morningStiffnessDuration = "More than 30 min"
                                }
                            }
                        }
                        
                        // Fatigue Level Section
                        VStack(spacing: 8) {
                            Text("Fatigue Level (0–10 FACIT)")
                                .font(.system(size: 18, weight: .bold))
                            
                            CustomSlider(value: $fatigueLevel, accentColor: .orange)
                            
                            Text("Fatigue: \(Int(fatigueLevel))")
                                .font(.system(size: 16))
                                .foregroundColor(.primary)
                        }
                        
                        // Other Symptoms Section
                        VStack(spacing: 8) {
                            Text("Other symptoms")
                                .font(.system(size: 18, weight: .bold))
                            
                            TextField("", text: $otherSymptoms)
                                .multilineTextAlignment(.center)
                                .font(.system(size: 18))
                                .padding(.bottom, 4)
                                .overlay(Rectangle().frame(height: 1).foregroundColor(.secondary.opacity(0.3)), alignment: .bottom)
                                .padding(.horizontal, 50)
                        }
                        
                        // Notes Section
                        VStack(spacing: 0) {
                            ZStack(alignment: .topLeading) {
                                if notes.isEmpty {
                                    Text("Notes")
                                        .foregroundColor(.secondary.opacity(0.4))
                                        .padding(.horizontal, 16)
                                        .padding(.vertical, 10)
                                }
                                TextEditor(text: $notes)
                                    .padding(8)
                                    .frame(height: 100)
                                    .background(Color.clear)
                                    .font(.system(size: 17))
                            }
                            .background(Color.appCard)
                            .overlay(RoundedRectangle(cornerRadius: 0).stroke(Color.appBorder, lineWidth: 1))
                            .padding(.horizontal, 30)
                        }
                        
                        // Submit Button
                        Button(action: submitLog) {
                            HStack {
                                if isLoading {
                                    ProgressView().tint(.black)
                                } else {
                                    Text("SUBMIT ENTRY")
                                        .font(.system(size: 15, weight: .bold))
                                        .foregroundColor(.primary)
                                }
                            }
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 14)
                            .background(Color(red: 0.88, green: 0.94, blue: 0.95))
                            .cornerRadius(10)
                            .padding(.horizontal, 30)
                        }
                        .disabled(isLoading)
                        
                        Spacer(minLength: 40)
                    }
                }
            }
            
            // Toast
            if showToast {
                VStack {
                    Spacer()
                    HStack(spacing: 12) {
                        Image(systemName: "checkmark.circle.fill")
                            .foregroundColor(.teal)
                        Text(toastMessage)
                            .font(.system(size: 15, weight: .medium))
                            .foregroundColor(.white)
                    }
                    .padding(.vertical, 14)
                    .padding(.horizontal, 24)
                    .background(Color(red: 0.1, green: 0.1, blue: 0.1))
                    .cornerRadius(15)
                    .padding(.bottom, 40)
                }
                .transition(.move(edge: .bottom).combined(with: .opacity))
            }
        }
        .navigationBarHidden(true)
        .onAppear {
            toastMessage = "Loaded 1 medications"
            withAnimation { showToast = true }
            DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
                withAnimation { showToast = false }
            }
        }
    }
    
    private func submitLog() {
        isLoading = true
        errorMessage = nil
        
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        let dateString = formatter.string(from: Date())
        
        var combinedNotes = ""
        if !otherSymptoms.isEmpty {
            combinedNotes += "Other symptoms: \(otherSymptoms)\n"
        }
        if !notes.isEmpty {
            combinedNotes += "Notes: \(notes)\n"
        }
        combinedNotes += "Stiffness Duration: \(morningStiffnessDuration)"
        
        let request = SymptomRequest(
            patientId: Int(SessionManager.shared.userId ?? "0") ?? 0,
            date: dateString,
            painLevel: Int(painLevel),
            stiffnessLevel: morningStiffnessDuration == "More than 30 min" ? 5 : 2,
            fatigueLevel: Int(fatigueLevel),
            jointCount: Int(jointCount) ?? 0,
            notes: combinedNotes
        )
        
        PatientService.shared.createSymptom(request: request) { result in
            DispatchQueue.main.async {
                isLoading = false
                switch result {
                case .success(let response):
                    if response.success {
                        toastMessage = "Symptom log submitted successfully"
                        withAnimation { showToast = true }
                        DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                            presentationMode.wrappedValue.dismiss()
                        }
                    } else {
                        errorMessage = response.message ?? "Submission failed"
                    }
                case .failure(let error):
                    errorMessage = error.localizedDescription
                    toastMessage = "Error: \(error.localizedDescription)"
                    withAnimation { showToast = true }
                }
            }
        }
    }
}

// MARK: - Custom UI Components

struct CustomSlider: View {
    @Binding var value: Double
    var accentColor: Color
    
    var body: some View {
        ZStack {
            Rectangle()
                .fill(Color.gray.opacity(0.3))
                .frame(height: 1.5)
            
            Slider(value: $value, in: 0...10, step: 1)
                .accentColor(accentColor)
        }
        .padding(.horizontal, 30)
    }
}

struct RadioButton: View {
    let title: String
    let isSelected: Bool
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            HStack(spacing: 10) {
                ZStack {
                    Circle()
                        .stroke(Color.teal, lineWidth: 2)
                        .frame(width: 24, height: 24)
                    
                    if isSelected {
                        Circle()
                            .fill(Color.teal)
                            .frame(width: 14, height: 14)
                    }
                }
                
                Text(title)
                    .font(.system(size: 15))
                    .foregroundColor(.primary)
            }
        }
    }
}

#Preview {
    SymptomLogView()
}
