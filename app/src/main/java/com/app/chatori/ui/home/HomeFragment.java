package com.app.chatori.ui.home;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.chatori.R;
import com.app.chatori.adapter.StallAdapter;
import com.app.chatori.model.Stall;
import com.app.chatori.repository.StallRepository;
import com.app.chatori.ui.search.SearchActivity;
import com.app.chatori.utils.LocationUtils;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Home fragment that displays recommended, nearby, and top-rated stalls.
 */
public class HomeFragment extends Fragment {

    private RecyclerView rvRecommendedStalls, rvNearbyStalls, rvTopRatedStalls;
    private TextView tvViewAllRecommended, tvViewAllNearby, tvViewAllTopRated;
    private LinearLayout searchContainer;
    private ProgressBar progressBar;

    private StallRepository stallRepository;
    private StallAdapter recommendedAdapter, nearbyAdapter, topRatedAdapter;
    private List<Stall> allStalls = new ArrayList<>();
    private Location currentLocation;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize repository
        stallRepository = StallRepository.getInstance();

        // Initialize views
        rvRecommendedStalls = view.findViewById(R.id.rv_recommended_stalls);
        rvNearbyStalls = view.findViewById(R.id.rv_nearby_stalls);
        rvTopRatedStalls = view.findViewById(R.id.rv_top_rated_stalls);
        tvViewAllRecommended = view.findViewById(R.id.tv_view_all_recommended);
        tvViewAllNearby = view.findViewById(R.id.tv_view_all_nearby);
        tvViewAllTopRated = view.findViewById(R.id.tv_view_all_top_rated);
        searchContainer = view.findViewById(R.id.search_container);
        progressBar = view.findViewById(R.id.progress_bar);

        // Set up RecyclerViews
        setupRecyclerViews();

        // Set up click listeners
        setupClickListeners();

        // Get current location
        getCurrentLocation();

        // Load stalls
        loadStalls();

        return view;
    }

    /**
     * Sets up the RecyclerViews for stalls
     */
    private void setupRecyclerViews() {
        // Recommended stalls
        recommendedAdapter = new StallAdapter(getContext(), new ArrayList<>());
        rvRecommendedStalls.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvRecommendedStalls.setAdapter(recommendedAdapter);

        // Nearby stalls
        nearbyAdapter = new StallAdapter(getContext(), new ArrayList<>());
        rvNearbyStalls.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvNearbyStalls.setAdapter(nearbyAdapter);

        // Top rated stalls
        topRatedAdapter = new StallAdapter(getContext(), new ArrayList<>());
        rvTopRatedStalls.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvTopRatedStalls.setAdapter(topRatedAdapter);
    }

    /**
     * Sets up click listeners for views
     */
    private void setupClickListeners() {
        // Search container click
        searchContainer.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), SearchActivity.class);
            startActivity(intent);
        });

        // View all buttons
        tvViewAllRecommended.setOnClickListener(v -> navigateToSearchWithFilter("recommended"));
        tvViewAllNearby.setOnClickListener(v -> navigateToSearchWithFilter("nearby"));
        tvViewAllTopRated.setOnClickListener(v -> navigateToSearchWithFilter("top_rated"));
    }

    /**
     * Gets the current location of the device
     */
    private void getCurrentLocation() {
        if (getContext() == null) return;

        if (LocationUtils.hasLocationPermission(getContext())) {
            LocationUtils.getLastLocation(getContext())
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            currentLocation = location;
                            updateNearbyStalls();
                        }
                    });
        } else {
            if (getActivity() != null) {
                LocationUtils.requestLocationPermission(getActivity());
            }
        }
    }

    /**
     * Loads all stalls from Firestore
     */
    private void loadStalls() {
        progressBar.setVisibility(View.VISIBLE);

        stallRepository.getAllStalls()
                .addOnSuccessListener(this::processStalls)
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), getString(R.string.error_loading_stalls), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Processes the stalls from Firestore and updates the UI
     */
    private void processStalls(QuerySnapshot querySnapshot) {
        allStalls.clear();
        
        for (DocumentSnapshot document : querySnapshot) {
            Stall stall = document.toObject(Stall.class);
            if (stall != null) {
                allStalls.add(stall);
            }
        }

        // Update all adapters
        updateRecommendedStalls();
        updateNearbyStalls();
        updateTopRatedStalls();

        progressBar.setVisibility(View.GONE);
    }

    /**
     * Updates the recommended stalls section
     * For now, we'll just show a random selection of stalls
     */
    private void updateRecommendedStalls() {
        if (allStalls.isEmpty()) return;

        List<Stall> recommendedStalls = new ArrayList<>(allStalls);
        Collections.shuffle(recommendedStalls);
        
        // Limit to 10 stalls
        int limit = Math.min(recommendedStalls.size(), 10);
        recommendedStalls = recommendedStalls.subList(0, limit);
        
        recommendedAdapter.updateStalls(recommendedStalls);
    }

    /**
     * Updates the nearby stalls section based on current location
     */
    private void updateNearbyStalls() {
        if (allStalls.isEmpty() || currentLocation == null) return;

        // Sort stalls by distance
        List<Stall> nearbyStalls = LocationUtils.sortStallsByDistance(allStalls, currentLocation);
        
        // Limit to 10 stalls
        int limit = Math.min(nearbyStalls.size(), 10);
        nearbyStalls = nearbyStalls.subList(0, limit);
        
        nearbyAdapter.updateStalls(nearbyStalls);
    }

    /**
     * Updates the top rated stalls section
     */
    private void updateTopRatedStalls() {
        if (allStalls.isEmpty()) return;

        List<Stall> topRatedStalls = new ArrayList<>(allStalls);
        
        // Sort by rating (highest first)
        Collections.sort(topRatedStalls, (s1, s2) -> Float.compare(s2.getRating(), s1.getRating()));
        
        // Limit to 10 stalls
        int limit = Math.min(topRatedStalls.size(), 10);
        topRatedStalls = topRatedStalls.subList(0, limit);
        
        topRatedAdapter.updateStalls(topRatedStalls);
    }

    /**
     * Navigates to the search activity with a filter
     */
    private void navigateToSearchWithFilter(String filterType) {
        Intent intent = new Intent(getContext(), SearchActivity.class);
        intent.putExtra("filter_type", filterType);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LocationUtils.LOCATION_PERMISSION_REQUEST_CODE) {
            getCurrentLocation();
        }
    }
}
