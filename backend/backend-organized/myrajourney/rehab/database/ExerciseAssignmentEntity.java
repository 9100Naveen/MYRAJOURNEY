package com.example.myrajourney.rehab.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.TypeConverters;
import com.example.myrajourney.rehab.database.converters.DateTimeConverter;
import com.example.myrajourney.rehab.database.converters.StringListConverter;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Room entity for exercise assignments
 */
@Entity(
    tableName = "exercise_assignments",
    foreignKeys = {
        @ForeignKey(
            entity = UserEntity.class,
            parentColumns = "id",
            childColumns = "doctorId",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = UserEntity.class,
            parentColumns = "id", 
            childColumns = "patientId",
            onDelete = ForeignKey.CASCADE
        )
    },
    indices = {
        @Index(value = {"patientId", "isActive"}),
        @Index(value = {"doctorId", "patientId"}),
        @Index(value = "assignedDate")
    }
)
@TypeConverters({DateTimeConverter.class, StringListConverter.class})
public class ExerciseAssignmentEntity {
    
    @PrimaryKey
    public String id;
    
    public String doctorId;
    public String patientId;
    public List<String> exerciseIds;
    public LocalDateTime assignedDate;
    public String notes;
    public boolean isActive;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
    
    public ExerciseAssignmentEntity() {}
    
    public ExerciseAssignmentEntity(String id, String doctorId, String patientId,
                                   List<String> exerciseIds, LocalDateTime assignedDate,
                                   String notes, boolean isActive) {
        this.id = id;
        this.doctorId = doctorId;
        this.patientId = patientId;
        this.exerciseIds = exerciseIds;
        this.assignedDate = assignedDate;
        this.notes = notes;
        this.isActive = isActive;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}