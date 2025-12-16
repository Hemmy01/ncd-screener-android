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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ncdscreener.R;
import com.example.ncdscreener.adapters.ConditionAdapter;
import com.example.ncdscreener.adapters.ObservationAdapter;
import com.example.ncdscreener.adapters.QuestionnaireAdapter;
import com.example.ncdscreener.adapters.ServiceRequestAdapter;
import com.example.ncdscreener.database.NCDScreenerDatabase;
import com.example.ncdscreener.database.dao.ScreeningDao;
import com.example.ncdscreener.model.Screening;
import com.example.ncdscreener.viewmodel.ScreeningViewModel;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Fragment for displaying detailed screening information including all captured data
 */
public class ScreeningDetailFragment extends Fragment {

    private TextView textPatientName;
    private TextView textChwName;
    private TextView textScreeningDate;
    private TextView textScreeningLocation;
    private TextView textRiskScore;
    private TextView textRiskLevel;
    private RecyclerView recyclerViewObservations;
    private RecyclerView recyclerViewConditions;
    private RecyclerView recyclerViewQuestionnaires;
    private RecyclerView recyclerViewServiceRequests;
    private MaterialButton buttonBack;

    private ScreeningViewModel viewModel;
    private ObservationAdapter observationAdapter;
    private ConditionAdapter conditionAdapter;
    private QuestionnaireAdapter questionnaireAdapter;
    private ServiceRequestAdapter serviceRequestAdapter;
    private ScreeningDao screeningDao;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_screening_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        textPatientName = view.findViewById(R.id.text_patient_name_detail);
        textChwName = view.findViewById(R.id.text_chw_name_detail);
        textScreeningDate = view.findViewById(R.id.text_screening_date_detail);
        textScreeningLocation = view.findViewById(R.id.text_screening_location_detail);
        textRiskScore = view.findViewById(R.id.text_risk_score_detail);
        textRiskLevel = view.findViewById(R.id.text_risk_level_detail);
        recyclerViewObservations = view.findViewById(R.id.recycler_view_observations_detail);
        recyclerViewConditions = view.findViewById(R.id.recycler_view_conditions_detail);
        recyclerViewQuestionnaires = view.findViewById(R.id.recycler_view_questionnaires_detail);
        recyclerViewServiceRequests = view.findViewById(R.id.recycler_view_service_requests_detail);
        buttonBack = view.findViewById(R.id.button_back);

        // Setup adapters
        observationAdapter = new ObservationAdapter(null);
        recyclerViewObservations.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewObservations.setAdapter(observationAdapter);

        conditionAdapter = new ConditionAdapter(null);
        recyclerViewConditions.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewConditions.setAdapter(conditionAdapter);

        questionnaireAdapter = new QuestionnaireAdapter(null);
        recyclerViewQuestionnaires.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewQuestionnaires.setAdapter(questionnaireAdapter);

        serviceRequestAdapter = new ServiceRequestAdapter(null);
        recyclerViewServiceRequests.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewServiceRequests.setAdapter(serviceRequestAdapter);

        // Get ViewModel and Database
        viewModel = new ViewModelProvider(requireActivity()).get(ScreeningViewModel.class);
        screeningDao = NCDScreenerDatabase.getDatabase(requireContext()).screeningDao();

        // Back button
        buttonBack.setOnClickListener(v -> requireActivity().onBackPressed());

        // Get screening ID from arguments
        Bundle args = getArguments();
        if (args != null) {
            int screeningId = args.getInt("screening_id", -1);
            if (screeningId != -1) {
                loadScreeningDetail(screeningId);
            }
        }
    }

    private void loadScreeningDetail(int screeningId) {
        // Load screening from database with all related data
        viewModel.loadScreeningDetail(screeningId);
        viewModel.getSelectedScreening().observe(getViewLifecycleOwner(), screening -> {
            if (screening != null) {
                displayScreeningDetail(screening);
            }
        });
    }

    private void displayScreeningDetail(Screening screening) {
        // Display patient information
        if (screening.getPatient() != null) {
            textPatientName.setText("Patient: " + screening.getPatient().getFullName());
        }

        // Display CHW information
        if (screening.getChw() != null) {
            textChwName.setText("CHW: " + screening.getChw().getFullName());
        }

        // Display screening date
        if (screening.getScreeningDate() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            textScreeningDate.setText("Date: " + dateFormat.format(screening.getScreeningDate()));
        }

        // Display location
        String location = screening.getLocation();
        textScreeningLocation.setText("Location: " + (location != null && !location.isEmpty() ? location : "N/A"));

        // Display risk score
        int riskScore = screening.calculateRiskScore();
        textRiskScore.setText("Risk Score: " + riskScore);

        // Display risk level with color
        String riskLevel = getRiskLevelText(riskScore);
        textRiskLevel.setText("Risk Level: " + riskLevel);
        int riskColor = getRiskLevelColor(riskScore);
        textRiskLevel.setTextColor(getResources().getColor(riskColor, null));

        // Display observations
        if (screening.getObservations() != null && !screening.getObservations().isEmpty()) {
            observationAdapter.updateObservations(screening.getObservations());
            recyclerViewObservations.setVisibility(View.VISIBLE);
        } else {
            recyclerViewObservations.setVisibility(View.GONE);
        }

        // Display conditions
        if (screening.getConditions() != null && !screening.getConditions().isEmpty()) {
            conditionAdapter.updateConditions(screening.getConditions());
            recyclerViewConditions.setVisibility(View.VISIBLE);
        } else {
            recyclerViewConditions.setVisibility(View.GONE);
        }

        // Display questionnaires
        if (screening.getQuestionnaires() != null && !screening.getQuestionnaires().isEmpty()) {
            questionnaireAdapter.updateQuestionnaires(screening.getQuestionnaires());
            recyclerViewQuestionnaires.setVisibility(View.VISIBLE);
        } else {
            recyclerViewQuestionnaires.setVisibility(View.GONE);
        }

        // Display service requests
        if (screening.getServiceRequests() != null && !screening.getServiceRequests().isEmpty()) {
            serviceRequestAdapter.updateServiceRequests(screening.getServiceRequests());
            recyclerViewServiceRequests.setVisibility(View.VISIBLE);
        } else {
            recyclerViewServiceRequests.setVisibility(View.GONE);
        }
    }

    private String getRiskLevelText(int riskScore) {
        if (riskScore >= 50) {
            return "High Risk";
        } else if (riskScore >= 25) {
            return "Moderate Risk";
        } else if (riskScore >= 10) {
            return "Low Risk";
        } else {
            return "Minimal Risk";
        }
    }

    private int getRiskLevelColor(int riskScore) {
        if (riskScore >= 50) {
            return R.color.risk_high;
        } else if (riskScore >= 25) {
            return R.color.risk_moderate;
        } else if (riskScore >= 10) {
            return R.color.risk_low;
        } else {
            return R.color.risk_minimal;
        }
    }
}
