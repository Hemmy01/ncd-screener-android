package com.example.ncdscreener.model;

/**
 * ServiceRequest model class representing referrals for further testing or follow-up
 * Maps to FHIR ServiceRequest resource
 */
public class ServiceRequest {
    private String referralCode;
    private String reasonText;
    private String status; // e.g., "draft", "active", "completed", "cancelled"

    // Constructors
    public ServiceRequest() {
    }

    public ServiceRequest(String referralCode, String reasonText, String status) {
        this.referralCode = referralCode;
        this.reasonText = reasonText;
        this.status = status;
    }

    // Getters and Setters
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

    // Methods from class diagram
    public void updateStatus(String newStatus) {
        this.status = newStatus;
    }
}

