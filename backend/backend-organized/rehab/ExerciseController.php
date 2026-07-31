<?php

namespace Src\Controllers;

use Src\Models\ExerciseModel;
use Src\Utils\Response;
use Src\Middlewares\Auth;

class ExerciseController
{
    private $exerciseModel;

    public function __construct()
    {
        $this->exerciseModel = new ExerciseModel();
    }

    /**
     * Get all RA exercises from the library
     * GET /api/v1/exercises
     */
    public function getAllExercises()
    {
        try {
            Auth::requireAuth();
            
            $exercises = $this->exerciseModel->getAllExercises();
            
            Response::json([
                'success' => true,
                'data' => $exercises,
                'message' => 'Exercises retrieved successfully'
            ]);
            
        } catch (\Exception $e) {
            Response::json([
                'success' => false,
                'message' => 'Failed to retrieve exercises: ' . $e->getMessage()
            ], 500);
        }
    }

    /**
     * Create exercise assignment
     * POST /api/v1/exercise-assignments
     */
    public function createAssignment()
    {
        try {
            Auth::requireAuth();
            $user = $_SERVER['auth'];
            
            // Only doctors can create assignments
            if ($user['role'] !== 'DOCTOR') {
                Response::json([
                    'success' => false,
                    'message' => 'Only doctors can assign exercises'
                ], 403);
                return;
            }
            
            $data = json_decode(file_get_contents('php://input'), true);
            
            // Validate required fields
            if (!isset($data['patient_id']) || !isset($data['exercise_ids'])) {
                Response::json([
                    'success' => false,
                    'message' => 'Missing required fields: patient_id, exercise_ids'
                ], 400);
                return;
            }
            
            $assignmentData = [
                'id' => $data['id'] ?? uniqid('assign_', true),
                'doctor_id' => $user['uid'],
                'patient_id' => $data['patient_id'],
                'exercise_ids' => json_encode($data['exercise_ids']),
                'notes' => $data['notes'] ?? null,
                'assigned_date' => date('Y-m-d H:i:s')
            ];
            
            $result = $this->exerciseModel->createAssignment($assignmentData);
            
            if ($result) {
                Response::json([
                    'success' => true,
                    'data' => ['assignment_id' => $assignmentData['id']],
                    'message' => 'Exercise assignment created successfully'
                ], 201);
            } else {
                Response::json([
                    'success' => false,
                    'message' => 'Failed to create assignment'
                ], 500);
            }
            
        } catch (\Exception $e) {
            Response::json([
                'success' => false,
                'message' => 'Failed to create assignment: ' . $e->getMessage()
            ], 500);
        }
    }

    /**
     * Create a new exercise in the library
     * POST /api/v1/exercises
     */
    public function createExercise()
    {
        try {
            Auth::requireAuth();
            $user = $_SERVER['auth'];
            
            // Only admins and doctors can create exercises
            if (!in_array($user['role'], ['ADMIN', 'DOCTOR'])) {
                Response::json([
                    'success' => false,
                    'message' => 'Unauthorized: Only admins and doctors can create exercises'
                ], 403);
                return;
            }
            
            $data = json_decode(file_get_contents('php://input'), true);
            
            if (!isset($data['name']) || !isset($data['category'])) {
                Response::json([
                    'success' => false,
                    'message' => 'Missing required fields: name, category'
                ], 400);
                return;
            }
            
            // Encode JSON fields if they are arrays
            if (isset($data['target_joints']) && is_array($data['target_joints'])) {
                $data['target_joints'] = json_encode($data['target_joints']);
            }
            if (isset($data['instructions']) && is_array($data['instructions'])) {
                $data['instructions'] = json_encode($data['instructions']);
            }
            if (isset($data['ra_benefits']) && is_array($data['ra_benefits'])) {
                $data['ra_benefits'] = json_encode($data['ra_benefits']);
            }
            
            $result = $this->exerciseModel->createExercise($data);
            
            if ($result) {
                Response::json([
                    'success' => true,
                    'message' => 'Exercise created successfully'
                ], 201);
            } else {
                Response::json([
                    'success' => false,
                    'message' => 'Failed to create exercise'
                ], 500);
            }
            
        } catch (\Exception $e) {
            Response::json([
                'success' => false,
                'message' => 'Error: ' . $e->getMessage()
            ], 500);
        }
    }

    /**
     * Get exercises by category
     * GET /api/v1/exercises/category/{category}
     */
    public function getExercisesByCategory($category)
    {
        try {
            Auth::requireAuth();
            
            $validCategories = ['WRIST', 'THUMB', 'FINGER', 'KNEE', 'HIP'];
            if (!in_array(strtoupper($category), $validCategories)) {
                Response::json([
                    'success' => false,
                    'message' => 'Invalid category'
                ], 400);
                return;
            }
            
            $exercises = $this->exerciseModel->getExercisesByCategory(strtoupper($category));
            
            Response::json([
                'success' => true,
                'data' => $exercises,
                'message' => 'Exercises retrieved successfully'
            ]);
            
        } catch (\Exception $e) {
            Response::json([
                'success' => false,
                'message' => 'Failed to retrieve exercises: ' . $e->getMessage()
            ], 500);
        }
    }

