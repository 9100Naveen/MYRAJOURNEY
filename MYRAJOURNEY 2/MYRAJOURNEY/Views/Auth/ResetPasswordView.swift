import SwiftUI

struct ResetPasswordView: View {
    let email: String
    @State private var newPassword = ""
    @State private var confirmPassword = ""
    @State private var isLoading = false
    @State private var message = ""
    @State private var isError = false
    @Environment(\.presentationMode) var presentationMode
    
    // For navigation back to root login
    @Environment(\.rootPresentationMode) private var rootPresentationMode: Binding<Bool>?
    
    var body: some View {
        ZStack {
            Color.white.ignoresSafeArea()
            
            VExternalContainer {
                VStack(spacing: 25) {
                    Image(systemName: "lock.shield")
                        .font(.system(size: 80))
                        .foregroundColor(.blue)
                        .padding(.bottom, 10)
                    
                    Text("Forgot Your Password?")
                        .font(.largeTitle)
                        .fontWeight(.bold)
                        .foregroundColor(.primary)
                        .multilineTextAlignment(.center)
                    
                    Text("Create a new password for your account.")
                        .font(.body)
                        .foregroundColor(.secondary)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal)
                    
                    Text("Email: \(email)")
                        .font(.headline)
                        .foregroundColor(.primary)
                    
                    ModernSecureField(title: "New Password", text: $newPassword, icon: "lock.fill")
                        .padding()
                        .background(Color.gray.opacity(0.1))
                        .cornerRadius(12)
                    
                    ModernSecureField(title: "Confirm Password", text: $confirmPassword, icon: "lock.fill")
                        .padding()
                        .background(Color.gray.opacity(0.1))
                        .cornerRadius(12)
                    
                    if !message.isEmpty {
                        Text(message)
                            .foregroundColor(isError ? .red : .green)
                            .padding()
                            .background(Color.gray.opacity(0.1))
                            .cornerRadius(10)
                    }
                    
                    Button(action: handleResetPassword) {
                        HStack {
                            if isLoading {
                                ProgressView()
                                    .progressViewStyle(CircularProgressViewStyle(tint: .white))
                                    .padding(.trailing, 5)
                            }
                            Text("RESET PASSWORD")
                                .fontWeight(.semibold)
                        }
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color.blue)
                        .foregroundColor(.white)
                        .cornerRadius(12)
                        .shadow(radius: 5)
                    }
                    .disabled(isLoading || newPassword.isEmpty || confirmPassword.isEmpty)
                    
                    Button(action: {
                        // Dismiss back to Login (if we have access to a custom rootPresentationMode, otherwise dismiss twice, etc. For simplicity just dismiss)
                        // If rootPresentationMode exists, use it, else dismiss
                        if let root = rootPresentationMode {
                            root.wrappedValue = false
                        } else {
                            presentationMode.wrappedValue.dismiss()
                        }
                    }) {
                        Text("BACK TO LOGIN")
                            .foregroundColor(.black)
                            .frame(maxWidth: .infinity)
                            .padding()
                            .background(Color.white)
                            .overlay(
                                RoundedRectangle(cornerRadius: 12)
                                    .stroke(Color.blue, lineWidth: 1)
                            )
                    }
                }
                .padding(30)
                .background(Color.white)
                .cornerRadius(20)
                .shadow(color: Color.black.opacity(0.1), radius: 20, x: 0, y: 10)
                .padding()
            }
        }
        .navigationBarHidden(true)
    }
    
    private func handleResetPassword() {
        if newPassword != confirmPassword {
            message = "Passwords do not match."
            isError = true
            return
        }
        
        isLoading = true
        message = ""
        
        AuthService.shared.resetPassword(email: email, newPassword: newPassword, code: "") { result in
            DispatchQueue.main.async {
                isLoading = false
                switch result {
                case .success(let response):
                    message = response.message ?? "Password reset successfully!"
                    isError = false
                    // Optional: auto navigate to login after success
                case .failure(let error):
                    message = error.localizedDescription
                    isError = true
                }
            }
        }
    }
}

// Custom Environment Key for returning to Root View
struct RootPresentationModeKey: EnvironmentKey {
    static let defaultValue: Binding<Bool>? = nil
}

extension EnvironmentValues {
    var rootPresentationMode: Binding<Bool>? {
        get { self[RootPresentationModeKey.self] }
        set { self[RootPresentationModeKey.self] = newValue }
    }
}

struct ResetPasswordView_Previews: PreviewProvider {
    static var previews: some View {
        ResetPasswordView(email: "test@example.com")
    }
}
