package com.example.ncdscreener.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ncdscreener.R;
import com.example.ncdscreener.adapters.ScreeningHistoryAdapter;
import com.example.ncdscreener.database.entity.ScreeningEntity;
import com.example.ncdscreener.viewmodel.ScreeningViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for displaying all screenings from all patients
 */
public class ScreeningListFragment extends Fragment {

    private RecyclerView recyclerViewScreenings;
    private TextView textEmptyState;
    private FloatingActionButton fabNewScreening;
    private FloatingActionButton fabScrollToTop;
    private ScreeningHistoryAdapter adapter;
    private ScreeningViewModel viewModel;
    private List<ScreeningEntity> allScreenings = new ArrayList<>();
    private boolean filterTodayOnly = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_screening_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get filter argument if passed
        if (getArguments() != null) {
            filterTodayOnly = getArguments().getBoolean("filter_today_only", false);
        }

        // Initialize views
        recyclerViewScreenings = view.findViewById(R.id.recycler_view_screenings);
        textEmptyState = view.findViewById(R.id.text_empty_state);
        fabNewScreening = view.findViewById(R.id.fab_new_screening);
        fabScrollToTop = view.findViewById(R.id.fab_scroll_to_top);

        // Setup ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(ScreeningViewModel.class);
        viewModel.getScreenings().observe(getViewLifecycleOwner(), screenings -> {
            allScreenings = screenings != null ? screenings : new ArrayList<>();
            // Filter screenings if today only filter is enabled
            List<ScreeningEntity> filteredScreenings = filterTodayOnly ? filterTodayScreenings(allScreenings) : allScreenings;
            updateScreeningList(filteredScreenings);
        });

        // Setup RecyclerView
        adapter = new ScreeningHistoryAdapter(null);
        adapter.setOnScreeningClickListener(screening -> {
            // Navigate to screening detail with screening ID
            Bundle args = new Bundle();
            args.putInt("screening_id", screening.getScreeningId());
            Navigation.findNavController(view).navigate(R.id.action_screening_list_to_screening_detail, args);
        });
        adapter.setOnScreeningDeleteListener(screeningId -> {
            showDeleteScreeningConfirmationDialog(screeningId);
        });
        recyclerViewScreenings.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewScreenings.setAdapter(adapter);

        // Setup scroll to top button
        setupScrollToTopButton();

        // Setup FAB - navigate to screening form (user will select patient in form or from patient list)
        fabNewScreening.setOnClickListener(v -> {
            // Navigate to screening form - user can select patient from there or navigate to patient list
            Navigation.findNavController(v).navigate(R.id.action_screening_list_to_new_screening);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh screenings when fragment becomes visible
        if (viewModel != null) {
            // The LiveData observer should automatically update
        }
    }

    /**
     * Setup scroll to top button functionality
     */
    private void setupScrollToTopButton() {
        // Show/hide scroll button based on scroll position
        recyclerViewScreenings.addOnScrollListener(new androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
                    if (firstVisiblePosition > 5) {
                        fabScrollToTop.show();
                    } else {
                        fabScrollToTop.hide();
                    }
                }
            }
        });

        // Scroll to top on click
        fabScrollToTop.setOnClickListener(v -> {
            recyclerViewScreenings.smoothScrollToPosition(0);
        });
    }

    /**
     * Filter screenings to show only today's screenings
     */
    private List<ScreeningEntity> filterTodayScreenings(List<ScreeningEntity> screenings) {
        List<ScreeningEntity> todayScreenings = new ArrayList<>();
        long todayStart = getTodayStartTimestamp();
        long todayEnd = todayStart + (24 * 60 * 60 * 1000) - 1; // End of today
        
        for (ScreeningEntity screening : screenings) {
            long screeningDate = screening.getScreeningDate();
            if (screeningDate >= todayStart && screeningDate <= todayEnd) {
                todayScreenings.add(screening);
            }
        }
        return todayScreenings;
    }
    
    /**
     * Get today's start timestamp (00:00:00)
     */
    private long getTodayStartTimestamp() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    /**
     * Update the screening list display
     */
    private void updateScreeningList(List<ScreeningEntity> screenings) {
        if (screenings == null || screenings.isEmpty()) {
            recyclerViewScreenings.setVisibility(View.GONE);
            textEmptyState.setVisibility(View.VISIBLE);
            if (filterTodayOnly) {
                textEmptyState.setText("No screenings found for today.");
            }
        } else {
            textEmptyState.setVisibility(View.GONE);
            recyclerViewScreenings.setVisibility(View.VISIBLE);
            adapter.updateScreenings(screenings);
        }
    }

    /**
     * Show confirmation dialog before deleting screening
     */
    private void showDeleteScreeningConfirmationDialog(int screeningId) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Screening")
            .setMessage("Are you sure you want to delete this screening? This will also delete all associated observations, conditions, and questionnaires. This action cannot be undone.")
            .setPositiveButton("Delete", (dialog, which) -> {
                viewModel.deleteScreening(screeningId);
                Toast.makeText(getContext(), "Screening deleted successfully", Toast.LENGTH_SHORT).show();
                // The list will automatically update via LiveData observer
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
