<?php
declare(strict_types=1);

namespace Src\Models;

use PDO;
use Src\Config\DB;

class SettingsModel
{
	private PDO $db;
	public function __construct(){ $this->db = DB::conn(); }

	public function getAll(int $userId): array
	{
		$stmt=$this->db->prepare('SELECT `key`,`value` FROM settings WHERE user_id=:u');
		$stmt->execute([':u'=>$userId]);
		$out=[]; foreach($stmt->fetchAll() as $row){ $out[$row['key']]=$row['value']; }
		return $out;
	}

	public function put(int $userId, string $key, ?string $value): void
	{
		$stmt=$this->db->prepare('INSERT INTO settings (user_id, `key`, `value`, updated_at) VALUES (:u,:k,:v,NOW()) ON DUPLICATE KEY UPDATE `value`=VALUES(`value`), updated_at=VALUES(updated_at)');
		$stmt->execute([':u'=>$userId,':k'=>$key,':v'=>$value]);
	}
}




















