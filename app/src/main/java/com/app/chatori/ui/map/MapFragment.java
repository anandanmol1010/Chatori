package com.app.chatori.ui.map;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.app.chatori.R;
import com.app.chatori.model.Stall;
import com.app.chatori.repository.StallRepository;
import com.app.chatori.ui.stall.StallDetailActivity;
import com.app.chatori.utils.LocationUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fragment for displaying stalls on a map.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ProgressBar progressBar;
    private ImageButton btnMyLocation;
    private MaterialButton btnFilter;
    private ChipGroup chipGroupRadius;
    private MaterialButton btnDishType;
    private ChipGroup chipGroupDishType;

    private StallRepository stallRepository;
    private List<Stall> allStalls = new ArrayList<>();
    private Map<String, Stall> markerStallMap = new HashMap<>();
    private Location currentLocation;

    // Filter states
    private double currentRadius = 5.0; // Default 5 km
    private String currentDishType = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        // Initialize repository
        stallRepository = StallRepository.getInstance();

        // Initialize views
        progressBar = view.findViewById(R.id.progress_bar);
        btnMyLocation = view.findViewById(R.id.fab_my_location);
        // btnFilter is not in the layout, so we'll use btnDishType instead
        btnDishType = view.findViewById(R.id.btn_dish_type);
        chipGroupRadius = view.findViewById(R.id.chip_group);
        chipGroupDishType = view.findViewById(R.id.chip_group); // Using the same chip group for both

        // Set up map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Set up click listeners
        setupClickListeners();

        // Get current location
        getCurrentLocation();

        return view;
    }

    /**
     * Sets up click listeners for buttons and chips
     */
    private void setupClickListeners() {
        // Set up My Location button
        if (btnMyLocation != null) {
            btnMyLocation.setOnClickListener(v -> {
                if (mMap != null && currentLocation != null) {
                    LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                } else {
                    getCurrentLocation();
                }
            });
        }

        // Use btnDishType as the filter button
        if (btnDishType != null) {
            btnDishType.setOnClickListener(v -> {
                if (chipGroupRadius != null) {
                    if (chipGroupRadius.getVisibility() == View.VISIBLE) {
                        chipGroupRadius.setVisibility(View.GONE);
                    } else {
                        chipGroupRadius.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
        
        // Set up radius chips
        setupRadiusChips();

        // Set up dish type chips
        setupDishTypeChips();
    }

    /**
     * Sets up radius filter chips
     */
    private void setupRadiusChips() {
        // Only proceed if chipGroupRadius is not null
        if (chipGroupRadius == null) return;
        
        String[] radiusOptions = {"1 km", "2 km", "5 km", "10 km", "All"};
        double[] radiusValues = {1.0, 2.0, 5.0, 10.0, -1.0}; // -1 means no limit

        chipGroupRadius.removeAllViews();

        for (int i = 0; i < radiusOptions.length; i++) {
            Chip chip = new Chip(getContext());
            chip.setText(radiusOptions[i]);
            chip.setCheckable(true);
            chip.setChecked(radiusValues[i] == currentRadius);

            final double radius = radiusValues[i];
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    currentRadius = radius;
                    filterStalls();
                    chipGroupRadius.setVisibility(View.GONE);
                }
            });

            chipGroupRadius.addView(chip);
        }
    }

    /**
     * Sets up dish type filter chips
     */
    private void setupDishTypeChips() {
        // Only proceed if chipGroupDishType is not null
        if (chipGroupDishType == null) return;
        
        // For simplicity, we'll just use a few hard-coded dish types
        // In a real app, you would fetch these from the DishRepository
        String[] dishTypes = {"All", "Chaat", "Momos", "Golgappe", "Tikki", "Rolls", "Biryani", "Chinese"};

        chipGroupDishType.removeAllViews();

        for (String dishType : dishTypes) {
            Chip chip = new Chip(getContext());
            chip.setText(dishType);
            chip.setCheckable(true);
            chip.setChecked(dishType.equals("All") ? currentDishType.isEmpty() : dishType.equals(currentDishType));

            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    currentDishType = dishType.equals("All") ? "" : dishType;
                    filterStalls();
                    chipGroupDishType.setVisibility(View.GONE);
                }
            });

            chipGroupDishType.addView(chip);
        }
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
                            if (mMap != null) {
                                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                            }
                            loadAllStalls();
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
     * Processes the stalls from Firestore and updates the map
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
     * Filters stalls based on radius and dish type
     */
    private void filterStalls() {
        if (mMap == null || allStalls.isEmpty()) return;

        // Clear existing markers
        mMap.clear();
        markerStallMap.clear();

        // Filter stalls
        List<Stall> filteredStalls = new ArrayList<>(allStalls);
        
        // Filter by dish type
        if (!currentDishType.isEmpty()) {
            List<Stall> dishTypeFiltered = new ArrayList<>();
            for (Stall stall : filteredStalls) {
                if (stall.getDishType().equalsIgnoreCase(currentDishType)) {
                    dishTypeFiltered.add(stall);
                }
            }
            filteredStalls = dishTypeFiltered;
        }
        
        // Filter by radius
        if (currentRadius > 0 && currentLocation != null) {
            filteredStalls = LocationUtils.filterStallsByRadius(filteredStalls, currentLocation, currentRadius);
        }
        
        // Add markers for filtered stalls
        for (Stall stall : filteredStalls) {
            LatLng position = new LatLng(
                    stall.getLocation().getLatitude(),
                    stall.getLocation().getLongitude());
            
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(position)
                    .title(stall.getName())
                    .snippet(stall.getDishType() + " • " + stall.getArea())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            
            Marker marker = mMap.addMarker(markerOptions);
            if (marker != null) {
                markerStallMap.put(marker.getId(), stall);
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        
        // Set up map settings
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        
        // Set up info window click listener
        mMap.setOnInfoWindowClickListener(marker -> {
            Stall stall = markerStallMap.get(marker.getId());
            if (stall != null) {
                Intent intent = new Intent(getContext(), StallDetailActivity.class);
                intent.putExtra("stall_id", stall.getStallId());
                startActivity(intent);
            }
        });
        
        // If we already have the current location, move camera to it
        if (currentLocation != null) {
            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            loadAllStalls();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LocationUtils.LOCATION_PERMISSION_REQUEST_CODE) {
            getCurrentLocation();
        }
    }
}
