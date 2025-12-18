package com.example.ncdscreener.utils;

import com.example.ncdscreener.model.Observation;

/**
 * Utility class for validating health data
 */
public class HealthDataValidator {
    
    /**
     * Validates blood pressure values
     * @param systolic Systolic pressure
     * @param diastolic Diastolic pressure
     * @return true if values are within reasonable range
     */
    public static boolean isValidBloodPressure(double systolic, double diastolic) {
        return systolic >= 50 && systolic <= 300 && 
               diastolic >= 30 && diastolic <= 200 &&
               systolic > diastolic;
    }
    
    /**
     * Validates glucose level
     * @param glucose Glucose value in mg/dL
     * @return true if value is within reasonable range
     */
    public static boolean isValidGlucose(double glucose) {
        return glucose >= 50 && glucose <= 600;
    }
    
    /**
     * Validates BMI value
     * @param bmi BMI value
     * @return true if value is within reasonable range
     */
    public static boolean isValidBMI(double bmi) {
        return bmi >= 10 && bmi <= 60;
    }
    
    /**
     * Validates weight value
     * @param weight Weight in kg
     * @return true if value is within reasonable range
     */
    public static boolean isValidWeight(double weight) {
        return weight >= 20 && weight <= 300;
    }
    
    /**
     * Validates height value
     * @param height Height in cm
     * @return true if value is within reasonable range
     */
    public static boolean isValidHeight(double height) {
        return height >= 100 && height <= 250;
    }
    
    /**
     * Categorizes blood pressure based on AHA guidelines
     * @param systolic Systolic pressure
     * @param diastolic Diastolic pressure
     * @return Category string
     */
    public static String categorizeBloodPressure(double systolic, double diastolic) {
        if (systolic >= 180 || diastolic >= 120) {
            return "Hypertensive Crisis";
        } else if (systolic >= 140 || diastolic >= 90) {
            return "Stage 2 Hypertension";
        } else if (systolic >= 130 || diastolic >= 80) {
            return "Stage 1 Hypertension";
        } else if (systolic >= 120 || diastolic >= 80) {
            return "Elevated";
        } else {
            return "Normal";
        }
    }
    
    /**
     * Categorizes glucose level
     * @param glucose Glucose value in mg/dL
     * @return Category string
     */
    public static String categorizeGlucose(double glucose) {
        if (glucose >= 200) {
            return "Diabetic";
        } else if (glucose >= 140) {
            return "Prediabetic";
        } else if (glucose >= 100) {
            return "At Risk";
        } else {
            return "Normal";
        }
    }
    
    /**
     * Calculates BMI from weight and height
     * @param weight Weight in kg
     * @param height Height in cm
     * @return BMI value
     */
    public static double calculateBMI(double weight, double height) {
        double heightInMeters = height / 100.0;
        return weight / (heightInMeters * heightInMeters);
    }
}

