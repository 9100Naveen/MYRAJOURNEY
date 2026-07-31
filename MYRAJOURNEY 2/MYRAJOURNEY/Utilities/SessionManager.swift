import Foundation

class SessionManager {
    static let shared = SessionManager()
    
    private let defaults = UserDefaults.standard
    private let tokenKey = "auth_token"
    private let userIdKey = "user_id"
    private let userEmailKey = "user_email"
    private let userRoleKey = "user_role"
    private let userNameKey = "user_name"
    
    private init() {}
    
    var token: String? {
        get { defaults.string(forKey: tokenKey) }
        set { defaults.set(newValue, forKey: tokenKey) }
    }
    
    var userName: String? {
        get { defaults.string(forKey: userNameKey) }
        set { defaults.set(newValue, forKey: userNameKey) }
    }
    
    var userId: String? {
        get { defaults.string(forKey: userIdKey) }
        set { defaults.set(newValue, forKey: userIdKey) }
    }
    
    var userEmail: String? {
        get { defaults.string(forKey: userEmailKey) }
        set { defaults.set(newValue, forKey: userEmailKey) }
    }
    
    var userRole: String? {
        get { defaults.string(forKey: userRoleKey) }
        set { defaults.set(newValue, forKey: userRoleKey) }
    }
    
    var isLoggedIn: Bool {
        token != nil && !(token?.isEmpty ?? true)
    }
    
    func saveSession(token: String, userId: String, email: String, role: String, name: String? = nil) {
        self.token = token
        self.userId = userId
        self.userEmail = email
        self.userRole = role
        if let name = name {
            self.userName = name
        }
    }
    
    func clearSession() {
        defaults.removeObject(forKey: tokenKey)
        defaults.removeObject(forKey: userIdKey)
        defaults.removeObject(forKey: userEmailKey)
        defaults.removeObject(forKey: userRoleKey)
        defaults.removeObject(forKey: userNameKey)
    }
}
