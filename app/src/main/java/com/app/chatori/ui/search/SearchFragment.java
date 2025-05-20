package com.app.chatori.ui.search;

import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.app.chatori.repository.DishRepository;
import com.app.chatori.repository.StallRepository;
import com.app.chatori.utils.LocationUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Fragment for searching and filtering stalls.
 */
public class SearchFragment extends Fragment {

    private EditText etSearch;
    private ImageView ivClearSearch;
    private MaterialButton btnDishType, btnArea, btnRating, btnDistance;
    private LinearLayout filterChipsContainer;
    private ChipGroup chipGroup;
    private TextView tvClearFilters;
    private RecyclerView rvSearchResults;
    private LinearLayout emptyState;
    private ProgressBar progressBar;

    private StallRepository stallRepository;
    private DishRepository dishRepository;
    private StallAdapter stallAdapter;
    private List<Stall> allStalls = new ArrayList<>();
    private List<Stall> filteredStalls = new ArrayList<>();
    private Location currentLocation;

    // Filter states
    private String currentDishType = "";
    private String currentArea = "";
    private float minRating = 0;
    private boolean sortByDistance = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        // Initialize repositories
        stallRepository = StallRepository.getInstance();
        dishRepository = DishRepository.getInstance();

        // Initialize views
        etSearch = view.findViewById(R.id.et_search);
        ivClearSearch = view.findViewById(R.id.iv_clear_search);
        btnDishType = view.findViewById(R.id.btn_dish_type);
        btnArea = view.findViewById(R.id.btn_area);
        btnRating = view.findViewById(R.id.btn_rating);
        btnDistance = view.findViewById(R.id.btn_distance);
        filterChipsContainer = view.findViewById(R.id.filter_chips_container);
        chipGroup = view.findViewById(R.id.chip_group);
        tvClearFilters = view.findViewById(R.id.tv_clear_filters);
        rvSearchResults = view.findViewById(R.id.rv_search_results);
        emptyState = view.findViewById(R.id.empty_state);
        progressBar = view.findViewById(R.id.progress_bar);

        // Set up RecyclerView
        setupRecyclerView();

        // Set up search functionality
        setupSearch();

        // Set up filter buttons
        setupFilterButtons();

        // Get current location
        getCurrentLocation();

        // Load all stalls
        loadAllStalls();

