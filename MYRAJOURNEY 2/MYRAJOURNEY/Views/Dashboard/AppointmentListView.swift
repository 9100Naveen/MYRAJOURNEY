import SwiftUI
import Combine

struct AppointmentListView: View {
    @State private var appointments: [Appointment] = []
    @State private var isLoading = true
    let isDoctor: Bool
    var patientId: Int? = nil
    var patientName: String? = nil
    
    var body: some View {
        ZStack {
            ScrollView(showsIndicators: false) {
                VStack(alignment: .leading, spacing: 24) {
                    AppGradientHeader(
                        title: "Schedule",
                        subtitle: patientName != nil ? "\(patientName!)'s Appointments" : (isDoctor ? "Physician Center" : "Your Visits"),
                        showMenuButton: false,
                        trailingAction: AnyView(addButton)
                    )
                    
                    VStack(spacing: 16) {
                        if isLoading {
                            ProgressView()
                                .padding(.vertical, 80)
                        } else if appointments.isEmpty {
                            VStack(spacing: 20) {
                                Image(systemName: "calendar.badge.exclamationmark")
                                    .font(.system(size: 60))
                                    .foregroundColor(.secondary.opacity(0.4))
                                Text("No Appointments")
                                    .font(.system(size: 18, weight: .bold))
                                    .foregroundColor(.secondary)
                            }
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 80)
                        } else {
                            ForEach(appointments) { appointment in
                                AppointmentCard(appointment: appointment)
                            }
                        }
                    }
                    .padding(.horizontal, 24)
                }
            }
            .background(Color.appBackground)
            .ignoresSafeArea(edges: .top)
        }
        .navigationBarHidden(true)
        .onAppear(perform: loadAppointments)
    }
    
    @ViewBuilder
    private var addButton: some View {
        if isDoctor {
            NavigationLink(destination: CreateAppointmentView()) {
                Image(systemName: "plus")
                    .font(.system(size: 18, weight: .bold))
                    .foregroundColor(.white)
                    .padding(12)
                    .background(Color.white.opacity(0.2))
                    .clipShape(Circle())
            }
        }
    }
    
    private func loadAppointments() {
        isLoading = true
        let userId = Int(SessionManager.shared.userId ?? "0") ?? 0
        
        if isDoctor {
            DoctorService.shared.getAppointments(patientId: patientId, doctorId: userId) { result in
                updateList(result)
            }
        } else {
            PatientService.shared.getAppointments { result in
                updateList(result)
            }
        }
    }
    
    private func updateList(_ result: Result<ApiResponse<[Appointment]>, NetworkError>) {
        DispatchQueue.main.async {
            isLoading = false
            if case .success(let response) = result, let data = response.data {
                self.appointments = data.sorted { ($0.startTime ?? "") < ($1.startTime ?? "") }
            }
        }
    }
}

#Preview("Patient") {
    NavigationView {
        AppointmentListView(isDoctor: false)
    }
}

#Preview("Doctor") {
    NavigationView {
        AppointmentListView(isDoctor: true)
    }
}
