import Foundation

struct User: Codable, Identifiable {
    let id: Int
    let name: String?
    let email: String?
    let role: String?
    let assignedDoctorId: Int?
    let assignedDoctorName: String?
    let phone: String?
    let address: String?
    let age: Int?
    let gender: String?
    let profileImage: String?
    let specialization: String?
    let active: Bool?
    let createdAt: String?
    let updatedAt: String?
    let lastLoginAt: String?
    let status: String?
    let avatarUrl: String?
    
    enum CodingKeys: String, CodingKey {
        case id, name, email, role, phone, address, age, gender, active, status
        case assignedDoctorId = "assigned_doctor_id"
        case assignedDoctorName = "assigned_doctor_name"
        case profileImage = "profile_image"
        case specialization = "specialization"
        case createdAt = "created_at"
        case updatedAt = "updated_at"
        case lastLoginAt = "last_login_at"
        case avatarUrl = "avatar_url"
    }
}
