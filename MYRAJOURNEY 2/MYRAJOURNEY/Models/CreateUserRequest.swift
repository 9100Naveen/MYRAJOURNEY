import Foundation

struct CreateUserRequest: Codable {
    var name: String
    var email: String
    var password: String
    var role: String
    var mobile: String?
    var address: String?
    var age: String?
    var specialization: String?
    
    enum CodingKeys: String, CodingKey {
        case name, email, password, role, address, age, specialization
        case mobile = "phone"
    }
}
