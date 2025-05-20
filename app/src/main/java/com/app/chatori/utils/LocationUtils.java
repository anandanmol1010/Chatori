package com.app.chatori.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.app.chatori.model.Stall;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Utility class for location-related operations.
 */
public class LocationUtils {
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    
    /**
     * Checks if location permissions are granted
     * @param context Application context
     * @return True if permissions are granted, false otherwise
     */
    public static boolean hasLocationPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, 
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * Requests location permissions
     * @param activity Activity to request permissions from
     */
    public static void requestLocationPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }
    
    /**
     * Gets the last known location
     * @param context Application context
     * @return Task containing the last known location
     */
    public static Task<Location> getLastLocation(Context context) {
        FusedLocationProviderClient fusedLocationClient = 
                LocationServices.getFusedLocationProviderClient(context);
        
        if (hasLocationPermission(context)) {
            return fusedLocationClient.getLastLocation();
        }
        
        return null;
    }
    
    /**
     * Calculates distance between two points in kilometers
     * @param lat1 Latitude of first point
     * @param lon1 Longitude of first point
     * @param lat2 Latitude of second point
     * @param lon2 Longitude of second point
     * @return Distance in kilometers
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Haversine formula to calculate distance between two points
        final int R = 6371; // Radius of the earth in km
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
    
    /**
     * Sorts stalls by distance from current location
     * @param stalls List of stalls to sort
     * @param currentLocation Current location
     * @return Sorted list of stalls
     */
    public static List<Stall> sortStallsByDistance(List<Stall> stalls, Location currentLocation) {
        if (currentLocation == null || stalls == null || stalls.isEmpty()) {
            return stalls;
        }
        
        List<Stall> sortedStalls = new ArrayList<>(stalls);
        
        Collections.sort(sortedStalls, (stall1, stall2) -> {
            double distance1 = calculateDistance(
                    currentLocation.getLatitude(), currentLocation.getLongitude(),
                    stall1.getLocation().getLatitude(), stall1.getLocation().getLongitude());
            
            double distance2 = calculateDistance(
                    currentLocation.getLatitude(), currentLocation.getLongitude(),
                    stall2.getLocation().getLatitude(), stall2.getLocation().getLongitude());
            
            return Double.compare(distance1, distance2);
        });
        
        return sortedStalls;
    }
    
    /**
     * Filters stalls within a certain radius
     * @param stalls List of stalls to filter
     * @param currentLocation Current location
     * @param radiusKm Radius in kilometers
     * @return Filtered list of stalls
     */
    public static List<Stall> filterStallsByRadius(List<Stall> stalls, Location currentLocation, double radiusKm) {
        if (currentLocation == null || stalls == null || stalls.isEmpty()) {
            return stalls;
        }
        
        List<Stall> filteredStalls = new ArrayList<>();
        
        for (Stall stall : stalls) {
            double distance = calculateDistance(
                    currentLocation.getLatitude(), currentLocation.getLongitude(),
                    stall.getLocation().getLatitude(), stall.getLocation().getLongitude());
            
            if (distance <= radiusKm) {
                filteredStalls.add(stall);
            }
        }
        
        return filteredStalls;
    }
}
