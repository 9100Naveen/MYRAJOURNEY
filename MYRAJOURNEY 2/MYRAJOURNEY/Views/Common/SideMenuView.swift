import SwiftUI

struct SideMenuItem: Identifiable {
    var id = UUID()
    var title: String
    var icon: String
    var color: Color
    var destination: AnyView
}

struct SideMenuView: View {
    let role: String
    @Binding var isShowing: Bool
    var selectedTab: Binding<Int>? = nil
    @ObservedObject private var appState = AppState.shared
    
    @State private var selectedDestination: AnyView? = nil
    @State private var navigateToSelected = false
    
    var body: some View {
        ZStack(alignment: .leading) {
            // Background dim
            if isShowing {
                Color.black.opacity(0.3)
                    .ignoresSafeArea()
                    .onTapGesture {
                        withAnimation {
                            isShowing = false
                        }
                    }
            }
            
            // Sidebar content
            HStack {
                VStack(alignment: .leading, spacing: 0) {
                    headerView
                    
                    ScrollView {
                        VStack(alignment: .leading, spacing: 8) {
                            ForEach(menuItems) { item in
                                Button(action: {
                                    withAnimation {
                                        isShowing = false
                                    }
                                    
                                    DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
                                        if let selectedTab = selectedTab {
                                            if item.title == "Dashboard" {
                                                selectedTab.wrappedValue = 0
                                                return
                                            } else if item.title == "Medications" {
                                                selectedTab.wrappedValue = 1
                                                return
                                            } else if item.title == "Rehab" {
                                                selectedTab.wrappedValue = 2
                                                return
                                            }
                                        }
                                        
                                        selectedDestination = item.destination
                                        navigateToSelected = true
                                    }
                                }) {
                                    HStack(spacing: 16) {
                                        ZStack {
                                            Circle()
                                                .fill(item.color.opacity(0.15))
                                                .frame(width: 38, height: 38)
                                            Image(systemName: item.icon)
                                                .font(.system(size: 18, weight: .medium))
                                                .foregroundColor(item.color)
                                        }
                                        Text(item.title)
                                            .foregroundColor(.primary)
                                            .font(.system(size: 17, weight: .medium))
                                        Spacer()
                                    }
                                    .padding(.vertical, 14)
                                    .padding(.horizontal, 20)
                                    .background(Color.appSurface)
                                    .cornerRadius(16)
                                }
                            }
                            
                            // Preferences Section Title
                            Text("Preferences")
                                .font(.system(size: 16, weight: .medium))
                                .foregroundColor(.secondary)
                                .padding(.horizontal, 24)
                                .padding(.top, 24)
                                .padding(.bottom, 8)
                            
                            // Toggle Dark Theme
                            Button(action: {
                                withAnimation {
                                    appState.isDarkMode.toggle()
                                }
                            }) {
                                HStack(spacing: 20) {
                                    Image(systemName: "calendar.day.timeline.left")
                                        .font(.system(size: 22))
                                        .foregroundColor(.blue)
                                        .frame(width: 30)
                                    Text("Toggle Dark Theme")
                                        .foregroundColor(.primary.opacity(0.9))
                                        .font(.system(size: 18, weight: .medium))
                                    Spacer()
                                }
                                .padding(.vertical, 14)
                                .padding(.horizontal, 24)
                            }
                            
                            // Logout
                            Button(action: {
                                isShowing = false
                                appState.logout()
                            }) {
                                HStack(spacing: 20) {
                                    Image(systemName: "rectangle.portrait.and.arrow.right")
                                        .font(.system(size: 22))
                                        .foregroundColor(.red.opacity(0.8))
                                        .frame(width: 30)
                                    Text("Logout")
                                        .foregroundColor(.red.opacity(0.8))
                                        .font(.system(size: 18, weight: .bold))
                                    Spacer()
                                }
                                .padding(.vertical, 14)
                                .padding(.horizontal, 24)
                            }
                        }
                    }
                }
                .frame(width: 280)
                .background(Color.appSurface)
                .offset(x: isShowing ? 0 : -280)
                .animation(.easeInOut, value: isShowing)
                
                Spacer()
            }
        }
        .navigationDestination(isPresented: $navigateToSelected) {
            if let destination = selectedDestination {
                destination
            }
        }
    }
    
    private var headerView: some View {
        VStack(alignment: .leading, spacing: 14) {
            HStack {
                VStack(alignment: .leading, spacing: 4) {
                    Text("MyraJourney")
                        .font(.system(size: 22, weight: .bold))
                        .foregroundColor(.white)
                    Text("User menu")
                        .font(.subheadline)
                        .foregroundColor(.white.opacity(0.85))
                }
                Spacer()
                Button(action: {
                    withAnimation {
                        isShowing = false
                    }
                }) {
                    Image(systemName: "xmark")
                        .foregroundColor(.white)
                        .padding(10)
                        .background(Color.white.opacity(0.18))
                        .clipShape(Circle())
                }
            }
            
            HStack(spacing: 14) {
                ZStack {
                    Circle()
                        .fill(Color.white.opacity(0.22))
                        .frame(width: 62, height: 62)
                    Image(systemName: "person.fill")
                        .resizable()
                        .scaledToFit()
                        .frame(width: 32, height: 32)
                        .foregroundColor(.white)
                }
                VStack(alignment: .leading, spacing: 4) {
                    Text(role == "DOCTOR" ? "Dr. " + (SessionManager.shared.userEmail?.split(separator: "@").first?.capitalized ?? "User") : (SessionManager.shared.userEmail ?? "User"))
                        .font(.system(size: 20, weight: .bold))
                        .foregroundColor(.white)
                    Text(SessionManager.shared.userEmail ?? "user@example.com")
                        .font(.system(size: 15))
                        .foregroundColor(.white.opacity(0.85))
                }
            }
        }
        .padding(24)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(
            LinearGradient(
                gradient: Gradient(colors: [Color.blue.opacity(0.95), Color.purple.opacity(0.85)]),
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
        )
        .clipShape(RoundedRectangle(cornerRadius: 32, style: .continuous))
        .shadow(color: Color.black.opacity(0.2), radius: 20, x: 0, y: 8)
    }
    
    private var menuItems: [SideMenuItem] {
        switch role.uppercased() {
        case "PATIENT":
            return [
                SideMenuItem(title: "Dashboard", icon: "house.fill", color: .blue, destination: AnyView(PatientDashboardView(isSideMenuShowing: $isShowing))),
                SideMenuItem(title: "Medications", icon: "pills.fill", color: .green, destination: AnyView(MedicationListView())),
                SideMenuItem(title: "Rehab", icon: "figure.walk", color: .orange, destination: AnyView(PatientRehabilitationView())),
                SideMenuItem(title: "Education Hub", icon: "book.fill", color: .purple, destination: AnyView(EducationHubView())),
                SideMenuItem(title: "Appointments", icon: "calendar", color: .red, destination: AnyView(AppointmentListView(isDoctor: false))),
                SideMenuItem(title: "Symptom Log", icon: "thermometer", color: .pink, destination: AnyView(SymptomLogView())),
                SideMenuItem(title: "Reports", icon: "doc.text.fill", color: .indigo, destination: AnyView(ReportListView(isDoctor: false))),
                SideMenuItem(title: "Settings", icon: "gearshape.fill", color: .gray, destination: AnyView(UserSettingsDetailView()))
            ]
        case "DOCTOR":
            return [
                SideMenuItem(title: "Add Patient", icon: "plus.circle", color: .blue, destination: AnyView(CreateUserView(role: "patient"))),
                SideMenuItem(title: "All Patients", icon: "person.2.fill", color: .blue, destination: AnyView(PatientListView())),
                SideMenuItem(title: "Schedule", icon: "calendar.badge.clock", color: .blue, destination: AnyView(AppointmentListView(isDoctor: true))),
                SideMenuItem(title: "Add Appointment", icon: "plus", color: .blue, destination: AnyView(CreateAppointmentView())),
                SideMenuItem(title: "Reports", icon: "photo.on.rectangle.angled", color: .blue, destination: AnyView(ReportListView(isDoctor: true))),
                SideMenuItem(title: "Settings", icon: "wrench.and.screwdriver.fill", color: .blue, destination: AnyView(UserSettingsDetailView()))
            ]
        case "ADMIN":
            return [
                SideMenuItem(title: "Dashboard", icon: "house.fill", color: .blue, destination: AnyView(AdminDashboardView(isSideMenuShowing: $isShowing))),
                SideMenuItem(title: "Add Patient", icon: "person.badge.plus", color: .green, destination: AnyView(CreateUserView(role: "patient"))),
                SideMenuItem(title: "Add Doctor", icon: "stethoscope", color: .teal, destination: AnyView(CreateUserView(role: "doctor"))),
                SideMenuItem(title: "User Management", icon: "person.2.fill", color: .orange, destination: AnyView(AdminManagementView())),
                SideMenuItem(title: "Assign Patients", icon: "arrow.2.squarepath", color: .purple, destination: AnyView(AssignPatientView())),
                SideMenuItem(title: "Settings", icon: "gearshape.fill", color: .gray, destination: AnyView(SystemSettingsView()))
            ]
        default:
            return []
        }
    }
}
