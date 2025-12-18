package com.example.ncdscreener.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.ncdscreener.database.entity.ScreeningEntity;

import java.util.List;

/**
 * Data Access Object for Screening operations
 */
@Dao
public interface ScreeningDao {
    
    @Query("SELECT * FROM screenings ORDER BY screeningDate DESC")
    LiveData<List<ScreeningEntity>> getAllScreenings();
    
    @Query("SELECT * FROM screenings WHERE screeningId = :screeningId")
    LiveData<ScreeningEntity> getScreeningById(int screeningId);
    
    @Query("SELECT * FROM screenings WHERE screeningId = :screeningId")
    ScreeningEntity getScreeningByIdSync(int screeningId);
    
    @Query("SELECT * FROM screenings ORDER BY screeningDate DESC")
    List<ScreeningEntity> getAllScreeningsSync();
    
    @Query("SELECT * FROM screenings WHERE patientId = :patientId ORDER BY screeningDate DESC")
    LiveData<List<ScreeningEntity>> getScreeningsByPatientId(int patientId);
    
    @Query("SELECT COUNT(*) FROM screenings")
    int getScreeningCount();
    
    @Query("SELECT * FROM screenings ORDER BY screeningDate DESC LIMIT :limit")
    List<ScreeningEntity> getRecentScreenings(int limit);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertScreening(ScreeningEntity screening);
    
    @Query("DELETE FROM screenings WHERE screeningId = :screeningId")
    void deleteScreening(int screeningId);
}

