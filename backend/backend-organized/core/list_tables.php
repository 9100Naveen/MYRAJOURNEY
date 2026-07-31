<?php
require 'src/Config/DB.php';
$db = \Src\Config\DB::conn();
print_r($db->query('SHOW TABLES')->fetchAll(PDO::FETCH_COLUMN));
