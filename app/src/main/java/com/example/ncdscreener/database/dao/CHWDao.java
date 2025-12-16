package com.example.ncdscreener.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.ncdscreener.database.entity.CHWEntity;

/**
 * Data Access Object for CHW operations
 */
@Dao
public interface CHWDao {
    
    @Query("SELECT * FROM chws WHERE username = :username AND password = :password")
    CHWEntity authenticate(String username, String password);
    
    @Query("SELECT * FROM chws WHERE username = :username")
    CHWEntity getCHWByUsername(String username);
    
    @Query("SELECT * FROM chws WHERE username = :username")
    CHWEntity getCHWByUsernameSync(String username);

    @Query("SELECT * FROM chws WHERE chwId = :chwId")
    CHWEntity getCHWById(int chwId);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertCHW(CHWEntity chw);

    @Query("UPDATE chws SET password = :newPassword WHERE username = :username")
    void updatePassword(String username, String newPassword);
    
    @Query("DELETE FROM chws WHERE chwId = :chwId")
    void deleteCHW(int chwId);
}

