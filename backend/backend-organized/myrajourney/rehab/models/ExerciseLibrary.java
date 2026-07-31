package com.example.myrajourney.rehab.models;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Container class for managing the default library of RA-specific exercises
 */
public class ExerciseLibrary {
    private List<RAExercise> exercises;

    public ExerciseLibrary() {
        this.exercises = new ArrayList<>();
    }

    public ExerciseLibrary(List<RAExercise> exercises) {
        this.exercises = exercises != null ? exercises : new ArrayList<>();
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

    /**
     * Add exercise to library
     */
    public void addExercise(RAExercise exercise) {
        if (exercise != null && !exercises.contains(exercise)) {
            exercises.add(exercise);
        }
    }

    /**
     * Remove exercise from library
     */
    public boolean removeExercise(String exerciseId) {
        return exercises.removeIf(exercise -> exercise.getId().equals(exerciseId));
    }

    /**
     * Get exercises by difficulty level
     */
    public List<RAExercise> getExercisesByDifficulty(int difficultyLevel) {
        return exercises.stream()
                .filter(exercise -> exercise.getDifficultyLevel() == difficultyLevel)
                .collect(Collectors.toList());
    }

    /**
     * Get exercises targeting specific joints
     */
    public List<RAExercise> getExercisesByTargetJoint(String joint) {
        return exercises.stream()
                .filter(exercise -> exercise.getTargetJoints().contains(joint))
                .collect(Collectors.toList());
    }

    /**
     * Get total number of exercises
     */
    public int getExerciseCount() {
        return exercises.size();
    }

    /**
     * Check if library contains exercise with given ID
     */
    public boolean containsExercise(String exerciseId) {
        return exercises.stream()
                .anyMatch(exercise -> exercise.getId().equals(exerciseId));
    }

    /**
     * Get exercises by multiple categories
     */
    public List<RAExercise> getExercisesByCategories(List<ExerciseCategory> categories) {
        return exercises.stream()
                .filter(exercise -> categories.contains(exercise.getCategory()))
                .collect(Collectors.toList());
    }

    /**
     * Set the complete exercise list
     */
    public void setExercises(List<RAExercise> exercises) {
        this.exercises = exercises != null ? exercises : new ArrayList<>();
    }

    /**
     * Clear all exercises from library
     */
    public void clearLibrary() {
        exercises.clear();
    }
}