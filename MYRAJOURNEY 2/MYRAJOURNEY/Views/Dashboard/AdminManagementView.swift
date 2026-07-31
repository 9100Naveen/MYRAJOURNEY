import SwiftUI

struct AdminManagementView: View {
    @State private var users: [User] = []
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
                    
                    Text("User Management")
                        .font(.system(size: 20, weight: .bold))
                        .foregroundColor(.white)
                    
                    Spacer()
                    
                    Button(action: loadData) {
                        Image(systemName: "arrow.clockwise")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(.white)
                    }
                }
                .padding(.horizontal, 20)
                .padding(.top, 12)
                .padding(.bottom, 15)
                .background(adminBlue)
                .shadow(color: Color.black.opacity(0.1), radius: 5, x: 0, y: 3)
                
                ScrollView {
                    VStack(spacing: 16) {
                        // WARNING BANNER
                        HStack(alignment: .top, spacing: 15) {
                            Image(systemName: "exclamationmark.triangle.fill")
                                .font(.system(size: 24))
                                .foregroundColor(.orange)
                            
                            VStack(alignment: .leading, spacing: 5) {
                                Text("User Management")
                                    .font(.system(size: 18, weight: .bold))
                                
                                Text("Delete users with caution. This action will permanently remove all associated data including medical records, appointments, reports, and medications.")
                                    .font(.system(size: 14))
                                    .foregroundColor(.secondary)
                                    .lineLimit(5)
                            }
                        }
                        .padding()
                        .background(Color.white)
                        .cornerRadius(12)
                        .shadow(color: Color.black.opacity(0.05), radius: 10, x: 0, y: 5)
                        .padding(.horizontal)
                        .padding(.top, 20)
                        
                        if isLoading {
                            ProgressView()
                                .padding(.top, 50)
                        } else if let error = errorMessage {
                            VStack(spacing: 12) {
                                Image(systemName: "xmark.circle.fill")
                                    .font(.system(size: 40))
                                    .foregroundColor(.red)
                                Text(error)
                                    .foregroundColor(.secondary)
                                Button("Retry") { loadData() }
                                    .foregroundColor(.blue)
                            }
                            .padding(.top, 50)
                        } else {
                            LazyVStack(spacing: 16) {
                                ForEach(users) { user in
                                    UserManagementCard(user: user, onRefresh: loadData)
                                }
                            }
                            .padding(.horizontal)
                        }
                        
                        Spacer(minLength: 30)
                    }
                }
                .refreshable {
                    loadData()
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
                    if response.success {
                        self.users = response.data ?? []
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

struct UserManagementCard: View {
    let user: User
    var onRefresh: () -> Void
    
    @State private var showingDeleteAlert = false
    @State private var isDeleting = false
    
    var body: some View {
        HStack(spacing: 15) {
            // User Avatar
            ZStack {
                Circle()
                    .fill(Color.gray.opacity(0.1))
                    .frame(width: 60, height: 60)
                
                Image(systemName: "person.fill")
                    .font(.system(size: 24))
                    .foregroundColor(.blue.opacity(0.7))
            }
            
            VStack(alignment: .leading, spacing: 4) {
                Text(user.name ?? "Unknown User")
                    .font(.system(size: 18, weight: .bold))
                
                Text(user.email ?? "No email")
                    .font(.system(size: 14))
                    .foregroundColor(.secondary)
                
                HStack(spacing: 8) {
                    HStack(spacing: 4) {
                         Image(systemName: "checkmark.circle.fill")
                             .font(.system(size: 12))
                         Text("Active")
                     }
                     .font(.system(size: 12, weight: .semibold))
                     
                    Text("Additional Info")
                        .font(.system(size: 12))
                        .foregroundColor(.blue)
                }
                .padding(.top, 2)
                
                Text("Registered: \(formattedDate)")
                    .font(.system(size: 12, weight: .medium))
                    .italic()
                    .foregroundColor(.secondary)
            }
            
            Spacer()
            
            // Buttons and Badge
            VStack(alignment: .trailing, spacing: 10) {
                // Role Badge
                Text(user.role?.uppercased() ?? "USER")
                    .font(.system(size: 10, weight: .bold))
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .background(user.role?.uppercased() == "DOCTOR" ? Color.blue : Color.cyan)
                    .foregroundColor(.white)
                    .cornerRadius(8)
                
                HStack(spacing: 12) {
                    NavigationLink(destination: EditUserView(user: user)) {
                        Image(systemName: "pencil")
                            .font(.system(size: 18))
                            .foregroundColor(.blue)
                    }
                    
                    Toggle("", isOn: .constant(true))
                        .labelsHidden()
                        .toggleStyle(SwitchToggleStyle(tint: .orange))
                        .scaleEffect(0.6)
                        .frame(width: 40)
                    
                    Button(action: { showingDeleteAlert = true }) {
                        if isDeleting {
                            ProgressView().scaleEffect(0.8)
                        } else {
                            Image(systemName: "trash.fill")
                                .font(.system(size: 18))
                                .foregroundColor(.red)
                        }
                    }
                }
            }
        }
        .padding(15)
        .background(Color.white)
        .cornerRadius(18)
        .shadow(color: Color.black.opacity(0.04), radius: 8, x: 0, y: 4)
        .overlay(
            RoundedRectangle(cornerRadius: 18)
                .stroke(Color.gray.opacity(0.1), lineWidth: 1)
        )
        .alert("Delete User", isPresented: $showingDeleteAlert) {
            Button("Cancel", role: .cancel) { }
            Button("Delete", role: .destructive) { deleteUser() }
        } message: {
            Text("Are you sure you want to permanently delete \(user.name ?? "this user")? This action cannot be undone.")
        }
    }
    
    private var formattedDate: String {
        // Mocking date for reference
        return "Jan 01, 2024"
    }
    
    private func deleteUser() {
        isDeleting = true
        let endpoint = Endpoint(path: "admin/users/\(user.id)", method: .delete)
        ApiClient.shared.request(endpoint) { (result: Result<ApiResponse<String>, NetworkError>) in
            DispatchQueue.main.async {
                isDeleting = false
                onRefresh()
            }
        }
    }
}

#Preview {
    NavigationView {
        AdminManagementView()
    }
}
