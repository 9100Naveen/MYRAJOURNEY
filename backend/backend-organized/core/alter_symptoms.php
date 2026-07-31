<?php
require 'src/Config/DB.php';
$db = \Src\Config\DB::conn();
try {
    $db->exec("ALTER TABLE symptom_logs ADD COLUMN IF NOT EXISTS `date` DATE NOT NULL AFTER patient_id");
    $db->exec("ALTER TABLE symptom_logs ADD COLUMN IF NOT EXISTS joint_count INT AFTER fatigue_level");
    echo "Columns added to symptom_logs successfully.\n";
} catch (Exception $e) {
    // If table doesn't exist, it's fine, it will be created with the correct columns on the next request.
    echo "Error (or table doesn't exist): " . $e->getMessage() . "\n";
}
