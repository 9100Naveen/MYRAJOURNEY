import SwiftUI

struct AllPatientsView: View {
    @State private var patients: [User] = []
    @State private var isLoading = true
    @State private var errorMessage: String?
    @Environment(\.dismiss) private var dismiss
    
    let adminBlue = Color(red: 0.11, green: 0.58, blue: 0.95)
    
    var body: some View {
        ZStack {
            Color(red: 0.98, green: 0.98, blue: 0.99).ignoresSafeArea()
            
            VStack(spacing: 0) {
                // PREMIUM BLUE HEADER
                HStack {
                    Button(action: { dismiss() }) {
                        Image(systemName: "chevron.left")
                            .font(.system(size: 22, weight: .bold))
                            .foregroundColor(.white)
                    }
                    
                    Spacer()
                    
                    Text("All Patients")
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
                .shadow(color: Color.black.opacity(0.1), radius: 5, x: 0, y: 3)
                
                if isLoading {
                    Spacer()
                    ProgressView()
                        .tint(adminBlue)
                        .scaleEffect(1.5)
                    Spacer()
                } else if let error = errorMessage {
                    Spacer()
                    VStack(spacing: 15) {
                        Image(systemName: "exclamationmark.triangle.fill")
                            .font(.system(size: 50))
                            .foregroundColor(.red)
                        Text(error)
                            .foregroundColor(.secondary)
                            .multilineTextAlignment(.center)
                            .padding(.horizontal)
                        Button("Retry") { loadData() }
                            .padding(.horizontal, 30)
                            .padding(.vertical, 12)
                            .background(adminBlue)
                            .foregroundColor(.white)
                            .cornerRadius(10)
                    }
                    Spacer()
                } else {
                    ScrollView {
                        LazyVStack(spacing: 16) {
                            ForEach(patients) { patient in
                                NavigationLink(destination: UserDetailsView(user: patient)) {
                                    PatientListCard(patient: patient)
                                }
                            }
                        }
                        .padding(.vertical, 20)
                        .padding(.horizontal, 16)
                    }
                    .refreshable {
                        loadData()
                    }
                }
            }
            
            // Floating Action Button
            VStack {
                Spacer()
                HStack {
                    Spacer()
                    NavigationLink(destination: CreateUserView(role: "patient")) {
                        HStack(spacing: 8) {
                            Image(systemName: "plus")
                                .font(.system(size: 18, weight: .bold))
                            Text("ADD PATIENT")
                                .font(.system(size: 15, weight: .bold))
                        }
                        .padding(.horizontal, 20)
                        .padding(.vertical, 14)
                        .background(adminBlue)
                        .foregroundColor(.white)
                        .cornerRadius(30)
                        .shadow(color: adminBlue.opacity(0.3), radius: 10, x: 0, y: 5)
                    }
                    .padding(25)
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
        DoctorService.shared.getAllPatients { result in
            DispatchQueue.main.async {
                self.isLoading = false
                switch result {
                case .success(let response):
                    if response.success {
                        self.patients = (response.data ?? []).filter { $0.role?.uppercased() == "PATIENT" }
                    } else {
                        self.errorMessage = response.message ?? "Failed to load patients"
                    }
                case .failure(let error):
                    self.errorMessage = error.localizedDescription
                }
            }
        }
    }
}

struct PatientListCard: View {
    let patient: User
    
    var body: some View {
        HStack(spacing: 15) {
            VStack(alignment: .leading, spacing: 6) {
                Text(patient.name ?? "Unknown Patient")
                    .font(.system(size: 18, weight: .bold))
                    .foregroundColor(.black)
                
                Text("Age: \(patient.age.map { String($0) } ?? "N/A") | \(patient.email ?? "No email")")
                    .font(.system(size: 14))
                    .foregroundColor(.secondary)
            }
            
            Spacer()
            
            Image(systemName: "person.fill")
                .font(.system(size: 24))
                .foregroundColor(.black.opacity(0.7))
                .frame(width: 45, height: 45)
                .background(Color.gray.opacity(0.1))
                .clipShape(Circle())
        }
        .padding(20)
        .background(Color.white)
        .cornerRadius(18)
        .shadow(color: Color.black.opacity(0.05), radius: 10, x: 0, y: 5)
        .overlay(
            RoundedRectangle(cornerRadius: 18)
                .stroke(Color.gray.opacity(0.1), lineWidth: 1)
        )
    }
}

#Preview {
    NavigationView {
        AllPatientsView()
    }
}
