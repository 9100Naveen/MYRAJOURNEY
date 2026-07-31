import SwiftUI

struct EducationHubView: View {
    @State private var articles: [EducationArticle] = []
    @State private var isLoading = true
    @Environment(\.dismiss) private var dismiss
    
    private let skyBlue = Color(red: 0.9, green: 0.95, blue: 1.0)
    
    // Category Data
    private let quickAccessItems = [
        EducationCategory(title: "What is RA?", icon: "books.vertical.fill", color: Color(red: 1.0, green: 0.95, blue: 0.85), categoryKey: "General"),
        EducationCategory(title: "Nutrition", icon: "leaf.fill", color: Color(red: 0.9, green: 0.98, blue: 0.9), categoryKey: "Nutrition"),
        EducationCategory(title: "Lifestyle", icon: "figure.run", color: Color(red: 0.95, green: 0.92, blue: 1.0), categoryKey: "Lifestyle"),
        EducationCategory(title: "Management", icon: "pills.fill", color: Color(red: 1.0, green: 0.92, blue: 0.92), categoryKey: "Management")
    ]
    
    var body: some View {
        VStack(spacing: 0) {
            // Standardized Modern Header
            AppGradientHeader(
                title: "Education Hub",
                subtitle: "Learning Center",
                showMenuButton: false,
                trailingAction: AnyView(
                    Button(action: { dismiss() }) {
                        Image(systemName: "xmark.circle.fill")
                            .font(.system(size: 24))
                            .foregroundColor(.white.opacity(0.8))
                    }
                )
            )
            
            ScrollView {
                VStack(alignment: .leading, spacing: 25) {
                    // Main Highlight Card
                    VStack(alignment: .leading, spacing: 15) {
                        HStack {
                            Spacer()
                            Image(systemName: "house.circle.fill")
                                .font(.system(size: 80))
                                .foregroundColor(.blue.opacity(0.8))
                            Spacer()
                        }
                        
                        VStack(alignment: .leading, spacing: 5) {
                            Text("Rheumatoid Arthritis Education Hub")
                                .font(.system(size: 24, weight: .bold))
                                .foregroundColor(.primary)
                                .lineLimit(2)
                            
                            Text("Your comprehensive guide to understanding and managing RA")
                                .font(.system(size: 16))
                                .foregroundColor(.primary.opacity(0.7))
                                .fixedSize(horizontal: false, vertical: true)
                        }
                    }
                    .padding(24)
                    .background(skyBlue)
                    .cornerRadius(20)
                    .padding(.horizontal)
                    .padding(.top, 20)
                    
                    // Quick Access Section
                    VStack(alignment: .leading, spacing: 16) {
                        Text("Quick Access")
                            .font(.system(size: 22, weight: .bold))
                            .foregroundColor(.primary)
                        
                        LazyVGrid(columns: [GridItem(.flexible(), spacing: 16), GridItem(.flexible(), spacing: 16)], spacing: 16) {
                            ForEach(quickAccessItems) { item in
                                if item.title == "What is RA?" {
                                    NavigationLink(destination: WhatIsRAView()) {
                                        EducationQuickCard(title: item.title, icon: item.icon, color: item.color)
                                    }
                                } else if item.title == "Nutrition" {
                                    NavigationLink(destination: NutritionView()) {
                                        EducationQuickCard(title: item.title, icon: item.icon, color: item.color)
                                    }
                                } else if item.title == "Lifestyle" {
                                    NavigationLink(destination: LifestyleView()) {
                                        EducationQuickCard(title: item.title, icon: item.icon, color: item.color)
                                    }
                                } else if item.title == "Management" {
                                    NavigationLink(destination: ManagementView()) {
                                        EducationQuickCard(title: item.title, icon: item.icon, color: item.color)
                                    }
                                } else {
                                    NavigationLink(destination: EducationCategoryView(title: item.title, categoryKey: item.categoryKey, allArticles: articles)) {
                                        EducationQuickCard(title: item.title, icon: item.icon, color: item.color)
                                    }
                                }
                            }
                        }
                    }
                    .padding(.horizontal)
                    
                }
                .padding(.bottom, 30)
            }
        }
        .background(Color.appBackground)
        .navigationBarHidden(true)
        .onAppear(perform: loadData)
    }
    
