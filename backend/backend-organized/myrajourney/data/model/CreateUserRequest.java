package com.example.myrajourney.data.model;

import com.google.gson.annotations.SerializedName;

public class CreateUserRequest {

    @SerializedName("name")
    private String name;

    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;

    @SerializedName("role")
    private String role;

    @SerializedName("phone")
    private String mobile;

    @SerializedName("address")
    private String address;

    @SerializedName("specialization")
    private String specialization;

    // ✅ ADDED: Age field is required for Patients
    @SerializedName("age")
    private String age;

    // Default Constructor
    public CreateUserRequest() {
    }

    // Constructor matching CreateDoctorActivity usage
    public CreateUserRequest(String name, String email, String mobile, String role, String password, String address) {
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.role = role;
        this.password = password;
        this.address = address;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    // ✅ ADDED: Getter and Setter for Age
    public String getAge() { return age; }
    public void setAge(String age) { this.age = age; }
}