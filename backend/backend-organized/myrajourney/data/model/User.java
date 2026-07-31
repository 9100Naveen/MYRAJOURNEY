package com.example.myrajourney.data.model;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("email")
    private String email;

    @SerializedName("role")
    private String role;

    @SerializedName("assigned_doctor_id")
    private Integer assignedDoctorId;

    @SerializedName("phone")
    private String phone;

    @SerializedName("address")
    private String address;

    // ✅ ADDED: Missing Age, Gender, and Profile Image fields
    @SerializedName("age")
    private String age;

    @SerializedName("gender")
    private String gender;

    @SerializedName("profile_image")
    private String profileImage;
    
    @SerializedName("specialization")
    private String specialization;
    
    @SerializedName("active")
    private boolean active = true;
    
    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    @SerializedName("last_login_at")
    private String lastLoginAt;

    @SerializedName("status")
    private String status;

    @SerializedName("avatar_url")
    private String avatarUrl;

    // Constructors
    public User() {}
    
    public User(String id, String name, String email, String role) {
        this.id = Integer.parseInt(id);
        this.name = name;
        this.email = email;
        this.role = role;
    }

    // --- Getters ---
    public int getId() { return id; }
    public String getIdString() { return String.valueOf(id); }
    
    // For compatibility with rehab system that expects String ID
    public String getIdAsString() { return String.valueOf(id); }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public Integer getAssignedDoctorId() { return assignedDoctorId; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }

    // ✅ ADDED: Getters for new fields
    public String getAge() { return age; }
    public String getGender() { return gender; }
    public String getProfileImage() { return profileImage; }
    public String getSpecialization() { return specialization; }
    public boolean isActive() { return active; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public String getLastLoginAt() { return lastLoginAt; }
    public String getStatus() { return status; }
    public String getAvatarUrl() { return avatarUrl; }

    // --- Setters ---
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(String role) { this.role = role; }
    public void setAssignedDoctorId(Integer assignedDoctorId) { this.assignedDoctorId = assignedDoctorId; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setAddress(String address) { this.address = address; }

    // ✅ ADDED: Setters for new fields
    public void setAge(String age) { this.age = age; }
    public void setGender(String gender) { this.gender = gender; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public void setActive(boolean active) { this.active = active; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    public void setLastLoginAt(String lastLoginAt) { this.lastLoginAt = lastLoginAt; }
    public void setStatus(String status) { this.status = status; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}