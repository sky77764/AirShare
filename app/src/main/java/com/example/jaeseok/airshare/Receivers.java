package com.example.jaeseok.airshare;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by JaeSeok on 2016. 7. 4..
 */
public class Receivers implements Comparable<Object>{
    int index;
    LatLng pos;
    double distance;    // meter
    double degree;      // radian
    boolean isIncluded;

    public void fillData(int index, LatLng pos, LatLng userLocation, double start_region, double end_region) {
        this.index = index;
        this.pos = pos;
        calculateDistance(userLocation);
        calculateDegree(userLocation);

        if(end_region-start_region <= Math.PI && this.degree > start_region && this.degree < end_region)
            isIncluded = true;
        else if(end_region-start_region > Math.PI && this.degree < start_region && this.degree > end_region)
            isIncluded = true;
        else
            isIncluded = false;


        Log.d("fillData", "index: " + String.valueOf(index) + ", pos.lat: " + String.valueOf(this.pos.latitude) +
                ", pos.long: " + String.valueOf(this.pos.longitude) + ", distance: " + String.valueOf(this.distance) +
                ", degree: " + String.valueOf(this.degree) + ", isIncluded: " + String.valueOf(this.isIncluded));


    }

    private void calculateDistance(LatLng userLocation) {
        double theta = userLocation.longitude - this.pos.longitude;
        double dist = Math.sin(deg2rad(userLocation.latitude)) * Math.sin(deg2rad(this.pos.latitude)) + Math.cos(deg2rad(userLocation.latitude)) * Math.cos(deg2rad(this.pos.latitude)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344 * 1000;
        this.distance = dist;
    }


    private void calculateDegree(LatLng userLocation) {
        // 가로 longitude 오른쪽이 큼, 세로 latitude 위쪽이 큼
        double theta = userLocation.longitude - this.pos.longitude;
        double dist = Math.sin(deg2rad(this.pos.latitude)) * Math.sin(deg2rad(this.pos.latitude)) + Math.cos(deg2rad(this.pos.latitude)) * Math.cos(deg2rad(this.pos.latitude)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515 * 1000;
        dist = dist * 1.609344;

        double degree = Math.asin(dist/this.distance);
        if(userLocation.longitude > this.pos.longitude)
            degree = -degree;
        this.degree = degree;
    }

    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    @Override
    public int compareTo(Object o) {
        return distance > ((Receivers) o).distance ? 1 : (distance == ((Receivers) o).distance ? 0 : -1);
    }
}
