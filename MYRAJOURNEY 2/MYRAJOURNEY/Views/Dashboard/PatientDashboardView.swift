import SwiftUI
import Combine

struct PatientDashboardView: View {
    @StateObject private var viewModel = PatientDashboardViewModel()
    @State private var showChat = false
    @Binding var isSideMenuShowing: Bool
    @State private var hasTakenMeds: Bool? = nil
    @State private var showToast = false
    @State private var toastMessage = ""
    @ObservedObject private var appState = AppState.shared
    
    var body: some View {
        ZStack(alignment: .bottom) {
            // Background with a subtle top-down gradient
            Group {
                Color.appBackground
                VStack {
                    LinearGradient(
                        colors: [Color.appAccent.opacity(0.08), Color.appBackground],
                        startPoint: .top,
                        endPoint: .center
                    )
                    .frame(height: 300)
                    Spacer()
                }
            }
            .ignoresSafeArea()
            
            VStack(spacing: 0) {
                // MARK: - Header
                DashboardHeader(title: "Patient Portal", isSideMenuShowing: $isSideMenuShowing, notificationCount: viewModel.notifications.filter { !$0.isRead }.count)
                
                ScrollView(showsIndicators: false) {
                    VStack(alignment: .leading, spacing: 32) {
                        // MARK: - Greeting
                        VStack(alignment: .leading, spacing: 6) {
                            Text("Good Morning,")
                                .font(.system(size: 16, weight: .medium))
                                .foregroundColor(.secondary)
                            Text("\(viewModel.overview?.patientName?.components(separatedBy: " ").first ?? appState.currentUser?.name?.components(separatedBy: " ").first ?? "Patient")!")
                                .font(.system(size: 36, weight: .bold, design: .rounded))
                                .foregroundColor(.primary)
                        }
                        .padding(.horizontal, 24)
                        .padding(.top, 24)
                        
                        // MARK: - AI Assistant
                        Button(action: { showChat = true }) {
                            HStack(spacing: 20) {
                                ZStack {
                                    RoundedRectangle(cornerRadius: 16)
                                        .fill(Design.Colors.primaryGradient)
                                        .frame(width: 60, height: 60)
                                    Image(systemName: "sparkles")
                                        .font(.system(size: 28))
                                        .foregroundColor(.white)
                                }
                                
                                VStack(alignment: .leading, spacing: 4) {
                                    Text("AI Health Assistant")
                                        .font(.system(size: 18, weight: .bold, design: .rounded))
                                        .foregroundColor(.primary)
                                    Text("Ask anything about your recovery")
                                        .font(.system(size: 14))
                                        .foregroundColor(.secondary)
                                }
                                
                                Spacer()
                                
                                Image(systemName: "chevron.right")
                                    .font(.system(size: 14, weight: .bold))
                                    .foregroundColor(.secondary.opacity(0.5))
                            }
                            .padding(20)
                            .background(Color.appCard)
                            .cornerRadius(24)
                            .shadow(color: Color.black.opacity(0.05), radius: 15, x: 0, y: 8)
                            .overlay(
                                RoundedRectangle(cornerRadius: 24)
                                    .stroke(Color.appBorder, lineWidth: 1)
                            )
                        }
                        .padding(.horizontal, 24)
                        
                        // MARK: - Medication Poll
                        VStack(alignment: .leading, spacing: 18) {
                            Text("Daily Check-in")
                                .font(.system(size: 20, weight: .bold, design: .rounded))
                                .padding(.horizontal, 24)
                            
                            VStack(spacing: 20) {
                                Text("Have you taken your medications today?")
                                    .font(.system(size: 16, weight: .medium))
                                    .foregroundColor(.primary.opacity(0.8))
                                    .multilineTextAlignment(.center)
                                
                                HStack(spacing: 16) {
                                    AdherenceButton(
                                        title: "YES",
                                        icon: "checkmark.circle.fill",
                                        color: .green,
                                        isSelected: hasTakenMeds == true
                                    ) { logMedication(taken: true) }
                                    
                                    AdherenceButton(
                                        title: "NO",
                                        icon: "xmark.circle.fill",
                                        color: .red,
                                        isSelected: hasTakenMeds == false
                                    ) { logMedication(taken: false) }
                                }
                            }
                            .padding(24)
                            .background(Color.appCard)
                            .cornerRadius(24)
                            .shadow(color: Color.black.opacity(0.04), radius: 12, x: 0, y: 6)
                            .overlay(
                                RoundedRectangle(cornerRadius: 24)
                                    .stroke(Color.appBorder, lineWidth: 1)
                            )
                            .padding(.horizontal, 24)
                        }
                        
                        // MARK: - Quick Services
                        VStack(alignment: .leading, spacing: 18) {
                            Text("Health Services")
                                .font(.system(size: 20, weight: .bold, design: .rounded))
                                .padding(.horizontal, 24)
                            
                            LazyVGrid(columns: [GridItem(.flexible(), spacing: 16), GridItem(.flexible(), spacing: 16)], spacing: 16) {
                                NavigationLink(destination: MedicationListView()) {
                                    ActionCard(title: "Medications", icon: "pills.fill", iconColor: .green)
                                }
                                
                                NavigationLink(destination: PatientRehabilitationView()) {
                                    ActionCard(title: "Rehab Plan", icon: "figure.walk.circle.fill", iconColor: .blue)
                                }
                                
                                NavigationLink(destination: ReportListView(isDoctor: false)) {
                                    ActionCard(title: "Clinical Reports", icon: "doc.text.fill", iconColor: .teal)
                                }
                                
                                NavigationLink(destination: AppointmentListView(isDoctor: false)) {
                                    ActionCard(title: "Schedule", icon: "calendar", iconColor: .orange)
                                }
                            }
                            .padding(.horizontal, 24)
                        }
                        
                        // MARK: - Upcoming Appointments
                        VStack(alignment: .leading, spacing: 18) {
                            HStack {
                                Text("Upcoming Appointments")
                                    .font(.system(size: 20, weight: .bold, design: .rounded))
                                Spacer()
                                NavigationLink(destination: AppointmentListView(isDoctor: false)) {
                                    Text("View All")
                                        .font(.system(size: 15, weight: .medium))
                                        .foregroundColor(.appAccent)
                                }
                            }
                            .padding(.horizontal, 24)
                            
                            if viewModel.upcomingAppointments.isEmpty {
                                EmptyStateView(image: "calendar.badge.clock", title: "No Appointments", message: "You have no upcoming consultations scheduled.")
                                    .padding(.horizontal, 24)
                            } else {
                                VStack(spacing: 16) {
                                    ForEach(viewModel.upcomingAppointments.prefix(2)) { appointment in
                                        AppointmentCard(appointment: appointment)
                                    }
                                }
                                .padding(.horizontal, 24)
                            }
                        }
                        .padding(.bottom, 120)
                    }
                }
            }
            
            // Toast Notification
            if showToast {
                HStack(spacing: 12) {
                    Image(systemName: "checkmark.circle.fill")
                        .foregroundColor(.green)
                        .font(.system(size: 18, weight: .bold))
                    Text(toastMessage)
                        .font(.system(size: 15, weight: .bold))
                        .foregroundColor(.white)
                }
                .padding(.vertical, 16)
                .padding(.horizontal, 24)
                .background(Color.black.opacity(0.85))
                .cornerRadius(18)
                .padding(.bottom, 100)
                .transition(.move(edge: .bottom).combined(with: .opacity))
                .onAppear {
                    DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
                        withAnimation { showToast = false }
                    }
                }
            }
        }
        .navigationBarHidden(true)
        .onAppear(perform: viewModel.loadData)
        .sheet(isPresented: $showChat) {
            ChatView()
        }
    }
    
    private func logMedication(taken: Bool) {
        withAnimation(.spring()) {
            hasTakenMeds = taken
        }
        let status = taken ? "YES" : "NO"
        
        // Server Sync Logic
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        
        let request = SymptomRequest(
            patientId: Int(SessionManager.shared.userId ?? "0") ?? 0,
            date: formatter.string(from: Date()),
            painLevel: 0,
            notes: "Medication adherence poll: \(status)"
        )
        
        PatientService.shared.createSymptom(request: request) { result in
            DispatchQueue.main.async {
                switch result {
                case .success:
                    toastMessage = "Medication status saved"
                    withAnimation { showToast = true }
                case .failure:
                    toastMessage = "Error saving status"
                    withAnimation { showToast = true }
                }
            }
        }
    }
}

#Preview {
    PatientDashboardView(isSideMenuShowing: .constant(false))
}


#Preview {
    PatientDashboardView(isSideMenuShowing: .constant(false))
}
