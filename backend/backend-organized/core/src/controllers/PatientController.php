<?php
declare(strict_types=1);

namespace Src\Controllers;
use PDO;


use Src\Utils\Response;
use Src\Config\DB;

class PatientController
{
    public function overviewMe(): void
        {
            $auth = $_SERVER['auth'] ?? [];
            $uid = (int)($auth['uid'] ?? 0);
            $db = DB::conn();

            // 1. Get User Details (Name)
            $userStmt = $db->prepare("SELECT name FROM users WHERE id = :uid");
            $userStmt->execute([':uid' => $uid]);
            $userName = $userStmt->fetchColumn();

            // 2. Next Appointment
            // [IMPORTANT] This query hides appointments that have passed by even 1 second
            $nextAppt = $db->prepare("SELECT * FROM appointments WHERE patient_id = :uid AND start_time >= NOW() ORDER BY start_time ASC LIMIT 1");
            $nextAppt->execute([':uid' => $uid]);
            $nextAppointment = $nextAppt->fetch(PDO::FETCH_ASSOC) ?: null;

            // 3. Recent Reports - Use ReportModel
            $reportModel = new \Src\Models\ReportModel();
            $recentReports = $reportModel->getRecentForPatient($uid, 5);

            // 4. Unread Notifications
            $unread = $db->prepare("SELECT COUNT(*) FROM notifications WHERE user_id = :uid AND read_at IS NULL");
            $unread->execute([':uid' => $uid]);

            // 5. Get specific metrics for DAS28 and Pain
            // (Fetching the most recent value for each type)
            $painStmt = $db->prepare("SELECT value FROM health_metrics WHERE patient_id = :uid AND metric_type = 'pain_level' ORDER BY recorded_at DESC LIMIT 1");
            $painStmt->execute([':uid' => $uid]);
            $painLevel = (int)$painStmt->fetchColumn();

            $dasStmt = $db->prepare("SELECT value FROM health_metrics WHERE patient_id = :uid AND metric_type = 'das28' ORDER BY recorded_at DESC LIMIT 1");
            $dasStmt->execute([':uid' => $uid]);
            $dasScore = (float)$dasStmt->fetchColumn();

            Response::json([
                'success' => true,
                'data' => [
                    'patient_name' => $userName,          // Added this
                    'next_appointment' => $nextAppointment,
                    'recent_reports' => $recentReports,
                    'unread_notifications' => (int)$unread->fetchColumn(),
                    'pain_level' => $painLevel,           // Added this
                    'das28_score' => $dasScore,           // Added this
                    'latest_metrics' => []                // Placeholder if needed
                ]
            ]);
        }

    // ---------------------------------------------------------
    // List all patients (doctor/admin)
    // ---------------------------------------------------------
    public function listAll(): void
    {
        $auth = $_SERVER['auth'] ?? [];
        $uid  = (int)($auth['uid'] ?? 0);
        $role = $auth['role'] ?? '';

        $db = DB::conn();

        // ---------------------------------------------------------
        // DOCTOR: Return ONLY assigned patients
        // ---------------------------------------------------------
        if ($role === 'DOCTOR') {

            $stmt = $db->prepare("
                SELECT
                    u.id, u.name, u.email, u.phone, u.role, u.created_at,
                    p.age, p.gender, p.medical_id, p.address, p.assigned_doctor_id
                FROM users u
                LEFT JOIN patients p ON u.id = p.id
                WHERE u.role = 'PATIENT'
                  AND u.status = 'ACTIVE'
                  AND p.assigned_doctor_id = :doctor_id
                ORDER BY u.created_at DESC
            ");

            $stmt->execute([':doctor_id' => $uid]);
            $patients = $stmt->fetchAll();

            foreach ($patients as &$p) {
                $p['id'] = (int)$p['id'];
                if (isset($p['assigned_doctor_id'])) {
                    $p['assigned_doctor_id'] = (int)$p['assigned_doctor_id'];
                }
            }

            Response::json([
                'success' => true,
                'data'    => $patients
            ]);

            return;
        }

        // ---------------------------------------------------------
        // ADMIN: Return ALL PATIENTS (NOT doctors)
        // ---------------------------------------------------------
        if ($role === 'ADMIN') {

            $stmt = $db->prepare("
                SELECT
                    u.id, u.name, u.email, u.phone, u.role, u.created_at,
                    p.age, p.gender, p.medical_id, p.address, p.assigned_doctor_id,
                    d.name AS doctor_name, doc.specialization
                FROM users u
                LEFT JOIN patients p ON u.id = p.id
                LEFT JOIN users d ON p.assigned_doctor_id = d.id
                LEFT JOIN doctors doc ON u.id = doc.id
                WHERE u.role = 'PATIENT'
                  AND u.status = 'ACTIVE'
                ORDER BY u.created_at DESC
            ");

            $stmt->execute();
            $patients = $stmt->fetchAll();

            foreach ($patients as &$p) {
                $p['id'] = (int)$p['id'];
                if (isset($p['assigned_doctor_id'])) {
                    $p['assigned_doctor_id'] = (int)$p['assigned_doctor_id'];
                }
            }

            Response::json([
                'success' => true,
                'data'    => $patients
            ]);

            return;
        }

        // ---------------------------------------------------------
        // Unauthorized
        // ---------------------------------------------------------
        Response::json([
            'success' => false,
            'error' => [
                'code' => 'FORBIDDEN',
                'message' => 'Access denied'
            ]
        ], 403);
    }
}
