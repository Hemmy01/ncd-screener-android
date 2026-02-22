package com.example.ncdscreener.service;

import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.http.*;

public interface FhirApiService {

    // ========== PATIENT ENDPOINTS ==========
    
    @GET("Patient/{id}")
    Call<JsonObject> getPatientById(@Path("id") String patientId);

    @PUT("Patient/{id}")
    Call<JsonObject> updatePatient(@Path("id") String patientId, @Body JsonObject patient);

    @POST("Patient")
    Call<JsonObject> createPatient(@Body JsonObject patient);

    @DELETE("Patient/{id}")
    Call<Void> deletePatient(@Path("id") String patientId);

    @GET("Patient")
    Call<JsonObject> searchPatients(@Query("identifier") String identifier);

    @GET("Patient")
    Call<JsonObject> getAllPatients(@Query("_count") int count);

    // ========== OBSERVATION ENDPOINTS ==========
    
    @GET("Observation/{id}")
    Call<JsonObject> getObservationById(@Path("id") String observationId);

    @POST("Observation")
    Call<JsonObject> createObservation(@Body JsonObject observation);

    @PUT("Observation/{id}")
    Call<JsonObject> updateObservation(@Path("id") String observationId, @Body JsonObject observation);

    @GET("Observation")
    Call<JsonObject> getObservationsByPatient(@Query("patient") String patientId);

    // ========== CONDITION ENDPOINTS ==========
    
    @GET("Condition/{id}")
    Call<JsonObject> getConditionById(@Path("id") String conditionId);

    @POST("Condition")
    Call<JsonObject> createCondition(@Body JsonObject condition);

    @GET("Condition")
    Call<JsonObject> getConditionsByPatient(@Query("patient") String patientId);

    // ========== QUESTIONNAIRE RESPONSE ENDPOINTS ==========
    
    @POST("QuestionnaireResponse")
    Call<JsonObject> createQuestionnaireResponse(@Body JsonObject questionnaireResponse);

    @GET("QuestionnaireResponse")
    Call<JsonObject> getQuestionnaireResponsesByPatient(@Query("patient") String patientId);

    // ========== SERVICE REQUEST ENDPOINTS ==========
    
    @POST("ServiceRequest")
    Call<JsonObject> createServiceRequest(@Body JsonObject serviceRequest);

    @GET("ServiceRequest")
    Call<JsonObject> getServiceRequestsByPatient(@Query("patient") String patientId);

    // ========== METADATA ENDPOINTS ==========
    
    @GET("metadata")
    Call<JsonObject> getMetadata();

    // ========== BUNDLE OPERATIONS ==========
    
    @POST("")
    Call<JsonObject> executeTransaction(@Body JsonObject bundle);
}
