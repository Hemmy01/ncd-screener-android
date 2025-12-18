package com.example.ncdscreener.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ncdscreener.R;
import com.example.ncdscreener.model.Observation;

import java.util.List;

/**
 * RecyclerView Adapter for displaying observations
 */
public class ObservationAdapter extends RecyclerView.Adapter<ObservationAdapter.ObservationViewHolder> {

    private List<Observation> observations;

    public ObservationAdapter(List<Observation> observations) {
        this.observations = observations;
    }

    @NonNull
    @Override
    public ObservationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_observation, parent, false);
        return new ObservationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ObservationViewHolder holder, int position) {
        Observation observation = observations.get(position);
        holder.bind(observation);
    }

    @Override
    public int getItemCount() {
        return observations != null ? observations.size() : 0;
    }

    public void updateObservations(List<Observation> newObservations) {
        this.observations = newObservations;
        notifyDataSetChanged();
    }

    class ObservationViewHolder extends RecyclerView.ViewHolder {
        private TextView textObservationType;
        private TextView textObservationValue;
        private TextView textRiskScore;

        public ObservationViewHolder(@NonNull View itemView) {
            super(itemView);
            textObservationType = itemView.findViewById(R.id.text_observation_type);
            textObservationValue = itemView.findViewById(R.id.text_observation_value);
            textRiskScore = itemView.findViewById(R.id.text_risk_score);
        }

        public void bind(Observation observation) {
            String type = observation.getObservationType();
            String displayType = formatObservationType(type);
            textObservationType.setText(displayType);
            
            String value = String.format("%.1f %s", observation.getValue(), observation.getUnit());
            textObservationValue.setText(value);
            
            if (observation.getFinalRiskScore() != null && !observation.getFinalRiskScore().isEmpty()) {
                textRiskScore.setText("Risk: " + observation.getFinalRiskScore());
                textRiskScore.setVisibility(View.VISIBLE);
            } else {
                textRiskScore.setVisibility(View.GONE);
            }
        }

        private String formatObservationType(String type) {
            switch (type) {
                case "blood_pressure_systolic":
                    return "Systolic BP";
                case "blood_pressure_diastolic":
                    return "Diastolic BP";
                case "glucose":
                    return "Blood Glucose";
                case "bmi":
                    return "BMI";
                default:
                    return type;
            }
        }
    }
}

