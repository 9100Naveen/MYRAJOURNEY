import SwiftUI

struct CreateAppointmentView: View {
    @Environment(\.dismiss) var dismiss
    @State private var patients: [Patient] = []
    @State private var doctors: [User] = []
    @State private var selectedPatientId: Int?
    @State private var selectedDoctorId: Int?
    @State private var selectedTimeSlot: String = "10:00 AM - 10:30 AM"
    @State private var selectedDate: Date?
    @State private var showDatePicker = false
    @State private var isLoading = false
    @State private var isSaving = false
    @State private var errorMessage: String?
    @State private var showSuccess = false
    
    let timeSlots = [
        "09:00 AM - 09:30 AM", "09:30 AM - 10:00 AM", "10:00 AM - 10:30 AM",
        "10:30 AM - 11:00 AM", "11:00 AM - 11:30 AM", "11:30 AM - 12:00 PM",
        "02:00 PM - 02:30 PM", "02:30 PM - 03:00 PM", "03:00 PM - 03:30 PM",
        "03:30 PM - 04:00 PM", "04:00 PM - 04:30 PM", "04:30 PM - 05:00 PM"
    ]
    
    var body: some View {
        VStack(spacing: 0) {
            // Purple header to match screenshot
            Color(red: 0.3, green: 0, blue: 0.7)
                .frame(height: 1) // Just a thin line or background for status bar area
                .ignoresSafeArea(edges: .top)
            
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    
                    // Patient Selection
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Select Patient")
                            .font(.system(size: 16, weight: .bold))
                        
                        if isLoading && patients.isEmpty {
                            ProgressView().padding(.vertical, 8)
                        } else {
                            Menu {
                                Picker("Select Patient", selection: $selectedPatientId) {
                                    Text("Select a patient").tag(nil as Int?)
                                    ForEach(patients) { patient in
                                        Text(patient.name).tag(patient.id as Int?)
                                    }
                                }
                            } label: {
                                selectionLabel(text: patients.first(where: { $0.id == selectedPatientId })?.name ?? "Select a patient", isPlaceholder: selectedPatientId == nil)
                            }
                        }
                    }
                    
                    // Doctor Selection
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Select Doctor")
                            .font(.system(size: 16, weight: .bold))
                        
                        if isLoading && doctors.isEmpty {
                            ProgressView().padding(.vertical, 8)
                        } else {
                            Menu {
                                Picker("Select Doctor", selection: $selectedDoctorId) {
                                    Text("Select a doctor").tag(nil as Int?)
                                    ForEach(doctors) { doctor in
                                        Text(doctor.name ?? "Unknown Doctor").tag(doctor.id as Int?)
                                    }
                                }
                            } label: {
                                selectionLabel(text: doctors.first(where: { $0.id == selectedDoctorId })?.name ?? "Select a doctor", isPlaceholder: selectedDoctorId == nil)
                            }
                        }
                    }
                    
                    // Time Slot Selection
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Select Time Slot")
                            .font(.system(size: 16, weight: .bold))
                        
