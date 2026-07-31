import SwiftUI

struct UserDetailsView: View {
    let user: User
    
    var body: some View {
        ZStack {
            Color.white.ignoresSafeArea()
            
            ScrollView {
                VStack(spacing: 25) {
                    // Profile Header
                    VStack(spacing: 15) {
                        ZStack {
                            Circle()
                                .fill(LinearGradient(gradient: Gradient(colors: [Color.blue, Color.purple]), startPoint: .topLeading, endPoint: .bottomTrailing))
                                .frame(width: 100, height: 100)
                            
                            Text(user.name?.prefix(1).uppercased() ?? "?")
                                .font(.system(size: 40, weight: .bold))
                                .foregroundColor(.white)
                        }
                        
                        VStack(spacing: 5) {
                            Text(user.name ?? "Unknown")
                                .font(.title)
                                .bold()
                                .foregroundColor(.primary)
                            Text(user.role?.capitalized ?? "")
                                .font(.headline)
                                .foregroundColor(.blue)
                        }
                    }
                    .padding(.top)
                    
                    // Info Grid
                    VStack(spacing: 1) {
                        InfoRow(label: "Email", value: user.email ?? "", icon: "envelope.fill")
                        InfoRow(label: "Phone", value: user.phone ?? "Not provided", icon: "phone.fill")
                        InfoRow(label: "Age", value: user.age.map { String($0) } ?? "N/A", icon: "calendar")
                        if let specialization = user.specialization {
                            InfoRow(label: "Specialization", value: specialization, icon: "cross.case.fill")
                        }
                        InfoRow(label: "Status", value: "Active", icon: "person.badge.shield.checkmark.fill")
                    }
                    .background(Color.gray.opacity(0.05))
                    .cornerRadius(15)
                    .padding(.horizontal)
                    
                    // Actions
                    HStack(spacing: 20) {
                        Button(action: {}) {
                            Label("Edit Profile", systemImage: "pencil")
                                .frame(maxWidth: .infinity)
                                .padding()
                                .background(Color.blue)
                                .foregroundColor(.white)
                                .cornerRadius(12)
                        }
                        
                        Button(action: {}) {
                            Label("Reset Password", systemImage: "key.fill")
                                .frame(maxWidth: .infinity)
                                .padding()
                                .background(Color.orange)
                                .foregroundColor(.white)
                                .cornerRadius(12)
                        }
                    }
                    .padding(.horizontal)
                    
                    if user.role?.lowercased() == "patient" {
                        // Patient Specific Content
                        VStack(alignment: .leading, spacing: 15) {
                            Text("Recent Medical Activity")
                                .font(.headline)
                                .foregroundColor(.primary)
                            
                            ActivityItem(title: "Last Checkup", date: "Mar 08, 2026", icon: "stethoscope")
                            ActivityItem(title: "Latest Report", date: "Mar 01, 2026", icon: "doc.text")
                        }
                        .padding()
                        .background(Color.gray.opacity(0.05))
                        .cornerRadius(15)
                        .padding(.horizontal)
                    }
                }
                .padding(.bottom, 30)
            }
        }
        .navigationTitle("Profile Details")
        .navigationBarTitleDisplayMode(.inline)
    }
}

struct InfoRow: View {
    let label: String
    let value: String
    let icon: String
    
    var body: some View {
        HStack {
            Image(systemName: icon)
                .foregroundColor(.blue)
                .frame(width: 30)
            VStack(alignment: .leading) {
                Text(label)
                    .font(.caption)
                    .foregroundColor(.secondary)
                Text(value)
                    .font(.body)
                    .foregroundColor(.primary)
            }
            Spacer()
        }
        .padding()
    }
}

struct ActivityItem: View {
    let title: String
    let date: String
    let icon: String
    
    var body: some View {
        HStack {
            Image(systemName: icon)
                .foregroundColor(.blue)
            Text(title)
                .foregroundColor(.primary)
            Spacer()
            Text(date)
                .font(.caption)
                .foregroundColor(.secondary)
        }
        .padding()
        .background(Color.gray.opacity(0.03))
        .cornerRadius(10)
    }
}

#Preview {
    NavigationView {
        UserDetailsView(user: User(
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
