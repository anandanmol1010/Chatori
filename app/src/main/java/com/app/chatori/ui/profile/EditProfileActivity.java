package com.app.chatori.ui.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.app.chatori.R;
import com.app.chatori.model.User;
import com.app.chatori.repository.UserRepository;
import com.app.chatori.utils.UIUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Activity for editing user profile information.
 */
public class EditProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private Toolbar toolbar;
    private ImageView ivProfileImage;
    private EditText etName, etBio, etPhone;
    private Button btnChangePhoto, btnSave;
    private ProgressBar progressBar;

    private UserRepository userRepository;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private Uri imageUri;
    private String currentImageUrl;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize Firebase
        userRepository = UserRepository.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        ivProfileImage = findViewById(R.id.iv_profile_image);
        etName = findViewById(R.id.et_name);
        etBio = findViewById(R.id.et_bio);
        etPhone = findViewById(R.id.et_phone);
        btnChangePhoto = findViewById(R.id.btn_change_photo);
        btnSave = findViewById(R.id.btn_save);
        progressBar = findViewById(R.id.progress_bar);

        // Set up toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.edit_profile);
        }

        // Set up click listeners
        setupClickListeners();

        // Load user data
        loadUserData();
    }

    /**
     * Sets up click listeners for buttons
     */
    private void setupClickListeners() {
        btnChangePhoto.setOnClickListener(v -> openImagePicker());

        btnSave.setOnClickListener(v -> saveUserData());
    }

    /**
     * Opens image picker to select profile image
     */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    /**
     * Loads user data from Firestore
     */
    private void loadUserData() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            finish();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        userRepository.getUserById(firebaseUser.getUid())
                .addOnSuccessListener(documentSnapshot -> {
                    progressBar.setVisibility(View.GONE);
                    if (documentSnapshot.exists()) {
                        currentUser = documentSnapshot.toObject(User.class);
                        if (currentUser != null) {
                            updateUI(currentUser);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, getString(R.string.error_loading_profile), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Updates the UI with user data
     */
    private void updateUI(User user) {
        // Load profile image
        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            currentImageUrl = user.getProfileImageUrl();
            UIUtils.loadProfileImage(this, currentImageUrl, ivProfileImage);
        }

        // Set user name
        etName.setText(user.getName());

        // Set user bio
        if (user.getBio() != null) {
            etBio.setText(user.getBio());
        }

        // Set user phone
        if (user.getPhone() != null) {
            etPhone.setText(user.getPhone());
        }
    }

    /**
     * Saves user data to Firestore
     */
    private void saveUserData() {
        String name = etName.getText().toString().trim();
        String bio = etBio.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        // Validate name
        if (name.isEmpty()) {
            etName.setError(getString(R.string.error_name_required));
            etName.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // If image was changed, skip image upload
        if (imageUri != null) {
            handleProfileImageWithoutUpload(name, bio, phone);
        } else {
            // Otherwise just update user data
            updateUserData(name, bio, phone, currentImageUrl);
        }
    }

    /**
     * Skips image upload and updates user data without an image URL.
     */
    private void handleProfileImageWithoutUpload(String name, String bio, String phone) {
        // Directly update user data without uploading image
        updateUserData(name, bio, phone, null);
        // Notify user that image upload is skipped
        Toast.makeText(this, "Profile image upload skipped due to billing constraints.", Toast.LENGTH_SHORT).show();
    }

    /**
     * Updates user data in Firestore
     */
    private void updateUserData(String name, String bio, String phone, String profileImageUrl) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            progressBar.setVisibility(View.GONE);
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("bio", bio);
        updates.put("phone", phone);
        updates.put("profileImageUrl", profileImageUrl);

        userRepository.updateUser(firebaseUser.getUid(), updates)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, getString(R.string.profile_updated), Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, getString(R.string.error_updating_profile), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            ivProfileImage.setImageURI(imageUri);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
