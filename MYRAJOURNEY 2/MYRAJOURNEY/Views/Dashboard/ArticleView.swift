import SwiftUI

struct ArticleView: View {
    let article: EducationArticle
    
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                // Header Image Placeholder
                ZStack {
                    RoundedRectangle(cornerRadius: 0)
                        .fill(Color.gray.opacity(0.05))
                        .frame(height: 200)
                    
                    VStack(alignment: .leading) {
                        Spacer()
                        Text(article.category ?? "Education")
                            .font(.caption)
                            .fontWeight(.bold)
                            .padding(.vertical, 4)
                            .padding(.horizontal, 10)
                            .background(Color.blue.opacity(0.1))
                            .foregroundColor(.blue)
                            .cornerRadius(5)
                        
                        Text(article.title)
                            .font(.largeTitle)
                            .fontWeight(.bold)
                            .foregroundColor(.primary)
                    }
                    .padding()
                }
                
                // Article Content
                VStack(alignment: .leading, spacing: 15) {
                    Text("Introduction")
                        .font(.headline)
                        .foregroundColor(.secondary)
                    
                    Text(article.displayContent)
                        .font(.body)
                        .foregroundColor(.primary)
                        .lineSpacing(5)
                    
                    // Add more sections as needed
                    Text("RA Management Tips")
                        .font(.headline)
                        .foregroundColor(.secondary)
                    
                    VStack(alignment: .leading, spacing: 12) {
                        RAInfoItem(text: "Consistent medication adherence is crucial.")
                        RAInfoItem(text: "Maintain a balanced anti-inflammatory diet.")
                        RAInfoItem(text: "Gentle exercises help maintain joint flexibility.")
                        RAInfoItem(text: "Stress management techniques can reduce flares.")
                    }
                }
                .padding()
            }
        }
        .edgesIgnoringSafeArea(.top)
        .navigationBarTitleDisplayMode(.inline)
    }
}

struct RAInfoItem: View {
    let text: String
    
    var body: some View {
        HStack(alignment: .top, spacing: 10) {
            Image(systemName: "checkmark.circle.fill")
                .foregroundColor(.green)
            Text(text)
                .font(.body)
                .foregroundColor(.primary)
        }
    }
}

#Preview {
    NavigationView {
        ArticleView(article: EducationArticle(id: "1", title: "Living with RA", slug: "living-with-ra", content: "Rheumatoid Arthritis (RA) is an autoimmune and inflammatory disease, which means that your immune system attacks healthy cells in your body by mistake, causing inflammation (painful swelling) in the affected parts of the body.", contentHtml: nil, thumbnail: nil, category: "Education"))
    }
}
