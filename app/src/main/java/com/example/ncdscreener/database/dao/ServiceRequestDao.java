package com.example.ncdscreener.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.ncdscreener.database.entity.ServiceRequestEntity;

import java.util.List;

/**
 * Data Access Object for ServiceRequest operations
 */
@Dao
public interface ServiceRequestDao {
    
    @Query("SELECT * FROM service_requests WHERE screeningId = :screeningId")
    List<ServiceRequestEntity> getServiceRequestsByScreeningId(int screeningId);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertServiceRequest(ServiceRequestEntity serviceRequest);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertServiceRequests(List<ServiceRequestEntity> serviceRequests);
    
    @Update
    void updateServiceRequest(ServiceRequestEntity serviceRequest);
    
    @Query("DELETE FROM service_requests WHERE screeningId = :screeningId")
    void deleteServiceRequestsByScreeningId(int screeningId);
}

