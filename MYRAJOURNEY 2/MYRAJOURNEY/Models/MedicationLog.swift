import Foundation

struct MedicationLog: Codable, Identifiable {
    var id: Int?
    var patientMedicationId: Int
    var patientId: Int
    var medicationName: String
    var dosage: String?
    var takenAt: String
    var status: String
    var notes: String?
    var createdAt: String?
    
    enum CodingKeys: String, CodingKey {
        case id, status, notes
        case patientMedicationId = "patient_medication_id"
        case patientId = "patient_id"
        case medicationName = "medication_name"
        case dosage
        case takenAt = "taken_at"
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
        
        // Handle patientMedicationId as Int or String
        if let intPmid = try? container.decode(Int.self, forKey: .patientMedicationId) {
            patientMedicationId = intPmid
        } else if let stringPmid = try? container.decode(String.self, forKey: .patientMedicationId), let intPmid = Int(stringPmid) {
            patientMedicationId = intPmid
        } else {
            patientMedicationId = 0
        }
        
        // Handle patientId as Int or String
        if let intPid = try? container.decode(Int.self, forKey: .patientId) {
            patientId = intPid
        } else if let stringPid = try? container.decode(String.self, forKey: .patientId), let intPid = Int(stringPid) {
            patientId = intPid
        } else {
            patientId = 0
        }
        
        medicationName = try container.decodeIfPresent(String.self, forKey: .medicationName) ?? "Unknown Medication"
        dosage = try container.decodeIfPresent(String.self, forKey: .dosage)
        takenAt = try container.decodeIfPresent(String.self, forKey: .takenAt) ?? ""
        status = try container.decodeIfPresent(String.self, forKey: .status) ?? ""
        notes = try container.decodeIfPresent(String.self, forKey: .notes)
        createdAt = try container.decodeIfPresent(String.self, forKey: .createdAt)
    }
}
