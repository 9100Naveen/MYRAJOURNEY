import SwiftUI

struct ExerciseHistoryView: View {
    @State private var history: [ExerciseHistoryRecord] = []
    @State private var isLoading = true

    var body: some View {
        ZStack {
            Color.white.ignoresSafeArea()

            if isLoading {
                ProgressView().tint(.blue)
            } else if history.isEmpty {
                VStack(spacing: 20) {
                    Image(systemName: "figure.walk.circle")
                        .font(.system(size: 80))
                        .foregroundColor(.secondary)
                    Text("No Exercise History")
                        .font(.headline)
                    Text("Complete assigned exercises to see your progress here.")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal)
                }
            } else {
                ScrollView {
                    LazyVStack(spacing: 15) {
                        ForEach(history) { record in
                            ExerciseHistoryRow(record: record)
                        }
                    }
                    .padding()
                }
            }
        }
        .navigationTitle("Exercise History")
        .onAppear(perform: loadHistory)
        .refreshable { loadHistory() }
    }

    private func loadHistory() {
        isLoading = true
        PatientService.shared.getExerciseHistory { result in
            DispatchQueue.main.async {
                self.isLoading = false
                if case .success(let response) = result, let data = response.data {
                    self.history = data
                }
            }
        }
    }
}

struct ExerciseHistoryRow: View {
    let record: ExerciseHistoryRecord

    var body: some View {
        HStack {
            VStack(alignment: .leading, spacing: 5) {
                Text(record.exerciseName)
                    .font(.headline)
                    .foregroundColor(.primary)
                Text("\(record.completedAt)\(record.duration.map { " • \($0)" } ?? "")")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }

            Spacer()

            Text(record.status)
                .font(.caption)
                .fontWeight(.bold)
                .padding(.vertical, 4)
                .padding(.horizontal, 10)
                .background(record.status.lowercased() == "completed" ? Color.green.opacity(0.1) : Color.orange.opacity(0.1))
                .foregroundColor(record.status.lowercased() == "completed" ? Color.green : Color.orange)
                .cornerRadius(5)
        }
        .padding()
        .background(Color.white)
        .cornerRadius(12)
        .shadow(color: .black.opacity(0.05), radius: 5, x: 0, y: 2)
    }
}

#Preview {
    NavigationView {
        ExerciseHistoryView()
    }
}
