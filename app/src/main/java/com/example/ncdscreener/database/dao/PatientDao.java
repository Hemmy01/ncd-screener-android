package com.example.ncdscreener.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.ncdscreener.database.entity.PatientEntity;

import java.util.List;

/**
 * Data Access Object for Patient operations
 */
@Dao
public interface PatientDao {
    
    @Query("SELECT * FROM patients ORDER BY firstName ASC")
    LiveData<List<PatientEntity>> getAllPatients();
    
    @Query("SELECT * FROM patients WHERE patientId = :patientId")
    LiveData<PatientEntity> getPatientById(int patientId);
    
    @Query("SELECT * FROM patients WHERE patientId = :patientId")
    PatientEntity getPatientByIdSync(int patientId);
    
    @Query("SELECT * FROM patients WHERE nationalId = :nationalId")
    PatientEntity getPatientByNationalId(int nationalId);
    
    @Query("SELECT * FROM patients ORDER BY firstName ASC")
    List<PatientEntity> getAllPatientsSync();
    
    @Query("SELECT COUNT(*) FROM patients")
    int getPatientCount();
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertPatient(PatientEntity patient);
    
    @Update
    void updatePatient(PatientEntity patient);
    
    @Query("DELETE FROM patients WHERE patientId = :patientId")
    void deletePatient(int patientId);
}

