<?php
declare(strict_types=1);

namespace Src\Models;

use PDO;
use Src\Config\DB;

class AppointmentModel
{
    private PDO $db;

    public function __construct() {
        $this->db = DB::conn();
    }

    /**
     * List appointments with filters
     */
    public function list(array $filters, int $page = 1, int $limit = 20): array
    {
        $w = [];
        $p = [];

        if (!empty($filters['patient_id'])) {
            $w[] = 'a.patient_id = :pid';
            $p[':pid'] = (int)$filters['patient_id'];
        }
        if (!empty($filters['doctor_id'])) {
            $w[] = 'a.doctor_id = :did';
            $p[':did'] = (int)$filters['doctor_id'];
        }

        $where = $w ? ('WHERE ' . implode(' AND ', $w)) : '';

        // Count query
        $countSql  = "SELECT COUNT(*) FROM appointments a $where";
        $stmtCount = $this->db->prepare($countSql);
        foreach ($p as $k => $v) {
            $stmtCount->bindValue($k, $v, PDO::PARAM_INT);
        }
        $stmtCount->execute();
        $total = (int)$stmtCount->fetchColumn();

        // Main list query
        $offset = ($page - 1) * $limit;

        $sql = "
            SELECT
                a.*,
                pu.name AS patient_name,
                du.name AS doctor_name,

                a.title       AS appointment_type,
                a.description AS reason

            FROM appointments a
            LEFT JOIN users pu ON a.patient_id = pu.id
            LEFT JOIN users du ON a.doctor_id = du.id
            $where
            ORDER BY a.start_time ASC
            LIMIT :lim OFFSET :off
        ";

        $stmt = $this->db->prepare($sql);

        foreach ($p as $k => $v) {
            $stmt->bindValue($k, $v, PDO::PARAM_INT);
        }

        $stmt->bindValue(':lim', $limit, PDO::PARAM_INT);
        $stmt->bindValue(':off', $offset, PDO::PARAM_INT);

        $stmt->execute();

        return [
            'items' => $stmt->fetchAll(),
            'total' => $total
        ];
    }

    /**
     * Create appointment
     */
    public function create(array $d): int
    {
        $stmt = $this->db->prepare("
            INSERT INTO appointments
                (patient_id, doctor_id, title, description, location, start_time, end_time, status, created_at, updated_at)
            VALUES
                (:patient_id, :doctor_id, :title, :description, :location, :start_time, :end_time, 'SCHEDULED', NOW(), NOW())
        ");

        $stmt->execute([
            ':patient_id' => (int)$d['patient_id'],
            ':doctor_id'  => (int)$d['doctor_id'],
            ':title'      => $d['title'],
            ':description'=> $d['description'] ?? null,
            ':location'   => $d['location'] ?? null,
            ':start_time' => $d['start_time'],
            ':end_time'   => $d['end_time']
        ]);

        return (int)$this->db->lastInsertId();
    }

    /**
     * Find appointment
     */
    public function find(int $id): ?array
    {
        $sql = "
            SELECT
                a.*,
                pu.name AS patient_name,
                du.name AS doctor_name,
                a.title       AS appointment_type,
                a.description AS reason
            FROM appointments a
            LEFT JOIN users pu ON a.patient_id = pu.id
            LEFT JOIN users du ON a.doctor_id = du.id
            WHERE a.id = :id
        ";

        $stmt = $this->db->prepare($sql);
        $stmt->execute([':id' => $id]);

        $row = $stmt->fetch();
        return $row ?: null;
    }

    /**
     * Update appointment
     */
    public function update(int $id, array $d): bool
    {
        $fields = [];
        $params = [':id' => $id];

        if (isset($d['title'])) {
            $fields[] = 'title = :title';
            $params[':title'] = $d['title'];
        }
        if (isset($d['description'])) {
            $fields[] = 'description = :description';
            $params[':description'] = $d['description'];
        }
        if (isset($d['start_time'])) {
            $fields[] = 'start_time = :start_time';
            $params[':start_time'] = $d['start_time'];
        }
        if (isset($d['end_time'])) {
            $fields[] = 'end_time = :end_time';
            $params[':end_time'] = $d['end_time'];
        }
        if (isset($d['status'])) {
            $fields[] = 'status = :status';
            $params[':status'] = $d['status'];
        }

        if (empty($fields)) {
            return false;
        }

        $fields[] = 'updated_at = NOW()';
        $sql = "UPDATE appointments SET " . implode(', ', $fields) . " WHERE id = :id";
        $stmt = $this->db->prepare($sql);
        return $stmt->execute($params);
    }

    /**
     * Delete appointment
     */
    public function delete(int $id): bool
    {
        $stmt = $this->db->prepare("DELETE FROM appointments WHERE id = :id");
        return $stmt->execute([':id' => $id]);
    }
}
