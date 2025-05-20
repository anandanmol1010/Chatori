package com.app.chatori.ui.search;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.chatori.R;
import com.app.chatori.adapter.StallAdapter;
import com.app.chatori.model.Stall;
import com.app.chatori.repository.StallRepository;
import com.app.chatori.utils.UIUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;
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
 * Activity for searching stalls with filtering options.
 */
public class SearchActivity extends AppCompatActivity {

    private EditText etSearch;
    private ImageButton btnBack, btnFilter;
    private RecyclerView rvStalls;
    private ProgressBar progressBar;
    private TextView tvNoResults;

    private StallRepository stallRepository;
    private StallAdapter stallAdapter;
    private List<Stall> allStalls = new ArrayList<>();
    private List<Stall> filteredStalls = new ArrayList<>();

    // Filter variables
    private String selectedDishType = "";
    private String selectedArea = "";
    private float minRating = 0;
    private String sortBy = "rating"; // Default sort by rating

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Initialize repository
        stallRepository = StallRepository.getInstance();

        // Initialize views
        etSearch = findViewById(R.id.et_search);
        btnBack = findViewById(R.id.btn_back);
        btnFilter = findViewById(R.id.btn_filter);
        rvStalls = findViewById(R.id.rv_stalls);
        progressBar = findViewById(R.id.progress_bar);
        tvNoResults = findViewById(R.id.tv_no_results);

        // Set up RecyclerView
        stallAdapter = new StallAdapter(this, filteredStalls);
        rvStalls.setLayoutManager(new LinearLayoutManager(this));
        rvStalls.setAdapter(stallAdapter);

        // Set up click listeners
        btnBack.setOnClickListener(v -> finish());
        btnFilter.setOnClickListener(v -> showFilterDialog());

