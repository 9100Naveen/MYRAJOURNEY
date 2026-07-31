import SwiftUI

struct RegisterView: View {
    @Environment(\.presentationMode) var presentationMode
    @State private var name = ""
    @State private var email = ""
    @State private var phone = ""
    @State private var password = ""
    @State private var confirmPassword = ""
    @State private var selectedRole = "Select Role"
    @State private var isLoading = false
    @State private var errorMessage: String?
    @State private var showPassword = false
    @State private var showConfirmPassword = false
    
    let roles = ["Patient", "Doctor", "Admin"]
    
    var body: some View {
        ZStack {
            // Background Gradient
            LinearGradient(
                colors: [Design.Colors.background, Color.white],
                startPoint: .top,
                endPoint: .bottom
            ).ignoresSafeArea()
            
            ScrollView(showsIndicators: false) {
                VStack(spacing: 32) {
                    VStack(spacing: 12) {
                        Text("Join MyRA")
                            .font(.system(size: 34, weight: .bold, design: .rounded))
                            .foregroundColor(Design.Colors.primary)
                        
                        Text("Your journey to better health starts here")
                            .font(.system(size: 16, weight: .medium))
                            .foregroundColor(.secondary)
                            .multilineTextAlignment(.center)
                    }
                    .padding(.top, 40)

                    VStack(spacing: 24) {
                        VStack(spacing: 18) {
                            ModernInputField(title: "Full Name", text: $name, icon: "person.fill")
                            ModernInputField(title: "Email Address", text: $email, icon: "envelope.fill", keyboardType: .emailAddress, textContentType: .emailAddress)
                            ModernInputField(title: "Phone Number", text: $phone, icon: "phone.fill", keyboardType: .phonePad, textContentType: .telephoneNumber)

                            VStack(alignment: .leading, spacing: 10) {
                                Text("Register As")
                                    .font(.caption)
                                    .fontWeight(.bold)
                                    .foregroundColor(.secondary)
                                    .textCase(.uppercase)
                                    .tracking(1)

                                Menu {
                                    ForEach(roles, id: \.self) { role in
                                        Button(role) {
                                            selectedRole = role
                                        }
                                    }
                                } label: {
                                    HStack {
                                        Text(selectedRole)
                                            .font(.system(size: 16, weight: .medium))
                                            .foregroundColor(selectedRole == "Select Role" ? .secondary : .primary)
                                        Spacer()
                                        Image(systemName: "chevron.down")
                                            .font(.system(size: 14, weight: .bold))
                                            .foregroundColor(.secondary)
                                    }
                                    .padding()
                                    .background(Color.appSurface)
                                    .cornerRadius(16)
                                    .overlay(
                                        RoundedRectangle(cornerRadius: 16)
                                            .stroke(Color.appBorder, lineWidth: 1)
                                    )
                                }
                            }

                            ModernSecureField(title: "Password", text: $password, icon: "lock.fill")
                            ModernSecureField(title: "Confirm Password", text: $confirmPassword, icon: "lock.fill")
                        }
                        
                        if let error = errorMessage {
                            Text(error)
                                .font(.system(size: 13, weight: .medium))
                                .foregroundColor(.red)
                                .multilineTextAlignment(.center)
                        }

                        Button(action: register) {
                            HStack {
                                if isLoading {
                                    ProgressView().tint(.white)
                                }
                                Text("Create Account")
                            }
                        }
                        .buttonStyle(PrimaryButtonStyle())
                    }
                    .padding(28)
                    .modernCard(radius: 40, shadow: Design.Shadows.medium)
                    .padding(.horizontal, 24)

                    Button(action: {
                        presentationMode.wrappedValue.dismiss()
                    }) {
                        HStack {
                            Text("Already have an account?")
                                .foregroundColor(.secondary)
                            Text("Sign In")
                                .fontWeight(.bold)
                                .foregroundColor(.appAccent)
                        }
                        .font(.system(size: 15))
                    }
                    .padding(.bottom, 40)
                }
            }
        }
        .navigationBarHidden(true)
    }
    
    private func register() {
        guard !name.isEmpty, !email.isEmpty, !password.isEmpty, !phone.isEmpty else {
            errorMessage = "Please fill in all fields"
            return
        }
        
        if password != confirmPassword {
            errorMessage = "Passwords do not match"
            return
        }
        
        if selectedRole == "Select Role" {
            errorMessage = "Please select a role"
            return
        }
        
        isLoading = true
        errorMessage = nil
        
        let request = AuthRequest(name: name, email: email, password: password, role: selectedRole.lowercased(), phone: phone)
        AuthService.shared.register(request: request) { result in
            DispatchQueue.main.async {
                isLoading = false
                switch result {
                case .success(let response):
                    if response.success {
                        presentationMode.wrappedValue.dismiss()
                    } else {
                        errorMessage = response.error?.message ?? response.message ?? "Registration failed"
                    }
                case .failure(let error):
                    errorMessage = error.localizedDescription
                }
            }
        }
    }
    
    private func customTextField(placeholder: String, text: Binding<String>) -> some View {
        TextField(placeholder, text: text)
            .autocapitalization(.none)
            .padding()
            .frame(height: 55)
            .background(Color(red: 0.95, green: 0.95, blue: 0.95))
            .cornerRadius(12)
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(Color.gray.opacity(0.3), lineWidth: 1.5)
            )
            .font(.system(size: 18))
    }
    
    private func customSecureField(placeholder: String, text: Binding<String>, isVisible: Binding<Bool>) -> some View {
        HStack {
            if isVisible.wrappedValue {
                TextField(placeholder, text: text)
                    .autocapitalization(.none)
            } else {
                SecureField(placeholder, text: text)
            }
            
            Button(action: { isVisible.wrappedValue.toggle() }) {
                Image(systemName: isVisible.wrappedValue ? "eye.slash" : "eye")
                    .foregroundColor(.gray)
                    .font(.system(size: 18, weight: .bold))
            }
        }
        .padding()
        .frame(height: 55)
        .background(Color(red: 0.95, green: 0.95, blue: 0.95))
        .cornerRadius(12)
        .overlay(
            RoundedRectangle(cornerRadius: 12)
                .stroke(Color.gray.opacity(0.3), lineWidth: 1.0)
        )
        .font(.system(size: 18))
    }
}

#Preview {
    RegisterView()
}
