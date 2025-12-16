package com.example.ncdscreener.database.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

/**
 * Room Entity for Observation
 */
@Entity(
    tableName = "observations",
    foreignKeys = {
        @ForeignKey(
            entity = ScreeningEntity.class,
            parentColumns = "screeningId",
            childColumns = "screeningId",
            onDelete = CASCADE
        )
    },
    indices = {@Index("screeningId")}
)
public class ObservationEntity {
    @PrimaryKey(autoGenerate = true)
    private int observationId;
    
    private int screeningId;
    private String observationType;
    private double value;
    private String unit;
    private String finalRiskScore;

    // Constructors
    public ObservationEntity() {
    }

    @Ignore
    public ObservationEntity(int observationId, int screeningId, String observationType,
                             double value, String unit, String finalRiskScore) {
        this.observationId = observationId;
        this.screeningId = screeningId;
        this.observationType = observationType;
        this.value = value;
        this.unit = unit;
        this.finalRiskScore = finalRiskScore;
    }

    // Getters and Setters
    public int getObservationId() {
        return observationId;
    }

    public void setObservationId(int observationId) {
        this.observationId = observationId;
    }

    public int getScreeningId() {
        return screeningId;
    }

    public void setScreeningId(int screeningId) {
        this.screeningId = screeningId;
    }

    public String getObservationType() {
        return observationType;
    }

    public void setObservationType(String observationType) {
        this.observationType = observationType;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getFinalRiskScore() {
        return finalRiskScore;
    }

    public void setFinalRiskScore(String finalRiskScore) {
        this.finalRiskScore = finalRiskScore;
    }
}