        // Set up search text watcher
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                filterStalls(s.toString());
            }
        });

        // Check if we have a filter from intent
        String filterType = getIntent().getStringExtra("filter_type");
        if (filterType != null) {
            if (filterType.equals("recommended")) {
                sortBy = "rating";
            } else if (filterType.equals("nearby")) {
                sortBy = "distance";
            } else if (filterType.equals("top_rated")) {
                sortBy = "rating";
                minRating = 4.0f;
            }
        }

        // Load stalls
        loadStalls();
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
                    UIUtils.showToast(this, getString(R.string.error_loading_stalls));
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

        // Apply initial filtering
        filterStalls(etSearch.getText().toString());
        
        progressBar.setVisibility(View.GONE);
    }

    /**
     * Filters stalls based on search query and selected filters
     */
    private void filterStalls(String query) {
        filteredStalls.clear();
        
        for (Stall stall : allStalls) {
            boolean matchesSearch = query.isEmpty() || 
                    stall.getName().toLowerCase().contains(query.toLowerCase()) ||
                    stall.getArea().toLowerCase().contains(query.toLowerCase()) ||
                    stall.getDishType().toLowerCase().contains(query.toLowerCase());
            
            boolean matchesDishType = selectedDishType.isEmpty() || 
                    stall.getDishType().equals(selectedDishType);
            
            boolean matchesArea = selectedArea.isEmpty() || 
                    stall.getArea().equals(selectedArea);
            
            boolean matchesRating = stall.getRating() >= minRating;
            
            if (matchesSearch && matchesDishType && matchesArea && matchesRating) {
                filteredStalls.add(stall);
            }
        }
        
        // Sort the filtered stalls
        sortStalls();
        
        // Update UI
        stallAdapter.updateStalls(filteredStalls);
        
        // Show/hide no results message
        if (filteredStalls.isEmpty()) {
            tvNoResults.setVisibility(View.VISIBLE);
        } else {
            tvNoResults.setVisibility(View.GONE);
        }
    }

    /**
     * Sorts the filtered stalls based on the selected sort option
     */
    private void sortStalls() {
        if (sortBy.equals("rating")) {
            Collections.sort(filteredStalls, (s1, s2) -> 
                    Float.compare(s2.getRating(), s1.getRating()));
        } else if (sortBy.equals("name")) {
            Collections.sort(filteredStalls, (s1, s2) -> 
                    s1.getName().compareToIgnoreCase(s2.getName()));
        }
        // Note: Distance sorting is handled in the MapFragment
    }

    /**
     * Shows the filter dialog
     */
    private void showFilterDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_filter, null);
        dialog.setContentView(dialogView);

        // Initialize dialog views
        ChipGroup chipGroupDishType = dialogView.findViewById(R.id.chip_group_dish_type);
        ChipGroup chipGroupArea = dialogView.findViewById(R.id.chip_group_area);
        ChipGroup chipGroupRating = dialogView.findViewById(R.id.chip_group_rating);
        ChipGroup chipGroupSort = dialogView.findViewById(R.id.chip_group_sort);
        MaterialButton btnApply = dialogView.findViewById(R.id.btn_apply);
        MaterialButton btnClear = dialogView.findViewById(R.id.btn_clear);

        // Populate dish types
        List<String> dishTypes = new ArrayList<>();
        for (Stall stall : allStalls) {
            if (!dishTypes.contains(stall.getDishType())) {
                dishTypes.add(stall.getDishType());
                
                Chip chip = new Chip(this);
                chip.setText(stall.getDishType());
                chip.setCheckable(true);
                chip.setChecked(stall.getDishType().equals(selectedDishType));
                chipGroupDishType.addView(chip);
            }
        }

        // Populate areas
        List<String> areas = new ArrayList<>();
        for (Stall stall : allStalls) {
            if (!areas.contains(stall.getArea())) {
                areas.add(stall.getArea());
                
                Chip chip = new Chip(this);
                chip.setText(stall.getArea());
                chip.setCheckable(true);
                chip.setChecked(stall.getArea().equals(selectedArea));
                chipGroupArea.addView(chip);
            }
        }

        // Set up rating chips
        for (int i = 0; i < chipGroupRating.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupRating.getChildAt(i);
            float rating = Float.parseFloat(chip.getTag().toString());
            chip.setChecked(rating == minRating);
        }

        // Set up sort chips
        for (int i = 0; i < chipGroupSort.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupSort.getChildAt(i);
            String sort = chip.getTag().toString();
            chip.setChecked(sort.equals(sortBy));
        }

        // Set up click listeners
        btnApply.setOnClickListener(v -> {
            // Get selected dish type
            int dishTypeId = chipGroupDishType.getCheckedChipId();
            selectedDishType = dishTypeId != View.NO_ID ? 
                    ((Chip) dialogView.findViewById(dishTypeId)).getText().toString() : "";

            // Get selected area
            int areaId = chipGroupArea.getCheckedChipId();
            selectedArea = areaId != View.NO_ID ? 
                    ((Chip) dialogView.findViewById(areaId)).getText().toString() : "";

            // Get selected rating
            int ratingId = chipGroupRating.getCheckedChipId();
            minRating = ratingId != View.NO_ID ? 
                    Float.parseFloat(((Chip) dialogView.findViewById(ratingId)).getTag().toString()) : 0;

            // Get selected sort
            int sortId = chipGroupSort.getCheckedChipId();
            sortBy = sortId != View.NO_ID ? 
                    ((Chip) dialogView.findViewById(sortId)).getTag().toString() : "rating";

            // Apply filters
            filterStalls(etSearch.getText().toString());
            
            dialog.dismiss();
        });

        btnClear.setOnClickListener(v -> {
            // Clear all selections
            chipGroupDishType.clearCheck();
            chipGroupArea.clearCheck();
            chipGroupRating.clearCheck();
            
            // Reset to default sort
            for (int i = 0; i < chipGroupSort.getChildCount(); i++) {
                Chip chip = (Chip) chipGroupSort.getChildAt(i);
                if (chip.getTag().toString().equals("rating")) {
                    chip.setChecked(true);
                    break;
                }
            }
            
            dialog.dismiss();
            
            // Reset filter variables
            selectedDishType = "";
            selectedArea = "";
            minRating = 0;
            sortBy = "rating";
            
            // Apply filters
            filterStalls(etSearch.getText().toString());
        });

        dialog.show();
    }
}
