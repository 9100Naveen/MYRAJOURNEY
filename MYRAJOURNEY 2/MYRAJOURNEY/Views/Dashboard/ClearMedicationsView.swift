import SwiftUI

struct ClearMedicationsView: View {
    @State private var isClearing = false
    @State private var statusMessage = "This will remove ALL patient medication assignments.\nUse with caution!"
    @State private var showFinalConfirmation = false
    @Environment(\.presentationMode) var presentationMode
    
    var body: some View {
        ZStack {
            Color.white.ignoresSafeArea()
            
            VStack(spacing: 30) {
                Image(systemName: "exclamationmark.triangle.fill")
                    .font(.system(size: 80))
                    .foregroundColor(.red)
                    .shadow(radius: 10)
                
                Text("Danger Zone")
                    .font(.largeTitle)
                    .bold()
                    .foregroundColor(.primary)
                
                Text(statusMessage)
                    .font(.body)
                    .multilineTextAlignment(.center)
                    .foregroundColor(.secondary)
                    .padding(.horizontal, 40)
                
                Spacer()
                
                VStack(spacing: 15) {
                    Button(action: { showFinalConfirmation = true }) {
                        HStack {
                            if isClearing {
                                ProgressView().tint(.blue)
                            } else {
                                Text("Clear All Medications")
                                    .fontWeight(.bold)
                            }
                        }
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(isClearing ? Color.gray : Color.red)
                        .foregroundColor(.white)
                        .cornerRadius(12)
                    }
                    .disabled(isClearing)
                    
                    Button(action: { presentationMode.wrappedValue.dismiss() }) {
                        Text("Cancel")
                            .foregroundColor(.secondary)
                            .padding()
                    }
                }
                .padding(.horizontal, 30)
                .padding(.bottom, 40)
            }
        }
        .navigationTitle("Medication Clearance")
        .navigationBarTitleDisplayMode(.inline)
        .alert(isPresented: $showFinalConfirmation) {
            Alert(
                title: Text("Permanent Action"),
                message: Text("Are you absolutely sure you want to delete ALL medication assignments for ALL patients? This cannot be undone."),
                primaryButton: .destructive(Text("Yes, Clear All")) {
                    performClear()
                },
                secondaryButton: .cancel()
            )
        }
    }
    
    private func performClear() {
        isClearing = true
        statusMessage = "Clearing all patient medications..."
        
        DoctorService.shared.clearAllMedications { result in
            DispatchQueue.main.async {
                self.isClearing = false
                switch result {
                case .success:
                    self.statusMessage = "✅ All patient medications cleared successfully!\n\nPatients will now see empty medication lists."
                case .failure(let error):
                    self.statusMessage = "❌ Failed to clear medications: \(error.localizedDescription)"
                }
            }
        }
    }
}

#Preview {
    NavigationView {
        ClearMedicationsView()
    }
}
