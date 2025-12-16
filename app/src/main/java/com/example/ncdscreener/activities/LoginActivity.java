package com.example.ncdscreener.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ncdscreener.R;
import com.example.ncdscreener.model.CHW;
import com.example.ncdscreener.repository.CHWRepository;
import com.example.ncdscreener.services.SyncManager;
import com.example.ncdscreener.utils.LocaleHelper;
import com.example.ncdscreener.utils.SessionManager;
import com.example.ncdscreener.utils.ThemeManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Login Activity for Community Health Worker authentication
 */
public class LoginActivity extends AppCompatActivity {

    private TextInputEditText editUsername;
    private TextInputEditText editPassword;
    private MaterialButton buttonLogin;
    private MaterialButton buttonCreateAccount;
    private MaterialButton buttonForgotPassword;
    private SyncManager syncManager;
    private CHWRepository chwRepository;
    private SessionManager sessionManager;

    @Override
    protected void attachBaseContext(Context newBase) {
        // Wrap base context with saved locale
        LocaleHelper localeHelper = new LocaleHelper(newBase);
        Context context = localeHelper.wrapContext(newBase);
        super.attachBaseContext(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before super to reduce flicker
        new ThemeManager(this).applySavedTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Check if already logged in
        sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            navigateToMain();
            return;
        }

        editUsername = findViewById(R.id.edit_username);
        editPassword = findViewById(R.id.edit_password);
        buttonLogin = findViewById(R.id.button_login);
        buttonCreateAccount = findViewById(R.id.button_create_account);
        buttonForgotPassword = findViewById(R.id.button_forgot_password);
        syncManager = new SyncManager(this);
        chwRepository = new CHWRepository(this);
        
        // Initialize default CHW for demo
        initializeDefaultCHW();

        buttonLogin.setOnClickListener(v -> performLogin());
        buttonCreateAccount.setOnClickListener(v -> showCreateAccountDialog());
        buttonForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
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
                // Save login state using SessionManager
                sessionManager.createLoginSession(
                    chw.getChwId(),
                    chw.getUsername(),
                    chw.getFirstName(),
                    chw.getLastName(),
                    chw.getFullName()
                );
                
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
     * Navigates to MainActivity
     */
    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showCreateAccountDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_account, null);
        TextInputEditText inputUsername = dialogView.findViewById(R.id.input_username);
        TextInputEditText inputFirstName = dialogView.findViewById(R.id.input_first_name);
        TextInputEditText inputLastName = dialogView.findViewById(R.id.input_last_name);
        TextInputEditText inputPhone = dialogView.findViewById(R.id.input_phone);
        TextInputEditText inputPassword = dialogView.findViewById(R.id.input_password);
        TextInputEditText inputConfirm = dialogView.findViewById(R.id.input_confirm_password);

        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.create_account)
            .setView(dialogView)
            .setPositiveButton(R.string.create_account, (dialog, which) -> {
                String username = getTextOrEmpty(inputUsername);
                String first = getTextOrEmpty(inputFirstName);
                String last = getTextOrEmpty(inputLastName);
                String phone = getTextOrEmpty(inputPhone);
                String pass = getTextOrEmpty(inputPassword);
                String confirm = getTextOrEmpty(inputConfirm);

                if (username.isEmpty() || first.isEmpty() || last.isEmpty() || pass.isEmpty()) {
                    Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!pass.equals(confirm)) {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                CHW newChw = new CHW();
                newChw.setUsername(username);
                newChw.setFirstName(first);
                newChw.setLastName(last);
                newChw.setPhoneNumber(phone);
                newChw.setPassword(pass); // NOTE: For production, hash the password before storing

                chwRepository.createCHW(newChw, (success, message) -> {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    if (success) {
                        editUsername.setText(username);
                        editPassword.setText(pass);
                    }
                });
            })
            .setNegativeButton(android.R.string.cancel, null)
            .show();
    }

    private void showForgotPasswordDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_forgot_password, null);
        TextInputEditText inputUsername = dialogView.findViewById(R.id.input_reset_username);
        TextInputEditText inputPassword = dialogView.findViewById(R.id.input_reset_password);
        TextInputEditText inputConfirm = dialogView.findViewById(R.id.input_reset_confirm_password);

        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.reset_password)
            .setView(dialogView)
            .setPositiveButton(R.string.reset_password, (dialog, which) -> {
                String username = getTextOrEmpty(inputUsername);
                String pass = getTextOrEmpty(inputPassword);
                String confirm = getTextOrEmpty(inputConfirm);

                if (username.isEmpty() || pass.isEmpty()) {
                    Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!pass.equals(confirm)) {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                chwRepository.resetPassword(username, pass, (success, message) -> {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    if (success) {
                        editUsername.setText(username);
                        editPassword.setText(pass);
                    }
                });
            })
            .setNegativeButton(android.R.string.cancel, null)
            .show();
    }

    private String getTextOrEmpty(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }
}

