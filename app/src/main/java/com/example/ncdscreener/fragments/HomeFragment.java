package com.example.ncdscreener.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.ncdscreener.R;

public class HomeFragment extends Fragment {

    private CardView cardNewScreening;
    private CardView cardPatientList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cardNewScreening = view.findViewById(R.id.card_new_screening);
        cardPatientList = view.findViewById(R.id.card_patient_list);

        cardNewScreening.setOnClickListener(v -> {
            // Always navigate to patient list first to select a patient
            Navigation.findNavController(v).navigate(R.id.action_home_to_patient_list);
            android.widget.Toast.makeText(getContext(), "Please select a patient to screen", android.widget.Toast.LENGTH_SHORT).show();
        });

        cardPatientList.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_home_to_patient_list);
        });
    }
}

