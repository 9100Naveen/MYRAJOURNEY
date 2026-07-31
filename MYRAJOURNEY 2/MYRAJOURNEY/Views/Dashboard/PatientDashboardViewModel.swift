import SwiftUI
import Combine

class PatientDashboardViewModel: ObservableObject {
    @Published var overview: PatientOverview?
    @Published var upcomingAppointments: [Appointment] = []
    @Published var notifications: [NotificationModel] = []
    @Published var isLoading = false
    @Published var errorMessage: String?
    
    func loadData() {
        isLoading = true
        errorMessage = nil
        
        let group = DispatchGroup()
        
        group.enter()
        PatientService.shared.getOverview { result in
            DispatchQueue.main.async {
                switch result {
                case .success(let response):
                    self.overview = response.data
                case .failure(let error):
                    self.errorMessage = error.localizedDescription
                }
                group.leave()
            }
        }
        
        group.enter()
        PatientService.shared.getAppointments { result in
            DispatchQueue.main.async {
                if case .success(let response) = result, let data = response.data {
                    self.upcomingAppointments = data
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
