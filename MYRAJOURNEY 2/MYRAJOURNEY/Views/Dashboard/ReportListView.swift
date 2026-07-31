import SwiftUI
import Combine

struct ReportListView: View {
    @State private var reports: [Report] = []
    @State private var isLoading = true
    @State private var showingAddReport = false
    let isDoctor: Bool
    var patientId: String? = nil
    var patientName: String? = nil
    
    var body: some View {
        ZStack {
            ScrollView(showsIndicators: false) {
                VStack(alignment: .leading, spacing: 24) {
                    AppGradientHeader(
                        title: "Reports",
                        subtitle: patientName ?? (isDoctor ? "Doctor Center" : "Medical Records"),
                        showMenuButton: false,
                        trailingAction: AnyView(
                            Button(action: { showingAddReport = true }) {
                                Image(systemName: "plus")
                                    .font(.system(size: 18, weight: .bold))
                                    .foregroundColor(.white)
                                    .padding(12)
                                    .background(Color.white.opacity(0.2))
                                    .clipShape(Circle())
                            }
                        )
                    )
                    
                    VStack(spacing: 16) {
                        if isLoading {
                            ProgressView()
                                .padding(.vertical, 100)
                        } else if reports.isEmpty {
                            VStack(spacing: 20) {
                                Image(systemName: "doc.text.magnifyingglass")
                                    .font(.system(size: 60))
                                    .foregroundColor(.secondary.opacity(0.5))
                                Text("No medical reports found")
                                    .font(.system(size: 17, weight: .bold))
                                    .foregroundColor(.secondary)
                            }
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 100)
                        } else {
                            ForEach(reports) { report in
                                ReportRow(report: report)
                            }
                        }
                    }
                    .padding(.horizontal, 24)
                }
            }
            .background(Color.appBackground)
            .ignoresSafeArea(edges: .top)
        }
        .navigationBarHidden(true)
        .sheet(isPresented: $showingAddReport) {
            AddReportView(patientId: patientId) {
                loadReports()
            }
        }
        .onAppear(perform: loadReports)
    }
    
    private func loadReports() {
        isLoading = true
        if isDoctor {
            DoctorService.shared.getReports { result in
                handleResponse(result)
            }
        } else {
            PatientService.shared.getReports { result in
                handleResponse(result)
            }
        }
    }
    
    private func handleResponse(_ result: Result<ApiResponse<[Report]>, NetworkError>) {
        DispatchQueue.main.async {
            isLoading = false
            if case .success(let response) = result, let data = response.data {
                if let pid = patientId {
                    self.reports = data.filter { $0.patientId == pid }
                } else if !isDoctor {
                    let userId = SessionManager.shared.userId ?? ""
                    self.reports = data.filter { $0.patientId == userId }
                } else {
                    self.reports = data
                }
            }
        }
    }
}

struct ReportRow: View {
    let report: Report
    
    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            HStack {
                ZStack {
                    Circle()
                        .fill(Design.Colors.primary.opacity(0.12))
                        .frame(width: 44, height: 44)
                    Image(systemName: "doc.text.fill")
                        .foregroundColor(.appAccent)
                        .font(.system(size: 18))
                }
                
                VStack(alignment: .leading, spacing: 4) {
                    Text(report.title)
                        .font(.system(size: 17, weight: .bold, design: .rounded))
                    Text(report.displayDate)
                        .font(.system(size: 13))
                        .foregroundColor(.secondary)
                }
                
                Spacer()
                
                StatusBadge(status: report.status)
            }
            
            HStack {
                Label("ID: \(report.patientId ?? "N/A")", systemImage: "person.text.rectangle")
                    .font(.system(size: 12, weight: .medium))
                    .foregroundColor(.secondary)
                
                Spacer()
                
                Text("View Details")
                    .font(.system(size: 12, weight: .bold))
                    .foregroundColor(.appAccent)
                Image(systemName: "chevron.right")
                    .font(.system(size: 10, weight: .bold))
                    .foregroundColor(.appAccent)
            }
        }
        .padding(20)
        .modernCard(radius: 24)
    }
}

struct StatusBadge: View {
    let status: String
    
    var body: some View {
        Text(status.uppercased())
            .font(.system(size: 10, weight: .black))
            .padding(.vertical, 6)
            .padding(.horizontal, 10)
            .background(statusColor.opacity(0.12))
            .foregroundColor(statusColor)
            .cornerRadius(12)
    }
    
    private var statusColor: Color {
        switch status.uppercased() {
        case "COMPLETED", "APPROVED": return .green
        case "PENDING": return .orange
        case "UPLOADED": return .blue
        default: return .gray
        }
    }
}

#Preview("Patient") {
    NavigationView {
        ReportListView(isDoctor: false)
    }
}

#Preview("Doctor") {
    NavigationView {
        ReportListView(isDoctor: true)
    }
}
