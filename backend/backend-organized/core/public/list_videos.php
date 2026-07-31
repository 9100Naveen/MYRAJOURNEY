<?php
// Save this as: backend/backend-organized/core/public/list_videos.php
header('Content-Type: text/plain');

echo "=== System Path Scan ===\n";
echo "Current directory: " . __DIR__ . "\n";
echo "Document root: " . ($_SERVER['DOCUMENT_ROOT'] ?? 'Not set') . "\n\n";

// Function to recursively scan for MP4 files
function scanForVideos($dir, &$results, $depth = 0, $maxDepth = 5) {
    if ($depth > $maxDepth) return;
    if (!is_dir($dir)) return;
    
    $files = @scandir($dir);
    if ($files === false) return;
    
    foreach ($files as $file) {
        if ($file === '.' || $file === '..') continue;
        
        $path = $dir . DIRECTORY_SEPARATOR . $file;
        if (is_dir($path)) {
            // Avoid scanning massive directories like node_modules or vendor
            if (in_array(strtolower($file), ['node_modules', 'vendor', '.git', 'deriveddata'])) continue;
            scanForVideos($path, $results, $depth + 1, $maxDepth);
        } else {
            $ext = strtolower(pathinfo($path, PATHINFO_EXTENSION));
            if ($ext === 'mp4' || $ext === 'mov') {
                $results[] = [
                    'path' => $path,
                    'size' => filesize($path),
                    'readable' => is_readable($path)
                ];
            }
        }
    }
}

// 1. Scan current directory (public)
echo "Scanning public directory...\n";
$videos = [];
scanForVideos(__DIR__, $videos);
if (empty($videos)) {
    echo "No videos found in public/\n\n";
} else {
    foreach ($videos as $v) {
        echo "Found: " . $v['path'] . " (" . round($v['size'] / 1024 / 1024, 2) . " MB) - Readable: " . ($v['readable'] ? 'Yes' : 'No') . "\n";
    }
    echo "\n";
}

// 2. Scan parent directories
echo "Scanning parent directories...\n";
$parentVideos = [];
$parentDir = dirname(__DIR__); // core/
$grandparentDir = dirname($parentDir); // backend-organized/
$rootAppDir = dirname($grandparentDir); // myrajourney/ (usually contains the app folder too)

echo "Scanning from root app directory: $rootAppDir\n";
scanForVideos($rootAppDir, $parentVideos, 0, 4);

if (empty($parentVideos)) {
    echo "No videos found in root app directory structure.\n";
} else {
    foreach ($parentVideos as $v) {
        echo "Found: " . $v['path'] . " (" . round($v['size'] / 1024 / 1024, 2) . " MB) - Readable: " . ($v['readable'] ? 'Yes' : 'No') . "\n";
    }
}

echo "\nScan complete!\n";
