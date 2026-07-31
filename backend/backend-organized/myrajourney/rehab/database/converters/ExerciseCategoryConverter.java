package com.example.myrajourney.rehab.database.converters;

import androidx.room.TypeConverter;
import com.example.myrajourney.rehab.models.ExerciseCategory;

/**
 * Room type converter for ExerciseCategory enum
 */
public class ExerciseCategoryConverter {
    
    @TypeConverter
    public static ExerciseCategory fromString(String value) {
        return value == null ? null : ExerciseCategory.valueOf(value);
    }
    
    @TypeConverter
    public static String fromExerciseCategory(ExerciseCategory category) {
        return category == null ? null : category.name();
    }
}