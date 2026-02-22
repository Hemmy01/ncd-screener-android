# FHIR Quick Start Guide - NCD Screener

## 🚀 Getting Started in 5 Minutes

### Step 1: Initialize FHIR Service
```java
// In your Activity or Fragment
FhirDataService fhirService = new FhirDataService(getContext());
```

### Step 2: Create a Patient
```java
Patient patient = new Patient();
patient.setFirstName("Jane");
patient.setLastName("Smith");
patient.setGender("female");
patient.setDateOfBirth(new Date());
patient.setNationalId(987654321);
patient.setPhoneNumber("+250788999888");
patient.setAddress("Kigali, Gasabo District");

fhirService.createPatient(patient, new FhirDataService.PatientCallback() {
    @Override
    public void onSuccess(Patient savedPatient) {
        // Patient created successfully
        String fhirId = savedPatient.getFhirId();
        Toast.makeText(context, "Patient saved: " + fhirId, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(String errorMessage) {
        // Handle error
        Toast.makeText(context, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
    }
});
```

### Step 3: Create Observations
```java
// Blood Pressure Systolic
Observation bpSystolic = new Observation();
bpSystolic.setObservationType("blood_pressure_systolic");
bpSystolic.setValue(140);
bpSystolic.setUnit("mmHg");

fhirService.createObservation(bpSystolic, patientFhirId, 
    new FhirDataService.ObservationCallback() {
        @Override
        public void onSuccess(Observation savedObservation) {
            Log.d("FHIR", "BP Systolic saved");
        }

        @Override
        public void onError(String errorMessage) {
            Log.e("FHIR", "Error: " + errorMessage);
        }
    });

// Blood Pressure Diastolic
Observation bpDiastolic = new Observation();
bpDiastolic.setObservationType("blood_pressure_diastolic");
bpDiastolic.setValue(90);
bpDiastolic.setUnit("mmHg");

fhirService.createObservation(bpDiastolic, patientFhirId, 
    new FhirDataService.ObservationCallback() {
        @Override
        public void onSuccess(Observation savedObservation) {
            Log.d("FHIR", "BP Diastolic saved");
        }

        @Override
        public void onError(String errorMessage) {
            Log.e("FHIR", "Error: " + errorMessage);
        }
    });

// Glucose
Observation glucose = new Observation();
glucose.setObservationType("glucose");
glucose.setValue(120);
glucose.setUnit("mg/dL");

fhirService.createObservation(glucose, patientFhirId, 
    new FhirDataService.ObservationCallback() {
        @Override
        public void onSuccess(Observation savedObservation) {
            Log.d("FHIR", "Glucose saved");
        }

        @Override
        public void onError(String errorMessage) {
            Log.e("FHIR", "Error: " + errorMessage);
        }
    });
```

### Step 4: Fetch All Patients
```java
fhirService.getAllPatients(new FhirDataService.PatientListCallback() {
    @Override
    public void onSuccess(List<Patient> patients) {
        // Update UI with patient list
        for (Patient p : patients) {
            Log.d("FHIR", "Patient: " + p.getFullName() + " (ID: " + p.getFhirId() + ")");
        }
        // Update RecyclerView adapter
        patientAdapter.setPatients(patients);
    }

    @Override
    public void onError(String errorMessage) {
        Toast.makeText(context, "Error loading patients: " + errorMessage, 
                      Toast.LENGTH_LONG).show();
    }
});
```

## 🔄 Offline Support

### Enable Offline-First Mode
```java
// Initialize sync service
FhirSyncService syncService = new FhirSyncService(getContext());

// Save data offline (will sync automatically when online)
JsonObject patientJson = fhirService.convertPatientToFhir(patient);
String localId = syncService.saveToOutbox("Patient", "CREATE", patientJson, 
    new FhirSyncService.SaveCallback() {
        @Override
        public void onSuccess(String localId) {
            Log.d("FHIR", "Saved offline with local ID: " + localId);
        }

        @Override
        public void onError(String error) {
            Log.e("FHIR", "Error saving offline: " + error);
        }
    });
```

### Manual Sync Trigger
```java
// Trigger sync manually (e.g., on button click or network reconnect)
syncService.syncOutbox();
```

### Check Sync Status
```java
FhirSyncService.SyncStatus status = syncService.getSyncStatus();
if (status.pendingCount > 0) {
    Toast.makeText(context, 
        status.pendingCount + " items pending sync", 
        Toast.LENGTH_SHORT).show();
}
```

## 🏥 Observation Types

### Supported Observation Types
```java
// Blood Pressure
"blood_pressure_systolic"   // LOINC: 8480-6, Unit: mmHg
"blood_pressure_diastolic"  // LOINC: 8462-4, Unit: mmHg

// Glucose
"glucose"                   // LOINC: 2339-0, Unit: mg/dL

// Body Measurements
"bmi"                       // LOINC: 39156-5, Unit: kg/m²
"weight"                    // LOINC: 29463-7, Unit: kg
"height"                    // LOINC: 8302-2, Unit: cm
```

## 🔍 Server Health Check

### Check Server Before Operations
```java
// Check if FHIR server is available
FhirServerHealthChecker.ServerHealth health = 
    FhirServerHealthChecker.findHealthyServer();

if (health != null && health.isHealthy) {
    Log.d("FHIR", "Server is healthy: " + health.url);
    Log.d("FHIR", "Response time: " + health.responseTimeMs + "ms");
    // Proceed with FHIR operations
} else {
    Log.w("FHIR", "No healthy server found - using offline mode");
    // Use offline mode
}
```

