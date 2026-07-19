package com.app.sonictaxi;

public class SettingsModel {
    private double baseFare;
    private double pricePerKm;
    private String cityName;

    public SettingsModel() {}

    public SettingsModel(double baseFare, double pricePerKm, String cityName) {
        this.baseFare = baseFare;
        this.pricePerKm = pricePerKm;
        this.cityName = cityName;
    }


    public double getBaseFare() { return baseFare; }
    public double getPricePerKm() { return pricePerKm; }
    public String getCityName() { return cityName; }


    public void setBaseFare(double baseFare) { this.baseFare = baseFare; }
    public void setPricePerKm(double pricePerKm) { this.pricePerKm = pricePerKm; }
    public void setCityName(String cityName) { this.cityName = cityName; }
}