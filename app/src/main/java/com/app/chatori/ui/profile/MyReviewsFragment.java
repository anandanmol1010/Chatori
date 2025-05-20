package com.app.chatori.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.chatori.R;
import com.app.chatori.adapter.ReviewAdapter;
import com.app.chatori.model.Review;
import com.app.chatori.repository.ReviewRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for displaying the user's reviews.
 */
public class MyReviewsFragment extends Fragment {

    private RecyclerView rvMyReviews;
    private ProgressBar progressBar;
    private LinearLayout emptyState;

    private ReviewRepository reviewRepository;
    private ReviewAdapter reviewAdapter;
    private List<Review> myReviews = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_reviews, container, false);

        // Initialize repository
        reviewRepository = ReviewRepository.getInstance();

        // Initialize views
        rvMyReviews = view.findViewById(R.id.rv_my_reviews);
        progressBar = view.findViewById(R.id.progress_bar);
        emptyState = view.findViewById(R.id.empty_state);

        // Set up RecyclerView
        setupRecyclerView();

        // Load user's reviews
        loadMyReviews();

        return view;
    }

    /**
     * Sets up the RecyclerView for reviews
     */
    private void setupRecyclerView() {
        reviewAdapter = new ReviewAdapter(getContext(), myReviews);
        rvMyReviews.setLayoutManager(new LinearLayoutManager(getContext()));
        rvMyReviews.setAdapter(reviewAdapter);
    }

    /**
     * Loads the user's reviews from Firestore
     */
    private void loadMyReviews() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        progressBar.setVisibility(View.VISIBLE);

        reviewRepository.getReviewsByUserId(currentUser.getUid())
                .addOnSuccessListener(this::processReviews)
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), getString(R.string.error_loading_reviews), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Processes the reviews from Firestore and updates the UI
     */
    private void processReviews(QuerySnapshot querySnapshot) {
        myReviews.clear();
        
        for (DocumentSnapshot document : querySnapshot) {
            Review review = document.toObject(Review.class);
            if (review != null) {
                myReviews.add(review);
            }
        }

        reviewAdapter.updateReviews(myReviews);
        updateEmptyState();
        progressBar.setVisibility(View.GONE);
    }

    /**
     * Shows or hides the empty state based on reviews list
     */
    private void updateEmptyState() {
        if (myReviews.isEmpty()) {
            rvMyReviews.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            rvMyReviews.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload reviews when fragment resumes
        loadMyReviews();
    }
}
