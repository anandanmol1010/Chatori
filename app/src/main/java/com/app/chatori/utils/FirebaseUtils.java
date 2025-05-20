package com.app.chatori.utils;

import android.net.Uri;

import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

/**
 * Utility class for Firebase operations, particularly Storage.
 */
public class FirebaseUtils {
    private static final String STORAGE_STALLS = "stalls";
    private static final String STORAGE_USERS = "users";
    
    private static final FirebaseStorage storage = FirebaseStorage.getInstance();
    
    /**
     * Uploads a stall image to Firebase Storage
     * @param stallId ID of the stall
     * @param imageUri URI of the image to upload
     * @return UploadTask for the operation
     */
    public static UploadTask uploadStallImage(String stallId, Uri imageUri) {
        String imageName = UUID.randomUUID().toString();
        StorageReference imageRef = storage.getReference()
                .child(STORAGE_STALLS)
                .child(stallId)
                .child(imageName);
        
        return imageRef.putFile(imageUri);
    }
    
    /**
     * Uploads a user profile image to Firebase Storage
     * @param userId ID of the user
     * @param imageUri URI of the image to upload
     * @return UploadTask for the operation
     */
    public static UploadTask uploadProfileImage(String userId, Uri imageUri) {
        String imageName = "profile.jpg";
        StorageReference imageRef = storage.getReference()
                .child(STORAGE_USERS)
                .child(userId)
                .child(imageName);
        
        return imageRef.putFile(imageUri);
    }
    
    /**
     * Gets the download URL for an uploaded image
     * @param uploadTask Completed upload task
     * @return Task containing the download URL
     */
    public static Task<Uri> getDownloadUrl(UploadTask.TaskSnapshot uploadTaskSnapshot) {
        return uploadTaskSnapshot.getStorage().getDownloadUrl();
    }
    
    /**
     * Deletes an image from Firebase Storage
     * @param imageUrl URL of the image to delete
     * @return Task for the operation
     */
    public static Task<Void> deleteImage(String imageUrl) {
        return storage.getReferenceFromUrl(imageUrl).delete();
    }
}
