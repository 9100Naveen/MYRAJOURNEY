import SwiftUI

struct OnboardingItem: Identifiable {
    let id = UUID()
    let image: String
    let title: String
    let description: String
}

struct OnboardingView: View {
    @AppStorage("onboardingCompleted") var onboardingCompleted = false
    @State private var currentPage = 0
    
    let items = [
        OnboardingItem(image: "lock.shield.fill", title: "Secure Access", description: "Your health data is protected with state-of-the-art encryption and secure login protocols."),
        OnboardingItem(image: "figure.walk.circle.fill", title: "Track Symptoms", description: "Easily log and monitor your daily RA symptoms, pain levels, and joint flexibility."),
        OnboardingItem(image: "chart.bar.fill", title: "Smart Insights", description: "Get personalized analytics and trends to help you and your doctor manage your RA better.")
    ]
    
    var body: some View {
        ZStack {
            LinearGradient(
                gradient: Gradient(colors: [Color.appAccent, Color.appAccentSoft]),
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
            .ignoresSafeArea()

            VStack {
                HStack {
                    Spacer()
                    Button(action: { onboardingCompleted = true }) {
                        Text("Skip")
                            .font(.headline)
                            .foregroundColor(.white.opacity(0.9))
                    }
                }
                .padding()

                TabView(selection: $currentPage) {
                    ForEach(0..<items.count, id: \.self) { index in
                        VStack(spacing: 30) {
                            ZStack {
                                RoundedRectangle(cornerRadius: 30)
                                    .fill(Color.appSurface)
                                    .shadow(color: Color.black.opacity(0.12), radius: 20, x: 0, y: 10)
                                VStack(spacing: 30) {
                                    Image(systemName: items[index].image)
                                        .resizable()
                                        .scaledToFit()
                                        .frame(height: 180)
                                        .foregroundColor(.appAccent)

                                    VStack(spacing: 18) {
                                        Text(items[index].title)
                                            .font(.system(size: 28, weight: .bold, design: .rounded))
                                            .foregroundColor(.primary)
                                            .multilineTextAlignment(.center)

                                        Text(items[index].description)
                                            .font(.body)
                                            .foregroundColor(.secondary)
                                            .multilineTextAlignment(.center)
                                            .padding(.horizontal, 24)
                                    }
                                }
                                .padding(30)
                            }
                            .frame(maxWidth: .infinity, maxHeight: .infinity)
                            .padding(.horizontal, 20)
                        }
                        .tag(index)
                    }
                }
                .tabViewStyle(PageTabViewStyle(indexDisplayMode: .always))
                .frame(height: 520)

                Button(action: {
                    if currentPage < items.count - 1 {
                        withAnimation { currentPage += 1 }
                    } else {
                        onboardingCompleted = true
                    }
                }) {
                    Text(currentPage == items.count - 1 ? "Get Started" : "Next")
                        .font(.headline)
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color.appAccent)
                        .cornerRadius(18)
                        .shadow(color: Color.appAccentDark.opacity(0.25), radius: 16, x: 0, y: 8)
                }
                .padding(.horizontal, 40)
                .padding(.bottom, 40)
            }
        }
    }
}

#Preview {
    OnboardingView()
}
