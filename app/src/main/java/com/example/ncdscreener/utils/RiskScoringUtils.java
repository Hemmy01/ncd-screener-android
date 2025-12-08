package com.example.ncdscreener.utils;

import com.example.ncdscreener.model.Observation;
import com.example.ncdscreener.model.Questionnaire;

import java.util.List;

/**
 * Utility class for calculating risk scores
 */
public class RiskScoringUtils {
    
    /**
     * Calculates overall NCD risk score based on observations and questionnaires
     * @param observations List of observations
     * @param questionnaires List of questionnaire responses
     * @return Risk score (0-100)
     */
    public static int calculateOverallRiskScore(List<Observation> observations, List<Questionnaire> questionnaires) {
        int riskScore = 0;
        
        // Calculate risk from observations
        for (Observation obs : observations) {
            String type = obs.getObservationType();
            double value = obs.getValue();
            
            if ("blood_pressure_systolic".equals(type)) {
                if (value >= 180) riskScore += 25;
                else if (value >= 140) riskScore += 15;
                else if (value >= 130) riskScore += 10;
                else if (value >= 120) riskScore += 5;
            } else if ("blood_pressure_diastolic".equals(type)) {
                if (value >= 120) riskScore += 25;
                else if (value >= 90) riskScore += 15;
                else if (value >= 80) riskScore += 5;
            } else if ("glucose".equals(type)) {
                if (value >= 200) riskScore += 25;
                else if (value >= 140) riskScore += 15;
                else if (value >= 100) riskScore += 5;
            } else if ("bmi".equals(type)) {
                if (value >= 30) riskScore += 10;
                else if (value >= 25) riskScore += 5;
            }
        }
        
        // Add risk from questionnaire answers
        for (Questionnaire q : questionnaires) {
            String code = q.getQuestionCode();
            String answer = q.getAnswer();
            
            if ("yes".equalsIgnoreCase(answer)) {
                switch (code) {
                    case "family_history_diabetes":
                    case "family_history_hypertension":
                        riskScore += 10;
                        break;
                    case "smoking":
                        riskScore += 8;
                        break;
                    case "physical_inactivity":
                        riskScore += 5;
                        break;
                    case "unhealthy_diet":
                        riskScore += 5;
                        break;
                }
            }
        }
        
        // Cap at 100
        return Math.min(riskScore, 100);
    }
    
    /**
     * Gets risk level description based on score
     * @param score Risk score
     * @return Risk level string
     */
    public static String getRiskLevel(int score) {
        if (score >= 50) {
            return "High Risk";
        } else if (score >= 25) {
            return "Moderate Risk";
        } else if (score >= 10) {
            return "Low Risk";
        } else {
            return "Minimal Risk";
        }
    }
}

