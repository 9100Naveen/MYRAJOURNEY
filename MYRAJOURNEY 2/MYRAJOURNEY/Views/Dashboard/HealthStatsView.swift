import SwiftUI

struct HealthStatsView: View {
    @State private var stats: HealthStats?
    @State private var isLoading = true

    var body: some View {
        ZStack {
            Color.white.ignoresSafeArea()

            ScrollView {
                VStack(alignment: .leading, spacing: 25) {
                    Text("Health Analytics")
                        .font(.system(size: 28, weight: .bold))
                        .foregroundColor(.primary)
                        .padding(.horizontal)

                    // Main Chart Placeholder
                    VStack {
                        HStack {
                            Text("Patient Symptom Trends")
                                .font(.headline)
                                .foregroundColor(.primary)
                            Spacer()
                            Text("Last 7 Days")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                        .padding([.horizontal, .top])

                        RoundedRectangle(cornerRadius: 15)
                            .fill(Color.blue.opacity(0.1))
                            .frame(height: 200)
                            .overlay(
                                Text("Symptom Data Visualization")
                                    .foregroundColor(.blue.opacity(0.8))
                            )
                            .padding()
                    }
                    .background(Color.gray.opacity(0.05))
                    .cornerRadius(20)
                    .padding(.horizontal)

                    if isLoading {
                        HStack {
                            Spacer()
                            ProgressView().tint(.blue)
                            Spacer()
                        }
                    } else {
                        // Grid of live stats from backend
                        LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 15) {
                            StatMetricCard(
                                title: "Active Patients",
                                value: "\(stats?.activePatients ?? 0)",
                                trend: "",
                                color: .blue
                            )
                            StatMetricCard(
                                title: "Avg Symptom Score",
                                value: String(format: "%.1f", stats?.avgSymptomScore ?? 0.0),
                                trend: "",
                                color: .green
                            )
                            StatMetricCard(
                                title: "Pending Reports",
                                value: "\(stats?.pendingReports ?? 0)",
                                trend: "",
                                color: .orange
                            )
                            StatMetricCard(
                                title: "Rehab Compliance",
                                value: String(format: "%.0f%%", stats?.rehabCompliance ?? 0.0),
                                trend: "",
                                color: .purple
                            )
                        }
                        .padding(.horizontal)
                    }
                }
                .padding(.vertical)
            }
        }
        .navigationTitle("Stats")
        .navigationBarTitleDisplayMode(.inline)
        .onAppear(perform: loadStats)
        .refreshable { loadStats() }
    }

    private func loadStats() {
        isLoading = true
        DoctorService.shared.getHealthStats { result in
            DispatchQueue.main.async {
                self.isLoading = false
                if case .success(let response) = result {
                    self.stats = response.data
                }
            }
        }
    }
}

struct StatMetricCard: View {
    let title: String
    let value: String
    let trend: String
    let color: Color

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text(title)
                .font(.caption)
                .foregroundColor(.secondary)

            HStack(alignment: .firstTextBaseline) {
                Text(value)
                    .font(.system(size: 24, weight: .bold))
                    .foregroundColor(.primary)

                Spacer()

                if !trend.isEmpty {
                    Text(trend)
                        .font(.caption2)
                        .fontWeight(.bold)
                        .foregroundColor(color)
                        .padding(4)
                        .background(color.opacity(0.1))
                        .cornerRadius(5)
                }
            }
        }
        .padding()
        .background(Color.white)
        .cornerRadius(15)
        .shadow(color: Color.black.opacity(0.05), radius: 10, x: 0, y: 5)
        .overlay(
            RoundedRectangle(cornerRadius: 15)
                .stroke(Color.gray.opacity(0.1), lineWidth: 1)
        )
    }
}

#Preview {
    NavigationView {
        HealthStatsView()
    }
}
