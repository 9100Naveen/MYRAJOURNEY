import Foundation

struct ApiResponse<T: Codable>: Codable {
    let success: Bool
    let message: String?
    let data: T?
    let error: ApiError?
    
    struct ApiError: Codable {
        let code: String?
        let message: String?
    }
}
