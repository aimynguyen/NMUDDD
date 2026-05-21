package com.example.diary_app.data.model;

import com.google.firebase.firestore.GeoPoint;

public class Location {
    private GeoPoint coordinates;
    private String address;
    private String city;

    public Location() {}

    public Location(GeoPoint coordinates, String address, String city) {
        this.coordinates = coordinates;
        this.address = address;
        this.city = city;
    }

    public GeoPoint getCoordinates() {return coordinates;}
    public void setCoordinates(GeoPoint coordinates) { this.coordinates = coordinates; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
}
