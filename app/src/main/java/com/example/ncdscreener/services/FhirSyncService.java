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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Background service for syncing local data with FHIR policy
 * Per requirements: Only fetch Patient from FHIR; all other records are stored locally in Room.
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
     * Coordinates background data operations
     */
    private void syncData() {
        executorService.execute(() -> {
            try {
                // Policy: Do NOT push data to FHIR. Only local Room ops here.
                // If needed, implement periodic fetch of patients by ID or criteria.
                Log.d(TAG, "Sync policy: Local data persisted; no remote POST.");
            } catch (Exception e) {
                Log.e(TAG, "Error during sync", e);
            } finally {
                isRunning = false;
                stopSelf();
            }
        });
    }

    // Example helper to demonstrate possible future patient fetch use
    private void fetchPatientIfNeeded(String patientId) {
        // Intentionally left unimplemented; fetching can be triggered on-demand via repositories
        Log.d(TAG, "fetchPatientIfNeeded called for id: " + patientId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}

