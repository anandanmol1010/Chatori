package com.app.chatori.ui.stall;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.chatori.R;
import com.app.chatori.adapter.SelectedImagesAdapter;
import com.google.firebase.firestore.GeoPoint;
import com.app.chatori.model.Stall;
import com.app.chatori.repository.StallRepository;
import com.app.chatori.utils.LocationUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Activity for adding a new stall.
 */
public class AddStallActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int PICK_IMAGES_REQUEST = 1;
    private static final int STORAGE_PERMISSION_REQUEST = 2;

    private Toolbar toolbar;
    private EditText etStallName, etDescription, etOpeningHours, etPhone, etArea;
    private ChipGroup chipGroupDishType;
    private Button btnAddImages, btnSubmit;
    private RecyclerView rvSelectedImages;
    private ProgressBar progressBar;
    private GoogleMap mMap;
    private ImageView btnMyLocation;
    private SupportMapFragment mapFragment;

    private StallRepository stallRepository;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    
    private List<Uri> selectedImageUris = new ArrayList<>();
    private SelectedImagesAdapter imagesAdapter;
    private String selectedDishType = "";
    private LatLng selectedLocation;
    private Location currentLocation;
    private Marker stallMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_stall);

        // Initialize Firebase
        stallRepository = StallRepository.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        etStallName = findViewById(R.id.et_stall_name);
        etDescription = findViewById(R.id.et_description);
        etOpeningHours = findViewById(R.id.et_opening_hours);
        etPhone = findViewById(R.id.et_phone);
        etArea = findViewById(R.id.et_area);
        chipGroupDishType = findViewById(R.id.chip_group_dish_type);
        btnAddImages = findViewById(R.id.btn_add_images);
        btnSubmit = findViewById(R.id.btn_submit);
        rvSelectedImages = findViewById(R.id.rv_selected_images);
        progressBar = findViewById(R.id.progress_bar);
        btnMyLocation = findViewById(R.id.btn_my_location);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        // Set up toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.add_stall);
            getSupportActionBar().setElevation(0); // Remove shadow
        }

        // Set up map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Set up RecyclerView for selected images
        setupRecyclerView();

        // Set up dish type chips
        setupDishTypeChips();

        // Set up click listeners
        setupClickListeners();

        // Get current location
        getCurrentLocation();
    }

    /**
     * Initializes views
     */
    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        etStallName = findViewById(R.id.et_stall_name);
        etDescription = findViewById(R.id.et_description);
        etOpeningHours = findViewById(R.id.et_opening_hours);
        etPhone = findViewById(R.id.et_phone);
        etArea = findViewById(R.id.et_area);
        chipGroupDishType = findViewById(R.id.chip_group_dish_type);
        btnAddImages = findViewById(R.id.btn_add_images);
        btnSubmit = findViewById(R.id.btn_submit);
        rvSelectedImages = findViewById(R.id.rv_selected_images);
        progressBar = findViewById(R.id.progress_bar);
        btnMyLocation = findViewById(R.id.btn_my_location);
    }

    /**
     * Sets up RecyclerView for selected images
     */
    private void setupRecyclerView() {
        imagesAdapter = new SelectedImagesAdapter(this, selectedImageUris, uri -> {
            selectedImageUris.remove(uri);
            imagesAdapter.notifyDataSetChanged();
            updateAddImagesButtonText();
        });
        rvSelectedImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvSelectedImages.setAdapter(imagesAdapter);
    }

    /**
     * Sets up dish type chips
     */
    private void setupDishTypeChips() {
        // For simplicity, we'll just use a few hard-coded dish types
        // In a real app, you would fetch these from the DishRepository
        String[] dishTypes = {"Chaat", "Momos", "Golgappe", "Tikki", "Rolls", "Biryani", "Chinese"};

        for (String dishType : dishTypes) {
            Chip chip = new Chip(this);
            chip.setText(dishType);
            chip.setCheckable(true);
            chip.setChecked(false);

            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    // Uncheck other chips
                    for (int i = 0; i < chipGroupDishType.getChildCount(); i++) {
                        Chip otherChip = (Chip) chipGroupDishType.getChildAt(i);
                        if (otherChip != buttonView) {
                            otherChip.setChecked(false);
                        }
                    }
                    selectedDishType = dishType;
                } else if (selectedDishType.equals(dishType)) {
                    selectedDishType = "";
                }
            });

            chipGroupDishType.addView(chip);
        }
    }

    /**
     * Sets up click listeners for buttons
     */
    private void setupClickListeners() {
        btnAddImages.setOnClickListener(v -> checkStoragePermissionAndOpenImagePicker());

        btnSubmit.setOnClickListener(v -> validateAndSubmitStall());

        btnMyLocation.setOnClickListener(v -> {
            if (mMap != null && currentLocation != null) {
                LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                updateSelectedLocation(latLng);
            }
        });
    }

    /**
     * Gets the current location of the device
     */
    private void getCurrentLocation() {
        if (LocationUtils.hasLocationPermission(this)) {
            LocationUtils.getLastLocation(this)
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            currentLocation = location;
                            if (mMap != null) {
                                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                                updateSelectedLocation(latLng);
                            }
                        }
                    });
        } else {
            LocationUtils.requestLocationPermission(this);
        }
    }

    /**
     * Checks storage permission and opens image picker
     */
    private void checkStoragePermissionAndOpenImagePicker() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_REQUEST);
        } else {
            openImagePicker();
        }
    }

    /**
     * Opens image picker to select stall images
     */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_images)), PICK_IMAGES_REQUEST);
    }

    /**
     * Updates the add images button text based on selected images count
     */
    private void updateAddImagesButtonText() {
        if (selectedImageUris.isEmpty()) {
            btnAddImages.setText(R.string.add_images);
        } else {
            btnAddImages.setText(getString(R.string.add_more_images, selectedImageUris.size()));
        }
    }

    /**
     * Updates the selected location on the map
     */
    private void updateSelectedLocation(LatLng latLng) {
        selectedLocation = latLng;
        
        if (stallMarker != null) {
            stallMarker.remove();
        }
        
        stallMarker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(getString(R.string.stall_location))
                .draggable(true));
    }

    /**
     * Validates form inputs and submits the stall
     */
    private void validateAndSubmitStall() {
        // Get form values
        String stallName = etStallName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String openingHours = etOpeningHours.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String area = etArea.getText().toString().trim();

        // Validate stall name
        if (stallName.isEmpty()) {
            etStallName.setError(getString(R.string.error_stall_name_required));
            etStallName.requestFocus();
            return;
        }

        // Validate dish type
        if (selectedDishType.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_dish_type_required), Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate area
        if (area.isEmpty()) {
            etArea.setError(getString(R.string.error_area_required));
            etArea.requestFocus();
            return;
        }

        // Validate location
        if (selectedLocation == null) {
            Toast.makeText(this, getString(R.string.error_location_required), Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if user is logged in
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, getString(R.string.error_login_required), Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSubmit.setEnabled(false);

        // Handle images without uploading to Firebase Storage
        handleImagesWithoutUpload(stallName, description, openingHours, phone, area, currentUser.getUid());
    }

    /**
     * Handles image processing without uploading to Firebase Storage.
     */
    private void handleImagesWithoutUpload(String stallName, String description, String openingHours, String phone, String area, String userId) {
        List<String> imageUrls = new ArrayList<>();
        // Directly add stall to Firestore without uploading images
        addStallToFirestore(stallName, description, openingHours, phone, area, userId, imageUrls);
        // Notify user that images are not uploaded
        Toast.makeText(this, "Images are not uploaded due to billing constraints.", Toast.LENGTH_SHORT).show();
    }

    /**
     * Adds the stall to Firestore
     */
    private void addStallToFirestore(String stallName, String description, String openingHours, String phone, String area, String userId, List<String> imageUrls) {
        // Create stall object
        Stall stall = new Stall();
        stall.setStallId(stallRepository.generateStallId());
        stall.setName(stallName);
        stall.setDescription(description);
        stall.setDishType(selectedDishType);
        stall.setArea(area);
        stall.setOpeningHours(openingHours);
        stall.setPhone(phone);
        stall.setUserId(userId);
        stall.setImages(imageUrls);
        stall.setRating(0);
        stall.setNumRatings(0);
        stall.setCreatedAt(new Date());
        
        // Set location
        com.google.firebase.firestore.GeoPoint location = new com.google.firebase.firestore.GeoPoint(
            selectedLocation.latitude, 
            selectedLocation.longitude
        );
        stall.setLocation(location);

        // Add stall to Firestore
        stallRepository.addStall(stall)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, getString(R.string.stall_added_successfully), Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnSubmit.setEnabled(true);
                    Toast.makeText(this, getString(R.string.error_adding_stall), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        
        // Set up map settings
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        
        // Set up marker drag listener
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
            }

            @Override
            public void onMarkerDrag(Marker marker) {
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                selectedLocation = marker.getPosition();
            }
        });
        
        // Set up map click listener
        mMap.setOnMapClickListener(latLng -> updateSelectedLocation(latLng));
        
        // If we already have the current location, move camera to it
        if (currentLocation != null) {
            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            updateSelectedLocation(latLng);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGES_REQUEST && resultCode == RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                // Multiple images selected
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    if (!selectedImageUris.contains(imageUri)) {
                        selectedImageUris.add(imageUri);
                    }
                }
            } else if (data.getData() != null) {
                // Single image selected
                Uri imageUri = data.getData();
                if (!selectedImageUris.contains(imageUri)) {
                    selectedImageUris.add(imageUri);
                }
            }
            
            imagesAdapter.notifyDataSetChanged();
            updateAddImagesButtonText();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LocationUtils.LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            }
        } else if (requestCode == STORAGE_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                Toast.makeText(this, getString(R.string.permission_required), Toast.LENGTH_SHORT).show();
            }
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
