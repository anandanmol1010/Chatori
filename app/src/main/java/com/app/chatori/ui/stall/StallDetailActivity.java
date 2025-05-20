package com.app.chatori.ui.stall;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.app.chatori.R;
import com.app.chatori.adapter.DishAdapter;
import com.app.chatori.adapter.ImageSliderAdapter;
import com.app.chatori.adapter.ReviewAdapter;
import com.app.chatori.model.Dish;
import com.app.chatori.model.Review;
import com.app.chatori.model.Stall;
import com.app.chatori.model.User;
import com.app.chatori.repository.DishRepository;
import com.app.chatori.repository.ReviewRepository;
import com.app.chatori.repository.StallRepository;
import com.app.chatori.repository.UserRepository;
import com.app.chatori.ui.review.AllReviewsActivity;
import com.app.chatori.ui.review.WriteReviewActivity;
import com.app.chatori.utils.LocationUtils;
import com.app.chatori.utils.StallDataHelper;
import com.app.chatori.utils.UIUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for displaying detailed information about a stall.
 */
public class StallDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    private Toolbar toolbar;
    private ViewPager2 viewPagerImages;
    private TextView tvStallName, tvDishType, tvArea, tvRating, tvNumRatings, tvDescription;
    private TextView tvOpeningHours, tvDistance, tvOwnerName, tvNoReviews, tvAllReviews;
    private RatingBar ratingBar;
    private Button btnWriteReview, btnFavorite, btnDirections;
    private ImageButton btnShare;
    private RecyclerView rvDishes, rvReviews;
    private FloatingActionButton fabCall;
    private ProgressBar progressBar;
    private GoogleMap mMap;

    private StallRepository stallRepository;
    private DishRepository dishRepository;
    private ReviewRepository reviewRepository;
    private UserRepository userRepository;
    
    private String stallId;
    private Stall stall;
    private List<Dish> dishes = new ArrayList<>();
    private List<Review> reviews = new ArrayList<>();
    private Location currentLocation;
    private boolean isFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_stall_detail);

            // Get stall ID from intent
            stallId = getIntent().getStringExtra("stall_id");
            if (stallId == null || stallId.isEmpty()) {
                Toast.makeText(this, "Error: Stall ID is missing", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Initialize repositories
            stallRepository = StallRepository.getInstance();
            dishRepository = DishRepository.getInstance();
            reviewRepository = ReviewRepository.getInstance();
            userRepository = UserRepository.getInstance();

            // Initialize views
            initializeViews();

            // Set up toolbar
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("");
            }

            // Set up map
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            if (mapFragment != null) {
                mapFragment.getMapAsync(this);
            }

            // Set up click listeners
            setupClickListeners();

            // Get current location
            getCurrentLocation();

            // Load stall data
            loadStallData();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error initializing activity", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Initializes views
     */
    private void initializeViews() {
        try {
            toolbar = findViewById(R.id.toolbar);
            viewPagerImages = findViewById(R.id.view_pager_images);
            tvStallName = findViewById(R.id.tv_stall_name);
            tvDishType = findViewById(R.id.tv_dish_type);
            tvArea = findViewById(R.id.tv_area);
            tvRating = findViewById(R.id.tv_rating);
            tvNumRatings = findViewById(R.id.tv_num_ratings);
            tvDescription = findViewById(R.id.tv_description);
            tvOpeningHours = findViewById(R.id.tv_opening_hours);
            tvDistance = findViewById(R.id.tv_distance);
            tvOwnerName = findViewById(R.id.tv_owner_name);
            tvNoReviews = findViewById(R.id.tv_no_reviews);
            tvAllReviews = findViewById(R.id.tv_all_reviews);
            ratingBar = findViewById(R.id.rating_bar);
            btnWriteReview = findViewById(R.id.btn_write_review);
            btnShare = findViewById(R.id.btn_share);
            btnFavorite = findViewById(R.id.btn_favorite);
            btnDirections = findViewById(R.id.btn_directions);
            rvDishes = findViewById(R.id.rv_dishes);
            rvReviews = findViewById(R.id.rv_reviews);
            fabCall = findViewById(R.id.fab_call);
            progressBar = findViewById(R.id.progress_bar);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error initializing views: " + e.getMessage());
        }
    }

    /**
     * Sets up click listeners for buttons
     */
    private void setupClickListeners() {
        try {
            btnWriteReview.setOnClickListener(v -> {
                if (stall != null) {
                    Intent intent = new Intent(this, WriteReviewActivity.class);
                    intent.putExtra("stall_id", stall.getStallId());
                    intent.putExtra("stall_name", StallDataHelper.getSafeName(stall));
                    startActivity(intent);
                }
            });

            btnShare.setOnClickListener(v -> {
                if (stall != null) {
                    String shareText = "Check out " + StallDataHelper.getSafeName(stall) + " on Chatori! " +
                            "They serve amazing " + StallDataHelper.getSafeDishType(stall) + " in " + 
                            StallDataHelper.getSafeArea(stall) + ".";
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                    startActivity(Intent.createChooser(shareIntent, "Share via"));
                }
            });

            btnFavorite.setOnClickListener(v -> toggleFavorite());

            btnDirections.setOnClickListener(v -> {
                if (stall != null && stall.getLocation() != null) {
                    try {
                        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + 
                                stall.getLocation().getLatitude() + "," + 
                                stall.getLocation().getLongitude());
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        mapIntent.setPackage("com.google.android.apps.maps");
                        if (mapIntent.resolveActivity(getPackageManager()) != null) {
                            startActivity(mapIntent);
                        } else {
                            Toast.makeText(this, getString(R.string.error_no_maps_app), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error opening directions", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            fabCall.setOnClickListener(v -> {
                if (stall != null && stall.getPhone() != null && !stall.getPhone().isEmpty()) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:" + stall.getPhone()));
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error making call", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, getString(R.string.error_no_phone), Toast.LENGTH_SHORT).show();
                }
            });

            tvAllReviews.setOnClickListener(v -> {
                if (stall != null) {
                    Intent intent = new Intent(this, AllReviewsActivity.class);
                    intent.putExtra("stall_id", stall.getStallId());
                    intent.putExtra("stall_name", StallDataHelper.getSafeName(stall));
                    startActivity(intent);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error setting up click listeners", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Gets the current location of the device
     */
    private void getCurrentLocation() {
        try {
            if (LocationUtils.hasLocationPermission(this)) {
                LocationUtils.getLastLocation(this)
                        .addOnSuccessListener(location -> {
                            if (location != null) {
                                currentLocation = location;
                                updateDistanceUI();
                            }
                        });
            } else {
                LocationUtils.requestLocationPermission(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Don't crash if location services are unavailable
        }
    }

    /**
     * Loads stall data from Firestore
     */
    private void loadStallData() {
        try {
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
            }
            
            if (stallRepository == null) {
                stallRepository = StallRepository.getInstance();
            }

            // Log the stall ID we're trying to load for debugging
            System.out.println("DEBUG: Attempting to load stall with ID: " + stallId);
            
            // First try to get all stalls to see what's available
            stallRepository.getAllStalls()
                .addOnSuccessListener(querySnapshot -> {
                    // Check if we have any stalls at all
                    if (querySnapshot == null || querySnapshot.isEmpty()) {
                        System.out.println("DEBUG: No stalls found in database");
                        showErrorAndFinish("No stalls available. Please add some stalls first.");
                        return;
                    }
                    
                    // Check if the requested stall exists
                    boolean stallExists = false;
                    DocumentSnapshot targetStallDoc = null;
                    DocumentSnapshot firstStallDoc = null;
                    
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        // Save the first stall as a fallback
                        if (firstStallDoc == null) {
                            firstStallDoc = doc;
                        }
                        
                        // Check if this is the stall we're looking for
                        Stall s = doc.toObject(Stall.class);
                        if (s != null && s.getStallId() != null && s.getStallId().equals(stallId)) {
                            stallExists = true;
                            targetStallDoc = doc;
                            break;
                        }
                    }
                    
                    if (stallExists && targetStallDoc != null) {
                        // Load the requested stall
                        System.out.println("DEBUG: Found requested stall with ID: " + stallId);
                        loadStallFromDocument(targetStallDoc);
                    } else if (firstStallDoc != null) {
                        // Load the first stall as a fallback
                        Stall firstStall = firstStallDoc.toObject(Stall.class);
                        if (firstStall != null) {
                            System.out.println("DEBUG: Requested stall not found. Loading first stall: " + firstStall.getStallId());
                            Toast.makeText(this, "Requested stall not found. Showing another stall instead.", Toast.LENGTH_SHORT).show();
                            stallId = firstStall.getStallId(); // Update stallId to match the loaded stall
                            loadStallFromDocument(firstStallDoc);
                        } else {
                            showErrorAndFinish("Error loading stall data");
                        }
                    } else {
                        showErrorAndFinish("No valid stalls found");
                    }
                })
                .addOnFailureListener(e -> {
                    System.out.println("DEBUG: Error loading stalls: " + e.getMessage());
                    showErrorAndFinish("Error loading stalls: " + e.getMessage());
                });
        } catch (Exception e) {
            e.printStackTrace();
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
            Toast.makeText(this, "Error loading stall data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    /**
     * Loads a stall from a document snapshot
     * @param doc The document snapshot containing the stall data
     */
    private void loadStallFromDocument(DocumentSnapshot doc) {
        try {
            stall = doc.toObject(Stall.class);
            if (stall != null) {
                // Ensure the stall ID is set correctly
                if (stall.getStallId() == null) {
                    stall.setStallId(doc.getId());
                }
                
                // Print stall data for debugging
                System.out.println("DEBUG: Loaded stall name: " + stall.getName());
                System.out.println("DEBUG: Loaded stall dish type: " + stall.getDishType());
                System.out.println("DEBUG: Loaded stall area: " + stall.getArea());
                
                // Update UI and load related data
                updateUI();
                loadDishes();
                loadReviews();
                checkIfFavorite();
            } else {
                showErrorAndFinish("Error: Could not load stall data");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAndFinish("Error processing stall data: " + e.getMessage());
        } finally {
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
        }
    }
    
    /**
     * Shows an error message and finishes the activity
     * @param errorMessage The error message to show
     */
    private void showErrorAndFinish(String errorMessage) {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        finish();
    }
    
    /**
     * Loads a specific stall by ID
     * @param id The stall ID to load
     */
    private void loadStallById(String id) {
        stallRepository.getStallById(id)
            .addOnSuccessListener(documentSnapshot -> {
                try {
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        stall = documentSnapshot.toObject(Stall.class);
                        if (stall != null) {
                            // Ensure the stall ID is set correctly
                            if (stall.getStallId() == null) {
                                stall.setStallId(id);
                            }
                            
                            // Always validate and fix stall data, don't fail if invalid
                            StallDataHelper.validateAndFixStall(stall);
                            
                            // Update UI and load related data
                            updateUI();
                            loadDishes();
                            loadReviews();
                            checkIfFavorite();
                        } else {
                            // Create a minimal valid stall to prevent crashes
                            stall = new Stall();
                            stall.setStallId(id);
                            stall.setName("Stall " + id);
                            StallDataHelper.validateAndFixStall(stall);
                            
                            updateUI();
                            Toast.makeText(this, "Limited stall data available", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Create a minimal valid stall to prevent crashes
                        stall = new Stall();
                        stall.setStallId(id);
                        stall.setName("Stall " + id);
                        StallDataHelper.validateAndFixStall(stall);
                        
                        updateUI();
                        Toast.makeText(this, "Limited stall data available", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    // Create a minimal valid stall to prevent crashes
                    stall = new Stall();
                    stall.setStallId(id);
                    stall.setName("Stall " + id);
                    StallDataHelper.validateAndFixStall(stall);
                    
                    updateUI();
                    Toast.makeText(this, "Error processing stall data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                } finally {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                }
            })
            .addOnFailureListener(e -> {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                e.printStackTrace();
                
                // Create a minimal valid stall to prevent crashes
                stall = new Stall();
                stall.setStallId(id);
                stall.setName("Stall " + id);
                StallDataHelper.validateAndFixStall(stall);
                
                updateUI();
                Toast.makeText(this, getString(R.string.error_loading_stall), Toast.LENGTH_SHORT).show();
            });
    }

    /**
     * Updates the UI with stall data
     */
    private void updateUI() {
        try {
            // Set stall name
            tvStallName.setText(StallDataHelper.getSafeName(stall));
            
            // Set dish type
            tvDishType.setText(StallDataHelper.getSafeDishType(stall));
            
            // Set area
            tvArea.setText(StallDataHelper.getSafeArea(stall));
            
            // Set rating
            ratingBar.setRating(stall.getRating());
            tvRating.setText(String.format("%.1f", stall.getRating()));
            tvNumRatings.setText(String.format("(%d)", stall.getNumRatings()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        try {
            // Set description
            String description = StallDataHelper.getSafeDescription(stall);
            if (!description.isEmpty()) {
                tvDescription.setText(description);
                tvDescription.setVisibility(View.VISIBLE);
            } else {
                tvDescription.setVisibility(View.GONE);
            }
            
            // Set opening hours
            if (stall.getOpeningHours() != null && !stall.getOpeningHours().isEmpty()) {
                tvOpeningHours.setText(stall.getOpeningHours());
            } else {
                tvOpeningHours.setText(R.string.not_available);
            }
            
            // Set owner name
            if (stall.getOwnerName() != null && !stall.getOwnerName().isEmpty()) {
                tvOwnerName.setText(stall.getOwnerName());
            } else {
                tvOwnerName.setText(R.string.not_available);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        try {
            // Set up image slider
            if (stall.getImages() != null && !stall.getImages().isEmpty() && viewPagerImages != null) {
                try {
                    ImageSliderAdapter imageAdapter = new ImageSliderAdapter(this, stall.getImages());
                    viewPagerImages.setAdapter(imageAdapter);
                } catch (Exception e) {
                    // Log the error but don't crash
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Update distance UI
        updateDistanceUI();
        
        // Update map
        updateMap();
    }

    /**
     * Updates the distance UI
     */
    private void updateDistanceUI() {
        try {
            if (stall != null && stall.getLocation() != null && currentLocation != null) {
                double distance = LocationUtils.calculateDistance(
                        currentLocation.getLatitude(), currentLocation.getLongitude(),
                        stall.getLocation().getLatitude(), stall.getLocation().getLongitude());
                
                String distanceText;
                if (distance < 1) {
                    // Convert to meters
                    int meters = (int) (distance * 1000);
                    distanceText = meters + " m";
                } else {
                    // Round to one decimal place
                    distanceText = String.format("%.1f km", distance);
                }
                
                tvDistance.setText(distanceText);
                tvDistance.setVisibility(View.VISIBLE);
            } else {
                tvDistance.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            tvDistance.setVisibility(View.GONE);
        }
    }

    /**
     * Updates the map with stall location
     */
    private void updateMap() {
        try {
            if (mMap != null && stall != null && stall.getLocation() != null) {
                LatLng stallLocation = new LatLng(
                        stall.getLocation().getLatitude(),
                        stall.getLocation().getLongitude());
                
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(stallLocation).title(StallDataHelper.getSafeName(stall)));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(stallLocation, 15));
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Don't crash if there's an issue with the map
        }
    }

    /**
     * Loads dishes for the stall from Firestore
     */
    private void loadDishes() {
        try {
            if (dishRepository != null && stallId != null) {
                dishRepository.getDishesByStallId(stallId)
                        .addOnSuccessListener(querySnapshot -> {
                            try {
                                dishes.clear();
                                for (DocumentSnapshot document : querySnapshot) {
                                    Dish dish = document.toObject(Dish.class);
                                    if (dish != null) {
                                        dishes.add(dish);
                                    }
                                }
                                
                                // Set up dishes RecyclerView
                                if (!dishes.isEmpty()) {
                                    DishAdapter dishAdapter = new DishAdapter(this, dishes);
                                    rvDishes.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
                                    rvDishes.setAdapter(dishAdapter);
                                    rvDishes.setVisibility(View.VISIBLE);
                                } else {
                                    rvDishes.setVisibility(View.GONE);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                rvDishes.setVisibility(View.GONE);
                            }
                        })
                        .addOnFailureListener(e -> {
                            e.printStackTrace();
                            rvDishes.setVisibility(View.GONE);
                        });
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (rvDishes != null) {
                rvDishes.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Loads reviews for the stall from Firestore
     */
    private void loadReviews() {
        try {
            if (reviewRepository != null && stallId != null) {
                reviewRepository.getReviewsByStallId(stallId, 3)
                        .addOnSuccessListener(querySnapshot -> {
                            try {
                                reviews.clear();
                                for (DocumentSnapshot document : querySnapshot) {
                                    Review review = document.toObject(Review.class);
                                    if (review != null) {
                                        reviews.add(review);
                                    }
                                }
                                
                                // Set up reviews RecyclerView
                                if (!reviews.isEmpty()) {
                                    ReviewAdapter reviewAdapter = new ReviewAdapter(this, reviews);
                                    rvReviews.setLayoutManager(new LinearLayoutManager(this));
                                    rvReviews.setAdapter(reviewAdapter);
                                    rvReviews.setVisibility(View.VISIBLE);
                                    tvNoReviews.setVisibility(View.GONE);
                                    
                                    // Show "View All" if there are more than 3 reviews
                                    if (reviews.size() >= 3) {
                                        tvAllReviews.setVisibility(View.VISIBLE);
                                    } else {
                                        tvAllReviews.setVisibility(View.GONE);
                                    }
                                } else {
                                    rvReviews.setVisibility(View.GONE);
                                    tvNoReviews.setVisibility(View.VISIBLE);
                                    tvAllReviews.setVisibility(View.GONE);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                rvReviews.setVisibility(View.GONE);
                                tvNoReviews.setVisibility(View.VISIBLE);
                                tvAllReviews.setVisibility(View.GONE);
                            }
                        })
                        .addOnFailureListener(e -> {
                            e.printStackTrace();
                            rvReviews.setVisibility(View.GONE);
                            tvNoReviews.setVisibility(View.VISIBLE);
                            tvAllReviews.setVisibility(View.GONE);
                        });
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (rvReviews != null) {
                rvReviews.setVisibility(View.GONE);
            }
            if (tvNoReviews != null) {
                tvNoReviews.setVisibility(View.VISIBLE);
            }
            if (tvAllReviews != null) {
                tvAllReviews.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Checks if the stall is in the user's favorites
     */
    private void checkIfFavorite() {
        try {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null && userRepository != null) {
                userRepository.getUserById(currentUser.getUid())
                        .addOnSuccessListener(documentSnapshot -> {
                            try {
                                if (documentSnapshot.exists()) {
                                    User user = documentSnapshot.toObject(User.class);
                                    if (user != null && user.getFavoriteStalls() != null) {
                                        isFavorite = user.getFavoriteStalls().contains(stallId);
                                        updateFavoriteButton();
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        })
                        .addOnFailureListener(e -> e.printStackTrace());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Toggles the favorite status of the stall
     */
    private void toggleFavorite() {
        try {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                if (isFavorite) {
                    // Remove from favorites
                    userRepository.removeFromFavorites(currentUser.getUid(), stallId)
                            .addOnSuccessListener(aVoid -> {
                                isFavorite = false;
                                updateFavoriteButton();
                                Toast.makeText(this, getString(R.string.removed_from_favorites), Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                e.printStackTrace();
                                Toast.makeText(this, getString(R.string.error_updating_favorites), Toast.LENGTH_SHORT).show();
                            });
                } else {
                    // Add to favoriteshttps://meet.google.com/wpj-rddm-bji
                    userRepository.addToFavorites(currentUser.getUid(), stallId)
                            .addOnSuccessListener(aVoid -> {
                                isFavorite = true;
                                updateFavoriteButton();
                                Toast.makeText(this, getString(R.string.added_to_favorites), Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                e.printStackTrace();
                                Toast.makeText(this, getString(R.string.error_updating_favorites), Toast.LENGTH_SHORT).show();
                            });
                }
            } else {
                Toast.makeText(this, getString(R.string.error_login_required), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error updating favorites", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Updates the favorite button text based on favorite status
     */
    private void updateFavoriteButton() {
        try {
            // For a Button, we need to update the text instead of the image
            btnFavorite.setText(isFavorite ? R.string.remove_from_favorites : R.string.add_to_favorites);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        try {
            mMap = googleMap;
            
            // Set up map settings
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setMapToolbarEnabled(false);
            
            // Update map with stall location if available
            if (stall != null) {
                updateMap();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LocationUtils.LOCATION_PERMISSION_REQUEST_CODE && 
            grantResults.length > 0 && 
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh reviews when returning to this activity
        if (stall != null) {
            loadReviews();
            checkIfFavorite();
        }
    }
}
