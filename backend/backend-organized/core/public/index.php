<?php
declare(strict_types=1);

require __DIR__ . '/../src/bootstrap.php';

use Src\Utils\Response;
use Src\Middlewares\Auth;
use Src\Controllers\AuthController;
use Src\Controllers\UserController;
use Src\Controllers\PatientController;
use Src\Controllers\DoctorController;
use Src\Controllers\AppointmentController;
use Src\Controllers\ReportController;
use Src\Controllers\MedicationController;
use Src\Controllers\RehabController;
use Src\Controllers\NotificationController;
use Src\Controllers\EducationController;
use Src\Controllers\SymptomController;
use Src\Controllers\MetricController;
use Src\Controllers\SettingsController;
use Src\Controllers\AdminController;
use Src\Controllers\ReportNoteController;
use Src\Controllers\CrpController;
use Src\Controllers\ChatbotController;
use Src\Controllers\ExerciseController;
use Src\Controllers\ExerciseSessionController;


// Basic CORS handling for preflight
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    Src\Config\Cors::preflight();
    exit;
}
Src\Config\Cors::allow();

// For subfolder deployments, normalize the URI to start from /api/v1/
if (strpos($uri, '/api/v1/') !== false) {
    $uri = strstr($uri, '/api/v1/');
}

// Normalize URI: remove trailing slashes and ensure leading slash
$uri = '/' . trim(parse_url($uri, PHP_URL_PATH), '/');

// CRITICAL: Log all requests
file_put_contents(__DIR__ . '/api_log.txt', date('[Y-m-d H:i:s] ') . $method . ' ' . $uri . ' (RAW: ' . ($_SERVER['REQUEST_URI'] ?? 'n/a') . ')' . PHP_EOL, FILE_APPEND);

// ADDITIONAL: Log POST data for report endpoints
if ($method === 'POST' && strpos($uri, 'reports') !== false) {
    file_put_contents(__DIR__ . '/api_log.txt', date('[Y-m-d H:i:s] REPORTS_POST - FILES: ') . print_r($_FILES, true) . PHP_EOL, FILE_APPEND);
    file_put_contents(__DIR__ . '/api_log.txt', date('[Y-m-d H:i:s] REPORTS_POST - POST: ') . print_r($_POST, true) . PHP_EOL, FILE_APPEND);
}

// Ensure URI starts with /
if ($uri === '' || ($uri[0] !== '/')) {
    $uri = '/' . $uri;
}

// Exclude certain helper files
$allowedFiles = ['admin-api.php', 'doctor-patients.php', 'clear-cache.php'];

// For XAMPP compatibility, we don't need the file serving logic
// since we're using PATH_INFO for routing

// If request targets non-API path and matches known test files or explicitly allowed filenames, return 404 JSON
if (strpos($uri, '/api/v1/') !== 0) {
    $testFiles = ['test-', 'debug-', 'api-info.php'];
    $isTestFile = false;
    foreach ($testFiles as $prefix) {
        if (strpos(basename($uri), $prefix) === 0) {
            $isTestFile = true;
            break;
        }
    }

    if ($isTestFile || in_array(basename($uri), $allowedFiles)) {
        http_response_code(404);
        Response::json([
            'success' => false,
            'error' => [
                'code' => 'NOT_FOUND',
                'message' => 'File not found. If this is a test file, access it directly via browser.'
            ]
        ], 404);
        exit;
    }
}

// Route helper
// Route helper - supports both exact match and hyphen/underscore aliases
function route(string $method, string $path): bool {
    global $uri;
    $reqMethod = $_SERVER['REQUEST_METHOD'] ?? 'GET';
    if ($reqMethod !== $method) return false;
    
    // Exact match
    if ($uri === $path) return true;
    
    // Alias match (rehab-plans == rehab_plans)
    $normalizedUri = str_replace('-', '_', $uri);
    $normalizedPath = str_replace('-', '_', $path);
    return $normalizedUri === $normalizedPath;
}

// ======================
// AUTH ROUTES
// ======================
if (route('POST', '/api/v1/auth/register')) { (new AuthController())->register(); exit; }
if (route('POST', '/api/v1/auth/login')) { (new AuthController())->login(); exit; }
if (route('GET', '/api/v1/auth/me')) { Auth::requireAuth(); (new AuthController())->me(); exit; }
if (route('POST', '/api/v1/auth/forgot-password')) { (new AuthController())->forgotPassword(); exit; }
if (route('POST', '/api/v1/auth/reset-password')) { (new AuthController())->resetPassword(); exit; }
if (route('POST', '/api/v1/auth/change-password')) { Auth::requireAuth(); (new AuthController())->changePassword(); exit; }

