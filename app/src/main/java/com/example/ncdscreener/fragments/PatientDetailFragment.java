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
import com.example.ncdscreener.adapters.ScreeningHistoryAdapter;
import com.example.ncdscreener.database.NCDScreenerDatabase;
import com.example.ncdscreener.database.dao.ScreeningDao;
import com.example.ncdscreener.database.entity.ScreeningEntity;
import com.example.ncdscreener.model.Patient;
import com.example.ncdscreener.viewmodel.PatientViewModel;
import com.example.ncdscreener.viewmodel.ScreeningViewModel;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class PatientDetailFragment extends Fragment {

    private TextView textPatientName;
    private TextView textPatientDetails;
    private MaterialButton buttonEditPatient;
    private MaterialButton buttonNewScreening;
    private MaterialButton buttonDeletePatient;
    private RecyclerView recyclerViewScreeningHistory;
    private TextView textNoScreeningHistory;
    private PatientViewModel viewModel;
    private ScreeningViewModel screeningViewModel;
    private ScreeningHistoryAdapter adapter;
    private ScreeningDao screeningDao;
    private boolean isEditMode = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_patient_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        textPatientName = view.findViewById(R.id.text_patient_name);
        textPatientDetails = view.findViewById(R.id.text_patient_details);
        buttonEditPatient = view.findViewById(R.id.button_edit_patient);
        buttonNewScreening = view.findViewById(R.id.button_new_screening);
        buttonDeletePatient = view.findViewById(R.id.button_delete_patient);
        recyclerViewScreeningHistory = view.findViewById(R.id.recycler_view_screening_history);
        textNoScreeningHistory = view.findViewById(R.id.text_no_screening_history);

        // Setup RecyclerView for screening history
        adapter = new ScreeningHistoryAdapter(null);
        adapter.setOnScreeningClickListener(screening -> {
            // Navigate to screening detail with screening ID
            Bundle args = new Bundle();
            args.putInt("screening_id", screening.getScreeningId());
            androidx.navigation.Navigation.findNavController(getView())
                    .navigate(R.id.action_patient_detail_to_screening_detail, args);
        });
        adapter.setOnScreeningDeleteListener(screeningId -> {
            showDeleteScreeningConfirmationDialog(screeningId);
        });
        recyclerViewScreeningHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewScreeningHistory.setAdapter(adapter);

        // Get ScreeningDao
        screeningDao = NCDScreenerDatabase.getDatabase(requireContext()).screeningDao();

        viewModel = new ViewModelProvider(requireActivity()).get(PatientViewModel.class);
        screeningViewModel = new ViewModelProvider(requireActivity()).get(ScreeningViewModel.class);

        viewModel.getSelectedPatient().observe(getViewLifecycleOwner(), this::displayPatient);

        buttonEditPatient.setOnClickListener(v -> {
            Patient patient = viewModel.getSelectedPatient().getValue();
            if (patient != null && patient.getPatientId() > 0) {
                // Patient is already selected in ViewModel, just navigate
                Navigation.findNavController(v).navigate(R.id.action_patient_detail_to_register_patient);
            } else {
                android.widget.Toast.makeText(getContext(), "Patient information not available", android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        buttonNewScreening.setOnClickListener(v -> {
            Patient patient = viewModel.getSelectedPatient().getValue();
            if (patient != null && patient.getPatientId() > 0) {
                // Ensure patient is selected before navigating
                viewModel.selectPatient(patient.getPatientId());
                Navigation.findNavController(v).navigate(R.id.action_patient_detail_to_screening_form);
            } else {
                android.widget.Toast.makeText(getContext(), "Patient information not available", android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        buttonDeletePatient.setOnClickListener(v -> {
            Patient patient = viewModel.getSelectedPatient().getValue();
            if (patient != null && patient.getPatientId() > 0) {
                showDeleteConfirmationDialog(patient);
            } else {
                android.widget.Toast.makeText(getContext(), "Patient information not available", android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirmationDialog(Patient patient) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Patient")
            .setMessage("Are you sure you want to delete " + patient.getFullName() + "? This will also delete all associated screenings. This action cannot be undone.")
            .setPositiveButton("Delete", (dialog, which) -> {
                int patientId = patient.getPatientId();
                viewModel.deletePatient(patientId);
                android.widget.Toast.makeText(getContext(), "Patient deleted successfully", android.widget.Toast.LENGTH_SHORT).show();
                // Navigate back to patient list
                Navigation.findNavController(getView()).navigateUp();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showDeleteScreeningConfirmationDialog(int screeningId) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Screening")
            .setMessage("Are you sure you want to delete this screening? This will also delete all associated observations, conditions, and questionnaires. This action cannot be undone.")
            .setPositiveButton("Delete", (dialog, which) -> {
                screeningViewModel.deleteScreening(screeningId);
                android.widget.Toast.makeText(getContext(), "Screening deleted successfully", android.widget.Toast.LENGTH_SHORT).show();
                // Refresh the screening history
                Patient patient = viewModel.getSelectedPatient().getValue();
                if (patient != null) {
                    loadScreeningHistory(patient.getPatientId());
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void displayPatient(Patient patient) {
        if (patient != null) {
            textPatientName.setText(patient.getFullName());
            String details = "National ID: " + patient.getNationalId() + "\n" +
                           "Phone: " + (patient.getPhoneNumber() != null ? patient.getPhoneNumber() : "N/A") + "\n" +
                           "Address: " + (patient.getAddress() != null ? patient.getAddress() : "N/A");
            textPatientDetails.setText(details);

            // Load screening history for this patient
            loadScreeningHistory(patient.getPatientId());
        }
    }

    private void loadScreeningHistory(int patientId) {
        screeningDao.getScreeningsByPatientId(patientId).observe(getViewLifecycleOwner(), screenings -> {
            if (screenings == null || screenings.isEmpty()) {
                recyclerViewScreeningHistory.setVisibility(View.GONE);
                textNoScreeningHistory.setVisibility(View.VISIBLE);
            } else {
                recyclerViewScreeningHistory.setVisibility(View.VISIBLE);
                textNoScreeningHistory.setVisibility(View.GONE);
                adapter.updateScreenings(screenings);
            }
        });
    }
}

