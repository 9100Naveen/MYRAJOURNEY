<?php
declare(strict_types=1);

namespace Src\Controllers;

use Src\Models\AppointmentModel;
use Src\Models\NotificationModel;
use Src\Utils\Response;

class AppointmentController
{
    private AppointmentModel $appts;

    public function __construct() {
        $this->appts = new AppointmentModel();
    }

    /**
     * List Appointments
     */
    public function list(): void
    {
        $auth = $_SERVER['auth'] ?? [];
        $uid  = (int)($auth['uid'] ?? 0);
        $role = $auth['role'] ?? '';

        $page  = max(1, (int)($_GET['page'] ?? 1));
        $limit = max(1, min(100, (int)($_GET['limit'] ?? 20)));

        $filters = [];

        if ($role === 'PATIENT') {
            $filters['patient_id'] = $uid;
        }
        elseif ($role === 'DOCTOR') {
            $filters['doctor_id'] = $uid;
        }
        else {
            if (!empty($_GET['patient_id'])) {
                $filters['patient_id'] = (int)$_GET['patient_id'];
            }
            if (!empty($_GET['doctor_id'])) {
                $filters['doctor_id'] = (int)$_GET['doctor_id'];
            }
        }

        $result = $this->appts->list($filters, $page, $limit);

        // Format for mobile
        foreach ($result['items'] as &$a) {
            $start = strtotime($a['start_time']);
            $end   = strtotime($a['end_time']);

            $a['formatted_date']      = date('M d, Y', $start);
            $a['formatted_time_slot'] = date('h:i A', $start) .
                                        ($end ? ' - ' . date('h:i A', $end) : '');

            $a['appointment_type'] = $a['appointment_type'] ?? $a['title'];
            $a['reason']           = $a['reason'] ?? $a['description'];
        }

        Response::json([
            'success' => true,
            'data'    => $result['items'],
            'meta'    => [
                'total' => $result['total'],
                'page'  => $page,
                'limit' => $limit
            ]
        ]);
    }

    /**
     * Create Appointment
     */
    public function create(): void
    {
        $auth = $_SERVER['auth'] ?? [];
        $uid  = (int)($auth['uid'] ?? 0);
        $role = $auth['role'] ?? '';

        $body = json_decode(file_get_contents('php://input'), true) ?? [];

        // auto-set for patients and doctors
        if ($role === 'PATIENT') {
            $body['patient_id'] = $uid;
        } elseif ($role === 'DOCTOR') {
            $body['doctor_id'] = $uid;
        }

        $required = ['patient_id','doctor_id','title','start_time','end_time'];
        foreach ($required as $f) {
            if (empty($body[$f])) {
                Response::json([
                    'success'=>false,
                    'error'=>[
                        'code'=>'VALIDATION',
                        'message'=>"Missing $f"
                    ]
                ], 422);
                return;
            }
        }

        // convert Android input: "reason" → description
        if (isset($body['reason']) && !isset($body['description'])) {
            $body['description'] = $body['reason'];
        }

        $id   = $this->appts->create($body);
        $item = $this->appts->find($id);

        // Push notification
        try {
            $notif = new NotificationModel();
            if ($role === 'DOCTOR') {
                $notif->create((int)$body['patient_id'], 'APPOINTMENT',
                    'New appointment scheduled', $body['title']);
            } else {
                $notif->create((int)$body['doctor_id'], 'APPOINTMENT',
                    'New appointment request', $body['title']);
            }
        } catch (\Throwable $e) {}

        // Format
        if ($item) {
            $start = strtotime($item['start_time']);
            $end   = strtotime($item['end_time']);

            $item['formatted_date']      = date('M d, Y', $start);
            $item['formatted_time_slot'] = date('h:i A', $start) .
                                           ($end ? ' - ' . date('h:i A', $end) : '');
            $item['appointment_type']    = $item['title'];
            $item['reason']              = $item['description'];
        }

        Response::json(['success'=>true,'data'=>$item], 201);
    }

    /**
     * Get Appointment
     */
    public function get(int $id): void
    {
        $item = $this->appts->find($id);

        if (!$item) {
            Response::json([
                'success'=>false,
                'error'=>[
                    'code'=>'NOT_FOUND',
                    'message'=>'Not found'
                ]
            ], 404);
            return;
        }

        $start = strtotime($item['start_time']);
        $end   = strtotime($item['end_time']);

        $item['formatted_date']      = date('M d, Y', $start);
        $item['formatted_time_slot'] = date('h:i A', $start) .
                                       ($end ? ' - ' . date('h:i A', $end) : '');
        $item['appointment_type']    = $item['title'];
        $item['reason']              = $item['description'];

        Response::json(['success'=>true,'data'=>$item]);
     }

     /**
      * Update Appointment
      */
     public function update(int $id): void
     {
         $auth = $_SERVER['auth'] ?? [];
         $uid  = (int)($auth['uid'] ?? 0);
         $role = $auth['role'] ?? '';

         $item = $this->appts->find($id);
         if (!$item) {
             Response::json(['success'=>false,'error'=>['code'=>'NOT_FOUND','message'=>'Not found']], 404);
             return;
         }

         // Check permission: patient or doctor associated with appointment
         if ($role === 'PATIENT' && (int)$item['patient_id'] !== $uid) {
             Response::json(['success'=>false,'error'=>['code'=>'FORBIDDEN','message'=>'Access denied']], 403);
             return;
         }
         if ($role === 'DOCTOR' && (int)$item['doctor_id'] !== $uid) {
             Response::json(['success'=>false,'error'=>['code'=>'FORBIDDEN','message'=>'Access denied']], 403);
             return;
         }

         $body = json_decode(file_get_contents('php://input'), true) ?? [];

         // Convert reason -> description if needed
         if (isset($body['reason']) && !isset($body['description'])) {
             $body['description'] = $body['reason'];
         }

         $this->appts->update($id, $body);
         $updatedItem = $this->appts->find($id);

         Response::json(['success'=>true,'data'=>$updatedItem]);
     }

     /**
      * Delete Appointment
      */
     public function delete(int $id): void
     {
         $auth = $_SERVER['auth'] ?? [];
         $uid  = (int)($auth['uid'] ?? 0);
         $role = $auth['role'] ?? '';

         $item = $this->appts->find($id);
         if (!$item) {
             Response::json(['success'=>false,'error'=>['code'=>'NOT_FOUND','message'=>'Not found']], 404);
             return;
         }

         // Check permission
         if ($role === 'PATIENT' && (int)$item['patient_id'] !== $uid) {
             Response::json(['success'=>false,'error'=>['code'=>'FORBIDDEN','message'=>'Access denied']], 403);
             return;
         }
         if ($role === 'DOCTOR' && (int)$item['doctor_id'] !== $uid) {
             Response::json(['success'=>false,'error'=>['code'=>'FORBIDDEN','message'=>'Access denied']], 403);
             return;
         }

         $this->appts->delete($id);

         Response::json(['success'=>true,'message'=>'Appointment cancelled successfully']);
     }
}
