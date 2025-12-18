package com.example.ncdscreener.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentContainerView;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.ncdscreener.R;
import com.example.ncdscreener.services.SyncManager;
import com.example.ncdscreener.utils.LocaleHelper;
import com.example.ncdscreener.utils.SessionManager;
import com.example.ncdscreener.utils.ThemeManager;
import com.example.ncdscreener.utils.ToastHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private MaterialToolbar toolbar;
    private FloatingActionButton fabNewScreening;
    private SharedPreferences sharedPreferences;
    private SyncManager syncManager;
    private SessionManager sessionManager;

    @Override
    protected void attachBaseContext(Context newBase) {
        // Wrap base context with saved locale to ensure resources load in correct language
        LocaleHelper localeHelper = new LocaleHelper(newBase);
        Context context = localeHelper.wrapContext(newBase);
        super.attachBaseContext(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme via AppCompatDelegate before super.onCreate to reduce flicker
        ThemeManager themeManager = new ThemeManager(this);
        themeManager.applySavedTheme();
        // Also apply the theme style for this activity
        applyThemeStyle(themeManager.getTheme());
        super.onCreate(savedInstanceState);
        
        // Check authentication with session management
        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }
        
        // Refresh session on activity start (user is active)
        sessionManager.refreshSession();
        
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        // Initialize sync manager
        syncManager = new SyncManager(this);
        syncManager.schedulePeriodicSync();

        // Setup toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        // Add profile menu item
        toolbar.inflateMenu(R.menu.main_menu);
        toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_profile) {
                if (navController != null) {
                    navController.navigate(R.id.profileFragment);
                }
                return true;
            } else if (id == R.id.menu_settings) {
                if (navController != null) {
                    navController.navigate(R.id.settingsFragment);
                }
                return true;
            }
            return false;
        });

        // Setup navigation - get NavController from NavHostFragment
        // Post to ensure fragment is attached
        findViewById(R.id.nav_host_fragment).post(() -> {
            NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.nav_host_fragment);
            if (navHostFragment != null) {
                navController = navHostFragment.getNavController();
                AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
                NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
            }
        });

        // Setup FAB - show options dialog (Add Patient or New Screening)
        fabNewScreening = findViewById(R.id.fab_new_screening);
        fabNewScreening.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (navController != null) {
                    showFabOptionsDialog();
                }
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (navController != null) {
            return navController.navigateUp() || super.onSupportNavigateUp();
        }
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check session validity on resume
        if (sessionManager != null && !sessionManager.isLoggedIn()) {
            navigateToLogin();
        } else if (sessionManager != null) {
            // Refresh session when user is active
            sessionManager.refreshSession();
        }
    }

    /**
     * Navigates to LoginActivity
     */
    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Apply theme style based on preference (light/dark)
     */
    private void applyThemeStyle(String theme) {
        // The style resources are generated from XML with dots converted to underscores
        // Theme.NCDScreener becomes Theme_NCDScreener
        // Theme.NCDScreener.Dark becomes Theme_NCDScreener_Dark
        if (ThemeManager.THEME_DARK.equals(theme)) {
            setTheme(R.style.Theme_NCDScreener_Dark);
        } else {
            setTheme(R.style.Theme_NCDScreener);
        }
    }

    /**
     * Smoothly recreate the activity with minimal flicker
     * Used when theme or language changes
     */
    public void recreateSmooth() {
        recreate();
        // Apply fade transition for smooth recreation
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    /**
     * Shows dialog with options: Add Patient or New Screening
     */
    private void showFabOptionsDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.choose_action));
        
        String[] options = {getString(R.string.add_new_patient), getString(R.string.new_screening)};
        builder.setItems(options, (dialog, which) -> {
            if (navController != null) {
                if (which == 0) {
                    // Add New Patient - navigate directly to register patient
                    androidx.lifecycle.ViewModelProvider provider = new androidx.lifecycle.ViewModelProvider(this);
                    com.example.ncdscreener.viewmodel.PatientViewModel patientViewModel = provider.get(com.example.ncdscreener.viewmodel.PatientViewModel.class);
                    patientViewModel.selectPatient(0); // Clear selection for new patient
                    navController.navigate(R.id.registerPatientFragment);
                } else if (which == 1) {
                    // New Screening - navigate directly to screening form (patient selection is in the form)
                    androidx.lifecycle.ViewModelProvider provider = new androidx.lifecycle.ViewModelProvider(this);
                    com.example.ncdscreener.viewmodel.PatientViewModel patientViewModel = provider.get(com.example.ncdscreener.viewmodel.PatientViewModel.class);
                    patientViewModel.selectPatient(0); // Clear selection so user chooses in form
                    navController.navigate(R.id.screeningFormFragment);
                }
            }
        });
        
        builder.setNegativeButton(getString(R.string.cancel), null);
        builder.show();
    }
}

