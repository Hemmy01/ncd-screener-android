package com.example.ncdscreener.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ncdscreener.R;
import com.example.ncdscreener.model.CHW;
import com.example.ncdscreener.repository.CHWRepository;
import com.example.ncdscreener.services.SyncManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Login Activity for Community Health Worker authentication
 */
public class LoginActivity extends AppCompatActivity {

    private TextInputEditText editUsername;
    private TextInputEditText editPassword;
    private MaterialButton buttonLogin;
    private SharedPreferences sharedPreferences;
    private SyncManager syncManager;
    private CHWRepository chwRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Check if already logged in
        sharedPreferences = getSharedPreferences("NCDScreenerPrefs", MODE_PRIVATE);
        if (isLoggedIn()) {
            navigateToMain();
            return;
        }

        editUsername = findViewById(R.id.edit_username);
        editPassword = findViewById(R.id.edit_password);
        buttonLogin = findViewById(R.id.button_login);
        syncManager = new SyncManager(this);
        chwRepository = new CHWRepository(this);
        
        // Initialize default CHW for demo
        initializeDefaultCHW();

        buttonLogin.setOnClickListener(v -> performLogin());
    }

    /**
     * Performs login authentication
     */
    private void performLogin() {
        String username = editUsername.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Authenticate CHW (async operation)
        authenticateCHW(username, password);
    }

    /**
     * Initializes default CHW for first-time setup
     * Creates a default CHW account if none exists
     */
    private void initializeDefaultCHW() {
        // Check if default CHW exists, if not create it
        chwRepository.getCHWByUsername("chw", existingCHW -> {
            if (existingCHW == null) {
                CHW defaultCHW = new CHW();
                defaultCHW.setChwId(1);
                defaultCHW.setUsername("chw");
                defaultCHW.setFirstName("Community");
                defaultCHW.setLastName("Health Worker");
                defaultCHW.setPhoneNumber("+1234567890");
                defaultCHW.setPassword("password");
                chwRepository.saveCHW(defaultCHW);
            }
        });
    }

    /**
     * Authenticates CHW credentials using repository
     */
    private void authenticateCHW(String username, String password) {
        chwRepository.authenticate(username, password, chw -> {
            if (chw != null) {
                // Save login state
                saveLoginState(chw);
                
                // Start sync service
                syncManager.schedulePeriodicSync();
                
                // Navigate to main activity
                navigateToMain();
            } else {
                Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Saves login state to SharedPreferences
     */
    private void saveLoginState(CHW chw) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putInt("chwId", chw.getChwId());
        editor.putString("chwUsername", chw.getUsername());
        editor.putString("chwFirstName", chw.getFirstName());
        editor.putString("chwLastName", chw.getLastName());
        editor.putString("chwName", chw.getFullName());
        editor.apply();
    }

    /**
     * Checks if user is already logged in
     */
    private boolean isLoggedIn() {
        return sharedPreferences.getBoolean("isLoggedIn", false);
    }

    /**
     * Navigates to MainActivity
     */
    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