    private func loadData() {
        isLoading = true
        PatientService.shared.getEducationArticles { result in
            DispatchQueue.main.async {
                self.isLoading = false
                if case .success(let response) = result, let data = response.data {
                    self.articles = data
                }
            }
        }
    }
}

// Support Models
struct EducationCategory: Identifiable {
    let id = UUID()
    let title: String
    let icon: String
    let color: Color
    let categoryKey: String
}

// Category Detail View
struct EducationCategoryView: View {
    let title: String
    let categoryKey: String
    let allArticles: [EducationArticle]
    @Environment(\.dismiss) private var dismiss
    
    var filteredArticles: [EducationArticle] {
        if categoryKey == "General" {
            return allArticles
        }
        return allArticles.filter { $0.category?.localizedCaseInsensitiveContains(categoryKey) ?? false }
    }
    
    var body: some View {
        VStack(spacing: 0) {
            AppGradientHeader(
                title: title,
                subtitle: "Educational Resources",
                showMenuButton: false,
                trailingAction: AnyView(
                    Button(action: { dismiss() }) {
                        Image(systemName: "xmark.circle.fill")
                            .font(.system(size: 24))
                            .foregroundColor(.white.opacity(0.8))
                    }
                )
            )
            
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    if filteredArticles.isEmpty {
                        VStack(spacing: 20) {
                            Spacer(minLength: 50)
                            Image(systemName: "doc.text.magnifyingglass")
                                .font(.system(size: 60))
                                .foregroundColor(.gray.opacity(0.3))
                            Text("No articles found in this category.")
                                .foregroundColor(.secondary)
                            Spacer()
                        }
                        .frame(maxWidth: .infinity)
                    } else {
                        ForEach(filteredArticles) { article in
                            NavigationLink(destination: ArticleView(article: article)) {
                                ArticleRow(article: article)
                            }
                        }
                        .padding(.top, 20)
                    }
                }
                .padding(.horizontal)
            }
        }
        .background(Color.appBackground)
        .navigationBarHidden(true)
    }
}

struct EducationQuickCard: View {
    let title: String
    let icon: String
    let color: Color
    
    var body: some View {
        VStack(spacing: 12) {
            Image(systemName: icon)
                .font(.system(size: 30))
                .foregroundColor(.primary.opacity(0.7))
            
            Text(title)
                .font(.system(size: 16, weight: .bold))
                .foregroundColor(.primary)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 30)
        .background(color)
        .cornerRadius(15)
        .shadow(color: .black.opacity(0.03), radius: 5, x: 0, y: 2)
    }
}

struct ArticleRow: View {
    let article: EducationArticle
    
    var body: some View {
        HStack(spacing: 16) {
            ZStack {
                RoundedRectangle(cornerRadius: 12)
                    .fill(Color.blue.opacity(0.1))
                    .frame(width: 80, height: 80)
                Image(systemName: "doc.text.fill")
                    .foregroundColor(.blue)
            }
            
            VStack(alignment: .leading, spacing: 8) {
                Text(article.category ?? "General")
                    .font(.caption)
                    .fontWeight(.bold)
                    .foregroundColor(.blue)
                    .padding(.vertical, 4)
                    .padding(.horizontal, 8)
                    .background(Color.blue.opacity(0.1))
                    .cornerRadius(4)
                
                Text(article.title)
                    .font(.system(size: 16, weight: .semibold))
                    .foregroundColor(.primary)
                    .lineLimit(2)
                    .multilineTextAlignment(.leading)
            }
            Spacer()
        }
        .padding()
        .background(Color.appCard)
        .cornerRadius(16)
        .shadow(color: .black.opacity(0.05), radius: 10, x: 0, y: 5)
    }
}

#Preview {
    NavigationView {
        EducationHubView()
    }
}
