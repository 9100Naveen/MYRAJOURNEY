import SwiftUI
import Combine

struct PatientRehabilitationView: View {
    @State private var rehabPlans: [RehabPlan] = []
    @State private var assignments: [ExerciseAssignment] = []
    @State private var exerciseLookup: [Int: Exercise] = [:]
    @State private var allUnifiedExercises: [UnifiedExercise] = []
    @State private var isLoading = true
    @State private var showToast = false
    @State private var toastMessage = ""
    @ObservedObject private var appState = AppState.shared
    @Environment(\.dismiss) var dismiss
    struct VideoItem: Identifiable {
        let id = UUID()
        let name: String
        let url: URL
    }
    @State private var selectedVideoItem: VideoItem? = nil
    
    private let tealColor = Color(red: 0/255, green: 128/255, blue: 108/255)
    
    var body: some View {
        ZStack(alignment: .top) {
            Color.appBackground.ignoresSafeArea()
            
            VStack(spacing: 0) {
                // Header
                headerView
                
                ScrollView(showsIndicators: false) {
                    VStack(alignment: .leading, spacing: 20) {
                        progressSection
                            .padding(.top, 16)
                        
                        if isLoading {
                            HStack {
                                Spacer()
                                ProgressView().tint(tealColor)
                                Spacer()
                            }
                            .padding(.top, 60)
                            .transition(.opacity)
                        } else if allUnifiedExercises.isEmpty {
                            EmptyStateView(
                                image: "figure.walk.circle",
                                title: "No Plans Yet",
                                message: "Assigned exercises will appear here."
                            )
                            .transition(.scale.combined(with: .opacity))
                        } else {
                            VStack(spacing: 8) {
                                // Combined Exercise List
                                ForEach(Array(allUnifiedExercises.enumerated()), id: \.element.id) { index, exerciseData in
                                    RehabExercisePremiumCard(
                                        category: exerciseData.category,
                                        name: exerciseData.name,
                                        description: exerciseData.description,
                                        benefits: exerciseData.benefits,
                                        sets: exerciseData.sets,
                                        reps: exerciseData.reps,
                                        videoUrl: exerciseData.videoUrl,
                                        isCompleted: exerciseData.isCompleted,
                                        onComplete: {
                                            markAsCompleted(id: exerciseData.id)
                                        },
                                        onWatchVideo: {
                                            watchVideoAction(for: exerciseData)
                                        }
                                    )
                                    .transition(.move(edge: .bottom).combined(with: .opacity))
                                    .animation(.spring(response: 0.5, dampingFraction: 0.7).delay(Double(index) * 0.1), value: isLoading)
                                }
                            }
                            .padding(.horizontal, 16)
                        }
                        
                        Spacer(minLength: 0)
                    }
                }
            }
                        
            // Toast Notification
            if showToast {
                VStack {
                    Spacer()
                    toastView
                        .padding(.bottom, 100)
                }
                .transition(.move(edge: .bottom).combined(with: .opacity))
                .animation(.spring(), value: showToast)
            }
        }
        .navigationBarHidden(true)
        .fullScreenCover(item: $selectedVideoItem) { item in
            LocalVideoPlayerView(exerciseName: item.name, videoURL: item.url)
        }
        .onAppear {
            withAnimation(.easeOut) {
                loadData()
            }
        }
    }
    
    private var headerView: some View {
        HStack {
            Button(action: {
                hapticFeedback(.light)
                dismiss()
            }) {
                Image(systemName: "arrow.left")
                    .font(.system(size: 18, weight: .bold))
                    .foregroundColor(.primary)
            }
            
            Spacer()
            
            Text("Rehab Exercises")
                .font(.system(size: 20, weight: .bold))
                .foregroundColor(.primary)
            
            Spacer()
            
            Color.clear.frame(width: 24)
        }
        .padding(.horizontal, 20)
        .frame(height: 56)
        .padding(.top, safeAreaTop())
        .background(Color.appSurface)
        .shadow(color: Color.black.opacity(0.03), radius: 5, x: 0, y: 2)
    }

