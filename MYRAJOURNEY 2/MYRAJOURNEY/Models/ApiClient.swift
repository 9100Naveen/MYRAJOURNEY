import Foundation

class ApiClient {
    static let shared = ApiClient()
    
    private let session: URLSession
    private let decoder: JSONDecoder
    
    private init() {
        let configuration = URLSessionConfiguration.default
        configuration.timeoutIntervalForRequest = 30
        configuration.timeoutIntervalForResource = 60
        // Matching Android's connectionPool(0, 1, TimeUnit.NANOSECONDS) logic isn't directly possible in URLSession,
        // but setting HTTPMaximumConnectionsPerHost  to 1 can limit pooling if needed.
        // configuration.httpMaximumConnectionsPerHost = 1
        
        self.session = URLSession(configuration: configuration)
        self.decoder = JSONDecoder()
        // Handle snake_case from backend via manual CodingKeys in models.
        // We do NOT use .convertFromSnakeCase here because our CodingKeys already map to snake_case.
        // self.decoder.keyDecodingStrategy = .convertFromSnakeCase
    }
    
    func request<T: Codable>(_ endpoint: Endpoint, completion: @escaping (Result<T, NetworkError>) -> Void) {
        guard let urlRequest = endpoint.urlRequest(baseURL: NetworkConfig.apiBaseURL) else {
            completion(.failure(.invalidURL))
            return
        }
        
        print("🚀 API Request: \(urlRequest.httpMethod ?? "") \(urlRequest.url?.absoluteString ?? "")")
        
        let task = session.dataTask(with: urlRequest) { data, response, error in
            if let error = error {
                completion(.failure(.unknown(error)))
                return
            }
            
            guard let httpResponse = response as? HTTPURLResponse else {
                completion(.failure(.noData))
                return
            }
            
            print("🕒 API Response Status: \(httpResponse.statusCode)")
            
            if httpResponse.statusCode == 401 {
                completion(.failure(.unauthorized))
                return
            }
            
            guard let data = data else {
                completion(.failure(.noData))
                return
            }
            
            // Print raw response for debugging
            if let jsonString = String(data: data, encoding: .utf8) {
                print("📦 API Data: \(jsonString)")
            }
            
            // Handle non-2xx responses before attempting JSON decode
            guard (200...299).contains(httpResponse.statusCode) else {
                // Try to extract a server error message from JSON body
                if let errorResponse = try? self.decoder.decode(ApiResponse<EmptyData>.self, from: data),
                   let msg = errorResponse.error?.message ?? errorResponse.message {
                    completion(.failure(.serverError(msg)))
                } else {
                    completion(.failure(.serverError("Server error (HTTP \(httpResponse.statusCode))")))
                }
                return
            }
            
            do {
                let decodedResponse = try self.decoder.decode(T.self, from: data)
                completion(.success(decodedResponse))
            } catch let decodingError as DecodingError {
                print("❌ Decoding Error: \(decodingError)")
                completion(.failure(.decodingError))
            } catch {
                print("❌ Unknown Error: \(error)")
                completion(.failure(.decodingError))
            }
        }
        task.resume()
    }
    
    func uploadMultipart<T: Codable>(
        path: String,
        parameters: [String: String],
        fileData: Data?,
        fileName: String?,
        mimeType: String?,
        completion: @escaping (Result<T, NetworkError>) -> Void
    ) {
        guard let url = URL(string: NetworkConfig.apiBaseURL + path) else {
            completion(.failure(.invalidURL))
            return
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        
        let boundary = UUID().uuidString
        request.setValue("multipart/form-data; boundary=\(boundary)", forHTTPHeaderField: "Content-Type")
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        request.setValue("MyRAJourney-iOS/1.0", forHTTPHeaderField: "User-Agent")
        
        if let token = SessionManager.shared.token {
            request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }
        
        var body = Data()
        
        for (key, value) in parameters {
            body.append("--\(boundary)\r\n".data(using: .utf8)!)
            body.append("Content-Disposition: form-data; name=\"\(key)\"\r\n\r\n".data(using: .utf8)!)
            body.append("\(value)\r\n".data(using: .utf8)!)
        }
        
        if let fileData = fileData, let fileName = fileName, let mimeType = mimeType {
            body.append("--\(boundary)\r\n".data(using: .utf8)!)
            body.append("Content-Disposition: form-data; name=\"file\"; filename=\"\(fileName)\"\r\n".data(using: .utf8)!)
            body.append("Content-Type: \(mimeType)\r\n\r\n".data(using: .utf8)!)
            body.append(fileData)
            body.append("\r\n".data(using: .utf8)!)
        }
        
        body.append("--\(boundary)--\r\n".data(using: .utf8)!)
        request.httpBody = body
        
        print("🚀 API Multipart Request: POST \(url.absoluteString)")
        
        let task = session.dataTask(with: request) { data, response, error in
            if let error = error {
                completion(.failure(.unknown(error)))
                return
            }
            
            guard let httpResponse = response as? HTTPURLResponse else {
                completion(.failure(.noData))
                return
            }
            
            print("🕒 API Response Status: \(httpResponse.statusCode)")
            
            if httpResponse.statusCode == 401 {
                completion(.failure(.unauthorized))
                return
            }
            
            guard let data = data else {
                completion(.failure(.noData))
                return
            }
            
            if let jsonString = String(data: data, encoding: .utf8) {
                print("📦 API Data: \(jsonString)")
            }
            
            // Handle non-2xx responses before attempting JSON decode
            guard (200...299).contains(httpResponse.statusCode) else {
                if let errorResponse = try? self.decoder.decode(ApiResponse<EmptyData>.self, from: data),
                   let msg = errorResponse.error?.message ?? errorResponse.message {
                    completion(.failure(.serverError(msg)))
                } else {
                    completion(.failure(.serverError("Server error (HTTP \(httpResponse.statusCode))")))
                }
                return
            }
            
            do {
                let decodedResponse = try self.decoder.decode(T.self, from: data)
                completion(.success(decodedResponse))
            } catch let decodingError as DecodingError {
                print("❌ Decoding Error: \(decodingError)")
                completion(.failure(.decodingError))
            } catch {
                print("❌ Unknown Error: \(error)")
                completion(.failure(.decodingError))
            }
        }
        task.resume()
    }
}

/// Placeholder used when decoding error-only responses with no meaningful data body
private struct EmptyData: Codable {}
