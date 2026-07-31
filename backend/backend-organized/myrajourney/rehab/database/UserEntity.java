package com.example.myrajourney.rehab.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Simple User entity for Room database foreign key references
 */
@Entity(tableName = "users")
public class UserEntity {
    
    @PrimaryKey
    public String id;
    
    public String name;
    public String email;
    public String role;
    
    public UserEntity() {}
    
    public UserEntity(String id, String name, String email, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }
}