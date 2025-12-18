package com.example.ncdscreener.model;

/**
 * Community Health Worker model class
 * Represents the health worker conducting screenings
 */
public class CHW {
    private int chwId;
    private String username;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String password; // For authentication

    // Constructors
    public CHW() {
    }

    public CHW(int chwId, String username, String firstName, String lastName, String phoneNumber) {
        this.chwId = chwId;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
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

    // Methods from class diagram
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean authenticate(String username, String password) {
        return this.username.equals(username) && this.password != null && this.password.equals(password);
    }
}

