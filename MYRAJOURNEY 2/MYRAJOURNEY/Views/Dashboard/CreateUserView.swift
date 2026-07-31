import SwiftUI

struct CreateUserView: View {
    @Environment(\.presentationMode) var presentationMode
    let role: String // "patient" or "doctor"
    
    @State private var name = ""
    @State private var mobile = ""
    @State private var age = ""
    @State private var dob = ""
    @State private var gender = ""
    @State private var email = ""
    @State private var address = ""
    @State private var specialization = ""
    @State private var agreementAccepted = false
    
    @State private var isLoading = false
    @State private var errorMessage: String?
    @State private var showSuccess = false
    @State private var showGenderPicker = false
    
    // Image selection state
    @State private var selectedImage: UIImage?
    @State private var showImagePicker = false
    @State private var imageSourceType: UIImagePickerController.SourceType = .photoLibrary
    @State private var showImageSourceDialog = false
    
    let genders = ["Male", "Female", "Other"]
    
    var body: some View {
        ScrollView {
            VStack(spacing: 24) {
                // Header Title
                Text("Create New \(role.capitalized)")
                    .font(.system(size: 28, weight: .bold))
                    .padding(.top, 10)
                
                // Profile Picture Section
                VStack(spacing: 16) {
                    Text("Profile Picture")
                        .font(.system(size: 18, weight: .bold))
                    
                    ZStack {
                        Circle()
                            .stroke(Color.blue, lineWidth: 3)
                            .frame(width: 150, height: 150)
                            .background(Circle().fill(Color.blue.opacity(0.05)))
                        
                        if let image = selectedImage {
                            Image(uiImage: image)
                                .resizable()
                                .scaledToFill()
                                .frame(width: 145, height: 145)
                                .clipShape(Circle())
                        } else {
                            Image(systemName: "camera")
                                .font(.system(size: 60))
                                .foregroundColor(.gray.opacity(0.5))
                        }
                    }
                    
                    Button(action: { showImageSourceDialog = true }) {
                        HStack {
                            Image(systemName: "camera.fill")
                            Text("Add Picture")
                        }
                        .padding(.vertical, 12)
                        .padding(.horizontal, 24)
                        .background(Color.blue)
                        .foregroundColor(.white)
                        .cornerRadius(10)
                        .font(.headline)
                    }
                }
                .padding()
                .frame(maxWidth: .infinity)
                .background(Color.white)
                .cornerRadius(12)
                .shadow(color: .black.opacity(0.05), radius: 5, x: 0, y: 2)
                
                // Patient Information Section
                VStack(alignment: .leading, spacing: 20) {
                    Text("\(role.capitalized) Information")
                        .font(.system(size: 18, weight: .bold))
                    
                    VStack(spacing: 16) {
                        ModernFormInput(title: "Full Name", placeholder: "Enter full name", text: $name)
                        ModernFormInput(title: "Mobile Number", placeholder: "Enter mobile number (starts with 6-", text: $mobile)
                            .keyboardType(.phonePad)
                        ModernFormInput(title: "Age", placeholder: "Enter age", text: $age)
                            .keyboardType(.numberPad)
                        ModernFormInput(title: "Date of Birth", placeholder: "DD/MM/YYYY (Must be 18+ years)", text: $dob)
                        
                        // Gender Selector
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Gender")
                                .font(.system(size: 16, weight: .medium))
                            
                            Menu {
                                ForEach(genders, id: \.self) { g in
                                    Button(g) { gender = g }
                                }
                            } label: {
                                HStack {
                                    Text(gender.isEmpty ? "Select Gender" : gender)
                                        .foregroundColor(gender.isEmpty ? .gray : .primary)
                                    Spacer()
                                    Image(systemName: "chevron.down")
                                        .font(.caption)
                                        .foregroundColor(.gray)
                                }
                                .padding()
                                .background(RoundedRectangle(cornerRadius: 10).stroke(Color.black.opacity(0.8), lineWidth: 1))
                            }
                        }
                        
                        ModernFormInput(title: "Email Address", placeholder: "Enter email address", text: $email)
                            .autocapitalization(.none)
                            .keyboardType(.emailAddress)
                        
                        ModernFormInput(title: "Address", placeholder: "Enter address", text: $address)
                    }
                }
                .padding()
                .frame(maxWidth: .infinity)
                .background(Color.white)
                .cornerRadius(12)
                .shadow(color: .black.opacity(0.05), radius: 5, x: 0, y: 2)
                
                // Privacy Policy Section
                VStack(alignment: .leading, spacing: 16) {
                    Text("Privacy Policy Agreement")
                        .font(.system(size: 18, weight: .bold))
                    
                    Toggle(isOn: $agreementAccepted) {
                        Text("I have read and agree to the Privacy Policy (Required)")
                            .font(.system(size: 14))
                            .foregroundColor(.black.opacity(0.8))
                    }
                    .toggleStyle(CheckboxStyle())
                    
                    Button("View Privacy Policy") {
                        // Action
                    }
                    .font(.system(size: 16, weight: .medium))
                    .foregroundColor(.black)
                }
                .padding()
                .frame(maxWidth: .infinity)
                .background(Color.orange.opacity(0.1))
                .cornerRadius(12)
                
                // Credentials Box
                VStack(alignment: .leading, spacing: 12) {
                    Text("Auto-Generated Credentials")
                        .font(.system(size: 18, weight: .bold))
                    
                    VStack(alignment: .leading, spacing: 8) {
                        HStack {
                            Text("Patient ID:")
                                .fontWeight(.bold)
                            Text("(Auto-generated)")
                        }
                        HStack {
                            Text("Username:")
                                .fontWeight(.bold)
                            Text("(Will be generated)")
                        }
                        HStack {
                            Text("Default Password:")
                                .fontWeight(.bold)
                            Text("welcome1")
                        }
                    }
                    .font(.system(size: 16))
                }
                .padding()
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(Color.blue.opacity(0.05))
                .cornerRadius(12)
                
                if let error = errorMessage {
                    Text(error)
                        .foregroundColor(.red)
                        .font(.caption)
                }
                
                // Register Button
                Button(action: createUser) {
                    if isLoading {
                        ProgressView().tint(.white)
                    } else {
                        Text("Register \(role.capitalized)")
                            .font(.system(size: 20, weight: .bold))
                    }
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 16)
                .background(agreementAccepted ? Color.blue : Color.gray)
                .foregroundColor(.white)
                .cornerRadius(12)
                .shadow(color: Color.black.opacity(0.1), radius: 10, x: 0, y: 5)
                .disabled(isLoading || !agreementAccepted)
            }
            .padding()
            .blur(radius: showImageSourceDialog ? 3 : 0)
        }
        .overlay {
            if showImageSourceDialog {
                Color.black.opacity(0.4)
                    .ignoresSafeArea()
                    .onTapGesture { showImageSourceDialog = false }
                
                VStack(spacing: 0) {
                    VStack(alignment: .leading, spacing: 16) {
                        Text("Select Profile Picture")
                            .font(.system(size: 20, weight: .bold))
                        
                        Text("Choose how you want to add your profile picture:")
                            .font(.system(size: 16))
                            .foregroundColor(.secondary)
                        
                        VStack(spacing: 0) {
                            Button(action: {
                                imageSourceType = .camera
                                showImageSourceDialog = false
                                showImagePicker = true
                            }) {
                                HStack(spacing: 12) {
                                    Image(systemName: "camera.fill")
                                    Text("TAKE PHOTO")
                                        .font(.system(size: 16, weight: .semibold))
                                    Spacer()
                                }
                                .padding(.vertical, 16)
                                .foregroundColor(.primary)
                            }
                            
                            Divider()
                            
                            Button(action: {
                                imageSourceType = .photoLibrary
                                showImageSourceDialog = false
                                showImagePicker = true
                            }) {
                                HStack(spacing: 12) {
                                    Image(systemName: "photo.on.rectangle.angled")
                                    Text("CHOOSE FROM GALLERY")
                                        .font(.system(size: 16, weight: .semibold))
                                    Spacer()
                                }
                                .padding(.vertical, 16)
                                .foregroundColor(.primary)
                            }
                        }
                    }
                    .padding(24)
                    
                    HStack {
                        Spacer()
                        Button("CANCEL") {
                            showImageSourceDialog = false
                        }
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(.black)
                        .padding(20)
                    }
                }
                .background(Color.white)
                .cornerRadius(4)
                .padding(.horizontal, 30)
                .transition(.scale.combined(with: .opacity))
            }
        }
        .sheet(isPresented: $showImagePicker) {
            ImagePicker(selectedImage: $selectedImage, sourceType: imageSourceType)
        }
        .background(Color(red: 0.98, green: 0.98, blue: 0.98))
        .navigationBarTitleDisplayMode(.inline)
        .alert(isPresented: $showSuccess) {
            Alert(
                title: Text("Success"),
                message: Text("\(role.capitalized) created successfully."),
                dismissButton: .default(Text("OK")) {
                    presentationMode.wrappedValue.dismiss()
                }
            )
        }
    }
    
    private func createUser() {
        isLoading = true
        errorMessage = nil
        
        let request = CreateUserRequest(
            name: name,
            email: email,
            password: "welcome1", // Matches the screenshot's default password
            role: role.uppercased(),
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
                    if response.success, let newUser = response.data {
                        // If a doctor is creating a patient, auto-assign
                        if role.lowercased() == "patient", 
                           let doctorIdStr = SessionManager.shared.userId,
                           let doctorId = Int(doctorIdStr) {
                            
                            DoctorService.shared.assignPatientToDoctor(patientId: newUser.id, doctorId: doctorId) { _ in
                                DispatchQueue.main.async {
                                    NotificationCenter.default.post(name: NSNotification.Name("RefreshDoctorDashboard"), object: nil)
                                    showSuccess = true
                                }
                            }
                        } else {
                            NotificationCenter.default.post(name: NSNotification.Name("RefreshDoctorDashboard"), object: nil)
                            showSuccess = true
                        }
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

struct ModernFormInput: View {
    let title: String
    let placeholder: String
    @Binding var text: String
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title)
                .font(.system(size: 16, weight: .medium))
            
            TextField(placeholder, text: $text)
                .padding()
                .background(RoundedRectangle(cornerRadius: 10).stroke(Color.black.opacity(0.8), lineWidth: 1))
        }
    }
}

struct CheckboxStyle: ToggleStyle {
    func makeBody(configuration: Configuration) -> some View {
        HStack {
            Image(systemName: configuration.isOn ? "checkmark.square.fill" : "square")
                .font(.title2)
                .foregroundColor(configuration.isOn ? .blue : .gray)
                .onTapGesture {
                    configuration.isOn.toggle()
                }
            
            configuration.label
        }
    }
}

#Preview {
    NavigationView {
        CreateUserView(role: "patient")
    }
}
