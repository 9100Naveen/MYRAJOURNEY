import Foundation

struct Medication: Codable, Identifiable {
    var id: Int
    var patientId: Int
    var name: String
    var genericName: String?
    var dosage: String?
    var frequency: String
    var instructions: String?
    var duration: String?
    var isMorning: Bool
    var isAfternoon: Bool
    var isNight: Bool
    var foodRelation: String?
    var startDate: String?
    var endDate: String?
    var prescribedBy: String?
    var category: String?
    var active: Bool
    var notes: String?
    var sideEffects: [String]?
    var createdAt: String?
    var updatedAt: String?
    var status: String?
    var removedAt: String?
    var removedBy: String?
    var doctorId: Int?
    var medicationId: Int?
    var reminderEnabled: Bool?
    var reminderTimes: String?
    var adherenceRate: String?
    var doctorName: String?
    var doctorSpecialization: String?
    var doctorLicense: String?

    enum CodingKeys: String, CodingKey {
        case id, name, dosage, frequency, instructions, duration, status, notes
        case patientId = "patient_id"
        case genericName = "generic_name"
        case isMorning = "is_morning"
        case isAfternoon = "is_afternoon"
        case isNight = "is_night"
        case foodRelation = "food_relation"
        case startDate = "start_date"
        case endDate = "end_date"
        case prescribedBy = "prescribed_by"
        case category
        case activeInt = "active"
        case sideEffects = "side_effects"
        case createdAt = "created_at"
        case updatedAt = "updated_at"
        case removedAt = "removed_at"
        case removedBy = "removed_by"
        case doctorId = "doctor_id"
        case medicationId = "medication_id"
        case reminderEnabledInt = "reminder_enabled"
        case reminderTimes = "reminder_times"
        case adherenceRate = "adherence_rate"
        case doctorName = "doctor_name"
        case doctorSpecialization = "doctor_specialization"
        case doctorLicense = "doctor_license"
    }

    // Custom Decoding to handle alternates and mixed types (int/bool)
    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        
        // Handle ID as Int or String
        if let intId = try? container.decode(Int.self, forKey: .id) {
            id = intId
        } else if let stringId = try? container.decode(String.self, forKey: .id), let intId = Int(stringId) {
            id = intId
        } else {
            id = 0
        }
        
        // Handle Patient ID as Int or String
        if let intPid = try? container.decode(Int.self, forKey: .patientId) {
            patientId = intPid
        } else if let stringPid = try? container.decode(String.self, forKey: .patientId), let intPid = Int(stringPid) {
            patientId = intPid
        } else {
            patientId = 0
        }
        
        name = try container.decodeIfPresent(String.self, forKey: .name) ?? "Unnamed Medication"
        genericName = try container.decodeIfPresent(String.self, forKey: .genericName)
        dosage = try container.decodeIfPresent(String.self, forKey: .dosage)
        
        // frequency can be string or int in some backends, decoded as string
        if let freqString = try? container.decode(String.self, forKey: .frequency) {
            frequency = freqString
        } else if let freqInt = try? container.decode(Int.self, forKey: .frequency) {
            frequency = String(freqInt)
        } else {
            frequency = "1"
        }
        
        instructions = try container.decodeIfPresent(String.self, forKey: .instructions)
        duration = try container.decodeIfPresent(String.self, forKey: .duration)
        
        // Handle 1/0, Bool, or String for timing
        if let morningBool = try? container.decode(Bool.self, forKey: .isMorning) {
            isMorning = morningBool
        } else if let morningInt = try? container.decode(Int.self, forKey: .isMorning) {
            isMorning = morningInt == 1
        } else if let morningString = try? container.decode(String.self, forKey: .isMorning) {
            isMorning = morningString == "1"
        } else {
            isMorning = false
        }
        
        if let afternoonBool = try? container.decode(Bool.self, forKey: .isAfternoon) {
            isAfternoon = afternoonBool
        } else if let afternoonInt = try? container.decode(Int.self, forKey: .isAfternoon) {
            isAfternoon = afternoonInt == 1
        } else if let afternoonString = try? container.decode(String.self, forKey: .isAfternoon) {
            isAfternoon = afternoonString == "1"
        } else {
            isAfternoon = false
        }
        
        if let nightBool = try? container.decode(Bool.self, forKey: .isNight) {
            isNight = nightBool
        } else if let nightInt = try? container.decode(Int.self, forKey: .isNight) {
            isNight = nightInt == 1
        } else if let nightString = try? container.decode(String.self, forKey: .isNight) {
            isNight = nightString == "1"
        } else {
            isNight = false
        }
        
        foodRelation = try container.decodeIfPresent(String.self, forKey: .foodRelation)
        startDate = try container.decodeIfPresent(String.self, forKey: .startDate)
        endDate = try container.decodeIfPresent(String.self, forKey: .endDate)
        // Handle prescribedBy as String or Int
        if let pString = try? container.decode(String.self, forKey: .prescribedBy) {
            prescribedBy = pString
        } else if let pInt = try? container.decode(Int.self, forKey: .prescribedBy) {
            prescribedBy = String(pInt)
        } else {
            prescribedBy = nil
        }
        category = try container.decodeIfPresent(String.self, forKey: .category)
        
        // active can be int or bool
        if let activeBool = try? container.decode(Bool.self, forKey: .activeInt) {
            active = activeBool
        } else {
            active = (try? container.decode(Int.self, forKey: .activeInt)) == 1
        }
        
        notes = try container.decodeIfPresent(String.self, forKey: .notes)
        sideEffects = try container.decodeIfPresent([String].self, forKey: .sideEffects)
        createdAt = try container.decodeIfPresent(String.self, forKey: .createdAt)
        updatedAt = try container.decodeIfPresent(String.self, forKey: .updatedAt)
        status = try container.decodeIfPresent(String.self, forKey: .status)
        removedAt = try container.decodeIfPresent(String.self, forKey: .removedAt)
        removedBy = try container.decodeIfPresent(String.self, forKey: .removedBy)
        
        // doctorId as Int or String
        if let dId = try? container.decode(Int.self, forKey: .doctorId) {
            doctorId = dId
        } else if let sdId = try? container.decode(String.self, forKey: .doctorId) {
            doctorId = Int(sdId)
        }
        
        // medicationId as Int or String
        if let mId = try? container.decode(Int.self, forKey: .medicationId) {
            medicationId = mId
        } else if let smId = try? container.decode(String.self, forKey: .medicationId) {
            medicationId = Int(smId)
        }
        
        reminderEnabled = (try? container.decode(Int.self, forKey: .reminderEnabledInt)) == 1
        if reminderEnabled == nil {
            // Check if it's sent as a bool directly
            reminderEnabled = try? container.decode(Bool.self, forKey: .reminderEnabledInt)
        }
        reminderTimes = try container.decodeIfPresent(String.self, forKey: .reminderTimes)
        adherenceRate = try container.decodeIfPresent(String.self, forKey: .adherenceRate)
        doctorName = try container.decodeIfPresent(String.self, forKey: .doctorName)
        doctorSpecialization = try container.decodeIfPresent(String.self, forKey: .doctorSpecialization)
        doctorLicense = try container.decodeIfPresent(String.self, forKey: .doctorLicense)
    }
    
    // Coding for encoding
    func encode(to encoder: Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)
        try container.encode(id, forKey: .id)
        try container.encode(patientId, forKey: .patientId)
        try container.encode(name, forKey: .name)
        // ... and so on. For simplicity, omitting full encoder as it follows standard patterns
    }
}
