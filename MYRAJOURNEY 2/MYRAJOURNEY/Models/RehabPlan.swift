import Foundation

struct RehabPlan: Codable, Identifiable {
    let id: String
    let title: String
    let description: String?
    let videoUrl: String?
    let exerciseName: String?
    let setsPerDay: Int?
    let repsPerSet: Int?
    let exercises: [RehabExercise]?
    
    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        
        // Handle ID as Int or String
        if let intId = try? container.decode(Int.self, forKey: .id) {
            id = String(intId)
        } else {
            id = try container.decode(String.self, forKey: .id)
        }
        
        title = try container.decode(String.self, forKey: .title)
        description = try container.decodeIfPresent(String.self, forKey: .description)
        videoUrl = try container.decodeIfPresent(String.self, forKey: .videoUrl)
        exerciseName = try container.decodeIfPresent(String.self, forKey: .exerciseName)
        setsPerDay = try container.decodeIfPresent(Int.self, forKey: .setsPerDay)
        repsPerSet = try container.decodeIfPresent(Int.self, forKey: .repsPerSet)
        exercises = try container.decodeIfPresent([RehabExercise].self, forKey: .exercises)
    }
    
    struct RehabExercise: Codable, Identifiable {
        let id: Int
        let name: String
        let description: String?
        let sets: Int?
        let reps: String?
        let frequencyPerWeek: String?
        let completed: Bool
        
        enum CodingKeys: String, CodingKey {
            case id, name, description, sets, reps, completed
            case frequencyPerWeek = "frequency_per_week"
        }
    }
    
    enum CodingKeys: String, CodingKey {
        case id, title, description, exercises
        case videoUrl = "video_url"
        case exerciseName = "exercise_name"
        case setsPerDay = "sets_per_day"
        case repsPerSet = "reps_per_set"
    }
}
