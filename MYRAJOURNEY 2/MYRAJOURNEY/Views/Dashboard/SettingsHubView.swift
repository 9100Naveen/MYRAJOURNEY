import SwiftUI

struct SettingsHubView: View {
    private let options: [SettingsOption] = [
        SettingsOption(
            title: "Account Settings",
            subtitle: "Edit your profile, email, phone, and password",
            icon: "person.crop.circle",
            destination: AnyView(UserSettingsDetailView())
        ),
        SettingsOption(
            title: "App Settings",
            subtitle: "Manage dark mode, notifications, and privacy",
            icon: "gearshape",
            destination: AnyView(SystemSettingsView())
        )
    ]
    
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                Text("Settings")
                    .font(.system(size: 34, weight: .bold))
                    .padding(.horizontal)
                    .padding(.top, 24)
                    
                ForEach(options) { option in
                    NavigationLink(destination: option.destination) {
                        HStack(alignment: .top, spacing: 16) {
                            ZStack {
                                RoundedRectangle(cornerRadius: 16)
                                    .fill(Color.blue.opacity(0.15))
                                    .frame(width: 52, height: 52)
                                Image(systemName: option.icon)
                                    .font(.system(size: 22, weight: .semibold))
                                    .foregroundColor(.blue)
                            }
                            VStack(alignment: .leading, spacing: 6) {
                                Text(option.title)
                                    .font(.system(size: 18, weight: .semibold))
                                    .foregroundColor(.primary)
                                Text(option.subtitle)
                                    .font(.system(size: 14))
                                    .foregroundColor(.secondary)
                                    .lineLimit(2)
                            }
                            Spacer()
                            Image(systemName: "chevron.right")
                                .foregroundColor(.secondary)
                        }
                        .padding()
                        .background(Color(UIColor.secondarySystemBackground))
                        .cornerRadius(20)
                        .shadow(color: Color.black.opacity(0.03), radius: 8, x: 0, y: 4)
                    }
                    .buttonStyle(PlainButtonStyle())
                    .padding(.horizontal)
                }
                Spacer(minLength: 40)
            }
        }
        .background(Color(UIColor.systemBackground).ignoresSafeArea())
    }
}

private struct SettingsOption: Identifiable {
    let id = UUID()
    let title: String
    let subtitle: String
    let icon: String
    let destination: AnyView
}

#Preview {
    NavigationStack {
        SettingsHubView()
    }
}
