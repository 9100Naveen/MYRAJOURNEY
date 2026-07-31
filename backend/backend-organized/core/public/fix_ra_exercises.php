<?php
// Save this as: backend/backend-organized/core/public/fix_ra_exercises.php
require __DIR__ . '/../src/bootstrap.php';
use Src\Config\DB;

try {
    $db = DB::conn();
    echo "Connected to database successfully...<br>";

    // 1. Create ra_exercises table
    echo "Creating 'ra_exercises' table...<br>";
    $sql = "
    CREATE TABLE IF NOT EXISTS ra_exercises (
        id INT AUTO_INCREMENT PRIMARY KEY,
        name VARCHAR(255) NOT NULL,
        description TEXT,
        category VARCHAR(100),
        target_joints JSON,
        difficulty_level ENUM('BEGINNER', 'INTERMEDIATE', 'ADVANCED') DEFAULT 'BEGINNER',
        video_url TEXT,
        animation_url TEXT,
        instructions JSON,
        ra_benefits JSON,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
    ";
    
    $db->exec($sql);
    echo "✅ 'ra_exercises' table created or already exists.<br>";

    // 2. Seed exercises
    echo "Checking if 'ra_exercises' needs seeding...<br>";
    $stmt = $db->query("SELECT COUNT(*) FROM ra_exercises");
    $count = $stmt->fetchColumn();

    if ($count == 0) {
        echo "Seeding 10 exercises...<br>";
        
        $exercises = [
            [
                'name' => 'Wrist Flexion/Extension',
                'description' => 'Gentle wrist movement to improve flexibility and reduce stiffness in wrist joints',
                'category' => 'WRIST',
                'target_joints' => ['Wrist', 'Forearm'],
                'difficulty_level' => 'BEGINNER',
                'video_url' => 'http://14.139.187.229:8081/sept_batch2025/spic726/myrajourney/public/exercise_videos/ex_001_wrist_flexion.mp4',
                'animation_url' => 'animation_wrist_flex.gif',
                'instructions' => ['Sit comfortably with your arm supported', 'Slowly bend your wrist up and down', 'Hold for 2-3 seconds at each position', 'Repeat 10-15 times'],
                'ra_benefits' => ['Reduces wrist stiffness common in RA', 'Improves range of motion', 'Helps maintain joint function']
            ],
            [
                'name' => 'Wrist Rotation (Clockwise/Counterclockwise)',
                'description' => 'Circular wrist movements to maintain joint mobility and reduce morning stiffness',
                'category' => 'WRIST',
                'target_joints' => ['Wrist', 'Radius', 'Ulna'],
                'difficulty_level' => 'BEGINNER',
                'video_url' => 'http://14.139.187.229:8081/sept_batch2025/spic726/myrajourney/public/exercise_videos/ex_002_wrist_rotation.mp4',
                'animation_url' => 'animation_wrist_rotation.gif',
                'instructions' => ['Extend your arm in front of you', 'Make slow circles with your wrist', 'Rotate 10 times clockwise', 'Rotate 10 times counterclockwise'],
                'ra_benefits' => ['Maintains wrist joint mobility', 'Reduces morning stiffness', 'Improves circulation in wrist area']
            ],
            [
                'name' => 'Thumb Opposition Exercise',
                'description' => 'Thumb-to-finger touching exercise to maintain thumb mobility and grip strength',
                'category' => 'THUMB',
                'target_joints' => ['Thumb', 'CMC Joint', 'Fingers'],
                'difficulty_level' => 'BEGINNER',
                'video_url' => 'http://14.139.187.229:8081/sept_batch2025/spic726/myrajourney/public/exercise_videos/ex_003_thumb_opposition.mp4',
                'animation_url' => 'animation_thumb_opposition.gif',
                'instructions' => ['Touch your thumb to each fingertip', 'Start with index finger, move to pinky', 'Hold each touch for 2 seconds', 'Repeat sequence 5-10 times'],
                'ra_benefits' => ['Maintains thumb joint flexibility', 'Improves grip strength', 'Helps with daily activities like writing']
            ],
            [
                'name' => 'Thumb Flexion/Extension',
                'description' => 'Thumb bending exercise to improve thumb joint range of motion',
                'category' => 'THUMB',
                'target_joints' => ['Thumb', 'MCP Joint', 'IP Joint'],
                'difficulty_level' => 'BEGINNER',
                'video_url' => 'http://14.139.187.229:8081/sept_batch2025/spic726/myrajourney/public/exercise_videos/ex_004_thumb_flexion.mp4',
                'animation_url' => 'animation_thumb_flex.gif',
                'instructions' => ['Keep your hand flat on a table', 'Slowly bend your thumb toward your palm', 'Straighten your thumb back up', 'Repeat 10-15 times'],
                'ra_benefits' => ['Improves thumb joint mobility', 'Reduces thumb stiffness', 'Helps maintain pinch strength']
            ],
            [
                'name' => 'Finger Flexion (Making a Fist)',
                'description' => 'Gentle fist-making exercise to maintain finger joint flexibility',
                'category' => 'FINGER',
                'target_joints' => ['Fingers', 'MCP Joints', 'PIP Joints', 'DIP Joints'],
                'difficulty_level' => 'BEGINNER',
                'video_url' => 'http://14.139.187.229:8081/sept_batch2025/spic726/myrajourney/public/exercise_videos/ex_005_finger_flexion.mp4',
                'animation_url' => 'animation_finger_flex.gif',
                'instructions' => ['Start with fingers straight and spread', 'Slowly curl fingers into a loose fist', 'Don\'t squeeze tightly', 'Hold for 3 seconds, then open', 'Repeat 10 times'],
                'ra_benefits' => ['Maintains finger joint flexibility', 'Improves grip strength gradually', 'Reduces finger stiffness']
            ],
            [
                'name' => 'Finger Extension/Spreading',
                'description' => 'Finger spreading exercise to improve finger extension and reduce joint contractures',
                'category' => 'FINGER',
                'target_joints' => ['Fingers', 'MCP Joints', 'Interosseous muscles'],
                'difficulty_level' => 'BEGINNER',
                'video_url' => 'http://14.139.187.229:8081/sept_batch2025/spic726/myrajourney/public/exercise_videos/ex_006_finger_extension.mp4',
                'animation_url' => 'animation_finger_spread.gif',
                'instructions' => ['Place your hand flat on a table', 'Spread your fingers as wide as comfortable', 'Hold for 5 seconds', 'Relax and repeat 10 times'],
                'ra_benefits' => ['Prevents finger contractures', 'Improves finger extension', 'Maintains hand span for gripping']
            ],
            [
                'name' => 'Finger Pinch Strengthening',
                'description' => 'Gentle pinching exercise using therapy putty or soft objects to maintain pinch strength',
                'category' => 'FINGER',
                'target_joints' => ['Thumb', 'Index finger', 'Pinch muscles'],
                'difficulty_level' => 'INTERMEDIATE',
                'video_url' => 'http://14.139.187.229:8081/sept_batch2025/spic726/myrajourney/public/exercise_videos/ex_007_finger_pinch.mp4',
                'animation_url' => 'animation_finger_pinch.gif',
                'instructions' => ['Use therapy putty or soft ball', 'Pinch between thumb and index finger', 'Hold for 3 seconds', 'Release slowly', 'Repeat 10-15 times'],
                'ra_benefits' => ['Maintains pinch strength for daily tasks', 'Improves fine motor control', 'Helps with buttoning and writing']
            ],
            [
                'name' => 'Knee Flexion/Extension (Seated)',
                'description' => 'Seated knee straightening exercise to maintain knee joint mobility and quadriceps strength',
                'category' => 'KNEE',
                'target_joints' => ['Knee', 'Quadriceps', 'Hamstrings'],
                'difficulty_level' => 'BEGINNER',
                'video_url' => 'http://14.139.187.229:8081/sept_batch2025/spic726/myrajourney/public/exercise_videos/ex_008_knee_flexion.mp4',
                'animation_url' => 'animation_knee_seated.gif',
                'instructions' => ['Sit in a chair with back support', 'Slowly straighten one leg', 'Hold for 3-5 seconds', 'Lower leg slowly', 'Repeat 10 times each leg'],
                'ra_benefits' => ['Maintains knee joint mobility', 'Strengthens quadriceps muscles', 'Reduces knee stiffness']
            ],
            [
                'name' => 'Hip Flexion (Seated/Standing)',
                'description' => 'Hip lifting exercise to maintain hip joint flexibility and hip flexor strength',
                'category' => 'HIP',
                'target_joints' => ['Hip', 'Hip flexors', 'Psoas'],
                'difficulty_level' => 'INTERMEDIATE',
                'video_url' => 'http://14.139.187.229:8081/sept_batch2025/spic726/myrajourney/public/exercise_videos/ex_009_hip_flexion.mp4',
                'animation_url' => 'animation_hip_flexion.gif',
                'instructions' => ['Sit or stand with support if needed', 'Slowly lift one knee toward chest', 'Hold for 3 seconds', 'Lower slowly', 'Repeat 10 times each leg'],
                'ra_benefits' => ['Maintains hip joint mobility', 'Improves walking ability', 'Reduces hip stiffness']
            ],
            [
                'name' => 'Hip Abduction (Side-lying/Standing)',
                'description' => 'Side leg lifting exercise to strengthen hip abductor muscles and improve stability',
                'category' => 'HIP',
                'target_joints' => ['Hip', 'Gluteus medius', 'Hip abductors'],
                'difficulty_level' => 'INTERMEDIATE',
                'video_url' => 'http://14.139.187.229:8081/sept_batch2025/spic726/myrajourney/public/exercise_videos/ex_010_hip_abduction.mp4',
                'animation_url' => 'animation_hip_abduction.gif',
                'instructions' => ['Lie on your side or stand with support', 'Slowly lift top leg to the side', 'Keep leg straight', 'Hold for 3 seconds', 'Lower slowly, repeat 10 times'],
                'ra_benefits' => ['Strengthens hip stabilizer muscles', 'Improves balance and stability', 'Reduces hip pain during walking']
            ]
        ];

        $stmt = $db->prepare("
            INSERT INTO ra_exercises 
            (name, description, category, target_joints, difficulty_level, video_url, animation_url, instructions, ra_benefits) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        ");

        foreach ($exercises as $exercise) {
            $stmt->execute([
                $exercise['name'],
                $exercise['description'],
                $exercise['category'],
                json_encode($exercise['target_joints']),
                $exercise['difficulty_level'],
                $exercise['video_url'],
                $exercise['animation_url'],
                json_encode($exercise['instructions']),
                json_encode($exercise['ra_benefits'])
            ]);
        }
        echo "✅ Successfully seeded 10 RA exercises.<br>";
    } else {
        echo "ℹ️ Seeding skipped: table already has $count exercises.<br>";
    }

    echo "<h3>Database setup for ra_exercises complete!</h3>";

} catch (Exception $e) {
    echo "<h1>Error:</h1> " . $e->getMessage();
}
