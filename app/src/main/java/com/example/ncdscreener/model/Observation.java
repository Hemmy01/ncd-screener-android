package com.example.ncdscreener.model;

/**
 * Observation model class representing vital signs and measurements
 * Maps to FHIR Observation resource
 */
public class Observation {
    private int observationId;
    private String observationType; // e.g., "blood_pressure", "glucose", "bmi", "weight"
    private double value;
    private String unit; // e.g., "mmHg", "mg/dL", "kg/m²"
    private String finalRiskScore; // Calculated risk score

    // Constructors
    public Observation() {
    }

    public Observation(int observationId, String observationType, double value, String unit) {
        this.observationId = observationId;
        this.observationType = observationType;
        this.value = value;
        this.unit = unit;
    }

    // Getters and Setters
    public int getObservationId() {
        return observationId;
    }

    public void setObservationId(int observationId) {
        this.observationId = observationId;
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

