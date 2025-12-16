package com.example.ncdscreener.model;

/**
 * Condition model class representing screening outcomes
 * Maps to FHIR Condition resource
 */
public class Condition {
    private String conditionCode;
    private String conditionName;

    // Constructors
    public Condition() {
    }

    public Condition(String conditionCode, String conditionName) {
        this.conditionCode = conditionCode;
        this.conditionName = conditionName;
    }

    // Getters and Setters
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

    // Methods from class diagram
    public boolean isCritical() {
        // Critical conditions include severe hypertension, diabetic crisis, etc.
        return conditionCode != null && 
               (conditionCode.equals("HYPERTENSION_SEVERE") || 
                conditionCode.equals("DIABETES_CRISIS") ||
                conditionCode.equals("CRITICAL"));
    }
}

