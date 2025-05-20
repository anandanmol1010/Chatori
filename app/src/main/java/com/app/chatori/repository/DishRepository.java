package com.app.chatori.repository;

import com.app.chatori.model.Dish;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

/**
 * Repository class for handling Dish data operations with Firestore.
 * Used for filter dropdowns and categorization.
 */
public class DishRepository {
    private static final String COLLECTION_DISHES = "Dishes";
    
    private final FirebaseFirestore db;
    
    // Singleton instance
    private static DishRepository instance;
    
    private DishRepository() {
        db = FirebaseFirestore.getInstance();
    }
    
    public static synchronized DishRepository getInstance() {
        if (instance == null) {
            instance = new DishRepository();
        }
        return instance;
    }
    
    /**
     * Gets all dishes for filter dropdowns
     * @return Task containing all dishes
     */
    public Task<QuerySnapshot> getAllDishes() {
        return db.collection(COLLECTION_DISHES)
                .orderBy("name")
                .get();
    }
    
    /**
     * Creates a new dish in Firestore
     * @param dish Dish object to create
     * @return Task containing the document reference
     */
    public Task<DocumentReference> createDish(Dish dish) {
        return db.collection(COLLECTION_DISHES)
                .add(dish);
    }
    
    /**
     * Adds a dish if it doesn't already exist
     * @param dishName Name of the dish to add
     */
    public void addDishIfNotExists(String dishName) {
        db.collection(COLLECTION_DISHES)
                .whereEqualTo("name", dishName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Dish dish = new Dish(dishName);
                        createDish(dish);
                    }
                });
    }
    
    /**
     * Gets dishes by tag
     * @param tag Tag to filter by
     * @return Task containing filtered dishes
     */
    public Task<QuerySnapshot> getDishesByTag(String tag) {
        return db.collection(COLLECTION_DISHES)
                .whereArrayContains("tags", tag)
                .get();
    }
    
    /**
     * Gets dishes by stall ID
     * @param stallId ID of the stall
     * @return Task containing the stall's dishes
     */
    public Task<QuerySnapshot> getDishesByStallId(String stallId) {
        return db.collection(COLLECTION_DISHES)
                .whereEqualTo("stallId", stallId)
                .get();
    }
}
