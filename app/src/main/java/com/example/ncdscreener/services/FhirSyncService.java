package com.example.ncdscreener.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.ncdscreener.api.ApiClient;
import com.example.ncdscreener.api.FhirApiService;
import com.example.ncdscreener.database.NCDScreenerDatabase;
import com.example.ncdscreener.database.dao.ConditionDao;
import com.example.ncdscreener.database.dao.ObservationDao;
import com.example.ncdscreener.database.dao.PatientDao;
import com.example.ncdscreener.database.dao.QuestionnaireDao;
import com.example.ncdscreener.database.dao.ScreeningDao;
import com.example.ncdscreener.database.dao.ServiceRequestDao;
import com.example.ncdscreener.database.entity.ConditionEntity;
import com.example.ncdscreener.database.entity.ObservationEntity;
import com.example.ncdscreener.database.entity.PatientEntity;
import com.example.ncdscreener.database.entity.QuestionnaireEntity;
import com.example.ncdscreener.database.entity.ScreeningEntity;
import com.example.ncdscreener.database.entity.ServiceRequestEntity;
import com.example.ncdscreener.model.Condition;
import com.example.ncdscreener.model.Observation;
import com.example.ncdscreener.model.Patient;
import com.example.ncdscreener.model.Questionnaire;
import com.example.ncdscreener.model.ServiceRequest;
import com.example.ncdscreener.utils.EntityConverter;
import com.example.ncdscreener.utils.FhirResourceConverter;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Background service for syncing local data to FHIR server
 * Handles offline data synchronization when network is available
 */
public class FhirSyncService extends Service {
    
    private static final String TAG = "FhirSyncService";
    private FhirApiService apiService;
    private PatientDao patientDao;
    private ScreeningDao screeningDao;
    private ObservationDao observationDao;
    private ConditionDao conditionDao;
    private QuestionnaireDao questionnaireDao;
    private ServiceRequestDao serviceRequestDao;
    private ExecutorService executorService;
    private boolean isRunning = false;

    @Override
    public void onCreate() {
        super.onCreate();
        apiService = ApiClient.getApiService();
        NCDScreenerDatabase database = NCDScreenerDatabase.getDatabase(getApplicationContext());
        patientDao = database.patientDao();
        screeningDao = database.screeningDao();
        observationDao = database.observationDao();
        conditionDao = database.conditionDao();
        questionnaireDao = database.questionnaireDao();
        serviceRequestDao = database.serviceRequestDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isRunning) {
            isRunning = true;
            syncData();
        }
        return START_STICKY; // Restart service if killed
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Syncs all unsynced data to FHIR server
     */
    private void syncData() {
        executorService.execute(() -> {
            try {
                // Sync patients
                syncPatients();
                
                // Sync screenings
                syncScreenings();
                
                Log.d(TAG, "Data sync completed");
            } catch (Exception e) {
                Log.e(TAG, "Error during sync", e);
            } finally {
                isRunning = false;
                stopSelf();
            }
        });
    }

    /**
     * Syncs patient data to FHIR server
     */
    private void syncPatients() {
        // Get all patients that need syncing
        // In a real implementation, you'd mark patients as synced/unsynced
        List<PatientEntity> patients = patientDao.getAllPatientsSync();
        
        if (patients != null && !patients.isEmpty()) {
            for (PatientEntity patient : patients) {
                // Convert to FHIR resource and send
                Object fhirPatient = convertPatientToFhir(patient);
                
                if (fhirPatient != null) {
                    apiService.createPatient(fhirPatient).enqueue(new Callback<Object>() {
                        @Override
                        public void onResponse(Call<Object> call, Response<Object> response) {
                            if (response.isSuccessful()) {
                                Log.d(TAG, "Patient synced successfully: " + patient.getPatientId());
                                // Mark as synced in database
                            } else {
                                Log.e(TAG, "Failed to sync patient: " + response.message());
                            }
                        }

                        @Override
                        public void onFailure(Call<Object> call, Throwable t) {
                            Log.e(TAG, "Error syncing patient", t);
                        }
                    });
                }
            }
        }
    }

    /**
     * Syncs screening data to FHIR server
     */
    private void syncScreenings() {
        // Get all screenings that need syncing
        List<ScreeningEntity> screenings = screeningDao.getAllScreeningsSync();
        
        if (screenings != null && !screenings.isEmpty()) {
            for (ScreeningEntity screening : screenings) {
                // Convert screening components to FHIR resources
                syncScreeningComponents(screening);
            }
        }
    }

