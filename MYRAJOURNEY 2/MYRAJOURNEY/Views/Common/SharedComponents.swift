import SwiftUI

extension Color {
    static let appBackground = Design.Colors.background
    static let appSurface = Design.Colors.surface
    static let appCard = Design.Colors.card
    static let appAccent = Design.Colors.primary
    static let appAccentDark = Color(red: 0.04, green: 0.33, blue: 0.78)
    static let appAccentSoft = Design.Colors.primary.opacity(0.12)
    static let appBorder = Color.gray.opacity(0.1)
}

struct ElevatedCard: ViewModifier {
    func body(content: Content) -> some View {
        content
            .background(Color.appCard)
            .cornerRadius(24)
            .shadow(color: Color.black.opacity(0.08), radius: 18, x: 0, y: 8)
            .overlay(
                RoundedRectangle(cornerRadius: 24)
                    .stroke(Color.appBorder, lineWidth: 1)
            )
    }
}

extension View {
    func elevatedCard() -> some View {
        modifier(ElevatedCard())
    }
}

struct PrimaryButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.system(size: 17, weight: .bold, design: .rounded))
            .foregroundColor(.white)
            .padding(.vertical, 16)
            .padding(.horizontal, 24)
            .frame(maxWidth: .infinity)
            .background(Design.Colors.primaryGradient)
            .cornerRadius(20)
            .scaleEffect(configuration.isPressed ? 0.97 : 1.0)
            .shadow(color: Design.Colors.primary.opacity(0.3), radius: 12, x: 0, y: 6)
            .animation(.spring(response: 0.3, dampingFraction: 0.6), value: configuration.isPressed)
    }
}

struct SecondaryButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.system(size: 16, weight: .semibold))
            .foregroundColor(Color.appAccentDark)
            .padding()
            .frame(maxWidth: .infinity)
            .background(Color.appSurface)
            .cornerRadius(18)
            .overlay(
                RoundedRectangle(cornerRadius: 18)
                    .stroke(Color.appBorder, lineWidth: 1)
            )
            .opacity(configuration.isPressed ? 0.86 : 1.0)
    }
}

// MARK: - ActionCard
struct ActionCard: View {
    let title: String
    let icon: String
    var cardColor: Color = Color.appSurface
    var iconColor: Color = .appAccent
    
    var body: some View {
        VStack(spacing: 16) {
            ZStack {
                Circle()
                    .fill(iconColor.opacity(0.12))
                    .frame(width: 56, height: 56)
                Image(systemName: icon)
                    .font(.system(size: 24, weight: .bold))
                    .foregroundColor(iconColor)
            }
            
            Text(title)
                .font(.system(size: 13, weight: .bold, design: .rounded))
                .foregroundColor(.primary.opacity(0.8))
                .multilineTextAlignment(.center)
                .lineLimit(2)
        }
        .padding(.vertical, 24)
        .padding(.horizontal, 12)
        .frame(maxWidth: .infinity)
        .background(Color.appCard)
        .cornerRadius(24)
        .shadow(color: Color.black.opacity(0.05), radius: 12, x: 0, y: 6)
        .overlay(
            RoundedRectangle(cornerRadius: 24)
                .stroke(Color.appBorder, lineWidth: 1)
        )
    }
}

// MARK: - PatientRow (Old Avatar Style)
struct PatientRow: View {
    let patient: Patient
    
    var body: some View {
        HStack(spacing: 16) {
            // Avatar
            ZStack {
                Circle()
                    .fill(Color.appAccentSoft)
                    .frame(width: 50, height: 50)
                Text(String(patient.name.prefix(1)))
                    .font(.headline)
                    .foregroundColor(.appAccent)
            }
            
            VStack(alignment: .leading, spacing: 4) {
                Text(patient.name)
                    .font(.system(size: 16, weight: .bold, design: .rounded))
                    .foregroundColor(.primary)
                Text("Age: \(patient.age ?? 0) • \(patient.gender ?? "N/A")")
                    .font(.system(size: 14))
                    .foregroundColor(.secondary)
            }
            
            Spacer()
            
            Image(systemName: "chevron.right")
                .font(.system(size: 14, weight: .bold))
                .foregroundColor(.secondary.opacity(0.4))
        }
        .padding()
        .background(Color.appCard)
        .cornerRadius(16)
        .shadow(color: Color.black.opacity(0.04), radius: 8, x: 0, y: 4)
    }
}