    private func safeAreaTop() -> CGFloat {
        guard let scene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
              let window = scene.windows.first else {
            return 20
        }
        return window.safeAreaInsets.top
    }
    
    private var progressSection: some View {
        let completedCount = allUnifiedExercises.filter { $0.isCompleted }.count
        let totalCount = allUnifiedExercises.count
        let progress = totalCount > 0 ? Double(completedCount) / Double(totalCount) : 0.0
        
        return VStack(alignment: .leading, spacing: 12) {
            Text("Overall Progress: \(Int(progress * 100))%")
                .font(.system(size: 16, weight: .bold))
                .foregroundColor(.primary)
            
            GeometryReader { geometry in
                ZStack(alignment: .leading) {
                    Capsule()
                        .fill(Color.black.opacity(0.05))
                        .frame(height: 12)
                    
                    Capsule()
                        .fill(tealColor)
                        .frame(width: geometry.size.width * CGFloat(progress), height: 12)
                        .animation(.spring(response: 0.6, dampingFraction: 0.8), value: progress)
                }
            }
            .frame(height: 12)
        }
        .padding(12)
        .background(Color.appCard)
        .cornerRadius(16)
        .shadow(color: Color.black.opacity(0.04), radius: 10, x: 0, y: 4)
        .padding(.horizontal, 16)
    }
    
    private var toastView: some View {
        HStack(spacing: 12) {
            Image(systemName: "checkmark.circle.fill")
                .foregroundColor(.green)
                .font(.system(size: 20))
            
            Text(toastMessage)
                .foregroundColor(.white)
                .font(.system(size: 15, weight: .medium))
            
            Spacer()
        }
        .padding(.vertical, 16)
        .padding(.horizontal, 20)
        .background(Color(red: 0.1, green: 0.1, blue: 0.1))
        .cornerRadius(12)
        .padding(.horizontal, 20)
        .shadow(radius: 10)
    }
    
    private func hapticFeedback(_ style: UIImpactFeedbackGenerator.FeedbackStyle) {
        let generator = UIImpactFeedbackGenerator(style: style)
        generator.impactOccurred()
    }
    
    // MARK: - Helper Data Structure
    struct UnifiedExercise: Identifiable {
        let id: UUID
        let serverId: Int?
        let category: String
        let name: String
        let description: String
        let benefits: [String]
        let sets: Int
        let reps: String
        let videoUrl: String?
        var isCompleted: Bool
        
        init(id: UUID = UUID(), serverId: Int? = nil, category: String, name: String, description: String, benefits: [String], sets: Int, reps: String, videoUrl: String?, isCompleted: Bool) {
            self.id = id
            self.serverId = serverId
            self.category = category
            self.name = name
            self.description = description
            self.benefits = benefits
            self.sets = sets
            self.reps = reps
            self.videoUrl = videoUrl
            self.isCompleted = isCompleted
        }
    }
    
