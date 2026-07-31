import SwiftUI
struct SplashView: View {
    @State private var scale = 0.8
    @State private var opacity = 0.5
    @Binding var showSplash: Bool
    var body: some View {
        ZStack {
            LinearGradient(
                gradient: Gradient(colors: [Color.appAccent, Color.appAccentSoft]),
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
            .ignoresSafeArea()
            VStack(spacing: 20) {   
                ZStack {	
                    Circle()
                        .fill(Color.white.opacity(0.22))
                        .frame(width: 160, height: 160)
                        .shadow(color: Color.black.opacity(0.1), radius: 24, x: 0, y: 10)

                    Image(systemName: "figure.walk.circle.fill")
                        .resizable()
                        .scaledToFit()
                        .frame(width: 88, height: 88)
                        .foregroundColor(.white)
                        .shadow(color: Color.white.opacity(0.35), radius: 18, x: 0, y: 8)
                }

                VStack(spacing: 10) {
                    Text("My RA Journey")
                        .font(.system(size: 36, weight: .black, design: .rounded))
                        .foregroundColor(.white)

                    Text("Your personalized RA companion")
                        .font(.system(size: 16, weight: .medium, design: .rounded))
                        .foregroundColor(.white.opacity(0.9))
                }
                .multilineTextAlignment(.center)
                .padding(.horizontal, 24)
            }
            .scaleEffect(scale)
            .opacity(opacity)
        }
        .onAppear {
            AppState.shared.syncWithSession()
            withAnimation(.easeOut(duration: 0.8)) {
                self.scale = 1.0
                self.opacity = 1.0
            }
            DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
                withAnimation(.spring()) {
                    self.showSplash = false
                }
            }
        }
    }
}

#Preview {
    SplashView(showSplash: .constant(true))
}






