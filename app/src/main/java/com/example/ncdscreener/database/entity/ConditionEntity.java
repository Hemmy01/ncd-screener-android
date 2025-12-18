package com.example.ncdscreener.database.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

/**
 * Room Entity for Condition
 */
@Entity(
    tableName = "conditions",
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
public class ConditionEntity {
    @PrimaryKey(autoGenerate = true)
    private int conditionId;
    
    private int screeningId;
    private String conditionCode;
    private String conditionName;

    // Constructors
    public ConditionEntity() {
    }

    @Ignore
    public ConditionEntity(int conditionId, int screeningId, String conditionCode, String conditionName) {
        this.conditionId = conditionId;
        this.screeningId = screeningId;
        this.conditionCode = conditionCode;
        this.conditionName = conditionName;
    }

    // Getters and Setters
    public int getConditionId() {
        return conditionId;
    }

    public void setConditionId(int conditionId) {
        this.conditionId = conditionId;
    }

    public int getScreeningId() {
        return screeningId;
    }

    public void setScreeningId(int screeningId) {
        this.screeningId = screeningId;
    }

    public String getConditionCode() {
        return conditionCode;
    }

    public void setConditionCode(String conditionCode) {
        this.conditionCode = conditionCode;
    }

    public String getConditionName() {
        return conditionName;
    }

    public void setConditionName(String conditionName) {
        this.conditionName = conditionName;
    }
}

