import Foundation

// Matches actual backend response from GET /appointments:
// {"id":15,"patient_id":84,"doctor_id":87,"appointment_date":"2026-04-25",
//  "appointment_time":"10:00:00","title":"Appointment","description":null,
//  "reason":null,"location":null,"type":"CONSULTATION","status":"SCHEDULED",
//  "notes":null,"patient_name":"Purushothman","doctor_name":"Test Doctor",
//  "appointment_type":"Appointment","formatted_date":"Apr 25, 2026",
//  "formatted_time_slot":"10:00 AM","start_time":"2026-04-25 10:00:00",
//  "end_time":"2026-04-25 11:00:00"}
struct Appointment: Codable, Identifiable {
    let id: Int             // ✅ Backend returns Int NOT String
    let patientId: Int?
    let doctorId: Int?
    let doctorName: String?
    let patientName: String?
    let appointmentDate: String?    // "2026-04-25"
    let appointmentTime: String?    // "10:00:00"
    let formattedDate: String?      // "Apr 25, 2026"
    let formattedTimeSlot: String?  // "10:00 AM"
    let title: String?
    let description: String?
    let status: String?
    let appointmentType: String?
    let reason: String?
    let location: String?
    let notes: String?
    let type: String?
    let startTime: String?          // "2026-04-25 10:00:00"
    let endTime: String?            // "2026-04-25 11:00:00"
    let createdAt: String?
    let updatedAt: String?

    enum CodingKeys: String, CodingKey {
        case id, title, description, status, reason, location, notes, type
        case patientId        = "patient_id"
        case doctorId         = "doctor_id"
        case doctorName       = "doctor_name"
        case patientName      = "patient_name"
        case appointmentDate  = "appointment_date"
        case appointmentTime  = "appointment_time"
        case formattedDate    = "formatted_date"
        case formattedTimeSlot = "formatted_time_slot"
        case appointmentType  = "appointment_type"
        case startTime        = "start_time"
        case endTime          = "end_time"
        case createdAt        = "created_at"
        case updatedAt        = "updated_at"
    }

    // MARK: - Computed display helpers
    var displayDate: String {
        return formattedDate ?? appointmentDate ?? "—"
    }

    var displayTimeSlot: String {
        return formattedTimeSlot ?? appointmentTime ?? "—"
    }

    var displayTitle: String {
        return title ?? appointmentType ?? "Appointment"
    }
}