    /**
     * Syncs individual screening components (observations, conditions, etc.)
     */
    private void syncScreeningComponents(ScreeningEntity screening) {
        int screeningId = screening.getScreeningId();
        int patientId = screening.getPatientId();
        
        // Get patient for reference
        PatientEntity patientEntity = patientDao.getPatientByIdSync(patientId);
        if (patientEntity == null) {
            Log.e(TAG, "Patient not found for screening: " + screeningId);
            return;
        }
        
        String patientReference = "Patient/" + patientId;
        
        // Sync Observations
        List<ObservationEntity> observationEntities = observationDao.getObservationsByScreeningId(screeningId);
        if (observationEntities != null && !observationEntities.isEmpty()) {
            for (ObservationEntity obsEntity : observationEntities) {
                Observation observation = EntityConverter.fromEntity(obsEntity);
                JSONObject fhirObservation = FhirResourceConverter.convertObservationToFhir(observation, patientReference);
                
                if (fhirObservation != null) {
                    apiService.createObservation(fhirObservation).enqueue(new Callback<Object>() {
                        @Override
                        public void onResponse(Call<Object> call, Response<Object> response) {
                            if (response.isSuccessful()) {
                                Log.d(TAG, "Observation synced: " + obsEntity.getObservationId());
                            } else {
                                Log.e(TAG, "Failed to sync observation: " + response.message());
                            }
                        }

                        @Override
                        public void onFailure(Call<Object> call, Throwable t) {
                            Log.e(TAG, "Error syncing observation", t);
                        }
                    });
                }
            }
        }
        
        // Sync Conditions
        List<ConditionEntity> conditionEntities = conditionDao.getConditionsByScreeningId(screeningId);
        if (conditionEntities != null && !conditionEntities.isEmpty()) {
            for (ConditionEntity condEntity : conditionEntities) {
                Condition condition = EntityConverter.fromEntity(condEntity);
                JSONObject fhirCondition = FhirResourceConverter.convertConditionToFhir(condition, patientReference);
                
                if (fhirCondition != null) {
                    apiService.createCondition(fhirCondition).enqueue(new Callback<Object>() {
                        @Override
                        public void onResponse(Call<Object> call, Response<Object> response) {
                            if (response.isSuccessful()) {
                                Log.d(TAG, "Condition synced: " + condEntity.getConditionId());
                            } else {
                                Log.e(TAG, "Failed to sync condition: " + response.message());
                            }
                        }

                        @Override
                        public void onFailure(Call<Object> call, Throwable t) {
                            Log.e(TAG, "Error syncing condition", t);
                        }
                    });
                }
            }
        }
        
        // Sync Questionnaires
        List<QuestionnaireEntity> questionnaireEntities = questionnaireDao.getQuestionnairesByScreeningId(screeningId);
        if (questionnaireEntities != null && !questionnaireEntities.isEmpty()) {
            List<Questionnaire> questionnaires = new ArrayList<>();
            for (QuestionnaireEntity qEntity : questionnaireEntities) {
                questionnaires.add(EntityConverter.fromEntity(qEntity));
            }
            
            JSONObject fhirQuestionnaire = FhirResourceConverter.convertQuestionnaireToFhir(questionnaires, patientReference);
            if (fhirQuestionnaire != null) {
                apiService.createQuestionnaireResponse(fhirQuestionnaire).enqueue(new Callback<Object>() {
                    @Override
                    public void onResponse(Call<Object> call, Response<Object> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "QuestionnaireResponse synced for screening: " + screeningId);
                        } else {
                            Log.e(TAG, "Failed to sync QuestionnaireResponse: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<Object> call, Throwable t) {
                        Log.e(TAG, "Error syncing QuestionnaireResponse", t);
                    }
                });
            }
        }
        
        // Sync Service Requests
        List<ServiceRequestEntity> serviceRequestEntities = serviceRequestDao.getServiceRequestsByScreeningId(screeningId);
        if (serviceRequestEntities != null && !serviceRequestEntities.isEmpty()) {
            for (ServiceRequestEntity srEntity : serviceRequestEntities) {
                ServiceRequest serviceRequest = EntityConverter.fromEntity(srEntity);
                JSONObject fhirServiceRequest = FhirResourceConverter.convertServiceRequestToFhir(serviceRequest, patientReference);
                
                if (fhirServiceRequest != null) {
                    apiService.createServiceRequest(fhirServiceRequest).enqueue(new Callback<Object>() {
                        @Override
                        public void onResponse(Call<Object> call, Response<Object> response) {
                            if (response.isSuccessful()) {
                                Log.d(TAG, "ServiceRequest synced: " + srEntity.getServiceRequestId());
                            } else {
                                Log.e(TAG, "Failed to sync ServiceRequest: " + response.message());
                            }
                        }

                        @Override
                        public void onFailure(Call<Object> call, Throwable t) {
                            Log.e(TAG, "Error syncing ServiceRequest", t);
                        }
                    });
                }
            }
        }
        
        Log.d(TAG, "Completed syncing screening components for screening: " + screeningId);
    }

    /**
     * Converts PatientEntity to FHIR Patient resource
     */
    private Object convertPatientToFhir(PatientEntity patientEntity) {
        Patient patient = EntityConverter.fromEntity(patientEntity);
        JSONObject fhirPatient = FhirResourceConverter.convertPatientToFhir(patient);
        return fhirPatient != null ? fhirPatient.toString() : new Object();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}

