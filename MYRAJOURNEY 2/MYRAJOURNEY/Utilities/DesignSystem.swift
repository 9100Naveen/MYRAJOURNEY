import SwiftUI

struct Design {
    // MARK: - Colors
    struct Colors {
        static let primary = Color(red: 0.08, green: 0.48, blue: 0.95) // Deep Blue
        static let secondary = Color(red: 0.43, green: 0.35, blue: 0.91) // Purple
        static let accent = Color(red: 0.05, green: 0.78, blue: 0.55) // Teal/Green
        static let warning = Color(red: 0.94, green: 0.28, blue: 0.28) // Red
        static let background = Color(UIColor.systemGroupedBackground)
        static let surface = Color(UIColor.secondarySystemBackground)
        static let card = Color(UIColor.systemBackground)
        
        static let primaryGradient = LinearGradient(
            colors: [primary, Color(red: 0.04, green: 0.33, blue: 0.78)],
            startPoint: .topLeading,
            endPoint: .bottomTrailing
        )
        
        static let secondaryGradient = LinearGradient(
            colors: [secondary, Color(red: 0.3, green: 0.25, blue: 0.7)],
            startPoint: .topLeading,
            endPoint: .bottomTrailing
        )
    }
    
    // MARK: - Shadows
    struct Shadows {
        static let soft = ShadowConfig(color: Color.black.opacity(0.06), radius: 10, x: 0, y: 5)
        static let medium = ShadowConfig(color: Color.black.opacity(0.12), radius: 20, x: 0, y: 10)
        static let glow = ShadowConfig(color: Colors.primary.opacity(0.3), radius: 15, x: 0, y: 8)
    }
    
    struct ShadowConfig {
        let color: Color
        let radius: CGFloat
        let x: CGFloat
        let y: CGFloat
    }
}

// MARK: - View Modifiers
struct ModernCard: ViewModifier {
    var cornerRadius: CGFloat = 24
    var shadow: Design.ShadowConfig = Design.Shadows.soft
    
    func body(content: Content) -> some View {
        content
            .background(Design.Colors.card)
            .cornerRadius(cornerRadius)
            .shadow(color: shadow.color, radius: shadow.radius, x: shadow.x, y: shadow.y)
    }
}

struct GlassBackground: ViewModifier {
    func body(content: Content) -> some View {
        content
            .background(.ultraThinMaterial)
            .cornerRadius(20)
    }
}

extension View {
    func modernCard(radius: CGFloat = 24, shadow: Design.ShadowConfig = Design.Shadows.soft) -> some View {
        modifier(ModernCard(cornerRadius: radius, shadow: shadow))
    }
    
    func glassBackground() -> some View {
        modifier(GlassBackground())
    }
}

// MARK: - Custom Components
struct AppGradientHeader: View {
    let title: String
    let subtitle: String?
    var showMenuButton: Bool = false
    var menuAction: (() -> Void)? = nil
    var trailingAction: AnyView? = nil
    
    var body: some View {
        VStack(spacing: 0) {
            HStack(alignment: .center) {
                if showMenuButton {
                    Button(action: { menuAction?() }) {
                        Image(systemName: "line.3.horizontal")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(.white)
                            .padding(10)
                            .background(Color.white.opacity(0.2))
                            .clipShape(Circle())
                    }
                }
                
                VStack(alignment: .leading, spacing: 2) {
                    if let subtitle = subtitle {
                        Text(subtitle)
                            .font(.system(size: 12, weight: .medium))
                            .foregroundColor(.white.opacity(0.8))
                    }
                    Text(title)
                        .font(.system(size: 24, weight: .bold, design: .rounded))
                        .foregroundColor(.white)
                }
                .padding(.leading, showMenuButton ? 6 : 0)
                
                Spacer()
                
                if let trailing = trailingAction {
                    trailing
                }
            }
            .padding(.horizontal, 20)
            .padding(.top, topSafeArea() + 8)
            .padding(.bottom, 16)
            .background(Design.Colors.primaryGradient)
            .clipShape(RoundedCorner(radius: 32, corners: [.bottomLeft, .bottomRight]))
            .shadow(color: Design.Colors.primary.opacity(0.25), radius: 15, x: 0, y: 8)
        }
    }
    
    private func topSafeArea() -> CGFloat {
        guard let scene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
              let window = scene.windows.first else {
            return 44
        }
        return window.safeAreaInsets.top
    }
}

struct RoundedCorner: Shape {
    var radius: CGFloat = .infinity
    var corners: UIRectCorner = .allCorners

    func path(in rect: CGRect) -> Path {
        let path = UIBezierPath(roundedRect: rect, byRoundingCorners: corners, cornerRadii: CGSize(width: radius, height: radius))
        return Path(path.cgPath)
    }
}