                        Menu {
                            Picker("Time Slot", selection: $selectedTimeSlot) {
                                ForEach(timeSlots, id: \.self) { slot in
                                    Text(slot).tag(slot)
                                }
                            }
                        } label: {
                            HStack {
                                Text(selectedTimeSlot)
                                    .foregroundColor(.primary)
                                Spacer()
                                Image(systemName: "arrowtriangle.down.fill")
                                    .font(.system(size: 12))
                                    .foregroundColor(.gray)
                            }
                            .padding()
                            .background(Color.white)
                            .overlay(
                                RoundedRectangle(cornerRadius: 4)
                                    .stroke(Color.gray.opacity(0.2), lineWidth: 1)
                            )
                        }
                    }
                    
                    // Date Picker Button
                    Button(action: { showDatePicker.toggle() }) {
                        Text("PICK DATE")
                            .font(.system(size: 16, weight: .bold))
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .padding()
                            .background(Color.blue)
                            .cornerRadius(4)
                    }
                    .padding(.top, 10)
                    
                    Text(selectedDate == nil ? "No date selected" : formattedDate)
                        .font(.system(size: 16))
                        .foregroundColor(.primary)
                    
                    Spacer(minLength: 40)
                    
                    // Create Appointment Button
                    Button(action: createAppointment) {
                        if isSaving {
                            ProgressView().tint(.white)
                                .frame(maxWidth: .infinity)
                                .padding()
                                .background(Color.blue.opacity(0.7))
                                .cornerRadius(8)
                        } else {
                            Text("CREATE APPOINTMENT")
                                .font(.system(size: 16, weight: .bold))
                                .foregroundColor(.white)
                                .frame(maxWidth: .infinity)
                                .padding()
                                .background(isFormValid ? Color.blue : Color.gray.opacity(0.5))
                                .cornerRadius(8)
                        }
                    }
                    .disabled(!isFormValid || isSaving)
                    
                    if let error = errorMessage {
                        Text(error)
                            .foregroundColor(.red)
                            .font(.system(size: 14))
                            .padding(.top, 10)
                    }
                }
                .padding(20)
            }
        }
        .navigationBarTitle("Create Appointment", displayMode: .inline)
        .onAppear(perform: loadData)
    .sheet(isPresented: $showDatePicker) {
        VStack {
            DatePicker("Select Date", selection: Binding(get: { self.selectedDate ?? Date() }, set: { self.selectedDate = $0 }), displayedComponents: .date)
                .datePickerStyle(GraphicalDatePickerStyle())
                .padding()
            
            Button("Done") {
                if selectedDate == nil { selectedDate = Date() }
                showDatePicker = false
            }
            .padding()
        }
    }
    .alert(isPresented: $showSuccess) {
        Alert(
            title: Text("Success"),
            message: Text("Appointment created successfully"),
            dismissButton: .default(Text("OK")) {
                dismiss()
            }
        )
        }
    }
    
    private var isFormValid: Bool {
        selectedPatientId != nil && selectedDoctorId != nil && selectedDate != nil
    }

    private var formattedDate: String {
        guard let date = selectedDate else { return "" }
        let formatter = DateFormatter()
        formatter.dateStyle = .long
        return formatter.string(from: date)
    }

    private func selectionLabel(text: String, isPlaceholder: Bool) -> some View {
        HStack {
            Text(text)
                .foregroundColor(isPlaceholder ? .gray : .primary)
            Spacer()
            Image(systemName: "arrowtriangle.down.fill")
                .font(.system(size: 12))
                .foregroundColor(.gray)
        }
        .padding()
        .background(Color.white)
        .overlay(
            RoundedRectangle(cornerRadius: 4)
                .stroke(Color.gray.opacity(0.2), lineWidth: 1)
        )
    }
    
    private func loadData() {
        isLoading = true
        let group = DispatchGroup()
        
        group.enter()
        DoctorService.shared.getPatients { result in
            DispatchQueue.main.async {
                if case .success(let response) = result {
                    self.patients = response.data ?? []
                }
                group.leave()
            }
        }
        
        group.enter()
        DoctorService.shared.getAllDoctors { result in
            DispatchQueue.main.async {
                if case .success(let response) = result {
                    self.doctors = response.data ?? []
                } else {
                    // Fallback to empty if error
                    self.doctors = []
                }
                group.leave()
            }
        }
        
        group.notify(queue: .main) {
            isLoading = false
            // Default to current doctor if available
            if let doctorIdStr = SessionManager.shared.userId, let doctorId = Int(doctorIdStr) {
                if self.selectedDoctorId == nil {
                    self.selectedDoctorId = doctorId
                }
                
                // Ensure current doctor is in the list so their name appears
                if !self.doctors.contains(where: { $0.id == doctorId }) {
                    let currentDoctor = User(
                        id: doctorId,
                        name: SessionManager.shared.userName ?? "Current Doctor",
                        email: SessionManager.shared.userEmail,
                        role: "DOCTOR",
                        assignedDoctorId: nil,
                        assignedDoctorName: nil,
                        phone: nil,
                        address: nil,
                        age: nil,
                        gender: nil,
                        profileImage: nil,
                        specialization: nil,
                        active: true,
                        createdAt: nil,
                        updatedAt: nil,
                        lastLoginAt: nil,
                        status: "Active",
                        avatarUrl: nil
                    )
                    self.doctors.append(currentDoctor)
                }
            }
        }
    }
    
    private func createAppointment() {
        guard let patientId = selectedPatientId, let doctorId = selectedDoctorId, let date = selectedDate else { return }
        
        isSaving = true
        errorMessage = nil
        
        // Prepare data for API
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        let dateString = formatter.string(from: date)
        
        // Extract start/end from slot "10:00 AM - 10:30 AM"
        let times = selectedTimeSlot.components(separatedBy: " - ")
        let startTimeStr = times.first ?? ""
        let endTimeStr = times.last ?? ""
        
        // Construct ISO-like timestamps for the backend
        let fullStartTime = "\(dateString) \(convertTo24Hour(startTimeStr))"
        let fullEndTime = "\(dateString) \(convertTo24Hour(endTimeStr))"
        
        let parameters: [String: Any] = [
            "patient_id": patientId,
            "doctor_id": doctorId,
            "start_time": fullStartTime,
            "end_time": fullEndTime,
            "title": "Medical Consultation",
            "description": "Scheduled appointment via dashboard",
            "appointment_type": "Regular",
            "status": "Scheduled"
        ]
        
        // We'll use a generic POST request or add a method to DoctorService
        // For now, let's assume we'll add it to DoctorService
        createAppointmentApiCall(parameters: parameters)
    }
    
    private func convertTo24Hour(_ time12: String) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "h:mm a"
        if let date = formatter.date(from: time12) {
            formatter.dateFormat = "HH:mm:ss"
            return formatter.string(from: date)
        }
        return "00:00:00"
    }
    
    private func createAppointmentApiCall(parameters: [String: Any]) {
        guard let body = try? JSONSerialization.data(withJSONObject: parameters) else {
            isSaving = false
            errorMessage = "Data error"
            return
        }
        
        let endpoint = Endpoint(path: "appointments", method: .post, body: body)
        ApiClient.shared.request(endpoint) { (result: Result<ApiResponse<Appointment>, NetworkError>) in
            DispatchQueue.main.async {
                isSaving = false
                switch result {
                case .success:
                    showSuccess = true
                case .failure(let error):
                    errorMessage = "Failed: \(error.localizedDescription)"
                }
            }
        }
    }
}

#Preview {
    NavigationView {
        CreateAppointmentView()
    }
}
