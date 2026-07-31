<?php
declare(strict_types=1);

namespace Src\Config;

use PDO;
use PDOException;
use Src\Utils\Response;

class DB
{
	private static ?PDO $pdo = null;
	private static int $reconnectAttempts = 0;
	private static int $maxReconnectAttempts = 3;

	public static function conn(): PDO
	{
		if (self::$pdo && self::isConnectionAlive()) {
			return self::$pdo;
		}
		
		return self::createConnection();
	}
	
	private static function createConnection(): PDO
	{
		$dsn = sprintf('mysql:host=%s;port=%s;dbname=%s;charset=utf8mb4',
			Config::get('DB_HOST', '127.0.0.1'),
			Config::get('DB_PORT', '3306'),
			Config::get('DB_NAME', 'myrajourney')
		);
		
		try {
			self::$pdo = new PDO($dsn, Config::get('DB_USER', 'root'), Config::get('DB_PASS', ''), [
				PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
				PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
				PDO::ATTR_TIMEOUT => 30,
				PDO::ATTR_EMULATE_PREPARES => false,
				PDO::ATTR_STRINGIFY_FETCHES => false,
				PDO::MYSQL_ATTR_INIT_COMMAND => "SET sql_mode='STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION'",
				PDO::ATTR_PERSISTENT => false, // Disable persistent connections to avoid "gone away" issues
			]);
			
			self::$reconnectAttempts = 0;
			return self::$pdo;
		} catch (PDOException $e) {
			error_log('DB connection failed: ' . $e->getMessage());
			
			if (self::$reconnectAttempts < self::$maxReconnectAttempts) {
				self::$reconnectAttempts++;
				sleep(1); // Wait 1 second before retry
				return self::createConnection();
			}
			
			Response::json([
				'success' => false,
				'error' => [
					'code' => 'DB_CONNECTION_FAILED',
					'message' => 'Database connection failed after ' . self::$maxReconnectAttempts . ' attempts'
				]
			], 500);
			exit;
		}
	}
	
	private static function isConnectionAlive(): bool
	{
		try {
			if (!self::$pdo) return false;
			
			// Simple query to check if connection is alive
			self::$pdo->query('SELECT 1');
			return true;
		} catch (PDOException $e) {
			error_log('DB connection check failed: ' . $e->getMessage());
			self::$pdo = null;
			return false;
		}
	}
	
	public static function reconnect(): PDO
	{
		self::$pdo = null;
		return self::createConnection();
	}
}




















