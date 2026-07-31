<?php
require 'vendor/autoload.php';
require 'src/Config/DB.php';
$db = \Src\Config\DB::conn();
$stmt = $db->query('SELECT count(*) FROM ra_exercises');
print_r($stmt->fetch());