// MARK: - PatientRecordRow (New Screenshot Style)
struct PatientRecordRow: View {
    let patient: Patient
    @Environment(\.colorScheme) var colorScheme
    
    var body: some View {
        HStack(spacing: 16) {
            Circle()
                .fill(Color.appAccentSoft)
                .frame(width: 56, height: 56)
                .overlay(
                    Text(String(patient.name.prefix(1)))
                        .font(.system(size: 24, weight: .bold, design: .rounded))
                        .foregroundColor(.appAccent)
                )
            
            VStack(alignment: .leading, spacing: 4) {
                Text(patient.name)
                    .font(.system(size: 18, weight: .bold, design: .rounded))
                    .foregroundColor(.primary)
                
                Text("Age: \(patient.age ?? 0) • \(patient.email)")
                    .font(.system(size: 13))
                    .foregroundColor(.secondary)
                    .lineLimit(1)
            }
            
            Spacer()
            
            HStack(spacing: 12) {
                Button(action: {}) {
                    Image(systemName: "calendar.badge.clock")
                        .font(.system(size: 14, weight: .bold))
                        .foregroundColor(.appAccent)
                        .padding(10)
                        .background(Color.appAccentSoft)
                        .clipShape(Circle())
                }
                
                Button(action: {}) {
                    Image(systemName: "doc.text")
                        .font(.system(size: 14, weight: .bold))
                        .foregroundColor(.secondary)
                        .padding(10)
                        .background(Color.gray.opacity(0.08))
                        .clipShape(Circle())
                }
            }
        }
        .padding(16)
        .background(Color.appCard)
        .cornerRadius(24)
        .shadow(color: Color.black.opacity(0.04), radius: 12, x: 0, y: 6)
        .overlay(
            RoundedRectangle(cornerRadius: 24)
                .stroke(Color.appBorder, lineWidth: 0.5)
        )
    }
}

// MARK: - StatCard
struct StatCard: View {
    let title: String
    let value: String
    let icon: String
    let color: Color
    
    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            ZStack {
                RoundedRectangle(cornerRadius: 12)
                    .fill(color.opacity(0.12))
                    .frame(width: 44, height: 44)
                Image(systemName: icon)
                    .font(.system(size: 18, weight: .bold))
                    .foregroundColor(color)
            }
            
            VStack(alignment: .leading, spacing: 4) {
                Text(value)
                    .font(.system(size: 32, weight: .bold, design: .rounded))
                    .foregroundColor(.primary)
                
                Text(title.uppercased())
                    .font(.system(size: 11, weight: .bold))
                    .foregroundColor(.secondary)
                    .tracking(0.5)
            }
        }
        .padding(24)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color.appCard)
        .cornerRadius(28)
        .shadow(color: Color.black.opacity(0.05), radius: 15, x: 0, y: 8)
        .overlay(
            RoundedRectangle(cornerRadius: 28)
                .stroke(Color.appBorder, lineWidth: 1)
        )
    }
}

// MARK: - ModernInputField
struct ModernInputField: View {
    let title: String
    @Binding var text: String
    let icon: String
    var keyboardType: UIKeyboardType = .default
    var textContentType: UITextContentType? = nil

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title)
                .font(.system(size: 13, weight: .bold, design: .rounded))
                .foregroundColor(.secondary)
                .padding(.leading, 4)
            
            HStack(spacing: 12) {
                Image(systemName: icon)
                    .foregroundColor(.appAccent)
                    .frame(width: 20)
                TextField(title, text: $text)
                    .keyboardType(keyboardType)
                    .textContentType(textContentType)
                    .autocorrectionDisabled(true)
                    .foregroundColor(.primary)
            }
            .padding()
            .background(Color.appCard)
            .cornerRadius(16)
            .overlay(
                RoundedRectangle(cornerRadius: 16)
                    .stroke(Color.appBorder, lineWidth: 1.5)
            )
        }
    }
}

