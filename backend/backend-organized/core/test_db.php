<?php
require 'vendor/autoload.php';
require 'src/Config/DB.php';
$db = \Src\Config\DB::conn();
$stmt = $db->query('SELECT * FROM exercise_assignments');
print_r($stmt->fetchAll(PDO::FETCH_ASSOC));
