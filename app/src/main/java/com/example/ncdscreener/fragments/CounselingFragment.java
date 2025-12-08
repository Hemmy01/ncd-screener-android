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

import com.example.ncdscreener.R;
import com.example.ncdscreener.model.Condition;
import com.example.ncdscreener.model.Screening;
import com.example.ncdscreener.viewmodel.ScreeningViewModel;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Fragment for providing counseling and lifestyle advice to patients
 */
public class CounselingFragment extends Fragment {

    private TextView textPatientName;
    private TextView textScreeningDate;
    private TextView textConditionCounseling;
    private TextView textFollowupCounseling;
    private MaterialButton buttonCompleteCounseling;
    
    private ScreeningViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_counseling, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        textPatientName = view.findViewById(R.id.text_patient_name_counseling);
        textScreeningDate = view.findViewById(R.id.text_screening_date);
        textConditionCounseling = view.findViewById(R.id.text_condition_counseling);
        textFollowupCounseling = view.findViewById(R.id.text_followup_counseling);
        buttonCompleteCounseling = view.findViewById(R.id.button_complete_counseling);

        viewModel = new ViewModelProvider(requireActivity()).get(ScreeningViewModel.class);

        // Observe current screening
        viewModel.getCurrentScreening().observe(getViewLifecycleOwner(), this::displayCounseling);

        buttonCompleteCounseling.setOnClickListener(v -> {
            // Navigate back to home
            Navigation.findNavController(v).navigateUp();
        });
    }

    private void displayCounseling(Screening screening) {
        if (screening == null) return;

        // Display patient name
        if (screening.getPatient() != null) {
            textPatientName.setText(screening.getPatient().getFullName());
        }

        // Display screening date
        if (screening.getScreeningDate() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            textScreeningDate.setText("Screening Date: " + dateFormat.format(screening.getScreeningDate()));
        }

        // Generate condition-specific counseling
        String conditionAdvice = generateConditionSpecificCounseling(screening);
        textConditionCounseling.setText(conditionAdvice);

        // Generate follow-up recommendations
        String followupAdvice = generateFollowupCounseling(screening);
        textFollowupCounseling.setText(followupAdvice);
    }

    private String generateConditionSpecificCounseling(Screening screening) {
        StringBuilder advice = new StringBuilder();
        List<Condition> conditions = screening.getConditions();

        if (conditions == null || conditions.isEmpty()) {
            advice.append("Your screening results show no immediate concerns. ");
            advice.append("Continue maintaining a healthy lifestyle with regular exercise, ");
            advice.append("balanced nutrition, and routine health check-ups.");
            return advice.toString();
        }

        for (Condition condition : conditions) {
            String conditionCode = condition.getConditionCode();
            String conditionName = condition.getConditionName();

            if (conditionCode.contains("HYPERTENSION")) {
                advice.append("Hypertension Management:\n");
                advice.append("• Reduce sodium intake to less than 2,300 mg per day\n");
                advice.append("• Follow DASH (Dietary Approaches to Stop Hypertension) diet\n");
                advice.append("• Monitor blood pressure regularly\n");
                advice.append("• Take prescribed medications consistently\n");
                advice.append("• Limit alcohol consumption\n\n");
            }

            if (conditionCode.contains("DIABETES")) {
                advice.append("Diabetes Management:\n");
                advice.append("• Monitor blood glucose levels as recommended\n");
                advice.append("• Follow a carbohydrate-controlled meal plan\n");
                advice.append("• Engage in regular physical activity\n");
                advice.append("• Take medications or insulin as prescribed\n");
                advice.append("• Check feet daily for any wounds or changes\n");
                advice.append("• Schedule regular eye and kidney function tests\n\n");
            }
        }

        if (advice.length() == 0) {
            advice.append("Based on your screening results, follow the general lifestyle recommendations provided.");
        }

        return advice.toString();
    }

    private String generateFollowupCounseling(Screening screening) {
        StringBuilder followup = new StringBuilder();
        int riskScore = screening.calculateRiskScore();

        followup.append("• Schedule regular health check-ups every 3-6 months\n");

        if (riskScore >= 50) {
            followup.append("• URGENT: Seek immediate medical attention\n");
            followup.append("• Follow up with healthcare provider within 1 week\n");
            followup.append("• Monitor vital signs daily if possible\n");
        } else if (riskScore >= 25) {
            followup.append("• Schedule appointment with healthcare provider within 2-4 weeks\n");
            followup.append("• Monitor your condition and report any changes\n");
        } else {
            followup.append("• Continue routine health monitoring\n");
            followup.append("• Annual comprehensive health check-up recommended\n");
        }

        followup.append("• Take any prescribed medications as directed\n");
        followup.append("• Contact healthcare provider if symptoms worsen\n");
        followup.append("• Keep a record of your health measurements\n");

        return followup.toString();
    }
}

