package com.app.chatori.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class representing a dish in the Chatori app.
 * Maps to the "Dishes" collection in Firestore.
 * Used for filter dropdowns and categorization.
 */
public class Dish {
    private String name;
    private List<String> tags;
    private String price;
    private String imageUrl;

    // Required empty constructor for Firestore
    public Dish() {
        tags = new ArrayList<>();
        price = "0";
    }

    public Dish(String name) {
        this.name = name;
        this.tags = new ArrayList<>();
        this.price = "0";
    }

    public Dish(String name, List<String> tags) {
        this.name = name;
        this.tags = tags;
        this.price = "0";
    }
    
    public Dish(String name, List<String> tags, String price, String imageUrl) {
        this.name = name;
        this.tags = tags;
        this.price = price;
        this.imageUrl = imageUrl;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
    
    public String getPrice() {
        return price;
    }
    
    public void setPrice(String price) {
        this.price = price;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public void addTag(String tag) {
        if (tags == null) {
            tags = new ArrayList<>();
        }
        if (!tags.contains(tag)) {
            tags.add(tag);
        }
    }
    
    public void removeTag(String tag) {
        if (tags != null) {
            tags.remove(tag);
        }
    }
}
