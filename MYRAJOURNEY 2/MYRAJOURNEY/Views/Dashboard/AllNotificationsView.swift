import SwiftUI

struct AllNotificationsView: View {
    @State private var notifications: [NotificationModel] = []
    @State private var isLoading = true
    
    var body: some View {
        ZStack {
            Color(UIColor.systemGroupedBackground).ignoresSafeArea()
            
            if isLoading {
                ProgressView().tint(.blue)
            } else if notifications.isEmpty {
                VStack(spacing: 20) {
                    Image(systemName: "bell.slash.fill")
                        .font(.system(size: 60))
                        .foregroundColor(.secondary.opacity(0.3))
                    Text("No Notifications")
                        .font(.system(size: 20, weight: .bold))
                        .foregroundColor(.primary)
                    Text("You're all caught up!")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                }
            } else {
                ScrollView {
                    LazyVStack(spacing: 16) {
                        ForEach(notifications) { notification in
                            NotificationRow(notification: notification)
                        }
                    }
                    .padding()
                }
            }
        }
        .navigationTitle("All Notifications")
        .navigationBarTitleDisplayMode(.inline)
        .onAppear(perform: loadNotifications)
    }
    
    private func loadNotifications() {
        isLoading = true
        PatientService.shared.getNotifications { result in
            DispatchQueue.main.async {
                self.isLoading = false
                if case .success(let response) = result, let data = response.data {
                    self.notifications = data
                }
            }
        }
    }
}



#Preview {
    NavigationView {
        AllNotificationsView()
    }
}
