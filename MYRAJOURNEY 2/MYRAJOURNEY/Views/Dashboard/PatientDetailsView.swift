import SwiftUI

struct PatientDetailsView: View {
    let patient: Patient
    @Environment(\.dismiss) private var dismiss
    
    @State private var alertMessage = ""
    @State private var diagnosis = ""
    @State private var treatmentSuggestions = ""
    @State private var isSavingDiagnosis = false
    @State private var isSendingAlert = false
    @State private var showSuccessToast = false
    @State private var toastMessage = ""
    
    // Sheet states
    @State private var showAssignMedication = false
    @State private var showAddRehabilitation = false
    
    // Mock data for counts (real apps would fetch these)
    @State private var medicationsCount = 0
    @State private var reportsCount = 0
    @State private var appointmentsCount = 0
    @State private var symptomsCount = 0
    
    var body: some View {
        ZStack {
            Color(UIColor.systemGroupedBackground).ignoresSafeArea()
            
            ScrollView(showsIndicators: false) {
                VStack(spacing: 16) {
                    profileHeaderCard
                    
                    medicationsSection
                    rehabilitationSection
                    appointmentsSection
                    patientReportsSection
                    symptomHistorySection
                    crfSection
                    sendAlertSection
                    diagnosisSection
                }
                .padding(16)
            }
            
            toastOverlay
        }
        .navigationTitle("Patient Details")
        .navigationBarTitleDisplayMode(.inline)
    }
    
