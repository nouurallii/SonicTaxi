package com.app.sonictaxi;

public class RideRequest {
    private String rideId;
    private String status;
    private String customerID;
    private double pickupLat, pickupLng;
    private String destination;

    // نظام التفاوض
    private double offerPrice; // السعر الذي يقترحه السائق في لحظة التفاوض
    private double finalPrice; // السعر الذي اتفق عليه الطرفان

    private double rating;
    private String driverId;
    private String officeId;
    private long timestamp; // لتنظيم الرحلات زمنياً

    public RideRequest() {}

    public RideRequest(String status, String customerID, double pickupLat, double pickupLng, String destination) {
        this.status = status;
        this.customerID = customerID;
        this.pickupLat = pickupLat;
        this.pickupLng = pickupLng;
        this.destination = destination;
        this.timestamp = System.currentTimeMillis();
    }



    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public double getOfferPrice() { return offerPrice; }
    public void setOfferPrice(double offerPrice) { this.offerPrice = offerPrice; }

    public double getFinalPrice() { return finalPrice; }
    public void setFinalPrice(double finalPrice) { this.finalPrice = finalPrice; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    // إضافة دوال الـ Getters والـ Setters المفقودة
    public String getRideId() { return rideId; }
    public void setRideId(String rideId) { this.rideId = rideId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCustomerID() { return customerID; }
    public void setCustomerID(String customerID) { this.customerID = customerID; }

    public double getPickupLat() { return pickupLat; }
    public void setPickupLat(double pickupLat) { this.pickupLat = pickupLat; }

    public double getPickupLng() { return pickupLng; }
    public void setPickupLng(double pickupLng) { this.pickupLng = pickupLng; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public String getDriverId() { return driverId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }

    public String getOfficeId() { return officeId; }
    public void setOfficeId(String officeId) { this.officeId = officeId; }


}
