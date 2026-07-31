<?php
require 'src/Config/DB.php';
$db = \Src\Config\DB::conn();
try {
    $db->exec("CREATE TABLE IF NOT EXISTS health_metrics (
        id INT PRIMARY KEY AUTO_INCREMENT,
        patient_id INT NOT NULL,
        metric_type VARCHAR(50) NOT NULL,
        value VARCHAR(50) NOT NULL,
        unit VARCHAR(20),
        recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE
    )");
    echo "health_metrics created.\n";
    
    $db->exec("CREATE TABLE IF NOT EXISTS symptom_logs (
        id INT PRIMARY KEY AUTO_INCREMENT,
        patient_id INT NOT NULL,
        pain_level INT NOT NULL,
        stiffness_level INT,
        fatigue_level INT,
        notes TEXT,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE
    )");
    echo "symptom_logs checked/created.\n";
} catch (Exception $e) {
    echo "Error: " . $e->getMessage();
}
