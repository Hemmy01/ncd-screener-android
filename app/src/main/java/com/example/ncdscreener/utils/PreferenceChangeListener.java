package com.example.ncdscreener.utils;

/**
 * Interface for listening to preference changes
 */
public interface PreferenceChangeListener {
    /**
     * Called when theme preference changes
     * @param isDarkTheme true if dark theme is now active
     */
    void onThemeChanged(boolean isDarkTheme);

    /**
     * Called when language preference changes
     * @param languageCode the new language code (en, fr, rw)
     */
    void onLanguageChanged(String languageCode);
}
