package com.example.ncdscreener.services;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.ncdscreener.api.ApiClient;
import com.example.ncdscreener.api.FhirApiService;
import com.example.ncdscreener.database.NCDScreenerDatabase;
import com.example.ncdscreener.database.dao.PatientDao;
import com.example.ncdscreener.database.dao.ScreeningDao;

/**
 * WorkManager worker for periodic FHIR data synchronization
 * Runs in background to sync data even when app is closed
 */
public class FhirSyncWorker extends Worker {
    
    private static final String TAG = "FhirSyncWorker";
    private FhirApiService apiService;
    private PatientDao patientDao;
    private ScreeningDao screeningDao;

    public FhirSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        apiService = ApiClient.getApiService();
        NCDScreenerDatabase database = NCDScreenerDatabase.getDatabase(context);
        patientDao = database.patientDao();
        screeningDao = database.screeningDao();
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Starting periodic sync");
        
        try {
            // Perform sync operations
            syncPendingData();
            
            Log.d(TAG, "Sync completed successfully");
            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Sync failed", e);
            return Result.retry(); // Retry on failure
        }
    }

    /**
     * Syncs all pending data to FHIR server
     */
    private void syncPendingData() {
        // Use FhirSyncService to perform actual sync
        // This worker triggers the sync service
        android.content.Intent syncIntent = new android.content.Intent(getApplicationContext(), com.example.ncdscreener.services.FhirSyncService.class);
        getApplicationContext().startService(syncIntent);
        
        Log.d(TAG, "Pending data sync initiated");
    }
}

