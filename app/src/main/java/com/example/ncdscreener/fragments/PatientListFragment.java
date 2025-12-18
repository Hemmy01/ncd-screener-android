package com.example.ncdscreener.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.example.ncdscreener.adapters.PatientAdapter;
import com.example.ncdscreener.model.Patient;
import com.example.ncdscreener.utils.DataExporter;
import com.example.ncdscreener.utils.ToastHelper;
import com.example.ncdscreener.viewmodel.PatientViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PatientListFragment extends Fragment {

    private RecyclerView recyclerViewPatients;
    private TextView textEmptyState;
    private FloatingActionButton fabAddPatient;
    private FloatingActionButton fabScrollToTop;
    private PatientAdapter adapter;
    private PatientViewModel viewModel;
    
    // Filter UI components
    private TextInputEditText editSearch;
    private ChipGroup chipGroupFilters;
    private Chip chipFilterAll;
    private Chip chipFilterId;
    private Chip chipFilterName;
    private Chip chipFilterPhone;
    private MaterialButton buttonExport;
    
    // Filter state
    private String currentFilterType = "all"; // all, id, name, phone
    private String currentSearchQuery = "";
    private List<Patient> allPatients = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_patient_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        recyclerViewPatients = view.findViewById(R.id.recycler_view_patients);
        textEmptyState = view.findViewById(R.id.text_empty_state);
        fabAddPatient = view.findViewById(R.id.fab_add_patient);
        fabScrollToTop = view.findViewById(R.id.fab_scroll_to_top);
        
        // Filter UI
        editSearch = view.findViewById(R.id.edit_search);
        chipGroupFilters = view.findViewById(R.id.chip_group_filters);
        chipFilterAll = view.findViewById(R.id.chip_filter_all);
        chipFilterId = view.findViewById(R.id.chip_filter_id);
        chipFilterName = view.findViewById(R.id.chip_filter_name);
        chipFilterPhone = view.findViewById(R.id.chip_filter_phone);
        buttonExport = view.findViewById(R.id.button_export);

        // Setup ViewModel FIRST before using it
        viewModel = new ViewModelProvider(requireActivity()).get(PatientViewModel.class);
        viewModel.getPatients().observe(getViewLifecycleOwner(), patients -> {
            android.util.Log.d("PatientListFragment", "Observer triggered with " + (patients != null ? patients.size() : 0) + " patients");
            allPatients = patients != null ? patients : new ArrayList<>();
            android.util.Log.d("PatientListFragment", "allPatients size: " + allPatients.size());
            applyFilters();
        });

        // Setup RecyclerView
        adapter = new PatientAdapter(null);
        adapter.setOnPatientClickListener(patient -> {
            if (patient != null && patient.getPatientId() > 0) {
                try {
                    // Select patient in ViewModel first
                    viewModel.selectPatient(patient.getPatientId());
                    // Wait a moment for ViewModel to update, then navigate
                    view.postDelayed(() -> {
                        try {
                            if (getView() != null && isAdded()) {
                                Navigation.findNavController(getView()).navigate(R.id.action_patient_list_to_patient_detail);
                            }
                        } catch (Exception e) {
                            android.util.Log.e("PatientListFragment", "Navigation error", e);
                            ToastHelper.showError(getContext(), "Unable to open patient details. Please try again");
                        }
                    }, 150);
                } catch (Exception e) {
                    android.util.Log.e("PatientListFragment", "Error selecting patient", e);
                    ToastHelper.showError(getContext(), "Error selecting patient. Please try again");
                }
            } else {
                ToastHelper.showError(getContext(), "Invalid patient information. Please select a valid patient");
            }
        });
        recyclerViewPatients.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewPatients.setAdapter(adapter);
        
        // Ensure RecyclerView is visible and properly sized
        recyclerViewPatients.post(() -> {
            android.util.Log.d("PatientListFragment", "RecyclerView dimensions - Width: " + recyclerViewPatients.getWidth() + ", Height: " + recyclerViewPatients.getHeight());
            android.util.Log.d("PatientListFragment", "RecyclerView setup complete - LayoutManager: " + (recyclerViewPatients.getLayoutManager() != null ? "SET" : "NULL") + ", Adapter: " + (recyclerViewPatients.getAdapter() != null ? "SET" : "NULL"));
        });

        // Setup scroll to top button
        setupScrollToTopButton();
        
        // Setup search and filter
        setupSearchAndFilter();
        
        // Setup export button
        setupExportButton();

        // Setup FAB - clear selected patient when adding new patient
        fabAddPatient.setOnClickListener(v -> {
            // Clear any selected patient so RegisterPatientFragment shows empty form for new patient
            viewModel.selectPatient(0); // Clear selection
            Navigation.findNavController(v).navigate(R.id.action_patient_list_to_register_patient);
        });
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Force refresh when fragment becomes visible
        android.util.Log.d("PatientListFragment", "onResume: Fragment resumed, checking patient count");
        // The LiveData observer should automatically update, but we can verify
        if (viewModel != null && getView() != null) {
            // The observer is already set up, LiveData should trigger automatically
            // But we can add a small delay to ensure the observer is active
            getView().postDelayed(() -> {
                android.util.Log.d("PatientListFragment", "onResume: Current allPatients size: " + allPatients.size());
            }, 100);
        }
    }
    
    /**
     * Setup scroll to top button functionality
     */
    private void setupScrollToTopButton() {
        // Show/hide scroll button based on scroll position
        recyclerViewPatients.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
            recyclerViewPatients.smoothScrollToPosition(0);
        });
    }
    
    /**
     * Setup search and filter functionality
     */
    private void setupSearchAndFilter() {
        // Search text watcher
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().toLowerCase(Locale.getDefault());
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // Filter chip selection
        chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                chipFilterAll.setChecked(true);
                currentFilterType = "all";
            } else {
                int checkedId = checkedIds.get(0);
                if (checkedId == R.id.chip_filter_all) {
                    currentFilterType = "all";
                } else if (checkedId == R.id.chip_filter_id) {
                    currentFilterType = "id";
                } else if (checkedId == R.id.chip_filter_name) {
                    currentFilterType = "name";
                } else if (checkedId == R.id.chip_filter_phone) {
                    currentFilterType = "phone";
                }
            }
            applyFilters();
        });
    }
    
    /**
     * Apply filters to patient list
     */
    private void applyFilters() {
        android.util.Log.d("PatientListFragment", "applyFilters called - allPatients size: " + (allPatients != null ? allPatients.size() : 0));
        if (allPatients == null || allPatients.isEmpty()) {
            android.util.Log.d("PatientListFragment", "No patients to filter, showing empty state");
            updatePatientList(new ArrayList<>());
            return;
        }
        
        List<Patient> filtered = new ArrayList<>();
        
        for (Patient patient : allPatients) {
            boolean matches = false;
            
            if (currentSearchQuery.isEmpty()) {
                matches = true;
            } else {
                switch (currentFilterType) {
                    case "id":
                        matches = String.valueOf(patient.getNationalId()).contains(currentSearchQuery) ||
                                 String.valueOf(patient.getPatientId()).contains(currentSearchQuery);
                        break;
                    case "name":
                        String fullName = patient.getFullName().toLowerCase(Locale.getDefault());
                        matches = fullName.contains(currentSearchQuery) ||
                                 (patient.getFirstName() != null && patient.getFirstName().toLowerCase(Locale.getDefault()).contains(currentSearchQuery)) ||
                                 (patient.getLastName() != null && patient.getLastName().toLowerCase(Locale.getDefault()).contains(currentSearchQuery));
                        break;
                    case "phone":
                        matches = patient.getPhoneNumber() != null && 
                                 patient.getPhoneNumber().toLowerCase(Locale.getDefault()).contains(currentSearchQuery);
                        break;
                    default: // "all"
                        matches = String.valueOf(patient.getNationalId()).contains(currentSearchQuery) ||
                                 String.valueOf(patient.getPatientId()).contains(currentSearchQuery) ||
                                 patient.getFullName().toLowerCase(Locale.getDefault()).contains(currentSearchQuery) ||
                                 (patient.getFirstName() != null && patient.getFirstName().toLowerCase(Locale.getDefault()).contains(currentSearchQuery)) ||
                                 (patient.getLastName() != null && patient.getLastName().toLowerCase(Locale.getDefault()).contains(currentSearchQuery)) ||
                                 (patient.getPhoneNumber() != null && patient.getPhoneNumber().toLowerCase(Locale.getDefault()).contains(currentSearchQuery));
                        break;
                }
            }
            
            if (matches) {
                filtered.add(patient);
            }
        }
        
        updatePatientList(filtered);
    }
    
    /**
     * Setup export button functionality
     */
    private void setupExportButton() {
        buttonExport.setOnClickListener(v -> {
            if (allPatients == null || allPatients.isEmpty()) {
                ToastHelper.showWarning(getContext(), "No patient data available to export");
                return;
            }
            
            // Show export options dialog
            new android.app.AlertDialog.Builder(getContext())
                .setTitle("Export Data")
                .setMessage("Choose export format:")
                .setPositiveButton("CSV", (dialog, which) -> exportData("csv"))
                .setNeutralButton("JSON", (dialog, which) -> exportData("json"))
                .setNegativeButton("Cancel", null)
                .show();
        });
    }
    
    /**
     * Export patient data
     */
    private void exportData(String format) {
        try {
            DataExporter exporter = new DataExporter(requireContext());
            String filePath = exporter.exportPatients(allPatients, format);
            
            if (filePath != null) {
                ToastHelper.showSuccessLong(getContext(), 
                    "Patient data exported successfully!\nSaved to: " + filePath);
            } else {
                ToastHelper.showError(getContext(), "Failed to export patient data. Please try again");
            }
        } catch (Exception e) {
            android.util.Log.e("PatientListFragment", "Export error", e);
            ToastHelper.showError(getContext(), "Export error occurred: " + e.getMessage());
        }
    }

    private void updatePatientList(List<Patient> patients) {
        android.util.Log.d("PatientListFragment", "updatePatientList called with " + (patients != null ? patients.size() : 0) + " patients");
        if (patients == null || patients.isEmpty()) {
            android.util.Log.d("PatientListFragment", "Showing empty state");
            recyclerViewPatients.setVisibility(View.GONE);
            textEmptyState.setVisibility(View.VISIBLE);
        } else {
            android.util.Log.d("PatientListFragment", "Updating adapter with " + patients.size() + " patients");
            textEmptyState.setVisibility(View.GONE);
            recyclerViewPatients.setVisibility(View.VISIBLE);
            android.util.Log.d("PatientListFragment", "RecyclerView visibility: " + (recyclerViewPatients.getVisibility() == View.VISIBLE ? "VISIBLE" : "GONE"));
            android.util.Log.d("PatientListFragment", "RecyclerView adapter: " + (recyclerViewPatients.getAdapter() != null ? "SET" : "NULL"));
            android.util.Log.d("PatientListFragment", "RecyclerView layout manager: " + (recyclerViewPatients.getLayoutManager() != null ? "SET" : "NULL"));
            adapter.updatePatients(patients);
            // Force a layout pass
            recyclerViewPatients.post(() -> {
                android.util.Log.d("PatientListFragment", "RecyclerView item count after update: " + adapter.getItemCount());
            });
        }
    }
}

