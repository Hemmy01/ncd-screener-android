package com.example.ncdscreener.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Screening model class representing a complete NCD screening session
 * Contains associations with Patient, CHW, and related entities
 */
public class Screening {
    private int screeningId;
    private Date screeningDate;
    private String location;
    private Patient patient;
    private CHW chw;
    private List<Condition> conditions;
    private List<Observation> observations;
    private List<Questionnaire> questionnaires;
    private List<ServiceRequest> serviceRequests;

    // Constructors
    public Screening() {
        this.conditions = new ArrayList<>();
        this.observations = new ArrayList<>();
        this.questionnaires = new ArrayList<>();
        this.serviceRequests = new ArrayList<>();
    }

    public Screening(int screeningId, Date screeningDate, String location, Patient patient, CHW chw) {
        this.screeningId = screeningId;
        this.screeningDate = screeningDate;
        this.location = location;
        this.patient = patient;
        this.chw = chw;
        this.conditions = new ArrayList<>();
        this.observations = new ArrayList<>();
        this.questionnaires = new ArrayList<>();
        this.serviceRequests = new ArrayList<>();
    }

    // Getters and Setters
    public int getScreeningId() {
        return screeningId;
    }

    public void setScreeningId(int screeningId) {
        this.screeningId = screeningId;
    }

    public Date getScreeningDate() {
        return screeningDate;
    }

    public void setScreeningDate(Date screeningDate) {
        this.screeningDate = screeningDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public CHW getChw() {
        return chw;
    }

    public void setChw(CHW chw) {
        this.chw = chw;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }

    public List<Observation> getObservations() {
        return observations;
    }

    public void setObservations(List<Observation> observations) {
        this.observations = observations;
    }

    public List<Questionnaire> getQuestionnaires() {
        return questionnaires;
    }

    public void setQuestionnaires(List<Questionnaire> questionnaires) {
        this.questionnaires = questionnaires;
    }

    public List<ServiceRequest> getServiceRequests() {
        return serviceRequests;
    }

    public void setServiceRequests(List<ServiceRequest> serviceRequests) {
        this.serviceRequests = serviceRequests;
    }

    // Methods from class diagram
    public void conductScreening() {
        // Logic to conduct the screening process
        this.screeningDate = new Date();
    }

    public String generateReport() {
        StringBuilder report = new StringBuilder();
        report.append("Screening Report #").append(screeningId).append("\n");
        report.append("Date: ").append(screeningDate).append("\n");
        report.append("Location: ").append(location).append("\n");
        report.append("Patient: ").append(patient != null ? patient.getFullName() : "N/A").append("\n");
        report.append("CHW: ").append(chw != null ? chw.getFullName() : "N/A").append("\n\n");
        
        report.append("Observations:\n");
        for (Observation obs : observations) {
            report.append("- ").append(obs.getObservationType())
                  .append(": ").append(obs.getValue()).append(" ").append(obs.getUnit()).append("\n");
        }
        
        report.append("\nConditions Identified:\n");
        for (Condition condition : conditions) {
            report.append("- ").append(condition.getConditionName()).append("\n");
        }
        
        return report.toString();
    }

    public int calculateRiskScore() {
        int riskScore = 0;
        
        // Calculate risk based on observations
        for (Observation obs : observations) {
            if ("blood_pressure_systolic".equals(obs.getObservationType())) {
                if (obs.getValue() >= 180) riskScore += 10;
                else if (obs.getValue() >= 140) riskScore += 5;
                else if (obs.getValue() >= 120) riskScore += 2;
            } else if ("blood_pressure_diastolic".equals(obs.getObservationType())) {
                if (obs.getValue() >= 120) riskScore += 10;
                else if (obs.getValue() >= 90) riskScore += 5;
                else if (obs.getValue() >= 80) riskScore += 2;
            } else if ("glucose".equals(obs.getObservationType())) {
                if (obs.getValue() >= 200) riskScore += 10;
                else if (obs.getValue() >= 140) riskScore += 5;
                else if (obs.getValue() >= 100) riskScore += 2;
            } else if ("bmi".equals(obs.getObservationType())) {
                if (obs.getValue() >= 30) riskScore += 3;
                else if (obs.getValue() >= 25) riskScore += 1;
            }
        }
        
        // Add risk from questionnaire answers
        for (Questionnaire q : questionnaires) {
            if ("family_history_diabetes".equals(q.getQuestionCode()) && "yes".equalsIgnoreCase(q.getAnswer())) {
                riskScore += 2;
            }
            if ("family_history_hypertension".equals(q.getQuestionCode()) && "yes".equalsIgnoreCase(q.getAnswer())) {
                riskScore += 2;
            }
            if ("smoking".equals(q.getQuestionCode()) && "yes".equalsIgnoreCase(q.getAnswer())) {
                riskScore += 2;
            }
        }
        
        return riskScore;
    }
}

