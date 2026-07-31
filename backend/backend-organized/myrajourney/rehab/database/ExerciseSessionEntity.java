package com.example.myrajourney.rehab.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.TypeConverters;
import com.example.myrajourney.rehab.database.converters.DateTimeConverter;
import com.example.myrajourney.rehab.database.converters.MotionDataConverter;
import com.example.myrajourney.rehab.database.converters.FloatListConverter;
import com.example.myrajourney.rehab.models.MotionFrame;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Room entity for exercise sessions
 */
@Entity(
    tableName = "exercise_sessions",
    foreignKeys = {
        @ForeignKey(
            entity = UserEntity.class,
            parentColumns = "id",
            childColumns = "patientId",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = RAExerciseEntity.class,
            parentColumns = "id",
            childColumns = "exerciseId",
            onDelete = ForeignKey.CASCADE
        )
    },
    indices = {
        @Index(value = {"patientId", "exerciseId"}),
        @Index(value = {"patientId", "startTime"}),
        @Index(value = "completed")
    }
)
@TypeConverters({DateTimeConverter.class, MotionDataConverter.class, FloatListConverter.class})
public class ExerciseSessionEntity {
    
    @PrimaryKey
    public String id;
    
    public String patientId;
    public String exerciseId;
    public LocalDateTime startTime;
    public LocalDateTime endTime;
    public Integer sessionDuration; // Duration in seconds
    public Float overallAccuracy; // 0.0 to 1.0
    public Float completionRate; // 0.0 to 1.0
    public List<MotionFrame> motionData;
    public List<Float> formAccuracyScores;
    public boolean completed;
    public LocalDateTime createdAt;
    
    public ExerciseSessionEntity() {}
    
    public ExerciseSessionEntity(String id, String patientId, String exerciseId,
                                LocalDateTime startTime) {
        this.id = id;
        this.patientId = patientId;
        this.exerciseId = exerciseId;
        this.startTime = startTime;
        this.completed = false;
        this.createdAt = LocalDateTime.now();
    }
}