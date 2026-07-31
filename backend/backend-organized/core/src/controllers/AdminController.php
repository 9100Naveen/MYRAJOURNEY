<?php
declare(strict_types=1);

namespace Src\Controllers;

use Src\Utils\Response;
use Src\Models\UserModel;
use Src\Config\DB;

class AdminController
{
    private UserModel $users;

    public function __construct()
    {
       $this->users = new UserModel();
    }

    public function createUser(): void
    {
       $auth = $_SERVER['auth'] ?? [];
       $role = $auth['role'] ?? '';
       $creatorId = (int)($auth['uid'] ?? 0);

       // ✅ CHANGE 1: Allow ADMIN and DOCTOR roles
       if ($role !== 'ADMIN' && $role !== 'DOCTOR') {
          Response::json(['success'=>false,'error'=>['code'=>'FORBIDDEN','message'=>'Access denied']], 403);
          return;
       }

       $body = json_decode(file_get_contents('php://input'), true) ?? [];

       $email = trim(strtolower($body['email'] ?? ''));
       $password = (string)($body['password'] ?? '');
       $userRole = in_array($body['role'] ?? 'PATIENT', ['PATIENT','DOCTOR']) ? $body['role'] : 'PATIENT';
       $name = $body['name'] ?? null;
       $phone = $body['phone'] ?? $body['mobile'] ?? null;

       // ✅ CHANGE 2: Doctors can ONLY create Patients
       if ($role === 'DOCTOR' && $userRole !== 'PATIENT') {
           Response::json(['success'=>false,'error'=>['code'=>'FORBIDDEN','message'=>'Doctors can only register new patients.']], 403);
           return;
       }

       if (!$email || strlen($password) < 6) {
          Response::json(['success'=>false,'error'=>['code'=>'VALIDATION','message'=>'Invalid email or password (min 6 chars)']], 422);
          return;
       }

       if ($this->users->findByEmail($email)) {
          Response::json(['success'=>false,'error'=>['code'=>'EMAIL_TAKEN','message'=>'Email already registered']], 409);
          return;
       }

       $db = DB::conn();
       try {
           $db->beginTransaction();

           $uid = $this->users->create([
              'email'=>$email,
              'password_hash'=>password_hash($password, PASSWORD_BCRYPT),
              'role'=>$userRole,
              'name'=>$name,
              'phone'=>$phone,
           ]);

           if ($userRole === 'PATIENT') {
              // ✅ CHANGE 3: Auto-assign to the creating Doctor
              if ($role === 'DOCTOR') {
                  $assignedDoctorId = $creatorId;
              } else {
                  // Admins can optionally assign a doctor ID via the request
                  $assignedDoctorId = isset($body['assigned_doctor_id']) ? (int)$body['assigned_doctor_id'] : null;
              }

              $address = $body['address'] ?? null;
              $age = $body['age'] ?? null;
              $gender = $body['gender'] ?? 'OTHER'; // Default to OTHER if not provided
              $medicalId = $body['medical_id'] ?? ('MJ-' . strtoupper(substr(uniqid(), -6)));

              $stmt = $db->prepare('INSERT INTO patients (id, assigned_doctor_id, address, age, gender, medical_id, created_at, updated_at) VALUES (:id, :doctor_id, :address, :age, :gender, :medical_id, NOW(), NOW())');
              $stmt->execute([
                  ':id' => $uid,
                  ':doctor_id' => $assignedDoctorId,
                  ':address' => $address,
                  ':age' => $age,
                  ':gender' => $gender,
                  ':medical_id' => $medicalId
              ]);
           }

           if ($userRole === 'DOCTOR') {
              $stmt = $db->prepare('INSERT INTO doctors (id, specialization, created_at, updated_at) VALUES (:id, :specialization, NOW(), NOW())');
              $stmt->execute([
                  ':id' => $uid,
                  ':specialization' => $body['specialization'] ?? null
              ]);
           }

           $db->commit();

           Response::json(['success'=>true,'data'=>[
              'id'=>$uid,
              'email'=>$email,
              'role'=>$userRole,
              'name'=>$name,
           ]], 201);

       } catch (\Throwable $e) {
           $db->rollBack();
           error_log("Create User Failed: " . $e->getMessage());
           Response::json([
               'success'=>false,
               'error'=>[
                   'code'=>'CREATION_FAILED',
                   'message'=>'Failed to create user. ' . $e->getMessage()
               ]
           ], 500);
       }
    }

