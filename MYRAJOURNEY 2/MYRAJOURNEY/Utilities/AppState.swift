import SwiftUI
import Combine

class AppState: ObservableObject {
    @Published var isLoggedIn: Bool = false
    @Published var userRole: String?
    @Published var currentUser: User?
    @AppStorage("isDarkMode") var isDarkMode: Bool = false
    
    static let shared = AppState()
    
    private init() {
        self.syncWithSession()
    }
    
    func syncWithSession() {
        self.isLoggedIn = SessionManager.shared.isLoggedIn
        self.userRole = SessionManager.shared.userRole
        if isLoggedIn, let name = SessionManager.shared.userName, let email = SessionManager.shared.userEmail, let role = SessionManager.shared.userRole, let idStr = SessionManager.shared.userId, let id = Int(idStr) {
            // Restore a minimal user object if needed, or just let partial state exist
            self.currentUser = User(id: id, name: name, email: email, role: role, assignedDoctorId: nil, assignedDoctorName: nil, phone: nil, address: nil, age: nil, gender: nil, profileImage: nil, specialization: nil, active: nil, createdAt: nil, updatedAt: nil, lastLoginAt: nil, status: nil, avatarUrl: nil)
        }
    }
    
    func login(user: User, token: String) {
        SessionManager.shared.saveSession(
            token: token,
            userId: String(user.id),
            email: user.email ?? "",
            role: user.role ?? "",
            name: user.name ?? ""
        )
        self.currentUser = user
        self.userRole = user.role
        self.isLoggedIn = true
    }
    
    func logout() {
        SessionManager.shared.clearSession()
        self.currentUser = nil
        self.userRole = nil
        self.isLoggedIn = false
    }
}
