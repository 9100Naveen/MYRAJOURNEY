<?php
declare(strict_types=1);

namespace Src\Models;

use PDO;
use Src\Config\DB;

class RehabModel
{
    private PDO $db;

    public function __construct()
    {
        $this->db = DB::conn();
    }

    /**
     * List all plans for a patient AND their exercises
     */
    public function plans(int $patientId): array
    {
        // 1. Get the Plans
        $stmt = $this->db->prepare(
            'SELECT * FROM rehab_plans WHERE patient_id = :pid ORDER BY updated_at DESC'
        );
        $stmt->execute([':pid' => $patientId]);
        $plans = $stmt->fetchAll();

        // 2. Loop through plans and attach exercises
        // ✅ FIX: The previous code stopped here. We need to get the details.
        foreach ($plans as &$plan) {
            $stmtEx = $this->db->prepare('SELECT * FROM rehab_exercises WHERE rehab_plan_id = :id');
            $stmtEx->execute([':id' => $plan['id']]);
            $plan['exercises'] = $stmtEx->fetchAll();
        }

        return $plans;
    }

    // ... keep the rest of your existing methods (createPlan, addExercises, planWithExercises) exactly as they are ...

    public function createPlan(array $d): int
    {
        $stmt = $this->db->prepare(
            'INSERT INTO rehab_plans (patient_id, title, description, created_at, updated_at)
             VALUES (:pid, :title, :description, NOW(), NOW())'
        );

        $stmt->execute([
            ':pid' => (int)$d['patient_id'],
            ':title' => $d['title'],
            ':description' => $d['description'] ?? null
        ]);

        return (int)$this->db->lastInsertId();
    }

    public function addExercises(int $planId, array $exercises): void
    {
        $stmt = $this->db->prepare(
            'INSERT INTO rehab_exercises
            (rehab_plan_id, name, description, reps, sets, frequency_per_week, created_at, updated_at)
            VALUES (:rid, :name, :desc, :reps, :sets, :fpw, NOW(), NOW())'
        );

        if (empty($exercises)) return;

        foreach ($exercises as $ex) {
            $stmt->execute([
                ':rid' => $planId,
                ':name' => $ex['name'],
                ':desc' => $ex['description'] ?? '',
                ':reps' => isset($ex['reps']) ? (string)$ex['reps'] : null,
                ':sets' => !empty($ex['sets']) ? (int)$ex['sets'] : null,
                ':fpw' => isset($ex['frequency_per_week']) ? (string)$ex['frequency_per_week'] : null,
            ]);
        }
    }

    public function planWithExercises(int $id): ?array
    {
        $p = $this->db->prepare('SELECT * FROM rehab_plans WHERE id = :id');
        $p->execute([':id' => $id]);
        $plan = $p->fetch();

        if (!$plan) {
            return null;
        }

        $e = $this->db->prepare('SELECT * FROM rehab_exercises WHERE rehab_plan_id = :id');
        $e->execute([':id' => $id]);

        $plan['exercises'] = $e->fetchAll();

        return $plan;
    }

    public function deleteExercise(int $exerciseId): bool
    {
        $stmt = $this->db->prepare('DELETE FROM rehab_exercises WHERE id = :id');
        return $stmt->execute([':id' => $exerciseId]);
    }
}
