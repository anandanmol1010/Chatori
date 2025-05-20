package com.app.chatori.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class representing a user in the Chatori app.
 * Maps to the "Users" collection in Firestore.
 */
public class User {
    private String userId;
    private String name;
    private String email;
    private String profileImageUrl;
    private String bio;
    private String phone;
    private List<String> favorites;

    // Required empty constructor for Firestore
    public User() {
        favorites = new ArrayList<>();
    }

    public User(String userId, String name, String email) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.profileImageUrl = "";
        this.bio = "";
        this.phone = "";
        this.favorites = new ArrayList<>();
    }

    public User(String userId, String name, String email, String profileImageUrl) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.profileImageUrl = profileImageUrl;
        this.bio = "";
        this.phone = "";
        this.favorites = new ArrayList<>();
    }
    
    public User(String userId, String name, String email, String profileImageUrl, String bio, String phone) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.profileImageUrl = profileImageUrl;
        this.bio = bio;
        this.phone = phone;
        this.favorites = new ArrayList<>();
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public List<String> getFavorites() {
        return favorites;
    }

    public List<String> getFavoriteStalls() {
        return favorites;
    }

    public void setFavorites(List<String> favorites) {
        this.favorites = favorites;
    }

    public void addFavorite(String stallId) {
        if (favorites == null) {
            favorites = new ArrayList<>();
        }
        if (!favorites.contains(stallId)) {
            favorites.add(stallId);
        }
    }

    public void removeFavorite(String stallId) {
        if (favorites != null) {
            favorites.remove(stallId);
        }
    }

    public boolean isFavorite(String stallId) {
        return favorites != null && favorites.contains(stallId);
    }
    
    public String getBio() {
        return bio;
    }
    
    public void setBio(String bio) {
        this.bio = bio;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
}
