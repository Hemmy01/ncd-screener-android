# FHIR Integration for NCD Screener

## Overview
This project now includes full FHIR (Fast Healthcare Interoperability Resources) integration for standardized healthcare data exchange. The integration supports offline-first architecture with automatic synchronization to FHIR servers.

## Features

### 1. **FHIR Resource Support**
- **Patient**: Demographics, contact information, national ID
- **Observation**: Vital signs (blood pressure, glucose, BMI, weight, height)
- **Condition**: NCD diagnoses and risk assessments
- **QuestionnaireResponse**: Screening questionnaire answers
- **ServiceRequest**: Referrals and follow-up requests

### 2. **Offline-First Architecture**
- All data is saved locally first
- Automatic background synchronization when network is available
- Retry mechanism with exponential backoff (up to 8 retries)
- Conflict resolution and version control

### 3. **Multi-Server Support**
- Automatic server health checking
- Fallback to alternative FHIR servers
- Configurable server endpoints

## Architecture

### Service Layer
```
com.example.ncdscreener.service/
├── FhirApiService.java          # Retrofit API interface
├── FhirDataService.java         # High-level FHIR operations
├── FhirSyncService.java         # Offline sync management
└── FhirServerHealthChecker.java # Server availability checking
```

### Database Layer
```
com.example.ncdscreener.database/
├── AppDatabase.java             # Room database
├── entity/
│   ├── FhirOutbox.java         # Sync queue
│   └── FhirResource.java       # Local resource cache
└── dao/
    ├── FhirOutboxDao.java      # Outbox operations
    └── FhirResourceDao.java    # Resource operations
```

### Utility Layer
```
com.example.ncdscreener.util/
└── RetrofitClient.java          # HTTP client configuration
```

## Usage Examples

### 1. Creating a Patient

```java
// Initialize service
FhirDataService fhirService = new FhirDataService(context);

// Create patient
Patient patient = new Patient();
patient.setFirstName("John");
patient.setLastName("Doe");
patient.setGender("male");
patient.setDateOfBirth(new Date());
patient.setNationalId(1234567890);
patient.setPhoneNumber("+250788123456");
patient.setAddress("Kigali, Rwanda");

// Save to FHIR
fhirService.createPatient(patient, new FhirDataService.PatientCallback() {
    @Override
    public void onSuccess(Patient savedPatient) {
        Log.d("FHIR", "Patient created with ID: " + savedPatient.getFhirId());
    }

    @Override
    public void onError(String errorMessage) {
        Log.e("FHIR", "Error: " + errorMessage);
    }
});
```

### 2. Creating an Observation

```java
Observation observation = new Observation();
observation.setObservationType("blood_pressure_systolic");
observation.setValue(140);
observation.setUnit("mmHg");

fhirService.createObservation(observation, patientFhirId, 
    new FhirDataService.ObservationCallback() {
        @Override
        public void onSuccess(Observation savedObservation) {
            Log.d("FHIR", "Observation created");
        }

        @Override
        public void onError(String errorMessage) {
            Log.e("FHIR", "Error: " + errorMessage);
        }
    });
```

### 3. Offline Sync

```java
// Initialize sync service
FhirSyncService syncService = new FhirSyncService(context);

// Save data offline (will sync automatically when online)
JsonObject patientJson = convertPatientToFhir(patient);
String localId = syncService.saveToOutbox("Patient", "CREATE", patientJson);

// Manual sync trigger
syncService.syncOutbox();

// Check sync status
FhirSyncService.SyncStatus status = syncService.getSyncStatus();
Log.d("FHIR", "Pending items: " + status.pendingCount);
```

### 4. Fetching All Patients

```java
fhirService.getAllPatients(new FhirDataService.PatientListCallback() {
    @Override
    public void onSuccess(List<Patient> patients) {
        Log.d("FHIR", "Fetched " + patients.size() + " patients");
        for (Patient p : patients) {
            Log.d("FHIR", "Patient: " + p.getFullName());
        }
    }

    @Override
    public void onError(String errorMessage) {
        Log.e("FHIR", "Error: " + errorMessage);
    }
});
```

## Configuration

### FHIR Server URLs
Edit `RetrofitClient.java` to change the primary FHIR server:

```java
private static final String BASE_URL = "https://fhirserver.hl7fundamentals.org/fhir/";
```

### Fallback Servers
Edit `FhirServerHealthChecker.java` to configure fallback servers:

```java
private static final String[] SERVER_URLS = {
    "https://fhirserver.hl7fundamentals.org/fhir/",
    "https://r4.smarthealthit.org/",
    "https://hapi.fhir.org/baseR4/"
};
```

