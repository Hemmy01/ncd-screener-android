package com.example.ncdscreener.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
import com.example.ncdscreener.viewmodel.PatientViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class PatientListFragment extends Fragment {

    private RecyclerView recyclerViewPatients;
    private TextView textEmptyState;
    private FloatingActionButton fabAddPatient;
    private PatientAdapter adapter;
    private PatientViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_patient_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerViewPatients = view.findViewById(R.id.recycler_view_patients);
        textEmptyState = view.findViewById(R.id.text_empty_state);
        fabAddPatient = view.findViewById(R.id.fab_add_patient);

        // Setup ViewModel FIRST before using it
        viewModel = new ViewModelProvider(requireActivity()).get(PatientViewModel.class);
        viewModel.getPatients().observe(getViewLifecycleOwner(), this::updatePatientList);

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
                            android.widget.Toast.makeText(getContext(), "Error opening patient details", android.widget.Toast.LENGTH_SHORT).show();
                        }
                    }, 150);
                } catch (Exception e) {
                    android.util.Log.e("PatientListFragment", "Error selecting patient", e);
                    android.widget.Toast.makeText(getContext(), "Error selecting patient", android.widget.Toast.LENGTH_SHORT).show();
                }
            } else {
                android.widget.Toast.makeText(getContext(), "Invalid patient data", android.widget.Toast.LENGTH_SHORT).show();
            }
        });
        recyclerViewPatients.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewPatients.setAdapter(adapter);

        // Setup FAB - clear selected patient when adding new patient
        fabAddPatient.setOnClickListener(v -> {
            // Clear any selected patient so RegisterPatientFragment shows empty form for new patient
            viewModel.selectPatient(0); // Clear selection
            Navigation.findNavController(v).navigate(R.id.action_patient_list_to_register_patient);
        });
    }

    private void updatePatientList(List<Patient> patients) {
        if (patients == null || patients.isEmpty()) {
            recyclerViewPatients.setVisibility(View.GONE);
            textEmptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerViewPatients.setVisibility(View.VISIBLE);
            textEmptyState.setVisibility(View.GONE);
            adapter.updatePatients(patients);
        }
    }
}

