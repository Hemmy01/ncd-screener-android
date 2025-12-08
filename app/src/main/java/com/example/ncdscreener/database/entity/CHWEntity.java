package com.example.ncdscreener.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room Entity for Community Health Worker
 */
@Entity(tableName = "chws")
public class CHWEntity {
    @PrimaryKey(autoGenerate = true)
    private int chwId;
    
    private String username;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String password; // Hashed password in production

    // Constructors
    public CHWEntity() {
    }

    public CHWEntity(int chwId, String username, String firstName, String lastName, String phoneNumber, String password) {
        this.chwId = chwId;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.password = password;
    }

    // Getters and Setters
    public int getChwId() {
        return chwId;
    }

    public void setChwId(int chwId) {
        this.chwId = chwId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

