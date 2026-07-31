import SwiftUI

struct SystemSettingsView: View {
    @State private var enablePushNotifications = true
    @State private var enableAutoBackup = true
    @State private var serverEndpoint = NetworkConfig.apiBaseURL
    @AppStorage("isDarkMode") private var isDarkMode = false
    
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                Text("System Settings")
                    .font(.system(size: 24, weight: .bold))
                    .foregroundColor(.primary)
                    .padding(.horizontal)
                
                VStack(spacing: 0) {
                    SettingsToggleRow(title: "Push Notifications", icon: "bell.fill", color: .red, isOn: $enablePushNotifications)
                    Divider()
                    SettingsToggleRow(title: "Dark Mode", icon: "moon.fill", color: .purple, isOn: $isDarkMode)
                    Divider()
                    SettingsToggleRow(title: "Automatic Backup", icon: "cloud.fill", color: .blue, isOn: $enableAutoBackup)
                }
                .background(Color.gray.opacity(0.05))
                .cornerRadius(15)
                .padding(.horizontal)
                
                Text("Server Configuration")
                    .font(.headline)
                    .foregroundColor(.primary)
                    .padding(.horizontal)
                
                VStack(alignment: .leading, spacing: 15) {
                    Text("Current Endpoint")
                        .font(.caption)
                        .foregroundColor(.secondary)
                    
                    TextField("Endpoint URL", text: $serverEndpoint)
                        .padding()
                        .background(Color.gray.opacity(0.1))
                        .cornerRadius(10)
                        .foregroundColor(.primary)
                        .autocapitalization(.none)
                        .disableAutocorrection(true)
                }
                .padding()
                .background(Color.gray.opacity(0.05))
                .cornerRadius(15)
                .padding(.horizontal)
                
                // Dangerous Actions
                VStack(alignment: .leading, spacing: 15) {
                    Text("Dangerous Actions")
                        .font(.headline)
                        .foregroundColor(.red)
                    
                    Button(action: {
                        AppState.shared.logout()
                    }) {
                        HStack {
                            Text("Delete Account")
                            Spacer()
                            Image(systemName: "person.crop.circle.badge.xmark")
                        }
                        .padding()
                        .background(Color.red.opacity(0.1))
                        .foregroundColor(.red)
                        .cornerRadius(10)
                    }
                }
                .padding()
                .background(Color.gray.opacity(0.05))
                .cornerRadius(15)
                .padding(.horizontal)
                
                VStack(alignment: .leading, spacing: 15) {
                    Text("About & Privacy")
                        .font(.headline)
                        .foregroundColor(.primary)
                    
                    Button(action: {
                        if let url = URL(string: "https://myrajourney.com/privacy") {
                            UIApplication.shared.open(url)
                        }
                    }) {
                        HStack {
                            Text("Privacy Policy")
                            Spacer()
                            Image(systemName: "lock.shield")
                        }
                        .padding()
                        .background(Color.blue.opacity(0.1))
                        .foregroundColor(.blue)
                        .cornerRadius(10)
                    }
                }
                .padding()
                .background(Color.gray.opacity(0.05))
                .cornerRadius(15)
                .padding(.horizontal)
            }
            .padding(.vertical)
        }
        .background(Color(UIColor.systemBackground).ignoresSafeArea())
        .navigationTitle("Global Settings")
        .navigationBarTitleDisplayMode(.inline)
    }
}

struct SettingsToggleRow: View {
    let title: String
    let icon: String
    let color: Color
    @Binding var isOn: Bool
    
    var body: some View {
        HStack {
            Image(systemName: icon)
                .foregroundColor(.white)
                .frame(width: 30, height: 30)
                .background(color)
                .cornerRadius(8)
            
            Text(title)
                .foregroundColor(.primary)
            
            Spacer()
            
            Toggle("", isOn: $isOn)
                .toggleStyle(SwitchToggleStyle(tint: color))
        }
        .padding()
    }
}

#Preview {
    NavigationView {
        SystemSettingsView()
    }
}