### Check All Servers
```java
List<FhirServerHealthChecker.ServerHealth> healthyServers = 
    FhirServerHealthChecker.checkAllServers();

for (FhirServerHealthChecker.ServerHealth server : healthyServers) {
    Log.d("FHIR", "Healthy server: " + server.url + 
          " (Response: " + server.responseTimeMs + "ms)");
}
```

## 📱 Integration with Existing Code

### In RegisterPatientFragment
```java
// After collecting patient data from form
private void savePatient() {
    Patient patient = new Patient();
    patient.setFirstName(etFirstName.getText().toString());
    patient.setLastName(etLastName.getText().toString());
    patient.setGender(spinnerGender.getSelectedItem().toString());
    // ... set other fields
    
    // Save to FHIR
    FhirDataService fhirService = new FhirDataService(requireContext());
    fhirService.createPatient(patient, new FhirDataService.PatientCallback() {
        @Override
        public void onSuccess(Patient savedPatient) {
            Toast.makeText(requireContext(), 
                "Patient registered successfully", 
                Toast.LENGTH_SHORT).show();
            // Navigate to next screen
            navigateToScreeningForm(savedPatient);
        }

        @Override
        public void onError(String errorMessage) {
            Toast.makeText(requireContext(), 
                "Error: " + errorMessage, 
                Toast.LENGTH_LONG).show();
        }
    });
}
```

### In ScreeningFormFragment
```java
// After collecting vital signs
private void saveVitalSigns(String patientFhirId) {
    FhirDataService fhirService = new FhirDataService(requireContext());
    
    // Save blood pressure
    Observation bpSystolic = new Observation();
    bpSystolic.setObservationType("blood_pressure_systolic");
    bpSystolic.setValue(Double.parseDouble(etBPSystolic.getText().toString()));
    bpSystolic.setUnit("mmHg");
    
    fhirService.createObservation(bpSystolic, patientFhirId, 
        new FhirDataService.ObservationCallback() {
            @Override
            public void onSuccess(Observation savedObservation) {
                Log.d("FHIR", "Vital signs saved");
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("FHIR", "Error: " + errorMessage);
            }
        });
    
    // Save other observations...
}
```

### In PatientListFragment
```java
@Override
public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    
    // Load patients from FHIR
    loadPatients();
}

private void loadPatients() {
    FhirDataService fhirService = new FhirDataService(requireContext());
    
    // Show loading indicator
    progressBar.setVisibility(View.VISIBLE);
    
    fhirService.getAllPatients(new FhirDataService.PatientListCallback() {
        @Override
        public void onSuccess(List<Patient> patients) {
            progressBar.setVisibility(View.GONE);
            patientAdapter.setPatients(patients);
        }

        @Override
        public void onError(String errorMessage) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(requireContext(), 
                "Error loading patients: " + errorMessage, 
                Toast.LENGTH_LONG).show();
        }
    });
}
```

## 🎯 Best Practices

### 1. Always Handle Callbacks
```java
// ✅ Good
fhirService.createPatient(patient, new FhirDataService.PatientCallback() {
    @Override
    public void onSuccess(Patient savedPatient) {
        // Update UI
    }

    @Override
    public void onError(String errorMessage) {
        // Show error to user
    }
});

// ❌ Bad - No error handling
fhirService.createPatient(patient, null);
```

### 2. Store FHIR IDs
```java
// ✅ Good - Store FHIR ID for future operations
patient.setFhirId(savedPatient.getFhirId());
saveToLocalDatabase(patient);

// ❌ Bad - Losing FHIR ID
// Not storing the FHIR ID means you can't update or query this patient later
```

### 3. Use Offline Mode for Critical Operations
```java
// ✅ Good - Save offline first, sync later
FhirSyncService syncService = new FhirSyncService(context);
syncService.saveToOutbox("Patient", "CREATE", patientJson);

// ❌ Bad - Direct API call without offline fallback
// If network fails, data is lost
```

### 4. Check Network Before Sync
```java
// ✅ Good
if (isNetworkAvailable()) {
    syncService.syncOutbox();
} else {
    Toast.makeText(context, "Offline - will sync when online", 
                  Toast.LENGTH_SHORT).show();
}
```

## 🐛 Common Issues & Solutions

### Issue: "No healthy server found"
**Solution**: Check internet connection, verify server URLs

### Issue: "Failed to create patient (Error: 400)"
**Solution**: Validate all required fields are set before creating patient

### Issue: "Pending items not syncing"
**Solution**: Call `syncService.syncOutbox()` manually or check logs

### Issue: "FHIR ID is null after creation"
**Solution**: Ensure you're using the patient object from the callback

## 📊 Monitoring & Debugging

### Enable Detailed Logging
```java
// Add to your Application class or MainActivity
if (BuildConfig.DEBUG) {
    // FHIR operations will log to Logcat with tag "FhirDataService"
    // Sync operations will log with tag "FhirSyncService"
}
```

### View Logs
```bash
# Filter FHIR logs
adb logcat | grep "Fhir"

# View sync status
adb logcat | grep "FhirSyncService"
```

## 🎓 Learning Resources

- **FHIR_INTEGRATION.md** - Complete documentation
- **INTEGRATION_SUMMARY.md** - Integration overview
- [FHIR R4 Specification](https://hl7.org/fhir/R4/)
- [HAPI FHIR Docs](https://hapifhir.io/)

---

**Need Help?** Check the logs, review the documentation, or test with the FHIR server's web interface.
