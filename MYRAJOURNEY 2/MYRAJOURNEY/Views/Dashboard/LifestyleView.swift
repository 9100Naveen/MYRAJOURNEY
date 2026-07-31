import SwiftUI

struct LifestyleView: View {
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
                    
                    Text("Lifestyle Management")
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
                            Text("Lifestyle Management for RA")
                                .font(.system(size: 24, weight: .bold))
                                .foregroundColor(Color(red: 0.2, green: 0.6, blue: 0.95))
                            
                            Text("A healthy lifestyle can significantly reduce symptoms and improve quality of life.")
                                .font(.system(size: 16))
                                .foregroundColor(.primary.opacity(0.8))
                                .lineSpacing(4)
                            
                            // Exercise Section
                            LifestyleSectionHeader(icon: "figure.run", title: "Exercise & Physical Activity", color: .blue)
                            
                            VStack(alignment: .leading, spacing: 10) {
                                Text("Benefits:")
                                    .fontWeight(.bold)
                                BulletPoint(text: "Reduces joint pain and stiffness")
                                BulletPoint(text: "Strengthens muscles around joints")
                                BulletPoint(text: "Improves flexibility")
                                BulletPoint(text: "Boosts energy and mood")
                            }
                            
                            VStack(alignment: .leading, spacing: 10) {
                                Text("Recommended Exercises:")
                                    .fontWeight(.bold)
                                BulletPoint(text: "**Low-Impact:** Walking, swimming, cycling")
                                BulletPoint(text: "**Stretching:** Gentle range-of-motion exercises")
                                BulletPoint(text: "**Strengthening:** Light weights, resistance bands")
                            }
                            
                            // Rest & Sleep
                            LifestyleSectionHeader(icon: "moon.zzz.fill", title: "Rest & Sleep", color: .blue)
                            VStack(alignment: .leading, spacing: 10) {
                                BulletPoint(text: "Aim for 7-9 hours of quality sleep")
                                BulletPoint(text: "Take short rest breaks during the day")
                                BulletPoint(text: "Listen to your body during flare-ups")
                            }
                            
                            // Stress Management
                            LifestyleSectionHeader(icon: "person.fill.viewfinder", title: "Stress Management", color: .blue)
                            VStack(alignment: .leading, spacing: 10) {
                                BulletPoint(text: "**Meditation:** 10-15 minutes daily")
                                BulletPoint(text: "**Deep Breathing:** Practice breathing exercises")
                                BulletPoint(text: "**Yoga:** Gentle poses for flexibility")
                                BulletPoint(text: "**Hobbies:** Engage in activities you enjoy")
                            }
                            
                            // Quit Smoking Card
                            VStack(alignment: .leading, spacing: 12) {
                                HStack {
                                    Image(systemName: "nosign")
                                        .foregroundColor(.red)
                                        .font(.system(size: 20, weight: .bold))
                                    Text("Quit Smoking:")
                                        .fontWeight(.bold)
                                }
                                
                                Text("Smoking increases RA severity and reduces medication effectiveness. Quitting improves treatment outcomes.")
                                    .font(.system(size: 15))
                                    .foregroundColor(.primary.opacity(0.8))
                                    .lineSpacing(3)
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
                            
                            // Joint Protection
                            LifestyleSectionHeader(icon: "shield.fill", title: "Joint Protection", color: .blue)
                            VStack(alignment: .leading, spacing: 10) {
                                BulletPoint(text: "Use larger, stronger joints when possible")
                                BulletPoint(text: "Avoid positions that stress joints")
                                BulletPoint(text: "Use assistive devices (jar openers, ergonomic tools)")
                                BulletPoint(text: "Maintain good posture")
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

struct LifestyleSectionHeader: View {
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

#Preview {
    LifestyleView()
}
