<?php
declare(strict_types=1);

spl_autoload_register(function ($class) {
	$prefix = 'Src\\';
	$base_dir = __DIR__ . '/';
	$len = strlen($prefix);
	if (strncmp($prefix, $class, $len) !== 0) {
		return;
	}
	$relative_class = substr($class, $len);
	$file = $base_dir . str_replace('\\', '/', $relative_class) . '.php';
	if (file_exists($file)) {
		require $file;
	}
});

// Load composer dependencies if they exist
$composerAutoload = __DIR__ . '/../vendor/autoload.php';
if (file_exists($composerAutoload)) {
    require_once $composerAutoload;
}

// Load env
Src\Utils\Env::load(__DIR__ . '/../.env');




