// ======================
// USER ROUTES
// ======================
if (route('PUT', '/api/v1/users/me')) { Auth::requireAuth(); (new UserController())->updateMe(); exit; }
if (route('GET', '/api/v1/users')) { Auth::requireAuth(); (new AdminController())->listUsers(); exit; }

// ======================
// PATIENT ROUTES
// ======================
if (route('GET', '/api/v1/patients/me/overview')) { Auth::requireAuth(); (new PatientController())->overviewMe(); exit; }
if (route('GET', '/api/v1/patients')) { Auth::requireAuth(); (new PatientController())->listAll(); exit; }

// ======================
// ADMIN ROUTES
// ======================
if (route('GET', '/api/v1/admin/test')) { Response::json(['success'=>true,'message'=>'Admin routes working','uri'=>$uri]); exit; }
if (route('GET', '/api/v1/admin/db-init')) {
    try {
        $pdo = Src\Config\DB::conn();
        
        // 1. Create ra_exercises
        $pdo->exec("CREATE TABLE IF NOT EXISTS ra_exercises (
            id INT AUTO_INCREMENT PRIMARY KEY,
            name VARCHAR(255) NOT NULL,
            description TEXT,
            category VARCHAR(100),
            target_joints JSON,
            difficulty_level ENUM('BEGINNER', 'INTERMEDIATE', 'ADVANCED'),
            video_url TEXT,
            animation_url TEXT,
            instructions JSON,
            ra_benefits JSON,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )");

        // 2. Create exercise_assignments
        $pdo->exec("CREATE TABLE IF NOT EXISTS exercise_assignments (
            id VARCHAR(50) PRIMARY KEY,
            doctor_id INT NOT NULL,
            patient_id INT NOT NULL,
            exercise_ids JSON NOT NULL,
            notes TEXT,
            assigned_date DATE,
            is_active BOOLEAN DEFAULT TRUE,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
        )");

        // 3. Create rehab_plans
        $pdo->exec("CREATE TABLE IF NOT EXISTS rehab_plans (
            id INT AUTO_INCREMENT PRIMARY KEY,
            patient_id INT NOT NULL,
            title VARCHAR(255) NOT NULL,
            description TEXT,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
        )");

        // 4. Create rehab_exercises
        $pdo->exec("CREATE TABLE IF NOT EXISTS rehab_exercises (
            id INT AUTO_INCREMENT PRIMARY KEY,
            rehab_plan_id INT NOT NULL,
            name VARCHAR(255) NOT NULL,
            description TEXT,
            reps VARCHAR(50),
            sets INT,
            frequency_per_week VARCHAR(50),
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
        )");

        // 5. Create exercise_sessions
        $pdo->exec("CREATE TABLE IF NOT EXISTS exercise_sessions (
            id VARCHAR(50) PRIMARY KEY,
            patient_id INT NOT NULL,
            assignment_id VARCHAR(50),
            session_date DATETIME NOT NULL,
            duration_minutes INT NOT NULL,
            pain_level_before INT,
            pain_level_after INT,
            notes TEXT,
            status ENUM('COMPLETED', 'PARTIAL', 'SKIPPED') DEFAULT 'COMPLETED',
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE
        )");

        $pdo->exec("CREATE TABLE IF NOT EXISTS health_metrics (
            id INT PRIMARY KEY AUTO_INCREMENT,
            patient_id INT NOT NULL,
            metric_type VARCHAR(50) NOT NULL,
            value VARCHAR(50) NOT NULL,
            unit VARCHAR(20),
            recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE
        )");

        $pdo->exec("DROP TABLE IF EXISTS symptom_logs");
        $pdo->exec("CREATE TABLE IF NOT EXISTS symptom_logs (
            id INT PRIMARY KEY AUTO_INCREMENT,
            patient_id INT NOT NULL,
            `date` DATE NOT NULL,
            pain_level INT NOT NULL,
            stiffness_level INT,
            fatigue_level INT,
            joint_count INT,
            notes TEXT,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE
        )");
        
        // 6. Seed some initial exercises if table is empty
        $count = $pdo->query("SELECT COUNT(*) FROM ra_exercises")->fetchColumn();
        if ($count == 0) {
            $pdo->exec("INSERT INTO ra_exercises (name, category, difficulty_level) VALUES 
                ('Thumb Flexion', 'THUMB', 'BEGINNER'),
                ('Thumb Opposition', 'THUMB', 'BEGINNER'),
                ('Wrist Rotation', 'WRIST', 'BEGINNER')");
        }

        Src\Utils\Response::json(['success' => true, 'message' => 'Database tables initialized successfully', 'tables' => ['ra_exercises', 'exercise_assignments', 'rehab_plans', 'rehab_exercises', 'exercise_sessions']]);
    } catch (\Exception $e) {
        Src\Utils\Response::json(['success' => false, 'message' => 'Initialization failed: ' . $e->getMessage()], 500);
    }
    exit;
}
if (route('GET', '/api/v1/admin/users')) { Auth::requireAuth(); (new AdminController())->listUsers(); exit; }
if (route('GET', '/api/v1/admin/patients')) { Auth::requireAuth(); (new PatientController())->listAll(); exit; }
if (route('POST', '/api/v1/admin/users')) { Auth::requireAuth(); (new AdminController())->createUser(); exit; }
if (route('POST', '/api/v1/admin/assign-patient')) { Auth::requireAuth(); (new AdminController())->assignPatientToDoctor(); exit; }
if (route('GET', '/api/v1/admin/doctors')) { Auth::requireAuth(); (new AdminController())->listDoctors(); exit; }
if (preg_match('#^/api/v1/admin/users/(\d+)$#', $uri, $m) && $_SERVER['REQUEST_METHOD'] === 'DELETE') {
    Auth::requireAuth(); (new AdminController())->deleteUser((int)$m[1]); exit;
}

// ======================
// DOCTOR ROUTES
// ======================
if (route('GET', '/api/v1/doctor/overview')) { Auth::requireAuth(); (new DoctorController())->overview(); exit; }
if (route('POST', '/api/v1/doctor/assign-medication')) { Auth::requireAuth(); (new MedicationController())->assign(); exit; }

// ======================
// APPOINTMENTS
// ======================
if (route('GET', '/api/v1/appointments')) { Auth::requireAuth(); (new AppointmentController())->list(); exit; }
if (route('POST', '/api/v1/appointments')) { Auth::requireAuth(); (new AppointmentController())->create(); exit; }
if (preg_match('#^/api/v1/appointments/(\d+)$#', $uri, $m) && $_SERVER['REQUEST_METHOD'] === 'GET') {
    Auth::requireAuth(); (new AppointmentController())->get((int)$m[1]); exit;
}
if (preg_match('#^/api/v1/appointments/(\d+)$#', $uri, $m) && $_SERVER['REQUEST_METHOD'] === 'PATCH') {
    Auth::requireAuth(); (new AppointmentController())->update((int)$m[1]); exit;
}
if (preg_match('#^/api/v1/appointments/(\d+)$#', $uri, $m) && $_SERVER['REQUEST_METHOD'] === 'DELETE') {
    Auth::requireAuth(); (new AppointmentController())->delete((int)$m[1]); exit;
}

// ======================
// REPORT ROUTES
// ======================
if (route('GET', '/api/v1/reports')) { Auth::requireAuth(); (new ReportController())->list(); exit; }
if (route('POST', '/api/v1/reports')) { Auth::requireAuth(); (new ReportController())->create(); exit; }
if (preg_match('#^/api/v1/reports/(\d+)$#', $uri, $m) && $_SERVER['REQUEST_METHOD'] === 'GET') {
    Auth::requireAuth(); (new ReportController())->get((int)$m[1]); exit;
}
if (route('POST', '/api/v1/reports/notes')) { Auth::requireAuth(); (new ReportNoteController())->create(); exit; }
if (preg_match('#^/api/v1/reports/(\d+)/notes$#', $uri, $m) && $_SERVER['REQUEST_METHOD'] === 'GET') {
    Auth::requireAuth(); (new ReportNoteController())->get((int)$m[1]); exit;
}

// ⭐ STATUS ROUTE — required by mobile app to update report status
if (route('POST', '/api/v1/reports/status')) {
    Auth::requireAuth();
    (new ReportController())->updateStatus();
    exit;
}

// ======================
// CRP ROUTES
// ======================
if (route('GET', '/api/v1/crp/test')) { Response::json(['success'=>true,'message'=>'CRP routes working','timestamp'=>date('Y-m-d H:i:s')]); exit; }
if (preg_match('#^/api/v1/crp/history/(\d+)$#', $uri, $m) && $_SERVER['REQUEST_METHOD'] === 'GET') {
    Auth::requireAuth(); (new CrpController())->getHistory((int)$m[1]); exit;
}
if (route('POST', '/api/v1/crp')) { Auth::requireAuth(); (new CrpController())->create(); exit; }

// ======================
// MEDICATION
// ======================
if (route('GET', '/api/v1/medications')) { Auth::requireAuth(); (new MedicationController())->search(); exit; }
if (route('GET', '/api/v1/patient-medications')) { Auth::requireAuth(); (new MedicationController())->listForPatient(); exit; }
if (route('POST', '/api/v1/patient-medications')) { Auth::requireAuth(); (new MedicationController())->assign(); exit; }
if (preg_match('#^/api/v1/patient-medications/(\d+)$#', $uri, $m) && $_SERVER['REQUEST_METHOD'] === 'PATCH') {
    Auth::requireAuth(); (new MedicationController())->setActive((int)$m[1]); exit;
}
if (preg_match('#^/api/v1/patient-medications/(\d+)$#', $uri, $m) && $_SERVER['REQUEST_METHOD'] === 'DELETE') {
    Auth::requireAuth(); (new MedicationController())->delete((int)$m[1]); exit;
}
if (route('GET', '/api/v1/medication-logs')) { Auth::requireAuth(); (new MedicationController())->listLogs(); exit; }
if (route('POST', '/api/v1/medication-logs')) { Auth::requireAuth(); (new MedicationController())->logIntake(); exit; }
if (route('POST', '/api/v1/medications/log')) { Auth::requireAuth(); (new MedicationController())->logIntake(); exit; }
if (route('GET', '/api/v1/medication-logs/test')) { Auth::requireAuth(); Response::json(['success'=>true,'message'=>'Medication logs endpoint working','timestamp'=>date('Y-m-d H:i:s')]); exit; }
if (route('POST', '/api/v1/medication-logs/debug')) { 
    Auth::requireAuth(); 
    $input = json_decode(file_get_contents('php://input'), true) ?? [];
    Response::json([
        'success'=>true,
        'message'=>'Debug endpoint working',
        'received_data'=>$input,
        'auth'=>$_SERVER['auth'] ?? [],
        'timestamp'=>date('Y-m-d H:i:s')
    ]); 
    exit; 
}

// Admin medication management
if (route('DELETE', '/api/v1/admin/patient-medications/clear-all')) { Auth::requireAuth(); (new MedicationController())->clearAllPatientMedications(); exit; }
if (route('GET', '/api/v1/admin/patient-medications/all')) { Auth::requireAuth(); (new MedicationController())->getAllPatientMedications(); exit; }

// ======================
// REHAB
// ======================
if (route('GET', '/api/v1/rehab-plans')) { Auth::requireAuth(); (new RehabController())->listForPatient(); exit; }
if (route('POST', '/api/v1/rehab-plans')) { Auth::requireAuth(); (new RehabController())->createPlan(); exit; }
if (preg_match('#^/api/v1/rehab-plans/(\d+)$#', $uri, $m) && $_SERVER['REQUEST_METHOD'] === 'GET') {
    Auth::requireAuth(); (new RehabController())->getPlan((int)$m[1]); exit;
}
if (preg_match('#^/api/v1/rehab-exercises/(\d+)$#', $uri, $m) && $_SERVER['REQUEST_METHOD'] === 'DELETE') {
    Auth::requireAuth(); (new RehabController())->deleteExercise((int)$m[1]); exit;
}

// ======================
// NOTIFICATIONS
// ======================
if (route('GET', '/api/v1/notifications')) { Auth::requireAuth(); (new NotificationController())->listMine(); exit; }
if (preg_match('#^/api/v1/notifications/(\d+)/read$#', $uri, $m) && $_SERVER['REQUEST_METHOD'] === 'POST') {
    Auth::requireAuth(); (new NotificationController())->markRead((int)$m[1]); exit;
}

// ======================
// EDUCATION
// ======================
if (route('GET', '/api/v1/education/articles')) { (new EducationController())->list(); exit; }
if (preg_match('#^/api/v1/education/articles/([A-Za-z0-9_-]+)$#', $uri, $m)
    && $_SERVER['REQUEST_METHOD'] === 'GET') {
    (new EducationController())->getBySlug($m[1]); exit;
}

// ======================
// SYMPTOMS
// ======================
if (route('GET', '/api/v1/symptoms')) { Auth::requireAuth(); (new SymptomController())->list(); exit; }
if (route('POST', '/api/v1/symptoms')) { Auth::requireAuth(); (new SymptomController())->create(); exit; }

// ======================
// METRICS
// ======================
if (route('GET', '/api/v1/health-metrics')) { Auth::requireAuth(); (new MetricController())->list(); exit; }
if (route('POST', '/api/v1/health-metrics')) { Auth::requireAuth(); (new MetricController())->create(); exit; }

// ======================
// SETTINGS
// ======================
if (route('GET', '/api/v1/settings')) { Auth::requireAuth(); (new SettingsController())->getMine(); exit; }
if (route('PUT', '/api/v1/settings')) { Auth::requireAuth(); (new SettingsController())->putMine(); exit; }

// ======================
// CHATBOT & CONVERSATION MANAGEMENT
// ======================
if (route('POST', '/api/v1/chatbot/chat')) { Auth::requireAuth(); (new ChatbotController())->chat(); exit; }
if (route('GET', '/api/v1/chatbot/history')) { Auth::requireAuth(); (new ChatbotController())->history(); exit; }
if (route('GET', '/api/v1/chatbot/session/history')) { Auth::requireAuth(); (new ChatbotController())->sessionHistory(); exit; }
if (route('POST', '/api/v1/chatbot/session/end')) { Auth::requireAuth(); (new ChatbotController())->endSession(); exit; }
if (route('GET', '/api/v1/chatbot/session/context')) { Auth::requireAuth(); (new ChatbotController())->getContext(); exit; }

// ======================
// EXERCISE TRACKING SYSTEM
// ======================
// Exercise Library Routes
if (route('GET', '/api/v1/exercises')) { Auth::requireAuth(); (new ExerciseController())->getAllExercises(); exit; }
if (route('POST', '/api/v1/exercises')) { Auth::requireAuth(); (new ExerciseController())->createExercise(); exit; }
if (preg_match('#^/api/v1/exercises/([A-Za-z0-9_-]+)$#', $uri, $m) && $_SERVER['REQUEST_METHOD'] === 'GET') {
    Auth::requireAuth(); (new ExerciseController())->getExerciseById($m[1]); exit;
}
if (preg_match('#^/api/v1/exercises/category/([A-Z]+)$#', $uri, $m) && $_SERVER['REQUEST_METHOD'] === 'GET') {
    Auth::requireAuth(); (new ExerciseController())->getExercisesByCategory($m[1]); exit;
}

// Exercise Assignment Routes
if (route('POST', '/api/v1/exercise-assignments')) { Auth::requireAuth(); (new ExerciseController())->createAssignment(); exit; }
if (route('GET', '/api/v1/exercise-assignments/patient')) { Auth::requireAuth(); (new ExerciseController())->getPatientAssignments(); exit; }
if (route('GET', '/api/v1/exercise-assignments/doctor')) { Auth::requireAuth(); (new ExerciseController())->getDoctorAssignments(); exit; }
if (preg_match('#^/api/v1/exercise-assignments/([A-Za-z0-9_-]+)$#', $uri, $m) && $_SERVER['REQUEST_METHOD'] === 'PUT') {
    Auth::requireAuth(); (new ExerciseController())->updateAssignment($m[1]); exit;
}
if (preg_match('#^/api/v1/exercise-assignments/([A-Za-z0-9_-]+)$#', $uri, $m) && $_SERVER['REQUEST_METHOD'] === 'DELETE') {
    Auth::requireAuth(); (new ExerciseController())->deleteAssignment($m[1]); exit;
}

// Exercise Session Routes
if (route('POST', '/api/v1/exercise-sessions')) { Auth::requireAuth(); (new ExerciseSessionController())->createSession(); exit; }
if (preg_match('#^/api/v1/exercise-sessions/patient/(\d+)$#', $uri, $m) && $_SERVER['REQUEST_METHOD'] === 'GET') {
    Auth::requireAuth(); (new ExerciseSessionController())->getPatientSessions((int)$m[1]); exit;
}
if (preg_match('#^/api/v1/exercise-sessions/([A-Za-z0-9_-]+)/report$#', $uri, $m) && $_SERVER['REQUEST_METHOD'] === 'POST') {
    Auth::requireAuth(); (new ExerciseSessionController())->generateReport($m[1]); exit;
}

// Exercise Report Routes
if (preg_match('#^/api/v1/exercise-reports/patient/(\d+)$#', $uri, $m) && $_SERVER['REQUEST_METHOD'] === 'GET') {
    Auth::requireAuth(); (new ExerciseSessionController())->getPatientReports((int)$m[1]); exit;
}



// ======================
// 404
// ======================
Response::json([
    'success' => false,
    'error' => [
        'code' => 'NOT_FOUND',
        'message' => 'Endpoint not found',
        'debug' => [
            'method' => $method,
            'uri' => $uri,
            'raw_uri' => $_SERVER['REQUEST_URI'] ?? 'n/a'
        ]
    ]
], 404);