struct ModernSecureField: View {
    let title: String
    @Binding var text: String
    let icon: String
    @State private var isSecure = true

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title)
                .font(.system(size: 13, weight: .bold, design: .rounded))
                .foregroundColor(.secondary)
                .padding(.leading, 4)
            
            HStack(spacing: 12) {
                Image(systemName: icon)
                    .foregroundColor(.appAccent)
                    .frame(width: 20)
                if isSecure {
                    SecureField(title, text: $text)
                        .autocorrectionDisabled(true)
                        .textContentType(.password)
                        .foregroundColor(.primary)
                } else {
                    TextField(title, text: $text)
                        .autocorrectionDisabled(true)
                        .textContentType(.password)
                        .foregroundColor(.primary)
                }
                Button(action: { isSecure.toggle() }) {
                    Image(systemName: isSecure ? "eye.slash" : "eye")
                        .foregroundColor(.secondary)
                }
            }
            .padding()
            .background(Color.appCard)
            .cornerRadius(16)
            .overlay(
                RoundedRectangle(cornerRadius: 16)
                    .stroke(Color.appBorder, lineWidth: 1.5)
            )
        }
    }
}
// MARK: - AppointmentCard
struct AppointmentCard: View {
    let appointment: Appointment
    
    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            HStack {
                VStack(alignment: .leading, spacing: 6) {
                    Text(appointment.displayTitle)
                        .font(.system(size: 18, weight: .bold, design: .rounded))
                    Text(appointment.displayDate)
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                }
                Spacer()
                ZStack {
                    Circle()
                        .fill(Color.appAccentSoft)
                        .frame(width: 44, height: 44)
                    Image(systemName: "calendar")
                        .foregroundColor(.appAccent)
                }
            }
            
            Text(appointment.displayTimeSlot)
                .font(.system(size: 17, weight: .bold, design: .rounded))
                .foregroundColor(.primary)
            
            if let doctor = appointment.doctorName {
                Label("Dr. \(doctor)", systemImage: "person.text.rectangle")
                    .font(.caption)
                    .foregroundColor(.appAccent)
                    .padding(.horizontal, 10)
                    .padding(.vertical, 4)
                    .background(Color.appAccentSoft)
                    .clipShape(Capsule())
            }
        }
        .padding(20)
        .background(Color.appCard)
        .cornerRadius(22)
        .shadow(color: Color.black.opacity(0.06), radius: 14, x: 0, y: 8)
        .overlay(
            RoundedRectangle(cornerRadius: 22)
                .stroke(Color.appBorder, lineWidth: 1)
        )
    }
}

// MARK: - EmptyStateView
struct EmptyStateView: View {
    var image: String
    var title: String
    let message: String
    
    init(image: String = "doc.text.magnifyingglass", title: String = "No Records Available", message: String) {
        self.image = image
        self.title = title
        self.message = message
    }
    
    var body: some View {
        VStack(spacing: 24) {
            ZStack {
                Circle()
                    .fill(Color.appAccentSoft)
                    .frame(width: 90, height: 90)
                Image(systemName: image)
                    .font(.system(size: 38))
                    .foregroundColor(.appAccent.opacity(0.6))
            }
            
            VStack(spacing: 8) {
                Text(title)
                    .font(.system(size: 20, weight: .bold, design: .rounded))
                Text(message)
                    .font(.system(size: 15))
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 32)
            }
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 50)
        .background(Color.appCard)
        .cornerRadius(28)
        .overlay(
            RoundedRectangle(cornerRadius: 28)
                .stroke(Color.appBorder, lineWidth: 1)
        )
    }
}
// MARK: - NotificationRow
struct NotificationRow: View {
    let notification: NotificationModel
    
