package com.example.myrajourney.doctor.dashboard;

public class HealthMetric {
    private String name;
    private String value;
    private String icon;
    private String color;
    
    public HealthMetric(String name, String value, String icon, String color) {
        this.name = name;
        this.value = value;
        this.icon = icon;
        this.color = color;
    }
    
    public String getName() {
        return name;
    }
    
    public String getValue() {
        return value;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public String getColor() {
        return color;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
    
    public void setIcon(String icon) {
        this.icon = icon;
    }
    
    public void setColor(String color) {
        this.color = color;
    }
}






