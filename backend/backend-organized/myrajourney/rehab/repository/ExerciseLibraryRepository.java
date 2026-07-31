package com.example.myrajourney.rehab.repository;

import com.example.myrajourney.rehab.models.ExerciseCategory;
import com.example.myrajourney.rehab.models.RAExercise;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Repository class containing the default library of 10 RA-specific exercises
 */
public class ExerciseLibraryRepository {

        private static ExerciseLibraryRepository instance;
        private List<RAExercise> exercises;

        private ExerciseLibraryRepository() {
                initializeDefaultExercises();
        }

        public static synchronized ExerciseLibraryRepository getInstance() {
                if (instance == null) {
                        instance = new ExerciseLibraryRepository();
                }
                return instance;
        }

        /**
         * Initialize the default library of 10 RA-specific exercises with specific
         * video URLs
         */
        private void initializeDefaultExercises() {
                exercises = new ArrayList<>();

                // 1. Wrist Flexion/Extension
                exercises.add(new RAExercise(
                                "ex_001",
                                "Wrist Flexion/Extension",
                                "Gentle wrist movement to improve flexibility and reduce stiffness in wrist joints",
                                ExerciseCategory.WRIST,
                                Arrays.asList("Wrist", "Forearm"),
                                1,
                                "file:///android_asset/exercise_videos/ex_001_wrist_flexion.mp4",
                                "animation_wrist_flex.gif",
                                Arrays.asList(
                                                "Sit comfortably with your arm supported",
                                                "Slowly bend your wrist up and down",
                                                "Hold for 2-3 seconds at each position",
                                                "Repeat 10-15 times"),
                                Arrays.asList(
                                                "Reduces wrist stiffness common in RA",
                                                "Improves range of motion",
                                                "Helps maintain joint function")));

                // 2. Wrist Rotation
                exercises.add(new RAExercise(
                                "ex_002",
                                "Wrist Rotation (Clockwise/Counterclockwise)",
                                "Circular wrist movements to maintain joint mobility and reduce morning stiffness",
                                ExerciseCategory.WRIST,
                                Arrays.asList("Wrist", "Radius", "Ulna"),
                                1,
                                "file:///android_asset/exercise_videos/ex_002_wrist_rotation.mp4",
                                "animation_wrist_rotation.gif",
                                Arrays.asList(
                                                "Extend your arm in front of you",
                                                "Make slow circles with your wrist",
                                                "Rotate 10 times clockwise",
                                                "Rotate 10 times counterclockwise"),
                                Arrays.asList(
                                                "Maintains wrist joint mobility",
                                                "Reduces morning stiffness",
                                                "Improves circulation in wrist area")));

                // 3. Thumb Opposition Exercise
                exercises.add(new RAExercise(
                                "ex_003",
                                "Thumb Opposition Exercise",
                                "Thumb-to-finger touching exercise to maintain thumb mobility and grip strength",
                                ExerciseCategory.THUMB,
                                Arrays.asList("Thumb", "CMC Joint", "Fingers"),
                                1,
                                "file:///android_asset/exercise_videos/ex_003_thumb_opposition.mp4",
                                "animation_thumb_opposition.gif",
                                Arrays.asList(
                                                "Touch your thumb to each fingertip",
                                                "Start with index finger, move to pinky",
                                                "Hold each touch for 2 seconds",
                                                "Repeat sequence 5-10 times"),
                                Arrays.asList(
                                                "Maintains thumb joint flexibility",
                                                "Improves grip strength",
                                                "Helps with daily activities like writing")));

                // 4. Thumb Flexion/Extension
                exercises.add(new RAExercise(
                                "ex_004",
                                "Thumb Flexion/Extension",
                                "Thumb bending exercise to improve thumb joint range of motion",
                                ExerciseCategory.THUMB,
                                Arrays.asList("Thumb", "MCP Joint", "IP Joint"),
                                1,
                                "file:///android_asset/exercise_videos/ex_004_thumb_flexion.mp4",
                                "animation_thumb_flex.gif",
                                Arrays.asList(
                                                "Keep your hand flat on a table",
                                                "Slowly bend your thumb toward your palm",
                                                "Straighten your thumb back up",
                                                "Repeat 10-15 times"),
                                Arrays.asList(
                                                "Improves thumb joint mobility",
                                                "Reduces thumb stiffness",
                                                "Helps maintain pinch strength")));

                // 5. Finger Flexion (Making a Fist)
                exercises.add(new RAExercise(
                                "ex_005",
                                "Finger Flexion (Making a Fist)",
                                "Gentle fist-making exercise to maintain finger joint flexibility",
                                ExerciseCategory.FINGER,
                                Arrays.asList("Fingers", "MCP Joints", "PIP Joints", "DIP Joints"),
                                1,
                                "file:///android_asset/exercise_videos/ex_005_finger_flexion.mp4",
                                "animation_finger_flex.gif",
                                Arrays.asList(
                                                "Start with fingers straight and spread",
                                                "Slowly curl fingers into a loose fist",
                                                "Don't squeeze tightly",
                                                "Hold for 3 seconds, then open",
                                                "Repeat 10 times"),
                                Arrays.asList(
                                                "Maintains finger joint flexibility",
                                                "Improves grip strength gradually",
                                                "Reduces finger stiffness")));

                // 6. Finger Extension/Spreading
                exercises.add(new RAExercise(
                                "ex_006",
                                "Finger Extension/Spreading",
                                "Finger spreading exercise to improve finger extension and reduce joint contractures",
                                ExerciseCategory.FINGER,
                                Arrays.asList("Fingers", "MCP Joints", "Interosseous muscles"),
                                1,
                                "file:///android_asset/exercise_videos/ex_006_finger_extension.mp4",
                                "animation_finger_spread.gif",
                                Arrays.asList(
                                                "Place your hand flat on a table",
                                                "Spread your fingers as wide as comfortable",
                                                "Hold for 5 seconds",
                                                "Relax and repeat 10 times"),
                                Arrays.asList(
                                                "Prevents finger contractures",
                                                "Improves finger extension",
                                                "Maintains hand span for gripping")));

                // 7. Finger Pinch Strengthening
                exercises.add(new RAExercise(
                                "ex_007",
                                "Finger Pinch Strengthening",
                                "Gentle pinching exercise using therapy putty or soft objects to maintain pinch strength",
                                ExerciseCategory.FINGER,
                                Arrays.asList("Thumb", "Index finger", "Pinch muscles"),
                                2,
                                "file:///android_asset/exercise_videos/ex_007_finger_pinch.mp4",
                                "animation_finger_pinch.gif",
                                Arrays.asList(
                                                "Use therapy putty or soft ball",
                                                "Pinch between thumb and index finger",
                                                "Hold for 3 seconds",
                                                "Release slowly",
                                                "Repeat 10-15 times"),
                                Arrays.asList(
                                                "Maintains pinch strength for daily tasks",
                                                "Improves fine motor control",
                                                "Helps with buttoning and writing")));

                // 8. Knee Flexion/Extension (Seated)
                exercises.add(new RAExercise(
                                "ex_008",
                                "Knee Flexion/Extension (Seated)",
                                "Seated knee straightening exercise to maintain knee joint mobility and quadriceps strength",
                                ExerciseCategory.KNEE,
                                Arrays.asList("Knee", "Quadriceps", "Hamstrings"),
                                1,
                                "file:///android_asset/exercise_videos/ex_008_knee_flexion.mp4",
                                "animation_knee_seated.gif",
                                Arrays.asList(
                                                "Sit in a chair with back support",
                                                "Slowly straighten one leg",
                                                "Hold for 3-5 seconds",
                                                "Lower leg slowly",
                                                "Repeat 10 times each leg"),
                                Arrays.asList(
                                                "Maintains knee joint mobility",
                                                "Strengthens quadriceps muscles",
                                                "Reduces knee stiffness")));

                // 9. Hip Flexion (Seated/Standing)
                exercises.add(new RAExercise(
                                "ex_009",
                                "Hip Flexion (Seated/Standing)",
                                "Hip lifting exercise to maintain hip joint flexibility and hip flexor strength",
                                ExerciseCategory.HIP,
                                Arrays.asList("Hip", "Hip flexors", "Psoas"),
                                2,
                                "file:///android_asset/exercise_videos/ex_009_hip_flexion.mp4",
                                "animation_hip_flexion.gif",
                                Arrays.asList(
                                                "Sit or stand with support if needed",
                                                "Slowly lift one knee toward chest",
                                                "Hold for 3 seconds",
                                                "Lower slowly",
                                                "Repeat 10 times each leg"),
                                Arrays.asList(
                                                "Maintains hip joint mobility",
                                                "Improves walking ability",
                                                "Reduces hip stiffness")));

                // 10. Hip Abduction (Side-lying/Standing)
                exercises.add(new RAExercise(
                                "ex_010",
                                "Hip Abduction (Side-lying/Standing)",
                                "Side leg lifting exercise to strengthen hip abductor muscles and improve stability",
                                ExerciseCategory.HIP,
                                Arrays.asList("Hip", "Gluteus medius", "Hip abductors"),
                                2,
                                "file:///android_asset/exercise_videos/ex_010_hip_abduction.mp4",
                                "animation_hip_abduction.gif",
                                Arrays.asList(
                                                "Lie on your side or stand with support",
                                                "Slowly lift top leg to the side",
                                                "Keep leg straight",
                                                "Hold for 3 seconds",
                                                "Lower slowly, repeat 10 times"),
                                Arrays.asList(
                                                "Strengthens hip stabilizer muscles",
                                                "Improves balance and stability",
                                                "Reduces hip pain during walking")));
        }

        /**
         * Get all exercises in the library
         */
        public List<RAExercise> getAllExercises() {
                return new ArrayList<>(exercises);
        }

        /**
         * Get exercises filtered by category
         */
        public List<RAExercise> getExercisesByCategory(ExerciseCategory category) {
                return exercises.stream()
                                .filter(exercise -> exercise.getCategory() == category)
                                .collect(Collectors.toList());
        }

        /**
         * Get exercise by ID
         */
        public RAExercise getExerciseById(String id) {
                return exercises.stream()
                                .filter(exercise -> exercise.getId().equals(id))
                                .findFirst()
                                .orElse(null);
        }
}