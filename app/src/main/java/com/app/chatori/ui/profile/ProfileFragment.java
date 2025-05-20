package com.app.chatori.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.app.chatori.R;
import com.app.chatori.model.User;
import com.app.chatori.repository.UserRepository;
import com.app.chatori.ui.auth.LoginActivity;
import com.app.chatori.utils.UIUtils;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Fragment for displaying user profile information and their stalls, reviews, and favorites.
 */
public class ProfileFragment extends Fragment {

    private ImageView ivProfileImage;
    private TextView tvName, tvEmail, tvBio;
    private Button btnEditProfile, btnLogout;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    
    private FirebaseAuth firebaseAuth;
    private UserRepository userRepository;
    private ProfilePagerAdapter pagerAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();
        
        // Initialize repository
        userRepository = UserRepository.getInstance();

        // Initialize views
        ivProfileImage = view.findViewById(R.id.iv_profile_image);
        tvName = view.findViewById(R.id.tv_name);
        tvEmail = view.findViewById(R.id.tv_email);
        tvBio = view.findViewById(R.id.tv_bio);
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        btnLogout = view.findViewById(R.id.btn_logout);
        tabLayout = view.findViewById(R.id.tab_layout);
        viewPager = view.findViewById(R.id.view_pager);

        // Set up click listeners
        setupClickListeners();
        
        // Set up ViewPager and TabLayout
        setupViewPager();
        
        // Load user data
        loadUserData();

        return view;
    }

    /**
     * Sets up click listeners for buttons
     */
    private void setupClickListeners() {
        btnEditProfile.setOnClickListener(v -> {
            // Navigate to edit profile activity
            Intent intent = new Intent(getContext(), EditProfileActivity.class);
            startActivity(intent);
        });
        
        btnLogout.setOnClickListener(v -> {
            // Sign out from Firebase
            firebaseAuth.signOut();
            
            // Navigate to login activity
            Intent intent = new Intent(getContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
    
    /**
     * Sets up ViewPager and TabLayout
     */
    private void setupViewPager() {
        pagerAdapter = new ProfilePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        
        // Connect TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText(R.string.tab_my_stalls);
                    break;
                case 1:
                    tab.setText(R.string.tab_my_reviews);
                    break;
                case 2:
                    tab.setText(R.string.tab_favorites);
                    break;
            }
        }).attach();
    }
    
    /**
     * Loads user data from Firestore
     */
    private void loadUserData() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            // If user is not logged in, navigate to login activity
            Intent intent = new Intent(getContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return;
        }
        
        // Get user data from Firestore
        userRepository.getUserById(currentUser.getUid())
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            updateUI(user);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), getString(R.string.error_loading_profile), Toast.LENGTH_SHORT).show();
                });
    }
    
    /**
     * Updates the UI with user data
     */
    private void updateUI(User user) {
        // Check if context and ImageView are not null before loading profile image
        if (getContext() != null && ivProfileImage != null) {
            // Load profile image
            if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                UIUtils.loadProfileImage(getContext(), user.getProfileImageUrl(), ivProfileImage);
            } else {
                // Set default profile image if URL is null or empty
                ivProfileImage.setImageResource(R.drawable.default_profile);
            }
        }
        
        // Set user name
        tvName.setText(user.getName());
        
        // Set user email
        tvEmail.setText(user.getEmail());
        
        // Set user bio
        if (user.getBio() != null && !user.getBio().isEmpty()) {
            tvBio.setText(user.getBio());
            tvBio.setVisibility(View.VISIBLE);
        } else {
            tvBio.setVisibility(View.GONE);
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Reload user data when fragment resumes
        loadUserData();
    }
}
