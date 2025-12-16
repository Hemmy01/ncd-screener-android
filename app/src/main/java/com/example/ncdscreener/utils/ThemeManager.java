package com.example.ncdscreener.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * Utility class for managing app theme preferences (Light/Dark mode)
 * Handles persistence of theme settings and applying it via AppCompatDelegate
 */
public class ThemeManager {
    private static final String PREF_NAME = "NCDScreenerPrefs";
    private static final String KEY_THEME = "app_theme";
    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";

    private final SharedPreferences sharedPreferences;
    private final Context context;

    public ThemeManager(Context context) {
        this.context = context.getApplicationContext();
        this.sharedPreferences = this.context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Get current theme setting
     * @return "light" or "dark"
     */
    public String getTheme() {
        return sharedPreferences.getString(KEY_THEME, THEME_LIGHT);
    }

    /**
     * Save theme preference and apply it
     * @param theme "light" or "dark"
     */
    public void saveTheme(String theme) {
        sharedPreferences.edit().putString(KEY_THEME, theme).apply();
        applyTheme(theme);
    }

    /**
     * Apply theme using AppCompatDelegate
     * This should be called before activity recreation for best results
     */
    public void applySavedTheme() {
        applyTheme(getTheme());
    }

    /**
     * Apply a specific theme with immediate delegate update
     * Use this before calling activity.recreate() to ensure smooth transitions
     */
    public void applyTheme(String theme) {
        if (THEME_DARK.equals(theme)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    /**
     * Apply theme and return true if change occurred
     * Useful for checking if recreation is needed
     */
    public boolean applyThemeIfChanged(String theme) {
        String currentTheme = getTheme();
        if (!currentTheme.equals(theme)) {
            applyTheme(theme);
            return true;
        }
        return false;
    }

    /**
     * Check if dark theme is enabled
     * @return true if dark theme is active
     */
    public boolean isDarkTheme() {
        return THEME_DARK.equals(getTheme());
    }
}
