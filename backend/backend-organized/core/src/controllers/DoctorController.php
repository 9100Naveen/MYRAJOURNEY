<?php
declare(strict_types=1);

namespace Src\Controllers;

use Src\Config\DB;
use Src\Utils\Response;

class DoctorController
{
	public function overview(): void
	{
		$auth = $_SERVER['auth'] ?? [];
		$uid = (int)($auth['uid'] ?? 0);
		$db = DB::conn();
		$today = date('Y-m-d');
		
		// Get today's schedule
		$schedule = $db->prepare("SELECT * FROM appointments WHERE doctor_id=:uid AND DATE(start_time)=:d ORDER BY start_time ASC");
		$schedule->execute([':uid'=>$uid, ':d'=>$today]);
		
		// Count only assigned patients' reports (last 7 days)
		$reportStmt = $db->prepare("SELECT COUNT(*) FROM reports r 
			INNER JOIN patients p ON r.patient_id = p.id 
			WHERE p.assigned_doctor_id = :uid AND r.uploaded_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)");
		$reportStmt->execute([':uid'=>$uid]);
		$reportCount = (int)$reportStmt->fetchColumn();
		
		// Count assigned patients
		$patientStmt = $db->prepare("SELECT COUNT(*) FROM patients WHERE assigned_doctor_id = :uid");
		$patientStmt->execute([':uid'=>$uid]);
		$patientCount = (int)$patientStmt->fetchColumn();
		
		Response::json(['success'=>true,'data'=>[
			'todaySchedule'=>$schedule->fetchAll(),
			'recentReportsCount'=>$reportCount,
			'patientsCount'=>$patientCount,
		]]);
	}
}




















