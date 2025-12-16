package com.example.ncdscreener.services;

import android.content.Context;
import android.content.Intent;

import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

/**
 * Manager class for handling data synchronization
 * Coordinates between Service and WorkManager
 */
public class SyncManager {
    
    private static final String SYNC_WORK_TAG = "fhir_sync_work";
    private Context context;

    public SyncManager(Context context) {
        this.context = context;
    }

    /**
     * Starts the sync service for immediate sync
     */
    public void startSyncService() {
        Intent intent = new Intent(context, FhirSyncService.class);
        context.startService(intent);
    }

    /**
     * Schedules periodic sync using WorkManager
     */
    public void schedulePeriodicSync() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build();

        PeriodicWorkRequest syncWork = new PeriodicWorkRequest.Builder(
                FhirSyncWorker.class,
                15, // Repeat every 15 minutes
                TimeUnit.MINUTES
        )
                .setConstraints(constraints)
                .addTag(SYNC_WORK_TAG)
                .build();

        WorkManager.getInstance(context).enqueue(syncWork);
    }

    /**
     * Cancels periodic sync
     */
    public void cancelPeriodicSync() {
        WorkManager.getInstance(context).cancelAllWorkByTag(SYNC_WORK_TAG);
    }

    /**
     * Triggers immediate sync
     */
    public void triggerSync() {
        startSyncService();
    }
}

