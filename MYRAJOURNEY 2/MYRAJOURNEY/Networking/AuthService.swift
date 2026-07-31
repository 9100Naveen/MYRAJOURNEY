import Foundation

class AuthService {
    static let shared = AuthService()
    private init() {}
    
    func login(request: AuthRequest, completion: @escaping (Result<ApiResponse<AuthResponse>, NetworkError>) -> Void) {
        guard let body = try? JSONEncoder().encode(request) else {
            completion(.failure(.decodingError))
            return
        }
        
        let endpoint = Endpoint(path: "auth/login", method: .post, body: body)
        ApiClient.shared.request(endpoint, completion: completion)
    }
    
    func register(request: AuthRequest, completion: @escaping (Result<ApiResponse<AuthResponse>, NetworkError>) -> Void) {
        guard let body = try? JSONEncoder().encode(request) else {
            completion(.failure(.decodingError))
            return
        }
        
        let endpoint = Endpoint(path: "auth/register", method: .post, body: body)
        ApiClient.shared.request(endpoint, completion: completion)
    }

    func forgotPassword(email: String, completion: @escaping (Result<ApiResponse<ForgotPasswordData>, NetworkError>) -> Void) {
        let request = ["email": email]
        guard let body = try? JSONSerialization.data(withJSONObject: request) else {
            completion(.failure(.decodingError))
            return
        }
        let endpoint = Endpoint(path: "auth/forgot-password", method: .post, body: body)
        ApiClient.shared.request(endpoint, completion: completion)
    }

    func resetPassword(email: String, newPassword: String, code: String, completion: @escaping (Result<ApiResponse<ResetPasswordData>, NetworkError>) -> Void) {
        let request = ResetPasswordRequest(email: email, password: newPassword, code: code)
        guard let body = try? JSONEncoder().encode(request) else {
            completion(.failure(.decodingError))
            return
        }
        let endpoint = Endpoint(path: "auth/reset-password", method: .post, body: body)
        ApiClient.shared.request(endpoint, completion: completion)
    }
}



// Support model for Auth Requests
struct AuthRequest: Codable {
    let name: String?
    let email: String
    let password: String
    let role: String?
    let phone: String?
}

struct ForgotPasswordData: Codable {
    let message: String?
    let resetToken: String?
    let expiresAt: Int?
}

struct ResetPasswordRequest: Codable {
    let email: String
    let password: String
    let code: String
}

struct ResetPasswordData: Codable {
    let message: String?
}
