import Foundation

struct PatientOverview: Codable {
    let nextAppointment: Appointment?
    let unreadNotifications: Int
    let das28Score: Double
    let painLevel: Int
    let patientName: String?
    let recentReports: [Report]?
    
    enum CodingKeys: String, CodingKey {
        case nextAppointment = "next_appointment"
        case unreadNotifications = "unread_notifications"
        case das28Score = "das28_score"
        case painLevel = "pain_level"
        case patientName = "patient_name"
        case recentReports = "recent_reports"
    }
}
