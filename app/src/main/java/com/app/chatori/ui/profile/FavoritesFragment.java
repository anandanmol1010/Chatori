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
import com.app.chatori.adapter.StallAdapter;
import com.app.chatori.model.Stall;
import com.app.chatori.model.User;
import com.app.chatori.repository.StallRepository;
import com.app.chatori.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for displaying the user's favorite stalls.
 */
public class FavoritesFragment extends Fragment {

    private RecyclerView rvFavorites;
    private ProgressBar progressBar;
    private LinearLayout emptyState;

    private UserRepository userRepository;
    private StallRepository stallRepository;
    private StallAdapter stallAdapter;
    private List<Stall> favoriteStalls = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);

        // Initialize repositories
        userRepository = UserRepository.getInstance();
        stallRepository = StallRepository.getInstance();

        // Initialize views
        rvFavorites = view.findViewById(R.id.rv_favorites);
        progressBar = view.findViewById(R.id.progress_bar);
        emptyState = view.findViewById(R.id.empty_state);

        // Set up RecyclerView
        setupRecyclerView();

        // Load user's favorite stalls
        loadFavoriteStalls();

        return view;
    }

    /**
     * Sets up the RecyclerView for stalls
     */
    private void setupRecyclerView() {
        stallAdapter = new StallAdapter(getContext(), favoriteStalls);
        rvFavorites.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFavorites.setAdapter(stallAdapter);
    }

    /**
     * Loads the user's favorite stalls from Firestore
     */
    private void loadFavoriteStalls() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        progressBar.setVisibility(View.VISIBLE);

        // First get the user to get their favorite stall IDs
        userRepository.getUserById(currentUser.getUid())
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null && user.getFavoriteStalls() != null && !user.getFavoriteStalls().isEmpty()) {
                            // Then get each stall by ID
                            loadStallsByIds(user.getFavoriteStalls());
                        } else {
                            // No favorite stalls
                            favoriteStalls.clear();
                            stallAdapter.updateStalls(favoriteStalls);
                            updateEmptyState();
                            progressBar.setVisibility(View.GONE);
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), getString(R.string.error_loading_favorites), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Loads stalls by their IDs
     */
    private void loadStallsByIds(List<String> stallIds) {
        favoriteStalls.clear();
        final int[] loadedCount = {0};
        final int totalCount = stallIds.size();

        for (String stallId : stallIds) {
            stallRepository.getStallById(stallId)
                    .addOnSuccessListener(documentSnapshot -> {
                        loadedCount[0]++;
                        
                        if (documentSnapshot.exists()) {
                            Stall stall = documentSnapshot.toObject(Stall.class);
                            if (stall != null) {
                                favoriteStalls.add(stall);
                            }
                        }
                        
                        // Check if all stalls have been loaded
                        if (loadedCount[0] >= totalCount) {
                            stallAdapter.updateStalls(favoriteStalls);
                            updateEmptyState();
                            progressBar.setVisibility(View.GONE);
                        }
                    })
                    .addOnFailureListener(e -> {
                        loadedCount[0]++;
                        
                        // Check if all stalls have been loaded
                        if (loadedCount[0] >= totalCount) {
                            stallAdapter.updateStalls(favoriteStalls);
                            updateEmptyState();
                            progressBar.setVisibility(View.GONE);
                        }
                    });
        }
    }

    /**
     * Shows or hides the empty state based on stalls list
     */
    private void updateEmptyState() {
        if (favoriteStalls.isEmpty()) {
            rvFavorites.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            rvFavorites.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload favorite stalls when fragment resumes
        loadFavoriteStalls();
    }
}
