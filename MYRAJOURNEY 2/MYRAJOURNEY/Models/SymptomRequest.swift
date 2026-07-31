import Foundation

struct SymptomRequest: Codable {
    var patientId: Int
    var date: String
    var painLevel: Int
    var stiffnessLevel: Int?
    var fatigueLevel: Int?
    var jointCount: Int?
    var notes: String?
    
    enum CodingKeys: String, CodingKey {
        case patientId = "patient_id"
        case date
        case painLevel = "pain_level"
        case stiffnessLevel = "stiffness_level"
        case fatigueLevel = "fatigue_level"
        case jointCount = "joint_count"
        case notes
    }
}
