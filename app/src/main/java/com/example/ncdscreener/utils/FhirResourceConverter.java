package com.example.ncdscreener.utils;

import com.example.ncdscreener.model.Condition;
import com.example.ncdscreener.model.Observation;
import com.example.ncdscreener.model.Patient;
import com.example.ncdscreener.model.Questionnaire;
import com.example.ncdscreener.model.ServiceRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Utility class for converting model objects to FHIR R4 JSON resources
 */
public class FhirResourceConverter {
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US);

    /**
     * Converts Patient model to FHIR Patient resource JSON
     */
    public static JSONObject convertPatientToFhir(Patient patient) {
        try {
            JSONObject fhirPatient = new JSONObject();
            fhirPatient.put("resourceType", "Patient");
            
            // ID
            if (patient.getPatientId() > 0) {
                JSONArray identifier = new JSONArray();
                JSONObject nationalId = new JSONObject();
                nationalId.put("system", "http://example.org/national-id");
                nationalId.put("value", String.valueOf(patient.getNationalId()));
                identifier.put(nationalId);
                fhirPatient.put("identifier", identifier);
            }
            
            // Name
            JSONArray name = new JSONArray();
            JSONObject nameObj = new JSONObject();
            JSONArray given = new JSONArray();
            if (patient.getFirstName() != null) {
                given.put(patient.getFirstName());
            }
            nameObj.put("given", given);
            if (patient.getLastName() != null) {
                nameObj.put("family", patient.getLastName());
            }
            name.put(nameObj);
            fhirPatient.put("name", name);
            
            // Gender
            if (patient.getGender() != null && !patient.getGender().isEmpty()) {
                String gender = patient.getGender().toLowerCase();
                if (gender.startsWith("m")) {
                    fhirPatient.put("gender", "male");
                } else if (gender.startsWith("f")) {
                    fhirPatient.put("gender", "female");
                } else {
                    fhirPatient.put("gender", "other");
                }
            }
            
            // Birth Date
            if (patient.getDateOfBirth() != null) {
                fhirPatient.put("birthDate", DATE_FORMAT.format(patient.getDateOfBirth()));
            }
            
            // Telecom (Phone)
            if (patient.getPhoneNumber() != null && !patient.getPhoneNumber().isEmpty()) {
                JSONArray telecom = new JSONArray();
                JSONObject phone = new JSONObject();
                phone.put("system", "phone");
                phone.put("value", patient.getPhoneNumber());
                telecom.put(phone);
                fhirPatient.put("telecom", telecom);
            }
            
            // Address
            if (patient.getAddress() != null && !patient.getAddress().isEmpty()) {
                JSONArray address = new JSONArray();
                JSONObject addressObj = new JSONObject();
                JSONArray line = new JSONArray();
                line.put(patient.getAddress());
                addressObj.put("line", line);
                address.put(addressObj);
                fhirPatient.put("address", address);
            }
            
            return fhirPatient;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Converts Observation model to FHIR Observation resource JSON
     */
    public static JSONObject convertObservationToFhir(Observation observation, String patientReference) {
        try {
            JSONObject fhirObservation = new JSONObject();
            fhirObservation.put("resourceType", "Observation");
            fhirObservation.put("status", "final");
            
            // Subject (Patient reference)
            JSONObject subject = new JSONObject();
            subject.put("reference", patientReference);
            fhirObservation.put("subject", subject);
            
            // Code
            JSONObject code = new JSONObject();
            JSONArray coding = new JSONArray();
            JSONObject codingObj = new JSONObject();
            
            String observationType = observation.getObservationType();
            String codeSystem = "http://loinc.org";
            String codeValue = "";
            String display = "";
            
            switch (observationType) {
                case "blood_pressure_systolic":
                    codeValue = "8480-6";
                    display = "Systolic blood pressure";
                    break;
                case "blood_pressure_diastolic":
                    codeValue = "8462-4";
                    display = "Diastolic blood pressure";
                    break;
                case "glucose":
                    codeValue = "2339-0";
                    display = "Glucose [Mass/volume] in Blood";
                    break;
                case "bmi":
                    codeValue = "39156-5";
                    display = "Body mass index (BMI) [Ratio]";
                    break;
                case "weight":
                    codeValue = "29463-7";
                    display = "Body weight";
                    break;
                case "height":
                    codeValue = "8302-2";
                    display = "Body height";
                    break;
                default:
                    codeValue = "unknown";
                    display = observationType;
            }
            
            codingObj.put("system", codeSystem);
            codingObj.put("code", codeValue);
            codingObj.put("display", display);
            coding.put(codingObj);
            code.put("coding", coding);
            fhirObservation.put("code", code);
            
            // Value
            JSONObject valueQuantity = new JSONObject();
            valueQuantity.put("value", observation.getValue());
            valueQuantity.put("unit", observation.getUnit());
            
            // Unit code mapping
            String unitCode = "";
            switch (observation.getUnit()) {
                case "mmHg":
                    unitCode = "mm[Hg]";
                    break;
                case "mg/dL":
                    unitCode = "mg/dL";
                    break;
                case "kg/m²":
                    unitCode = "kg/m2";
                    break;
                case "kg":
                    unitCode = "kg";
                    break;
                case "cm":
                    unitCode = "cm";
                    break;
                default:
                    unitCode = observation.getUnit();
            }
            valueQuantity.put("code", unitCode);
            valueQuantity.put("system", "http://unitsofmeasure.org");
            fhirObservation.put("valueQuantity", valueQuantity);
            
            // Effective date time
            fhirObservation.put("effectiveDateTime", DATETIME_FORMAT.format(new Date()));
            
            return fhirObservation;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Converts Questionnaire model to FHIR QuestionnaireResponse resource JSON
     */
    public static JSONObject convertQuestionnaireToFhir(List<Questionnaire> questionnaires, String patientReference) {
        try {
            JSONObject fhirQuestionnaireResponse = new JSONObject();
            fhirQuestionnaireResponse.put("resourceType", "QuestionnaireResponse");
            fhirQuestionnaireResponse.put("status", "completed");
            
            // Subject (Patient reference)
            JSONObject subject = new JSONObject();
            subject.put("reference", patientReference);
            fhirQuestionnaireResponse.put("subject", subject);
            
            // Authored
            fhirQuestionnaireResponse.put("authored", DATETIME_FORMAT.format(new Date()));
            
            // Item (Answers)
            JSONArray item = new JSONArray();
            for (Questionnaire q : questionnaires) {
                JSONObject itemObj = new JSONObject();
                JSONObject linkId = new JSONObject();
                linkId.put("valueString", q.getQuestionCode());
                itemObj.put("linkId", q.getQuestionCode());
                
                JSONArray answer = new JSONArray();
                JSONObject answerObj = new JSONObject();
                answerObj.put("valueBoolean", "yes".equalsIgnoreCase(q.getAnswer()));
                answer.put(answerObj);
                itemObj.put("answer", answer);
                
                item.put(itemObj);
            }
            fhirQuestionnaireResponse.put("item", item);
            
            return fhirQuestionnaireResponse;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Converts Condition model to FHIR Condition resource JSON
     */
    public static JSONObject convertConditionToFhir(Condition condition, String patientReference) {
        try {
            JSONObject fhirCondition = new JSONObject();
            fhirCondition.put("resourceType", "Condition");
            JSONObject clinicalStatus = new JSONObject();
            JSONArray clinicalStatusCoding = new JSONArray();
            JSONObject clinicalStatusCodingObj = new JSONObject();
            clinicalStatusCodingObj.put("system", "http://terminology.hl7.org/CodeSystem/condition-clinical");
            clinicalStatusCodingObj.put("code", "active");
            clinicalStatusCoding.put(clinicalStatusCodingObj);
            clinicalStatus.put("coding", clinicalStatusCoding);
            fhirCondition.put("clinicalStatus", clinicalStatus);
            
            // Subject (Patient reference)
            JSONObject subject = new JSONObject();
            subject.put("reference", patientReference);
            fhirCondition.put("subject", subject);
            
            // Code
            JSONObject code = new JSONObject();
            JSONArray coding = new JSONArray();
            JSONObject codingObj = new JSONObject();
            
            String conditionCode = condition.getConditionCode();
            String codeSystem = "http://snomed.info/sct";
            String codeValue = "";
            String display = condition.getConditionName();
            
            switch (conditionCode) {
                case "HYPERTENSION":
                case "HYPERTENSION_SEVERE":
                    codeValue = "38341003";
                    display = "Hypertensive disorder";
                    break;
                case "DIABETES":
                case "DIABETES_CRISIS":
                    codeValue = "73211009";
                    display = "Diabetes mellitus";
                    break;
                default:
                    codeValue = "64572001";
                    display = condition.getConditionName();
            }
            
            codingObj.put("system", codeSystem);
            codingObj.put("code", codeValue);
            codingObj.put("display", display);
            coding.put(codingObj);
            code.put("coding", coding);
            fhirCondition.put("code", code);
            
            // Onset date time
            fhirCondition.put("onsetDateTime", DATETIME_FORMAT.format(new Date()));
            
            return fhirCondition;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Converts ServiceRequest model to FHIR ServiceRequest resource JSON
     */
    public static JSONObject convertServiceRequestToFhir(ServiceRequest serviceRequest, String patientReference) {
        try {
            JSONObject fhirServiceRequest = new JSONObject();
            fhirServiceRequest.put("resourceType", "ServiceRequest");
            fhirServiceRequest.put("status", serviceRequest.getStatus() != null ? serviceRequest.getStatus() : "active");
            fhirServiceRequest.put("intent", "order");
            
            // Subject (Patient reference)
            JSONObject subject = new JSONObject();
            subject.put("reference", patientReference);
            fhirServiceRequest.put("subject", subject);
            
            // Code (Referral)
            JSONObject code = new JSONObject();
            JSONArray coding = new JSONArray();
            JSONObject codingObj = new JSONObject();
            codingObj.put("system", "http://snomed.info/sct");
            codingObj.put("code", "3457005");
            codingObj.put("display", "Patient referral");
            coding.put(codingObj);
            code.put("coding", coding);
            fhirServiceRequest.put("code", code);
            
            // Reason code
            if (serviceRequest.getReasonText() != null && !serviceRequest.getReasonText().isEmpty()) {
                JSONArray reasonCode = new JSONArray();
                JSONObject reasonCodeObj = new JSONObject();
                reasonCodeObj.put("text", serviceRequest.getReasonText());
                reasonCode.put(reasonCodeObj);
                fhirServiceRequest.put("reasonCode", reasonCode);
            }
            
            // Authored on
            fhirServiceRequest.put("authoredOn", DATETIME_FORMAT.format(new Date()));
            
            // Requisition (Referral code)
            if (serviceRequest.getReferralCode() != null) {
                JSONObject requisition = new JSONObject();
                requisition.put("value", serviceRequest.getReferralCode());
                fhirServiceRequest.put("requisition", requisition);
            }
            
            return fhirServiceRequest;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

