import SwiftUI

struct WhatIsRAView: View {
    @Environment(\.dismiss) private var dismiss
    
    var body: some View {
        VStack(spacing: 0) {
            // Header matching the first screenshot
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
                    
                    Text("What is Rheumatoid Arthritis")
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
            .padding(.top, -10) // Adjustment for alignment
            
            ScrollView {
                VStack(spacing: 30) {
                    // Main Card
                    VStack(alignment: .leading, spacing: 25) {
                        Text("What is Rheumatoid Arthritis")
                            .font(.system(size: 32, weight: .bold))
                            .foregroundColor(Color(red: 0.15, green: 0.25, blue: 0.35)) // Navy/Dark Blue
                        
                        Text("Rheumatoid arthritis (RA) is an autoimmune disease causing chronic joint inflammation. Early diagnosis and treatment are key.")
                            .font(.system(size: 18))
                            .foregroundColor(.primary.opacity(0.8))
                            .lineSpacing(4)
                    }
                    .padding(32)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .background(Color.white)
                    .cornerRadius(16)
                    .shadow(color: .black.opacity(0.08), radius: 15, x: 0, y: 5)
                    .padding(.horizontal, 20)
                    .padding(.top, 40)
                }
            }
            .background(Color(red: 0.96, green: 0.97, blue: 0.98))
        }
        .navigationBarHidden(true)
    }
}

#Preview {
    WhatIsRAView()
}
