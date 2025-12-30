package com.example.ncdscreener.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.ncdscreener.R;
import com.example.ncdscreener.database.NCDScreenerDatabase;
import com.example.ncdscreener.database.dao.PatientDao;
import com.example.ncdscreener.database.dao.ScreeningDao;
import com.example.ncdscreener.database.entity.ScreeningEntity;
import com.example.ncdscreener.utils.SessionManager;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Home Fragment - Beautiful dashboard with statistics and quick actions
 */
public class HomeFragment extends Fragment {

    private MaterialCardView cardNewScreening;
    private MaterialCardView cardPatientList;
    private MaterialCardView cardTotalPatients;
    private MaterialCardView cardTotalScreenings;
    private MaterialCardView cardTodayScreenings;
    
    private TextView textWelcome;
    private TextView textChwNameHeader;
    private TextView textTotalPatients;
    private TextView textTotalScreenings;
    private TextView textTodayScreenings;
    private LinearLayout layoutRecentActivity;
    private TextView textNoRecentActivity;
    
    private PatientDao patientDao;
    private ScreeningDao screeningDao;
    private ExecutorService executorService;
    private SessionManager sessionManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize components
        initializeViews(view);
        initializeDatabase();
        setupClickListeners();
        loadStatistics();
        loadRecentActivity();
        displayWelcomeMessage();
    }

    private void initializeViews(View view) {
        cardNewScreening = view.findViewById(R.id.card_new_screening);
        cardPatientList = view.findViewById(R.id.card_patient_list);
        cardTotalPatients = view.findViewById(R.id.card_total_patients);
        cardTotalScreenings = view.findViewById(R.id.card_total_screenings);
        cardTodayScreenings = view.findViewById(R.id.card_today_screenings);
        
        textWelcome = view.findViewById(R.id.text_welcome);
        textChwNameHeader = view.findViewById(R.id.text_chw_name_header);
        textTotalPatients = view.findViewById(R.id.text_total_patients);
        textTotalScreenings = view.findViewById(R.id.text_total_screenings);
        textTodayScreenings = view.findViewById(R.id.text_today_screenings);
        layoutRecentActivity = view.findViewById(R.id.layout_recent_activity);
        textNoRecentActivity = view.findViewById(R.id.text_no_recent_activity);
        
        sessionManager = new SessionManager(requireContext());
        executorService = Executors.newSingleThreadExecutor();
    }

    private void initializeDatabase() {
        NCDScreenerDatabase database = NCDScreenerDatabase.getDatabase(requireContext());
        patientDao = database.patientDao();
        screeningDao = database.screeningDao();
    }

    private void setupClickListeners() {
        // Header buttons
        View profileBtn = requireView().findViewById(R.id.button_profile);
        if (profileBtn != null) {
            profileBtn.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_home_to_profile));
        }
        View settingsBtn = requireView().findViewById(R.id.button_settings);
        if (settingsBtn != null) {
            settingsBtn.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_home_to_settings));
        }

        // New Screening card - goes to screening list
        cardNewScreening.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_home_to_screening_list);
        });

        // Patient List card - goes to patient list
        cardPatientList.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_home_to_patient_list));
        
        // Statistics cards navigation - patients go to patient list, screenings go to screening list
        cardTotalPatients.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_home_to_patient_list));
        
        cardTotalScreenings.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_home_to_screening_list));
        
        // Today's Screenings - navigate with filter parameter to show only today's screenings
        cardTodayScreenings.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putBoolean("filter_today_only", true);
            Navigation.findNavController(v).navigate(R.id.action_home_to_screening_list, args);
        });
    }

    private void displayWelcomeMessage() {
        String chwName = sessionManager.getChwName();
        if (chwName != null && !chwName.isEmpty()) {
            textChwNameHeader.setText(chwName);
        } else {
            textChwNameHeader.setText(getString(R.string.community_health_worker));
        }
        
        // Set welcome message based on time of day
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int hour = cal.get(java.util.Calendar.HOUR_OF_DAY);
        String greeting;
        if (hour < 12) {
            greeting = getString(R.string.good_morning);
        } else if (hour < 17) {
            greeting = getString(R.string.good_afternoon);
        } else {
            greeting = getString(R.string.good_evening);
        }
        textWelcome.setText(greeting);
    }

    private void loadStatistics() {
        executorService.execute(() -> {
            try {
                final int patientCount = patientDao.getPatientCount();
                final int screeningCount = screeningDao.getScreeningCount();
                
                // Calculate today's screenings
                List<ScreeningEntity> allScreenings = screeningDao.getRecentScreenings(1000); // Get many to filter
                final long todayStart = getTodayStartTimestamp();
                int todayCount = 0;
                for (ScreeningEntity screening : allScreenings) {
                    if (screening.getScreeningDate() >= todayStart) {
                        todayCount++;
                    }
                }
                final int finalTodayCount = todayCount;
                
                // Update UI on main thread
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        textTotalPatients.setText(String.valueOf(patientCount));
                        textTotalScreenings.setText(String.valueOf(screeningCount));
                        textTodayScreenings.setText(String.valueOf(finalTodayCount));
                        
                        // Add animation
                        animateCount(textTotalPatients);
                        animateCount(textTotalScreenings);
                        animateCount(textTodayScreenings);
                    });
                }
            } catch (Exception e) {
                android.util.Log.e("HomeFragment", "Error loading statistics", e);
            }
        });
    }
    
    private long getTodayStartTimestamp() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private void animateCount(TextView textView) {
        // Simple fade-in animation
        textView.setAlpha(0f);
        textView.animate()
            .alpha(1f)
            .setDuration(500)
            .start();
    }

    private void loadRecentActivity() {
        executorService.execute(() -> {
            try {
                List<ScreeningEntity> recentScreenings = screeningDao.getRecentScreenings(3);
                
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> displayRecentActivity(recentScreenings));
                }
            } catch (Exception e) {
                android.util.Log.e("HomeFragment", "Error loading recent activity", e);
            }
        });
    }

    private void displayRecentActivity(List<ScreeningEntity> screenings) {
        layoutRecentActivity.removeAllViews();
        
        if (screenings == null || screenings.isEmpty()) {
            textNoRecentActivity.setVisibility(View.VISIBLE);
            return;
        }
        
        textNoRecentActivity.setVisibility(View.GONE);
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault());
        
        for (ScreeningEntity screening : screenings) {
            View itemView = createRecentActivityItem(screening, dateFormat);
            layoutRecentActivity.addView(itemView);
        }
    }

    private View createRecentActivityItem(ScreeningEntity screening, SimpleDateFormat dateFormat) {
        View itemView = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_recent_activity, layoutRecentActivity, false);
        
        TextView textDate = itemView.findViewById(R.id.text_activity_date);
        TextView textDescription = itemView.findViewById(R.id.text_activity_description);
        
        String dateStr = dateFormat.format(new Date(screening.getScreeningDate()));
        textDate.setText(dateStr);
        
        String description = "Screening #" + screening.getScreeningId();
        if (screening.getLocation() != null && !screening.getLocation().isEmpty()) {
            description += " • " + screening.getLocation();
        }
        textDescription.setText(description);
        
        // Add click listener to navigate to screening detail
        itemView.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putInt("screening_id", screening.getScreeningId());
            Navigation.findNavController(v).navigate(R.id.screeningDetailFragment, args);
        });
        
        return itemView;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh statistics when fragment is resumed
        loadStatistics();
        loadRecentActivity();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
