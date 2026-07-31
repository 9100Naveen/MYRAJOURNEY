import SwiftUI

struct AdminDashboardView: View {
    @ObservedObject private var appState = AppState.shared
    @State private var showingLogoutAlert = false
    @Binding var isSideMenuShowing: Bool

    var body: some View {
        ZStack {
            Color.appBackground.ignoresSafeArea()

            ScrollView(showsIndicators: false) {
                VStack(alignment: .leading, spacing: 24) {
                    AppGradientHeader(
                        title: "Admin Dashboard",
                        subtitle: "Management Center",
                        showMenuButton: true,
                        menuAction: { withAnimation { isSideMenuShowing.toggle() } },
                        trailingAction: AnyView(
                            Button(action: { showingLogoutAlert = true }) {
                                Image(systemName: "rectangle.portrait.and.arrow.right")
                                    .font(.system(size: 18, weight: .bold))
                                    .foregroundColor(.white)
                                    .padding(12)
                                    .background(Color.white.opacity(0.2))
                                    .clipShape(Circle())
                            }
                        )
                    )
                    
                    VStack(spacing: 24) {
                        VStack(alignment: .leading, spacing: 20) {
                            Text("Quick Actions")
                                .font(.system(size: 20, weight: .bold))
                                .foregroundColor(.primary)
                                .padding(.horizontal, 4)

                            VStack(spacing: 14) {
                                NavigationLink(destination: CreateUserView(role: "patient")) {
                                    AdminModernButton(title: "Create Patient", icon: "plus.circle.fill", isFilled: true)
                                }

                                NavigationLink(destination: CreateUserView(role: "doctor")) {
                                    AdminModernButton(title: "Create Doctor", icon: "stethoscope", isFilled: true)
                                }

                                NavigationLink(destination: AssignPatientView()) {
                                    AdminModernButton(title: "Assign Patients", icon: "arrow.2.squarepath", isFilled: true)
                                }

                                NavigationLink(destination: AllPatientsView()) {
                                    AdminModernButton(title: "View All Patients", icon: "person.2.fill", isFilled: false)
                                }

                                NavigationLink(destination: AdminManagementView()) {
                                    AdminModernButton(title: "Manage Users", icon: "person.fill.xmark", isFilled: true)
                                }
                            }
                        }
                        .padding(26)
                        .background(Color.appSurface)
                        .cornerRadius(28)
                        .shadow(color: Color.black.opacity(0.08), radius: 18, x: 0, y: 10)
                        .padding(.horizontal, 16)
                        .padding(.top, 28)

                        Spacer(minLength: 40)
                    }
                }
            }
        }
        .toolbar(.hidden, for: .navigationBar)
        .edgesIgnoringSafeArea(.top)
        .alert("Logout", isPresented: $showingLogoutAlert) {
            Button("Cancel", role: .cancel) { }
            Button("Logout", role: .destructive) { appState.logout() }
        } message: {
            Text("Are you sure you want to logout?")
        }
    }
}

struct AdminModernButton: View {
    let title: String
    let icon: String
    let isFilled: Bool

    var body: some View {
        HStack(spacing: 16) {
            ZStack {
                Circle()
                    .fill(Color.white.opacity(isFilled ? 0.25 : 0.4))
                    .frame(width: 44, height: 44)
                Image(systemName: icon)
                    .font(.system(size: 20, weight: .semibold))
                    .foregroundColor(.white)
            }

            Text(title)
                .font(.system(size: 18, weight: .semibold))
                .foregroundColor(.white)

            Spacer()
        }
        .frame(maxWidth: .infinity)
        .frame(height: 60)
        .padding(.horizontal, 18)
        .background(
            LinearGradient(
                gradient: Gradient(colors:
                    isFilled
                    ? [Color.appAccent, Color.appAccent]   // solid look
                    : [Color.appAccent.opacity(0.15), Color.appAccent.opacity(0.08)]
                ),
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
        )
        .cornerRadius(28)
        .overlay(
            isFilled
            ? nil
            : RoundedRectangle(cornerRadius: 28)
                .stroke(Color.appAccent.opacity(0.3), lineWidth: 1.5)
        )
        .shadow(
            color: isFilled ? Color.appAccent.opacity(0.25) : Color.clear,
            radius: 12,
            x: 0,
            y: 6
        )
    }
}

#Preview {
    NavigationStack {
        AdminDashboardView(isSideMenuShowing: .constant(false))
    }
}
