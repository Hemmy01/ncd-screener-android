package com.example.ncdscreener.utils;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;

import java.util.Locale;

/**
 * Utility class for managing app language/locale preferences
 * Supports English, French, and Kinyarwanda
 */
public class LocaleHelper {
    private static final String PREF_NAME = "NCDScreenerPrefs";
    private static final String KEY_LANGUAGE = "app_language";
    public static final String LANGUAGE_EN = "en";
    public static final String LANGUAGE_FR = "fr";
    public static final String LANGUAGE_RW = "rw";

    private final SharedPreferences sharedPreferences;
    private final Context appContext;

    public LocaleHelper(Context context) {
        this.appContext = context.getApplicationContext();
        this.sharedPreferences = this.appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Wrap the base context with the saved locale (call from attachBaseContext)
     */
    public ContextWrapper wrapContext(Context base) {
        String lang = getLanguage();
        Locale locale = getLocale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration(base.getResources().getConfiguration());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale);
            Context ctx = base.createConfigurationContext(config);
            return new ContextWrapper(ctx);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale);
            return new ContextWrapper(base.createConfigurationContext(config));
        } else {
            // Deprecated path for very old devices
            config.locale = locale;
            base.getResources().updateConfiguration(config, base.getResources().getDisplayMetrics());
            return new ContextWrapper(base);
        }
    }

    /**
     * Get current language setting
     * @return "en", "fr", or "rw"
     */
    public String getLanguage() {
        return sharedPreferences.getString(KEY_LANGUAGE, LANGUAGE_EN);
    }

    /**
     * Save language preference to SharedPreferences
     * @param language "en", "fr", or "rw"
     */
    public void saveLanguage(String language) {
        sharedPreferences.edit().putString(KEY_LANGUAGE, language).apply();
    }

    /**
     * Convenience: apply saved language to a running activity via recreate()
     * Use together with wrapContext in attachBaseContext for full coverage.
     */
    public void applySavedLanguageRuntime() {
        // no-op placeholder (kept for API symmetry). Actual app should call activity.recreate().
    }

    /**
     * Get locale for a language code
     */
    public static Locale getLocale(String language) {
        if (LANGUAGE_FR.equals(language)) return new Locale("fr");
        if (LANGUAGE_RW.equals(language)) return new Locale("rw");
        return new Locale("en");
    }
}
