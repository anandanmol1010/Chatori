package com.app.chatori.ui.review;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.app.chatori.R;
import com.app.chatori.model.Review;
import com.app.chatori.model.User;
import com.app.chatori.repository.ReviewRepository;
import com.app.chatori.repository.StallRepository;
import com.app.chatori.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Activity for writing a review for a stall.
 */
public class WriteReviewActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvStallName;
    private RatingBar ratingBar;
    private EditText etReviewText;
    private Button btnSubmit;
    private ProgressBar progressBar;

    private ReviewRepository reviewRepository;
    private StallRepository stallRepository;
    private UserRepository userRepository;
    
    private String stallId;
    private String stallName;
    private float rating = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_review);

        // Get stall ID and name from intent
        stallId = getIntent().getStringExtra("stall_id");
        stallName = getIntent().getStringExtra("stall_name");
        if (stallId == null) {
            finish();
            return;
        }

        // Initialize repositories
        reviewRepository = ReviewRepository.getInstance();
        stallRepository = StallRepository.getInstance();
        userRepository = UserRepository.getInstance();

        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        tvStallName = findViewById(R.id.tv_stall_name);
        ratingBar = findViewById(R.id.rating_bar);
        etReviewText = findViewById(R.id.et_review_text);
        btnSubmit = findViewById(R.id.btn_submit);
        progressBar = findViewById(R.id.progress_bar);

        // Set up toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.write_review);
        }

        // Set stall name
        if (stallName != null) {
            tvStallName.setText(stallName);
        }

        // Set up rating bar listener
        ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            this.rating = rating;
        });

        // Set up submit button listener
        btnSubmit.setOnClickListener(v -> submitReview());
    }

    /**
     * Submits the review to Firestore
     */
    private void submitReview() {
        // Check if user is logged in
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, getString(R.string.error_login_required), Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate rating
        if (rating == 0) {
            Toast.makeText(this, getString(R.string.error_rating_required), Toast.LENGTH_SHORT).show();
            return;
        }

        // Get review text
        String reviewText = etReviewText.getText().toString().trim();
        if (reviewText.isEmpty()) {
            etReviewText.setError(getString(R.string.error_review_text_required));
            etReviewText.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSubmit.setEnabled(false);

        // Get user data
        userRepository.getUserById(currentUser.getUid())
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            // Create review object
                            Review review = new Review();
                            review.setReviewId(reviewRepository.generateReviewId());
                            review.setStallId(stallId);
                            review.setStallName(stallName);
                            review.setUserId(currentUser.getUid());
                            review.setUserName(user.getName());
                            review.setUserProfileImageUrl(user.getProfileImageUrl());
                            review.setRating(rating);
                            review.setText(reviewText);
                            review.setCreatedAt(new Date());

                            // Add review to Firestore
                            reviewRepository.addReview(review)
                                    .addOnSuccessListener(aVoid -> {
                                        // Update stall rating
                                        updateStallRating();
                                    })
                                    .addOnFailureListener(e -> {
                                        progressBar.setVisibility(View.GONE);
                                        btnSubmit.setEnabled(true);
                                        Toast.makeText(this, getString(R.string.error_submitting_review), Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            progressBar.setVisibility(View.GONE);
                            btnSubmit.setEnabled(true);
                            Toast.makeText(this, getString(R.string.error_user_not_found), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                        btnSubmit.setEnabled(true);
                        Toast.makeText(this, getString(R.string.error_user_not_found), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnSubmit.setEnabled(true);
                    Toast.makeText(this, getString(R.string.error_loading_user), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Updates the stall's rating in Firestore
     */
    private void updateStallRating() {
        stallRepository.updateStallRating(stallId, rating)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, getString(R.string.review_submitted), Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnSubmit.setEnabled(true);
                    Toast.makeText(this, getString(R.string.error_updating_rating), Toast.LENGTH_SHORT).show();
                });
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
