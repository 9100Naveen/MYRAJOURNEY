<?php
require __DIR__ . '/../src/bootstrap.php';
use Src\Config\DB;

$db = DB::conn();

echo "--- USERS TABLE ---\n";
$stmt = $db->query("DESCRIBE users");
while ($row = $stmt->fetch()) {
    echo "{$row['Field']} - {$row['Type']} - NULL: {$row['Null']} - DEF: {$row['Default']}\n";
}

echo "\n--- PATIENTS TABLE ---\n";
$stmt = $db->query("DESCRIBE patients");
while ($row = $stmt->fetch()) {
    echo "{$row['Field']} - {$row['Type']} - NULL: {$row['Null']} - DEF: {$row['Default']}\n";
}
