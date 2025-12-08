package com.example.ncdscreener.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

/**
 * Room Entity for Patient
 */
@Entity(tableName = "patients")
public class PatientEntity {
    @PrimaryKey(autoGenerate = true)
    private int patientId;
    
    private int nationalId;
    private String firstName;
    private String lastName;
    private long dateOfBirth; // Stored as timestamp
    private String gender;
    private String phoneNumber;
    private String address;

    // Constructors
    public PatientEntity() {
    }

    public PatientEntity(int patientId, int nationalId, String firstName, String lastName,
                        long dateOfBirth, String gender, String phoneNumber, String address) {
        this.patientId = patientId;
        this.nationalId = nationalId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }

    // Getters and Setters
    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getNationalId() {
        return nationalId;
    }

    public void setNationalId(int nationalId) {
        this.nationalId = nationalId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public long getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(long dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}

