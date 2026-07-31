import SwiftUI

struct AppHeader: View {
    let title: String
    var showBack: Bool = true
    var backAction: (() -> Void)?

    var body: some View {
        ZStack {
            LinearGradient(
                gradient: Gradient(colors: [Color.appAccent, Color.appAccentSoft]),
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
            HStack {
                if showBack {
                    Button(action: {
                        backAction?()
                    }) {
                        Image(systemName: "chevron.left")
                            .font(.system(size: 18, weight: .semibold))
                            .foregroundColor(.white)
                            .frame(width: 44, height: 44)
                            .background(Color.white.opacity(0.15))
                            .clipShape(Circle())
                    }
                } else {
                    Spacer().frame(width: 44)
                }

                Spacer()

                Text(title)
                    .font(.system(size: 20, weight: .bold))
                    .foregroundColor(.white)

                Spacer()

                Spacer().frame(width: 44)
            }
            .padding(.horizontal, 18)
            .padding(.bottom, 12)
            .padding(.top, topSafeArea() + 6)
        }
        .frame(maxWidth: .infinity)
        .shadow(color: .black.opacity(0.1), radius: 10, x: 0, y: 6)
    }

    private func topSafeArea() -> CGFloat {
        guard let scene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
              let window = scene.windows.first else {
            return 20
        }
        return window.safeAreaInsets.top
    }
}

struct AppHeader_Previews: PreviewProvider {
    static var previews: some View {
        VStack {
            AppHeader(title: "Title")
            Spacer()
        }
        .edgesIgnoringSafeArea(.top)
    }
}
