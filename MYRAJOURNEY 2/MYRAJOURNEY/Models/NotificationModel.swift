import Foundation

struct NotificationModel: Codable, Identifiable {
    let id: String
    let title: String
    let body: String
    let readAt: String?
    let createdAt: String?
    
    enum CodingKeys: String, CodingKey {
        case id, title, body
        case readAt = "read_at"
        case createdAt = "created_at"
    }
    
    var isRead: Bool {
        readAt != nil
    }
}
// Renamed to NotificationModel to avoid conflict with Foundation's Notification
