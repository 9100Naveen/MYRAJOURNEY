import SwiftUI

struct DoctorDashboardView: View {
    @State private var overview: DoctorOverview?
    @State private var patients: [Patient] = []
    @State private var appointments: [Appointment] = []
    @State private var notifications: [NotificationModel] = []
    @State private var isLoading = true
    @Binding var isSideMenuShowing: Bool
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
                DashboardHeader(title: "Physician Center", isSideMenuShowing: $isSideMenuShowing, notificationCount: notifications.filter { !$0.isRead }.count)
                
                ScrollView(showsIndicators: false) {
                    VStack(alignment: .leading, spacing: 32) {
                        // MARK: - Greeting
                        VStack(alignment: .leading, spacing: 6) {
                            Text("Welcome back,")
                                .font(.system(size: 16, weight: .medium))
                                .foregroundColor(.secondary)
                            Text("Doctor!")
                                .font(.system(size: 36, weight: .bold, design: .rounded))
                                .foregroundColor(.primary)
                        }
                        .padding(.horizontal, 24)
                        .padding(.top, 24)
                        
                        // MARK: - Quick Actions
                        VStack(alignment: .leading, spacing: 18) {
                            Text("Quick Actions")
                                .font(.system(size: 20, weight: .bold, design: .rounded))
                                .padding(.horizontal, 24)
                            
                            LazyVGrid(columns: [GridItem(.flexible(), spacing: 16), GridItem(.flexible(), spacing: 16)], spacing: 16) {
                                NavigationLink(destination: CreateUserView(role: "patient")) {
                                    QuickActionTile(title: "New Patient", icon: "person.badge.plus", color: .blue)
                                }
                                
                                NavigationLink(destination: PhysicianAppointmentDispatchView()) {
                                    QuickActionTile(title: "Schedule", icon: "calendar.badge.clock", color: .purple)
                                }
                                
                                NavigationLink(destination: ReportListView(isDoctor: true)) {
                                    QuickActionTile(title: "Clinical Reports", icon: "doc.text.below.ecg", color: .teal)
                                }
                                
                                NavigationLink(destination: PatientListView()) {
                                    QuickActionTile(title: "Manage Care", icon: "heart.text.square", color: .orange)
                                }
                            }
                            .padding(.horizontal, 24)
                        }
                        
                        // MARK: - Notifications
                        VStack(alignment: .leading, spacing: 18) {
                            HStack {
                                Text("Recent Activity")
                                    .font(.system(size: 20, weight: .bold, design: .rounded))
                                Spacer()
                                Button(action: { loadData() }) {
                                    Image(systemName: "arrow.clockwise")
                                        .font(.system(size: 14, weight: .bold))
                                        .foregroundColor(.appAccent)
                                        .padding(8)
                                        .background(Color.appAccentSoft)
                                        .clipShape(Circle())
                                }
                                NavigationLink(destination: AllNotificationsView()) {
                                    Text("View All")
                                        .font(.system(size: 15, weight: .medium))
                                        .foregroundColor(.appAccent)
                                }
                            }
                            .padding(.horizontal, 24)
                            
                            VStack(spacing: 14) {
                                if notifications.isEmpty && !isLoading {
                                    EmptyStateView(image: "bell.slash", title: "All Clear", message: "No new activity or notifications at the moment.")
                                } else {
                                    ForEach(notifications.prefix(3)) { notification in
                                        NotificationRow(notification: notification)
                                    }
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
                                NavigationLink(destination: PhysicianAppointmentDispatchView()) {
                                    Text("View All")
                                        .font(.system(size: 15, weight: .medium))
                                        .foregroundColor(.appAccent)
                                }
                            }
                            .padding(.horizontal, 24)
                            
                            VStack(spacing: 18) {
                                if appointments.isEmpty && !isLoading {
                                    EmptyStateView(image: "calendar.badge.exclamationmark", title: "No Appointments", message: "You have no scheduled consultations for today.")
                                } else {
                                    ForEach(appointments.prefix(2)) { appointment in
                                        DetailedAppointmentCard(appointment: appointment)
                                    }
                                }
                            }
                            .padding(.horizontal, 24)
                        }
                        
                        // MARK: - Patients Record
                        VStack(alignment: .leading, spacing: 18) {
                            HStack {
                                Text("My Patients")
                                    .font(.system(size: 20, weight: .bold, design: .rounded))
                                Spacer()
                                NavigationLink(destination: PatientListView()) {
                                    Text("View All")
                                        .font(.system(size: 15, weight: .medium))
                                        .foregroundColor(.appAccent)
                                }
                            }
                            .padding(.horizontal, 24)
                            
                            VStack(spacing: 12) {
                                if patients.isEmpty && !isLoading {
                                    EmptyStateView(image: "person.2.slash", title: "No Patients Found", message: "You haven't assigned any patients to your care yet.")
                                } else {
                                    ForEach(patients.prefix(3)) { patient in
                                        NavigationLink(destination: PatientDetailsView(patient: patient)) {
                                            SimplePatientRow(patient: patient)
                                        }
                                    }
                                }
                            }
                            .padding(.horizontal, 24)
                        }
                        .padding(.bottom, 120)
                    }
                }
            }
            
