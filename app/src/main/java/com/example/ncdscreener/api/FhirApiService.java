package com.example.ncdscreener.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * FHIR API Service interface for Retrofit
 * Defines endpoints for FHIR R4 resources
 */
public interface FhirApiService {
    
    String BASE_URL = "http://hapi.fhir.org/baseR4/";
    
    // Patient endpoints
    @GET("Patient/{patientId}")
    Call<Object> getPatient(@Path("patientId") String patientId);
    
    @POST("Patient")
    Call<Object> createPatient(@Body Object patientResource);
    
    // Observation endpoints
    @POST("Observation")
    Call<Object> createObservation(@Body Object observationResource);
    
    // QuestionnaireResponse endpoints
    @POST("QuestionnaireResponse")
    Call<Object> createQuestionnaireResponse(@Body Object questionnaireResponseResource);
    
    // Condition endpoints
    @POST("Condition")
    Call<Object> createCondition(@Body Object conditionResource);
    
    // ServiceRequest endpoints
    @POST("ServiceRequest")
    Call<Object> createServiceRequest(@Body Object serviceRequestResource);
}

