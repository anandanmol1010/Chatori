package com.app.chatori.model;

import java.util.Date;

/**
 * Model class representing a review in the Chatori app.
 * Maps to the "Reviews" collection in Firestore.
 */
public class Review {
    private String reviewId;
    private String stallId;
    private String userId;
    private float rating;
    private String comment;
    private Date timestamp;
    
    // Transient fields (not stored in Firestore)
    private String userName;
    private String userProfileImageUrl;
    private String stallName;
    private Date createdAt;

    // Required empty constructor for Firestore
    public Review() {
        timestamp = new Date();
        createdAt = timestamp;
    }

    public Review(String reviewId, String stallId, String userId, float rating, String comment) {
        this.reviewId = reviewId;
        this.stallId = stallId;
        this.userId = userId;
        this.rating = rating;
        this.comment = comment;
        this.timestamp = new Date();
        this.createdAt = timestamp;
    }

    // Getters and Setters
    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    public String getStallId() {
        return stallId;
    }

    public void setStallId(String stallId) {
        this.stallId = stallId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserProfileImageUrl() {
        return userProfileImageUrl;
    }

    public void setUserProfileImageUrl(String userProfileImageUrl) {
        this.userProfileImageUrl = userProfileImageUrl;
    }
    
    public String getStallName() {
        return stallName;
    }
    
    public void setStallName(String stallName) {
        this.stallName = stallName;
    }
    
    public Date getCreatedAt() {
        return createdAt != null ? createdAt : timestamp;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    // Alias methods for compatibility with adapter
    public String getText() {
        return comment;
    }
    
    public void setText(String text) {
        this.comment = text;
    }
}