    private func triggerToast(message: String) {
        self.toastMessage = message
        withAnimation {
            showSuccessToast = true
        }
        DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
            withAnimation {
                showSuccessToast = false
            }
        }
    }
    
    private var toastOverlay: some View {
        VStack {
            Spacer()
            if showSuccessToast {
                HStack(spacing: 12) {
                    // Small custom image for the heart icon can be added, using star for now
                    Image(systemName: "heart.text.square.fill")
                        .foregroundColor(.teal)
                        .font(.system(size: 12))
                    Text(toastMessage)
                        .font(.system(size: 14, weight: .medium))
                        .foregroundColor(.white)
                }
                .padding(.horizontal, 24)
                .padding(.vertical, 16)
                .background(Color(UIColor.darkGray))
                .cornerRadius(12)
                .padding(.bottom, 40)
                .shadow(radius: 10)
                .transition(.move(edge: .bottom).combined(with: .opacity))
            }
        }
    }
    
    private var profileHeaderCard: some View {
        VStack(spacing: 20) {
            HStack(spacing: 20) {
                ZStack {
                    Circle()
                        .stroke(Color.blue, lineWidth: 3)
                        .frame(width: 90, height: 90)
                    Circle()
                        .fill(Color.blue.opacity(0.1))
                        .frame(width: 84, height: 84)
                    Image(systemName: "person.fill")
                        .font(.system(size: 30))
                        .foregroundColor(.black)
                }
                
                VStack(alignment: .leading, spacing: 6) {
                    Text(patient.name)
                        .font(.title2)
                        .bold()
                    Text("Age: \(patient.age == nil || patient.age == 0 ? "N/A" : "\(patient.age!)") Years")
                        .font(.subheadline)
                        .foregroundColor(.black.opacity(0.8))
                    Text("ID: \(patient.id)")
                        .font(.subheadline)
                        .foregroundColor(.black.opacity(0.8))
                }
                Spacer()
            }
            
            Button(action: {
                // Edit patient action
            }) {
                Text("EDIT PATIENT")
                    .font(.system(size: 14, weight: .regular))
                    // Outlined text effect similar to screenshots
                    .shadow(color: .black, radius: 0.2, x: 0, y: 0)
                    .foregroundColor(.black)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 12)
                    .background(Color.white)
                    .overlay(
                        RoundedRectangle(cornerRadius: 16)
                            .stroke(Color.blue, lineWidth: 1)
                    )
            }
        }
        .padding(16)
        .background(Color.white)
        .cornerRadius(16)
        .shadow(color: Color.black.opacity(0.05), radius: 8, x: 0, y: 4)
    }
    
    private var medicationsSection: some View {
        CustomSectionCard(
            title: "Medications",
            trailingView: AnyView(
                NavigationLink(destination: AssignMedicationsView(patient: patient)) {
                    Text("Assign Medication")
                        .font(.system(size: 14, weight: .medium))
                        .foregroundColor(.white)
                        .padding(.horizontal, 16)
                        .padding(.vertical, 12)
                        .background(Color.blue)
                        .cornerRadius(12)
                }
            )
        ) {
            EmptyView()
        }
    }
    
    private var rehabilitationSection: some View {
        CustomSectionCard(
            title: "Rehabilitation",
            trailingView: AnyView(
                NavigationLink(destination: AssignRehabilitationView(patient: patient)) {
                    Text("+ Add")
                        .font(.system(size: 14, weight: .medium))
                        .foregroundColor(.white)
                        .padding(.horizontal, 24)
                        .padding(.vertical, 12)
                        .background(Color.blue)
                        .cornerRadius(12)
                }
            )
        ) {
            EmptyView()
        }
    }
    
    private var appointmentsSection: some View {
        CustomSectionCard(
            title: "Appointments",
            trailingView: AnyView(
                NavigationLink(destination: AppointmentListView(isDoctor: true, patientId: patient.id, patientName: patient.name)) {
                    Text("View All")
                        .font(.system(size: 14, weight: .medium))
                        .foregroundColor(.black)
                }
            )
        ) {
            // Using a mockup for the single appointment to match screenshot exactly
            VStack(alignment: .leading, spacing: 8) {
                Text(patient.name)
                    .font(.system(size: 16, weight: .bold))
                HStack {
                    VStack(alignment: .leading, spacing: 4) {
                        Text("Follow-up Consultation")
                            .font(.system(size: 14))
                            .foregroundColor(.black.opacity(0.8))
                        Text("Jul 30, 2026")
                            .font(.system(size: 12))
                            .foregroundColor(.black.opacity(0.6))
                    }
                    Spacer()
                    Text("3:01 AM")
                        .font(.system(size: 16, weight: .bold))
                }
            }
            .padding(.horizontal, 4)
            .padding(.top, 4)
        }
    }
    
    private var patientReportsSection: some View {
        NavigationLink(destination: ReportListView(isDoctor: true, patientId: String(patient.id), patientName: patient.name)) {
            CustomSectionCard(
                title: "Patient Reports",
                icon: "📋"
            ) {
                EmptyView()
            }
        }
        .buttonStyle(PlainButtonStyle())
    }
    
    private var symptomHistorySection: some View {
        CustomSectionCard(
            title: "Symptom History",
            icon: "📄",
            trailingView: AnyView(
                HStack(spacing: 12) {
                    Text("0 entries")
                        .font(.system(size: 12))
                        .padding(.horizontal, 12)
                        .padding(.vertical, 6)
                        .background(Color.white)
                        .overlay(
                            RoundedRectangle(cornerRadius: 12)
                                .stroke(Color.blue.opacity(0.5), lineWidth: 1)
                        )
                    
                    NavigationLink(destination: SymptomLogView(patientId: patient.id)) {
                        Text("View All")
                            .font(.system(size: 14, weight: .medium))
                            .foregroundColor(.black)
                    }
                }
            )
        ) {
            Text("No symptom logs recorded yet")
                .font(.system(size: 14))
                .foregroundColor(.black)
                .frame(maxWidth: .infinity, alignment: .center)
                .padding(.vertical, 16)
        }
    }
    
    private var crfSection: some View {
        CustomSectionCard(
            title: "CRF",
            icon: "📊"
        ) {
            Text("No chart data available.")
                .font(.system(size: 14))
                .foregroundColor(.black)
                .frame(maxWidth: .infinity, alignment: .center)
                .padding(.vertical, 16)
        }
    }
    
    private var sendAlertSection: some View {
        CustomSectionCard(
            title: "Send Alert"
        ) {
            VStack(spacing: 16) {
                TextEditor(text: $alertMessage)
                    .frame(height: 80)
                    .padding(8)
                    .overlay(
                        RoundedRectangle(cornerRadius: 8)
                            .stroke(Color.black, lineWidth: 1)
                    )
                    .overlay(
                        Group {
                            if alertMessage.isEmpty {
                                Text("Enter alert message")
                                    .foregroundColor(.black)
                                    .padding(.leading, 12)
                                    .padding(.top, 16)
                            }
                        },
                        alignment: .topLeading
                    )
                
                Button(action: {
                    isSendingAlert = true
                    DoctorService.shared.sendAlert(patientId: patient.id, message: alertMessage) { result in
                        DispatchQueue.main.async {
                            isSendingAlert = false
                            alertMessage = ""
                            switch result {
                            case .success(_):
                                triggerToast(message: "API Response: 200")
                            case .failure(_):
                                triggerToast(message: "API Error")
                            }
                        }
                    }
                }) {
                    if isSendingAlert {
                        ProgressView().tint(.white)
                    } else {
                        Text("Send Alert")
                    }
                }
                .font(.system(size: 16, weight: .medium))
                .foregroundColor(.white)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 16)
                .background(Color.blue)
                .cornerRadius(12)
            }
        }
    }
    
    private var diagnosisSection: some View {
        CustomSectionCard(
            title: "Doctor's Diagnosis & Suggestions",
            icon: "💊"
        ) {
            VStack(spacing: 16) {
                TextEditor(text: $diagnosis)
                    .frame(height: 80)
                    .padding(8)
                    .overlay(
                        RoundedRectangle(cornerRadius: 8)
                            .stroke(Color.black, lineWidth: 1)
                    )
                    .overlay(
                        Group {
                            if diagnosis.isEmpty {
                                Text("Enter diagnosis...")
                                    .foregroundColor(.black)
                                    .padding(.leading, 12)
                                    .padding(.top, 16)
                            }
                        },
                        alignment: .topLeading
                    )
                
                TextEditor(text: $treatmentSuggestions)
                    .frame(height: 80)
                    .padding(8)
                    .overlay(
                        RoundedRectangle(cornerRadius: 8)
                            .stroke(Color.black, lineWidth: 1)
                    )
                    .overlay(
                        Group {
                            if treatmentSuggestions.isEmpty {
                                Text("Enter treatment suggestions...")
                                    .foregroundColor(.black)
                                    .padding(.leading, 12)
                                    .padding(.top, 16)
                            }
                        },
                        alignment: .topLeading
                    )
                
                Button(action: {
                    isSavingDiagnosis = true
                    DoctorService.shared.saveDiagnosis(patientId: patient.id, diagnosis: diagnosis, suggestions: treatmentSuggestions) { result in
                        DispatchQueue.main.async {
                            isSavingDiagnosis = false
                            switch result {
                            case .success(_):
                                triggerToast(message: "API Response: 200")
                            case .failure(_):
                                triggerToast(message: "API Error")
                            }
                        }
                    }
                }) {
                    if isSavingDiagnosis {
                        ProgressView().tint(.white)
                    } else {
                        Text("Save Diagnosis")
                    }
                }
                .font(.system(size: 16, weight: .medium))
                .foregroundColor(.white)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 16)
                .background(Color.blue)
                .cornerRadius(12)
            }
        }
    }
}

struct CustomSectionCard<Content: View>: View {
    let title: String
    var icon: String? = nil
    var trailingView: AnyView? = nil
    @ViewBuilder let content: Content
    
    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            HStack {
                if let icon = icon {
                    if icon == "📄" {
                        Image(systemName: "doc.fill").foregroundColor(.blue)
                    } else if icon == "📊" {
                        Image(systemName: "chart.bar.fill").foregroundColor(.blue).opacity(0.8)
                    } else {
                        Text(icon)
                    }
                }
                
                Text(title)
                    .font(.system(size: 18, weight: .regular))
                    // Text outline effect from screenshots
                    .shadow(color: .black, radius: 0.2, x: 0, y: 0)
                    
                Spacer()
                
                if let trailingView = trailingView {
                    trailingView
                }
            }
            .padding(16)
            .background(Color.blue.opacity(0.12))
            .cornerRadius(12)
            
            if !(Content.self == EmptyView.self) {
                content
            }
        }
        .padding(12)
        .background(Color.white)
        .cornerRadius(16)
        .shadow(color: Color.black.opacity(0.05), radius: 8, x: 0, y: 4)
    }
}
