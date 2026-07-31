import SwiftUI

struct UserSettingsDetailView: View {
    @Environment(\.dismiss) private var dismiss
    @ObservedObject private var appState = AppState.shared
    
    @State private var fullName = "sai"
    @State private var email = "sai@gmail.com"
    @State private var phoneNumber = ""
    @State private var dob = ""
    @State private var address = ""
    @State private var newPassword = ""
    
    @State private var showToast = false
    @State private var toastMessage = ""
    
    var body: some View {
        ZStack {
            Color(UIColor.systemGroupedBackground).ignoresSafeArea()
            
            ScrollView {
                VStack(spacing: 24) {
                    // Appearance Section
                    VStack(alignment: .leading, spacing: 16) {
                        Text("Appearance")
                            .font(.system(size: 20, weight: .bold))
                            .foregroundColor(.primary)
                        
                        HStack {
                            VStack(alignment: .leading, spacing: 4) {
                                Text("Dark Theme")
                                    .font(.system(size: 16, weight: .bold))
                                    .foregroundColor(.primary)
                                Text("Switch between light and dark mode")
                                    .font(.system(size: 14))
                                    .foregroundColor(.secondary)
                            }
                            
                            Spacer()
                            
                            Toggle("", isOn: $appState.isDarkMode)
                                .toggleStyle(SwitchToggleStyle(tint: .blue))
                        }
                        .padding()
                        .background(appState.isDarkMode ? Color.blue.opacity(0.1) : Color(red: 0.92, green: 0.96, blue: 1.0))
                        .cornerRadius(12)
                    }
                    .padding()
                    .background(Color(UIColor.secondarySystemGroupedBackground))
                    .cornerRadius(20)
                    .shadow(color: .black.opacity(appState.isDarkMode ? 0.2 : 0.05), radius: 10, x: 0, y: 5)
                    .padding(.horizontal)
                    
                    // Account Information Section
                    VStack(alignment: .leading, spacing: 20) {
                        Text("Account Information")
                            .font(.system(size: 20, weight: .bold))
                            .foregroundColor(.primary)
                        
                        VStack(alignment: .leading, spacing: 16) {
                            SettingsInputField(title: "Full Name", placeholder: "Full Name", text: $fullName)
                            SettingsInputField(title: "Email Address", placeholder: "Email Address", text: $email)
                            SettingsInputField(title: "Phone Number", placeholder: "Phone Number", text: $phoneNumber)
                            SettingsInputField(title: "Date of Birth", placeholder: "YYYY-MM-DD", text: $dob, hasCalendar: true)
                            SettingsInputField(title: "Address", placeholder: "Address", text: $address)
                            SettingsInputField(title: "New Password (Optional)", placeholder: "Enter new password to change", text: $newPassword)
                        }
                        
                        Button(action: saveChanges) {
                            Text("Save")
                                .font(.system(size: 18, weight: .bold))
                                .foregroundColor(.white)
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 16)
                                .background(Color.blue)
                                .cornerRadius(12)
                        }
                        .padding(.top, 8)
                    }
                    .padding()
                    .background(Color(UIColor.secondarySystemGroupedBackground))
                    .cornerRadius(20)
                    .shadow(color: .black.opacity(appState.isDarkMode ? 0.2 : 0.05), radius: 10, x: 0, y: 5)
                    .padding(.horizontal)
                    
                    // Danger Zone Section
                    VStack(alignment: .leading, spacing: 16) {
                        Text("Danger Zone")
                            .font(.system(size: 20, weight: .bold))
                            .foregroundColor(.primary)
                        
                        VStack(alignment: .leading, spacing: 12) {
                            Text("Delete your account permanently if you no longer want access.")
                                .font(.system(size: 14))
                                .foregroundColor(.primary.opacity(0.8))
                            
                            Button(action: deleteAccount) {
                                Text("Delete Account")
                                    .font(.system(size: 16, weight: .bold))
                                    .foregroundColor(.white)
                                    .frame(maxWidth: .infinity)
                                    .padding(.vertical, 12)
                                    .background(Color.red)
                                    .cornerRadius(8)
                            }
                        }
                        .padding()
                        .background(appState.isDarkMode ? Color.red.opacity(0.1) : Color(red: 1.0, green: 0.94, blue: 0.94))
                        .cornerRadius(12)
                    }
                    .padding()
                    .background(Color(UIColor.secondarySystemGroupedBackground))
                    .cornerRadius(20)
                    .shadow(color: .black.opacity(appState.isDarkMode ? 0.2 : 0.05), radius: 10, x: 0, y: 5)
                    .padding(.horizontal)
                    .padding(.bottom, 40)
                }
                .padding(.top, 10)
            }
            
            // Toast Notification
            if showToast {
                VStack {
                    Spacer()
                    HStack(spacing: 12) {
                        Image(systemName: "heart.text.square")
                            .foregroundColor(.cyan)
                        Text(toastMessage)
                            .font(.system(size: 15, weight: .medium))
                            .foregroundColor(.white)
                        Spacer()
                    }
                    .padding(.vertical, 14)
                    .padding(.horizontal, 20)
                    .background(Color.black.opacity(0.85))
                    .cornerRadius(12)
                    .padding(.horizontal, 30)
                    .padding(.bottom, 50)
                }
                .transition(.move(edge: .bottom).combined(with: .opacity))
            }
        }
        .navigationTitle("Settings")
        .navigationBarTitleDisplayMode(.inline)
        .onAppear {
            loadUserData()
        }
    }
    
    private func loadUserData() {
        if let emailStored = SessionManager.shared.userEmail {
            self.email = emailStored
            self.fullName = emailStored.split(separator: "@").first?.capitalized ?? "User"
        }
    }
    
    private func saveChanges() {
        withAnimation {
            toastMessage = "Profile updated successfully"
            showToast = true
        }
        
        DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
            withAnimation {
                showToast = false
            }
        }
    }
    
    private func deleteAccount() {
        withAnimation {
            toastMessage = "Request submitted"
            showToast = true
        }
        
        DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
            withAnimation {
                showToast = false
            }
        }
    }
}

struct SettingsInputField: View {
    let title: String
    let placeholder: String
    @Binding var text: String
    var hasCalendar: Bool = false
    @Environment(\.colorScheme) var colorScheme
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title)
                .font(.system(size: 14, weight: .semibold))
                .foregroundColor(.primary.opacity(0.8))
            
            HStack {
                TextField(placeholder, text: $text)
                    .font(.system(size: 16))
                    .foregroundColor(.primary)
                
                if hasCalendar {
                    Image(systemName: "calendar.badge.plus")
                        .foregroundColor(.secondary)
                }
            }
            .padding()
            .background(Color(UIColor.systemBackground))
            .cornerRadius(10)
            .overlay(
                RoundedRectangle(cornerRadius: 10)
                    .stroke(colorScheme == .dark ? Color.white.opacity(0.3) : Color.black, lineWidth: 1)
            )
        }
    }
}

#Preview {
    NavigationView {
        UserSettingsDetailView()
    }
}
