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
import com.example.ncdscreener.utils.ToastHelper;
import com.example.ncdscreener.viewmodel.PatientViewModel;
import com.example.ncdscreener.viewmodel.ScreeningViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ScreeningFormFragment extends Fragment {

    private TextInputLayout layoutPatientSelection;
    private TextInputLayout layoutSystolicBp;
    private TextInputLayout layoutDiastolicBp;
    private TextInputLayout layoutGlucose;
    private TextInputLayout layoutWeight;
    private TextInputLayout layoutHeight;
    private MaterialAutoCompleteTextView editPatientSelection;
    private TextInputEditText editSystolicBp;
    private TextInputEditText editDiastolicBp;
    private TextInputEditText editGlucose;
    private TextInputEditText editWeight;
    private TextInputEditText editHeight;
    private TextView textBmi;
    private List<Patient> allPatients = new ArrayList<>();
    private SwitchMaterial switchFamilyHistoryDiabetes;
    private SwitchMaterial switchFamilyHistoryHypertension;
    private SwitchMaterial switchSmoking;
    private SwitchMaterial switchPhysicalInactivity;
    private SwitchMaterial switchUnhealthyDiet;
    private MaterialButton buttonSubmitScreening;

    private PatientViewModel patientViewModel;
    private ScreeningViewModel screeningViewModel;
    
    // Need to get ViewModel for patients - actually already have patientViewModel above

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_screening_form, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        layoutPatientSelection = view.findViewById(R.id.layout_patient_selection);
        editPatientSelection = view.findViewById(R.id.edit_patient_selection);
        layoutSystolicBp = view.findViewById(R.id.layout_systolic_bp);
        layoutDiastolicBp = view.findViewById(R.id.layout_diastolic_bp);
        layoutGlucose = view.findViewById(R.id.layout_glucose);
        layoutWeight = view.findViewById(R.id.layout_weight);
        layoutHeight = view.findViewById(R.id.layout_height);
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

        // Setup patient selection dropdown
        setupPatientSelection();

        // Note: Patient selection will be handled in setupPatientSelection when patients load
        // If a patient was pre-selected, it will be set in the observer

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

        // Setup real-time validation
        setupInputValidation();

        buttonSubmitScreening.setOnClickListener(v -> submitScreening());
    }

    /**
     * Setup patient selection dropdown
     */
    private void setupPatientSelection() {
        // Check if views are initialized
        if (editPatientSelection == null || layoutPatientSelection == null) {
            return;
        }
        
        // Observe patients list
        patientViewModel.getPatients().observe(getViewLifecycleOwner(), patients -> {
            if (editPatientSelection == null) return; // View might be destroyed
            
            allPatients = patients != null ? patients : new ArrayList<>();
            
            // Create display strings for dropdown
            List<String> patientDisplayNames = new ArrayList<>();
            for (Patient patient : allPatients) {
                patientDisplayNames.add(patient.getFullName() + " (ID: " + patient.getNationalId() + ")");
            }
            
            // Setup adapter
            ArrayAdapter<String> patientAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                patientDisplayNames
            );
            editPatientSelection.setAdapter(patientAdapter);
            
            // Check if a patient was pre-selected (e.g., from patient detail page)
            Patient currentPatient = patientViewModel.getSelectedPatient().getValue();
            if (currentPatient != null && currentPatient.getPatientId() > 0 && editPatientSelection != null) {
                // Pre-select this patient in the dropdown
                for (int i = 0; i < allPatients.size(); i++) {
                    if (allPatients.get(i).getPatientId() == currentPatient.getPatientId()) {
                        String displayName = allPatients.get(i).getFullName() + " (ID: " + allPatients.get(i).getNationalId() + ")";
                        editPatientSelection.setText(displayName, false);
                        break;
                    }
                }
            }

            // Handle patient selection
            if (editPatientSelection != null) {
                editPatientSelection.setOnItemClickListener((parent, view, position, id) -> {
                    if (position < allPatients.size()) {
                        Patient selectedPatient = allPatients.get(position);
                        patientViewModel.selectPatient(selectedPatient.getPatientId());
                        if (layoutPatientSelection != null) {
                            layoutPatientSelection.setError(null);
                        }
                    }
                });
            }
        });
    }

    /**
     * Setup real-time validation for all input fields
     */
    private void setupInputValidation() {
        // Systolic BP validation
        editSystolicBp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateSystolicBP(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Diastolic BP validation
        editDiastolicBp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateDiastolicBP(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Glucose validation
        editGlucose.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateGlucose(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Weight validation
        editWeight.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateWeight(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Height validation
        editHeight.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateHeight(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Validates systolic blood pressure input
     */
    private void validateSystolicBP(String value) {
        if (value == null || value.trim().isEmpty()) {
            layoutSystolicBp.setError(null);
            return;
        }

        try {
            double systolic = Double.parseDouble(value);
            String diastolicStr = editDiastolicBp.getText().toString();
            
            if (systolic < 50 || systolic > 300) {
                layoutSystolicBp.setError("Systolic BP must be between 50-300 mmHg");
            } else if (!diastolicStr.isEmpty()) {
                try {
                    double diastolic = Double.parseDouble(diastolicStr);
                    if (systolic <= diastolic) {
                        layoutSystolicBp.setError("Systolic must be greater than diastolic");
                    } else {
                        layoutSystolicBp.setError(null);
                    }
                } catch (NumberFormatException e) {
                    layoutSystolicBp.setError(null);
                }
            } else {
                layoutSystolicBp.setError(null);
            }
        } catch (NumberFormatException e) {
            layoutSystolicBp.setError("Please enter a valid number");
        }
    }

    /**
     * Validates diastolic blood pressure input
     */
    private void validateDiastolicBP(String value) {
        if (value == null || value.trim().isEmpty()) {
            layoutDiastolicBp.setError(null);
            return;
        }

        try {
            double diastolic = Double.parseDouble(value);
            String systolicStr = editSystolicBp.getText().toString();
            
            if (diastolic < 30 || diastolic > 200) {
                layoutDiastolicBp.setError("Diastolic BP must be between 30-200 mmHg");
            } else if (!systolicStr.isEmpty()) {
                try {
                    double systolic = Double.parseDouble(systolicStr);
                    if (systolic <= diastolic) {
                        layoutDiastolicBp.setError("Diastolic must be less than systolic");
                    } else {
                        layoutDiastolicBp.setError(null);
                        // Also clear systolic error if it was due to comparison
                        if (layoutSystolicBp.getError() != null && 
                            layoutSystolicBp.getError().toString().contains("greater than")) {
                            validateSystolicBP(systolicStr);
                        }
                    }
                } catch (NumberFormatException e) {
                    layoutDiastolicBp.setError(null);
                }
            } else {
                layoutDiastolicBp.setError(null);
            }
        } catch (NumberFormatException e) {
            layoutDiastolicBp.setError("Please enter a valid number");
        }
    }

    /**
     * Validates glucose level input
     */
    private void validateGlucose(String value) {
        if (value == null || value.trim().isEmpty()) {
            layoutGlucose.setError(null);
            return;
        }

        try {
            double glucose = Double.parseDouble(value);
            if (glucose < 50 || glucose > 600) {
                layoutGlucose.setError("Glucose must be between 50-600 mg/dL");
            } else {
                layoutGlucose.setError(null);
            }
        } catch (NumberFormatException e) {
            layoutGlucose.setError("Please enter a valid number");
        }
    }

    /**
     * Validates weight input
     */
    private void validateWeight(String value) {
        if (value == null || value.trim().isEmpty()) {
            layoutWeight.setError(null);
            return;
        }

        try {
            double weight = Double.parseDouble(value);
            if (weight < 20 || weight > 300) {
                layoutWeight.setError("Weight must be between 20-300 kg");
            } else {
                layoutWeight.setError(null);
            }
        } catch (NumberFormatException e) {
            layoutWeight.setError("Please enter a valid number");
        }
    }

    /**
     * Validates height input
     */
    private void validateHeight(String value) {
        if (value == null || value.trim().isEmpty()) {
            layoutHeight.setError(null);
            return;
        }

        try {
            double height = Double.parseDouble(value);
            if (height < 100 || height > 250) {
                layoutHeight.setError("Height must be between 100-250 cm");
            } else {
                layoutHeight.setError(null);
            }
        } catch (NumberFormatException e) {
            layoutHeight.setError("Please enter a valid number");
        }
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

            // Clear any previous errors and validate
            boolean hasErrors = false;
            
            if (systolicStr.isEmpty() || diastolicStr.isEmpty()) {
                if (systolicStr.isEmpty()) {
                    layoutSystolicBp.setError("Systolic BP is required");
                    hasErrors = true;
                }
                if (diastolicStr.isEmpty()) {
                    layoutDiastolicBp.setError("Diastolic BP is required");
                    hasErrors = true;
                }
                if (hasErrors) {
                    ToastHelper.showWarning(getContext(), "Please enter both systolic and diastolic blood pressure values");
                    return;
                }
            }

            double systolic = Double.parseDouble(systolicStr);
            double diastolic = Double.parseDouble(diastolicStr);

            // Validate blood pressure values
            validateSystolicBP(systolicStr);
            validateDiastolicBP(diastolicStr);
            
            if (layoutSystolicBp.getError() != null || layoutDiastolicBp.getError() != null) {
                ToastHelper.showError(getContext(), "Please correct the blood pressure values");
                return;
            }

            if (!HealthDataValidator.isValidBloodPressure(systolic, diastolic)) {
                layoutSystolicBp.setError("Invalid blood pressure values");
                layoutDiastolicBp.setError("Invalid blood pressure values");
                ToastHelper.showError(getContext(), "Invalid blood pressure values. Systolic should be 50-300 mmHg and diastolic should be 30-200 mmHg, with systolic greater than diastolic");
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
                validateGlucose(glucoseStr);
                if (layoutGlucose.getError() == null) {
                    double glucose = Double.parseDouble(glucoseStr);
                    if (HealthDataValidator.isValidGlucose(glucose)) {
                        Observation obsGlucose = new Observation();
                        obsGlucose.setObservationId(0); // Will be auto-generated by database
                        obsGlucose.setObservationType("glucose");
                        obsGlucose.setValue(glucose);
                        obsGlucose.setUnit("mg/dL");
                        observations.add(obsGlucose);
                    }
                } else {
                    ToastHelper.showError(getContext(), "Please correct the glucose value");
                    return;
                }
            }

            // Add BMI if weight and height provided
            if (!weightStr.isEmpty() && !heightStr.isEmpty()) {
                validateWeight(weightStr);
                validateHeight(heightStr);
                if (layoutWeight.getError() == null && layoutHeight.getError() == null) {
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
                } else {
                    ToastHelper.showError(getContext(), "Please correct the weight and/or height values");
                    return;
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

            // Validate patient selection
            Patient patient = patientViewModel.getSelectedPatient().getValue();
            if (patient == null || patient.getPatientId() == 0) {
                if (layoutPatientSelection != null) {
                    layoutPatientSelection.setError("Please select a patient");
                }
                ToastHelper.showWarning(getContext(), "Please select a patient before submitting the screening form");
                return;
            } else {
                if (layoutPatientSelection != null) {
                    layoutPatientSelection.setError(null);
                }
            }

            // Get current logged-in CHW from SharedPreferences
            android.content.SharedPreferences prefs = requireContext().getSharedPreferences("NCDScreenerPrefs", android.content.Context.MODE_PRIVATE);
            int chwId = prefs.getInt("chwId", 0);
            String chwFirstName = prefs.getString("chwFirstName", "");
            String chwLastName = prefs.getString("chwLastName", "");
            
            if (chwId == 0) {
                ToastHelper.showError(getContext(), "Community Health Worker information not found. Please logout and login again");
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

            ToastHelper.showSuccess(getContext(), "Screening completed successfully! Risk Score: " + riskScore);
            Navigation.findNavController(getView()).navigate(R.id.action_screening_form_to_screening_results);
        } catch (NumberFormatException e) {
            ToastHelper.showError(getContext(), "Please enter valid numeric values for all measurements. Check your inputs and try again");
        }
    }

    private void addQuestionnaire(List<Questionnaire> questionnaires, String questionCode, boolean isChecked) {
        Questionnaire q = new Questionnaire();
        q.setQuestionCode(questionCode);
        q.setAnswer(isChecked ? "yes" : "no");
        questionnaires.add(q);
    }
}

