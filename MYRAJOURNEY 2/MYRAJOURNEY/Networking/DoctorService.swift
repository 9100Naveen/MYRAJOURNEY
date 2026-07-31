import Foundation

class DoctorService {
    static let shared = DoctorService()
    private init() {}
    
    func getOverview(completion: @escaping (Result<ApiResponse<DoctorOverview>, NetworkError>) -> Void) {
        let endpoint = Endpoint(path: "doctor/overview", method: .get)
        ApiClient.shared.request(endpoint, completion: completion)
    }
    
    // ✅ Correct endpoint: /patients returns patients assigned to the logged-in doctor
    // ❌ /doctor/patients returns 404 NOT FOUND
    func getPatients(completion: @escaping (Result<ApiResponse<[Patient]>, NetworkError>) -> Void) {
        let endpoint = Endpoint(path: "patients", method: .get)
        ApiClient.shared.request(endpoint, completion: completion)
    }

    func getAllDoctors(completion: @escaping (Result<ApiResponse<[User]>, NetworkError>) -> Void) {
        let endpoint = Endpoint(path: "admin/doctors", method: .get)
        ApiClient.shared.request(endpoint, completion: completion)
    }

    func getAllPatients(completion: @escaping (Result<ApiResponse<[User]>, NetworkError>) -> Void) {
        let endpoint = Endpoint(path: "patients", method: .get)
        ApiClient.shared.request(endpoint, completion: completion)
    }

    func getAllUsers(completion: @escaping (Result<ApiResponse<[User]>, NetworkError>) -> Void) {
        let endpoint = Endpoint(path: "admin/users", method: .get)
        ApiClient.shared.request(endpoint, completion: completion)
    }

    // NOTE: POST /admin/assignments does NOT exist on this backend.
    // Assignment is handled automatically when creating patient via POST /admin/users.
    // This function now updates the patient's assigned_doctor_id via PUT /admin/users/{id}
    func assignPatientToDoctor(patientId: Int, doctorId: Int, completion: @escaping (Result<ApiResponse<String>, NetworkError>) -> Void) {
        let request: [String: Int] = ["patient_id": patientId, "doctor_id": doctorId]
        guard let body = try? JSONSerialization.data(withJSONObject: request) else {
            completion(.failure(.decodingError))
            return
        }
        let endpoint = Endpoint(path: "admin/assign-patient", method: .post, body: body)
        ApiClient.shared.request(endpoint, completion: completion)
    }
    
    func createUser(request: CreateUserRequest, completion: @escaping (Result<ApiResponse<User>, NetworkError>) -> Void) {
        guard let body = try? JSONEncoder().encode(request) else {
            completion(.failure(.decodingError))
            return
        }
        let endpoint = Endpoint(path: "admin/users", method: .post, body: body)
        ApiClient.shared.request(endpoint, completion: completion)
    }
    
    func getAppointments(patientId: Int? = nil, doctorId: Int? = nil, completion: @escaping (Result<ApiResponse<[Appointment]>, NetworkError>) -> Void) {
        var path = "appointments?"
        if let pid = patientId { path += "patient_id=\(pid)&" }
        if let did = doctorId { path += "doctor_id=\(did)&" }
        let endpoint = Endpoint(path: path, method: .get)
        ApiClient.shared.request(endpoint, completion: completion)
    }
    
    func getReports(completion: @escaping (Result<ApiResponse<[Report]>, NetworkError>) -> Void) {
        let endpoint = Endpoint(path: "reports", method: .get)
        ApiClient.shared.request(endpoint, completion: completion)
    }
    
    // Exercise Management
    func getExercises(completion: @escaping (Result<ApiResponse<[Exercise]>, NetworkError>) -> Void) {
        let endpoint = Endpoint(path: "exercises", method: .get)
        ApiClient.shared.request(endpoint, completion: completion)
    }
    
    func createExercise(exercise: Exercise, completion: @escaping (Result<ApiResponse<String>, NetworkError>) -> Void) {
        guard let body = try? JSONEncoder().encode(exercise) else {
            completion(.failure(.decodingError))
            return
        }
        let endpoint = Endpoint(path: "exercises", method: .post, body: body)
        ApiClient.shared.request(endpoint, completion: completion)
    }
    
    func assignExercises(patientId: Int, exerciseIds: [Int], notes: String?, completion: @escaping (Result<ApiResponse<ExerciseAssignmentResponse>, NetworkError>) -> Void) {
        // This endpoint is currently failing on hosted server due to missing table
        let request = ExerciseAssignmentRequest(patient_id: patientId, exercise_ids: exerciseIds, notes: notes)
        guard let body = try? JSONEncoder().encode(request) else {
            completion(.failure(.decodingError))
            return
        }
        let endpoint = Endpoint(path: "exercise-assignments", method: .post, body: body)
        ApiClient.shared.request(endpoint, completion: completion)
    }

    func createRehabPlan(patientId: Int, title: String, exercises: [[String: Any]], completion: @escaping (Result<ApiResponse<RehabPlan>, NetworkError>) -> Void) {
        let request: [String: Any] = [
            "patient_id": patientId,
            "title": title,
            "exercises": exercises
        ]
        guard let body = try? JSONSerialization.data(withJSONObject: request) else {
            completion(.failure(.decodingError))
            return
        }
        let endpoint = Endpoint(path: "rehab-plans", method: .post, body: body)
        ApiClient.shared.request(endpoint, completion: completion)
    }
    
    func clearAllMedications(completion: @escaping (Result<ApiResponse<String>, NetworkError>) -> Void) {
        let endpoint = Endpoint(path: "admin/patient-medications/clear-all", method: .delete)
        ApiClient.shared.request(endpoint, completion: completion)
    }
    
    func updateUser(userId: Int, request: CreateUserRequest, completion: @escaping (Result<ApiResponse<User>, NetworkError>) -> Void) {
        guard let body = try? JSONEncoder().encode(request) else {
            completion(.failure(.decodingError))
            return
        }
        let endpoint = Endpoint(path: "admin/users/\(userId)", method: .put, body: body)
        ApiClient.shared.request(endpoint, completion: completion)
    }
    
    func getHealthStats(completion: @escaping (Result<ApiResponse<HealthStats>, NetworkError>) -> Void) {
        let endpoint = Endpoint(path: "admin/test", method: .get)
        ApiClient.shared.request(endpoint, completion: completion)
    }
    
    // Send Alert
    func sendAlert(patientId: Int, message: String, completion: @escaping (Result<ApiResponse<String>, NetworkError>) -> Void) {
        let request: [String: Any] = ["patient_id": patientId, "message": message]
        guard let body = try? JSONSerialization.data(withJSONObject: request) else {
            completion(.failure(.decodingError))
            return
        }
        let endpoint = Endpoint(path: "alerts", method: .post, body: body)
        ApiClient.shared.request(endpoint, completion: completion)
    }
    
    // Save Diagnosis
    func saveDiagnosis(patientId: Int, diagnosis: String, suggestions: String, completion: @escaping (Result<ApiResponse<String>, NetworkError>) -> Void) {
        let request: [String: Any] = ["patient_id": patientId, "diagnosis": diagnosis, "treatment_suggestions": suggestions]
        guard let body = try? JSONSerialization.data(withJSONObject: request) else {
            completion(.failure(.decodingError))
            return
        }
        let endpoint = Endpoint(path: "diagnoses", method: .post, body: body)
        ApiClient.shared.request(endpoint, completion: completion)
    }
}
