package com.example.myrajourney.rehab.services.impl;

import com.example.myrajourney.rehab.models.ExerciseCategory;
import com.example.myrajourney.rehab.models.RAExercise;
import com.example.myrajourney.rehab.repository.ExerciseLibraryRepository;
import com.example.myrajourney.rehab.services.ExerciseLibraryService;
import java.util.List;

/**
 * Implementation of ExerciseLibraryService
 */
public class ExerciseLibraryServiceImpl implements ExerciseLibraryService {
    
    private final ExerciseLibraryRepository repository;
    
    public ExerciseLibraryServiceImpl() {
        this.repository = ExerciseLibraryRepository.getInstance();
    }
    
    @Override
    public List<RAExercise> getAllExercises() {
        return repository.getAllExercises();
    }
    
    @Override
    public List<RAExercise> getExercisesByCategory(ExerciseCategory category) {
        return repository.getExercisesByCategory(category);
    }
    
    @Override
    public RAExercise getExerciseById(String id) {
        return repository.getExerciseById(id);
    }
    
    @Override
    public String getExerciseVideoUrl(String exerciseId) {
        RAExercise exercise = repository.getExerciseById(exerciseId);
        return exercise != null ? exercise.getVideoUrl() : null;
    }
    
    @Override
    public String getExerciseAnimationUrl(String exerciseId) {
        RAExercise exercise = repository.getExerciseById(exerciseId);
        return exercise != null ? exercise.getAnimationUrl() : null;
    }
}