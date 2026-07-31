package com.example.myrajourney.rehab.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Index;
import androidx.room.TypeConverters;
import com.example.myrajourney.rehab.database.converters.ExerciseCategoryConverter;
import com.example.myrajourney.rehab.database.converters.StringListConverter;
import com.example.myrajourney.rehab.models.ExerciseCategory;
import java.util.List;

/**
 * Room entity for RA exercises
 */
@Entity(
    tableName = "ra_exercises",
    indices = {
        @Index(value = "category"),
        @Index(value = "difficultyLevel")
    }
)
@TypeConverters({ExerciseCategoryConverter.class, StringListConverter.class})
public class RAExerciseEntity {
    
    @PrimaryKey
    public String id;
    
    public String name;
    public String description;
    public ExerciseCategory category;
    public List<String> targetJoints;
    public int difficultyLevel;
    public String videoUrl;
    public String animationUrl;
    public List<String> instructions;
    public List<String> raSpecificBenefits;
    
    public RAExerciseEntity() {}
    
    public RAExerciseEntity(String id, String name, String description, ExerciseCategory category,
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
}