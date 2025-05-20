package com.app.chatori.ui.review;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.chatori.R;
import com.app.chatori.adapter.ReviewAdapter;
import com.app.chatori.model.Review;
import com.app.chatori.repository.ReviewRepository;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for displaying all reviews for a stall.
 */
public class AllReviewsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView rvReviews;
    private ProgressBar progressBar;
    private LinearLayout emptyState;
    private TextView tvStallName;

    private ReviewRepository reviewRepository;
    private ReviewAdapter reviewAdapter;
    private List<Review> reviews = new ArrayList<>();
    
    private String stallId;
    private String stallName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_reviews);

        // Get stall ID and name from intent
        stallId = getIntent().getStringExtra("stall_id");
        stallName = getIntent().getStringExtra("stall_name");
        if (stallId == null) {
            finish();
            return;
        }

        // Initialize repository
        reviewRepository = ReviewRepository.getInstance();

        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        rvReviews = findViewById(R.id.rv_reviews);
        progressBar = findViewById(R.id.progress_bar);
        emptyState = findViewById(R.id.empty_state);
        tvStallName = findViewById(R.id.tv_stall_name);

        // Set up toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.all_reviews);
        }

        // Set stall name
        if (stallName != null) {
            tvStallName.setText(stallName);
        }

        // Set up RecyclerView
        setupRecyclerView();

        // Load reviews
        loadReviews();
    }

    /**
     * Sets up the RecyclerView for reviews
     */
    private void setupRecyclerView() {
        reviewAdapter = new ReviewAdapter(this, reviews, false);
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        rvReviews.setAdapter(reviewAdapter);
    }

    /**
     * Loads reviews for the stall from Firestore
     */
    private void loadReviews() {
        progressBar.setVisibility(View.VISIBLE);

        reviewRepository.getReviewsByStallId(stallId)
                .addOnSuccessListener(this::processReviews)
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, getString(R.string.error_loading_reviews), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Processes the reviews from Firestore and updates the UI
     */
    private void processReviews(QuerySnapshot querySnapshot) {
        reviews.clear();
        
        for (DocumentSnapshot document : querySnapshot) {
            Review review = document.toObject(Review.class);
            if (review != null) {
                reviews.add(review);
            }
        }

        reviewAdapter.updateReviews(reviews);
        updateEmptyState();
        progressBar.setVisibility(View.GONE);
    }

    /**
     * Shows or hides the empty state based on reviews list
     */
    private void updateEmptyState() {
        if (reviews.isEmpty()) {
            rvReviews.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            rvReviews.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
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
