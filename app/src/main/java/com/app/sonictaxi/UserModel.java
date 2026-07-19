package com.app.sonictaxi;

public class UserModel {
    private String userId;
    private String name;
    private String email;
    private String role;
    private String carDetails;

    private String status;
    private double currentLat;
    private double currentLng;

    public UserModel() {}

    public UserModel(String userId, String name, String email, String role, String carDetails) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
        this.carDetails = carDetails;
        this.status = "offline";
    }

    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getCarDetails() { return carDetails; }
    public String getStatus() { return status; }
    public double getCurrentLat() { return currentLat; }
    public double getCurrentLng() { return currentLng; }

    // Setters
    public void setUserId(String userId) { this.userId = userId; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(String role) { this.role = role; }
    public void setCarDetails(String carDetails) { this.carDetails = carDetails; }
    public void setStatus(String status) { this.status = status; }
    public void setCurrentLat(double currentLat) { this.currentLat = currentLat; }
    public void setCurrentLng(double currentLng) { this.currentLng = currentLng; }
}