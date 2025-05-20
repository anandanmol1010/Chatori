package com.app.chatori.model;

import java.util.ArrayList;
import java.util.List;

import com.google.firebase.firestore.GeoPoint;

/**
 * Model class representing a food stall in the Chatori app.
 * Maps to the "Stalls" collection in Firestore.
 */
public class Stall {
    private String stallId;
    private String name;
    private String dishType;
    private String area;
    private com.google.firebase.firestore.GeoPoint location;
    private List<String> images;
    private String createdBy;
    private float rating;
    private int numRatings;
    private String description;
    private String openingHours;
    private String phone;
    private String userId;
    private String ownerName;
    private java.util.Date createdAt;

    // Required empty constructor for Firestore
    public Stall() {
        images = new ArrayList<>();
        rating = 0.0f;
        numRatings = 0;
    }

    public Stall(String stallId, String name, String dishType, String area, com.google.firebase.firestore.GeoPoint location, String createdBy) {
        this.stallId = stallId;
        this.name = name;
        this.dishType = dishType;
        this.area = area;
        this.location = location;
        this.createdBy = createdBy;
        this.images = new ArrayList<>();
        this.rating = 0.0f;
        this.numRatings = 0;
    }

    // Getters and Setters
    public String getStallId() {
        return stallId;
    }

    public void setStallId(String stallId) {
        this.stallId = stallId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDishType() {
        return dishType;
    }

    public void setDishType(String dishType) {
        this.dishType = dishType;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public com.google.firebase.firestore.GeoPoint getLocation() {
        return location;
    }

    public void setLocation(com.google.firebase.firestore.GeoPoint location) {
        this.location = location;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public void addImage(String imageUrl) {
        if (images == null) {
            images = new ArrayList<>();
        }
        images.add(imageUrl);
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public int getNumRatings() {
        return numRatings;
    }

    public void setNumRatings(int numRatings) {
        this.numRatings = numRatings;
    }

    /**
     * Updates the rating when a new review is added
     * @param newRating The rating from the new review
     */
    public void updateRating(float newRating) {
        float totalRating = this.rating * this.numRatings;
        numRatings++;
        this.rating = (totalRating + newRating) / numRatings;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getOpeningHours() {
        return openingHours;
    }
    
    public void setOpeningHours(String openingHours) {
        this.openingHours = openingHours;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getOwnerName() {
        return ownerName;
    }
    
    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
    
    public java.util.Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(java.util.Date createdAt) {
        this.createdAt = createdAt;
    }

    // Using com.google.firebase.firestore.GeoPoint instead of a custom GeoPoint class
}
