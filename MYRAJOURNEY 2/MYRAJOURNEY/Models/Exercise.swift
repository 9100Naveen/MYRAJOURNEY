import Foundation

struct Exercise: Codable, Identifiable {
    let id: Int?
    let name: String
    let description: String?
    let category: String
    let targetJoints: [String]?
    let difficultyLevel: String?
    let videoUrl: String?
    let animationUrl: String?
    let instructions: [String]?
    let raBenefits: [String]?
    let createdAt: String?
    
    enum CodingKeys: String, CodingKey {
        case id, name, description, category
        case targetJoints = "target_joints"
        case difficultyLevel = "difficulty_level"
        case videoUrl = "video_url"
        case animationUrl = "animation_url"
        case instructions
        case raBenefits = "ra_benefits"
        case createdAt = "created_at"
    }
}

struct ExerciseAssignmentRequest: Codable {
    let patient_id: Int
    let exercise_ids: [Int]
    let notes: String?
}

struct ExerciseAssignmentResponse: Codable {
    let assignment_id: String
}

struct ExerciseAssignment: Codable, Identifiable {
    let id: String
    let doctorId: Int
    let patientId: Int
    let exerciseIds: [Int]
    let notes: String?
    let assignedDate: String
    var exercises: [Exercise]?
    
    enum CodingKeys: String, CodingKey {
        case id, notes, exercises
        case doctorId = "doctor_id"
        case patientId = "patient_id"
        case exerciseIds = "exercise_ids"
        case assignedDate = "assigned_date"
    }
}

struct ExerciseSessionRequest: Codable {
    let exercise_id: Int
    let start_time: String
    let session_duration: Int?
    let overall_accuracy: Double?
    let completion_rate: Double?
    let completed: Bool
}
