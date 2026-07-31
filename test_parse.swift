import Foundation

struct ApiResponse<T: Codable>: Codable {
    let success: Bool?
    let message: String?
    let data: T?
    let error: ApiError?
    
    struct ApiError: Codable {
        let code: String?
        let message: String?
    }
}

private struct EmptyData: Codable {}

let jsonString = """
{"success":false,"error":{"code":"EMAIL_NOT_FOUND","message":"Email not found"}}
"""
let data = jsonString.data(using: .utf8)!

let decoder = JSONDecoder()
decoder.keyDecodingStrategy = .convertFromSnakeCase

do {
    let errorResponse = try decoder.decode(ApiResponse<EmptyData>.self, from: data)
    print("Success! Message: \(errorResponse.error?.message ?? errorResponse.message ?? "None")")
} catch {
    print("Failed: \(error)")
}
