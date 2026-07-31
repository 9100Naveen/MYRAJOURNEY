import Foundation

// Matches actual backend response from GET /patients:
// {"id":88,"name":"Test Patient","email":"testpatient99@test.com","phone":"9876543210",
//  "role":"PATIENT","created_at":"...","age":25,"gender":null,"medical_id":null,
//  "address":"Chennai","assigned_doctor_id":87}
struct Patient: Codable, Identifiable {
    let id: Int
    let name: String
    let email: String
    let phone: String?
    let age: Int?           // Backend sends Int (nullable)
    let gender: String?
    let address: String?
    let role: String?
    let createdAt: String?

    // Fields present in backend but not always needed
    let medicalId: String?
    let assignedDoctorId: Int?

    // RA-specific fields (not from /patients endpoint, used in detail views)
    var diagnosisDate: Date?
    var doctorId: String?
    var profilePicture: String?
    var riskFactors: [String]?
    var emergencyPhone: String?
    var raType: String?
    var diseaseActivity: String?
    var affectedJoints: [String]?
    var currentTreatmentPlan: String?
    var isActive: Bool?

    enum CodingKeys: String, CodingKey {
        case id, name, email, phone, age, gender, address, role
        case createdAt         = "created_at"
        case medicalId         = "medical_id"
        case assignedDoctorId  = "assigned_doctor_id"
        case diagnosisDate, doctorId, profilePicture
        case riskFactors, emergencyPhone, raType
        case diseaseActivity, affectedJoints, currentTreatmentPlan, isActive
    }

    // MARK: - Computed helpers
    var ageGroup: String {
        guard let age = age else { return "Unknown" }
        if age < 30 { return "Young Adult" }
        if age < 50 { return "Middle-aged" }
        if age < 65 { return "Older Adult" }
        return "Senior"
    }

    var hasHighDiseaseActivity: Bool {
        diseaseActivity?.lowercased() == "high"
    }
}