    private func updateUnifiedExercises() {
        var list: [UnifiedExercise] = []
        
        // 1. Process Rehab Plans
        for plan in rehabPlans {
            if let exList = plan.exercises {
                for ex in exList {
                    list.append(UnifiedExercise(
                        id: UUID(),
                        serverId: ex.id,
                        category: "HAND",
                        name: ex.name,
                        description: ex.description ?? "Recover and improve flexibility.",
                        benefits: ["Improves flexibility"],
                        sets: ex.sets ?? 3,
                        reps: ex.reps ?? "10",
                        videoUrl: plan.videoUrl,
                        isCompleted: ex.completed
                    ))
                }
            } else if let exName = plan.exerciseName {
                list.append(UnifiedExercise(
                    id: UUID(),
                    serverId: nil,
                    category: "HAND",
                    name: exName,
                    description: plan.description ?? "Recover and improve flexibility.",
                    benefits: ["Improves flexibility"],
                    sets: plan.setsPerDay ?? 3,
                    reps: "\(plan.repsPerSet ?? 10)",
                    videoUrl: plan.videoUrl,
                    isCompleted: false
                ))
            }
        }
        
        // 2. Process Exercise Assignments (Directly from Doctor)
        for assignment in assignments {
            if let exList = assignment.exercises {
                for ex in exList {
                    // Avoid duplicates by name
                    if !list.contains(where: { $0.name == ex.name }) {
                        list.append(UnifiedExercise(
                            id: UUID(),
                            serverId: ex.id,
                            category: ex.category.uppercased(),
                            name: ex.name,
                            description: ex.description ?? "Doctor assigned exercise.",
                            benefits: ex.raBenefits ?? ["Improves joint health", "Strengthens muscles"],
                            sets: 3,
                            reps: "10",
                            videoUrl: ex.videoUrl,
                            isCompleted: false
                        ))
                    }
                }
            }
        }
        
        // 3. Fallback to Mock Data ONLY if absolutely no real data exists
        if list.isEmpty {
            list = getMockExercises()
        }
        
        self.allUnifiedExercises = list
    }
    
    private func getMockExercises() -> [UnifiedExercise] {
        return [
            UnifiedExercise(
                serverId: nil,
                category: "HAND",
                name: "Thumb Flexion",
                description: "Bend your thumb across your palm and hold",
                benefits: ["Improves thumb flexibility", "Reduces joint stiffness"],
                sets: 3,
                reps: "10",
                videoUrl: nil,
                isCompleted: false
            ),
            UnifiedExercise(
                serverId: nil,
                category: "HAND",
                name: "Thumb Opposition",
                description: "Touch your thumb to each finger tip in sequence",
                benefits: ["Improves dexterity", "Coordinates fine motor skills"],
                sets: 3,
                reps: "10",
                videoUrl: nil,
                isCompleted: false
            ),
            UnifiedExercise(
                serverId: nil,
                category: "HAND",
                name: "Wrist Rotation",
                description: "Rotate your wrist in circles, 10 times each direction",
                benefits: ["Improves range of motion", "Strengthens forearm muscles"],
                sets: 3,
                reps: "10",
                videoUrl: nil,
                isCompleted: false
            )
        ]
    }
    