        return view;
    }

    /**
     * Sets up the RecyclerView for stalls
     */
    private void setupRecyclerView() {
        stallAdapter = new StallAdapter(getContext(), filteredStalls);
        rvSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSearchResults.setAdapter(stallAdapter);
    }

    /**
     * Sets up search functionality
     */
    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim().toLowerCase();
                ivClearSearch.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
                filterStalls();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        ivClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            ivClearSearch.setVisibility(View.GONE);
        });
    }

    /**
     * Sets up filter buttons
     */
    private void setupFilterButtons() {
        btnDishType.setOnClickListener(v -> showDishTypeFilter());
        btnArea.setOnClickListener(v -> showAreaFilter());
        btnRating.setOnClickListener(v -> showRatingFilter());
        btnDistance.setOnClickListener(v -> {
            sortByDistance = !sortByDistance;
            btnDistance.setIconTint(getResources().getColorStateList(
                    sortByDistance ? R.color.colorPrimary : R.color.colorTextPrimary, null));
            filterStalls();
        });

        tvClearFilters.setOnClickListener(v -> clearFilters());
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
                            if (sortByDistance) {
                                filterStalls();
                            }
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
    private void loadAllStalls() {
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

        filterStalls();
        progressBar.setVisibility(View.GONE);
    }

    /**
     * Filters stalls based on search query and filters
     */
    private void filterStalls() {
        if (allStalls.isEmpty()) return;

        String query = etSearch.getText().toString().trim().toLowerCase();
        
        // Filter by search query, dish type, area, and rating
        filteredStalls.clear();
        for (Stall stall : allStalls) {
            boolean matchesQuery = query.isEmpty() ||
                    stall.getName().toLowerCase().contains(query) ||
                    stall.getDishType().toLowerCase().contains(query) ||
                    stall.getArea().toLowerCase().contains(query);
            
            boolean matchesDishType = currentDishType.isEmpty() ||
                    stall.getDishType().equalsIgnoreCase(currentDishType);
            
            boolean matchesArea = currentArea.isEmpty() ||
                    stall.getArea().equalsIgnoreCase(currentArea);
            
            boolean matchesRating = stall.getRating() >= minRating;
            
            if (matchesQuery && matchesDishType && matchesArea && matchesRating) {
                filteredStalls.add(stall);
            }
        }
        
        // Sort by distance if needed
        if (sortByDistance && currentLocation != null) {
            filteredStalls = LocationUtils.sortStallsByDistance(filteredStalls, currentLocation);
        } else {
            // Otherwise sort by rating
            Collections.sort(filteredStalls, (s1, s2) -> Float.compare(s2.getRating(), s1.getRating()));
        }
        
        // Update UI
        stallAdapter.updateStalls(filteredStalls);
        updateEmptyState();
    }

    /**
     * Shows or hides the empty state based on filtered stalls
     */
    private void updateEmptyState() {
        if (filteredStalls.isEmpty()) {
            rvSearchResults.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            rvSearchResults.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }
    }

    /**
     * Shows dish type filter options
     */
    private void showDishTypeFilter() {
        // For simplicity, we'll just use a few hard-coded dish types
        // In a real app, you would fetch these from the DishRepository
        String[] dishTypes = {"Chaat", "Momos", "Golgappe", "Tikki", "Rolls", "Biryani", "Chinese"};
        
        showFilterChips(dishTypes, "dish");
    }

    /**
     * Shows area filter options
     */
    private void showAreaFilter() {
        // For simplicity, we'll just use a few hard-coded areas
        // In a real app, you would extract these from the stalls
        String[] areas = {"Karol Bagh", "Connaught Place", "Chandni Chowk", "Lajpat Nagar", "Saket", "Hauz Khas"};
        
        showFilterChips(areas, "area");
    }

    /**
     * Shows rating filter options
     */
    private void showRatingFilter() {
        String[] ratings = {"4.5+", "4.0+", "3.5+", "3.0+"};
        
        showFilterChips(ratings, "rating");
    }

    /**
     * Shows filter chips for selection
     */
    private void showFilterChips(String[] options, String filterType) {
        chipGroup.removeAllViews();
        
        for (String option : options) {
            Chip chip = new Chip(getContext());
            chip.setText(option);
            chip.setCheckable(true);
            
            // Check if this option is currently selected
            boolean isSelected = false;
            if (filterType.equals("dish") && currentDishType.equals(option)) {
                isSelected = true;
            } else if (filterType.equals("area") && currentArea.equals(option)) {
                isSelected = true;
            } else if (filterType.equals("rating")) {
                float rating = Float.parseFloat(option.replace("+", ""));
                if (minRating == rating) {
                    isSelected = true;
                }
            }
            
            chip.setChecked(isSelected);
            
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    // Update filter state
                    if (filterType.equals("dish")) {
                        currentDishType = option;
                        btnDishType.setIconTint(getResources().getColorStateList(R.color.colorPrimary, null));
                    } else if (filterType.equals("area")) {
                        currentArea = option;
                        btnArea.setIconTint(getResources().getColorStateList(R.color.colorPrimary, null));
                    } else if (filterType.equals("rating")) {
                        minRating = Float.parseFloat(option.replace("+", ""));
                        btnRating.setIconTint(getResources().getColorStateList(R.color.colorPrimary, null));
                    }
                } else {
                    // Clear this filter if unchecked
                    if (filterType.equals("dish") && currentDishType.equals(option)) {
                        currentDishType = "";
                        btnDishType.setIconTint(getResources().getColorStateList(R.color.colorTextPrimary, null));
                    } else if (filterType.equals("area") && currentArea.equals(option)) {
                        currentArea = "";
                        btnArea.setIconTint(getResources().getColorStateList(R.color.colorTextPrimary, null));
                    } else if (filterType.equals("rating") && minRating == Float.parseFloat(option.replace("+", ""))) {
                        minRating = 0;
                        btnRating.setIconTint(getResources().getColorStateList(R.color.colorTextPrimary, null));
                    }
                }
                
                // Apply filters
                filterStalls();
            });
            
            chipGroup.addView(chip);
        }
        
        filterChipsContainer.setVisibility(View.VISIBLE);
    }

    /**
     * Clears all filters
     */
    private void clearFilters() {
        currentDishType = "";
        currentArea = "";
        minRating = 0;
        sortByDistance = false;
        
        btnDishType.setIconTint(getResources().getColorStateList(R.color.colorTextPrimary, null));
        btnArea.setIconTint(getResources().getColorStateList(R.color.colorTextPrimary, null));
        btnRating.setIconTint(getResources().getColorStateList(R.color.colorTextPrimary, null));
        btnDistance.setIconTint(getResources().getColorStateList(R.color.colorTextPrimary, null));
        
        chipGroup.removeAllViews();
        filterChipsContainer.setVisibility(View.GONE);
        
        filterStalls();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LocationUtils.LOCATION_PERMISSION_REQUEST_CODE) {
            getCurrentLocation();
        }
    }
}
