package com.example.ncdscreener.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.ncdscreener.database.entity.FhirResource;
import java.util.List;

@Dao
public interface FhirResourceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(FhirResource resource);
    
    @Update
    void update(FhirResource resource);
    
    @Query("SELECT * FROM fhir_resources WHERE localId = :localId")
    FhirResource getByLocalId(String localId);
    
    @Query("SELECT * FROM fhir_resources WHERE serverId = :serverId AND resourceType = :resourceType")
    FhirResource getByServerId(String serverId, String resourceType);
    
    @Query("SELECT * FROM fhir_resources WHERE resourceType = :resourceType AND isDeleted = 0")
    List<FhirResource> getAllByType(String resourceType);
    
    @Query("UPDATE fhir_resources SET serverId = :serverId, serverUrl = :serverUrl, etag = :etag, lastSynced = :syncTime, serverJson = :serverJson WHERE localId = :localId")
    void updateServerInfo(String localId, String serverId, String serverUrl, String etag, long syncTime, String serverJson);
    
    @Query("UPDATE fhir_resources SET isDeleted = 1 WHERE localId = :localId")
    void markAsDeleted(String localId);
}
