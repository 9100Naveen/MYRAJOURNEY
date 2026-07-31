import SwiftUI
import Combine

struct PatientListView: View {
    @State private var patients: [Patient] = []
    @State private var isLoading = true
    @State private var searchText = ""
    @State private var errorMessage: String?
    
    // Selection mode properties
    var isSelectionMode: Bool = false
    var selectionAction: ((Patient) -> Void)? = nil
    
    @Environment(\.dismiss) private var dismiss
    
    var filteredPatients: [Patient] {
        if searchText.isEmpty {
            return patients
        } else {
            return patients.filter { 
                $0.name.lowercased().contains(searchText.lowercased()) || 
                $0.email.lowercased().contains(searchText.lowercased()) 
            }
        }
    }
    
    @State private var showAddPatient = false
    @State private var showToast = false
    @State private var toastMessage = ""
    
    var body: some View {
        ZStack {
            Color(red: 0.98, green: 0.98, blue: 0.99).ignoresSafeArea()
            
            VStack(spacing: 0) {
                AppGradientHeader(
                    title: isSelectionMode ? "Select Patient" : "All Patients",
                    subtitle: "Clinical Center",
                    showMenuButton: false,
                    trailingAction: AnyView(
                        Button(action: loadPatients) {
                            Image(systemName: "arrow.clockwise")
                                .font(.system(size: 18, weight: .bold))
                                .foregroundColor(.white)
                                .padding(10)
                                .background(Color.white.opacity(0.2))
                                .clipShape(Circle())
                        }
                    )
                )
                
                // Search Bar
                HStack {
                    Image(systemName: "magnifyingglass")
                        .foregroundColor(.gray)
                    TextField("Search by name or email...", text: $searchText)
                        .autocapitalization(.none)
                }
                .padding()
                .background(Color.white)
                .cornerRadius(12)
                .shadow(color: .black.opacity(0.05), radius: 5, x: 0, y: 2)
                .padding(.horizontal)
                .padding(.top, 16)
                
                if isLoading && patients.isEmpty {
                    Spacer()
                    ProgressView()
                        .tint(Color(red: 0.11, green: 0.58, blue: 0.95))
                    Spacer()
                } else if let error = errorMessage {
                    Spacer()
                    VStack(spacing: 12) {
                        Image(systemName: "exclamationmark.triangle.fill")
                            .font(.system(size: 40))
                            .foregroundColor(.red)
                        Text(error)
                            .multilineTextAlignment(.center)
                            .foregroundColor(.secondary)
                        Button("Try Again") { loadPatients() }
                            .padding(.top, 8)
                            .foregroundColor(.blue)
                    }
                    Spacer()
                } else {
                    ScrollView {
                        LazyVStack(spacing: 12) {
                            ForEach(filteredPatients) { patient in
                                if isSelectionMode {
                                    Button(action: {
                                        selectionAction?(patient)
                                    }) {
                                        PatientRecordRow(patient: patient)
                                    }
                                    .buttonStyle(PlainButtonStyle())
                                } else {
                                    NavigationLink(destination: PatientDetailsView(patient: patient)) {
                                        PatientRecordRow(patient: patient)
                                    }
                                }
                            }
                        }
                        .padding(.horizontal)
                        .padding(.top, 16)
                        .padding(.bottom, 100)
                    }
                    .refreshable {
                        loadPatients()
                    }
                }
            }
            
            // Floating Action Button
            if !isSelectionMode {
                VStack {
                    Spacer()
                    HStack {
                        Spacer()
                        Button(action: { showAddPatient = true }) {
                            HStack(spacing: 8) {
                                Image(systemName: "plus.circle.fill")
                                    .font(.system(size: 20))
                                Text("ADD PATIENT")
                                    .font(.system(size: 15, weight: .bold))
                            }
                            .padding(.horizontal, 20)
                            .padding(.vertical, 14)
                            .background(Color(red: 0.11, green: 0.58, blue: 0.95))
                            .foregroundColor(.white)
                            .cornerRadius(30)
                            .shadow(color: Color.blue.opacity(0.3), radius: 10, x: 0, y: 5)
                        }
                        .padding(25)
                    }
                }
            }
        }
        .navigationBarHidden(true)
        .onAppear(perform: loadPatients)
        .sheet(isPresented: $showAddPatient) {
            NavigationView {
                CreateUserView(role: "patient")
            }
            .onDisappear {
                loadPatients()
            }
        }
    }
    
    private func loadPatients() {
        isLoading = true
        errorMessage = nil
        print("🚀 Fetching all patient records from doctor/patients...")
        
        DoctorService.shared.getPatients { result in
            DispatchQueue.main.async {
                self.isLoading = false
                switch result {
                case .success(let response):
                    if let data = response.data {
                        print("✅ Successfully loaded \(data.count) patients")
                        self.patients = data.sorted(by: { $0.id > $1.id })
                    } else {
                        print("⚠️ Backend returned success but no patient data")
                        self.errorMessage = "No patient data found."
                    }
                case .failure(let error):
                    print("❌ Failed to load patients: \(error)")
                    let msg: String
                    switch error {
                    case .unauthorized:
                        msg = "Session expired. Please log in again."
                    case .serverError(let srvMsg):
                        msg = "Server error: \(srvMsg)"
                    case .decodingError:
                        msg = "Data format error. Please contact support."
                    default:
                        msg = "Connection failed. Please check your internet."
                    }
                    self.errorMessage = msg
                    self.toastMessage = msg
                    withAnimation {
                        self.showToast = true
                    }
                }
            }
        }
    }
}

#Preview {
    NavigationView {
        PatientListView()
    }
}