    /**
     * Get exercise by ID
     * GET /api/v1/exercises/{id}
     */
    public function getExerciseById($id)
    {
        try {
            Auth::requireAuth();
            
            $exercise = $this->exerciseModel->getExerciseById($id);
            
            if (!$exercise) {
                Response::json([
                    'success' => false,
                    'message' => 'Exercise not found'
                ], 404);
                return;
            }
            
            Response::json([
                'success' => true,
                'data' => $exercise,
                'message' => 'Exercise retrieved successfully'
            ]);
            
        } catch (\Exception $e) {
            Response::json([
                'success' => false,
                'message' => 'Failed to retrieve exercise: ' . $e->getMessage()
            ], 500);
        }
    }

    /**
     * Get patient assignments
     * GET /api/v1/exercise-assignments/patient
     */
    public function getPatientAssignments()
    {
        try {
            Auth::requireAuth();
            $user = $_SERVER['auth'];
            
            $patientId = $_GET['patient_id'] ?? $user['uid'];
            
            // Patients can only view their own assignments
            if ($user['role'] === 'PATIENT' && $patientId != $user['uid']) {
                Response::json([
                    'success' => false,
                    'message' => 'Unauthorized access'
                ], 403);
                return;
            }
            
            $assignments = $this->exerciseModel->getPatientAssignments($patientId);
            
            Response::json([
                'success' => true,
                'data' => $assignments,
                'message' => 'Assignments retrieved successfully'
            ]);
            
        } catch (\Exception $e) {
            Response::json([
                'success' => false,
                'message' => 'Failed to retrieve assignments: ' . $e->getMessage()
            ], 500);
        }
    }

    /**
     * Get doctor assignments
     * GET /api/v1/exercise-assignments/doctor
     */
    public function getDoctorAssignments()
    {
        try {
            Auth::requireAuth();
            $user = $_SERVER['auth'];
            
            if ($user['role'] !== 'DOCTOR') {
                Response::json([
                    'success' => false,
                    'message' => 'Only doctors can access this endpoint'
                ], 403);
                return;
            }
            
            $doctorId = $_GET['doctor_id'] ?? $user['uid'];
            
            $assignments = $this->exerciseModel->getDoctorAssignments($doctorId);
            
            Response::json([
                'success' => true,
                'data' => $assignments,
                'message' => 'Assignments retrieved successfully'
            ]);
            
        } catch (\Exception $e) {
            Response::json([
                'success' => false,
                'message' => 'Failed to retrieve assignments: ' . $e->getMessage()
            ], 500);
        }
    }

    /**
     * Update assignment
     * PUT /api/v1/exercise-assignments/{id}
     */
    public function updateAssignment($id)
    {
        try {
            Auth::requireAuth();
            $user = $_SERVER['auth'];
            
            if ($user['role'] !== 'DOCTOR') {
                Response::json([
                    'success' => false,
                    'message' => 'Only doctors can update assignments'
                ], 403);
                return;
            }
            
            $data = json_decode(file_get_contents('php://input'), true);
            
            $updateData = [
                'exercise_ids' => json_encode($data['exercise_ids']),
                'notes' => $data['notes'] ?? null
            ];
            
            $result = $this->exerciseModel->updateAssignment($id, $updateData);
            
            if ($result) {
                Response::json([
                    'success' => true,
                    'message' => 'Assignment updated successfully'
                ]);
            } else {
                Response::json([
                    'success' => false,
                    'message' => 'Failed to update assignment'
                ], 500);
            }
            
        } catch (\Exception $e) {
            Response::json([
                'success' => false,
                'message' => 'Failed to update assignment: ' . $e->getMessage()
            ], 500);
        }
    }

    /**
     * Delete assignment
     * DELETE /api/v1/exercise-assignments/{id}
     */
    public function deleteAssignment($id)
    {
        try {
            Auth::requireAuth();
            $user = $_SERVER['auth'];
            
            if ($user['role'] !== 'DOCTOR') {
                Response::json([
                    'success' => false,
                    'message' => 'Only doctors can delete assignments'
                ], 403);
                return;
            }
            
            $result = $this->exerciseModel->deleteAssignment($id);
            
            if ($result) {
                Response::json([
                    'success' => true,
                    'message' => 'Assignment deleted successfully'
                ]);
            } else {
                Response::json([
                    'success' => false,
                    'message' => 'Failed to delete assignment'
                ], 500);
            }
            
        } catch (\Exception $e) {
            Response::json([
                'success' => false,
                'message' => 'Failed to delete assignment: ' . $e->getMessage()
            ], 500);
        }
    }
}
