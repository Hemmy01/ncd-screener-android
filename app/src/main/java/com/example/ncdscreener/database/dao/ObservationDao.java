package com.example.ncdscreener.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.ncdscreener.database.entity.ObservationEntity;

import java.util.List;

/**
 * Data Access Object for Observation operations
 */
@Dao
public interface ObservationDao {
    
    @Query("SELECT * FROM observations WHERE screeningId = :screeningId")
    List<ObservationEntity> getObservationsByScreeningId(int screeningId);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertObservation(ObservationEntity observation);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertObservations(List<ObservationEntity> observations);
    
    @Query("DELETE FROM observations WHERE screeningId = :screeningId")
    void deleteObservationsByScreeningId(int screeningId);
}

