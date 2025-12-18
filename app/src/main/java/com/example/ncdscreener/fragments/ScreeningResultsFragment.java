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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ncdscreener.R;
import com.example.ncdscreener.adapters.ConditionAdapter;
import com.example.ncdscreener.adapters.ObservationAdapter;
import com.example.ncdscreener.adapters.QuestionnaireAdapter;
import com.example.ncdscreener.adapters.ServiceRequestAdapter;
import com.example.ncdscreener.model.Condition;
import com.example.ncdscreener.model.Observation;
import com.example.ncdscreener.model.Questionnaire;
import com.example.ncdscreener.model.Screening;
import com.example.ncdscreener.model.ServiceRequest;
import com.example.ncdscreener.utils.RiskScoringUtils;
import com.example.ncdscreener.utils.ToastHelper;
import com.example.ncdscreener.viewmodel.ScreeningViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import androidx.navigation.Navigation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ScreeningResultsFragment extends Fragment {

    private TextView textRiskScore;
    private TextView textRiskLevel;
    private TextView textRecommendations;
    private RecyclerView recyclerViewObservations;
    private RecyclerView recyclerViewConditions;
    private RecyclerView recyclerViewQuestionnaires;
    private RecyclerView recyclerViewServiceRequests;
    private MaterialButton buttonProvideCounseling;
    private MaterialButton buttonGenerateReferral;
    private MaterialCardView cardRiskScore;

    private ScreeningViewModel viewModel;
    private ObservationAdapter observationAdapter;
    private ConditionAdapter conditionAdapter;
    private QuestionnaireAdapter questionnaireAdapter;
    private ServiceRequestAdapter serviceRequestAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_screening_results, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        textRiskScore = view.findViewById(R.id.text_risk_score);
        textRiskLevel = view.findViewById(R.id.text_risk_level);
        textRecommendations = view.findViewById(R.id.text_recommendations);
        recyclerViewObservations = view.findViewById(R.id.recycler_view_observations);
        recyclerViewConditions = view.findViewById(R.id.recycler_view_conditions);
        recyclerViewQuestionnaires = view.findViewById(R.id.recycler_view_questionnaires);
        recyclerViewServiceRequests = view.findViewById(R.id.recycler_view_service_requests);
        buttonProvideCounseling = view.findViewById(R.id.button_provide_counseling);
        buttonGenerateReferral = view.findViewById(R.id.button_generate_referral);
        cardRiskScore = view.findViewById(R.id.card_risk_score);

        viewModel = new ViewModelProvider(requireActivity()).get(ScreeningViewModel.class);

        // Setup RecyclerViews
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

        // Observe current screening
        viewModel.getCurrentScreening().observe(getViewLifecycleOwner(), this::displayResults);

        buttonProvideCounseling.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_screening_results_to_counseling);
        });

        buttonGenerateReferral.setOnClickListener(v -> generateReferral());
    }

    private void displayResults(Screening screening) {
        if (screening == null) return;

        // Calculate and display risk score
        int riskScore = screening.calculateRiskScore();
        String riskLevel = RiskScoringUtils.getRiskLevel(riskScore);

        textRiskScore.setText(String.valueOf(riskScore));
        textRiskLevel.setText(riskLevel);

        // Set risk score card color based on level
        int colorRes;
        if (riskScore >= 50) {
            colorRes = R.color.risk_high;
        } else if (riskScore >= 25) {
            colorRes = R.color.risk_moderate;
        } else if (riskScore >= 10) {
            colorRes = R.color.risk_low;
        } else {
            colorRes = R.color.risk_minimal;
        }
        cardRiskScore.setCardBackgroundColor(getResources().getColor(colorRes, null));

        // Display observations
        List<Observation> observations = screening.getObservations();
        if (observations != null && !observations.isEmpty()) {
            observationAdapter.updateObservations(observations);
        }

        // Display conditions
        List<Condition> conditions = screening.getConditions();
        if (conditions != null && !conditions.isEmpty()) {
            conditionAdapter.updateConditions(conditions);
        }

        // Display questionnaires
        List<Questionnaire> questionnaires = screening.getQuestionnaires();
        if (questionnaires != null && !questionnaires.isEmpty()) {
            questionnaireAdapter.updateQuestionnaires(questionnaires);
            recyclerViewQuestionnaires.setVisibility(View.VISIBLE);
        } else {
            recyclerViewQuestionnaires.setVisibility(View.GONE);
        }

        // Display service requests
        List<ServiceRequest> serviceRequests = screening.getServiceRequests();
        if (serviceRequests != null && !serviceRequests.isEmpty()) {
            serviceRequestAdapter.updateServiceRequests(serviceRequests);
            recyclerViewServiceRequests.setVisibility(View.VISIBLE);
        } else {
            recyclerViewServiceRequests.setVisibility(View.GONE);
        }

        // Generate recommendations
        String recommendations = generateRecommendations(screening, riskScore, riskLevel);
        textRecommendations.setText(recommendations);
    }

    private String generateRecommendations(Screening screening, int riskScore, String riskLevel) {
        StringBuilder recommendations = new StringBuilder();

        if (riskScore >= 50) {
            recommendations.append("HIGH RISK: Immediate medical attention recommended. ");
        } else if (riskScore >= 25) {
            recommendations.append("MODERATE RISK: Regular monitoring and lifestyle modifications advised. ");
        } else if (riskScore >= 10) {
            recommendations.append("LOW RISK: Preventive measures and periodic screening recommended. ");
        } else {
            recommendations.append("MINIMAL RISK: Continue healthy lifestyle practices. ");
        }

        List<Condition> conditions = screening.getConditions();
        if (conditions != null && !conditions.isEmpty()) {
            recommendations.append("\n\nIdentified Conditions:\n");
            for (Condition condition : conditions) {
                recommendations.append("• ").append(condition.getConditionName()).append("\n");
            }
        }

        recommendations.append("\nGeneral Recommendations:\n");
        recommendations.append("• Maintain healthy diet\n");
        recommendations.append("• Regular physical activity\n");
        recommendations.append("• Regular health check-ups\n");
        if (riskScore >= 25) {
            recommendations.append("• Consider referral to healthcare facility");
        }

        return recommendations.toString();
    }

    private void generateReferral() {
        Screening screening = viewModel.getCurrentScreening().getValue();
        if (screening == null) {
            ToastHelper.showError(getContext(), "No screening data available. Please complete a screening first");
            return;
        }

        // Generate referral code
        String referralCode = "REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        // Build reason text based on screening results
        StringBuilder reasonText = new StringBuilder();
        reasonText.append("NCD Screening Referral\n\n");
        reasonText.append("Patient: ").append(screening.getPatient() != null ? screening.getPatient().getFullName() : "N/A").append("\n");
        reasonText.append("Screening Date: ").append(screening.getScreeningDate()).append("\n");
        reasonText.append("Risk Score: ").append(screening.calculateRiskScore()).append("\n\n");
        
        // Add conditions
        List<Condition> conditions = screening.getConditions();
        if (conditions != null && !conditions.isEmpty()) {
            reasonText.append("Identified Conditions:\n");
            for (Condition condition : conditions) {
                reasonText.append("- ").append(condition.getConditionName()).append("\n");
            }
            reasonText.append("\n");
        }
        
        // Add observations summary
        List<Observation> observations = screening.getObservations();
        if (observations != null && !observations.isEmpty()) {
            reasonText.append("Key Findings:\n");
            for (Observation obs : observations) {
                if ("blood_pressure_systolic".equals(obs.getObservationType()) || 
                    "blood_pressure_diastolic".equals(obs.getObservationType()) ||
                    "glucose".equals(obs.getObservationType())) {
                    reasonText.append("- ").append(obs.getObservationType())
                              .append(": ").append(obs.getValue()).append(" ").append(obs.getUnit()).append("\n");
                }
            }
        }
        
        reasonText.append("\nRecommendation: Further clinical evaluation and follow-up required.");

        // Create ServiceRequest
        ServiceRequest serviceRequest = new ServiceRequest();
        serviceRequest.setReferralCode(referralCode);
        serviceRequest.setReasonText(reasonText.toString());
        serviceRequest.setStatus("active");

        // Add to screening
        List<ServiceRequest> serviceRequests = screening.getServiceRequests();
        if (serviceRequests == null) {
            serviceRequests = new ArrayList<>();
        }
        serviceRequests.add(serviceRequest);
        screening.setServiceRequests(serviceRequests);

        // Save screening with new ServiceRequest
        viewModel.saveScreening(screening);

        ToastHelper.showSuccessLong(getContext(), "Medical referral generated successfully! Referral Code: " + referralCode);
    }
}