## FHIR Resource Mapping

### Patient → FHIR Patient
- `firstName`, `lastName` → `name.given`, `name.family`
- `gender` → `gender`
- `dateOfBirth` → `birthDate`
- `phoneNumber` → `telecom[system=phone]`
- `address` → `address.text`
- `nationalId` → `identifier[system=national-id]`

### Observation → FHIR Observation
- `observationType` → `code.coding.code` (LOINC codes)
- `value` → `valueQuantity.value`
- `unit` → `valueQuantity.unit`
- Patient reference → `subject.reference`

### LOINC Code Mapping
- Blood Pressure Systolic: `8480-6`
- Blood Pressure Diastolic: `8462-4`
- Glucose: `2339-0`
- BMI: `39156-5`
- Weight: `29463-7`
- Height: `8302-2`

## Dependencies

### Added to build.gradle.kts
```kotlin
// Retrofit for FHIR API
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

// HAPI FHIR
implementation("ca.uhn.hapi.fhir:hapi-fhir-structures-r4:7.4.0")
implementation("ca.uhn.hapi.fhir:hapi-fhir-base:7.4.0")

// Room Database
implementation("androidx.room:room-runtime:2.6.1")
annotationProcessor("androidx.room:room-compiler:2.6.1")

// Gson
implementation("com.google.code.gson:gson:2.10.1")

// Guava
implementation("com.google.guava:guava:30.1.1-android")
```

## Testing

### Test FHIR Server Connection
```java
FhirServerHealthChecker.ServerHealth health = 
    FhirServerHealthChecker.checkServerHealth(
        "https://fhirserver.hl7fundamentals.org/fhir/"
    );

if (health.isHealthy) {
    Log.d("FHIR", "Server is healthy (Response time: " + health.responseTimeMs + "ms)");
} else {
    Log.e("FHIR", "Server error: " + health.errorMessage);
}
```

### Test Offline Sync
1. Turn off network
2. Create patients/observations
3. Turn on network
4. Call `syncService.syncOutbox()`
5. Verify data appears on FHIR server

## Best Practices

1. **Always use offline-first approach**: Save to local database first, then sync
2. **Handle callbacks properly**: Update UI on success/error
3. **Check sync status**: Monitor pending items before critical operations
4. **Use FHIR IDs**: Store both local IDs and FHIR server IDs
5. **Implement retry logic**: Network failures are common in mobile apps
6. **Validate data**: Ensure required fields are present before FHIR conversion
7. **Log operations**: Use Android Log for debugging FHIR operations

## Troubleshooting

### Common Issues

**Issue**: "No healthy server found"
- **Solution**: Check internet connection, verify server URLs are accessible

**Issue**: "Failed to create patient (Error: 400)"
- **Solution**: Validate patient data, ensure required fields are present

**Issue**: "Pending items not syncing"
- **Solution**: Call `syncService.syncOutbox()` manually, check logs for errors

**Issue**: "Room database errors"
- **Solution**: Clear app data, rebuild project, check entity annotations

## Performance Optimization

1. **Batch operations**: Use FHIR Bundle for multiple resources
2. **Pagination**: Fetch patients in batches using `_count` parameter
3. **Background sync**: Use WorkManager for periodic sync
4. **Cache responses**: Store server responses in FhirResource table
5. **Compress data**: Enable GZIP compression in OkHttp

## Security Considerations

1. **HTTPS only**: All FHIR servers use HTTPS
2. **Authentication**: Add OAuth2 tokens if required by server
3. **Data encryption**: Encrypt sensitive data in Room database
4. **Access control**: Implement user permissions for FHIR operations
5. **Audit logging**: Log all FHIR operations for compliance

## Future Enhancements

- [ ] Add FHIR Practitioner support for CHWs
- [ ] Implement FHIR Encounter for screening sessions
- [ ] Add FHIR CarePlan for treatment plans
- [ ] Support FHIR Subscription for real-time updates
- [ ] Implement FHIR Search with advanced filters
- [ ] Add FHIR Provenance for audit trails
- [ ] Support FHIR Binary for document attachments

## Resources

- [FHIR R4 Specification](https://hl7.org/fhir/R4/)
- [HAPI FHIR Documentation](https://hapifhir.io/)
- [Retrofit Documentation](https://square.github.io/retrofit/)
- [Room Database Guide](https://developer.android.com/training/data-storage/room)

## Support

For issues or questions about FHIR integration:
1. Check logs for detailed error messages
2. Verify FHIR server is accessible
3. Review FHIR resource structure
4. Test with FHIR server's web interface

---

**Last Updated**: 2024
**FHIR Version**: R4
**Android Min SDK**: 24
