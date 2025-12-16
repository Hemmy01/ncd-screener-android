package rw.ac.ncdscreener.services;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import rw.ac.ncdscreener.api.ApiClient;
import rw.ac.ncdscreener.api.FhirApiService;
import rw.ac.ncdscreener.database.NCDScreenerDatabase;
import rw.ac.ncdscreener.database.dao.PatientDao;
import rw.ac.ncdscreener.database.dao.ScreeningDao;

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
        android.content.Intent syncIntent = new android.content.Intent(getApplicationContext(), FhirSyncService.class);
        getApplicationContext().startService(syncIntent);

        Log.d(TAG, "Pending data sync initiated");
    }
}