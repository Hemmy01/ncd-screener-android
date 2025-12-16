package com.example.ncdscreener.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.ncdscreener.database.dao.CHWDao;
import com.example.ncdscreener.database.dao.ConditionDao;
import com.example.ncdscreener.database.dao.ObservationDao;
import com.example.ncdscreener.database.dao.PatientDao;
import com.example.ncdscreener.database.dao.QuestionnaireDao;
import com.example.ncdscreener.database.dao.ScreeningDao;
import com.example.ncdscreener.database.dao.ServiceRequestDao;
import com.example.ncdscreener.database.entity.CHWEntity;
import com.example.ncdscreener.database.entity.ConditionEntity;
import com.example.ncdscreener.database.entity.ObservationEntity;
import com.example.ncdscreener.database.entity.PatientEntity;
import com.example.ncdscreener.database.entity.QuestionnaireEntity;
import com.example.ncdscreener.database.entity.ScreeningEntity;
import com.example.ncdscreener.database.entity.ServiceRequestEntity;

/**
 * Room Database for NCD Screener App
 */
@Database(
    entities = {
        CHWEntity.class,
        PatientEntity.class,
        ScreeningEntity.class,
        ObservationEntity.class,
        ConditionEntity.class,
        QuestionnaireEntity.class,
        ServiceRequestEntity.class
        },
        version = 3,
    exportSchema = false
)
public abstract class NCDScreenerDatabase extends RoomDatabase {
    
    private static volatile NCDScreenerDatabase INSTANCE;
    
    public abstract CHWDao chwDao();
    public abstract PatientDao patientDao();
    public abstract ScreeningDao screeningDao();
    public abstract ObservationDao observationDao();
    public abstract ConditionDao conditionDao();
    public abstract QuestionnaireDao questionnaireDao();
    public abstract ServiceRequestDao serviceRequestDao();
    
    public static NCDScreenerDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (NCDScreenerDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        NCDScreenerDatabase.class,
                        "ncd_screener_database"
                    )
                    .fallbackToDestructiveMigration() // For development - in production, implement proper migrations
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}

