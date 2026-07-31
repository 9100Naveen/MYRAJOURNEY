<?php
require __DIR__ . '/vendor/autoload.php';

// Try to load .env
if (file_exists(__DIR__ . '/.env')) {
    $lines = file(__DIR__ . '/.env', FILE_IGNORE_NEW_LINES | FILE_SKIP_EMPTY_LINES);
    foreach ($lines as $line) {
        if (strpos(trim($line), '#') === 0) continue;
        list($name, $value) = explode('=', $line, 2);
        $_ENV[trim($name)] = trim($value);
    }
}

require_DIR__ . '/src/utils/OpenAI.php';
// We need to require other classes if they are not autoloaded, but since vendor/autoload is there it might work.
// Let's just create a raw OpenAI instance and test it
$openai = new \Src\Utils\OpenAI();
try {
    echo "Testing OpenRouter API...\n";
    $resp = $openai->getChatResponse("Hello, what is RA? One sentence.");
    echo "SUCCESS: " . $resp . "\n";
} catch (\Exception $e) {
    echo "FAILED: " . $e->getMessage() . "\n";
}
