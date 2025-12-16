package com.example.ncdscreener.database.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

/**
 * Room Entity for Questionnaire
 */
@Entity(
    tableName = "questionnaires",
    foreignKeys = {
        @ForeignKey(
            entity = ScreeningEntity.class,
            parentColumns = "screeningId",
            childColumns = "screeningId",
            onDelete = CASCADE
        )
    }
)
public class QuestionnaireEntity {
    @PrimaryKey(autoGenerate = true)
    private int questionnaireId;
    
    private int screeningId;
    private String questionCode;
    private String answer;

    // Constructors
    public QuestionnaireEntity() {
    }

    public QuestionnaireEntity(int questionnaireId, int screeningId, String questionCode, String answer) {
        this.questionnaireId = questionnaireId;
        this.screeningId = screeningId;
        this.questionCode = questionCode;
        this.answer = answer;
    }

    // Getters and Setters
    public int getQuestionnaireId() {
        return questionnaireId;
    }

    public void setQuestionnaireId(int questionnaireId) {
        this.questionnaireId = questionnaireId;
    }

    public int getScreeningId() {
        return screeningId;
    }

    public void setScreeningId(int screeningId) {
        this.screeningId = screeningId;
    }

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

