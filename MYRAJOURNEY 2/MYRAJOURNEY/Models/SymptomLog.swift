import Foundation

struct SymptomLog: Codable, Identifiable {
    var id: Int?
    var patientId: Int
    var date: String
    var painLevel: Int
    var stiffnessLevel: Int?
    var fatigueLevel: Int?
    var jointCount: Int?
    var notes: String?
    var createdAt: String?
    
    enum CodingKeys: String, CodingKey {
        case id, date, notes
        case patientId = "patient_id"
        case painLevel = "pain_level"
        case stiffnessLevel = "stiffness_level"
        case fatigueLevel = "fatigue_level"
        case jointCount = "joint_count"
        case createdAt = "created_at"
    }
}
