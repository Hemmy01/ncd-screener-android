package com.example.ncdscreener.database.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

/**
 * Room Entity for ServiceRequest
 */
@Entity(
    tableName = "service_requests",
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
public class ServiceRequestEntity {
    @PrimaryKey(autoGenerate = true)
    private int serviceRequestId;
    
    private int screeningId;
    private String referralCode;
    private String reasonText;
    private String status;

    // Constructors
    public ServiceRequestEntity() {
    }

    @Ignore
    public ServiceRequestEntity(int serviceRequestId, int screeningId, String referralCode,
                                String reasonText, String status) {
        this.serviceRequestId = serviceRequestId;
        this.screeningId = screeningId;
        this.referralCode = referralCode;
        this.reasonText = reasonText;
        this.status = status;
    }

    // Getters and Setters
    public int getServiceRequestId() {
        return serviceRequestId;
    }

    public void setServiceRequestId(int serviceRequestId) {
        this.serviceRequestId = serviceRequestId;
    }

    public int getScreeningId() {
        return screeningId;
    }

    public void setScreeningId(int screeningId) {
        this.screeningId = screeningId;
    }

    public String getReferralCode() {
        return referralCode;
    }

    public void setReferralCode(String referralCode) {
        this.referralCode = referralCode;
    }

    public String getReasonText() {
        return reasonText;
    }

    public void setReasonText(String reasonText) {
        this.reasonText = reasonText;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

