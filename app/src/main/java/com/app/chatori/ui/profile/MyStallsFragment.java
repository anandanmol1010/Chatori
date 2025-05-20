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
import com.app.chatori.repository.StallRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for displaying the user's stalls.
 */
public class MyStallsFragment extends Fragment {

    private RecyclerView rvMyStalls;
    private ProgressBar progressBar;
    private LinearLayout emptyState;

    private StallRepository stallRepository;
    private StallAdapter stallAdapter;
    private List<Stall> myStalls = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_stalls, container, false);

        // Initialize repository
        stallRepository = StallRepository.getInstance();

        // Initialize views
        rvMyStalls = view.findViewById(R.id.rv_my_stalls);
        progressBar = view.findViewById(R.id.progress_bar);
        emptyState = view.findViewById(R.id.empty_state);

        // Set up RecyclerView
        setupRecyclerView();

        // Load user's stalls
        loadMyStalls();

        return view;
    }

    /**
     * Sets up the RecyclerView for stalls
     */
    private void setupRecyclerView() {
        stallAdapter = new StallAdapter(getContext(), myStalls);
        rvMyStalls.setLayoutManager(new LinearLayoutManager(getContext()));
        rvMyStalls.setAdapter(stallAdapter);
    }

    /**
     * Loads the user's stalls from Firestore
     */
    private void loadMyStalls() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        progressBar.setVisibility(View.VISIBLE);

        stallRepository.getStallsByUserId(currentUser.getUid())
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
        myStalls.clear();
        
        for (DocumentSnapshot document : querySnapshot) {
            Stall stall = document.toObject(Stall.class);
            if (stall != null) {
                myStalls.add(stall);
            }
        }

        stallAdapter.updateStalls(myStalls);
        updateEmptyState();
        progressBar.setVisibility(View.GONE);
    }

    /**
     * Shows or hides the empty state based on stalls list
     */
    private void updateEmptyState() {
        if (myStalls.isEmpty()) {
            rvMyStalls.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            rvMyStalls.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload stalls when fragment resumes
        loadMyStalls();
    }
}
