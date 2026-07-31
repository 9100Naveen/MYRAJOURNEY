import Foundation

struct HealthMetric: Codable, Identifiable {
    var id: Int?
    var patientId: Int
    var metricType: String
    var value: String
    var unit: String?
    var recordedAt: String
    var createdAt: String?
    
    enum CodingKeys: String, CodingKey {
        case id, value, unit
        case patientId = "patient_id"
        case metricType = "metric_type"
        case recordedAt = "recorded_at"
        case createdAt = "created_at"
    }
    
    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        
        // Handle ID as Int or String
        if let intId = try? container.decode(Int.self, forKey: .id) {
            id = intId
        } else if let stringId = try? container.decode(String.self, forKey: .id), let intId = Int(stringId) {
            id = intId
        }
        
        // Handle Patient ID as Int or String
        if let intPid = try? container.decode(Int.self, forKey: .patientId) {
            patientId = intPid
        } else if let stringPid = try? container.decode(String.self, forKey: .patientId), let intPid = Int(stringPid) {
            patientId = intPid
        } else {
            patientId = 0
        }
        
        metricType = try container.decode(String.self, forKey: .metricType)
        
        // value can be float, int or string in backend
        if let floatVal = try? container.decode(Double.self, forKey: .value) {
            value = String(floatVal)
        } else if let intVal = try? container.decode(Int.self, forKey: .value) {
            value = String(intVal)
        } else {
            value = try container.decode(String.self, forKey: .value)
        }
        
        unit = try container.decodeIfPresent(String.self, forKey: .unit)
        recordedAt = try container.decode(String.self, forKey: .recordedAt)
        createdAt = try container.decodeIfPresent(String.self, forKey: .createdAt)
    }
}
