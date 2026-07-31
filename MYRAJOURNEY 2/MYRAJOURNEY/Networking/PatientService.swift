import Foundation

class PatientService {
    static let shared = PatientService()
    private init() {}
    
    func getOverview(completion: @escaping (Result<ApiResponse<PatientOverview>, NetworkError>) -> Void) {
        let endpoint = Endpoint(path: "patients/me/overview", method: .get)
        ApiClient.shared.request(endpoint, completion: completion)
    }
    
    func getReports(completion: @escaping (Result<ApiResponse<[Report]>, NetworkError>) -> Void) {
        let endpoint = Endpoint(path: "reports", method: .get)
        ApiClient.shared.request(endpoint, completion: completion)
    }
    
    func getAppointments(completion: @escaping (Result<ApiResponse<[Appointment]>, NetworkError>) -> Void) {
        let endpoint = Endpoint(path: "appointments", method: .get)
        ApiClient.shared.request(endpoint, completion: completion)
    }
    
    func createSymptom(request: SymptomRequest, completion: @escaping (Result<ApiResponse<[String: Int]>, NetworkError>) -> Void) {
        guard let body = try? JSONEncoder().encode(request) else {
            completion(.failure(.decodingError))
            return
        }
        let endpoint = Endpoint(path: "symptoms", method: .post, body: body)
        ApiClient.shared.request(endpoint, completion: completion)
    }
    
    func getMedications(patientId: Int? = nil, completion: @escaping (Result<ApiResponse<[Medication]>, NetworkError>) -> Void) {
        var path = "patient-medications"
        if let pid = patientId {
            path += "?patient_id=\(pid)"
        }
        let endpoint = Endpoint(path: path, method: .get)
        ApiClient.shared.request(endpoint, completion: completion)
    }
    
    func getSymptoms(patientId: Int? = nil, completion: @escaping (Result<ApiResponse<[SymptomLog]>, NetworkError>) -> Void) {
        var path = "symptoms"
        if let pid = patientId {
            path += "?patient_id=\(pid)"
        }
        let endpoint = Endpoint(path: path, method: .get)
        ApiClient.shared.request(endpoint, completion: completion)
    }
    
    func getHealthMetrics(patientId: Int? = nil, metricType: String? = nil, completion: @escaping (Result<ApiResponse<[HealthMetric]>, NetworkError>) -> Void) {
        var path = "health-metrics?"
        if let pid = patientId { path += "patient_id=\(pid)&" }
        if let type = metricType { path += "metric_type=\(type)&" }
        let endpoint = Endpoint(path: path, method: .get)
        ApiClient.shared.request(endpoint, completion: completion)
    }
    
    func getMedicationLogs(patientId: Int? = nil, completion: @escaping (Result<ApiResponse<[MedicationLog]>, NetworkError>) -> Void) {
        var path = "medication-logs"
        if let pid = patientId {
            path += "?patient_id=\(pid)"
        }
        let endpoint = Endpoint(path: path, method: .get)
        ApiClient.shared.request(endpoint, completion: completion)
    }
    
    func getEducationArticles(completion: @escaping (Result<ApiResponse<[EducationArticle]>, NetworkError>) -> Void) {
        let endpoint = Endpoint(path: "education/articles", method: .get)
        ApiClient.shared.request(endpoint, completion: completion)
    }
    
    func getRehabPlans(patientId: String? = nil, completion: @escaping (Result<ApiResponse<[RehabPlan]>, NetworkError>) -> Void) {
        var path = "rehab-plans"
        if let id = patientId {
            path += "?patient_id=\(id)"
        }
        let endpoint = Endpoint(path: path, method: .get)
        ApiClient.shared.request(endpoint, completion: completion)
    }
    
    func getExerciseAssignments(completion: @escaping (Result<ApiResponse<[ExerciseAssignment]>, NetworkError>) -> Void) {
        let endpoint = Endpoint(path: "exercise-assignments/patient", method: .get)
        ApiClient.shared.request(endpoint, completion: completion)
    }
    
    func createReport(title: String, description: String, patientId: String? = nil, fileData: Data?, fileName: String?, mimeType: String?, completion: @escaping (Result<ApiResponse<Report>, NetworkError>) -> Void) {
        var parameters = [
            "title": title,
            "description": description
        ]
        
        if let pid = patientId {
            parameters["patient_id"] = pid
            parameters["user_id"] = pid // Try both common names
        }
        
        if let did = SessionManager.shared.userId {
            parameters["doctor_id"] = did
        }
        
        ApiClient.shared.uploadMultipart(
            path: "reports",
            parameters: parameters,
            fileData: fileData,
            fileName: fileName,
            mimeType: mimeType,
            completion: completion
        )
    }
    
    func assignMedication(parameters: [String: Any], completion: @escaping (Result<ApiResponse<[String: Int]>, NetworkError>) -> Void) {
        guard let body = try? JSONSerialization.data(withJSONObject: parameters) else {
            completion(.failure(.decodingError))
            return
        }
        let endpoint = Endpoint(path: "patient-medications", method: .post, body: body)
        ApiClient.shared.request(endpoint, completion: completion)
    }
    
    func getNotifications(completion: @escaping (Result<ApiResponse<[NotificationModel]>, NetworkError>) -> Void) {
        let endpoint = Endpoint(path: "notifications", method: .get)
        ApiClient.shared.request(endpoint, completion: completion)
    }
    
    func getExerciseHistory(completion: @escaping (Result<ApiResponse<[ExerciseHistoryRecord]>, NetworkError>) -> Void) {
        let endpoint = Endpoint(path: "exercise-assignments/patient", method: .get)
        ApiClient.shared.request(endpoint, completion: completion)
    }
    
    func logExerciseSession(request: ExerciseSessionRequest, completion: @escaping (Result<ApiResponse<[String: String]>, NetworkError>) -> Void) {
        guard let body = try? JSONEncoder().encode(request) else {
            completion(.failure(.decodingError))
            return
        }
        let endpoint = Endpoint(path: "exercise-sessions", method: .post, body: body)
        ApiClient.shared.request(endpoint, completion: completion)
    }
}


