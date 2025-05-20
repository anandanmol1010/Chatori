package com.app.chatori.utils;

import android.content.Context;
import android.widget.Toast;

import com.app.chatori.model.Stall;
import com.google.firebase.firestore.GeoPoint;

/**
 * Helper class for handling stall data validation and error prevention
 */
public class StallDataHelper {

    /**
     * Validates a stall object and ensures all required fields are present
     * @param stall The stall to validate
     * @return True if the stall is valid, false otherwise
     */
    public static boolean validateStall(Stall stall) {
        if (stall == null) {
            return false;
        }

        // Check for required fields
        if (stall.getName() == null || stall.getName().isEmpty()) {
            return false;
        }

        // Ensure GeoPoint is valid
        if (stall.getLocation() == null) {
            // Create a default GeoPoint to prevent crashes
            stall.setLocation(new GeoPoint(0, 0));
        }

        // Ensure images list is initialized
        if (stall.getImages() == null) {
            stall.setImages(new java.util.ArrayList<>());
        }

        return true;
    }
    
    /**
     * Validates and fixes a stall object to ensure it's always valid
     * This method will never return false, it will always fix the stall
     * @param stall The stall to validate and fix
     * @return True always, as the stall will be fixed if invalid
     */
    public static boolean validateAndFixStall(Stall stall) {
        if (stall == null) {
            return false; // Can't fix a null stall
        }

        // Fix missing name
        if (stall.getName() == null || stall.getName().isEmpty()) {
            stall.setName("Unknown Stall");
            if (stall.getStallId() != null) {
                stall.setName("Stall " + stall.getStallId());
            }
        }
        
        // Fix missing stall ID
        if (stall.getStallId() == null || stall.getStallId().isEmpty()) {
            stall.setStallId("stall_" + System.currentTimeMillis());
        }

        // Fix missing location
        if (stall.getLocation() == null) {
            stall.setLocation(new GeoPoint(0, 0));
        }

        // Fix missing images list
        if (stall.getImages() == null) {
            stall.setImages(new java.util.ArrayList<>());
        }
        
        // Fix missing dish type
        if (stall.getDishType() == null || stall.getDishType().isEmpty()) {
            stall.setDishType("Various");
        }
        
        // Fix missing area
        if (stall.getArea() == null || stall.getArea().isEmpty()) {
            stall.setArea("Unknown Area");
        }
        
        // Fix missing description
        if (stall.getDescription() == null) {
            stall.setDescription("");
        }
        
        // Fix missing rating
        if (stall.getRating() < 0) {
            stall.setRating(0);
        }
        
        // Fix missing numRatings
        if (stall.getNumRatings() < 0) {
            stall.setNumRatings(0);
        }
        
        // Fix missing opening hours
        if (stall.getOpeningHours() == null || stall.getOpeningHours().isEmpty()) {
            stall.setOpeningHours("Not specified");
        }
        
        return true;
    }

    /**
     * Safely gets a stall name, preventing null pointer exceptions
     * @param stall The stall to get the name from
     * @return The stall name or a default value
     */
    public static String getSafeName(Stall stall) {
        if (stall == null || stall.getName() == null) {
            return "Unknown Stall";
        }
        return stall.getName();
    }

    /**
     * Safely gets a stall dish type, preventing null pointer exceptions
     * @param stall The stall to get the dish type from
     * @return The stall dish type or a default value
     */
    public static String getSafeDishType(Stall stall) {
        if (stall == null || stall.getDishType() == null) {
            return "Unknown";
        }
        return stall.getDishType();
    }

    /**
     * Safely gets a stall area, preventing null pointer exceptions
     * @param stall The stall to get the area from
     * @return The stall area or a default value
     */
    public static String getSafeArea(Stall stall) {
        if (stall == null || stall.getArea() == null) {
            return "Unknown";
        }
        return stall.getArea();
    }

    /**
     * Safely gets a stall description, preventing null pointer exceptions
     * @param stall The stall to get the description from
     * @return The stall description or an empty string
     */
    public static String getSafeDescription(Stall stall) {
        if (stall == null || stall.getDescription() == null) {
            return "";
        }
        return stall.getDescription();
    }

    /**
     * Safely gets a stall location, preventing null pointer exceptions
     * @param stall The stall to get the location from
     * @return The stall location or a default GeoPoint
     */
    public static GeoPoint getSafeLocation(Stall stall) {
        if (stall == null || stall.getLocation() == null) {
            return new GeoPoint(0, 0);
        }
        return stall.getLocation();
    }

    /**
     * Shows a toast message and logs an error
     * @param context The context
     * @param message The message to show
     * @param error The error to log
     */
    public static void handleError(Context context, String message, Exception error) {
        if (context != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
        if (error != null) {
            error.printStackTrace();
        }
    }
}
