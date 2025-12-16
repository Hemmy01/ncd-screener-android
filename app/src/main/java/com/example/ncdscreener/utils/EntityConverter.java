package com.example.ncdscreener.utils;

import com.example.ncdscreener.database.entity.CHWEntity;
import com.example.ncdscreener.database.entity.ConditionEntity;
import com.example.ncdscreener.database.entity.ObservationEntity;
import com.example.ncdscreener.database.entity.PatientEntity;
import com.example.ncdscreener.database.entity.QuestionnaireEntity;
import com.example.ncdscreener.database.entity.ScreeningEntity;
import com.example.ncdscreener.database.entity.ServiceRequestEntity;
import com.example.ncdscreener.model.CHW;
import com.example.ncdscreener.model.Condition;
import com.example.ncdscreener.model.Observation;
import com.example.ncdscreener.model.Patient;
import com.example.ncdscreener.model.Questionnaire;
import com.example.ncdscreener.model.Screening;
import com.example.ncdscreener.model.ServiceRequest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Utility class for converting between Model classes and Room Entities
 */
public class EntityConverter {
    
    // CHW conversions
    public static CHWEntity toEntity(CHW chw) {
        CHWEntity entity = new CHWEntity();
        entity.setChwId(chw.getChwId());
        entity.setUsername(chw.getUsername());
        entity.setFirstName(chw.getFirstName());
        entity.setLastName(chw.getLastName());
        entity.setPhoneNumber(chw.getPhoneNumber());
        entity.setPassword(chw.getPassword());
        return entity;
    }
    
    public static CHW fromEntity(CHWEntity entity) {
        CHW chw = new CHW();
        chw.setChwId(entity.getChwId());
        chw.setUsername(entity.getUsername());
        chw.setFirstName(entity.getFirstName());
        chw.setLastName(entity.getLastName());
        chw.setPhoneNumber(entity.getPhoneNumber());
        chw.setPassword(entity.getPassword());
        return chw;
    }
    
    // Patient conversions
    public static PatientEntity toEntity(Patient patient) {
        PatientEntity entity = new PatientEntity();
        entity.setPatientId(patient.getPatientId());
        entity.setNationalId(patient.getNationalId());
        entity.setFirstName(patient.getFirstName());
        entity.setLastName(patient.getLastName());
        entity.setDateOfBirth(patient.getDateOfBirth() != null ? patient.getDateOfBirth().getTime() : 0);
        entity.setGender(patient.getGender());
        entity.setPhoneNumber(patient.getPhoneNumber());
        entity.setAddress(patient.getAddress());
        return entity;
    }
    
    public static Patient fromEntity(PatientEntity entity) {
        Patient patient = new Patient();
        patient.setPatientId(entity.getPatientId());
        patient.setNationalId(entity.getNationalId());
        patient.setFirstName(entity.getFirstName());
        patient.setLastName(entity.getLastName());
        patient.setDateOfBirth(new Date(entity.getDateOfBirth()));
        patient.setGender(entity.getGender());
        patient.setPhoneNumber(entity.getPhoneNumber());
        patient.setAddress(entity.getAddress());
        return patient;
    }
    
    // Screening conversions
    public static ScreeningEntity toEntity(Screening screening) {
        ScreeningEntity entity = new ScreeningEntity();
        entity.setScreeningId(screening.getScreeningId());
        entity.setScreeningDate(screening.getScreeningDate() != null ? screening.getScreeningDate().getTime() : System.currentTimeMillis());
        entity.setLocation(screening.getLocation());
        entity.setPatientId(screening.getPatient() != null ? screening.getPatient().getPatientId() : 0);
        entity.setChwId(screening.getChw() != null ? screening.getChw().getChwId() : 0);
        entity.setChwName(screening.getChw() != null ? screening.getChw().getFullName() : "");
        return entity;
    }
    
    public static Screening fromEntity(ScreeningEntity entity, Patient patient, CHW chw,
                                      List<Observation> observations, List<Condition> conditions,
                                      List<Questionnaire> questionnaires, List<ServiceRequest> serviceRequests) {
        Screening screening = new Screening();
        screening.setScreeningId(entity.getScreeningId());
        screening.setScreeningDate(new Date(entity.getScreeningDate()));
        screening.setLocation(entity.getLocation());
        screening.setPatient(patient);
        screening.setChw(chw);
        screening.setObservations(observations != null ? observations : new ArrayList<>());
        screening.setConditions(conditions != null ? conditions : new ArrayList<>());
        screening.setQuestionnaires(questionnaires != null ? questionnaires : new ArrayList<>());
        screening.setServiceRequests(serviceRequests != null ? serviceRequests : new ArrayList<>());
        return screening;
    }
    
    // Observation conversions
    public static ObservationEntity toEntity(Observation observation, int screeningId) {
        ObservationEntity entity = new ObservationEntity();
        entity.setObservationId(observation.getObservationId());
        entity.setScreeningId(screeningId);
        entity.setObservationType(observation.getObservationType());
        entity.setValue(observation.getValue());
        entity.setUnit(observation.getUnit());
        entity.setFinalRiskScore(observation.getFinalRiskScore());
        return entity;
    }
    
    public static Observation fromEntity(ObservationEntity entity) {
        Observation observation = new Observation();
        observation.setObservationId(entity.getObservationId());
        observation.setObservationType(entity.getObservationType());
        observation.setValue(entity.getValue());
        observation.setUnit(entity.getUnit());
        observation.setFinalRiskScore(entity.getFinalRiskScore());
        return observation;
    }
    
    // Condition conversions
    public static ConditionEntity toEntity(Condition condition, int screeningId) {
        ConditionEntity entity = new ConditionEntity();
        entity.setScreeningId(screeningId);
        entity.setConditionCode(condition.getConditionCode());
        entity.setConditionName(condition.getConditionName());
        return entity;
    }
    
    public static Condition fromEntity(ConditionEntity entity) {
        Condition condition = new Condition();
        condition.setConditionCode(entity.getConditionCode());
        condition.setConditionName(entity.getConditionName());
        return condition;
    }
    
    // Questionnaire conversions
    public static QuestionnaireEntity toEntity(Questionnaire questionnaire, int screeningId) {
        QuestionnaireEntity entity = new QuestionnaireEntity();
        entity.setScreeningId(screeningId);
        entity.setQuestionCode(questionnaire.getQuestionCode());
        entity.setAnswer(questionnaire.getAnswer());
        return entity;
    }
    
    public static Questionnaire fromEntity(QuestionnaireEntity entity) {
        Questionnaire questionnaire = new Questionnaire();
        questionnaire.setQuestionCode(entity.getQuestionCode());
        questionnaire.setAnswer(entity.getAnswer());
        return questionnaire;
    }
    
    // ServiceRequest conversions
    public static ServiceRequestEntity toEntity(ServiceRequest serviceRequest, int screeningId) {
        ServiceRequestEntity entity = new ServiceRequestEntity();
        entity.setScreeningId(screeningId);
        entity.setReferralCode(serviceRequest.getReferralCode());
        entity.setReasonText(serviceRequest.getReasonText());
        entity.setStatus(serviceRequest.getStatus());
        return entity;
    }
    
    public static ServiceRequest fromEntity(ServiceRequestEntity entity) {
        ServiceRequest serviceRequest = new ServiceRequest();
        serviceRequest.setReferralCode(entity.getReferralCode());
        serviceRequest.setReasonText(entity.getReasonText());
        serviceRequest.setStatus(entity.getStatus());
        return serviceRequest;
    }
}

