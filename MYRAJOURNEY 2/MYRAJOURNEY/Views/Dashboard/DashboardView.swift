import SwiftUI

struct DashboardView: View {
    @StateObject private var appState = AppState.shared
    @State private var isSideMenuShowing = false
    @State private var selectedTab = 0
    
    var body: some View {
        Group {
            if appState.userRole?.uppercased() == "DOCTOR" {
                TabView {
                    NavigationStack {
                        ZStack {
                            DoctorDashboardView(isSideMenuShowing: $isSideMenuShowing)
                            SideMenuView(role: "DOCTOR", isShowing: $isSideMenuShowing)
                        }
                        .toolbar(.hidden, for: .navigationBar)
                    }
                    .tabItem {
                        Label("Dashboard", systemImage: "line.3.horizontal")
                    }
                    
                    NavigationStack {
                        PatientListView()
                    }
                    .tabItem {
                        Label("Patients", systemImage: "person.fill")
                    }
                    
                    NavigationStack {
                        AppointmentListView(isDoctor: true)
                    }
                    .tabItem {
                        Label("Schedule", systemImage: "figure.walk")
                    }
                    
                    NavigationStack {
                        ReportListView(isDoctor: true)
                    }
                    .tabItem {
                        Label("Reports", systemImage: "doc.text.fill")
                    }
                }
                .background(Color.appBackground.ignoresSafeArea())
            } else if appState.userRole?.uppercased() == "PATIENT" {
                TabView(selection: $selectedTab) {
                    NavigationStack {
                        ZStack {
                            PatientDashboardView(isSideMenuShowing: $isSideMenuShowing)
                            SideMenuView(role: "PATIENT", isShowing: $isSideMenuShowing, selectedTab: $selectedTab)
                        }
                        .toolbar(.hidden, for: .navigationBar)
                    }
                    .tag(0)
                    .tabItem {
                        Image(systemName: "doc.text.fill")
                        Text("Symptoms")
                    }
                    
                    NavigationStack {
                        MedicationListView()
                            .toolbar(.hidden, for: .navigationBar)
                    }
                    .tag(1)
                    .tabItem {
                        Image(systemName: "pills.fill")
                        Text("Meds")
                    }
                    
                    NavigationStack {
                        PatientRehabilitationView()
                            .toolbar(.hidden, for: .navigationBar)
                    }
                    .tag(2)
                    .tabItem {
                        Image(systemName: "dumbbell.fill")
                        Text("Rehab")
                    }
                }
                .accentColor(.appAccent)
            } else {
                NavigationStack {
                    ZStack {
                        Group {
                            if appState.userRole?.uppercased() == "ADMIN" {
                                AdminDashboardView(isSideMenuShowing: $isSideMenuShowing)
                            } else {
                                // Default fallback
                                VStack {
                                    Text("Dashboard")
                                        .font(.title)
                                    Text("Role: \(appState.userRole ?? "Unknown")")
                                    Button("Logout") { appState.logout() }
                                }
                            }
                        }
                        
                        SideMenuView(role: appState.userRole ?? "", isShowing: $isSideMenuShowing)
                    }
                    .background(Color(UIColor.systemBackground))
                    .toolbar(.hidden, for: .navigationBar)
                    .navigationTitle("")
                    .navigationBarTitleDisplayMode(.inline)
                }
            }
        }
    }
}


#Preview {
    DashboardView()
}
