import SwiftUI

struct AssignPatientView: View {
    @State private var patients: [User] = []
    @State private var doctors: [User] = []
    @State private var isLoading = true
    @State private var errorMessage: String?
    @Environment(\.dismiss) private var dismiss
    
    let adminBlue = Color(red: 0.11, green: 0.58, blue: 0.95)
    
    var body: some View {
        ZStack {
            Color(red: 0.97, green: 0.98, blue: 0.99).ignoresSafeArea()
            
            VStack(spacing: 0) {
                // PREMIUM BLUE HEADER
                HStack {
                    Button(action: { dismiss() }) {
                        Image(systemName: "chevron.left")
                            .font(.system(size: 22, weight: .bold))
                            .foregroundColor(.white)
                    }
                    
                    Spacer()
                    
                    Text("Assign Patients to Doctors")
                        .font(.system(size: 20, weight: .bold))
                        .foregroundColor(.white)
                    
                    Spacer()
                    
                    // Spacer for balance
                    Color.clear.frame(width: 22)
                }
                .padding(.horizontal, 20)
                .padding(.top, 12)
                .padding(.bottom, 15)
                .background(adminBlue)
                
                if isLoading {
                    Spacer()
                    ProgressView().tint(adminBlue)
                    Spacer()
                } else if let error = errorMessage {
                    Spacer()
                    VStack(spacing: 15) {
                        Image(systemName: "exclamationmark.triangle")
                            .font(.largeTitle)
                            .foregroundColor(.red)
                        Text(error)
                            .foregroundColor(.red)
                            .multilineTextAlignment(.center)
                            .padding(.horizontal)
                        Button("Retry") { loadData() }
                            .padding()
                            .background(adminBlue)
                            .foregroundColor(.white)
                            .cornerRadius(10)
                    }
                    Spacer()
                } else {
                    ScrollView {
                        VStack(spacing: 16) {
                            ForEach(patients.filter { $0.role?.uppercased() == "PATIENT" }) { patient in
                                PatientAssignmentCard(patient: patient, doctors: doctors)
                            }
                        }
                        .padding(.vertical, 20)
                    }
                }
            }
        }
        .navigationBarBackButtonHidden(true)
        .toolbar(.hidden, for: .navigationBar)
        .navigationBarHidden(true)
        .onAppear(perform: loadData)
    }
    
    private func loadData() {
        isLoading = true
        errorMessage = nil
        DoctorService.shared.getAllUsers { result in
            DispatchQueue.main.async {
                self.isLoading = false
                switch result {
                case .success(let response):
                    if response.success, let data = response.data {
                        self.patients = data.filter { $0.role?.uppercased() == "PATIENT" }
                        self.doctors = data.filter { $0.role?.uppercased() == "DOCTOR" }
                    } else {
                        self.errorMessage = response.message ?? "Failed to load users"
                    }
                case .failure(let error):
                    self.errorMessage = error.localizedDescription
                }
            }
        }
    }
}

struct PatientAssignmentCard: View {
    let patient: User
    let doctors: [User]
    @State private var selectedDoctorId: Int?
    @State private var isSaving = false
    @State private var success = false
    
    let adminBlue = Color(red: 0.11, green: 0.58, blue: 0.95)
    
    init(patient: User, doctors: [User]) {
        self.patient = patient
        self.doctors = doctors
        _selectedDoctorId = State(initialValue: patient.assignedDoctorId)
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: 15) {
            // Patient Info
            VStack(alignment: .leading, spacing: 4) {
                Text(patient.name ?? "Unknown Patient")
                    .font(.system(size: 20, weight: .bold))
                
                Text(patient.email ?? "")
                    .font(.system(size: 16))
                    .foregroundColor(.secondary)
                
                Text("Assigned to: \(patient.assignedDoctorName ?? doctors.first(where: { $0.id == patient.assignedDoctorId })?.name ?? "None")")
                    .font(.system(size: 16, weight: .medium))
                    .italic()
                    .foregroundColor(.gray)
                    .padding(.top, 4)
            }
            
            // Selection Section
            VStack(alignment: .leading, spacing: 8) {
                Text("Assign to Doctor:")
                    .font(.system(size: 16, weight: .bold))
                
                // Picker Styled like an Input Box
                Menu {
                    Picker("Select Doctor", selection: $selectedDoctorId) {
                        Text("Unassigned").tag(nil as Int?)
                        ForEach(doctors) { doctor in
                            Text(doctor.name ?? "Unnamed Doctor").tag(doctor.id as Int?)
                        }
                    }
                } label: {
                    HStack {
                        Text(doctors.first(where: { $0.id == selectedDoctorId })?.name ?? "Select Doctor")
                            .foregroundColor(selectedDoctorId == nil ? .gray : .primary)
                        Spacer()
                        Image(systemName: "chevron.down")
                            .foregroundColor(.gray)
                    }
                    .padding()
                    .frame(height: 50)
                    .background(Color.white)
                    .cornerRadius(8)
                    .overlay(
                        RoundedRectangle(cornerRadius: 8)
                            .stroke(Color.gray.opacity(0.3), lineWidth: 1)
                    )
                }
            }
            
            // Action Button
            Button(action: save) {
                if isSaving {
                    ProgressView().tint(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 50)
                        .background(adminBlue)
                        .cornerRadius(10)
                } else if success {
                    HStack {
                        Image(systemName: "checkmark.circle.fill")
                        Text("ASSIGNED")
                    }
                    .font(.headline)
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .frame(height: 50)
                    .background(Color.green)
                    .cornerRadius(10)
                } else {
                    Text("ASSIGN")
                        .font(.headline)
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 50)
                        .background(adminBlue)
                        .cornerRadius(10)
                }
            }
            .disabled(isSaving || (selectedDoctorId == patient.assignedDoctorId && !success))
        }
        .padding(20)
        .background(Color.white)
        .cornerRadius(15)
        .shadow(color: Color.black.opacity(0.1), radius: 10, x: 0, y: 5)
        .padding(.horizontal, 16)
    }
    
    private func save() {
        guard let docId = selectedDoctorId else { return }
        isSaving = true
        DoctorService.shared.assignPatientToDoctor(patientId: patient.id, doctorId: docId) { result in
            DispatchQueue.main.async {
                isSaving = false
                switch result {
                case .success:
                    withAnimation {
                        success = true
                    }
                    DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                        withAnimation {
                            success = false
                        }
                    }
                case .failure(let error):
                    print("Assignment error: \(error)")
                }
            }
        }
    }
}
