import Foundation

struct EducationArticle: Codable, Identifiable {
    let id: String
    let title: String
    let slug: String
    let content: String?
    let contentHtml: String?
    let thumbnail: String?
    let category: String?
    
    enum CodingKeys: String, CodingKey {
        case id, title, slug, content, thumbnail, category
        case contentHtml = "content_html"
    }
    
    var displayContent: String {
        return contentHtml ?? content ?? ""
    }
}
