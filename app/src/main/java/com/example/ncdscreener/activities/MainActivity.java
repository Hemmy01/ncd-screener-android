package com.example.ncdscreener.activities;

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
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private MaterialToolbar toolbar;
    private FloatingActionButton fabNewScreening;
    private SharedPreferences sharedPreferences;
    private SyncManager syncManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Check authentication
        sharedPreferences = getSharedPreferences("NCDScreenerPrefs", MODE_PRIVATE);
        if (!isLoggedIn()) {
            navigateToLogin();
            return;
        }
        
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        // Initialize sync manager
        syncManager = new SyncManager(this);
        syncManager.schedulePeriodicSync();

        // Setup toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

    /**
     * Checks if user is logged in
     */
    private boolean isLoggedIn() {
        return sharedPreferences.getBoolean("isLoggedIn", false);
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
     * Shows dialog with options: Add Patient or New Screening
     */
    private void showFabOptionsDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Choose an action");
        
        String[] options = {"Add New Patient", "New Screening"};
        builder.setItems(options, (dialog, which) -> {
            if (navController != null) {
                if (which == 0) {
                    // Add New Patient - navigate to patient list, then to register patient
                    androidx.lifecycle.ViewModelProvider provider = new androidx.lifecycle.ViewModelProvider(this);
                    com.example.ncdscreener.viewmodel.PatientViewModel patientViewModel = provider.get(com.example.ncdscreener.viewmodel.PatientViewModel.class);
                    patientViewModel.selectPatient(0); // Clear selection
                    navController.navigate(R.id.patientListFragment);
                    // Navigate to register patient after navigation completes
                    findViewById(R.id.nav_host_fragment).postDelayed(() -> {
                        if (navController != null && navController.getCurrentDestination() != null) {
                            try {
                                navController.navigate(R.id.registerPatientFragment);
                            } catch (Exception e) {
                                android.util.Log.e("MainActivity", "Navigation error", e);
                            }
                        }
                    }, 500);
                } else if (which == 1) {
                    // New Screening - navigate to patient list to select patient first
                    navController.navigate(R.id.patientListFragment);
                    android.widget.Toast.makeText(this, "Please select a patient to screen", android.widget.Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}

