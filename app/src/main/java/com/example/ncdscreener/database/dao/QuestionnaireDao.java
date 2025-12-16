package com.example.ncdscreener.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.ncdscreener.database.entity.QuestionnaireEntity;

import java.util.List;

/**
 * Data Access Object for Questionnaire operations
 */
@Dao
public interface QuestionnaireDao {
    
    @Query("SELECT * FROM questionnaires WHERE screeningId = :screeningId")
    List<QuestionnaireEntity> getQuestionnairesByScreeningId(int screeningId);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertQuestionnaire(QuestionnaireEntity questionnaire);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertQuestionnaires(List<QuestionnaireEntity> questionnaires);
    
    @Query("DELETE FROM questionnaires WHERE screeningId = :screeningId")
    void deleteQuestionnairesByScreeningId(int screeningId);
}