    private let fallbackExercises: [String: (videoUrl: String, description: String, benefits: [String])] = [
        "finger extension/spreading": (
            videoUrl: "https://www.youtube.com/watch?v=lBuL9kAnkiU",
            description: "Finger spreading exercise to improve finger extension and reduce joint contractures.",
            benefits: ["Prevents finger contractures", "Improves finger extension", "Maintains hand span for gripping"]
        ),
        "finger extension": (
            videoUrl: "https://www.youtube.com/watch?v=lBuL9kAnkiU",
            description: "Finger spreading exercise to improve finger extension and reduce joint contractures.",
            benefits: ["Prevents finger contractures", "Improves finger extension", "Maintains hand span for gripping"]
        ),
        "wrist flexion": (
            videoUrl: "https://www.youtube.com/watch?v=QKAiNAhlXac",
            description: "Gentle wrist movement to improve flexibility and reduce stiffness in wrist joints.",
            benefits: ["Reduces wrist stiffness common in RA", "Improves range of motion", "Helps maintain joint function"]
        ),
        "wrist flexion/extension": (
            videoUrl: "https://www.youtube.com/watch?v=QKAiNAhlXac",
            description: "Gentle wrist movement to improve flexibility and reduce stiffness in wrist joints.",
            benefits: ["Reduces wrist stiffness common in RA", "Improves range of motion", "Helps maintain joint function"]
        ),
        "wrist rotation": (
            videoUrl: "https://www.youtube.com/watch?v=sD5rYQZRos8",
            description: "Circular wrist movements to maintain joint mobility and reduce morning stiffness.",
            benefits: ["Maintains wrist joint mobility", "Reduces morning stiffness", "Improves circulation in wrist area"]
        ),
        "wrist rotation (clockwise/counterclockwise)": (
            videoUrl: "https://www.youtube.com/watch?v=sD5rYQZRos8",
            description: "Circular wrist movements to maintain joint mobility and reduce morning stiffness.",
            benefits: ["Maintains wrist joint mobility", "Reduces morning stiffness", "Improves circulation in wrist area"]
        ),
        "thumb opposition": (
            videoUrl: "https://www.youtube.com/watch?v=uSgBNyhXvFs",
            description: "Thumb-to-finger touching exercise to maintain thumb mobility and grip strength.",
            benefits: ["Maintains thumb joint flexibility", "Improves grip strength", "Helps with daily activities like writing"]
        ),
        "thumb opposition exercise": (
            videoUrl: "https://www.youtube.com/watch?v=uSgBNyhXvFs",
            description: "Thumb-to-finger touching exercise to maintain thumb mobility and grip strength.",
            benefits: ["Maintains thumb joint flexibility", "Improves grip strength", "Helps with daily activities like writing"]
        ),
        "thumb flexion": (
            videoUrl: "https://www.youtube.com/watch?v=0ceVKwRSo8k",
            description: "Thumb bending exercise to improve thumb joint range of motion.",
            benefits: ["Improves thumb joint mobility", "Reduces thumb stiffness", "Helps maintain pinch strength"]
        ),
        "thumb flexion/extension": (
            videoUrl: "https://www.youtube.com/watch?v=0ceVKwRSo8k",
            description: "Thumb bending exercise to improve thumb joint range of motion.",
            benefits: ["Improves thumb joint mobility", "Reduces thumb stiffness", "Helps maintain pinch strength"]
        ),
        "finger flexion": (
            videoUrl: "https://www.youtube.com/watch?v=jJ6LBu6ATgU",
            description: "Gentle fist-making exercise to maintain finger joint flexibility.",
            benefits: ["Maintains finger joint flexibility", "Improves grip strength gradually", "Reduces finger stiffness"]
        ),
        "finger flexion (making a fist)": (
            videoUrl: "https://www.youtube.com/watch?v=jJ6LBu6ATgU",
            description: "Gentle fist-making exercise to maintain finger joint flexibility.",
            benefits: ["Maintains finger joint flexibility", "Improves grip strength gradually", "Reduces finger stiffness"]
        ),
        "finger pinch": (
            videoUrl: "https://www.youtube.com/watch?v=MclzYk3IVos",
            description: "Gentle pinching exercise using therapy putty or soft objects to maintain pinch strength.",
            benefits: ["Maintains pinch strength for daily tasks", "Improves fine motor control", "Helps with buttoning and writing"]
        ),
        "finger pinch strengthening": (
            videoUrl: "https://www.youtube.com/watch?v=MclzYk3IVos",
            description: "Gentle pinching exercise using therapy putty or soft objects to maintain pinch strength.",
            benefits: ["Maintains pinch strength for daily tasks", "Improves fine motor control", "Helps with buttoning and writing"]
        ),
        "knee flexion": (
            videoUrl: "https://www.youtube.com/watch?v=SH4P0PbyWxY",
            description: "Seated knee straightening exercise to maintain knee joint mobility and quadriceps strength.",
            benefits: ["Maintains knee joint mobility", "Strengthens quadriceps muscles", "Reduces knee stiffness"]
        ),
        "knee flexion/extension (seated)": (
            videoUrl: "https://www.youtube.com/watch?v=SH4P0PbyWxY",
            description: "Seated knee straightening exercise to maintain knee joint mobility and quadriceps strength.",
            benefits: ["Maintains knee joint mobility", "Strengthens quadriceps muscles", "Reduces knee stiffness"]
        ),
        "hip flexion": (
            videoUrl: "https://www.youtube.com/watch?v=rjlBMD4VYaA",
            description: "Hip lifting exercise to maintain hip joint flexibility and hip flexor strength.",
            benefits: ["Maintains hip joint mobility", "Improves walking ability", "Reduces hip stiffness"]
        ),
        "hip flexion (seated/standing)": (
            videoUrl: "https://www.youtube.com/watch?v=rjlBMD4VYaA",
            description: "Hip lifting exercise to maintain hip joint flexibility and hip flexor strength.",
            benefits: ["Maintains hip joint mobility", "Improves walking ability", "Reduces hip stiffness"]
        ),
        "hip abduction": (
            videoUrl: "https://www.youtube.com/watch?v=a9Tq7pWjPLs",
            description: "Side leg lifting exercise to strengthen hip abductor muscles and improve stability.",
            benefits: ["Strengthens hip stabilizer muscles", "Improves balance and stability", "Reduces hip pain during walking"]
        ),
        "hip abduction (side-lying/standing)": (
            videoUrl: "https://www.youtube.com/watch?v=a9Tq7pWjPLs",
            description: "Side leg lifting exercise to strengthen hip abductor muscles and improve stability.",
            benefits: ["Strengthens hip stabilizer muscles", "Improves balance and stability", "Reduces hip pain during walking"]
        )
    ]
    
