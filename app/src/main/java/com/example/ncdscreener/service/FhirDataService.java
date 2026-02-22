package com.example.ncdscreener.service;

import android.content.Context;
import android.util.Log;
import com.example.ncdscreener.model.Patient;
import com.example.ncdscreener.model.Observation;
import com.example.ncdscreener.util.RetrofitClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FhirDataService {
    private static final String TAG = "FhirDataService";
    private final FhirApiService apiService;
    private final Context context;

    public FhirDataService(Context context) {
        this.context = context;
        this.apiService = RetrofitClient.getClient().create(FhirApiService.class);
    }

    // ========== PATIENT METHODS ==========

    public void createPatient(Patient patient, PatientCallback callback) {
        Log.d(TAG, "Creating patient in FHIR: " + patient.getFullName());
        
        JsonObject fhirPatient = convertPatientToFhir(patient);
        
        apiService.createPatient(fhirPatient).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Successfully created patient in FHIR");
                    if (response.body().has("id")) {
                        String fhirId = response.body().get("id").getAsString();
                        patient.setPatientId(Integer.parseInt(fhirId.replaceAll("[^0-9]", "")));
                    }
                    callback.onSuccess(patient);
                } else {
                    Log.e(TAG, "Failed to create patient: " + response.code());
                    callback.onError("Failed to create patient (Error: " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e(TAG, "Network error creating patient", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void getAllPatients(PatientListCallback callback) {
        Log.d(TAG, "Fetching all patients from FHIR");
        
        apiService.getAllPatients(1000).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Successfully fetched patients from FHIR");
                    try {
                        List<Patient> patients = parsePatientBundle(response.body());
                        callback.onSuccess(patients);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing patient data", e);
                        callback.onError("Failed to parse patient data: " + e.getMessage());
                    }
                } else {
                    Log.e(TAG, "Failed to fetch patients: " + response.code());
                    callback.onError("Failed to fetch patients (Error: " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e(TAG, "Network error fetching patients", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    // ========== OBSERVATION METHODS ==========

    public void createObservation(Observation observation, String patientFhirId, ObservationCallback callback) {
        Log.d(TAG, "Creating observation in FHIR");
        
        JsonObject fhirObservation = convertObservationToFhir(observation, patientFhirId);
        
        apiService.createObservation(fhirObservation).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Successfully created observation in FHIR");
                    if (response.body().has("id")) {
                        String fhirId = response.body().get("id").getAsString();
                        observation.setObservationId(Integer.parseInt(fhirId.replaceAll("[^0-9]", "")));
                    }
                    callback.onSuccess(observation);
                } else {
                    Log.e(TAG, "Failed to create observation: " + response.code());
                    callback.onError("Failed to create observation (Error: " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e(TAG, "Network error creating observation", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    // ========== CONVERSION METHODS ==========

    private JsonObject convertPatientToFhir(Patient patient) {
        JsonObject fhirPatient = new JsonObject();
        fhirPatient.addProperty("resourceType", "Patient");
        fhirPatient.addProperty("active", true);

        // Name
        JsonArray nameArray = new JsonArray();
        JsonObject name = new JsonObject();
        name.addProperty("use", "official");
        name.addProperty("family", patient.getLastName());
        JsonArray givenArray = new JsonArray();
        givenArray.add(patient.getFirstName());
        name.add("given", givenArray);
        nameArray.add(name);
        fhirPatient.add("name", nameArray);

        // Gender
        if (patient.getGender() != null) {
            fhirPatient.addProperty("gender", patient.getGender().toLowerCase());
        }

        // Birth date
        if (patient.getDateOfBirth() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            fhirPatient.addProperty("birthDate", sdf.format(patient.getDateOfBirth()));
        }

        // Telecom
        JsonArray telecomArray = new JsonArray();
        if (patient.getPhoneNumber() != null && !patient.getPhoneNumber().isEmpty()) {
            JsonObject phone = new JsonObject();
            phone.addProperty("system", "phone");
            phone.addProperty("value", patient.getPhoneNumber());
            phone.addProperty("use", "mobile");
            telecomArray.add(phone);
        }
        fhirPatient.add("telecom", telecomArray);

        // Address
        if (patient.getAddress() != null && !patient.getAddress().isEmpty()) {
            JsonArray addressArray = new JsonArray();
            JsonObject address = new JsonObject();
            address.addProperty("use", "home");
            address.addProperty("text", patient.getAddress());
            addressArray.add(address);
            fhirPatient.add("address", addressArray);
        }

        // Identifier (National ID)
        JsonArray identifierArray = new JsonArray();
        JsonObject identifier = new JsonObject();
        identifier.addProperty("system", "http://rwanda.gov.rw/national-id");
        identifier.addProperty("value", String.valueOf(patient.getNationalId()));
        identifierArray.add(identifier);
        fhirPatient.add("identifier", identifierArray);

        return fhirPatient;
    }

    private JsonObject convertObservationToFhir(Observation observation, String patientFhirId) {
        JsonObject fhirObservation = new JsonObject();
        fhirObservation.addProperty("resourceType", "Observation");
        fhirObservation.addProperty("status", "final");

        // Subject (patient reference)
        JsonObject subject = new JsonObject();
        subject.addProperty("reference", "Patient/" + patientFhirId);
        fhirObservation.add("subject", subject);

        // Code (observation type)
        JsonObject code = new JsonObject();
        JsonObject coding = new JsonObject();
        coding.addProperty("system", "http://loinc.org");
        coding.addProperty("code", getLoincCode(observation.getObservationType()));
        coding.addProperty("display", observation.getObservationType());
        JsonArray codingArray = new JsonArray();
        codingArray.add(coding);
        code.add("coding", codingArray);
        code.addProperty("text", observation.getObservationType());
        fhirObservation.add("code", code);

        // Value
        JsonObject valueQuantity = new JsonObject();
        valueQuantity.addProperty("value", observation.getValue());
        valueQuantity.addProperty("unit", observation.getUnit());
        valueQuantity.addProperty("system", "http://unitsofmeasure.org");
        valueQuantity.addProperty("code", observation.getUnit());
        fhirObservation.add("valueQuantity", valueQuantity);

        // Effective date/time
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US);
        fhirObservation.addProperty("effectiveDateTime", sdf.format(new java.util.Date()));

        return fhirObservation;
    }

    private String getLoincCode(String observationType) {
        switch (observationType.toLowerCase()) {
            case "blood_pressure_systolic": return "8480-6";
            case "blood_pressure_diastolic": return "8462-4";
            case "glucose": return "2339-0";
            case "bmi": return "39156-5";
            case "weight": return "29463-7";
            case "height": return "8302-2";
            default: return "85354-9"; // Generic observation
        }
    }

    private List<Patient> parsePatientBundle(JsonObject bundle) {
        List<Patient> patients = new ArrayList<>();
        
        if (bundle.has("entry")) {
            JsonArray entries = bundle.getAsJsonArray("entry");
            for (int i = 0; i < entries.size(); i++) {
                try {
                    JsonObject entry = entries.get(i).getAsJsonObject();
                    JsonObject resource = entry.getAsJsonObject("resource");
                    Patient patient = parseFhirPatient(resource);
                    if (patient != null) {
                        patients.add(patient);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing patient entry", e);
                }
            }
        }
        
        return patients;
    }

    private Patient parseFhirPatient(JsonObject fhirPatient) {
        Patient patient = new Patient();
        
        try {
            // ID
            if (fhirPatient.has("id")) {
                String fhirId = fhirPatient.get("id").getAsString();
                patient.setPatientId(Integer.parseInt(fhirId.replaceAll("[^0-9]", "")));
            }

            // Name
            if (fhirPatient.has("name")) {
                JsonArray names = fhirPatient.getAsJsonArray("name");
                if (names.size() > 0) {
                    JsonObject name = names.get(0).getAsJsonObject();
                    if (name.has("family")) {
                        patient.setLastName(name.get("family").getAsString());
                    }
                    if (name.has("given")) {
                        JsonArray given = name.getAsJsonArray("given");
                        if (given.size() > 0) {
                            patient.setFirstName(given.get(0).getAsString());
                        }
                    }
                }
            }

            // Gender
            if (fhirPatient.has("gender")) {
                patient.setGender(fhirPatient.get("gender").getAsString());
            }

            // Birth date
            if (fhirPatient.has("birthDate")) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    patient.setDateOfBirth(sdf.parse(fhirPatient.get("birthDate").getAsString()));
                } catch (Exception e) {
                    Log.w(TAG, "Could not parse birth date", e);
                }
            }

            // Telecom
            if (fhirPatient.has("telecom")) {
                JsonArray telecom = fhirPatient.getAsJsonArray("telecom");
                for (int i = 0; i < telecom.size(); i++) {
                    JsonObject contact = telecom.get(i).getAsJsonObject();
                    if (contact.has("system") && "phone".equals(contact.get("system").getAsString())) {
                        if (contact.has("value")) {
                            patient.setPhoneNumber(contact.get("value").getAsString());
                        }
                    }
                }
            }

            // Address
            if (fhirPatient.has("address")) {
                JsonArray addresses = fhirPatient.getAsJsonArray("address");
                if (addresses.size() > 0) {
                    JsonObject address = addresses.get(0).getAsJsonObject();
                    if (address.has("text")) {
                        patient.setAddress(address.get("text").getAsString());
                    }
                }
            }

            // Identifier (National ID)
            if (fhirPatient.has("identifier")) {
                JsonArray identifiers = fhirPatient.getAsJsonArray("identifier");
                for (int i = 0; i < identifiers.size(); i++) {
                    JsonObject identifier = identifiers.get(i).getAsJsonObject();
                    if (identifier.has("system") && identifier.get("system").getAsString().contains("national-id")) {
                        if (identifier.has("value")) {
                            try {
                                patient.setNationalId(Integer.parseInt(identifier.get("value").getAsString()));
                            } catch (NumberFormatException e) {
                                Log.w(TAG, "Could not parse national ID", e);
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Error parsing FHIR patient", e);
            return null;
        }
        
        return patient;
    }

    // ========== CALLBACK INTERFACES ==========

    public interface PatientCallback {
        void onSuccess(Patient patient);
        void onError(String errorMessage);
    }

    public interface PatientListCallback {
        void onSuccess(List<Patient> patients);
        void onError(String errorMessage);
    }

    public interface ObservationCallback {
        void onSuccess(Observation observation);
        void onError(String errorMessage);
    }
}