    var body: some View {
        HStack(alignment: .top, spacing: 16) {
            ZStack {
                Circle()
                    .fill(notification.isRead ? Color.gray.opacity(0.1) : Color.appAccentSoft)
                    .frame(width: 44, height: 44)
                Image(systemName: notificationIcon)
                    .font(.system(size: 18))
                    .foregroundColor(notification.isRead ? .gray : .appAccent)
            }
            
            VStack(alignment: .leading, spacing: 6) {
                HStack {
                    Text(notification.title)
                        .font(.system(size: 17, weight: .bold, design: .rounded))
                        .foregroundColor(.primary)
                    
                    Spacer()
                    
                    if !notification.isRead {
                        Circle()
                            .fill(Color.red)
                            .frame(width: 8, height: 8)
                    }
                }
                
                Text(notification.body)
                    .font(.system(size: 14))
                    .foregroundColor(.secondary)
                    .lineLimit(2)
                
                if let date = notification.createdAt {
                    Text(date)
                        .font(.system(size: 12))
                        .foregroundColor(.secondary.opacity(0.6))
                        .padding(.top, 2)
                }
            }
        }
        .padding(18)
        .background(Color.appCard)
        .cornerRadius(24)
        .shadow(color: Color.black.opacity(0.04), radius: 10, x: 0, y: 5)
        .overlay(
            RoundedRectangle(cornerRadius: 24)
                .stroke(Color.appBorder, lineWidth: 1)
        )
    }
    
    private var notificationIcon: String {
        let title = notification.title.lowercased()
        if title.contains("report") { return "doc.badge.plus" }
        if title.contains("symptom") { return "heart.text.square" }
        if title.contains("appointment") { return "calendar" }
        return "bell.fill"
    }
}

// MARK: - DashboardHeader
struct DashboardHeader: View {
    let title: String
    @Binding var isSideMenuShowing: Bool
    let notificationCount: Int
    
    var body: some View {
        HStack(spacing: 16) {
            Button(action: { withAnimation(.spring()) { isSideMenuShowing.toggle() } }) {
                Image(systemName: "line.3.horizontal.decrease")
                    .font(.system(size: 20, weight: .semibold))
                    .foregroundColor(.primary)
                    .frame(width: 44, height: 44)
                    .background(Color.appSurface)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
            }
            
            Text(title)
                .font(.system(size: 22, weight: .bold, design: .rounded))
                .foregroundColor(.primary)
            
            Spacer()
            
            HStack(spacing: 12) {
                
                Button(action: { AppState.shared.logout() }) {
                    Image(systemName: "rectangle.portrait.and.arrow.right")
                        .font(.system(size: 18, weight: .bold))
                        .foregroundColor(.white)
                        .frame(width: 44, height: 44)
                        .background(Design.Colors.primaryGradient)
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                        .shadow(color: Design.Colors.primary.opacity(0.3), radius: 8, x: 0, y: 4)
                }
            }
        }
        .padding(.horizontal, 24)
        .padding(.vertical, 16)
        .background(Color.white)
        .shadow(color: Color.black.opacity(0.03), radius: 10, x: 0, y: 5)
    }
}

// MARK: - QuickActionTile
struct QuickActionTile: View {
    let title: String
    let icon: String
    let color: Color
    
    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            ZStack {
                RoundedRectangle(cornerRadius: 12)
                    .fill(color.opacity(0.1))
                    .frame(width: 48, height: 48)
                Image(systemName: icon)
                    .font(.system(size: 22))
                    .foregroundColor(color)
            }
            
            Text(title)
                .font(.system(size: 14, weight: .bold, design: .rounded))
                .foregroundColor(.primary.opacity(0.8))
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(18)
        .background(Color.appCard)
        .cornerRadius(20)
        .shadow(color: Color.black.opacity(0.04), radius: 12, x: 0, y: 6)
        .overlay(
            RoundedRectangle(cornerRadius: 20)
                .stroke(Color.appBorder, lineWidth: 1)
        )
    }
}
// MARK: - AdherenceButton
struct AdherenceButton: View {
    let title: String
    let icon: String
    let color: Color
    let isSelected: Bool
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            HStack(spacing: 8) {
                Image(systemName: icon)
                Text(title)
                    .font(.system(size: 15, weight: .bold, design: .rounded))
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 16)
            .background(isSelected ? color : Color.appSurface)
            .foregroundColor(isSelected ? .white : color)
            .cornerRadius(16)
            .overlay(
                RoundedRectangle(cornerRadius: 16)
                    .stroke(color.opacity(0.3), lineWidth: isSelected ? 0 : 1.5)
            )
            .shadow(color: isSelected ? color.opacity(0.3) : Color.clear, radius: 8, x: 0, y: 4)
        }
    }
}
