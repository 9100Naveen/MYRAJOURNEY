import Foundation

enum NetworkError: Error {
    case invalidURL
    case noData
    case decodingError
    case serverError(String)
    case unauthorized
    case unknown(Error)
    
    var localizedDescription: String {
        switch self {
        case .invalidURL: return "Invalid URL"
        case .noData: return "No data received from server"
        case .decodingError: return "Failed to decode server response"
        case .serverError(let message): return message
        case .unauthorized: return "Unauthorized access. Please login again."
        case .unknown(let error): return error.localizedDescription
        }
    }
}
