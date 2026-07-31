<?php
declare(strict_types=1);

namespace Src\Models;

use PDO;
use Src\Config\DB;

class MetricModel
{
	private PDO $db;
	public function __construct(){ $this->db = DB::conn(); }

	public function list(int $patientId, ?string $type, ?string $from, ?string $to): array
	{
		$where='WHERE patient_id=:pid'; $p=[':pid'=>$patientId];
		if ($type) { $where.=' AND metric_type = :t'; $p[':t']=$type; }
		if ($from) { $where.=' AND recorded_at >= :f'; $p[':f']=$from; }
		if ($to) { $where.=' AND recorded_at <= :to'; $p[':to']=$to; }
		$stmt=$this->db->prepare("SELECT * FROM health_metrics $where ORDER BY recorded_at DESC");
		$stmt->execute($p);
		return $stmt->fetchAll();
	}

	public function create(array $d): int
	{
		// Handle both 'value' and 'metric_value' field names
		$value = $d['value'] ?? $d['metric_value'] ?? null;
		
		$stmt=$this->db->prepare('INSERT INTO health_metrics (patient_id, metric_type, value, unit, recorded_at, created_at) VALUES (:pid,:type,:value,:unit,:recorded_at,NOW())');
		$stmt->execute([
			':pid'=>(int)$d['patient_id'],
			':type'=>$d['metric_type'],
			':value'=>$value,
			':unit'=>$d['unit'] ?? null,
			':recorded_at'=>$d['recorded_at'],
		]);
		return (int)$this->db->lastInsertId();
	}
}




















