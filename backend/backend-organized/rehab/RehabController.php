<?php
declare(strict_types=1);

namespace Src\Controllers;

use Src\Models\RehabModel;
use Src\Models\NotificationModel;
use Src\Utils\Response;

class RehabController
{
    private RehabModel $rehab;

    public function __construct()
    {
        $this->rehab = new RehabModel();
    }

    /**
     * List rehab plans for a patient
     */
    public function listForPatient(): void
    {
        $auth = $_SERVER['auth'] ?? [];
        $uid = (int)($auth['uid'] ?? 0);
        $role = $auth['role'] ?? '';

        // PATIENT role = auto use UID
        if ($role === 'PATIENT') {
            $pid = $uid;
        } else {
            // Doctor/Admin must include patient_id in query
            $pid = (int)($_GET['patient_id'] ?? 0);
        }

        if ($pid === 0) {
            Response::json([
                'success' => false,
                'error' => [
                    'code' => 'VALIDATION',
                    'message' => 'patient_id required'
                ]
            ], 422);
            return;
        }

        $data = $this->rehab->plans($pid);

        Response::json([
            'success' => true,
            'data' => $data
        ]);
    }

    /**
     * Create a rehab plan with exercises
     */
    public function createPlan(): void
    {
        $body = json_decode(file_get_contents('php://input'), true) ?? [];

        if (empty($body['patient_id']) || empty($body['title'])) {
            Response::json([
                'success' => false,
                'error' => [
                    'code' => 'VALIDATION',
                    'message' => 'Missing patient_id or title'
                ]
            ], 422);
            return;
        }

        $planId = $this->rehab->createPlan($body);

        if (!empty($body['exercises']) && is_array($body['exercises'])) {
            $this->rehab->addExercises($planId, $body['exercises']);
        }

        try {
            (new NotificationModel())->create(
                (int)$body['patient_id'],
                'REHAB',
                'Rehab plan updated',
                (string)$body['title']
            );
        } catch (\Throwable $e) {
            // Notification failure ignored safely
        }

        $output = $this->rehab->planWithExercises($planId);

        Response::json([
            'success' => true,
            'data' => $output
        ], 201);
    }

    /**
     * Fetch single plan with exercises
     */
    public function getPlan(int $id): void
    {
        $item = $this->rehab->planWithExercises($id);

        if (!$item) {
            Response::json([
                'success' => false,
                'error' => [
                    'code' => 'NOT_FOUND',
                    'message' => 'Plan not found'
                ]
            ], 404);
            return;
        }

        Response::json([
            'success' => true,
            'data' => $item
        ]);
    }
    /**
     * Delete an exercise from a rehab plan
     */
    public function deleteExercise(int $id): void
    {
        $auth = $_SERVER['auth'] ?? [];
        $role = $auth['role'] ?? '';

        if ($role !== 'DOCTOR' && $role !== 'ADMIN') {
            Response::json([
                'success' => false,
                'error' => [
                    'code' => 'UNAUTHORIZED',
                    'message' => 'Only doctors or admins can delete exercises'
                ]
            ], 403);
            return;
        }

        $success = $this->rehab->deleteExercise($id);

        if (!$success) {
            Response::json([
                'success' => false,
                'error' => [
                    'code' => 'NOT_FOUND',
                    'message' => 'Exercise not found or could not be deleted'
                ]
            ], 404);
            return;
        }

        Response::json([
            'success' => true,
            'message' => 'Exercise deleted'
        ]);
    }
}
