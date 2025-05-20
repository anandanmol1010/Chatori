package com.app.chatori.repository;

import com.app.chatori.model.Review;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

/**
 * Repository class for handling Review data operations with Firestore.
 */
public class ReviewRepository {
    private static final String COLLECTION_REVIEWS = "Reviews";
    
    private final FirebaseFirestore db;
    private final StallRepository stallRepository;
    
    // Singleton instance
    private static ReviewRepository instance;
    
    private ReviewRepository() {
        db = FirebaseFirestore.getInstance();
        stallRepository = StallRepository.getInstance();
    }
    
    public static synchronized ReviewRepository getInstance() {
        if (instance == null) {
            instance = new ReviewRepository();
        }
        return instance;
    }
    
    /**
     * Creates a new review in Firestore and updates the stall's rating
     * @param review Review object to create
     * @return Task containing the document reference
     */
    public Task<DocumentReference> createReview(Review review) {
        // First add the review
        return db.collection(COLLECTION_REVIEWS)
                .add(review)
                .addOnSuccessListener(documentReference -> {
                    // Set the review ID
                    String reviewId = documentReference.getId();
                    review.setReviewId(reviewId);
                    documentReference.set(review);
                    
                    // Update the stall's rating
                    stallRepository.updateStallRating(review.getStallId(), review.getRating());
                });
    }
    
    /**
     * Gets reviews for a specific stall
     * @param stallId ID of the stall
     * @return Task containing the stall's reviews
     */
    public Task<QuerySnapshot> getReviewsByStall(String stallId) {
        return db.collection(COLLECTION_REVIEWS)
                .whereEqualTo("stallId", stallId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get();
    }
    
    /**
     * Gets reviews for a specific stall (alias method)
     * @param stallId ID of the stall
     * @return Task containing the stall's reviews
     */
    public Task<QuerySnapshot> getReviewsByStallId(String stallId) {
        return getReviewsByStall(stallId);
    }
    
    /**
     * Gets reviews for a specific stall with limit
     * @param stallId ID of the stall
     * @param limit Maximum number of reviews to retrieve
     * @return Task containing the stall's reviews
     */
    public Task<QuerySnapshot> getReviewsByStallId(String stallId, int limit) {
        return db.collection(COLLECTION_REVIEWS)
                .whereEqualTo("stallId", stallId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit)
                .get();
    }
    
    /**
     * Gets reviews by a specific user
     * @param userId ID of the user
     * @return Task containing the user's reviews
     */
    public Task<QuerySnapshot> getReviewsByUser(String userId) {
        return db.collection(COLLECTION_REVIEWS)
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get();
    }
    
    /**
     * Gets reviews by a specific user (alias method)
     * @param userId ID of the user
     * @return Task containing the user's reviews
     */
    public Task<QuerySnapshot> getReviewsByUserId(String userId) {
        return getReviewsByUser(userId);
    }
    
    /**
     * Updates a review
     * @param review Review object with updated data
     * @return Task for the operation
     */
    public Task<Void> updateReview(Review review) {
        return db.collection(COLLECTION_REVIEWS)
                .document(review.getReviewId())
                .set(review);
    }
    
    /**
     * Deletes a review
     * @param reviewId ID of the review to delete
     * @return Task for the operation
     */
    public Task<Void> deleteReview(String reviewId) {
        return db.collection(COLLECTION_REVIEWS)
                .document(reviewId)
                .delete();
    }
    
    /**
     * Checks if a user has already reviewed a stall
     * @param userId ID of the user
     * @param stallId ID of the stall
     * @return Task containing the query result
     */
    public Task<QuerySnapshot> hasUserReviewedStall(String userId, String stallId) {
        return db.collection(COLLECTION_REVIEWS)
                .whereEqualTo("userId", userId)
                .whereEqualTo("stallId", stallId)
                .limit(1)
                .get();
    }
    
    /**
     * Adds a review to Firestore (alias for createReview)
     * @param review Review object to add
     * @return Task containing the document reference
     */
    public Task<DocumentReference> addReview(Review review) {
        return createReview(review);
    }
    
    /**
     * Generates a unique review ID
     * @return A unique review ID
     */
    public String generateReviewId() {
        return db.collection(COLLECTION_REVIEWS).document().getId();
    }
}
