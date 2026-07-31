import SwiftUI

struct NutritionView: View {
    @Environment(\.dismiss) private var dismiss
    
    var body: some View {
        VStack(spacing: 0) {
            // Header matching the screenshot
            ZStack(alignment: .bottom) {
                Color(red: 0.2, green: 0.6, blue: 0.95) // Bright Blue
                    .ignoresSafeArea(edges: .top)
                
                HStack {
                    Button(action: { dismiss() }) {
                        Image(systemName: "chevron.left")
                            .font(.system(size: 20, weight: .bold))
                            .foregroundColor(.white)
                    }
                    
                    Spacer()
                    
                    Text("Nutrition for RA Patients")
                        .font(.system(size: 18, weight: .bold))
                        .foregroundColor(.white)
                    
                    Spacer()
                    
                    // Invisible spacer for centering
                    Image(systemName: "chevron.left")
                        .font(.system(size: 20, weight: .bold))
                        .opacity(0)
                }
                .padding(.horizontal)
                .padding(.bottom, 12)
            }
            .frame(height: 100)
            .padding(.top, -10)
            
            ScrollView {
                VStack(spacing: 20) {
                    VStack(alignment: .leading, spacing: 25) {
                        Text("Education Article")
                            .font(.system(size: 32, weight: .bold))
                            .foregroundColor(Color(red: 0.15, green: 0.25, blue: 0.35))
                        
                        VStack(alignment: .leading, spacing: 15) {
                            Text("Nutrition for RA Patients")
                                .font(.system(size: 24, weight: .bold))
                                .foregroundColor(Color(red: 0.2, green: 0.6, blue: 0.95))
                            
                            Text("Proper nutrition can help reduce inflammation, maintain healthy weight, and improve overall well-being.")
                                .font(.system(size: 16))
                                .foregroundColor(.primary.opacity(0.8))
                                .lineSpacing(4)
                            
                            // Anti-Inflammatory Foods
                            NutritionSectionHeader(icon: "fish.fill", title: "Anti-Inflammatory Foods", color: .blue)
                            
                            VStack(alignment: .leading, spacing: 10) {
                                Text("Omega-3 Rich Fish:")
                                    .fontWeight(.bold)
                                BulletPoint(text: "Salmon, mackerel, sardines, tuna")
                                BulletPoint(text: "Aim for 2-3 servings per week")
                                BulletPoint(text: "Helps reduce inflammation")
                            }
                            
                            VStack(alignment: .leading, spacing: 10) {
                                Text("Colorful Fruits & Vegetables:")
                                    .fontWeight(.bold)
                                BulletPoint(text: "Berries (blueberries, strawberries)")
                                BulletPoint(text: "Leafy greens (spinach, kale, broccoli)")
                                BulletPoint(text: "Tomatoes, bell peppers, carrots")
                            }
                            
                            // Whole Grains
                            NutritionSectionHeader(icon: "leaf.fill", title: "Whole Grains", color: .blue)
                            
                            VStack(alignment: .leading, spacing: 10) {
                                BulletPoint(text: "Brown rice, quinoa, oats")
                                BulletPoint(text: "Whole wheat bread and pasta")
                                BulletPoint(text: "Provides sustained energy")
                            }
                            
                            // Foods to Limit Card
                            VStack(alignment: .leading, spacing: 12) {
                                HStack {
                                    Image(systemName: "xmark.circle.fill")
                                        .foregroundColor(.red)
                                    Text("Foods to Limit:")
                                        .fontWeight(.bold)
                                }
                                
                                BulletPoint(text: "Processed foods and fast food")
                                BulletPoint(text: "Sugary drinks and desserts")
                                BulletPoint(text: "Red meat and fried foods")
                                BulletPoint(text: "Excessive alcohol")
                            }
                            .padding(20)
                            .background(Color.red.opacity(0.1))
                            .cornerRadius(12)
                            .overlay(
                                HStack {
                                    Rectangle().fill(Color.red).frame(width: 4)
                                    Spacer()
                                }, alignment: .leading
                            )
                            
                            // Hydration
                            NutritionSectionHeader(icon: "drop.fill", title: "Hydration", color: .blue)
                            
                            VStack(alignment: .leading, spacing: 10) {
                                BulletPoint(text: "Drink 8-10 glasses of water daily")
                                BulletPoint(text: "Herbal teas (green tea, ginger tea)")
                                BulletPoint(text: "Proper hydration helps joint lubrication")
                            }
                        }
                    }
                    .padding(24)
                    .background(Color.white)
                    .cornerRadius(16)
                    .shadow(color: .black.opacity(0.05), radius: 10, x: 0, y: 5)
                    .padding(.horizontal, 20)
                    .padding(.vertical, 20)
                }
            }
            .background(Color(red: 0.96, green: 0.97, blue: 0.98))
        }
        .navigationBarHidden(true)
    }
}

struct NutritionSectionHeader: View {
    let icon: String
    let title: String
    let color: Color
    
    var body: some View {
        HStack(spacing: 8) {
            Image(systemName: icon)
                .foregroundColor(color)
            Text(title)
                .font(.system(size: 20, weight: .bold))
                .foregroundColor(Color(red: 0.2, green: 0.6, blue: 0.95))
        }
        .padding(.top, 10)
    }
}

struct BulletPoint: View {
    let text: String
    
    var body: some View {
        HStack(alignment: .top, spacing: 10) {
            Text("•")
                .fontWeight(.bold)
            Text(text)
                .font(.system(size: 15))
                .foregroundColor(.primary.opacity(0.8))
        }
        .padding(.leading, 10)
    }
}

#Preview {
    NutritionView()
}
