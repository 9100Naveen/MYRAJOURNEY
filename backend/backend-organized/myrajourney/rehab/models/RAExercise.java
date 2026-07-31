package com.example.myrajourney.rehab.models;

import java.util.List;

/**
 * Model class representing a Rheumatoid Arthritis specific exercise
 */
public class RAExercise {
    private String id;
    private String name;
    private String description;
    private ExerciseCategory category;
    private List<String> targetJoints;
    private int difficultyLevel;
    private String videoUrl;
    private String animationUrl;
    private List<String> instructions;
    private List<String> raSpecificBenefits;

    public RAExercise() {}

    public RAExercise(String id, String name, String description, ExerciseCategory category,
                      List<String> targetJoints, int difficultyLevel, String videoUrl,
                      String animationUrl, List<String> instructions, List<String> raSpecificBenefits) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.targetJoints = targetJoints;
        this.difficultyLevel = difficultyLevel;
        this.videoUrl = videoUrl;
        this.animationUrl = animationUrl;
        this.instructions = instructions;
        this.raSpecificBenefits = raSpecificBenefits;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ExerciseCategory getCategory() { return category; }
    public void setCategory(ExerciseCategory category) { this.category = category; }

    public List<String> getTargetJoints() { return targetJoints; }
    public void setTargetJoints(List<String> targetJoints) { this.targetJoints = targetJoints; }

    public int getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(int difficultyLevel) { this.difficultyLevel = difficultyLevel; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public String getAnimationUrl() { return animationUrl; }
    public void setAnimationUrl(String animationUrl) { this.animationUrl = animationUrl; }

    public List<String> getInstructions() { return instructions; }
    public void setInstructions(List<String> instructions) { this.instructions = instructions; }

    public List<String> getRaSpecificBenefits() { return raSpecificBenefits; }
    public void setRaSpecificBenefits(List<String> raSpecificBenefits) { this.raSpecificBenefits = raSpecificBenefits; }
}