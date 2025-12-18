package com.example.ncdscreener.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Session Manager for handling user authentication sessions
 * Implements 1-hour session expiration
 */
public class SessionManager {
    private static final String PREF_NAME = "NCDScreenerPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_CHW_ID = "chwId";
    private static final String KEY_CHW_USERNAME = "chwUsername";
    private static final String KEY_CHW_FIRST_NAME = "chwFirstName";
    private static final String KEY_CHW_LAST_NAME = "chwLastName";
    private static final String KEY_CHW_NAME = "chwName";
    private static final String KEY_LOGIN_TIME = "loginTime";
    
    // Session expiration time: 1 hour in milliseconds
    private static final long SESSION_DURATION = 60 * 60 * 1000; // 1 hour
    
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;
    
    public SessionManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }
    
    /**
     * Creates a login session
     */
    public void createLoginSession(int chwId, String username, String firstName, String lastName, String fullName) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_CHW_ID, chwId);
        editor.putString(KEY_CHW_USERNAME, username);
        editor.putString(KEY_CHW_FIRST_NAME, firstName);
        editor.putString(KEY_CHW_LAST_NAME, lastName);
        editor.putString(KEY_CHW_NAME, fullName);
        editor.putLong(KEY_LOGIN_TIME, System.currentTimeMillis());
        editor.apply();
    }
    
    /**
     * Checks if user is logged in and session is valid
     */
    public boolean isLoggedIn() {
        boolean isLoggedIn = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
        
        if (!isLoggedIn) {
            return false;
        }
        
        // Check if session has expired
        long loginTime = sharedPreferences.getLong(KEY_LOGIN_TIME, 0);
        long currentTime = System.currentTimeMillis();
        
        if (currentTime - loginTime > SESSION_DURATION) {
            // Session expired
            logout();
            return false;
        }
        
        return true;
    }
    
    /**
     * Gets remaining session time in milliseconds
     */
    public long getRemainingSessionTime() {
        long loginTime = sharedPreferences.getLong(KEY_LOGIN_TIME, 0);
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - loginTime;
        long remaining = SESSION_DURATION - elapsed;
        return remaining > 0 ? remaining : 0;
    }
    
    /**
     * Gets remaining session time as formatted string
     */
    public String getRemainingSessionTimeFormatted() {
        long remaining = getRemainingSessionTime();
        if (remaining <= 0) {
            return "Session expired";
        }
        
        long minutes = remaining / (60 * 1000);
        long seconds = (remaining % (60 * 1000)) / 1000;
        
        if (minutes > 0) {
            return String.format("%d min %d sec", minutes, seconds);
        } else {
            return String.format("%d sec", seconds);
        }
    }
    
    /**
     * Checks if session is about to expire (within 5 minutes)
     */
    public boolean isSessionExpiringSoon() {
        long remaining = getRemainingSessionTime();
        return remaining > 0 && remaining < (5 * 60 * 1000); // Less than 5 minutes
    }
    
    /**
     * Gets CHW ID
     */
    public int getChwId() {
        return sharedPreferences.getInt(KEY_CHW_ID, 0);
    }
    
    /**
     * Gets CHW username
     */
    public String getChwUsername() {
        return sharedPreferences.getString(KEY_CHW_USERNAME, "");
    }
    
    /**
     * Gets CHW first name
     */
    public String getChwFirstName() {
        return sharedPreferences.getString(KEY_CHW_FIRST_NAME, "");
    }
    
    /**
     * Gets CHW last name
     */
    public String getChwLastName() {
        return sharedPreferences.getString(KEY_CHW_LAST_NAME, "");
    }
    
    /**
     * Gets CHW full name
     */
    public String getChwName() {
        return sharedPreferences.getString(KEY_CHW_NAME, "");
    }
    
    /**
     * Logs out user and clears session
     */
    public void logout() {
        editor.clear();
        editor.apply();
    }
    
    /**
     * Refreshes session (extends login time)
     */
    public void refreshSession() {
        if (isLoggedIn()) {
            editor.putLong(KEY_LOGIN_TIME, System.currentTimeMillis());
            editor.apply();
        }
    }
}

