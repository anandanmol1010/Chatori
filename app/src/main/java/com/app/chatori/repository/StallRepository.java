package com.app.chatori.repository;

import androidx.lifecycle.MutableLiveData;

import com.app.chatori.model.Stall;
import com.app.chatori.utils.StallDataHelper;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository class for handling Stall data operations with Firestore.
 */
public class StallRepository {
    private static final String COLLECTION_STALLS = "Stalls";
    
    private final FirebaseFirestore db;
    
    // Singleton instance
    private static StallRepository instance;
    
    private StallRepository() {
        db = FirebaseFirestore.getInstance();
    }
    
    public static synchronized StallRepository getInstance() {
        if (instance == null) {
            instance = new StallRepository();
        }
        return instance;
    }
    
    /**
     * Creates a new stall in Firestore
     * @param stall Stall object to create
     * @return Task containing the document reference
     */
    public Task<DocumentReference> createStall(Stall stall) {
        // Generate ID first if not already set
        if (stall.getStallId() == null || stall.getStallId().isEmpty()) {
            stall.setStallId(generateStallId());
        }
        
        // Validate GeoPoint
        if (stall.getLocation() == null) {
            stall.setLocation(new com.google.firebase.firestore.GeoPoint(0, 0));
        }
        
        // Set creation date if not set
        if (stall.getCreatedAt() == null) {
            stall.setCreatedAt(new java.util.Date());
        }
        
        return db.collection(COLLECTION_STALLS)
                .document(stall.getStallId())
                .set(stall)
                .continueWithTask(task -> {
                    if (task.isSuccessful()) {
                        return Tasks.forResult(db.collection(COLLECTION_STALLS).document(stall.getStallId()));
                    } else {
                        return Tasks.forException(task.getException());
                    }
                });
    }
    
    /**
     * Gets a stall by its ID
     * @param stallId ID of the stall to retrieve
     * @return Task containing the stall document
     */
    public Task<DocumentSnapshot> getStallById(String stallId) {
        return db.collection(COLLECTION_STALLS)
                .document(stallId)
                .get();
    }
    
    /**
     * Gets all stalls
     * @return Task containing all stalls
     */
    public Task<QuerySnapshot> getAllStalls() {
        return db.collection(COLLECTION_STALLS)
                .orderBy("rating", Query.Direction.DESCENDING)
                .get();
    }
    
    /**
     * Gets stalls by dish type
     * @param dishType Type of dish to filter by
     * @return Task containing filtered stalls
     */
    public Task<QuerySnapshot> getStallsByDishType(String dishType) {
        return db.collection(COLLECTION_STALLS)
                .whereEqualTo("dishType", dishType)
                .orderBy("rating", Query.Direction.DESCENDING)
                .get();
    }
    
    /**
     * Gets stalls by area
     * @param area Area to filter by
     * @return Task containing filtered stalls
     */
    public Task<QuerySnapshot> getStallsByArea(String area) {
        return db.collection(COLLECTION_STALLS)
                .whereEqualTo("area", area)
                .orderBy("rating", Query.Direction.DESCENDING)
                .get();
    }
    
    /**
     * Gets stalls by dish type and area
     * @param dishType Type of dish to filter by
     * @param area Area to filter by
     * @return Task containing filtered stalls
     */
    public Task<QuerySnapshot> getStallsByDishTypeAndArea(String dishType, String area) {
        return db.collection(COLLECTION_STALLS)
                .whereEqualTo("dishType", dishType)
                .whereEqualTo("area", area)
                .orderBy("rating", Query.Direction.DESCENDING)
                .get();
    }
    
    /**
     * Gets stalls created by a specific user
     * @param userId ID of the user who created the stalls
     * @return Task containing the user's stalls
     */
    public Task<QuerySnapshot> getStallsByUser(String userId) {
        return db.collection(COLLECTION_STALLS)
                .whereEqualTo("createdBy", userId)
                .get();
    }
    
    /**
     * Gets stalls created by a specific user (alias method)
     * @param userId ID of the user who created the stalls
     * @return Task containing the user's stalls
     */
    public Task<QuerySnapshot> getStallsByUserId(String userId) {
        return getStallsByUser(userId);
    }
    
    /**
     * Updates a stall
     * @param stall Stall object with updated data
     * @return Task for the operation
     */
    public Task<Void> updateStall(Stall stall) {
        return db.collection(COLLECTION_STALLS)
                .document(stall.getStallId())
                .set(stall);
    }
    
    /**
     * Adds an image URL to a stall
     * @param stallId ID of the stall
     * @param imageUrl URL of the image to add
     * @return Task for the operation
     */
    public Task<Void> addStallImage(String stallId, String imageUrl) {
        DocumentReference stallRef = db.collection(COLLECTION_STALLS).document(stallId);
        return db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(stallRef);
            Stall stall = snapshot.toObject(Stall.class);
            if (stall != null) {
                stall.addImage(imageUrl);
                transaction.set(stallRef, stall);
            }
            return null;
        });
    }
    
    /**
     * Updates a stall's rating when a new review is added
     * @param stallId ID of the stall
     * @param newRating New rating to incorporate
     * @return Task for the operation
     */
    public Task<Void> updateStallRating(String stallId, float newRating) {
        DocumentReference stallRef = db.collection(COLLECTION_STALLS).document(stallId);
        return db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(stallRef);
            Stall stall = snapshot.toObject(Stall.class);
            if (stall != null) {
                stall.updateRating(newRating);
                transaction.set(stallRef, stall);
            }
            return null;
        });
    }
    
    /**
     * Searches for stalls by name, dish type, or area
     * @param query Search query
     * @return LiveData containing the search results
     */
    public MutableLiveData<List<Stall>> searchStalls(String query) {
        MutableLiveData<List<Stall>> stallsLiveData = new MutableLiveData<>();
        
        // Normalize the query
        String normalizedQuery = query.toLowerCase().trim();
        
        // Search by name, dish type, and area
        db.collection(COLLECTION_STALLS)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Stall> stalls = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Stall stall = document.toObject(Stall.class);
                        if (stall != null) {
                            // Check if the stall matches the search query
                            if (stall.getName().toLowerCase().contains(normalizedQuery) ||
                                    stall.getDishType().toLowerCase().contains(normalizedQuery) ||
                                    stall.getArea().toLowerCase().contains(normalizedQuery)) {
                                stalls.add(stall);
                            }
                        }
                    }
                    stallsLiveData.setValue(stalls);
                })
                .addOnFailureListener(e -> stallsLiveData.setValue(new ArrayList<>()));
        
        return stallsLiveData;
    }
    
    /**
     * Adds a stall to Firestore (alias for createStall)
     * @param stall Stall object to add
     * @return Task containing the document reference
     */
    public Task<DocumentReference> addStall(Stall stall) {
        return createStall(stall);
    }
    
    /**
     * Generates a unique stall ID
     * @return A unique stall ID
     */
    public String generateStallId() {
        return db.collection(COLLECTION_STALLS).document().getId();
    }
}
