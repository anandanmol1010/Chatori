package com.app.chatori.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.app.chatori.R;
import com.app.chatori.ui.map.MapFragment;
import com.app.chatori.ui.profile.ProfileFragment;
import com.app.chatori.ui.search.SearchFragment;
import com.app.chatori.ui.stall.AddStallActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Main activity that hosts the bottom navigation and fragments.
 */
public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnItemSelectedListener {

    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabAddStall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fabAddStall = findViewById(R.id.fab_add_stall);

        // Set up bottom navigation
        bottomNavigationView.setOnItemSelectedListener(this);

        // Set up FAB click listener
        fabAddStall.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddStallActivity.class);
            startActivity(intent);
        });

        // Load the default fragment (Home)
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }
    }

    /**
     * Handles bottom navigation item selection
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        int itemId = item.getItemId();

        if (itemId == R.id.navigation_home) {
            fragment = new HomeFragment();
        } else if (itemId == R.id.navigation_search) {
            fragment = new SearchFragment();
        } else if (itemId == R.id.navigation_map) {
            fragment = new MapFragment();
        } else if (itemId == R.id.navigation_profile) {
            fragment = new ProfileFragment();
        }

        return loadFragment(fragment);
    }

    /**
     * Loads a fragment into the container
     */
    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }
}
