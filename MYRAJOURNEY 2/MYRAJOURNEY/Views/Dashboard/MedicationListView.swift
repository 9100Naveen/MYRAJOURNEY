import SwiftUI
import Combine

struct MedicationListView: View {
    @State private var medications: [Medication] = []
    @State private var isLoading = true
    @State private var showToast = false
    @State private var toastMessage = ""
    @ObservedObject private var appState = AppState.shared
    
    var patientId: Int? = nil
    var patientName: String? = nil
    var isDoctor: Bool = false
    
    var body: some View {
        ZStack {
            Color.appBackground.ignoresSafeArea()
            
            VStack(spacing: 0) {
                // Custom Blue Header (Matching Image 1)
                prescriptionHeader
                
                ScrollView(showsIndicators: false) {
                    VStack(alignment: .leading, spacing: 0) {
                        // Patient & Rx Info Section
                        patientInfoSection
                        
                        Divider()
                            .padding(.horizontal, 24)
                            .padding(.top, 8)
                        
                        // Medications Section
                        VStack(alignment: .leading, spacing: 20) {
                            Text("MEDICATIONS / ADVICE")
                                .font(.system(size: 16, weight: .bold))
                                .foregroundColor(.primary)
                                .padding(.horizontal, 24)
                                .padding(.top, 24)
                            
                            if medications.isEmpty && !isLoading {
                                emptyStateView
                            } else {
                                medicationListItems
                            }
                        }
                        
                        // Advice Section
                        adviceSection
                        
                        Spacer(minLength: 120)
                    }
                }
            }
            .ignoresSafeArea(edges: .top)
            
            // Bottom Action Bar (Matching Image 2)
            VStack {
                Spacer()
                actionButtons
            }
            
            // Loading Overlay
            if isLoading && medications.isEmpty {
                loadingOverlay
            }
            
            // Toast Notification (Matching "Loading notifications" style)
            if showToast {
                VStack {
                    Spacer()
                    toastView
                        .padding(.bottom, 120)
                }
                .transition(.move(edge: .bottom).combined(with: .opacity))
            }
        }
        .navigationBarHidden(true)
        .onAppear(perform: loadMedications)
    }
    
    // MARK: - Subviews
    
    private var prescriptionHeader: some View {
        VStack(spacing: 0) {
            HStack(alignment: .center) {
                Text("•myrajourney•")
                    .font(.system(size: 28, weight: .black, design: .rounded))
                    .foregroundColor(.white)
                    .minimumScaleFactor(0.8)
                    .lineLimit(1)
                
                Spacer()
                
                VStack(alignment: .trailing, spacing: 2) {
                    Text(medications.first?.doctorName ?? "sai")
                        .font(.system(size: 16, weight: .bold))
                    Text(medications.first?.doctorSpecialization ?? "Rheumatology Specialist")
                        .font(.system(size: 12, weight: .medium))
                    Text("Digital Health Clinic")
                        .font(.system(size: 11))
                    Text("License: \(medications.first?.doctorLicense ?? "MJ-2026-DIGITAL")")
                        .font(.system(size: 10))
                }
                .foregroundColor(.white.opacity(0.9))
                .multilineTextAlignment(.trailing)
            }
            .padding(.horizontal, 24)
            .padding(.top, topSafeArea() + 10)
            .padding(.bottom, 20)
        }
        .background(Color(red: 0.15, green: 0.25, blue: 0.6))
    }
    
    private var patientInfoSection: some View {
        VStack(spacing: 12) {
            HStack(alignment: .top) {
                VStack(alignment: .leading, spacing: 4) {
                    Text(patientName ?? medications.first?.doctorName == nil ? (appState.currentUser?.name ?? "lingaiah") : "lingaiah")
                        .font(.system(size: 22, weight: .bold))
                        .foregroundColor(.primary)
                    Text("Patient ID: \(patientId ?? appState.currentUser?.id ?? 99)")
                        .font(.system(size: 15))
                        .foregroundColor(.secondary)
                }
                Spacer()
                VStack(alignment: .trailing, spacing: 4) {
                    Text(currentDateFormatted)
                        .font(.system(size: 18, weight: .bold))
                    Text("Prescription ID: DIG-\(String(format: "%03d", (patientId ?? 1)))")
                        .font(.system(size: 14))
                        .foregroundColor(.secondary)
                }
                .foregroundColor(.primary)
            }
            .padding(.horizontal, 24)
            .padding(.top, 24)
        }
    }
    
    private var medicationListItems: some View {
        VStack(alignment: .leading, spacing: 18) {
            ForEach(medications) { med in
                HStack(alignment: .top, spacing: 12) {
                    Image(systemName: "checkmark")
                        .font(.system(size: 14, weight: .black))
                        .foregroundColor(.primary)
                        .padding(.top, 4)
                    
                    VStack(alignment: .leading, spacing: 4) {
                        Text("\(med.name) \(med.dosage ?? "") - (\(getTimingString(med: med))) - \(med.foodRelation ?? "With food") - \(formatFrequency(med.frequency))")
                            .font(.system(size: 16, weight: .medium))
                            .foregroundColor(.primary)
                            .lineSpacing(4)
                    }
                }
                .padding(.horizontal, 24)
            }
        }
    }
    
