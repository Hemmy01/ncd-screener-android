package com.example.ncdscreener.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.ncdscreener.database.entity.FhirOutbox;
import java.util.List;

@Dao
public interface FhirOutboxDao {
    @Insert
    long insert(FhirOutbox outbox);
    
    @Update
    void update(FhirOutbox outbox);
    
    @Query("SELECT * FROM fhir_outbox WHERE status = 'PENDING' ORDER BY createdAt ASC LIMIT :limit")
    List<FhirOutbox> getPendingItems(int limit);
    
    @Query("SELECT * FROM fhir_outbox WHERE localId = :localId AND status = 'PENDING'")
    FhirOutbox getPendingByLocalId(String localId);
    
    @Query("SELECT * FROM fhir_outbox WHERE id = :id")
    FhirOutbox getById(long id);
    
    @Query("UPDATE fhir_outbox SET status = :status, serverId = :serverId, serverUrl = :serverUrl WHERE id = :id")
    void markAsSent(long id, String status, String serverId, String serverUrl);
    
    @Query("UPDATE fhir_outbox SET status = 'FAILED', errorMessage = :error, httpStatusCode = :statusCode, retryCount = retryCount + 1, lastRetryAt = :retryTime WHERE id = :id")
    void markAsFailed(long id, String error, int statusCode, long retryTime);
    
    @Query("UPDATE fhir_outbox SET retryCount = retryCount + 1, lastRetryAt = :retryTime WHERE id = :id")
    void incrementRetry(long id, long retryTime);
    
    @Query("DELETE FROM fhir_outbox WHERE status = 'SENT' AND lastRetryAt < :beforeTime")
    void deleteOldSent(long beforeTime);
    
    @Query("SELECT COUNT(*) FROM fhir_outbox WHERE status = 'PENDING'")
    int getPendingCount();
}