    public function listUsers(): void
    {
       $auth = $_SERVER['auth'] ?? [];
       $role = $auth['role'] ?? '';

       if ($role !== 'ADMIN') {
          Response::json(['success'=>false,'error'=>['code'=>'FORBIDDEN','message'=>'Access denied']], 403);
          return;
       }

       $db = DB::conn();
        $stmt = $db->prepare("
            SELECT 
                u.id, u.name, u.email, u.role, u.phone, u.status,
                p.age, p.gender, p.address, p.assigned_doctor_id,
                d_u.name AS assigned_doctor_name,
                doc.specialization
            FROM users u
            LEFT JOIN patients p ON u.id = p.id
            LEFT JOIN users d_u ON p.assigned_doctor_id = d_u.id
            LEFT JOIN doctors doc ON u.id = doc.id
            ORDER BY u.created_at DESC
        ");
        $stmt->execute();
        $users = $stmt->fetchAll();

        // Optional: ensure numeric types if PDO doesn't handle them
        foreach ($users as &$u) {
            $u['id'] = (int)$u['id'];
            if (isset($u['assigned_doctor_id'])) {
                $u['assigned_doctor_id'] = (int)$u['assigned_doctor_id'];
            }
        }

       Response::json(['success'=>true,'data'=>$users]);
    }

    public function assignPatientToDoctor(): void
    {
       $auth = $_SERVER['auth'] ?? [];
       $role = $auth['role'] ?? '';

       if ($role !== 'ADMIN') {
          Response::json(['success'=>false,'error'=>['code'=>'FORBIDDEN','message'=>'Only admins can assign patients']], 403);
          return;
       }

       $body = json_decode(file_get_contents('php://input'), true) ?? [];
       $patientId = (int)($body['patient_id'] ?? 0);
       $doctorId = isset($body['doctor_id']) ? (int)$body['doctor_id'] : null;

       if ($patientId <= 0) {
          Response::json(['success'=>false,'error'=>['code'=>'VALIDATION','message'=>'Invalid patient ID']], 422);
          return;
       }

       $db = DB::conn();

       $stmt = $db->prepare('SELECT id FROM users WHERE id = :id AND role = "PATIENT"');
       $stmt->execute([':id'=>$patientId]);
       if (!$stmt->fetch()) {
          Response::json(['success'=>false,'error'=>['code'=>'NOT_FOUND','message'=>'Patient not found']], 404);
          return;
       }

       if ($doctorId !== null) {
          $stmt = $db->prepare('SELECT id FROM users WHERE id = :id AND role = "DOCTOR"');
          $stmt->execute([':id'=>$doctorId]);
          if (!$stmt->fetch()) {
             Response::json(['success'=>false,'error'=>['code'=>'NOT_FOUND','message'=>'Doctor not found']], 404);
             return;
          }
       }

       $stmt = $db->prepare('UPDATE patients SET assigned_doctor_id = :doctor_id, updated_at = NOW() WHERE id = :patient_id');
       $stmt->execute([':doctor_id'=>$doctorId, ':patient_id'=>$patientId]);

       Response::json(['success'=>true,'message'=>'Patient assigned successfully']);
    }

    public function listDoctors(): void
    {
       $auth = $_SERVER['auth'] ?? [];
       $role = $auth['role'] ?? '';

       if ($role !== 'ADMIN') {
          Response::json(['success'=>false,'error'=>['code'=>'FORBIDDEN','message'=>'Access denied']], 403);
          return;
       }

       $db = DB::conn();
       $stmt = $db->prepare("SELECT u.id, u.name, u.email, d.specialization
          FROM users u
          LEFT JOIN doctors d ON u.id = d.id
          WHERE u.role = 'DOCTOR' AND u.status = 'ACTIVE'
          ORDER BY u.name ASC");
       $stmt->execute();
       $doctors = $stmt->fetchAll();

       foreach ($doctors as &$d) {
           $d['id'] = (int)$d['id'];
       }

       Response::json(['success'=>true,'data'=>$doctors]);
    }
    public function deleteUser(int $id): void
    {
        $auth = $_SERVER['auth'] ?? [];
        $role = $auth['role'] ?? '';

        if ($role !== 'ADMIN') {
            Response::json(['success'=>false,'error'=>['code'=>'FORBIDDEN','message'=>'Only admins can delete users']], 403);
            return;
        }

        $db = DB::conn();
        try {
            $db->beginTransaction();
            $db->prepare("DELETE FROM patients WHERE id = ?")->execute([$id]);
            $db->prepare("DELETE FROM doctors WHERE id = ?")->execute([$id]);
            $db->prepare("DELETE FROM appointments WHERE patient_id = ? OR doctor_id = ?")->execute([$id, $id]);
            $db->prepare("DELETE FROM reports WHERE patient_id = ?")->execute([$id]);
            $db->prepare("DELETE FROM health_metrics WHERE patient_id = ?")->execute([$id]);
            $db->prepare("DELETE FROM patient_medications WHERE patient_id = ?")->execute([$id]);
            $db->prepare("DELETE FROM notifications WHERE user_id = ?")->execute([$id]);
            
            $stmt = $db->prepare("DELETE FROM users WHERE id = ?");
            $stmt->execute([$id]);

            if ($stmt->rowCount() === 0) {
                $db->rollBack();
                Response::json(['success'=>false,'error'=>['code'=>'NOT_FOUND','message'=>'User not found']], 404);
                return;
            }

            $db->commit();
            Response::json(['success'=>true,'message'=>'User deleted successfully']);
        } catch (\Throwable $e) {
            $db->rollBack();
            Response::json(['success'=>false,'error'=>['code'=>'DELETE_FAILED','message'=>$e->getMessage()]], 500);
        }
    }
}
