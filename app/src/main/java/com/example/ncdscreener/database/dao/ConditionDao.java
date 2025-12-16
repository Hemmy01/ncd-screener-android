package com.example.ncdscreener.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.ncdscreener.database.entity.ConditionEntity;

import java.util.List;

/**
 * Data Access Object for Condition operations
 */
@Dao
public interface ConditionDao {
    
    @Query("SELECT * FROM conditions WHERE screeningId = :screeningId")
    List<ConditionEntity> getConditionsByScreeningId(int screeningId);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCondition(ConditionEntity condition);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertConditions(List<ConditionEntity> conditions);
    
    @Query("DELETE FROM conditions WHERE screeningId = :screeningId")
    void deleteConditionsByScreeningId(int screeningId);
}

