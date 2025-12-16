package com.example.ncdscreener.database.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

/**
 * Room Entity for Screening
 */
@Entity(
    tableName = "screenings",
    foreignKeys = {
        @ForeignKey(
            entity = PatientEntity.class,
            parentColumns = "patientId",
            childColumns = "patientId",
            onDelete = CASCADE
        )
    },
    indices = {@Index("patientId")}
)
public class ScreeningEntity {
    @PrimaryKey(autoGenerate = true)
    private int screeningId;
    
    private long screeningDate; // Stored as timestamp
    private String location;
    private int patientId;
    private int chwId;
    private String chwName;

    // Constructors
    public ScreeningEntity() {
    }

    @Ignore
    public ScreeningEntity(int screeningId, long screeningDate, String location,
                           int patientId, int chwId, String chwName) {
        this.screeningId = screeningId;
        this.screeningDate = screeningDate;
        this.location = location;
        this.patientId = patientId;
        this.chwId = chwId;
        this.chwName = chwName;
    }

    // Getters and Setters
    public int getScreeningId() {
        return screeningId;
    }

    public void setScreeningId(int screeningId) {
        this.screeningId = screeningId;
    }

    public long getScreeningDate() {
        return screeningDate;
    }

    public void setScreeningDate(long screeningDate) {
        this.screeningDate = screeningDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getChwId() {
        return chwId;
    }

    public void setChwId(int chwId) {
        this.chwId = chwId;
    }

    public String getChwName() {
        return chwName;
    }

    public void setChwName(String chwName) {
        this.chwName = chwName;
    }
}

