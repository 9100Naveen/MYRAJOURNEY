package com.example.myrajourney.rehab.services;

import com.example.myrajourney.rehab.models.ExerciseCategory;
import com.example.myrajourney.rehab.models.RAExercise;
import java.util.List;

/**
 * Service interface for managing the RA exercise library
 */
public interface ExerciseLibraryService {
    
    /**
     * Get all available RA exercises
     * @return List of all exercises
     */
    List<RAExercise> getAllExercises();
    
    /**
     * Get exercises filtered by category
     * @param category The exercise category to filter by
     * @return List of exercises in the specified category
     */
    List<RAExercise> getExercisesByCategory(ExerciseCategory category);
    
    /**
     * Get a specific exercise by ID
     * @param id The exercise ID
     * @return The exercise or null if not found
     */
    RAExercise getExerciseById(String id);
    
    /**
     * Get the video URL for an exercise
     * @param exerciseId The exercise ID
     * @return The video URL or null if not available
     */
    String getExerciseVideoUrl(String exerciseId);
    
    /**
     * Get the animation URL for an exercise (fallback)
     * @param exerciseId The exercise ID
     * @return The animation URL or null if not available
     */
    String getExerciseAnimationUrl(String exerciseId);
}