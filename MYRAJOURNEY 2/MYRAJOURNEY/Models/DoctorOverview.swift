import Foundation

// Matches actual backend response from GET /doctor/overview:
// {"success":true,"data":{"todaySchedule":[],"recentReportsCount":0,"patientsCount":1}}
struct DoctorOverview: Codable {
    let patientsCount: Int?
    let recentReportsCount: Int?
    let todaySchedule: [Appointment]?

    // Fields the backend does NOT send — kept optional to avoid decode crash
    let activePatients: Int?
    let pendingAppointments: Int?

    enum CodingKeys: String, CodingKey {
        case patientsCount        = "patientsCount"
        case recentReportsCount   = "recentReportsCount"
        case todaySchedule        = "todaySchedule"
        case activePatients       = "active_patients"
        case pendingAppointments  = "pending_appointments"
    }

    /// Safe computed count from whichever field the backend provides
    var computedPatientsCount: Int {
        return patientsCount ?? activePatients ?? 0
    }
}
