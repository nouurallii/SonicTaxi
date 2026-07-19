package com.app.sonictaxi;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Driver {
    private String id;
    private String name;
    private String carModel;
    private String plateNumber;
    private String status;
    private double lat;
    private double lng;
    public String phone, managerId;

    public Driver() {
    }

    // 2. المشيد المخصص (للاستخدام اليدوي)
    public Driver(String name, String carModel, String plateNumber, String status, double lat, double lng) {
        this.name = name;
        this.carModel = carModel;
        this.plateNumber = plateNumber;
        this.status = status;
        this.lat = lat;
        this.lng = lng;
    }

    // 3. Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCarModel() { return carModel; }
    public void setCarModel(String carModel) { this.carModel = carModel; }

    public String getPlateNumber() { return plateNumber; }
    public void setPlateNumber(String plateNumber) { this.plateNumber = plateNumber; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }

    public double getLng() { return lng; }
    public void setLng(double lng) { this.lng = lng; }

    // أضفنا Getters للـ phone و managerId ليتمكن الـ Adapter من الوصول إليها إذا لزم الأمر
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getManagerId() { return managerId; }
    public void setManagerId(String managerId) { this.managerId = managerId; }
}