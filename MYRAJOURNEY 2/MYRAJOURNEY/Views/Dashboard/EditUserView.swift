import SwiftUI

struct EditUserView: View {
    @Environment(\.presentationMode) var presentationMode
    let user: User
    
    @State private var name: String
    @State private var email: String
    @State private var mobile: String
    @State private var age: String
    @State private var address: String
    @State private var specialization: String
    @State private var isLoading = false
    @State private var showSuccess = false
    @State private var errorMessage: String?
    
    init(user: User) {
        self.user = user
        _name = State(initialValue: user.name ?? "")
        _email = State(initialValue: user.email ?? "")
        _mobile = State(initialValue: user.phone ?? "")
        _age = State(initialValue: user.age.map { String($0) } ?? "")
        _address = State(initialValue: user.address ?? "")
        _specialization = State(initialValue: user.specialization ?? "")
    }
    
    var body: some View {
        ZStack {
            Color.white.ignoresSafeArea()
            
            ScrollView {
                VStack(spacing: 25) {
                    Text("Edit \(user.role?.lowercased() ?? "user") info")
                        .font(.headline)
                        .foregroundColor(.secondary)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(.horizontal)
                    
                    VStack(spacing: 15) {
                        ModernInputField(title: "Full Name", text: $name, icon: "person.fill")
                        ModernInputField(title: "Email Address", text: $email, icon: "envelope.fill")
                            .disabled(true)
                            .opacity(0.6)
                        ModernInputField(title: "Mobile Number", text: $mobile, icon: "phone.fill")
                        ModernInputField(title: "Age", text: $age, icon: "calendar")
                        
                        if user.role?.lowercased() == "doctor" {
                            ModernInputField(title: "Specialization", text: $specialization, icon: "cross.case.fill")
                        } else {
                            VStack(alignment: .leading, spacing: 10) {
                                Text("Residential Address")
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                                TextEditor(text: $address)
                                    .frame(height: 100)
                                    .padding(8)
                                    .background(Color.gray.opacity(0.05))
                                    .cornerRadius(10)
                                    .foregroundColor(.primary)
                            }
                        }
                    }
                    .padding()
                    .background(Color.gray.opacity(0.05))
                    .cornerRadius(20)
                    .padding(.horizontal)
                    
                    if let error = errorMessage {
                        Text(error)
                            .font(.caption)
                            .foregroundColor(.red)
                            .padding(.horizontal)
                    }

                    Button(action: updateUser) {
                        HStack {
                            if isLoading {
                                ProgressView().tint(.blue)
                            } else {
                                Text("Save Changes")
                                    .fontWeight(.bold)
                            }
                        }
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color.blue)
                        .foregroundColor(.white)
                        .cornerRadius(12)
                    }
                    .padding(.horizontal)
                    .disabled(isLoading)
                }
                .padding(.vertical)
            }
        }
        .navigationTitle("Edit Profile")
        .navigationBarTitleDisplayMode(.inline)
        .alert(isPresented: $showSuccess) {
            Alert(title: Text("Success"), message: Text("Profile updated successfully."), dismissButton: .default(Text("OK")) {
                presentationMode.wrappedValue.dismiss()
            })
        }
    }
    
    private func updateUser() {
        isLoading = true
        let request = CreateUserRequest(
            name: name,
            email: email,
            password: "",
            role: user.role?.uppercased() ?? "",
            mobile: mobile,
            address: address,
            age: age,
            specialization: user.role?.lowercased() == "doctor" ? specialization : nil
        )
        DoctorService.shared.updateUser(userId: user.id, request: request) { result in
            DispatchQueue.main.async {
                self.isLoading = false
                switch result {
                case .success(let response):
                    if response.success {
                        self.showSuccess = true
                    }
                case .failure(let error):
                    self.errorMessage = "Update failed: \(error.localizedDescription)"
                }
            }
        }
    }
}

#Preview {
    NavigationView {
        EditUserView(user: User(
            id: 1,
            name: "John Doe",
            email: "john@example.com",
            role: "patient",
            assignedDoctorId: nil,
            assignedDoctorName: nil,
            phone: "+1234567890",
            address: "123 Main St",
            age: 45,
            gender: "Male",
            profileImage: nil,
            specialization: nil,
            active: true,
            createdAt: nil,
            updatedAt: nil,
            lastLoginAt: nil,
            status: "active",
            avatarUrl: nil
        ))
    }
}
