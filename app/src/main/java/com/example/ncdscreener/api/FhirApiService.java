package com.example.ncdscreener.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * FHIR API Service interface for Retrofit
 * Defines endpoints for FHIR R4 resources
 */
public interface FhirApiService {
    
    String BASE_URL = "https://fhirserver.hl7fundamentals.org/fhir/";
    
    // Patient endpoints (fetch only)
    @GET("Patient/{patientId}")
    Call<Object> getPatient(@Path("patientId") String patientId);
}