    private func getBackendVideoAndDetails(forName name: String, fromBackendExercises dbExercises: [Exercise]) -> (videoUrl: String?, description: String, benefits: [String]) {
        let cleanName = name.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
        
        // 1. Search in fetched exercises from backend
        for dbEx in dbExercises {
            let dbCleanName = dbEx.name.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
            if dbCleanName == cleanName || dbCleanName.contains(cleanName) || cleanName.contains(dbCleanName) {
                return (dbEx.videoUrl, dbEx.description ?? "Recover and improve flexibility.", dbEx.raBenefits ?? ["Improves flexibility", "Reduces joint stiffness"])
            }
        }
        
        // 2. Fall back to static premium metadata lookup mapped to correct seed video URLs
        if let fallback = fallbackExercises[cleanName] {
            return (fallback.videoUrl, fallback.description, fallback.benefits)
        }
        
        // 3. Substring match fallback
        for (key, val) in fallbackExercises {
            if cleanName.contains(key) || key.contains(cleanName) {
                return (val.videoUrl, val.description, val.benefits)
            }
        }
        
        // 4. Final fallback
        return (nil, "This exercise was assigned specifically for your recovery plan by your doctor.", ["Targeted Relief", "Recovery Focus"])
    }

    private func loadData() {
        isLoading = true
        
        // Fetch assignments from the "Live Bridge" (Doctor's local assignment)
        let myId = SessionManager.shared.userId ?? "99"
        let doctorAssignedNames = UserDefaults.standard.stringArray(forKey: "doctor_assigned_rehab_\(myId)") ?? []
        
        // Fetch exercises from database first to map correct video URLs and details
        DoctorService.shared.getExercises { result in
            DispatchQueue.main.async {
                var backendExercises: [Exercise] = []
                if case .success(let response) = result, let data = response.data {
                    backendExercises = data
                }
                
                var list: [UnifiedExercise] = []
                
                // 1. Add Doctor's assigned exercises
                for name in doctorAssignedNames {
                    let details = self.getBackendVideoAndDetails(forName: name, fromBackendExercises: backendExercises)
                    list.append(UnifiedExercise(
                        category: "DOCTOR ASSIGNED",
                        name: name,
                        description: details.description,
                        benefits: details.benefits,
                        sets: 3,
                        reps: "12",
                        videoUrl: details.videoUrl,
                        isCompleted: false
                    ))
                }
                
                // 2. Add standard "Already existing data"
                let standardExercises = ["Thumb Flexion", "Wrist Rotation"]
                for name in standardExercises {
                    let details = self.getBackendVideoAndDetails(forName: name, fromBackendExercises: backendExercises)
                    list.append(UnifiedExercise(
                        category: name == "Thumb Flexion" ? "HAND" : "WRIST",
                        name: name,
                        description: details.description,
                        benefits: details.benefits,
                        sets: 3,
                        reps: name == "Thumb Flexion" ? "10" : "15",
                        videoUrl: details.videoUrl,
                        isCompleted: false
                    ))
                }
                
                self.allUnifiedExercises = list
                
                withAnimation(.spring()) {
                    self.isLoading = false
                }
            }
        }
    }
    
