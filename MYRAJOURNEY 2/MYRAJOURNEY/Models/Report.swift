import Foundation

struct Report: Codable, Identifiable {
    let id: String
    let patientId: String?
    let patientName: String?
    let title: String
    let description: String?
    let fileUrl: String?
    var status: String
    let createdAt: String?
    let updatedAt: String?
    let uploadedAt: String?
    
    enum CodingKeys: String, CodingKey {
        case id, title, description, status
        case patientId = "patient_id"
        case patientName = "patient_name"
        case fileUrl = "file_url"
        case createdAt = "created_at"
        case updatedAt = "updated_at"
        case uploadedAt = "uploaded_at"
    }
    
    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        
        // Handle ID as Int or String
        if let intId = try? container.decode(Int.self, forKey: .id) {
            id = String(intId)
        } else {
            id = try container.decode(String.self, forKey: .id)
        }
        
        // Handle Patient ID as Int or String
        if let intPid = try? container.decode(Int.self, forKey: .patientId) {
            patientId = String(intPid)
        } else {
            patientId = try? container.decode(String.self, forKey: .patientId)
        }
        
        patientName = try container.decodeIfPresent(String.self, forKey: .patientName)
        title = try container.decode(String.self, forKey: .title)
        description = try container.decodeIfPresent(String.self, forKey: .description)
        fileUrl = try container.decodeIfPresent(String.self, forKey: .fileUrl)
        status = try container.decode(String.self, forKey: .status)
        createdAt = try container.decodeIfPresent(String.self, forKey: .createdAt)
        updatedAt = try container.decodeIfPresent(String.self, forKey: .updatedAt)
        uploadedAt = try container.decodeIfPresent(String.self, forKey: .uploadedAt)
    }
    
    var displayStatus: String {
        return status.isEmpty ? "Pending" : status
    }
    
    var displayDate: String {
        return uploadedAt ?? createdAt ?? "Recent"
    }
}
