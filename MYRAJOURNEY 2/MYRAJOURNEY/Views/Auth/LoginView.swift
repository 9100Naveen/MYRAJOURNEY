import SwiftUI

struct LoginView: View {
    @ObservedObject private var appState = AppState.shared
    @State private var email = ""
    @State private var password = ""
    @State private var isLoading = false
    @State private var errorMessage: String?
    @State private var showPassword = false
    
    var body: some View {
        NavigationStack {
            ZStack {
                // Background Gradient
                LinearGradient(
                    colors: [Design.Colors.background, Color.white],
                    startPoint: .top,
                    endPoint: .bottom
                ).ignoresSafeArea()
                
                // Decorative Circle
                Circle()
                    .fill(Design.Colors.primaryGradient.opacity(0.05))
                    .frame(width: 400, height: 400)
                    .offset(x: 200, y: -200)

                ScrollView(showsIndicators: false) {
                    VStack(spacing: 40) {
                        VStack(spacing: 16) {
                            ZStack {
                                Circle()
                                    .fill(Design.Colors.primaryGradient)
                                    .frame(width: 100, height: 100)
                                Image(systemName: "heart.fill")
                                    .font(.system(size: 40))
                                    .foregroundColor(.white)
                            }
                            .shadow(color: Design.Colors.primary.opacity(0.3), radius: 20, x: 0, y: 10)
                            
                            VStack(spacing: 8) {
                                Text("MyRA Journey")
                                    .font(.system(size: 32, weight: .black, design: .rounded))
                                    .foregroundColor(Design.Colors.primary)
                                
                                Text("Sign in to continue your journey")
                                    .font(.system(size: 16, weight: .medium))
                                    .foregroundColor(.secondary)
                            }
                        }
                        .padding(.top, 60)

                        VStack(spacing: 24) {
                            VStack(spacing: 20) {
                                ModernInputField(title: "Email Address", text: $email, icon: "envelope.fill", keyboardType: .emailAddress, textContentType: .emailAddress)
                                ModernSecureField(title: "Password", text: $password, icon: "lock.fill")

                                HStack {
                                    Spacer()
                                    NavigationLink(destination: ForgotPasswordView()) {
                                        Text("Forgot Password?")
                                            .font(.system(size: 14, weight: .bold))
                                            .foregroundColor(.appAccent)
                                    }
                                }
                            }
                            
                            if let error = errorMessage {
                                Text(error)
                                    .font(.system(size: 13, weight: .medium))
                                    .foregroundColor(.red)
                                    .multilineTextAlignment(.center)
                            }
                            
                            Button(action: login) {
                                HStack(spacing: 12) {
                                    if isLoading {
                                        ProgressView().tint(.white)
                                    }
                                    Text("Sign In")
                                }
                            }
                            .buttonStyle(PrimaryButtonStyle())
                        }
                        .padding(32)
                        .modernCard(radius: 40, shadow: Design.Shadows.medium)
                        .padding(.horizontal, 24)

                        VStack(spacing: 16) {
                            Text("New to MyRAJourney?")
                                .font(.system(size: 15))
                                .foregroundColor(.secondary)

                            NavigationLink(destination: RegisterView()) {
                                Text("Create an account")
                                    .font(.system(size: 16, weight: .bold))
                                    .foregroundColor(.appAccent)
                            }
                        }
                        .padding(.bottom, 40)
                    }
                }
            }
            .navigationBarHidden(true)
        }
    }
    
    private func login() {
        guard !email.isEmpty, !password.isEmpty else {
            errorMessage = "Please enter both email and password"
            return
        }
        
        isLoading = true
        errorMessage = nil
        
        let loginRequest = AuthRequest(name: nil, email: email, password: password, role: nil, phone: nil)
        
        AuthService.shared.login(request: loginRequest) { result in
            DispatchQueue.main.async {
                self.isLoading = false
                
                switch result {
                case .success(let response):
                    if response.success, let authData = response.data, let user = authData.user, let token = authData.token {
                        // Log in through AppState using real data from backend
                        appState.login(user: user, token: token)
                    } else if response.success {
                        // This handles cases where success is true but data is unexpectedly missing required fields for login
                        self.errorMessage = "Missing authorization data from server"
                    } else {
                        self.errorMessage = response.error?.message ?? response.message ?? "Login failed"
                    }
                case .failure(let error):
                    switch error {
                    case .unauthorized:
                        self.errorMessage = "Invalid email or password"
                    case .noData:
                        self.errorMessage = "No response from server"
                    case .decodingError:
                        self.errorMessage = "Failed to parse server response"
                    case .invalidURL:
                        self.errorMessage = "Invalid server configuration"
                    case .serverError(let message):
                        self.errorMessage = message
                    case .unknown(let innerError):
                        self.errorMessage = innerError.localizedDescription
                    }
                }
            }
        }
    }
    
    private func customTextField(title: String, text: Binding<String>, icon: String) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title)
                .font(.caption)
                .fontWeight(.bold)
                .foregroundColor(.secondary)
            
            HStack {
                Image(systemName: icon)
                    .foregroundColor(.blue)
                    .frame(width: 20)
                
                TextField(title, text: text)
                    .autocapitalization(.none)
                    .keyboardType(.emailAddress)
            }
            .padding()
            .background(Color(UIColor.secondarySystemBackground))
            .cornerRadius(10)
            .shadow(color: .black.opacity(0.05), radius: 5, x: 0, y: 2)
        }
    }
    
    private func customSecureField(title: String, text: Binding<String>, icon: String) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title)
                .font(.caption)
                .fontWeight(.bold)
                .foregroundColor(.secondary)
            
            HStack {
                Image(systemName: icon)
                    .foregroundColor(.blue)
                    .frame(width: 20)
                
                if showPassword {
                    TextField(title, text: text)
                } else {
                    SecureField(title, text: text)
                }
                
                Button(action: { showPassword.toggle() }) {
                    Image(systemName: showPassword ? "eye.slash" : "eye")
                        .foregroundColor(.secondary)
                }
            }
            .padding()
            .background(Color(UIColor.secondarySystemBackground))
            .cornerRadius(10)
            .shadow(color: .black.opacity(0.05), radius: 5, x: 0, y: 2)
        }
    }
}

#Preview {
    LoginView()
}
