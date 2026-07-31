import SwiftUI

struct RehabExerciseModel: Codable, Identifiable {
    let id: String
    let name: String
    let description: String
    let reps: String
    let frequency: String
    let videoUrl: String
    let thumbnailUrl: String
}

struct AllRehabView: View {
    @State private var rehabList: [Exercise] = []
    @State private var isLoading = true
    @State private var errorMessage: String?
    
    var body: some View {
        ZStack {
            Color.white.ignoresSafeArea()
            
            if isLoading {
                ProgressView().tint(.blue)
            } else if let error = errorMessage {
                VStack(spacing: 20) {
                    Image(systemName: "exclamationmark.triangle")
                        .font(.largeTitle)
                        .foregroundColor(.red)
                    Text(error)
                        .foregroundColor(.secondary)
                    Button("Retry") { loadRehabData() }
                }
            } else {
                ScrollView {
                    LazyVStack(spacing: 20) {
                        ForEach(rehabList) { exercise in
                            RehabExerciseCard(exercise: exercise)
                        }
                    }
                    .padding()
                }
            }
        }
        .navigationTitle("Exercise Library")
        .toolbar {
            if AppState.shared.currentUser?.role?.uppercased() != "PATIENT" {
                ToolbarItem(placement: .navigationBarTrailing) {
                    NavigationLink(destination: AddExerciseView()) {
                        Image(systemName: "plus")
                    }
                }
            }
        }
        .onAppear(perform: loadRehabData)
    }
    
    private func loadRehabData() {
        isLoading = true
        DoctorService.shared.getExercises { result in
            DispatchQueue.main.async {
                self.isLoading = false
                switch result {
                case .success(let response):
                    self.rehabList = response.data ?? []
                case .failure(let error):
                    self.errorMessage = "Failed to load exercises: \(error.localizedDescription)"
                }
            }
        }
    }
}

struct RehabExerciseCard: View {
    let exercise: Exercise
    
    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            // Header Image Placeholder
            ZStack {
                Rectangle()
                    .fill(LinearGradient(gradient: Gradient(colors: [Color.blue.opacity(0.3), Color.purple.opacity(0.3)]), startPoint: .topLeading, endPoint: .bottomTrailing))
                    .frame(height: 180)
                
                Image(systemName: "play.circle.fill")
                    .font(.system(size: 50))
                    .foregroundColor(.white)
                    .shadow(radius: 5)
            }
            
            VStack(alignment: .leading, spacing: 12) {
                HStack {
                    Text(exercise.name)
                        .font(.title2)
                        .bold()
                        .foregroundColor(.primary)
                    Spacer()
                    Text(exercise.category)
                        .font(.caption)
                        .fontWeight(.bold)
                        .padding(.vertical, 4)
                        .padding(.horizontal, 10)
                        .background(Color.blue.opacity(0.2))
                        .foregroundColor(.blue)
                        .cornerRadius(5)
                }
                
                Text(exercise.description ?? "")
                    .font(.body)
                    .foregroundColor(.secondary)
                
                HStack {
                    Label(exercise.difficultyLevel ?? "Beginner", systemImage: "chart.bar.fill")
                        .foregroundColor(.secondary)
                    Spacer()
                    Button(action: {
                        if let videoUrl = exercise.videoUrl, let url = URL(string: videoUrl) {
                            UIApplication.shared.open(url)
                        }
                    }) {
                        HStack(spacing: 4) {
                            Image(systemName: "play.circle.fill")
                            Text("Watch Tutorial")
                        }
                        .font(.subheadline)
                        .bold()
                        .foregroundColor(.blue)
                    }
                }
                .padding(.top, 10)
            }
            .padding()
        }
        .background(Color.white)
        .cornerRadius(20)
        .shadow(color: Color.black.opacity(0.1), radius: 10, x: 0, y: 5)
        .overlay(
            RoundedRectangle(cornerRadius: 20)
                .stroke(Color.gray.opacity(0.1), lineWidth: 1)
        )
    }
}

#Preview {
    NavigationView {
        AllRehabView()
    }
}
