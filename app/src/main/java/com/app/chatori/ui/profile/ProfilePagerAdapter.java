package com.app.chatori.ui.profile;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * Adapter for the ViewPager in the ProfileFragment.
 * Handles the tabs for My Stalls, My Reviews, and Favorites.
 */
public class ProfilePagerAdapter extends FragmentStateAdapter {

    private static final int NUM_TABS = 3;
    private static final int TAB_MY_STALLS = 0;
    private static final int TAB_MY_REVIEWS = 1;
    private static final int TAB_FAVORITES = 2;

    public ProfilePagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case TAB_MY_STALLS:
                return new MyStallsFragment();
            case TAB_MY_REVIEWS:
                return new MyReviewsFragment();
            case TAB_FAVORITES:
                return new FavoritesFragment();
            default:
                return new MyStallsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return NUM_TABS;
    }
}
