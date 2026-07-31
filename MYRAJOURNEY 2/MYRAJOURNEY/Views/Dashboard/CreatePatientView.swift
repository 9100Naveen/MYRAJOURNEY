import SwiftUI

struct CreatePatientView: View {
    @Environment(\.presentationMode) var presentationMode
    @State private var name = ""
    @State private var email = ""
    @State private var mobile = ""
    @State private var age = ""
    @State private var address = ""
    @State private var isLoading = false
    @State private var errorMessage: String?
    @State private var showSuccess = false
    
    var body: some View {
        NavigationView {
            Form {
                Section(header: Text("Basic Information")) {
                    TextField("Full Name", text: $name)
                    TextField("Email", text: $email)
                        .autocapitalization(.none)
                        .keyboardType(.emailAddress)
                    TextField("Mobile", text: $mobile)
                        .keyboardType(.phonePad)
                    TextField("Age", text: $age)
                        .keyboardType(.numberPad)
                }
                
                Section(header: Text("Address")) {
                    TextEditor(text: $address)
                        .frame(height: 100)
                }
                
                if let error = errorMessage {
                    Section {
                        Text(error)
                            .foregroundColor(.red)
                            .font(.caption)
                    }
                }
                
                Section {
                    Button(action: createPatient) {
                        if isLoading {
                            HStack {
                                Spacer()
                                ProgressView().tint(.blue)
                                Spacer()
                            }
                        } else {
                            Text("Register Patient")
                                .frame(maxWidth: .infinity)
                                .fontWeight(.bold)
                        }
                    }
                    .disabled(isLoading || name.isEmpty || email.isEmpty)
                }
            }
            .navigationTitle("New Patient")
            .scrollContentBackground(.hidden)
            .background(Color.white)
            .navigationBarItems(leading: Button("Cancel") {
                presentationMode.wrappedValue.dismiss()
            })
            .alert(isPresented: $showSuccess) {
                Alert(
                    title: Text("Success"),
                    message: Text("Patient registered successfully."),
                    dismissButton: .default(Text("OK")) {
                        presentationMode.wrappedValue.dismiss()
                    }
                )
            }
        }
    }
    
    private func createPatient() {
        isLoading = true
        errorMessage = nil
        
        let request = CreateUserRequest(
            name: name,
            email: email,
            password: "Welcome@456", // Default password as per Android logic
            role: "PATIENT",
            mobile: mobile,
            address: address,
            age: age,
            specialization: nil
        )
        
        DoctorService.shared.createUser(request: request) { result in
            DispatchQueue.main.async {
                isLoading = false
                switch result {
                case .success(let response):
                    if response.success {
                        showSuccess = true
                    } else {
                        errorMessage = response.message ?? "Registration failed"
                    }
                case .failure(let error):
                    errorMessage = error.localizedDescription
                }
            }
        }
    }
}

#Preview {
    CreatePatientView()
}
