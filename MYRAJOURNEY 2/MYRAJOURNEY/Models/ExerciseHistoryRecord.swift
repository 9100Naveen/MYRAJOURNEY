import Foundation

struct ExerciseHistoryRecord: Codable, Identifiable {
    let id: Int
    let exerciseName: String
    let duration: String?
    let completedAt: String
    let status: String

    enum CodingKeys: String, CodingKey {
        case id, status, duration
        case exerciseName = "exercise_name"
        case completedAt = "completed_at"
    }
}
