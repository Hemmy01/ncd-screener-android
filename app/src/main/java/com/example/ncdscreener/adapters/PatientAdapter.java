package com.example.ncdscreener.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ncdscreener.R;
import com.example.ncdscreener.model.Patient;

import java.util.List;

/**
 * RecyclerView Adapter for displaying patient list
 */
public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.PatientViewHolder> {

    private List<Patient> patients;
    private OnPatientClickListener listener;

    public interface OnPatientClickListener {
        void onPatientClick(Patient patient);
    }

    public PatientAdapter(List<Patient> patients) {
        this.patients = patients;
    }

    public void setOnPatientClickListener(OnPatientClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_patient, parent, false);
        return new PatientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PatientViewHolder holder, int position) {
        if (patients != null && position < patients.size()) {
            Patient patient = patients.get(position);
            holder.bind(patient);
        } else {
            holder.bind(null);
        }
    }

    @Override
    public int getItemCount() {
        return patients != null ? patients.size() : 0;
    }

    public void updatePatients(List<Patient> newPatients) {
        this.patients = newPatients;
        notifyDataSetChanged();
    }

    class PatientViewHolder extends RecyclerView.ViewHolder {
        private TextView textPatientName;
        private TextView textPatientId;
        private TextView textPatientPhone;

        public PatientViewHolder(@NonNull View itemView) {
            super(itemView);
            textPatientName = itemView.findViewById(R.id.text_patient_name);
            textPatientId = itemView.findViewById(R.id.text_patient_id);
            textPatientPhone = itemView.findViewById(R.id.text_patient_phone);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && patients != null) {
                    Patient patient = patients.get(position);
                    // Navigate to patient detail using ViewModel to pass patient
                    // This works because PatientViewModel is shared across fragments
                    if (listener != null) {
                        listener.onPatientClick(patient);
                    }
                }
            });
        }

        public void bind(Patient patient) {
            if (patient == null) {
                textPatientName.setText("Unknown Patient");
                textPatientId.setText("ID: N/A");
                textPatientPhone.setText("Phone: N/A");
                return;
            }
            
            String fullName = patient.getFullName();
            textPatientName.setText(fullName != null && !fullName.isEmpty() ? fullName : "Unknown Patient");
            
            int nationalId = patient.getNationalId();
            textPatientId.setText("ID: " + (nationalId > 0 ? String.valueOf(nationalId) : "N/A"));
            
            String phone = patient.getPhoneNumber();
            textPatientPhone.setText("Phone: " + (phone != null && !phone.isEmpty() ? phone : "N/A"));
        }
    }
}