    private func watchVideoAction(for exerciseData: UnifiedExercise) {
        let cleanName = exerciseData.name.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
        let videoURL: URL
        
        // 1. Force the use of valid YouTube URLs since the backend MP4 files are missing
        if let fallbackData = fallbackExercises[cleanName], let ytURL = URL(string: fallbackData.videoUrl) {
            videoURL = ytURL
        }
        // 2. Try the raw videoUrl if it's already a valid direct link
        else if let rawUrlString = exerciseData.videoUrl, let rawURL = URL(string: rawUrlString) {
            videoURL = rawURL
        }
        // 3. Last resort fallback
        else {
            videoURL = URL(string: "https://www.youtube.com/watch?v=0ceVKwRSo8k")!
        }
        
        self.selectedVideoItem = VideoItem(name: exerciseData.name, url: videoURL)
    }

    private func getBackendVideoUrl(forName name: String, id: String?) -> URL? {
        let cleanName = name.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
        
        let filename: String
        if let id = id, !id.isEmpty {
            switch id {
            case "ex_001": filename = "ex_001_wrist_flexion.mp4"
            case "ex_002": filename = "ex_002_wrist_rotation.mp4"
            case "ex_003": filename = "ex_003_thumb_opposition.mp4"
            case "ex_004": filename = "ex_004_thumb_flexion.mp4"
            case "ex_005": filename = "ex_005_finger_flexion.mp4"
            case "ex_006": filename = "ex_006_finger_extension.mp4"
            case "ex_007": filename = "ex_007_finger_pinch.mp4"
            case "ex_008": filename = "ex_008_knee_flexion.mp4"
            case "ex_009": filename = "ex_009_hip_flexion.mp4"
            case "ex_010": filename = "ex_010_hip_abduction.mp4"
            default: filename = ""
            }
        } else {
            // Map by name matching
            if cleanName.contains("wrist flexion") {
                filename = "ex_001_wrist_flexion.mp4"
            } else if cleanName.contains("wrist rotation") {
                filename = "ex_002_wrist_rotation.mp4"
            } else if cleanName.contains("thumb opposition") {
                filename = "ex_003_thumb_opposition.mp4"
            } else if cleanName.contains("thumb flexion") {
                filename = "ex_004_thumb_flexion.mp4"
            } else if cleanName.contains("finger flexion") {
                filename = "ex_005_finger_flexion.mp4"
            } else if cleanName.contains("finger extension") || cleanName.contains("finger spreading") {
                filename = "ex_006_finger_extension.mp4"
            } else if cleanName.contains("finger pinch") {
                filename = "ex_007_finger_pinch.mp4"
            } else if cleanName.contains("knee flexion") {
                filename = "ex_008_knee_flexion.mp4"
            } else if cleanName.contains("hip flexion") {
                filename = "ex_009_hip_flexion.mp4"
            } else if cleanName.contains("hip abduction") {
                filename = "ex_010_hip_abduction.mp4"
            } else {
                filename = ""
            }
        }
        
        guard !filename.isEmpty else { return nil }
        
        // Build the complete URL using NetworkConfig
        let urlString = NetworkConfig.serverURL + "exercise_videos/" + filename
        return URL(string: urlString)
    }

