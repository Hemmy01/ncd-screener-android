package com.example.ncdscreener.model;

/**
 * Questionnaire model class representing screening questions and answers
 * Maps to FHIR QuestionnaireResponse resource
 */
public class Questionnaire {
    private String questionCode;
    private String answer;

    // Constructors
    public Questionnaire() {
    }

    public Questionnaire(String questionCode, String answer) {
        this.questionCode = questionCode;
        this.answer = answer;
    }

    // Getters and Setters
    public String getQuestionCode() {
        return questionCode;
    }

    public void setQuestionCode(String questionCode) {
        this.questionCode = questionCode;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}

