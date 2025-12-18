package com.example.ncdscreener.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ncdscreener.R;
import com.example.ncdscreener.activities.LoginActivity;
import com.example.ncdscreener.utils.SessionManager;
import com.example.ncdscreener.utils.ToastHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

/**
 * Profile/Settings Fragment
 * Displays user information and provides logout functionality
 */
public class ProfileFragment extends Fragment {

    private TextView textChwName;
    private TextView textChwUsername;
    private TextView textSessionTime;
    private MaterialButton buttonLogout;
    private MaterialCardView cardProfile;
    private SessionManager sessionManager;
    private Handler handler;
    private Runnable sessionUpdateRunnable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        handler = new Handler(Looper.getMainLooper());

        textChwName = view.findViewById(R.id.text_chw_name);
        textChwUsername = view.findViewById(R.id.text_chw_username);
        textSessionTime = view.findViewById(R.id.text_session_time);
        buttonLogout = view.findViewById(R.id.button_logout);
        cardProfile = view.findViewById(R.id.card_profile);

        // Display user information
        displayUserInfo();

        // Setup logout button
        buttonLogout.setOnClickListener(v -> showLogoutConfirmation());

        // Start session time updater
        startSessionTimeUpdater();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Check if session is still valid
        if (!sessionManager.isLoggedIn()) {
            navigateToLogin();
        } else {
            // Refresh session on resume (user is active)
            sessionManager.refreshSession();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopSessionTimeUpdater();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopSessionTimeUpdater();
    }

    private void displayUserInfo() {
        String chwName = sessionManager.getChwName();
        String username = sessionManager.getChwUsername();

        textChwName.setText(chwName != null && !chwName.isEmpty() ? chwName : "Unknown User");
        textChwUsername.setText("Username: " + (username != null && !username.isEmpty() ? username : "N/A"));
        
        updateSessionTime();
    }

    private void updateSessionTime() {
        if (sessionManager.isLoggedIn()) {
            String timeRemaining = sessionManager.getRemainingSessionTimeFormatted();
            textSessionTime.setText("Session expires in: " + timeRemaining);
            
            // Change color if session is expiring soon
            if (sessionManager.isSessionExpiringSoon()) {
                textSessionTime.setTextColor(getResources().getColor(android.R.color.holo_red_dark, null));
            } else {
                textSessionTime.setTextColor(getResources().getColor(android.R.color.darker_gray, null));
            }
        } else {
            textSessionTime.setText("Session expired");
            textSessionTime.setTextColor(getResources().getColor(android.R.color.holo_red_dark, null));
        }
    }

    private void startSessionTimeUpdater() {
        sessionUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                if (sessionManager.isLoggedIn()) {
                    updateSessionTime();
                    handler.postDelayed(this, 1000); // Update every second
                } else {
                    // Session expired, navigate to login
                    navigateToLogin();
                }
            }
        };
        handler.post(sessionUpdateRunnable);
    }

    private void stopSessionTimeUpdater() {
        if (sessionUpdateRunnable != null) {
            handler.removeCallbacks(sessionUpdateRunnable);
        }
    }

    private void showLogoutConfirmation() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout", (dialog, which) -> performLogout())
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void performLogout() {
        sessionManager.logout();
        ToastHelper.showSuccess(getContext(), "You have been logged out successfully");
        navigateToLogin();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}

