package com.decibel.civilianc2.model.entities;

/**
 * Created by dburnett on 12/28/2017.
 */

public class Position {
    public Position(double latitude, double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Position(double latitude, double longitude, double ambiguity){
        this.longitude = longitude;
        this.latitude = latitude;
        this.ambiguity = ambiguity;
    }

    public double getLongitude(){
        return this.longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public Double getAmbiguity(){
        return ambiguity;
    }

    public static double distanceBetween(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double dist = EarthRadius * c;
        return dist;
    }

    public double distanceFrom(Position otherPosition){
        return distanceBetween(latitude, longitude, otherPosition.latitude, otherPosition.longitude);
    }

    private double latitude;
    private double longitude;
    private Double ambiguity;
    private static final double EarthRadius = 3958.75;
}
