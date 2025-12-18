package com.example.ncdscreener.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ncdscreener.R;
import com.example.ncdscreener.utils.LocaleHelper;
import com.example.ncdscreener.utils.ThemeManager;
import com.example.ncdscreener.utils.ToastHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

/**
 * Settings Fragment - Manages theme and language preferences
 * Uses MaterialButtonToggleGroup for exclusive selection
 */
public class SettingsFragment extends Fragment {

    private MaterialButtonToggleGroup toggleTheme;
    private MaterialButtonToggleGroup toggleLanguage;
    private MaterialButton btnThemeLight;
    private MaterialButton btnThemeDark;
    private MaterialButton btnLanguageEn;
    private MaterialButton btnLanguageFr;
    private MaterialButton btnLanguageRw;
    private MaterialButton buttonApply;

    private ThemeManager themeManager;
    private LocaleHelper localeHelper;
    private boolean isUpdatingUI = false;

    // Track selected values before applying
    private String selectedTheme;
    private String selectedLanguage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize managers
        themeManager = new ThemeManager(requireContext());
        localeHelper = new LocaleHelper(requireContext());

        // Find views
        toggleTheme = view.findViewById(R.id.toggle_theme);
        toggleLanguage = view.findViewById(R.id.toggle_language);
        btnThemeLight = view.findViewById(R.id.btn_theme_light);
        btnThemeDark = view.findViewById(R.id.btn_theme_dark);
        btnLanguageEn = view.findViewById(R.id.btn_language_en);
        btnLanguageFr = view.findViewById(R.id.btn_language_fr);
        btnLanguageRw = view.findViewById(R.id.btn_language_rw);
        buttonApply = view.findViewById(R.id.button_apply_settings);

        // Load current settings
        loadCurrentSettings();

        // Setup theme toggle listener - track selection
        toggleTheme.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isUpdatingUI && isChecked) {
                if (checkedId == R.id.btn_theme_light) {
                    selectedTheme = ThemeManager.THEME_LIGHT;
                } else if (checkedId == R.id.btn_theme_dark) {
                    selectedTheme = ThemeManager.THEME_DARK;
                }
            }
        });

        // Setup language toggle listener - track selection
        toggleLanguage.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isUpdatingUI && isChecked) {
                if (checkedId == R.id.btn_language_en) {
                    selectedLanguage = LocaleHelper.LANGUAGE_EN;
                } else if (checkedId == R.id.btn_language_fr) {
                    selectedLanguage = LocaleHelper.LANGUAGE_FR;
                } else if (checkedId == R.id.btn_language_rw) {
                    selectedLanguage = LocaleHelper.LANGUAGE_RW;
                }
            }
        });

        // Setup Apply button - apply changes only when user clicks
        buttonApply.setOnClickListener(v -> applySettings());
    }

    /**
     * Apply the selected theme and language settings
     */
    private void applySettings() {
        String currentTheme = themeManager.getTheme();
        String currentLanguage = localeHelper.getLanguage();

        // Check if anything changed
        boolean themeChanged = selectedTheme != null && !selectedTheme.equals(currentTheme);
        boolean languageChanged = selectedLanguage != null && !selectedLanguage.equals(currentLanguage);

        if (!themeChanged && !languageChanged) {
            ToastHelper.showInfo(requireContext(), "No settings changes detected to apply");
            return;
        }

        // Apply theme if changed
        if (themeChanged) {
            themeManager.saveTheme(selectedTheme);
            themeManager.applySavedTheme();
        }

        // Apply language if changed
        if (languageChanged) {
            localeHelper.saveLanguage(selectedLanguage);
        }

        // Only recreate once if either changed (this triggers recreate once)
        if (themeChanged || languageChanged) {
            // Small delay to ensure changes are saved
            buttonApply.postDelayed(() -> {
                if (isAdded()) {
                    // Use MainActivity's smooth recreation if available
                    if (getActivity() instanceof com.example.ncdscreener.activities.MainActivity) {
                        ((com.example.ncdscreener.activities.MainActivity) getActivity()).recreateSmooth();
                    } else {
                        requireActivity().recreate();
                    }
                }
            }, 100);
        }
    }

    /**
     * Load and set current theme and language preferences
     */
    private void loadCurrentSettings() {
        isUpdatingUI = true;

        // Load theme setting
        selectedTheme = themeManager.getTheme();
        if (ThemeManager.THEME_DARK.equals(selectedTheme)) {
            toggleTheme.check(R.id.btn_theme_dark);
        } else {
            toggleTheme.check(R.id.btn_theme_light);
        }

        // Load language setting
        selectedLanguage = localeHelper.getLanguage();
        switch (selectedLanguage) {
            case LocaleHelper.LANGUAGE_FR:
                toggleLanguage.check(R.id.btn_language_fr);
                break;
            case LocaleHelper.LANGUAGE_RW:
                toggleLanguage.check(R.id.btn_language_rw);
                break;
            case LocaleHelper.LANGUAGE_EN:
            default:
                toggleLanguage.check(R.id.btn_language_en);
                break;
        }

        isUpdatingUI = false;
    }
}