            if isLoading {
                ZStack {
                    Color.white.opacity(0.8)
                    VStack(spacing: 16) {
                        ProgressView()
                            .scaleEffect(1.2)
                            .tint(.appAccent)
                        Text("Synchronizing data...")
                            .font(.system(size: 14, weight: .medium))
                            .foregroundColor(.secondary)
                    }
                }
                .ignoresSafeArea()
            }
        }
        .navigationBarHidden(true)
        .onAppear(perform: loadData)
    }
    
    private func loadData() {
        isLoading = true
        let group = DispatchGroup()
        
        group.enter()
        DoctorService.shared.getPatients { result in
            DispatchQueue.main.async {
                if case .success(let response) = result, let data = response.data {
                    self.patients = data.sorted(by: { $0.id > $1.id })
                }
                group.leave()
            }
        }
        
        group.enter()
        DoctorService.shared.getOverview { result in
            DispatchQueue.main.async {
                if case .success(let response) = result {
                    self.overview = response.data
                }
                group.leave()
            }
        }
        
        group.enter()
        let doctorIdInt = Int(SessionManager.shared.userId ?? "")
        DoctorService.shared.getAppointments(doctorId: doctorIdInt) { result in
            DispatchQueue.main.async {
                if case .success(let response) = result {
                    self.appointments = response.data?.filter({ $0.status?.uppercased() == "SCHEDULED" }) ?? []
                }
                group.leave()
            }
        }
        
        group.enter()
        PatientService.shared.getNotifications { result in
            DispatchQueue.main.async {
                if case .success(let response) = result {
                    self.notifications = response.data ?? []
                }
                group.leave()
            }
        }
        
        group.notify(queue: .main) {
            self.isLoading = false
        }
    }
}

// MARK: - Subviews

struct DetailedAppointmentCard: View {
    let appointment: Appointment
    
    var body: some View {
        VStack(alignment: .leading, spacing: 20) {
            HStack(alignment: .top) {
                VStack(alignment: .leading, spacing: 6) {
                    Text(appointment.patientName ?? "Patient Record")
                        .font(.system(size: 20, weight: .bold, design: .rounded))
                        .foregroundColor(.primary)
                    
                    Text(appointment.displayTitle)
                        .font(.system(size: 15, weight: .medium))
                        .foregroundColor(.secondary)
                }
                
                Spacer()
                
                Text(appointment.status?.uppercased() ?? "SCHEDULED")
                    .font(.system(size: 10, weight: .bold))
                    .padding(.horizontal, 10)
                    .padding(.vertical, 6)
                    .background(Color.green.opacity(0.1))
                    .foregroundColor(.green)
                    .clipShape(Capsule())
            }
            
            VStack(alignment: .leading, spacing: 12) {
                HStack(spacing: 16) {
                    Label {
                        Text(appointment.displayDate)
                            .font(.system(size: 14, weight: .semibold))
                    } icon: {
                        Image(systemName: "calendar")
                            .foregroundColor(.appAccent)
                    }
                    
                    Label {
                        Text(appointment.displayTimeSlot)
                            .font(.system(size: 14, weight: .semibold))
                    } icon: {
                        Image(systemName: "clock")
                            .foregroundColor(.appAccent)
                    }
                }
                
                Label {
                    Text(appointment.location ?? "Standard Clinical Consultation")
                        .font(.system(size: 14))
                        .foregroundColor(.secondary)
                } icon: {
                    Image(systemName: "mappin.and.ellipse")
                        .foregroundColor(.secondary)
                }
            }
            
            HStack(spacing: 12) {
                Button(action: {}) {
                    Text("Reschedule")
                        .font(.system(size: 14, weight: .bold))
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 12)
                        .background(Color.appAccentSoft)
                        .foregroundColor(.appAccent)
                        .cornerRadius(12)
                }
                
                Button(action: {}) {
                    Text("Cancel")
                        .font(.system(size: 14, weight: .bold))
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 12)
                        .background(Color.red.opacity(0.1))
                        .foregroundColor(.red)
                        .cornerRadius(12)
                }
            }
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
}

struct SimplePatientRow: View {
    let patient: Patient
    
    var body: some View {
        HStack(spacing: 16) {
            ZStack {
                Circle()
                    .fill(Color.appAccentSoft)
                    .frame(width: 52, height: 52)
                Text(String(patient.name.prefix(1)))
                    .font(.system(size: 20, weight: .bold))
                    .foregroundColor(.appAccent)
            }
            
            VStack(alignment: .leading, spacing: 4) {
                Text(patient.name)
                    .font(.system(size: 17, weight: .bold, design: .rounded))
                    .foregroundColor(.primary)
                Text("Age: \(patient.age ?? 0) • \(patient.email)")
                    .font(.system(size: 13))
                    .foregroundColor(.secondary)
                    .lineLimit(1)
            }
            
            Spacer()
            
            Image(systemName: "chevron.right")
                .font(.system(size: 14, weight: .bold))
                .foregroundColor(.secondary.opacity(0.5))
        }
        .padding(16)
        .background(Color.appCard)
        .cornerRadius(18)
        .shadow(color: Color.black.opacity(0.04), radius: 10, x: 0, y: 5)
        .overlay(
            RoundedRectangle(cornerRadius: 18)
                .stroke(Color.appBorder, lineWidth: 1)
        )
    }
}

struct PhysicianAppointmentDispatchView: View {
    var body: some View {
        AppointmentListView(isDoctor: true)
    }
}

#Preview {
    NavigationView {
        DoctorDashboardView(isSideMenuShowing: .constant(false))
    }
}


