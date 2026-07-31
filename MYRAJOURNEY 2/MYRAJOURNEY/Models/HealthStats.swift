import Foundation

struct HealthStats: Codable {
    let activePatients: Int?
    let avgSymptomScore: Double?
    let pendingReports: Int?
    let rehabCompliance: Double?

    enum CodingKeys: String, CodingKey {
        case activePatients   = "active_patients"
        case avgSymptomScore  = "avg_symptom_score"
        case pendingReports   = "pending_reports"
        case rehabCompliance  = "rehab_compliance"
    }
}
