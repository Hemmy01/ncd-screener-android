package com.example.ncdscreener.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.example.ncdscreener.database.dao.FhirOutboxDao;
import com.example.ncdscreener.database.dao.FhirResourceDao;
import com.example.ncdscreener.database.entity.FhirOutbox;
import com.example.ncdscreener.database.entity.FhirResource;

@Database(entities = {FhirOutbox.class, FhirResource.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract FhirOutboxDao outboxDao();
    public abstract FhirResourceDao resourceDao();
    
    private static volatile AppDatabase INSTANCE;
    
    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "ncd_screener_database")
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
