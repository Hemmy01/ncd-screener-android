package com.example.ncdscreener.fragments;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.ncdscreener.R;
import com.example.ncdscreener.model.Patient;
import com.example.ncdscreener.viewmodel.PatientViewModel;
import com.example.ncdscreener.utils.ToastHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RegisterPatientFragment extends Fragment {

    private TextInputLayout layoutNationalId;
    private TextInputLayout layoutFirstName;
    private TextInputLayout layoutLastName;
    private TextInputEditText editNationalId;
    private TextInputEditText editFirstName;
    private TextInputEditText editLastName;
    private TextInputEditText editDateOfBirth;
    private MaterialAutoCompleteTextView editGender;
    private TextInputEditText editPhone;
    private TextInputEditText editAddress;
    private MaterialButton buttonRegister;
    private MaterialButton buttonGetLocation;
    private PatientViewModel viewModel;
    private FusedLocationProviderClient fusedLocationClient;
    private Calendar selectedDate;
    private SimpleDateFormat dateFormat;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        selectedDate = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        return inflater.inflate(R.layout.fragment_register_patient, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        layoutNationalId = view.findViewById(R.id.layout_national_id);
        layoutFirstName = view.findViewById(R.id.layout_first_name);
        layoutLastName = view.findViewById(R.id.layout_last_name);
        editNationalId = view.findViewById(R.id.edit_national_id);
        editFirstName = view.findViewById(R.id.edit_first_name);
        editLastName = view.findViewById(R.id.edit_last_name);
        editDateOfBirth = view.findViewById(R.id.edit_date_of_birth);
        editGender = view.findViewById(R.id.edit_gender);
        editPhone = view.findViewById(R.id.edit_phone);
        editAddress = view.findViewById(R.id.edit_address);
        buttonRegister = view.findViewById(R.id.button_register);
        buttonGetLocation = view.findViewById(R.id.button_get_location);

        // Setup Date of Birth picker
        editDateOfBirth.setOnClickListener(v -> showDatePicker());
        editDateOfBirth.setFocusable(false);
        editDateOfBirth.setClickable(true);

        // Setup Gender dropdown
        String[] genderOptions = {"Male", "Female"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(requireContext(), 
            android.R.layout.simple_dropdown_item_1line, genderOptions);
        editGender.setAdapter(genderAdapter);

        // Setup Location button
        buttonGetLocation.setOnClickListener(v -> requestLocationPermissionAndGetLocation());

        viewModel = new ViewModelProvider(requireActivity()).get(PatientViewModel.class);

        // Setup input validation FIRST before observer can trigger clearForm()
        setupInputValidation();

        // Check if editing existing patient (only if patientId > 0)
        // If no patient selected (patientId = 0 or null), show empty form for new patient
        viewModel.getSelectedPatient().observe(getViewLifecycleOwner(), patient -> {
            if (patient != null && patient.getPatientId() > 0) {
                // Load patient data for editing
                loadPatientData(patient);
                buttonRegister.setText("Update Patient");
            } else {
                // New patient registration - ensure form is clear
                // Don't clear if form already has data (user might be typing)
                if (editNationalId.getText().toString().isEmpty() && 
                    editFirstName.getText().toString().isEmpty() && 
                    editLastName.getText().toString().isEmpty()) {
                    clearForm();
                }
                buttonRegister.setText("Register Patient");
            }
        });

        buttonRegister.setOnClickListener(v -> {
            Patient selectedPatient = viewModel.getSelectedPatient().getValue();
            if (selectedPatient != null && selectedPatient.getPatientId() > 0) {
                updatePatient();
            } else {
                registerPatient();
            }
        });
    }

    private void loadPatientData(Patient patient) {
        editNationalId.setText(String.valueOf(patient.getNationalId()));
        editNationalId.setEnabled(false); // Don't allow changing National ID
        editFirstName.setText(patient.getFirstName());
        editLastName.setText(patient.getLastName());
        if (patient.getDateOfBirth() != null) {
            editDateOfBirth.setText(dateFormat.format(patient.getDateOfBirth()));
            Calendar cal = Calendar.getInstance();
            cal.setTime(patient.getDateOfBirth());
            selectedDate = cal;
        }
        editGender.setText(patient.getGender(), false);
        editPhone.setText(patient.getPhoneNumber());
        editAddress.setText(patient.getAddress());
    }

    private void clearForm() {
        // Clear errors first
        if (layoutNationalId != null) layoutNationalId.setError(null);
        if (layoutFirstName != null) layoutFirstName.setError(null);
        if (layoutLastName != null) layoutLastName.setError(null);
        
        // Temporarily remove text watchers to avoid triggering validation during clear
        // Note: We'll add them back in setupInputValidation which is called after this
        editNationalId.setText("");
        editNationalId.setEnabled(true); // Enable for new patient
        editFirstName.setText("");
        editLastName.setText("");
        editDateOfBirth.setText("");
        editGender.setText("", false);
        editPhone.setText("");
        editAddress.setText("");
        selectedDate = Calendar.getInstance();
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            requireContext(),
            (view, year, month, dayOfMonth) -> {
                selectedDate.set(year, month, dayOfMonth);
                editDateOfBirth.setText(dateFormat.format(selectedDate.getTime()));
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        // Set maximum date to today (can't be born in the future)
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void requestLocationPermissionAndGetLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Request permission
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permission already granted, get location
            getCurrentLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                ToastHelper.showWarningLong(getContext(), "Location permission denied. Please enable location access in device settings to use this feature");
            }
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        buttonGetLocation.setEnabled(false);
        buttonGetLocation.setText("Getting location...");

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), location -> {
                    if (location != null) {
                        getAddressFromLocation(location);
                    } else {
                        ToastHelper.showError(getContext(), "Unable to retrieve your current location. Please check GPS settings and try again");
                        buttonGetLocation.setEnabled(true);
                        buttonGetLocation.setText("Get Current Location");
                    }
                })
                .addOnFailureListener(requireActivity(), e -> {
                    ToastHelper.showError(getContext(), "Error getting location: " + (e.getMessage() != null ? e.getMessage() : "Unknown error occurred"));
                    buttonGetLocation.setEnabled(true);
                    buttonGetLocation.setText("Get Current Location");
                });
    }

    private void getAddressFromLocation(Location location) {
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    1);

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                StringBuilder addressString = new StringBuilder();
                
                // Build address string
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    if (i > 0) addressString.append(", ");
                    addressString.append(address.getAddressLine(i));
                }
                
                editAddress.setText(addressString.toString());
                editAddress.setEnabled(true);
                ToastHelper.showSuccess(getContext(), "Patient location detected and saved successfully");
            } else {
                // Fallback: use coordinates
                String coordinates = String.format(Locale.getDefault(), 
                    "Lat: %.6f, Long: %.6f", location.getLatitude(), location.getLongitude());
                editAddress.setText(coordinates);
                editAddress.setEnabled(true);
                ToastHelper.showInfo(getContext(), "Location coordinates saved (address not available)");
            }
        } catch (IOException e) {
            // Fallback: use coordinates if geocoding fails
            String coordinates = String.format(Locale.getDefault(), 
                "Lat: %.6f, Long: %.6f", location.getLatitude(), location.getLongitude());
            editAddress.setText(coordinates);
            editAddress.setEnabled(true);
            ToastHelper.showInfo(getContext(), "Location coordinates saved (address lookup failed)");
        } finally {
            buttonGetLocation.setEnabled(true);
            buttonGetLocation.setText("Get Current Location");
        }
    }

    /**
     * Setup real-time validation for patient registration form
     */
    private void setupInputValidation() {
        // National ID validation
        editNationalId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateNationalId(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // First Name validation
        editFirstName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateFirstName(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Last Name validation
        editLastName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateLastName(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Validates National ID input
     */
    private void validateNationalId(String value) {
        if (layoutNationalId == null) return; // Layout not initialized yet
        
        if (value == null || value.trim().isEmpty()) {
            layoutNationalId.setError("National ID is required");
            return;
        }

        try {
            long nationalId = Long.parseLong(value);
            if (nationalId <= 0) {
                layoutNationalId.setError("National ID must be a positive number");
            } else {
                layoutNationalId.setError(null);
            }
        } catch (NumberFormatException e) {
            layoutNationalId.setError("National ID must be a valid number");
        }
    }

    /**
     * Validates First Name input
     */
    private void validateFirstName(String value) {
        if (layoutFirstName == null) return; // Layout not initialized yet
        
        if (value == null || value.trim().isEmpty()) {
            layoutFirstName.setError("First Name is required");
        } else if (value.trim().length() < 2) {
            layoutFirstName.setError("First Name must be at least 2 characters");
        } else {
            layoutFirstName.setError(null);
        }
    }

    /**
     * Validates Last Name input
     */
    private void validateLastName(String value) {
        if (layoutLastName == null) return; // Layout not initialized yet
        
        if (value == null || value.trim().isEmpty()) {
            layoutLastName.setError("Last Name is required");
        } else if (value.trim().length() < 2) {
            layoutLastName.setError("Last Name must be at least 2 characters");
        } else {
            layoutLastName.setError(null);
        }
    }

    private void registerPatient() {
        // Validate inputs with visual feedback
        String nationalIdStr = editNationalId.getText().toString().trim();
        String firstNameStr = editFirstName.getText().toString().trim();
        String lastNameStr = editLastName.getText().toString().trim();
        
        // Trigger validation to show errors
        validateNationalId(nationalIdStr);
        validateFirstName(firstNameStr);
        validateLastName(lastNameStr);
        
        // Check if there are any errors (with null checks)
        if ((layoutNationalId != null && layoutNationalId.getError() != null) || 
            (layoutFirstName != null && layoutFirstName.getError() != null) || 
            (layoutLastName != null && layoutLastName.getError() != null)) {
            ToastHelper.showWarning(getContext(), "Please correct the errors in the form");
            return;
        }

        try {
            int nationalId = Integer.parseInt(nationalIdStr);
            
            // Check if patient already exists asynchronously
            viewModel.getPatientByNationalIdAsync(nationalId, existingPatient -> {
                // This callback runs on a background thread, so we need to post to main thread for UI updates
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (existingPatient != null) {
                            if (layoutNationalId != null) {
                                layoutNationalId.setError("A patient with this National ID already exists");
                            }
                            ToastHelper.showError(getContext(), "A patient with this National ID already exists in the system. Please use a different ID");
                            return;
                        }
                        
                        // Patient doesn't exist, proceed with registration
                        registerPatientAfterCheck(nationalId);
                    });
                }
            });
        } catch (NumberFormatException e) {
            ToastHelper.showError(getContext(), "Invalid National ID format. Please enter a valid numeric ID");
        }
    }
    
    /**
     * Registers a new patient after checking that they don't already exist
     */
    private void registerPatientAfterCheck(int nationalId) {
        // Create new patient
        Patient patient = new Patient();
        // Patient ID will be auto-generated by database
        patient.setPatientId(0);
        patient.setNationalId(nationalId);
        patient.setFirstName(editFirstName.getText().toString().trim());
        patient.setLastName(editLastName.getText().toString().trim());
        
        // Parse date of birth from selected date
        if (selectedDate != null) {
            patient.setDateOfBirth(new Date(selectedDate.getTimeInMillis()));
        } else {
            // If no date selected, use current date as default
            patient.setDateOfBirth(new Date());
        }
        
        patient.setGender(editGender.getText().toString().trim());
        patient.setPhoneNumber(editPhone.getText().toString().trim());
        patient.setAddress(editAddress.getText().toString().trim());

        // Save patient synchronously to ensure it's saved before navigating
        // This runs on a background thread to avoid blocking UI
        new Thread(() -> {
            Patient savedPatient = viewModel.savePatientSync(patient);
            
            // Post back to main thread for UI operations
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    ToastHelper.showSuccess(getContext(), "New patient has been registered successfully");
                    
                    // Select the saved patient
                    if (savedPatient != null && savedPatient.getPatientId() > 0) {
                        viewModel.selectPatient(savedPatient.getPatientId());
                    }
                    
                    // Navigate back to patient list
                    if (getView() != null && isAdded()) {
                        Navigation.findNavController(getView()).navigateUp();
                    }
                });
            }
        }).start();
    }

    private void updatePatient() {
        // Validate inputs with visual feedback
        String firstNameStr = editFirstName.getText().toString().trim();
        String lastNameStr = editLastName.getText().toString().trim();
        
        // Trigger validation to show errors
        validateFirstName(firstNameStr);
        validateLastName(lastNameStr);
        
        // Check if there are any errors (with null checks)
        if ((layoutFirstName != null && layoutFirstName.getError() != null) || 
            (layoutLastName != null && layoutLastName.getError() != null)) {
            ToastHelper.showWarning(getContext(), "Please correct the errors in the form");
            return;
        }

        try {
            Patient selectedPatient = viewModel.getSelectedPatient().getValue();
            if (selectedPatient == null) {
                ToastHelper.showError(getContext(), "No patient selected for update. Please select a patient first");
                return;
            }

            // Update patient data
            selectedPatient.setFirstName(editFirstName.getText().toString().trim());
            selectedPatient.setLastName(editLastName.getText().toString().trim());
            
            // Parse date of birth from selected date
            if (selectedDate != null) {
                selectedPatient.setDateOfBirth(new Date(selectedDate.getTimeInMillis()));
            } else if (selectedPatient.getDateOfBirth() == null) {
                // If no date selected and no existing date, use current date
                selectedPatient.setDateOfBirth(new Date());
            }
            // If selectedDate is null but patient already has a date, keep the existing date
            
            selectedPatient.setGender(editGender.getText().toString().trim());
            selectedPatient.setPhoneNumber(editPhone.getText().toString().trim());
            selectedPatient.setAddress(editAddress.getText().toString().trim());

            // Update patient
            viewModel.updatePatient(selectedPatient);
            
            ToastHelper.showSuccess(getContext(), "Patient information has been updated successfully");
            // Navigate to patient detail
            Navigation.findNavController(getView()).navigate(R.id.action_register_patient_to_patient_detail);
        } catch (Exception e) {
            ToastHelper.showError(getContext(), "Failed to update patient information. Please try again");
        }
    }
}

