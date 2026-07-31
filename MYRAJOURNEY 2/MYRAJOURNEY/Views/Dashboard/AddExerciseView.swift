import SwiftUI

struct AddExerciseView: View {
    @Environment(\.dismiss) var dismiss
    @State private var name = ""
    @State private var description = ""
    @State private var category = "WRIST"
    @State private var difficulty = "BEGINNER"
    @State private var videoUrl = ""
    @State private var isSaving = false
    @State private var errorMessage: String?
    
    let categories = ["WRIST", "THUMB", "FINGER", "KNEE", "HIP"]
    let difficulties = ["BEGINNER", "INTERMEDIATE", "ADVANCED"]
    
    var body: some View {
        Form {
            Section(header: Text("Exercise Details")) {
                TextField("Exercise Name", text: $name)
                TextField("Description", text: $description)
                
                Picker("Category", selection: $category) {
                    ForEach(categories, id: \.self) { cat in
                        Text(cat).tag(cat)
                    }
                }
                
                Picker("Difficulty", selection: $difficulty) {
                    ForEach(difficulties, id: \.self) { diff in
                        Text(diff).tag(diff)
                    }
                }
            }
            
            Section(header: Text("Media")) {
                TextField("Video URL (Optional)", text: $videoUrl)
            }
            
            if let error = errorMessage {
                Section {
                    Text(error).foregroundColor(.red)
                }
            }
            
            Section {
                Button(action: saveExercise) {
                    if isSaving {
                        ProgressView().tint(.blue)
                    } else {
                        Text("Add Exercise")
                            .frame(maxWidth: .infinity)
                            .bold()
                    }
                }
                .disabled(name.isEmpty || isSaving)
            }
        }
        .navigationTitle("New Exercise")
    }
    
    private func saveExercise() {
        isSaving = true
        errorMessage = nil
        
        let newExercise = Exercise(
            id: nil,
            name: name,
            description: description,
            category: category,
            targetJoints: [],
            difficultyLevel: difficulty,
            videoUrl: videoUrl,
            animationUrl: nil,
            instructions: [],
            raBenefits: [],
            createdAt: nil
        )
        
        DoctorService.shared.createExercise(exercise: newExercise) { result in
            DispatchQueue.main.async {
                self.isSaving = false
                switch result {
                case .success:
                    dismiss()
                case .failure(let error):
                    self.errorMessage = "Failed to add exercise: \(error.localizedDescription)"
                }
            }
        }
    }
}
