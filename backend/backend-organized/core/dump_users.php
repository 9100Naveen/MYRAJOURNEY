<?php
require __DIR__ . '/src/bootstrap.php';
use Src\Config\DB;
$pdo = DB::conn();
$stmt = $pdo->query("SELECT id, name, email, role FROM users");
$users = $stmt->fetchAll(PDO::FETCH_ASSOC);
print_r($users);
