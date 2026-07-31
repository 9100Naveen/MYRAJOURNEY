import SwiftUI

struct ManagementView: View {
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
                    
                    Text("Managing Your Symptoms")
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
                            Text("Managing Your RA Symptoms")
                                .font(.system(size: 24, weight: .bold))
                                .foregroundColor(Color(red: 0.2, green: 0.6, blue: 0.95))
                            
                            Text("Effective symptom management is essential for maintaining quality of life with RA.")
                                .font(.system(size: 16))
                                .foregroundColor(.primary.opacity(0.8))
                                .lineSpacing(4)
                            
                            // Medication Adherence
                            ManagementSectionHeader(icon: "pill.fill", title: "Medication Adherence", color: .orange)
                            
                            VStack(alignment: .leading, spacing: 10) {
                                Text("Why It's Critical:")
                                    .fontWeight(.bold)
                                BulletPoint(text: "Prevents disease progression")
                                BulletPoint(text: "Reduces inflammation and pain")
                                BulletPoint(text: "Prevents joint damage")
                            }
                            
                            // Medication Tips Card
                            VStack(alignment: .leading, spacing: 12) {
                                HStack {
                                    Image(systemName: "iphone")
                                        .foregroundColor(.black)
                                    Text("Medication Tips:")
                                        .fontWeight(.bold)
                                }
                                
                                BulletPoint(text: "Set reminders on your phone")
                                BulletPoint(text: "Use a pill organizer")
                                BulletPoint(text: "Never skip doses")
                                BulletPoint(text: "Report side effects immediately")
                            }
                            .padding(20)
                            .background(Color.blue.opacity(0.08))
                            .cornerRadius(12)
                            .overlay(
                                HStack {
                                    Rectangle().fill(Color.blue).frame(width: 4)
                                    Spacer()
                                }, alignment: .leading
                            )
                            
                            // Managing Flare-Ups
                            ManagementSectionHeader(icon: "flame.fill", title: "Managing Flare-Ups", color: .orange)
                            VStack(alignment: .leading, spacing: 10) {
                                Text("What to Do:")
                                    .fontWeight(.bold)
                                BulletPoint(text: "Rest affected joints")
                                BulletPoint(text: "Apply ice to reduce swelling")
                                BulletPoint(text: "Take prescribed pain medication")
                                BulletPoint(text: "Avoid strenuous activities")
                                BulletPoint(text: "Contact your doctor if severe")
                            }
                            
                            // Morning Stiffness
                            ManagementSectionHeader(icon: "sun.max.fill", title: "Morning Stiffness", color: .orange)
                            VStack(alignment: .leading, spacing: 10) {
                                Text("Relief Strategies:")
                                    .fontWeight(.bold)
                                BulletPoint(text: "Take a warm shower or bath")
                                BulletPoint(text: "Do gentle stretching exercises")
                                BulletPoint(text: "Use heating pads on stiff joints")
                                BulletPoint(text: "Allow extra time for morning routine")
                            }
                            
                            // Pain Management
                            ManagementSectionHeader(icon: "face.dashed.fill", title: "Pain Management", color: .orange)
                            VStack(alignment: .leading, spacing: 12) {
                                ManagementFeatureItem(title: "Physical Therapy", detail: "Customized exercise programs")
                                ManagementFeatureItem(title: "Occupational Therapy", detail: "Joint protection techniques")
                                ManagementFeatureItem(title: "Massage", detail: "Gentle massage for muscle tension")
                                ManagementFeatureItem(title: "Heat/Cold Therapy", detail: "Alternate for best results")
                            }
                            
                            // Track Your Symptoms
                            ManagementSectionHeader(icon: "chart.bar.fill", title: "Track Your Symptoms", color: .blue)
                            VStack(alignment: .leading, spacing: 12) {
                                HStack(alignment: .top) {
                                    Image(systemName: "lightbulb.fill")
                                        .foregroundColor(.yellow)
                                    VStack(alignment: .leading, spacing: 4) {
                                        Text("Use This App:")
                                            .fontWeight(.bold)
                                        Text("MyRA Journey helps you track symptoms, medications, and appointments. Regular tracking helps your doctor adjust treatment.")
                                            .font(.system(size: 15))
                                            .foregroundColor(.primary.opacity(0.8))
                                    }
                                }
                            }
                            .padding(20)
                            .background(Color.green.opacity(0.08))
                            .cornerRadius(12)
                            .overlay(
                                HStack {
                                    Rectangle().fill(Color.green).frame(width: 4)
                                    Spacer()
                                }, alignment: .leading
                            )
                            
                            // When to Call Your Doctor
                            ManagementSectionHeader(icon: "exclamationmark.triangle.fill", title: "When to Call Your Doctor", color: .red)
                            VStack(alignment: .leading, spacing: 12) {
                                Text("Seek Help If:")
                                    .fontWeight(.bold)
                                BulletPoint(text: "Severe, sudden joint pain")
                                BulletPoint(text: "High fever (>101°F)")
                                BulletPoint(text: "Severe medication side effects")
                                BulletPoint(text: "Symptoms not improving")
                            }
                            .padding(20)
                            .background(Color.red.opacity(0.05))
                            .cornerRadius(12)
                            .overlay(
                                HStack {
                                    Rectangle().fill(Color.red).frame(width: 4)
                                    Spacer()
                                }, alignment: .leading
                            )
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

struct ManagementSectionHeader: View {
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

struct ManagementFeatureItem: View {
    let title: String
    let detail: String
    
    var body: some View {
        HStack(alignment: .top, spacing: 10) {
            Text("•")
                .fontWeight(.bold)
            VStack(alignment: .leading, spacing: 2) {
                Text(title + ": ")
                    .fontWeight(.bold) +
                Text(detail)
            }
            .font(.system(size: 15))
            .foregroundColor(.primary.opacity(0.8))
        }
        .padding(.leading, 10)
    }
}

#Preview {
    ManagementView()
}
