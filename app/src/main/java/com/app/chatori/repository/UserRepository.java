package com.app.chatori.repository;

import androidx.lifecycle.MutableLiveData;

import com.app.chatori.model.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Repository class for handling User data operations with Firestore.
 */
public class UserRepository {
    private static final String COLLECTION_USERS = "Users";
    
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    
    // Singleton instance
    private static UserRepository instance;
    
    // LiveData for current user
    private final MutableLiveData<User> currentUserLiveData = new MutableLiveData<>();
    
    private UserRepository() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }
    
    public static synchronized UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }
        return instance;
    }
    
    /**
     * Creates a new user in Firestore after authentication
     * @param user User object to create
     * @return Task for the operation
     */
    public Task<Void> createUser(User user) {
        return db.collection(COLLECTION_USERS)
                .document(user.getUserId())
                .set(user);
    }
    
    /**
     * Gets a user by their ID
     * @param userId ID of the user to retrieve
     * @return Task containing the user document
     */
    public Task<DocumentSnapshot> getUserById(String userId) {
        return db.collection(COLLECTION_USERS)
                .document(userId)
                .get();
    }
    
    /**
     * Updates a user's profile
     * @param user User object with updated data
     * @return Task for the operation
     */
    public Task<Void> updateUser(User user) {
        return db.collection(COLLECTION_USERS)
                .document(user.getUserId())
                .set(user);
    }
    
    /**
     * Updates a user's profile image URL
     * @param userId ID of the user
     * @param imageUrl New profile image URL
     * @return Task for the operation
     */
    public Task<Void> updateProfileImage(String userId, String imageUrl) {
        return db.collection(COLLECTION_USERS)
                .document(userId)
                .update("profileImageUrl", imageUrl);
    }
    
    /**
     * Updates specific fields of a user's profile
     * @param userId ID of the user
     * @param updates Map of field names and values to update
     * @return Task for the operation
     */
    public Task<Void> updateUser(String userId, Map<String, Object> updates) {
        return db.collection(COLLECTION_USERS)
                .document(userId)
                .update(updates);
    }
    
    /**
     * Adds a stall to user's favorites
     * @param userId ID of the user
     * @param stallId ID of the stall to add to favorites
     * @return Task for the operation
     */
    public Task<Void> addToFavorites(String userId, String stallId) {
        DocumentReference userRef = db.collection(COLLECTION_USERS).document(userId);
        return db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(userRef);
            User user = snapshot.toObject(User.class);
            if (user != null) {
                user.addFavorite(stallId);
                transaction.set(userRef, user);
            }
            return null;
        });
    }
    
    /**
     * Removes a stall from user's favorites
     * @param userId ID of the user
     * @param stallId ID of the stall to remove from favorites
     * @return Task for the operation
     */
    public Task<Void> removeFromFavorites(String userId, String stallId) {
        DocumentReference userRef = db.collection(COLLECTION_USERS).document(userId);
        return db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(userRef);
            User user = snapshot.toObject(User.class);
            if (user != null) {
                List<String> favorites = user.getFavorites();
                if (favorites != null) {
                    favorites.remove(stallId);
                    user.setFavorites(favorites);
                    transaction.set(userRef, user);
                }
            }
            return null;
        });
    }
    
    /**
     * Toggles a stall in user's favorites (adds if not present, removes if present)
     * @param userId ID of the user
     * @param stallId ID of the stall to toggle in favorites
     * @return Task for the operation
     */
    public Task<Void> toggleFavoriteStall(String userId, String stallId) {
        DocumentReference userRef = db.collection(COLLECTION_USERS).document(userId);
        return db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(userRef);
            User user = snapshot.toObject(User.class);
            if (user != null) {
                List<String> favorites = user.getFavorites();
                if (favorites == null) {
                    favorites = new ArrayList<>();
                }
                
                if (favorites.contains(stallId)) {
                    favorites.remove(stallId);
                } else {
                    favorites.add(stallId);
                }
                
                user.setFavorites(favorites);
                transaction.set(userRef, user);
            }
            return null;
        });
    }
    
    /**
     * Gets the currently authenticated user from Firestore
     * @return LiveData containing the current user
     */
    public MutableLiveData<User> getCurrentUser() {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser != null) {
            getUserById(firebaseUser.getUid())
                .addOnSuccessListener(documentSnapshot -> {
                    User user = documentSnapshot.toObject(User.class);
                    currentUserLiveData.setValue(user);
                });
        } else {
            currentUserLiveData.setValue(null);
        }
        return currentUserLiveData;
    }
}
