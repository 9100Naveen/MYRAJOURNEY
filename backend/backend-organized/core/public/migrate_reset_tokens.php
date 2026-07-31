<?php
declare(strict_types=1);

require_once __DIR__ . '/../src/bootstrap.php';

use Src\Config\DB;

header('Content-Type: text/plain');

try {
    $db = DB::conn();
    
    $sql = "ALTER TABLE users 
            ADD COLUMN reset_code VARCHAR(10) NULL,
            ADD COLUMN reset_expires_at DATETIME NULL";
            
    $db->exec($sql);
    
    echo "Successfully added reset_code and reset_expires_at columns to users table.\n";
} catch (\PDOException $e) {
    if (strpos($e->getMessage(), 'Duplicate column name') !== false) {
        echo "Columns already exist.\n";
    } else {
        echo "Error: " . $e->getMessage() . "\n";
    }
}
