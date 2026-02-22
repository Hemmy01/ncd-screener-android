package com.example.ncdscreener.service;

import android.content.Context;
import android.util.Log;
import com.example.ncdscreener.database.AppDatabase;
import com.example.ncdscreener.database.dao.FhirOutboxDao;
import com.example.ncdscreener.database.dao.FhirResourceDao;
import com.example.ncdscreener.database.entity.FhirOutbox;
import com.example.ncdscreener.database.entity.FhirResource;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.OkHttpClient;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class FhirSyncService {
    private static final String TAG = "FhirSyncService";
    private static final int MAX_RETRIES = 8;
    private static final int BATCH_SIZE = 10;
    
    public interface SaveCallback {
        void onSuccess(String localId);
        void onError(String error);
    }
    
    private final Context context;
    private final FhirOutboxDao outboxDao;
    private final FhirResourceDao resourceDao;
    private String currentServerUrl;
    
    public FhirSyncService(Context context) {
        this.context = context;
        AppDatabase db = AppDatabase.getDatabase(context);
        this.outboxDao = db.outboxDao();
        this.resourceDao = db.resourceDao();
    }
    
    public String saveToOutbox(String resourceType, String operation, JsonObject resourceJson) {
        return saveToOutbox(resourceType, operation, resourceJson, null);
    }
    
    public String saveToOutbox(String resourceType, String operation, JsonObject resourceJson, SaveCallback callback) {
        String localId = UUID.randomUUID().toString();
        final String resourceJsonString = resourceJson.toString();
        final String finalLocalId = localId;
        
        new Thread(() -> {
            try {
                long now = System.currentTimeMillis();
                
                FhirOutbox outbox = new FhirOutbox();
                outbox.localId = finalLocalId;
                outbox.resourceType = resourceType;
                outbox.operation = operation;
                outbox.resourceJson = resourceJsonString;
                outbox.status = "PENDING";
                outbox.idempotencyKey = UUID.randomUUID().toString();
                outbox.retryCount = 0;
                outbox.createdAt = now;
                outbox.lastRetryAt = now;
                
                long id = outboxDao.insert(outbox);
                Log.d(TAG, "Saved to outbox: " + resourceType + " " + operation + " (localId: " + finalLocalId + ", id: " + id + ")");
                
                saveToResources(finalLocalId, resourceType, resourceJsonString, null);
                
                if (callback != null) {
                    callback.onSuccess(finalLocalId);
                }
                
                syncOutbox();
                
            } catch (Exception e) {
                Log.e(TAG, "Error saving to outbox", e);
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        }).start();
        
        return localId;
    }
    
    private void saveToResources(String localId, String resourceType, String resourceJson, String serverId) {
        FhirResource resource = resourceDao.getByLocalId(localId);
        long now = System.currentTimeMillis();
        
        if (resource == null) {
            resource = new FhirResource();
            resource.localId = localId;
            resource.resourceType = resourceType;
            resource.resourceJson = resourceJson;
            resource.lastModified = now;
            resource.isDeleted = false;
            if (serverId != null) {
                resource.serverId = serverId;
                resource.lastSynced = now;
            }
            resourceDao.insert(resource);
        } else {
            resource.resourceJson = resourceJson;
            resource.lastModified = now;
            if (serverId != null) {
                resource.serverId = serverId;
                resource.lastSynced = now;
            }
            resourceDao.update(resource);
        }
    }
    
    public void syncOutbox() {
        new Thread(() -> {
            try {
                Log.d(TAG, "=== Starting sync process ===");
                
                int pendingCount = outboxDao.getPendingCount();
                Log.d(TAG, "Pending items in outbox: " + pendingCount);
                
                if (pendingCount == 0) {
                    Log.d(TAG, "No pending items to sync");
                    return;
                }
                
                Log.d(TAG, "Checking server health...");
                FhirServerHealthChecker.ServerHealth health = FhirServerHealthChecker.findHealthyServer();
                if (health == null) {
                    Log.w(TAG, "No healthy server found, skipping sync");
                    return;
                }
                
                currentServerUrl = health.url;
                Log.d(TAG, "Found healthy server: " + currentServerUrl);
                
                processOutbox(currentServerUrl);
                
                Log.d(TAG, "=== Sync process completed ===");
                
            } catch (Exception e) {
                Log.e(TAG, "Error syncing outbox", e);
            }
        }).start();
    }
    
    public SyncStatus getSyncStatus() {
        try {
            int pending = outboxDao.getPendingCount();
            return new SyncStatus(pending);
        } catch (Exception e) {
            Log.e(TAG, "Error getting sync status", e);
            return new SyncStatus(0);
        }
    }
    
    public static class SyncStatus {
        public int pendingCount;
        
        public SyncStatus(int pendingCount) {
            this.pendingCount = pendingCount;
        }
    }
    
    private void processOutbox(String serverUrl) {
        List<FhirOutbox> pendingItems;
        try {
            pendingItems = outboxDao.getPendingItems(BATCH_SIZE);
        } catch (Exception e) {
            Log.e(TAG, "Error getting pending items", e);
            return;
        }
        
        if (pendingItems.isEmpty()) {
            Log.d(TAG, "No pending items to sync");
            return;
        }
        
        Log.d(TAG, "Processing " + pendingItems.size() + " pending items");
        
        Retrofit retrofit = createRetrofitForServer(serverUrl);
        FhirApiService apiService = retrofit.create(FhirApiService.class);
        
        for (FhirOutbox item : pendingItems) {
            try {
                boolean success = processOutboxItem(item, apiService, serverUrl);
                
                if (success) {
                    Log.d(TAG, "Successfully synced item: " + item.localId);
                } else {
                    if (shouldRetry(item)) {
                        long nextRetryTime = calculateNextRetryTime(item.retryCount);
                        item.lastRetryAt = System.currentTimeMillis() + nextRetryTime;
                        outboxDao.incrementRetry(item.id, item.lastRetryAt);
                    } else {
                        outboxDao.markAsFailed(item.id, item.errorMessage, item.httpStatusCode, System.currentTimeMillis());
                    }
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error processing outbox item", e);
            }
        }
    }
    
    private boolean processOutboxItem(FhirOutbox item, FhirApiService apiService, String serverUrl) {
        try {
            Log.d(TAG, "Processing " + item.operation + " for " + item.resourceType);
            
            JsonObject resourceJson = JsonParser.parseString(item.resourceJson).getAsJsonObject();
            Response<JsonObject> response = null;
            
            switch (item.operation) {
                case "CREATE":
                    response = createResource(apiService, item.resourceType, resourceJson);
                    break;
                case "UPDATE":
                    if (item.serverId != null && !item.serverId.isEmpty()) {
                        response = updateResource(apiService, item.resourceType, item.serverId, resourceJson);
                    }
                    break;
                case "DELETE":
                    if (item.serverId != null && !item.serverId.isEmpty()) {
                        response = deleteResource(apiService, item.resourceType, item.serverId);
                    }
                    break;
            }
            
            if (response != null && response.isSuccessful()) {
                Log.d(TAG, "Successfully synced " + item.resourceType);
                
                String serverId = extractServerId(response.body());
                String etag = extractEtag(response);
                
                outboxDao.markAsSent(item.id, "SENT", serverId, serverUrl);
                
                String serverJson = response.body() != null ? response.body().toString() : null;
                resourceDao.updateServerInfo(item.localId, serverId, serverUrl, etag, System.currentTimeMillis(), serverJson);
                
                return true;
            } else {
                int statusCode = response != null ? response.code() : 0;
                String errorMsg = "HTTP " + statusCode;
                item.errorMessage = errorMsg;
                item.httpStatusCode = statusCode;
                return false;
            }
            
        } catch (Exception e) {
            item.errorMessage = e.getMessage();
            Log.e(TAG, "Error processing outbox item", e);
            return false;
        }
    }
    
    private Response<JsonObject> createResource(FhirApiService apiService, String resourceType, JsonObject resource) throws IOException {
        Call<JsonObject> call;
        
        switch (resourceType) {
            case "Patient":
                call = apiService.createPatient(resource);
                break;
            case "Observation":
                call = apiService.createObservation(resource);
                break;
            case "Condition":
                call = apiService.createCondition(resource);
                break;
            case "QuestionnaireResponse":
                call = apiService.createQuestionnaireResponse(resource);
                break;
            case "ServiceRequest":
                call = apiService.createServiceRequest(resource);
                break;
            default:
                throw new IllegalArgumentException("Unknown resource type: " + resourceType);
        }
        
        return call.execute();
    }
    
    private Response<JsonObject> updateResource(FhirApiService apiService, String resourceType, String serverId, JsonObject resource) throws IOException {
        Call<JsonObject> call;
        
        switch (resourceType) {
            case "Patient":
                call = apiService.updatePatient(serverId, resource);
                break;
            case "Observation":
                call = apiService.updateObservation(serverId, resource);
                break;
            default:
                throw new IllegalArgumentException("Update not implemented for: " + resourceType);
        }
        
        return call.execute();
    }
    
    private Response<JsonObject> deleteResource(FhirApiService apiService, String resourceType, String serverId) throws IOException {
        resourceDao.markAsDeleted(serverId);
        return null;
    }
    
    private String extractServerId(JsonObject response) {
        if (response == null) return null;
        
        if (response.has("id")) {
            return response.get("id").getAsString();
        }
        
        if (response.has("entry")) {
            com.google.gson.JsonArray entries = response.getAsJsonArray("entry");
            if (entries.size() > 0) {
                com.google.gson.JsonObject firstEntry = entries.get(0).getAsJsonObject();
                if (firstEntry.has("resource")) {
                    com.google.gson.JsonObject resource = firstEntry.getAsJsonObject("resource");
                    if (resource.has("id")) {
                        return resource.get("id").getAsString();
                    }
                }
            }
        }
        
        return null;
    }
    
    private String extractEtag(Response<JsonObject> response) {
        if (response == null) return null;
        String etag = response.headers().get("ETag");
        if (etag != null && etag.startsWith("W/")) {
            return etag.substring(2).replace("\"", "");
        }
        return etag;
    }
    
    private boolean shouldRetry(FhirOutbox item) {
        return item.retryCount < MAX_RETRIES;
    }
    
    private long calculateNextRetryTime(int retryCount) {
        return (long) Math.pow(2, retryCount) * 1000;
    }
    
    private Retrofit createRetrofitForServer(String baseUrl) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}
