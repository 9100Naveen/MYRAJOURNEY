import Foundation

struct NetworkConfig {
    static var baseIP: String = "14.139.187.229:8081"
    
    static var apiBaseURL: String {
        return "http://\(baseIP)/sept_batch2025/spic726/myrajourney/public/index.php/api/v1/"
    }
    
    static var adminURL: String {
        return "http://\(baseIP)/sept_batch2025/spic726/myrajourney/public/index.php/api/v1/admin/"
    }
    
    static var serverURL: String {
        return "http://\(baseIP)/sept_batch2025/spic726/myrajourney/public/"
    }
}
//http://14.139.187.229:8081/sept_batch2025/spic726/myrajourney/create-views.php
