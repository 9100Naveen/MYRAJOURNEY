import SwiftUI

struct ContentView: View {
    @ObservedObject private var appState = AppState.shared
    @AppStorage("onboardingCompleted") var onboardingCompleted = false
    @State private var showSplash = true
    
    var body: some View {
        ZStack {
            if showSplash {
                SplashView(showSplash: $showSplash)
                    .transition(.opacity)
            } else if !onboardingCompleted {
                OnboardingView()
                    .transition(.move(edge: .trailing))
            } else {
                if appState.isLoggedIn {
                    DashboardView()
                        .transition(.opacity)
                } else {
                    LoginView()
                        .transition(.opacity)
                }
            }
        }
        .animation(.easeInOut, value: showSplash)
        .animation(.easeInOut, value: onboardingCompleted)
        .animation(.easeInOut, value: appState.isLoggedIn)
    }
}

#Preview {
    ContentView()
}
