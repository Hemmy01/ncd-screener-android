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

import com.example.ncdscreener.R;
import com.example.ncdscreener.model.Condition;
import com.example.ncdscreener.model.Observation;
import com.example.ncdscreener.model.Patient;
import com.example.ncdscreener.model.Questionnaire;
import com.example.ncdscreener.model.Screening;
import com.example.ncdscreener.model.CHW;
import com.example.ncdscreener.utils.HealthDataValidator;
import com.example.ncdscreener.utils.RiskScoringUtils;
import com.example.ncdscreener.viewmodel.PatientViewModel;
import com.example.ncdscreener.viewmodel.ScreeningViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ScreeningFormFragment extends Fragment {

    private TextInputEditText editSystolicBp;
    private TextInputEditText editDiastolicBp;
    private TextInputEditText editGlucose;
    private TextInputEditText editWeight;
    private TextInputEditText editHeight;
    private TextView textBmi;
    private SwitchMaterial switchFamilyHistoryDiabetes;
    private SwitchMaterial switchFamilyHistoryHypertension;
    private SwitchMaterial switchSmoking;
    private SwitchMaterial switchPhysicalInactivity;
    private SwitchMaterial switchUnhealthyDiet;
    private MaterialButton buttonSubmitScreening;

    private PatientViewModel patientViewModel;
    private ScreeningViewModel screeningViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_screening_form, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editSystolicBp = view.findViewById(R.id.edit_systolic_bp);
        editDiastolicBp = view.findViewById(R.id.edit_diastolic_bp);
        editGlucose = view.findViewById(R.id.edit_glucose);
        editWeight = view.findViewById(R.id.edit_weight);
        editHeight = view.findViewById(R.id.edit_height);
        textBmi = view.findViewById(R.id.text_bmi);
        switchFamilyHistoryDiabetes = view.findViewById(R.id.switch_family_history_diabetes);
        switchFamilyHistoryHypertension = view.findViewById(R.id.switch_family_history_hypertension);
        switchSmoking = view.findViewById(R.id.switch_smoking);
        switchPhysicalInactivity = view.findViewById(R.id.switch_physical_inactivity);
        switchUnhealthyDiet = view.findViewById(R.id.switch_unhealthy_diet);
        buttonSubmitScreening = view.findViewById(R.id.button_submit_screening);

        patientViewModel = new ViewModelProvider(requireActivity()).get(PatientViewModel.class);
        screeningViewModel = new ViewModelProvider(requireActivity()).get(ScreeningViewModel.class);

        // Check if patient is selected when fragment loads
        Patient currentPatient = patientViewModel.getSelectedPatient().getValue();
        if (currentPatient == null || currentPatient.getPatientId() == 0) {
            // No patient selected - navigate back to patient list
            // User can add a new patient or select existing one from there
            Toast.makeText(getContext(), "Please select a patient first. You can add a new patient or select an existing one.", Toast.LENGTH_LONG).show();
            // Navigate to patient list after a short delay to allow toast to show
            view.postDelayed(() -> {
                if (getView() != null && isAdded()) {
                    try {
                        Navigation.findNavController(getView()).navigate(R.id.patientListFragment);
                    } catch (Exception e) {
                        android.util.Log.e("ScreeningFormFragment", "Navigation error", e);
                    }
                }
            }, 2500);
        }

        // Calculate BMI when weight or height changes
        TextWatcher bmiCalculator = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateBMI();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        editWeight.addTextChangedListener(bmiCalculator);
        editHeight.addTextChangedListener(bmiCalculator);

        buttonSubmitScreening.setOnClickListener(v -> submitScreening());
    }

    private void calculateBMI() {
        try {
            String weightStr = editWeight.getText().toString();
            String heightStr = editHeight.getText().toString();

            if (!weightStr.isEmpty() && !heightStr.isEmpty()) {
                double weight = Double.parseDouble(weightStr);
                double height = Double.parseDouble(heightStr);

                if (HealthDataValidator.isValidWeight(weight) && HealthDataValidator.isValidHeight(height)) {
                    double bmi = HealthDataValidator.calculateBMI(weight, height);
                    textBmi.setText(String.format("BMI: %.1f", bmi));
                } else {
                    textBmi.setText("BMI: Invalid values");
                }
            }
        } catch (NumberFormatException e) {
            textBmi.setText("BMI: --");
        }
    }

    private void submitScreening() {
        try {
            // Validate and collect observations
            List<Observation> observations = new ArrayList<>();
            
            String systolicStr = editSystolicBp.getText().toString();
            String diastolicStr = editDiastolicBp.getText().toString();
            String glucoseStr = editGlucose.getText().toString();
            String weightStr = editWeight.getText().toString();
            String heightStr = editHeight.getText().toString();

            if (systolicStr.isEmpty() || diastolicStr.isEmpty()) {
                Toast.makeText(getContext(), "Please enter blood pressure values", Toast.LENGTH_SHORT).show();
                return;
            }

            double systolic = Double.parseDouble(systolicStr);
            double diastolic = Double.parseDouble(diastolicStr);

            if (!HealthDataValidator.isValidBloodPressure(systolic, diastolic)) {
                Toast.makeText(getContext(), "Invalid blood pressure values", Toast.LENGTH_SHORT).show();
                return;
            }

            // Add blood pressure observations
            Observation obsSystolic = new Observation();
            obsSystolic.setObservationId(0); // Will be auto-generated by database
            obsSystolic.setObservationType("blood_pressure_systolic");
            obsSystolic.setValue(systolic);
            obsSystolic.setUnit("mmHg");
            observations.add(obsSystolic);

            Observation obsDiastolic = new Observation();
            obsDiastolic.setObservationId(0); // Will be auto-generated by database
            obsDiastolic.setObservationType("blood_pressure_diastolic");
            obsDiastolic.setValue(diastolic);
            obsDiastolic.setUnit("mmHg");
            observations.add(obsDiastolic);

            // Add glucose if provided
            if (!glucoseStr.isEmpty()) {
                double glucose = Double.parseDouble(glucoseStr);
                if (HealthDataValidator.isValidGlucose(glucose)) {
                    Observation obsGlucose = new Observation();
                    obsGlucose.setObservationId(0); // Will be auto-generated by database
                    obsGlucose.setObservationType("glucose");
                    obsGlucose.setValue(glucose);
                    obsGlucose.setUnit("mg/dL");
                    observations.add(obsGlucose);
                }
            }

            // Add BMI if weight and height provided
            if (!weightStr.isEmpty() && !heightStr.isEmpty()) {
                double weight = Double.parseDouble(weightStr);
                double height = Double.parseDouble(heightStr);
                if (HealthDataValidator.isValidWeight(weight) && HealthDataValidator.isValidHeight(height)) {
                    double bmi = HealthDataValidator.calculateBMI(weight, height);
                    Observation obsBmi = new Observation();
                    obsBmi.setObservationId(0); // Will be auto-generated by database
                    obsBmi.setObservationType("bmi");
                    obsBmi.setValue(bmi);
                    obsBmi.setUnit("kg/m²");
                    observations.add(obsBmi);
                }
            }

            // Collect questionnaire responses
            List<Questionnaire> questionnaires = new ArrayList<>();
            
            addQuestionnaire(questionnaires, "family_history_diabetes", switchFamilyHistoryDiabetes.isChecked());
            addQuestionnaire(questionnaires, "family_history_hypertension", switchFamilyHistoryHypertension.isChecked());
            addQuestionnaire(questionnaires, "smoking", switchSmoking.isChecked());
            addQuestionnaire(questionnaires, "physical_inactivity", switchPhysicalInactivity.isChecked());
            addQuestionnaire(questionnaires, "unhealthy_diet", switchUnhealthyDiet.isChecked());

            // Calculate risk score
            int riskScore = RiskScoringUtils.calculateOverallRiskScore(observations, questionnaires);
            String riskLevel = RiskScoringUtils.getRiskLevel(riskScore);

            // Identify conditions
            List<Condition> conditions = new ArrayList<>();
            String bpCategory = HealthDataValidator.categorizeBloodPressure(systolic, diastolic);
            if (bpCategory.contains("Hypertension")) {
                Condition condition = new Condition();
                condition.setConditionCode("HYPERTENSION");
                condition.setConditionName(bpCategory);
                conditions.add(condition);
            }

            if (!glucoseStr.isEmpty()) {
                double glucose = Double.parseDouble(glucoseStr);
                String glucoseCategory = HealthDataValidator.categorizeGlucose(glucose);
                if (glucoseCategory.contains("Diabetic") || glucoseCategory.contains("Prediabetic")) {
                    Condition condition = new Condition();
                    condition.setConditionCode("DIABETES");
                    condition.setConditionName(glucoseCategory);
                    conditions.add(condition);
                }
            }

            // Get selected patient - required for screening
            Patient patient = patientViewModel.getSelectedPatient().getValue();
            if (patient == null || patient.getPatientId() == 0) {
                Toast.makeText(getContext(), "Please select a patient first", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get current logged-in CHW from SharedPreferences
            android.content.SharedPreferences prefs = requireContext().getSharedPreferences("NCDScreenerPrefs", android.content.Context.MODE_PRIVATE);
            int chwId = prefs.getInt("chwId", 0);
            String chwFirstName = prefs.getString("chwFirstName", "");
            String chwLastName = prefs.getString("chwLastName", "");
            
            if (chwId == 0) {
                Toast.makeText(getContext(), "CHW information not found. Please login again.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create CHW object
            CHW chw = new CHW();
            chw.setChwId(chwId);
            chw.setFirstName(chwFirstName);
            chw.setLastName(chwLastName);

            // Create screening
            Screening screening = new Screening();
            screening.setScreeningId(0); // Will be auto-generated by database
            screening.setScreeningDate(new Date());
            screening.setLocation("Community Visit");
            screening.setPatient(patient);
            screening.setChw(chw);

            screening.setObservations(observations);
            screening.setQuestionnaires(questionnaires);
            screening.setConditions(conditions);

            // Set risk score in observations
            for (Observation obs : observations) {
                obs.setFinalRiskScore(riskLevel + " (" + riskScore + ")");
            }

            // Save screening
            screeningViewModel.saveScreening(screening);
            screeningViewModel.setCurrentScreening(screening);

            Toast.makeText(getContext(), "Screening completed. Risk Score: " + riskScore, Toast.LENGTH_SHORT).show();
            Navigation.findNavController(getView()).navigate(R.id.action_screening_form_to_screening_results);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Please enter valid numeric values", Toast.LENGTH_SHORT).show();
        }
    }

    private void addQuestionnaire(List<Questionnaire> questionnaires, String questionCode, boolean isChecked) {
        Questionnaire q = new Questionnaire();
        q.setQuestionCode(questionCode);
        q.setAnswer(isChecked ? "yes" : "no");
        questionnaires.add(q);
    }
}