    private func formatFrequency(_ freq: String) -> String {
        if let _ = Int(freq) {
            return "\(freq)x daily"
        }
        if freq.lowercased().contains("daily") {
            return freq
        }
        return "\(freq)x daily"
    }
    
    private var adviceSection: some View {
        VStack(alignment: .leading, spacing: 15) {
            Text("GENERAL ADVICE / NOTES")
                .font(.system(size: 16, weight: .bold))
                .foregroundColor(.primary)
                .padding(.horizontal, 24)
                .padding(.top, 30)
            
            VStack(alignment: .leading, spacing: 12) {
                adviceRow(text: "Follow the prescribed dosage strictly.")
                adviceRow(text: "Avoid alcohol while on medication.")
                adviceRow(text: "Visit the nearest ER if symptoms worsen.")
                adviceRow(text: "Keep hydrated.")
            }
            .padding(.horizontal, 24)
            
            Divider()
                .padding(.horizontal, 24)
                .padding(.top, 30)
            
            Text("Disclaimer: This prescription is based on the information provided. Visit a doctor immediately if you experience severe side effects.")
                .font(.system(size: 10, weight: .bold))
                .foregroundColor(.primary)
                .multilineTextAlignment(.center)
                .frame(maxWidth: .infinity)
                .padding(.top, 40)
                .padding(.horizontal, 24)
        }
    }
    
    private func adviceRow(text: String) -> some View {
        HStack(alignment: .top, spacing: 12) {
            Image(systemName: "checkmark")
                .font(.system(size: 14, weight: .black))
                .foregroundColor(.primary)
                .padding(.top, 2)
            Text(text)
                .font(.system(size: 16))
                .foregroundColor(.primary)
        }
    }
    
    private var actionButtons: some View {
        HStack(spacing: 12) {
            Button(action: loadMedications) {
                Text("REFRESH")
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 16)
                    .background(Color(red: 0.1, green: 0.25, blue: 0.5)) // Navy/Blue
                    .cornerRadius(8)
            }
            
            Button(action: { /* Download logic */ }) {
                Text("DOWNLOAD")
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 16)
                    .background(Color(red: 0.0, green: 0.6, blue: 0.45)) // Green
                    .cornerRadius(8)
            }
            
            Button(action: { /* Share logic */ }) {
                Text("SHARE")
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 16)
                    .background(Color(red: 0.45, green: 0.35, blue: 0.85)) // Purple
                    .cornerRadius(8)
            }
        }
        .padding(.horizontal, 16)
        .padding(.bottom, 34)
        .background(Color.appSurface)
    }
    
    private var toastView: some View {
        HStack(spacing: 12) {
            Image(systemName: "pills.fill")
                .resizable()
                .aspectRatio(contentMode: .fit)
                .frame(width: 20, height: 20)
                .foregroundColor(.appAccent)
                .cornerRadius(4)
                .overlay(
                    RoundedRectangle(cornerRadius: 4)
                        .stroke(Color.white.opacity(0.1), lineWidth: 1)
                )
            
            Text(toastMessage)
                .foregroundColor(.white)
                .font(.system(size: 15, weight: .medium))
            
            Spacer()
        }
        .padding(.vertical, 20)
        .padding(.horizontal, 24)
        .background(Color(red: 0.1, green: 0.1, blue: 0.1))
        .cornerRadius(12)
        .padding(.horizontal, 20)
        .shadow(radius: 10)
    }
    
    private var loadingOverlay: some View {
        Color.appBackground.opacity(0.5)
            .overlay(ProgressView())
    }
    
    private var emptyStateView: some View {
        Text("No active prescriptions found.")
            .font(.system(size: 15))
            .foregroundColor(.secondary)
            .frame(maxWidth: .infinity, alignment: .center)
            .padding(.vertical, 40)
    }
    
    // MARK: - Helpers
    
    private var currentDateFormatted: String {
        let formatter = DateFormatter()
        formatter.dateFormat = "MMMM dd, yyyy"
        return formatter.string(from: Date())
    }
    
    private func getTimingString(med: Medication) -> String {
        var timings: [String] = []
        if med.isMorning { timings.append("Morning") }
        if med.isAfternoon { timings.append("Afternoon") }
        if med.isNight { timings.append("Night") }
        return timings.joined(separator: "/")
    }
    
    // MARK: - Logic
    
    private func loadMedications() {
        isLoading = true
        toastMessage = "Loading notifications for: \(appState.currentUser?.name ?? "user") (ID: \(appState.currentUser?.id ?? 0))"
        showToast = true
        
        PatientService.shared.getMedications(patientId: patientId) { result in
            DispatchQueue.main.async {
                isLoading = false
                if case .success(let response) = result, let data = response.data {
                    self.medications = data
                    if data.isEmpty {
                        toastMessage = "No medications found"
                    } else {
                        toastMessage = "Prescription updated successfully"
                    }
                } else {
                    toastMessage = "Failed to fetch medications"
                }
                
                DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                    withAnimation {
                        showToast = false
                    }
                }
            }
        }
    }
    
    private func topSafeArea() -> CGFloat {
        guard let scene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
              let window = scene.windows.first else {
            return 44
        }
        return window.safeAreaInsets.top
    }
}

#Preview {
    NavigationView {
        MedicationListView()
    }
}