    private func markAsCompleted(id: UUID) {
        hapticFeedback(.medium)
        if let index = allUnifiedExercises.firstIndex(where: { $0.id == id }) {
            let exercise = allUnifiedExercises[index]
            
            // If already completed, we don't log again for now (or we could)
            guard !exercise.isCompleted else { return }
            
            withAnimation(.spring()) {
                allUnifiedExercises[index].isCompleted = true
                toastMessage = "Exercise Completed!"
                showToast = true
                
                // Persist to server if we have a serverId
                if let serverId = exercise.serverId {
                    let dateFormatter = DateFormatter()
                    dateFormatter.dateFormat = "yyyy-MM-dd HH:mm:ss"
                    let now = dateFormatter.string(from: Date())
                    
                    let request = ExerciseSessionRequest(
                        exercise_id: serverId,
                        start_time: now,
                        session_duration: 300, // Mock 5 mins
                        overall_accuracy: 1.0,
                        completion_rate: 1.0,
                        completed: true
                    )
                    
                    PatientService.shared.logExerciseSession(request: request) { _ in
                        // Handled silently or could update UI
                    }
                }
                
                DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                    withAnimation { showToast = false }
                }
            }
        }
    }
}

struct RehabExercisePremiumCard: View {
    let category: String
    let name: String
    let description: String
    let benefits: [String]
    let sets: Int
    let reps: String
    let videoUrl: String?
    let isCompleted: Bool
    let onComplete: () -> Void
    let onWatchVideo: () -> Void
    
    private let tealColor = Color(red: 0/255, green: 121/255, blue: 107/255)
    
    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            HStack {
                Text(category)
                    .font(.system(size: 12, weight: .bold))
                    .foregroundColor(tealColor)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .background(tealColor.opacity(0.1))
                    .cornerRadius(4)
                
                Spacer()
                
                if isCompleted {
                    Image(systemName: "checkmark.circle.fill")
                        .foregroundColor(.green)
                        .font(.system(size: 18))
                        .transition(.scale.combined(with: .opacity))
                }
            }
            
            Text(name)
                .font(.system(size: 22, weight: .bold))
                .foregroundColor(.primary)
            
            Text(description)
                .font(.system(size: 16))
                .foregroundColor(.secondary)
                .lineLimit(2)
            
            VStack(alignment: .leading, spacing: 8) {
                Text("Benefits:")
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(.primary)
                
                ForEach(benefits, id: \.self) { benefit in
                    HStack(alignment: .top, spacing: 8) {
                        Text("•")
                            .font(.system(size: 16, weight: .bold))
                        Text(benefit)
                            .font(.system(size: 15))
                    }
                    .foregroundColor(.secondary)
                }
            }
            
            Divider()
            
            HStack {
                MetricColumn(title: "SETS", value: "\(sets)")
                
                Divider()
                    .frame(height: 30)
                    .padding(.horizontal, 10)
                
                MetricColumn(title: "REPS", value: reps)
                
                Spacer()
                
                Button(action: onWatchVideo) {
                    HStack(spacing: 6) {
                        Image(systemName: "play.circle.fill")
                            .font(.system(size: 16, weight: .bold))
                        Text("Watch Video")
                            .font(.system(size: 16, weight: .bold))
                    }
                    .foregroundColor(tealColor)
                }
            }
            .padding(.bottom, 4)
            
            Button(action: onComplete) {
                Text(isCompleted ? "Completed" : "Mark as Completed")
                    .font(.system(size: 18, weight: .bold))
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 16)
                    .background(isCompleted ? Color.green : tealColor)
                    .clipShape(Capsule())
                    .shadow(color: (isCompleted ? Color.green : tealColor).opacity(0.3), radius: 8, x: 0, y: 4)
            }
            .animation(.spring(), value: isCompleted)
        }
        .padding(12)
        .background(Color.appCard)
        .cornerRadius(16)
        .shadow(color: Color.black.opacity(0.06), radius: 10, x: 0, y: 5)
    }
}

struct MetricColumn: View {
    let title: String
    let value: String
    
    var body: some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(title)
                .font(.system(size: 12, weight: .bold))
                .foregroundColor(.secondary.opacity(0.7))
            Text(value)
                .font(.system(size: 20, weight: .bold))
                .foregroundColor(.primary)
        }
    }
}



#Preview {
    NavigationStack {
        PatientRehabilitationView()
    }
}

